/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Zimbra, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Timer;

import org.apache.mina.core.buffer.IoBuffer;
import org.dom4j.DocumentException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.zimbra.common.calendar.WellKnownTimeZones;
import com.zimbra.common.lmtp.SmtpToLmtp;
import com.zimbra.common.localconfig.ConfigException;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.localconfig.LocalConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapTransport;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.AuthTokenRegistry;
import com.zimbra.cs.account.AutoProvisionThread;
import com.zimbra.cs.account.ExternalAccountManagerTask;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.Versions;
import com.zimbra.cs.extension.ExtensionManager;
import com.zimbra.cs.index.IndexStore;
import com.zimbra.cs.index.IndexingService;
import com.zimbra.cs.index.solr.SolrCloudIndex;
import com.zimbra.cs.index.solr.SolrIndex;
import com.zimbra.cs.iochannel.MessageChannel;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.PurgeThread;
import com.zimbra.cs.mailbox.ScheduledTaskManager;
import com.zimbra.cs.mailbox.acl.AclPushTask;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.server.ServerManager;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.cs.session.SessionCache;
import com.zimbra.cs.session.WaitSetMgr;
import com.zimbra.cs.stats.ZimbraPerf;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.znative.Util;

/**
 * Class that encapsulates the initialization and shutdown of services needed
 * by any process that adds mail items.  Services under control include redo
 * logging and indexing.
 */
public final class Zimbra {
    private static boolean inited = false;
    private static boolean isMailboxd = false;
    private static boolean minimal = false;

    /** ApplicationContext, cast as an Object so that this class can be used by webapps that don't classload Spring yet */
    private static Object appContext = null;

    /** Sets system properties before the server fully starts up.  Note that
     *  there's a potential race condition if {@link FirstServlet} or another
     *  servlet faults in classes or references properties before they're set
     *  here. */
    private static void setSystemProperties() {
        System.setProperty("mail.mime.decodetext.strict",       "false");
        System.setProperty("mail.mime.encodefilename",          "true");
        System.setProperty("mail.mime.charset",                 "utf-8");
        System.setProperty("mail.mime.base64.ignoreerrors",     "true");
        System.setProperty("mail.mime.ignoremultipartencoding", "false");
        System.setProperty("mail.mime.multipart.allowempty",    "true");
    }

    private static final String HEAP_DUMP_JAVA_OPTION = "-xx:heapdumppath=";

    private static void validateJavaOptions() throws ServiceException {
        String options = LC.mailboxd_java_options.value();
        if (options != null && options.toLowerCase().indexOf(HEAP_DUMP_JAVA_OPTION) > -1) {
            int start = options.toLowerCase().indexOf(HEAP_DUMP_JAVA_OPTION) + HEAP_DUMP_JAVA_OPTION.length();
            int end = -1;
            for (int i = start; i < options.length(); i++) {
                char c = options.charAt(i);
                if (c == ' ') {
                    end = i;
                    break;
                }
            }
            String path = null;
            if (end > -1) {
                path = options.substring(start, end);
            } else {
                path = options.substring(start);
            }
            try {
                if (path.trim().length() <= 0) {
                    throw new IOException("Heap dump path not specified correctly? mailboxd_java_options="+LC.mailboxd_java_options.value());
                }
                File dir = new File(path);
                FileUtil.ensureDirExists(dir);
                if (!dir.canWrite()) {
                    throw new IOException("Heap dump path not writable: " + path);
                }
            } catch (IOException e) {
                throw ServiceException.FAILURE("Unable to find/create HeapDumpPath", e);
            }
        }
    }

    private static void checkForClass(String clzName, String jarName) {
        try {
            String s = Class.forName(clzName).getName();
            ZimbraLog.misc.debug("checked for class " + s + " and found it");
        } catch (ClassNotFoundException cnfe) {
            ZimbraLog.misc.error(jarName + " not in your common/lib?", cnfe);
        } catch (UnsatisfiedLinkError ule) {
            ZimbraLog.misc.error("error in shared library used by " + jarName + "?", ule);
        }
    }

    private static String getSysProperty(String prop) {
        try {
            return System.getProperty(prop);
        } catch (SecurityException e) {
            return "(accessing " + prop + " is not allowed by security manager)";
        }
    }

    public static ApplicationContext getAppContext() {
        return (ApplicationContext)appContext;
    }

