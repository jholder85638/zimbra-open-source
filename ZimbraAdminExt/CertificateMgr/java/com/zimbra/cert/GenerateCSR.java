/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cert;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.common.base.Strings;
import com.google.common.net.HostSpecifier;
import com.zimbra.common.account.Key.ServerBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.CertMgrConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.cs.rmgmt.RemoteResult;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GenCSRRequest;
import com.zimbra.soap.base.CertSubjectAttrs;

public class GenerateCSR extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        GenCSRRequest req = JaxbUtil.elementToJaxb(request);

        Server server = null;
        String serverId = req.getServer();

        if (serverId != null && serverId.equals(ZimbraCertMgrExt.ALL_SERVERS)) {
            server = prov.getLocalServer() ;
        }else {
            server = prov.get(ServerBy.id, serverId);
        }

        if (server == null) {
            throw ServiceException.INVALID_REQUEST("Server with id " + serverId + " could not be found", null);
        }
        checkRight(lc, context, server, Admin.R_generateCSR);
        ZimbraLog.security.debug("Generate the CSR info from server: %s", server.getName()) ;

        StringBuilder cmd = new StringBuilder(ZimbraCertMgrExt.CREATE_CSR_CMD);
        if ("1".equals(req.getNewCSR())) {
            String keysize = req.getKeySize();
            String type = req.getType();
            if (Strings.isNullOrEmpty(type)) {
                throw ServiceException.INVALID_REQUEST("No valid CSR type is set.", null);
            } else if (type.equals(ZimbraCertMgrExt.CERT_TYPE_SELF) || type.equals(ZimbraCertMgrExt.CERT_TYPE_COMM)) {
                cmd.append(" ").append(type);
            } else {
                throw ServiceException.INVALID_REQUEST(String.format("Invalid CSR type: '%s'. Must be '%s' or '%s' ",
                        type, ZimbraCertMgrExt.CERT_TYPE_SELF, ZimbraCertMgrExt.CERT_TYPE_COMM), null);
            }
            if (keysize != null && !keysize.isEmpty()) {
                try {
                    int iKeySize = Integer.parseInt(keysize);
                    if (iKeySize < 2048) {
                        throw ServiceException.INVALID_REQUEST("Minimum allowed key size is 2048", null);
                    }
                    cmd.append(" -new -keysize ").append(keysize);
                } catch (NumberFormatException nfe) {
                    throw ServiceException.INVALID_REQUEST("Invalid value for parameter " + CertMgrConstants.E_KEYSIZE,
                            nfe);
                }
            }
            String digest = req.getDigest();
            if (digest != null && !digest.isEmpty()) {
                if (!digest.matches("[a-zA-Z0-9]*")) {
                    throw ServiceException.INVALID_REQUEST(String.format("digest '%s' is not valid.", digest), null);
                }
                cmd.append(" -digest ").append(digest);
            }
            appendSubjectArgToCommand(cmd, getSubject(req));

            String subjectAltNames = getSubjectAltNames(req.getSubjectAltNames()) ;
            if (!Strings.isNullOrEmpty(subjectAltNames)) {
                cmd.append(" -subjectAltNames '").append(StringEscapeUtils.escapeJavaScript(subjectAltNames))
                        .append("'");
            }
            RemoteManager rmgr = RemoteManager.getRemoteManager(server);
            ZimbraLog.security.debug("***** Executing the cmd = %s", cmd);
            RemoteResult rr = rmgr.execute(cmd.toString());
            Charset csutf8 = Charset.forName("UTF-8");
            String stdout = (rr.getMStdout() != null) ? new String(rr.getMStdout(), csutf8) : null;
            String stderr = (rr.getMStderr() != null) ? new String(rr.getMStderr(), csutf8) : null;
            if (rr.getMExitStatus() != 0) {
                String errmsg = String.format("Command \"%s\" failed; exit code=%d; stderr=\n%s", cmd,
                        rr.getMExitStatus(), stderr);
                ZimbraLog.security.error(errmsg);
                throw ServiceException.FAILURE(errmsg, null);
            } else {
                ZimbraLog.security.debug(stdout);
            }
        } else {
            ZimbraLog.security.info("No new CSR need to be created.");
        }

        Element response = lc.createElement(CertMgrConstants.GEN_CSR_RESPONSE);
        response.addAttribute(AdminConstants.A_SERVER, server.getName());
        return response;
    }

    public static StringBuilder appendSubjectArgToCommand(StringBuilder cmd, String subject)
    throws ServiceException {
        if (Strings.isNullOrEmpty(subject)) {
            return cmd;
        }
        cmd.append(" -subject '").append(subject).append("'");
        return cmd;
    }

    private static void appendToSubject(StringBuilder subject, String attrName, String attrValue) {
        if (!Strings.isNullOrEmpty(attrValue)) {
            subject.append("/").append(attrName).append("=").append(StringEscapeUtils.escapeJavaScript(attrValue));
        }
    }

    public static String getSubject(CertSubjectAttrs req)
    throws ServiceException {
        StringBuilder subject = new StringBuilder();
        String iso3166_1_alpha2_countryCode = req.getC();
        if (iso3166_1_alpha2_countryCode != null && !iso3166_1_alpha2_countryCode.matches("[a-zA-Z][a-zA-Z]")) {
            throw ServiceException.INVALID_REQUEST(String.format(
                    "Country Code '%s' is not valid.", iso3166_1_alpha2_countryCode), null);
        }
        appendToSubject(subject, CertMgrConstants.E_subjectAttr_C, iso3166_1_alpha2_countryCode);
        appendToSubject(subject, CertMgrConstants.E_subjectAttr_ST, req.getSt());
        appendToSubject(subject, CertMgrConstants.E_subjectAttr_L, req.getL());
        appendToSubject(subject, CertMgrConstants.E_subjectAttr_O, req.getO());
        appendToSubject(subject, CertMgrConstants.E_subjectAttr_OU, req.getOu());
        appendToSubject(subject, CertMgrConstants.E_subjectAttr_CN, req.getCn());
        String subjectString = subject.toString();
        /* Allowed pattern taken from /opt/zimbra/libexec/zmrcd  %REGEX_CHECKED_COMMANDS entry for zmcertmgr */
        String regex = "[a-zA-Z0-9/.\\-\\\\_:@,=\'\"* ]*";
        if (!subjectString.matches(regex)) {
            ZimbraLog.security.debug("Invalid subject = '%s' does not match regex %s", subjectString, regex);
            throw ServiceException.INVALID_REQUEST("Invalid subject", null);
        }
        return subjectString;
    }

    public static String getSubjectAltNames(List<String> subjectAltNamesList)
    throws ServiceException {
        StringBuilder subjectAltNames = new StringBuilder();
        for (String altName : subjectAltNamesList) {
            if (!HostSpecifier.isValid(altName)) {
                throw ServiceException.INVALID_REQUEST("Invalid subjectAltName '" + altName + "'", null);
            }
            if (!Strings.isNullOrEmpty(altName)) {
                if (0 != subjectAltNames.length()) {
                    subjectAltNames.append(',');
                }
                subjectAltNames.append(altName);
            }
        }
        return subjectAltNames.toString();
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
         relatedRights.add(Admin.R_generateCSR);
    }
}
