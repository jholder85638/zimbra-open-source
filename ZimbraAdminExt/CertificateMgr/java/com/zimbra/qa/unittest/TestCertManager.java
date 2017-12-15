/*
 * ***** BEGIN LICENSE BLOCK *****
 *
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2016 Synacor, Inc.
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
 *
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.qa.unittest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.zimbra.cert.ZimbraCertMgrExt;
import com.zimbra.client.ZMailbox;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.zclient.ZClientException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.InstallCertRequest;
import com.zimbra.soap.admin.message.InstallCertResponse;
import com.zimbra.soap.admin.type.AidAndFilename;
import com.zimbra.soap.admin.type.CSRSubject;
import com.zimbra.soap.admin.type.CommCert;

public class TestCertManager extends TestCase {
    private static String ADMIN_UPLOAD_URL = "/service/upload?fmt=raw";
    private static String CERT_FILE_CONTENT = "this is a certificate";
    private static String CA_CERT_FILE_CONTENT = "this is a root CA file";
    private static String INTERMEDIATE_CERT_FILE_CONTENT = "this is an intermediate CA file";
    private SoapProvisioning adminSoapProv = null;
    private String UUID_PATTERN = "[a-z0-9]+\\-[a-z0-9]+\\-[a-z0-9]+\\-[a-z0-9]+\\-[a-z0-9]+";
    private Pattern TEMP_PATH_PATTERN = Pattern
            .compile("\\/opt\\/zimbra\\/data\\/tmp\\/[a-z0-9]+\\-[a-z0-9]+\\-[a-z0-9]+\\-[a-z0-9]+\\-[a-z0-9]+");
    private String privateKey = null;

    @Test
    public void testUploadCertificateFiles() throws ServiceException, IOException {
        org.junit.Assume.assumeNotNull(privateKey);
        org.junit.Assume.assumeTrue(!privateKey.isEmpty());
        String certAID = uploadRawContent("commercial_test_certificate.crt",
                new ByteArrayInputStream(CERT_FILE_CONTENT.getBytes()), "application/x-x509-ca-cert", CERT_FILE_CONTENT.length());
        String rootAID = uploadRawContent("commercial_test_rootca.crt",
                new ByteArrayInputStream(CA_CERT_FILE_CONTENT.getBytes()),
                "text/plain", CA_CERT_FILE_CONTENT.length());
        String intermediateAID = uploadRawContent("commercial_test_intermediateca.crt",
                new ByteArrayInputStream(INTERMEDIATE_CERT_FILE_CONTENT.getBytes()), "text/plain",
                INTERMEDIATE_CERT_FILE_CONTENT.length());
        InstallCertRequest req = new InstallCertRequest(Provisioning.getInstance().getLocalServer().getId(),
                ZimbraCertMgrExt.CERT_TYPE_COMM);
        CommCert commCert = new CommCert();
        commCert.addIntermediateCA(new AidAndFilename(intermediateAID, "commercial_test_intermediateca.crt"));
        commCert.setCert(new AidAndFilename(certAID, "commercial_test_certificate.crt"));
        commCert.setRootCA(new AidAndFilename(rootAID, "commercial_test_rootca.crt"));
        req.setCommCert(commCert);
        req.setSkipCleanup(true);
        CSRSubject subj = new CSRSubject();
        subj.setC("US");
        subj.setSt("CA");
        subj.setL("San Mateo");
        subj.setL("Zimbra");
        subj.setOu("Zimbra");
        subj.setCn("eng.zibmra.com");
        req.setSubject(subj);
        try {
            adminSoapProv.invokeJaxb(req);
            fail("should throw SoapFaultException, because the fies are fake");
        } catch (SoapFaultException e) {
            assertNotNull(e);
            String msg = e.getMessage();
            assertNotNull(msg);
            assertFalse(msg.isEmpty());
            assertTrue("error message should contain 'zmcertmgr'. Error message: " + msg,
 msg.contains("zmcertmgr"));
            assertTrue("error message should contain 'RemoteManager'. Error message: " + msg,
                    msg.contains("RemoteManager"));
            Matcher m = TEMP_PATH_PATTERN.matcher(e.getMessage());
            assertTrue(m.find());
            String tmpDirPath = m.group();
            assertNotNull(tmpDirPath);
            File tmpFolder = new File(tmpDirPath);
            assertTrue("temp folder should exist: " + tmpDirPath, tmpFolder.exists());
            assertTrue("temp folder should be a directory: " + tmpDirPath, tmpFolder.isDirectory());
            File tempFiles[] = tmpFolder.listFiles();
            boolean foundCertFile = false;
            boolean foundChainFile = false;
            for (File file : tempFiles) {
                if (file.isFile()) {
                    byte[] fileBytes = Files.readAllBytes(FileSystems.getDefault().getPath(file.getAbsolutePath()));
                    String fileContent = new String(fileBytes);
                    if (!foundCertFile) {
                        foundCertFile = fileContent.contains(CERT_FILE_CONTENT);
                    }
                    if (!foundChainFile) {
                        foundChainFile = fileContent.contains(INTERMEDIATE_CERT_FILE_CONTENT)
                                && fileContent.contains(CA_CERT_FILE_CONTENT);
                    }
                }
            }
            assertTrue("could not find cert file content", foundCertFile);
            assertTrue("could not chain file content", foundChainFile);
        }
    }

    @Test
    public void testUploadCWithCleanup() throws ServiceException, IOException {
        org.junit.Assume.assumeNotNull(privateKey);
        org.junit.Assume.assumeTrue(!privateKey.isEmpty());
        String certAID = uploadRawContent("commercial_test_certificate.crt",
                new ByteArrayInputStream(CERT_FILE_CONTENT.getBytes()), "application/x-x509-ca-cert",
                CERT_FILE_CONTENT.length());
        String rootAID = uploadRawContent("commercial_test_rootca.crt",
                new ByteArrayInputStream(CA_CERT_FILE_CONTENT.getBytes()), "text/plain", CA_CERT_FILE_CONTENT.length());
        String intermediateAID = uploadRawContent("commercial_test_intermediateca.crt", new ByteArrayInputStream(
                INTERMEDIATE_CERT_FILE_CONTENT.getBytes()), "text/plain", INTERMEDIATE_CERT_FILE_CONTENT.length());
        InstallCertRequest req = new InstallCertRequest(Provisioning.getInstance().getLocalServer().getId(),
                ZimbraCertMgrExt.CERT_TYPE_COMM);
        CommCert commCert = new CommCert();
        commCert.addIntermediateCA(new AidAndFilename(intermediateAID, "commercial_test_intermediateca.crt"));
        commCert.setCert(new AidAndFilename(certAID, "commercial_test_certificate.crt"));
        commCert.setRootCA(new AidAndFilename(rootAID, "commercial_test_rootca.crt"));
        req.setCommCert(commCert);
        CSRSubject subj = new CSRSubject();
        subj.setC("US");
        subj.setSt("CA");
        subj.setL("San Mateo");
        subj.setL("Zimbra");
        subj.setOu("Zimbra");
        subj.setCn("eng.zibmra.com");
        req.setSubject(subj);
        try {
            adminSoapProv.invokeJaxb(req);
            fail("should throw SoapFaultException, because the fies are fake");
        } catch (SoapFaultException e) {
            assertNotNull(e);
            String msg = e.getMessage();
            assertNotNull(msg);
            assertFalse(msg.isEmpty());
            assertTrue("error message should contain 'zmcertmgr'. Error message: " + msg, msg.contains("zmcertmgr"));
            assertTrue("error message should contain 'RemoteManager'. Error message: " + msg,
                    msg.contains("RemoteManager"));
            Matcher m = TEMP_PATH_PATTERN.matcher(e.getMessage());
            assertTrue(m.find());
            String tmpDirPath = m.group();
            assertNotNull(tmpDirPath);
            File tmpFolder = new File(tmpDirPath);
            assertFalse(tmpFolder.exists());
        }
    }

    @Test
    public void testInstallSelfSignedCert() throws ServiceException, IOException {
        org.junit.Assume.assumeNotNull(privateKey);
        org.junit.Assume.assumeTrue(!privateKey.isEmpty());
        InstallCertRequest req = new InstallCertRequest(Provisioning.getInstance().getLocalServer().getId(),
                ZimbraCertMgrExt.CERT_TYPE_SELF);
        CSRSubject subj = new CSRSubject();
        subj.setC("US");
        subj.setSt("CA");
        subj.setL("San Mateo");
        subj.setL("Zimbra");
        subj.setOu("Zimbra");
        subj.setCn("eng.zibmra.com");
        req.setSubject(subj);
        req.setValidationDays("365");
        req.setDigest("sha256");
        req.setKeySize("2048");
        InstallCertResponse resp = adminSoapProv.invokeJaxb(req);
        assertNotNull(resp);
    }

    @Test
    public void testInstallSelfSignedCertNoDefaults() throws ServiceException, IOException {
        org.junit.Assume.assumeNotNull(privateKey);
        org.junit.Assume.assumeTrue(!privateKey.isEmpty());
        InstallCertRequest req = new InstallCertRequest(Provisioning.getInstance().getLocalServer().getId(),
                ZimbraCertMgrExt.CERT_TYPE_SELF);
        CSRSubject subj = new CSRSubject();
        subj.setC("US");
        subj.setSt("CA");
        subj.setL("San Mateo");
        subj.setL("Zimbra");
        subj.setOu("Zimbra");
        subj.setCn("eng.zibmra.com");
        req.setSubject(subj);
        req.setValidationDays("365");
        InstallCertResponse resp = adminSoapProv.invokeJaxb(req);
        assertNotNull(resp);
    }

    String uploadRawContent(String fileName, InputStream in, String contentType, long contentLength)
            throws ServiceException, IOException {
        SoapHttpTransport transport = new SoapHttpTransport(TestUtil.getAdminSoapUrl());
        com.zimbra.soap.admin.message.AuthRequest req = new com.zimbra.soap.admin.message.AuthRequest(
                LC.zimbra_ldap_user.value(), LC.zimbra_ldap_password.value());
        req.setCsrfSupported(true);
        Element resp = transport.invoke(JaxbUtil.jaxbToElement(req, SoapProtocol.SoapJS.getFactory()));
        com.zimbra.soap.admin.message.AuthResponse authResp = JaxbUtil.elementToJaxb(resp);
        String authToken = authResp.getAuthToken();
        String csrfToken = authResp.getCsrfToken();
        int port = 7071;
        try {
            port = Provisioning.getInstance().getLocalServer().getIntAttr(Provisioning.A_zimbraAdminPort, 0);
        } catch (ServiceException e) {
            ZimbraLog.test.error("Unable to get admin SOAP port", e);
        }
        String Url = "https://localhost:" + port + ADMIN_UPLOAD_URL;

        String aid = null;
        if (fileName != null) {
            contentType += "; name=" + fileName;
        }

        HttpClient client = ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();
        HttpState state = new HttpState();
        state.addCookie(new org.apache.commons.httpclient.Cookie("localhost", ZimbraCookie.authTokenCookieName(true),
                authToken, "/", null, false));
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        client.setState(state);
        PostMethod post = new PostMethod(Url);
        int statusCode;
        try {
            post = HttpClientUtil.addInputStreamToHttpMethod(post, in, contentLength, contentType);
            post.addRequestHeader(Constants.CSRF_TOKEN, csrfToken);
            statusCode = HttpClientUtil.executeMethod(client, post);
            assertEquals("getting non 200 response from upload servlet", HttpServletResponse.SC_OK, statusCode);
            String response = post.getResponseBodyAsString();

            aid = ZMailbox.getAttachmentId(response);
            assertNotNull("attachment ID should not be null " + response, aid);
        } catch (IOException e) {
            throw ZClientException.IO_ERROR(e.getMessage(), e);
        } finally {
            post.releaseConnection();
        }
        return aid;
    }

    @Before
    public void setUp() throws Exception {
        cleanup();
        adminSoapProv = TestUtil.newSoapProvisioning();
        privateKey = Provisioning.getInstance().getLocalServer().getSSLPrivateKey();
    }

    @After
    public void tearDown() throws Exception {
        cleanup();
    }

    private void cleanup() throws Exception {
        File tmpFolder = new File("/opt/zimbra/data/tmp");
        File tempFiles[] = tmpFolder.listFiles();
        for (File f : tempFiles) {
            if (tmpFolder.exists() && f.isDirectory() && f.getName().matches(UUID_PATTERN)) {
                FileUtils.deleteDirectory(f);
            }
        }
    }
}