    private static void logVersionAndSysInfo() {
        ZimbraLog.misc.info("version=" + BuildInfo.VERSION +
                " release=" + BuildInfo.RELEASE +
                " builddate=" + BuildInfo.DATE +
                " buildhost=" + BuildInfo.HOST);

        ZimbraLog.misc.info("LANG environment is set to: " + System.getenv("LANG"));

        ZimbraLog.misc.info("System property java.home="            + getSysProperty("java.home"));
        ZimbraLog.misc.info("System property java.runtime.version=" + getSysProperty("java.runtime.version"));
        ZimbraLog.misc.info("System property java.version="         + getSysProperty("java.version"));
        ZimbraLog.misc.info("System property java.vm.info="         + getSysProperty("java.vm.info"));
        ZimbraLog.misc.info("System property java.vm.name="         + getSysProperty("java.vm.name"));
        ZimbraLog.misc.info("System property java.vm.version="      + getSysProperty("java.vm.version"));
        ZimbraLog.misc.info("System property os.arch="              + getSysProperty("os.arch"));
        ZimbraLog.misc.info("System property os.name="              + getSysProperty("os.name"));
        ZimbraLog.misc.info("System property os.version="           + getSysProperty("os.version"));
        ZimbraLog.misc.info("System property sun.arch.data.model="  + getSysProperty("sun.arch.data.model"));
        ZimbraLog.misc.info("System property sun.cpu.endian="       + getSysProperty("sun.cpu.endian"));
        ZimbraLog.misc.info("System property sun.cpu.isalist="      + getSysProperty("sun.cpu.isalist"));
        ZimbraLog.misc.info("System property sun.os.patch.level="   + getSysProperty("sun.os.patch.level"));
    }

    private static void checkForClasses() {
        checkForClass("javax.activation.DataSource", "activation.jar");
        checkForClass("javax.mail.internet.MimeMessage", "javamail-1.4.3.jar");
        checkForClass("com.zimbra.znative.IO", "zimbra-native.jar");
    }

    public static void startup() {
        try {
            startup(true);
        } catch (ServiceException se) {
            Zimbra.halt("Exception during startup, aborting server, please check your config", se);
        }
    }

    public static void startupCLI() throws ServiceException {
        startup(false);
    }

    /** Perform minimal startup with default config class */
    public static void startupMinimal() throws ServiceException {
        minimal = true;
        initAppContext();
    }

    /** Perform minimal start-up with supplied config classes */
    public static void startupMinimal(Class<?> ...configClasses) throws ServiceException {
        minimal = true;
        initAppContext(configClasses);
    }

    private static void initAppContext() {
        initAppContext(ZimbraConfig.class);
    }

    public static void initAppContext(Class<?> ...configClasses) {
        AnnotationConfigApplicationContext appContext_ = new AnnotationConfigApplicationContext();
        appContext = appContext_; // required before refresh() below, for @PostConstruct handlers that do their own wiring
        appContext_.register(configClasses);
        appContext_.refresh();
    }

    /**
     * Initialize the various subsystems at server/CLI startup time.
     * @param forMailboxd true if this is the mailboxd process; false for CLI processes
     * @throws ServiceException
     */
    private static synchronized void startup(boolean forMailboxd, Class<?> ...configClasses) throws ServiceException {
        if (inited)
            return;

        isMailboxd = forMailboxd;
        if (isMailboxd) {
            FirstServlet.waitForInitialization();
        }

        setSystemProperties();
        validateJavaOptions();

        logVersionAndSysInfo();

        SoapTransport.setDefaultUserAgent("ZCS", BuildInfo.VERSION);

        checkForClasses();

        DbPool.startup();

        // Initialize a Spring ApplicationContext. Objects initialized before this point
        // are not (yet) managed by Spring.
        if (configClasses == null || configClasses.length == 0) {
            initAppContext();
        } else {
            initAppContext(configClasses);
        }

        ZimbraApplication app = ZimbraApplication.getInstance();
        app.initializeZimbraDb(forMailboxd);


        if (!Versions.checkVersions()) {
            Zimbra.halt("Data version mismatch.  Reinitialize or upgrade the backend data store.");
        }

        DbPool.loadSettings();

        String tzFilePath = LC.timezone_file.value();
        try {
            File tzFile = new File(tzFilePath);
            WellKnownTimeZones.loadFromFile(tzFile);
        } catch (Throwable t) {
            Zimbra.halt("Unable to load timezones from " + tzFilePath, t);
        }

        Provisioning prov = Provisioning.getInstance();
        if (prov instanceof LdapProv) {
            ((LdapProv) prov).waitForLdapServer();
            if (forMailboxd) {
                AttributeManager.loadLdapSchemaExtensionAttrs((LdapProv) prov);
            }
        }

        Server localServer = prov.getLocalServer();
        if (localServer.isMailSSLClientCertOCSPEnabled()) {
            // Activate OCSP
            Security.setProperty("ocsp.enable", "true");
            // Activate CRLDP
            System.setProperty("com.sun.security.enableCRLDP", "true");
        }else {
            // Disable OCSP
            Security.setProperty("ocsp.enable", "false");
            // Disable CRLDP
            System.setProperty("com.sun.security.enableCRLDP", "false");
        }

        try {
            RightManager.getInstance();
        } catch (ServiceException e) {
            Util.halt("cannot initialize RightManager", e);
        }

        //register internal IndexStore implementations before initializing extensions
        //this way extensions can overwrite them if needed
        IndexStore.registerIndexFactory("solr", SolrIndex.Factory.class.getName());
        IndexStore.registerIndexFactory("solrcloud", SolrCloudIndex.Factory.class.getName());

        ExtensionManager.getInstance().initAll();

        try {
            StoreManager.getInstance().startup();
        } catch (IOException e) {
            throw ServiceException.FAILURE("Unable to initialize StoreManager.", e);
        }

        app.startup();

        if (app.supports(EhcacheManager.class.getName())) {
            EhcacheManager.getInstance().startup();
        }

        // ZimletUtil.loadZimlets();

        //start indexing service before server manager
        ((ApplicationContext)appContext).getBean(IndexingService.class).startUp();

        RedoLogProvider redoLog = RedoLogProvider.getInstance();
        if (isMailboxd) {
            redoLog.startup(true);
        } else {
            redoLog.initRedoLogManager();
        }

        System.setProperty("ical4j.unfolding.relaxed", "true");

        MailboxManager.getInstance().startup();

        app.initialize(isMailboxd);
        if (isMailboxd) {
            SessionCache.startup();
            AuthTokenRegistry.startup(prov.getConfig(Provisioning.A_zimbraAuthTokenNotificationInterval).getIntAttr(Provisioning.A_zimbraAuthTokenNotificationInterval, 60000));
            dbSessionCleanup();

            if (!redoLog.isSlave()) {
                boolean useDirectBuffers = localServer.isMailUseDirectBuffers();
                IoBuffer.setUseDirectBuffer(useDirectBuffers);
                ZimbraLog.misc.info("MINA setUseDirectBuffers(" + useDirectBuffers + ")");

                ServerManager.getInstance().startServers();
            }

            if (app.supports(WaitSetMgr.class.getName())) {
                WaitSetMgr.startup();
            }

            if (app.supports(MemoryStats.class.getName())) {
                MemoryStats.startup();
            }

            if (app.supports(ScheduledTaskManager.class.getName())) {
                ScheduledTaskManager.startup();
            }

            if (app.supports(PurgeThread.class.getName())) {
                PurgeThread.startup();
            }

            if (app.supports(AutoProvisionThread.class.getName())) {
                AutoProvisionThread.switchAutoProvThreadIfNecessary();
            }

            ZimbraLog.misc.info("SmtpToLmtpEnabled : " + prov.getLocalServer().isMailboxSmtpToLmtpEnabled());
            if (prov.getLocalServer().isMailboxSmtpToLmtpEnabled()) {
                int smtpPort = prov.getLocalServer().getMailboxSmtpToLmtpPort();
                int lmtpPort = prov.getLocalServer().getLmtpBindPort();
                SmtpToLmtp smtpServer = SmtpToLmtp.startup(smtpPort, "localhost", lmtpPort);
                smtpServer.setRecipientValidator(new SmtpRecipientValidator());
            }

            if (app.supports(AclPushTask.class)) {
                long pushInterval = localServer.getSharingUpdatePublishInterval();
                sTimer.schedule(new AclPushTask(), pushInterval, pushInterval);
            }

            if (app.supports(ExternalAccountManagerTask.class)) {
                long interval = localServer.getExternalAccountStatusCheckInterval();
                sTimer.schedule(new ExternalAccountManagerTask(), interval, interval);
            }

            if (prov.getLocalServer().isMessageChannelEnabled()) {
                try {
                    MessageChannel.getInstance().startup();
                } catch (IOException e) {
                    ZimbraLog.misc.warn("can't start notification channels", e);
                }
            }

            String provPort = localServer.getAttr(Provisioning.A_zimbraMailPort);
            String lcPort = LC.zimbra_mail_service_port.value();
            if (!lcPort.equals(provPort)) {
                LocalConfig lc;
                try {
                    lc = new LocalConfig(null);
                    lc.set(LC.zimbra_mail_service_port.key(), provPort);
                    lc.save();
                    LC.reload();
                } catch (DocumentException | ConfigException | IOException e) {
                    ZimbraLog.misc.warn("Cannot set LC zimbra_mail_service_port", e);
                }
            }


            // should be last, so that other subsystems can add dynamic stats counters
            if (app.supports(ZimbraPerf.class.getName())) {
                ZimbraPerf.initialize();
            }
        }

        ExtensionManager.getInstance().postInitAll();

        inited = true;
    }

    public static synchronized void shutdown() throws ServiceException {
        if (minimal) {
            closeAppContext();
            return;
        } else if (!inited) {
            return;
        }

        inited = false;

        if (isMailboxd) {
            PurgeThread.shutdown();
            AutoProvisionThread.shutdown();
        }

        ZimbraApplication app = ZimbraApplication.getInstance();

        app.shutdown();


        if (isMailboxd) {
            if (app.supports(MemoryStats.class.getName())) {
                MemoryStats.shutdown();
            }

            if (app.supports(WaitSetMgr.class.getName())) {
                WaitSetMgr.shutdown();
            }
        }

        RedoLogProvider redoLog = RedoLogProvider.getInstance();
        if (isMailboxd) {
            if (!redoLog.isSlave()) {
                ServerManager.getInstance().stopServers();
            }

            dbSessionCleanup();

            SessionCache.shutdown();
        }

        if (isMailboxd) {
            redoLog.shutdown();
        }

        if (app.supports(EhcacheManager.class.getName())) {
            EhcacheManager.getInstance().shutdown();
        }

        if (isMailboxd) {
            StoreManager.getInstance().shutdown();
        }

        sTimer.cancel();

        try {
            DbPool.shutdown();
        } catch (Exception ignored) {
        }

        closeAppContext();
    }

    private static void closeAppContext() throws ServiceException {
        if (Zimbra.getAppContext() instanceof Closeable) {
            try {
                ((Closeable)Zimbra.getAppContext()).close();
            } catch (IOException e) {
                throw ServiceException.FAILURE("Failed closing Spring application context", e);
            }
        }
    }

    public static synchronized boolean started() {
        return inited | minimal;
    }

    public static Timer sTimer = new Timer("Timer-Zimbra", true);

    /**
     * Logs the given message and shuts down the server.
     *
     * @param message the message to log before shutting down
     */
    public static void halt(String message) {
        try {
            ZimbraLog.system.fatal(message);
        } finally {
            Runtime.getRuntime().halt(1);
        }
    }

    /**
     * Logs the given message and shuts down the server.
     *
     * @param message the message to log before shutting down
     * @param t the exception that was thrown
     */
    public static void halt(String message, Throwable t) {
        try {
            ZimbraLog.system.fatal(message, t);
        } finally {
            Runtime.getRuntime().halt(1);
        }
    }

    public static String getAlwaysOnClusterId() {
        Provisioning prov = Provisioning.getInstance();
        Server localServer;
        try {
            localServer = prov.getLocalServer();
            return localServer.getAlwaysOnClusterId();
        } catch (ServiceException e) {
            Zimbra.halt("Exception during getAlwaysOnClusterId, aborting server, please check your logs", e);
        }
        return null;
    }

    public static boolean isAlwaysOn() {
        return getAlwaysOnClusterId() != null;
    }

    private static void dbSessionCleanup() throws ServiceException {
        //DbSessions Cleanup
        DbConnection conn = null;
        try {
            if (isAlwaysOn()) {
                conn = DbPool.getConnection();
                conn.commit();
            }
        } finally {
            if (conn != null)
                conn.closeQuietly();
        }
    }
}
