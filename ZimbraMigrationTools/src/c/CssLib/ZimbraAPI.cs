/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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


using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Xml.Linq;
using System.Xml;
using System;

namespace CssLib
{

// Implements ZCS SOAP commands
public class ZimbraAPI
{
    // Errors
    internal const int OOO_NO_TEXT = 92;
    internal const int TASK_CREATE_FAILED_FLDR = 93;
    internal const int APPT_CREATE_FAILED_FLDR = 94;
    internal const int CONTACT_CREATE_FAILED_FLDR = 95;
    internal const int FOLDER_CREATE_FAILED_SYN = 96;
    internal const int FOLDER_CREATE_FAILED_SEM = 97;
    internal const int ACCOUNT_NO_NAME = 98;
    internal const int ACCOUNT_CREATE_FAILED = 99;

    // Upload modes
    public const int STRING_MODE = 1;           // for messages -- request is all string data
    public const int CONTACT_MODE = 2;          // for contacts -- with pictures
    public const int APPT_VALUE_MODE = 3;       // for appts -- with binary
    public const int APPT_EMB_MODE = 4;         // for appts -- embedded messages

    // Values
    internal const int INLINE_LIMIT = 4000;     // smaller than this limit, we'll inline; larger, we'll upload

    char[] specialCharacters = { ':','/','"'};

    private string lastError;
    public string LastError 
    {
        get { return lastError; }
        set
        {
            lastError = value;
        }
    }

    private string sAccountName;
    public string AccountName 
    {
        get { return sAccountName; }
        set
        {
            sAccountName = value;
        }
    }

    private string sAccountID;
    public string AccountID
    {
        get { return sAccountID; }
        set
        {
            sAccountID = value;
        }
    }

    private bool bIsAdminAccount;
    public bool IsAdminAccount 
    {
        get { return bIsAdminAccount; }
        set
        {
            bIsAdminAccount = value;
        }
    }

    private bool bIsDomainAdminAccount;
    public bool IsDomainAdminAccount 
    {
        get { return bIsDomainAdminAccount; }
        set
        {
            bIsDomainAdminAccount = value;
        }
    }

    private bool bIsServerMigration;
    public bool IsServerMigration
    {
        get { return bIsServerMigration; }
        set
        {
            bIsServerMigration = value;
        }
    }

    // dictMapFolderPathToFolderId is appended at the end of CreateFolder and therefore contains
    // a map of all folders created in this account
    private Dictionary<string, string> dictMapFolderPathToFolderId;

    private string ReplaceSlash;

    public ZimbraAPI(bool isServerMigration, string replaceslash = "_")
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("ZimbraAPI.ZimbraAPI"))
        {
            bIsServerMigration = isServerMigration;

            dictMapFolderPathToFolderId = new Dictionary<string, string>();
            dictMapFolderPathToFolderId.Add("/MAPIRoot", "1");

            ReplaceSlash = replaceslash;
        }
    }

    // =======================================================
    // Parse Methods - these are used to parse SOAP responses
    // =======================================================
    // [note that we don't have Parse methods for CreateContact, CreateFolder, etc.]
    private string ParseSoapFault(string rsperr, bool bSuppressNoSuchAccountErr = true)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (rsperr.Length == 0)
            {
                Log.err("SOAP Fault <empty>");
                return "";
            }

            if (rsperr.IndexOf("<soap:Fault>") == -1)
            {
                Log.err("SOAP Fault <no Fault node>");
                return "";
            }

            // Look for "Reason"s
            string soapReason = "";
            XDocument xmlDoc = XDocument.Parse(rsperr);
            XNamespace ns = "http://www.w3.org/2003/05/soap-envelope";
            IEnumerable<XElement> de = from el in xmlDoc.Descendants() select el;
            foreach (XElement el in de)
            {
                if (el.Name == ns + "Reason")
                {
                    soapReason = el.Value;
                    break;
                }
            }

            bool bIsNoSuchAccount = soapReason.IndexOf("no such account:")==0;
            if (!bSuppressNoSuchAccountErr || !bIsNoSuchAccount)
            {
                Log.err("Fault:" + rsperr);
                Log.err("Reason:" + soapReason);
            }

            return soapReason;
        }
    }

    private void ParseLogon(string rsp, bool isAdmin)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string authToken = "";
            string isDomainAdmin = "false";

            if (rsp != null)
            {
                int startIdx = rsp.IndexOf("<authToken>");
                if (startIdx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = (isAdmin) ? "urn:zimbraAdmin" : "urn:zimbraAccount";

                    // we'll have to deal with this -- need to figure this out later -- with GetInfo
                    // for now, just faking -- always setting admin stuff to false if not admin -- not right
                    foreach (var objIns in xmlDoc.Descendants(ns + "AuthResponse"))
                    {
                        authToken += objIns.Element(ns + "authToken").Value;
                        isDomainAdmin = "false";
                        if (isAdmin)
                        {
                            var x = from a in objIns.Elements(ns + "a") where a.Attribute("n").Value == "zimbraIsDomainAdminAccount" select a.Value;

                            if (x.Any())    // FBS bug 72777
                            {
                                isDomainAdmin = x.ElementAt(0);
                            }
                        }
                    }
                }
            }
            ZimbraValues.GetZimbraValues().AuthToken = authToken;
        }
    }

    private void ParseGetInfo(string rsp)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string accountName = "";
            string serverVersion = "";

            if (rsp != null)
            {
                int startNameIdx = rsp.IndexOf("<name>");
                int startVersionIdx = rsp.IndexOf("<version>");

                if ((startNameIdx != -1) && (startVersionIdx != -1))
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraAccount";

                    foreach (var objIns in xmlDoc.Descendants(ns + "GetInfoResponse"))
                    {
                        accountName += objIns.Element(ns + "name").Value;
                        serverVersion += objIns.Element(ns + "version").Value;
                    }
                }
            }
            ZimbraValues.GetZimbraValues().ServerVersion = serverVersion;
            ZimbraValues.GetZimbraValues().AccountName = accountName;
        }
    }

    private int ParseGetAccount(string rsp)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            int retval = 0;

            if (rsp != null)
            {
                int dIdx = rsp.IndexOf("id=");
                if (dIdx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraAdmin";

                    foreach (var objIns in xmlDoc.Descendants(ns + "GetAccountResponse"))
                    {
                        foreach (XElement accountIns in objIns.Elements())
                        {
                            foreach (XAttribute accountAttr in accountIns.Attributes())
                            {
                                if (accountAttr.Name == "name")
                                {
                                    retval = (accountAttr.Value).Length;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return retval;
        }
    }

    private int ParseCreateAccount(string rsp)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            int retval = 0;

            if (rsp != null)
            {
                int dIdx = rsp.IndexOf("id=");

                if (dIdx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraAdmin";

                    foreach (var objIns in xmlDoc.Descendants(ns + "CreateAccountResponse"))
                    {
                        foreach (XElement accountIns in objIns.Elements())
                        {
                            foreach (XAttribute accountAttr in accountIns.Attributes())
                            {
                                if (accountAttr.Name == "name")
                                {
                                    retval = (accountAttr.Value).Length;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return retval;
        }
    }

    private void ParseGetAllDomain(string rsp)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (rsp != null)
            {
                int dIdx = rsp.IndexOf("domain");
                if (dIdx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraAdmin";

                    foreach (var objIns in xmlDoc.Descendants(ns + "GetAllDomainsResponse"))
                    {
                        foreach (XElement domainIns in objIns.Elements())
                        {
                            string name = "";
                            string id = "";
                            string domaindefaultcosid = "";

                            foreach (XAttribute domainAttr in domainIns.Attributes())
                            {
                                if (domainAttr.Name == "name")
                                    name = domainAttr.Value;
                                if (domainAttr.Name == "id")
                                    id = domainAttr.Value;
                            }
                            foreach (XElement nelements in domainIns.Elements())
                            {
                                foreach (XAttribute nattr in nelements.Attributes())
                                {
                                    if (nattr.Name == "n" && nattr.Value == "zimbraDomainDefaultCOSId")
                                    {
                                        domaindefaultcosid = nelements.Value;
                                    }
                                }
                            }
                            if ((name.Length > 0) || (id.Length > 0))
                                ZimbraValues.GetZimbraValues().ZimbraDomains.Add(new DomainInfo(name, id, domaindefaultcosid));
                        }
                    }
                }
            }
        }
    }

    private void ParseGetAllCos(string rsp)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (rsp != null)
            {
                int dIdx = rsp.IndexOf("cos");
                if (dIdx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraAdmin";

                    foreach (var objIns in xmlDoc.Descendants(ns + "GetAllCosResponse"))
                    {
                        foreach (XElement cosIns in objIns.Elements())
                        {
                            string name = "";
                            string id = "";

                            foreach (XAttribute cosAttr in cosIns.Attributes())
                            {
                                if (cosAttr.Name == "name")
                                    name = cosAttr.Value;
                                if (cosAttr.Name == "id")
                                    id = cosAttr.Value;
                            }
                            if ((name.Length > 0) || (id.Length > 0))
                                ZimbraValues.GetZimbraValues().COSes.Add(new CosInfo(name, id));
                        }
                    }
                }
            }
        }
    }

    private void ParseGetTag(string rsp, MigrationAccount acct)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (rsp != null)
            {
                int dIdx = rsp.IndexOf("tag");
                if (dIdx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraMail";

                    foreach (var objIns in xmlDoc.Descendants(ns + "GetTagResponse"))
                    {
                        foreach (XElement tagIns in objIns.Elements())
                        {
                            string name = "";
                            string id = "";

                            foreach (XAttribute tagAttr in tagIns.Attributes())
                            {
                                if (tagAttr.Name == "name")
                                    name = tagAttr.Value;
                                if (tagAttr.Name == "id")
                                    id = tagAttr.Value;
                            }
                            if ((name.Length > 0) || (id.Length > 0))
                                acct.TagDict.Add(name, id);
                        }
                    }
                }
            }
        }
    }

    // may not need this -- it's here anyway for now
    private void ParseAddMsg(string rsp, out string mID)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            mID = "";
            if (rsp != null)
            {
                int midIdx = rsp.IndexOf("m id");
                if (midIdx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraMail";

                    foreach (var objIns in xmlDoc.Descendants(ns + "AddMsgResponse"))
                    {
                        foreach (XElement mIns in objIns.Elements())
                        {
                            foreach (XAttribute mAttr in mIns.Attributes())
                            {
                                if (mAttr.Name == "id")
                                    mID = mAttr.Value;
                            }
                        }
                    }
                }
            }
        }
    }

    private void ParseCreateFolder(string rsp, out string folderID)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            folderID = "";
            if (rsp != null)
            {
                int idx = rsp.IndexOf("id=");
                if (idx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraMail";

                    foreach (var objIns in xmlDoc.Descendants(ns + "CreateFolderResponse"))
                    {
                        foreach (XElement folderIns in objIns.Elements())
                        {
                            foreach (XAttribute mAttr in folderIns.Attributes())
                            {
                                if (mAttr.Name == "id")
                                    folderID = mAttr.Value;
                            }
                        }
                    }
                }
            }
        }
    }

    private void ParseCreateTag(string rsp, out string tagID)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            tagID = "";
            if (rsp != null)
            {
                int idx = rsp.IndexOf("id=");
                if (idx != -1)
                {
                    XDocument xmlDoc = XDocument.Parse(rsp);
                    XNamespace ns = "urn:zimbraMail";

                    foreach (var objIns in xmlDoc.Descendants(ns + "CreateTagResponse"))
                    {
                        foreach (XElement tagIns in objIns.Elements())
                        {
                            foreach (XAttribute mAttr in tagIns.Attributes())
                            {
                                if (mAttr.Name == "id")
                                    tagID = mAttr.Value;
                            }
                        }
                    }
                }
            }
        }
    }

    // ////////


    // =======================================================
    // SendRequest
    // =======================================================
    private int SendRequest(WebServiceClient client, StringBuilder req, out string rsp)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string msg = req.ToString();
            rsp = "";

            try
            {
                // -----------------------
                // Hide <password>
                // -----------------------
                string sLog = msg;
                int nPosStart = msg.IndexOf("<password>");
                if (nPosStart != -1)
                {
                    int nPosEnd = msg.IndexOf("</password>");
                    if (nPosEnd != -1)
                    {
                        sLog = msg.Substring(0, nPosStart+10);
                        sLog += "*****";
                        sLog = sLog + msg.Substring(nPosEnd);
                    }
                }
                Log.verbose("SOAPRequest:");
                Log.verbosepretty(sLog);

                client.InvokeService(msg, out rsp);
            }
            finally
            {
                Log.verbose(" ");
                Log.verbose("SOAPResponse:");
                Log.verbosepretty(rsp);
            }

            //work on auth expire
            if (client.errResponseMessage.Contains("service.AUTH_EXPIRED"))
            {
                Log.info("ZimbraAPI - SendRequest - Auth Expired. Re-Logon initiated.");
                ZimbraValues.GetZimbraValues().zmSessionExpired = true;

                int lRet = Logon(ZimbraValues.GetZimbraValues().HostName,
                               ZimbraValues.GetZimbraValues().Port,
                               ZimbraValues.GetZimbraValues().zmUser,
                               ZimbraValues.GetZimbraValues().zmUserPwd,
                               ZimbraValues.GetZimbraValues().zmIsSSL,
                               ZimbraValues.GetZimbraValues().zmIsAdmin);
                if (lRet == 0)
                {
                    Log.info("ZimbraAPI - SendRequest - Auth Expired. Re-Logon succeeded.");
                    Log.info("Resending request");
                    client.InvokeService(msg, out rsp);
                }
                else
                {
                    Log.err("ZimbraAPI - SendRequest - Auth Expired. Re-Logon FAILED.");
                }
            }
            return client.status;
        }
    }

    // =======================================================
    // UploadFile
    // =======================================================
    // private UploadFile method
    private int UploadFile(string filepath, string mimebuffer, string contentdisposition, string contenttype, int mode, 
                           out string uploadInfo /*uploadToken if successful, else an error msg*/)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            Log.trace("Uploading " + filepath);
            bool isSecure = (ZimbraValues.GetZimbraValues().Url).Substring(0, 5) == "https";

            WebServiceClient client = (isSecure) ?
                new WebServiceClient
                {
                    Url = "https://" + ZimbraValues.GetZimbraValues().HostName + ":" + ZimbraValues.GetZimbraValues().Port + "/service/upload?fmt=raw",
                    WSServiceType = WebServiceClient.ServiceType.Traditional
                }
                :
                new WebServiceClient
                {
                    Url = "http://" + ZimbraValues.GetZimbraValues().HostName + ":" + ZimbraValues.GetZimbraValues().Port + "/service/upload?fmt=raw",
                    WSServiceType = WebServiceClient.ServiceType.Traditional
                };

            int retval = 0;
            string rsp = "";

            uploadInfo = "";

            client.InvokeUploadService(ZimbraValues.GetZimbraValues().AuthToken, IsServerMigration, filepath, mimebuffer, contentdisposition, contenttype, mode, out rsp);
            retval = client.status;
            if (retval == 0)
            {
                int li = rsp.LastIndexOf(",");
                if (li != -1)
                {
                    // get the string with the upload token, which will have a leading ' and a trailing '\r\n -- so strip that stuff off
                    int uti = li + 1;               // upload token index
                    string tmp = rsp.Substring(uti, (rsp.Length - uti));
                    int lastsinglequoteidx = tmp.LastIndexOf("'");

                    uploadInfo = tmp.Substring(1, (lastsinglequoteidx - 1));
                    Log.verbose("Upload token: " + uploadInfo);

                    if ((uploadInfo == "") || (uploadInfo == "null"))
                        Log.err("NULL upload token");
                }
            }
            else
            {
                Log.err(client.exceptionMessage);
                uploadInfo = client.exceptionMessage;
            }
            return retval;
        }
    }

    //






    // =======================================================
    // private API helper methods
    // =======================================================

    // example: <name>foo</name>
    private void WriteNVPair(XmlWriter writer, string name, string value)
    {
        writer.WriteStartElement(name);
        writer.WriteValue(value);
        writer.WriteEndElement();
    }

    // example: <a n="displayName">bar</a>
    private void WriteAttrNVPair(XmlWriter writer, string fieldType, string fieldName, string attrName, string attrValue)
    {
        writer.WriteStartElement(fieldType);
        writer.WriteStartAttribute(fieldName);
        writer.WriteString(attrName);
        writer.WriteEndAttribute();
        writer.WriteValue(attrValue);
        writer.WriteEndElement();
    }

    // example: <account by="name">foo@bar.com</account>
    private void WriteAccountBy(XmlWriter writer, string val)
    {
        WriteAttrNVPair(writer, "account", "by", "name", val);
    }

    private void WriteHeader(XmlWriter writer, bool bWriteSessionId, bool bWriteAuthtoken, bool bWriteAccountBy)
    {
        writer.WriteStartElement("Header", "http://www.w3.org/2003/05/soap-envelope");
        writer.WriteStartElement("context", "urn:zimbra");

        writer.WriteStartElement("nonotify");
        writer.WriteEndElement();               // nonotify

        writer.WriteStartElement("noqualify");
        writer.WriteEndElement();               // noqualify

        writer.WriteStartElement("nosession");
        writer.WriteEndElement();               // nosession

        if (bWriteSessionId)
        {
            writer.WriteStartElement("sessionId");
            writer.WriteEndElement();           // sessionId
        }

        if (bWriteAuthtoken)
            WriteNVPair(writer, "authToken", ZimbraValues.zimbraValues.AuthToken);

        if (bWriteAccountBy)                    // would only happen after a logon
            WriteAccountBy(writer, AccountName);

        writer.WriteEndElement();               // context
        writer.WriteEndElement();               // header
    }

    //






    // =======================================================
    // API methods i.e. SOAP commands
    // =======================================================
    public int Logon(string hostname, string port, string username, string password, bool isSecure, bool isAdmin)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            //save zcs connection params for later re-login
            ZimbraValues.GetZimbraValues().zmUser = username;
            ZimbraValues.GetZimbraValues().zmUserPwd = password;
            ZimbraValues.GetZimbraValues().zmIsSSL = isSecure;
            ZimbraValues.GetZimbraValues().zmIsAdmin = isAdmin;

            if ((ZimbraValues.GetZimbraValues().AuthToken.Length > 0) && (!ZimbraValues.GetZimbraValues().zmSessionExpired))
                return 0;                           // already logged on

            lastError = "";

            // FBS Bug 73394 -- 4/26/12 -- rewrite this section
            string mode = isSecure ? "https://" : "http://";
            string svc = isAdmin ? "/service/admin/soap" : "/service/soap";
            string urn = isAdmin ? "urn:zimbraAdmin" : "urn:zimbraAccount";
            ZimbraValues.GetZimbraValues().Url = mode + hostname + ":" + port + svc;
            // end Bug 73394

            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, false, false, false);

                // body
                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                writer.WriteStartElement("AuthRequest", urn);
                if (isAdmin)
                    WriteNVPair(writer, "name", username);
                else
                    WriteAccountBy(writer, username);

                WriteNVPair(writer, "password", password);

                writer.WriteEndElement();           // AuthRequest
                writer.WriteEndElement();           // soap body
                // end body

                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            int retval = SendRequest(client, sb, out rsp);
            if (retval == 0)
            {
                ParseLogon(rsp, isAdmin);
                ZimbraValues.GetZimbraValues().HostName = hostname;
                ZimbraValues.GetZimbraValues().Port = port;
                ZimbraValues.GetZimbraValues().zmSessionExpired = false;
            }
            else
            {
                string soapReason = ParseSoapFault(client.errResponseMessage);
                if (soapReason.Length > 0)
                    lastError = soapReason;
                else
                    lastError = client.exceptionMessage;
            }
            return retval;
        }
    }

    public int GetInfo()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, false);

                // body
                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                writer.WriteStartElement("GetInfoRequest", "urn:zimbraAccount");
                writer.WriteAttributeString("sections", "mbox");
                writer.WriteEndElement();           // GetInfoRequest
                writer.WriteEndElement();           // soap body
                // end body

                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            int retval = SendRequest(client, sb, out rsp);
            if (retval == 0)
            {
                ParseGetInfo(rsp);
            }
            else
            {
                string soapReason = ParseSoapFault(client.errResponseMessage);
                if (soapReason.Length > 0)
                    lastError = soapReason;
                else
                    lastError = client.exceptionMessage;
            }
            return retval;
        }
    }

    public int GetAllDomains()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (ZimbraValues.zimbraValues.Domains.Count > 0)        // already got 'em
                return 0;

            lastError = "";
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, false);

                // body
                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                writer.WriteStartElement("GetAllDomainsRequest", "urn:zimbraAdmin");
                writer.WriteEndElement();           // GetAllDomainsRequest
                writer.WriteEndElement();           // soap body
                // end body

                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            int retval = SendRequest(client, sb, out rsp);

            if (retval == 0)
            {
                ParseGetAllDomain(rsp);
            }
            else
            {
                string soapReason = ParseSoapFault(client.errResponseMessage);

                if (soapReason.Length > 0)
                    lastError = soapReason;
                else
                    lastError = client.exceptionMessage;
            }
            return retval;
        }
    }

    public int GetAllCos()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (ZimbraValues.zimbraValues.COSes.Count > 0)  // already got 'em
                return 0;

            lastError = "";
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, false);

                // body
                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                writer.WriteStartElement("GetAllCosRequest", "urn:zimbraAdmin");
                writer.WriteEndElement();           // GetAllCosRequest
                writer.WriteEndElement();           // soap body
                // end body

                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            int retval = SendRequest(client, sb, out rsp);
            if (retval == 0)
            {
                ParseGetAllCos(rsp);
            }
            else
            {
                string soapReason = ParseSoapFault(client.errResponseMessage);
                if (soapReason.Length > 0)
                    lastError = soapReason;
                else
                    lastError = client.exceptionMessage;
            }
            return retval;
        }
    }

    public int GetAccount(string accountname, bool bSuppressNoSuchAccountErr = false)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            int retval = 0;

            lastError = "";
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, false);

                // body
                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                writer.WriteStartElement("GetAccountRequest", "urn:zimbraAdmin");

                WriteAccountBy(writer, accountname);

                writer.WriteEndElement();           // GetAccountRequest
                writer.WriteEndElement();           // soap body
                // end body

                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            retval = SendRequest(client, sb, out rsp);

            if (retval == 0)
            {
                if (ParseGetAccount(rsp) == 0)      // length of name is 0 -- this is bad
                    retval = ACCOUNT_NO_NAME;
            }
            else
            {
                string soapReason = ParseSoapFault(client.errResponseMessage, bSuppressNoSuchAccountErr);

                if (soapReason.Length > 0)
                    lastError = soapReason;
                else
                    lastError = client.exceptionMessage;
            }
            return retval;
        }
    }

    public int CreateAccount(string accountname, string displayname, string givenname, string sn, string zfp, string defaultpw, bool mustChangePW, string cosid)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            int retval = 0;

            lastError = "";
            try
            {
                if (displayname.Length == 0)
                {
                    displayname = accountname.Substring(0, accountname.IndexOf("@"));
                }
                string zimbraForeignPrincipal = (zfp.Length > 0) ? zfp : "ad:" + displayname;

                WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
                StringBuilder sb = new StringBuilder();
                XmlWriterSettings settings = new XmlWriterSettings();
                settings.OmitXmlDeclaration = true;

                using (XmlWriter writer = XmlWriter.Create(sb, settings))
                {
                    writer.WriteStartDocument();
                    writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                    WriteHeader(writer, true, true, false);

                    // body
                    writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                    writer.WriteStartElement("CreateAccountRequest", "urn:zimbraAdmin");

                    WriteNVPair(writer, "name", accountname);
                    WriteNVPair(writer, "password", defaultpw);

                    WriteAttrNVPair(writer, "a", "n", "displayName", displayname);
                    if (givenname.Length > 0)
                    {
                        WriteAttrNVPair(writer, "a", "n", "givenName", givenname);
                    }
                    if (sn.Length > 0)
                    {
                        WriteAttrNVPair(writer, "a", "n", "sn", sn);
                    }
                    WriteAttrNVPair(writer, "a", "n", "zimbraForeignPrincipal", zimbraForeignPrincipal);
                    WriteAttrNVPair(writer, "a", "n", "zimbraCOSId", cosid);
                    if (mustChangePW)
                    {
                        WriteAttrNVPair(writer, "a", "n", "zimbraPasswordMustChange", "TRUE");
                    }

                    writer.WriteEndElement();           // CreateAccountRequest
                    writer.WriteEndElement();           // soap body
                    // end body

                    writer.WriteEndElement();           // soap envelope
                    writer.WriteEndDocument();
                }

                string rsp = "";
                retval = SendRequest(client, sb, out rsp);
                if (retval == 0)
                {
                    if (ParseCreateAccount(rsp) == 0)   // length of name is 0 -- this is bad
                        retval = ACCOUNT_CREATE_FAILED;
                }
                else
                {
                    string soapReason = ParseSoapFault(client.errResponseMessage);
                    if (soapReason.Length > 0)
                        lastError = soapReason;
                    else
                        lastError = client.exceptionMessage;
                }
            }
            catch (Exception e)
            {
                Log.err("ZimbraAPI: Exception in CreateAccount", e.Message);
            }
            return retval;
        }

    }

    public void CreateContactRequest(XmlWriter writer, Dictionary<string, string> contact, string folderId, int requestId)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            bool IsGroup = false;

            writer.WriteStartElement("CreateContactRequest", "urn:zimbraMail");
            if (requestId != -1)
                writer.WriteAttributeString("requestId", requestId.ToString());
            writer.WriteStartElement("cn");
            writer.WriteAttributeString("l", folderId);
            if (contact["tags"].Length > 0)
            {
                writer.WriteAttributeString("t", contact["tags"]);
            }

            foreach (KeyValuePair<string, string> pair in contact)
            {
                string nam = pair.Key;
                string val = pair.Value;

                if (nam == "image")
                {
                    if (val.Length > 0)
                    {
                        string uploadToken = "";
                        string tmp = "";
                        //if (UploadFile(val, tmp, "", "", CONTACT_MODE, out uploadToken) == 0)
                        if (UploadFile(val, tmp, contact["imageContentDisp"], contact["imageContentType"], CONTACT_MODE, out uploadToken) == 0)
                        {
                            writer.WriteStartElement("a");
                            writer.WriteAttributeString("n", nam);
                            writer.WriteAttributeString("aid", uploadToken);
                            writer.WriteEndElement();
                        }
                        File.Delete(val);
                    }
                }
                else
                {
                    if ((nam.CompareTo("imageContentDisp") == 0) || (nam.CompareTo("imageContentType") == 0))
                    {
                    }
                    else
                        if (nam.CompareTo("dlist") == 0)
                        {
                            IsGroup = true;
                            //string[] tokens = contact["dlist"].Split(',');
                            string[] tokens = contact["dlist"].Split(';');
                            if (tokens.Length > 0)
                            {
                                for (int i = 0; i < tokens.Length; i++)
                                {
                                    val = tokens.GetValue(i).ToString();
                                    if (val.Length > 0) // Bug 101134.
                                    {
                                        writer.WriteStartElement("m");
                                        writer.WriteAttributeString("type", "I");
                                        writer.WriteAttributeString("value", val);

                                        writer.WriteEndElement();
                                    }
                                }
                            }
                            else if (val.Length > 0)
                            {
                                writer.WriteStartElement("m");
                                writer.WriteAttributeString("type", "I");
                                writer.WriteAttributeString("value", val);

                                writer.WriteEndElement();
                            }
                        }
                        else
                            WriteAttrNVPair(writer, "a", "n", nam, val);
                }
            }

            if (IsGroup)
            {
                string tempname = contact["fileAs"];
                int index = tempname.IndexOf(":");
                if (index > 0)
                {
                    string nickname = tempname.Substring((index + 1));
                    if (nickname.Length > 0)
                    {
                        WriteAttrNVPair(writer, "a", "n", "nickname", nickname);
                    }
                }
            }
            writer.WriteEndElement();               // cn
            writer.WriteEndElement();               // CreateContactRequest
        }
    }

    public int CreateContact(Dictionary<string, string> contact, string folderPath = "")
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";

            // Create in Contacts unless another folder was desired
            string folderId = "7";

            if (folderPath.Length > 0)
            {
                folderId = FolderPathToFolderId(folderPath);
                if (folderId.Length == 0)
                {
                    lastError = "Failed to find folder id from path '"+folderPath+"'";
                    return CONTACT_CREATE_FAILED_FLDR;
                }
            }

            // //////
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            int retval = 0;
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, true);

                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");

                CreateContactRequest(writer, contact, folderId, -1);

                writer.WriteEndElement();           // soap body
                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            retval = SendRequest(client, sb, out rsp);
            if (retval != 0)
            {
                string soapReason = ParseSoapFault(client.errResponseMessage);
                if (soapReason.Length > 0)
                    lastError = soapReason;
                else
                    lastError = client.exceptionMessage;
            }

            return retval;
        }
    }

    public int CreateContacts(List<Dictionary<string, string> > lContacts, string folderPath = "")
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";

            // Create in Contacts unless another folder was desired
            string folderId = "7";

            if (folderPath.Length > 0)
            {
                folderId = FolderPathToFolderId(folderPath);
                if (folderId.Length == 0)
                {
                    lastError = "Failed to find folder id from path '"+folderPath+"'";
                    return CONTACT_CREATE_FAILED_FLDR;
                }
            }

            // //////
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            int retval = 0;
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, true);

                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                writer.WriteStartElement("BatchRequest", "urn:zimbra");
                for (int i = 0; i < lContacts.Count; i++)
                {
                    Dictionary<string, string> contact = lContacts[i];
                    CreateContactRequest(writer, contact, folderId, i);
                }
                writer.WriteEndElement();           // BatchRequest
                writer.WriteEndElement();           // soap body
                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            retval = SendRequest(client, sb, out rsp);

            return retval;
        }
    }

    public void AddMsgRequest(XmlWriter writer, string uploadInfo, ZimbraMessage message, bool isInline, int requestId)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // if isLine, uploadInfo will be a file path; if not, uploadInfo will be the upload token
            writer.WriteStartElement("AddMsgRequest", "urn:zimbraMail");
            if (requestId != -1)
                writer.WriteAttributeString("requestId", requestId.ToString());
            writer.WriteStartElement("m");

            string fID = message.folderId;
            if (fID.Length == 0)
            {
                // This is the same default as the server will use
                Log.warn("folderId is empty, using root (0)");
                fID = "0";
            }
            writer.WriteAttributeString("l", fID);

            long nrd = 0;
            if (long.TryParse(message.rcvdDate, out nrd) == true)
                writer.WriteAttributeString("d", message.rcvdDate);
            else
                if (long.TryParse(message.DateUnixString, out nrd) == true)
                {
                    Log.warn("delivery date not found. Using message date for 'd' attribute.");
                    writer.WriteAttributeString("d", message.DateUnixString);
                }
                else
                {
                    //do nothing. 'd' is optional
                    //skipping it will result in migration datetime
                    //shown in ZCS UI mail list but no error will happen
                    //and content will be migarted.
                    Log.warn("Date and delivery date not found. Skipping the 'd' attribute.");
                }
            writer.WriteAttributeString("f", message.flags);
            if (message.tags.Length > 0)
            {
                writer.WriteAttributeString("t", message.tags);
            }
            if (isInline)
            {
                WriteNVPair(writer, "content", System.Text.Encoding.Default.GetString(File.ReadAllBytes(uploadInfo)));
            }
            else
            {
                writer.WriteAttributeString("aid", uploadInfo);
            }
            writer.WriteEndElement();               // m
            writer.WriteEndElement();               // AddMsgRequest
        }
    }

    public int AddMessage(Dictionary<string, string> message)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";
            
            // =========================================================
            // Create a ZimbraMessage
            // =========================================================
            // Use reflection to set ZimbraMessage field values
            ZimbraMessage zm = new ZimbraMessage("", "", "", "", "", "");
            System.Type MsgType = typeof(ZimbraMessage);
            FieldInfo[] myFields = MsgType.GetFields(BindingFlags.Public | BindingFlags.Instance);
            for (int i = 0; i < myFields.Length; i++)  // Loop round zm's fields
            {
                string fieldName = (string)myFields[i].Name;
                myFields[i].SetValue(zm, message[fieldName]);
            }

            // =========================================================
            // Decide whether to inline the mime data
            // =========================================================
            bool bIsFile = (message["MimeFile"].Length > 0);

            bool isInline = false;
            if (bIsFile)
            {
                FileInfo f = new FileInfo(zm.MimeFile);// use a try/catch?
                isInline = (f.Length < INLINE_LIMIT);
            }

            // =========================================================
            // Upload the MIME
            // =========================================================
            int retval = 0;
            string uploadInfo = "";
            if (isInline)
                uploadInfo = zm.MimeFile;
            else
            {
                string mimebuff = "";
                if(message.ContainsKey("wstrmimeBuffer"))
                    mimebuff = message["wstrmimeBuffer"];

                retval = UploadFile(zm.MimeFile, mimebuff, "", "", STRING_MODE, out uploadInfo); // File deleted at bottom of this method
            }

            if (retval == 0)
            {
                // =========================================================
                // Create AddMsgRequest
                // =========================================================
                WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
                StringBuilder sb = new StringBuilder();
                XmlWriterSettings settings = new XmlWriterSettings();
                settings.OmitXmlDeclaration = true;

                using (XmlWriter writer = XmlWriter.Create(sb, settings))
                {
                    writer.WriteStartDocument();
                        writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                            WriteHeader(writer, true, true, true);

                                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");

                                AddMsgRequest(writer, uploadInfo, zm, isInline, -1);

                            writer.WriteEndElement();       // soap body
                        writer.WriteEndElement();       // soap envelope
                    writer.WriteEndDocument();
                }

                // =========================================================
                // Send it
                // =========================================================
                string rsp = "";
                retval = SendRequest(client, sb, out rsp);
                if (retval == 0)
                {
                    string mID = "";
                    ParseAddMsg(rsp, out mID);      // get the id
                }
                else
                {
                    string soapReason = ParseSoapFault(client.errResponseMessage);
                    string errMsg = (soapReason.IndexOf("upload ID: null") != -1)    // FBS bug 75159 -- 6/7/12
                                    ? "Unable to upload file. Please check server message size limits (Global Settings General Information and MTA)."
                                    : soapReason;
                    if (soapReason.Length > 0)
                    {
                        lastError = soapReason;
                        Log.err("Error on message '", message["Subject"], "' --", errMsg);
                    }
                    else
                        lastError = client.exceptionMessage;
                }
            }
            else
            {
                // UploadFil returns erorr message in uploadInfo
                lastError = uploadInfo;
            }

            if (bIsFile)
                File.Delete(zm.MimeFile);

            return retval;
        }
    }

    string CleanInvalidChars(string strToClean)
    {
        string tmp = strToClean;
        foreach (char c in System.IO.Path.GetInvalidFileNameChars())
        {
            tmp = tmp.Replace(c, '_');
        }
        return tmp;
    }

    private string ExceptionAttrName(string sAttr, int numEx)
    {
        return "ex" + numEx.ToString() + "_" + sAttr;
    }

    private string AttachAttrName(string sAttr, int numEx, int numAtt)
    //
    // Generates an attachment attribute name 
    //      e.g. 
    //      att0_attContentType         <- belongs to top-level
    //      ex0_att0_attContentType     <- belongs to first exception
    //
    // numEx = -1 for attachments which belong to the series rather than an exception,
    // in which case "ex<n>_" prefix is omitted
    // 
    {
        if (numEx>=0)
            return "ex" + numEx.ToString() + "_" + "att" + numAtt.ToString() + "_" + sAttr;
        else
            return "att" + numAtt.ToString() + "_" + sAttr;
    }

    private void WriteTZElement(XmlWriter writer, Dictionary<string, string> appt, bool bGotNewStyleTZ, bool bGotOldStyleTZ)
    {
        // Write list of TZs we reference in <s> and <e> nodes
        // (New-style TZ takes precedence)
        if (bGotNewStyleTZ)
        {
            WriteTimezone(writer, appt, "tz_start_");
            if (appt["tz_end_tid"] != appt["tz_start_tid"])
                WriteTimezone(writer, appt, "tz_end_");
        }
        else
        // Fall back to legacy - only avail on recurring
        if (bGotOldStyleTZ)
            WriteTimezone(writer, appt, "tz_legacy_");
    }

    private void WriteTZAttr(XmlWriter writer, Dictionary<string, string> appt, bool bGotNewStyleTZ, bool bGotOldStyleTZ, bool bStartNotEnd)
    {
        // tzid - prefer new style
        string sTZID = "";
        if (bGotNewStyleTZ)
            sTZID = bStartNotEnd?appt["tz_start_tid"]:appt["tz_end_tid"];
        else
        if (bGotOldStyleTZ)
            sTZID =  appt["tz_legacy_tid"];

        if (sTZID != "")
            writer.WriteAttributeString("tz", sTZID);
    }

    public void SetAppointmentRequest(XmlWriter writer, Dictionary<string, string> appt, string folderId)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            bool isRecurring = appt.ContainsKey("freq");
            int numExceptions = (appt.ContainsKey("numExceptions")) ? Int32.Parse(appt["numExceptions"]) : 0;
            
            bool bGotNewStyleTZ = false;
            bool bGotOldStyleTZ = false;
            if (   appt.ContainsKey("tz_start_tid") && (appt["tz_start_tid"]!="")
                && appt.ContainsKey("tz_end_tid")   && (appt["tz_end_tid"]  !="") )
                bGotNewStyleTZ = true;
            else
            if (appt.ContainsKey("tz_legacy_tid") && (appt["tz_legacy_tid"] != ""))
                bGotOldStyleTZ = true;


            writer.WriteStartElement("SetAppointmentRequest", "urn:zimbraMail");
            writer.WriteAttributeString("l", folderId);
            if (appt["tags"].Length > 0)
                writer.WriteAttributeString("t", appt["tags"]);

            // <default>
            {
                writer.WriteStartElement("default");
                writer.WriteAttributeString("ptst", appt["ptst"]);

                // <m>
                {
                    writer.WriteStartElement("m");

                    // <tz>
                    WriteTZElement(writer, appt, bGotNewStyleTZ, bGotOldStyleTZ);

                    // <inv>
                    {
                        writer.WriteStartElement("inv");
                        writer.WriteAttributeString("method", "REQUEST");

                        // fb
                        if (appt["fb"].Length > 0)
                            writer.WriteAttributeString("fb", appt["fb"]);

                        // transp
                        writer.WriteAttributeString("transp", appt["transp"]);

                        // allDay
                        if (appt["allDay"].Length > 0)
                            writer.WriteAttributeString("allDay", appt["allDay"]);
                        else
                            writer.WriteAttributeString("allDay", "0");

                        // name
                        writer.WriteAttributeString("name", appt["name"]);

                        // loc
                        writer.WriteAttributeString("loc", appt["loc"]);

                        // class
                        if (appt["class"].Length > 0)
                        {
                            if (appt["class"] == "1")
                                writer.WriteAttributeString("class", "PRI");
                        }

                        // uid
                        if (appt["uid"].Length > 0)
                            writer.WriteAttributeString("uid", appt["uid"]);

                        // <s>
                        if (appt["s"].Length > 0)
                        {
                            writer.WriteStartElement("s");
                            writer.WriteAttributeString("d", appt["s"]);
                            WriteTZAttr(writer, appt, bGotNewStyleTZ, bGotOldStyleTZ, true);
                            writer.WriteEndElement();
                        }

                        // <e>
                        if (appt["e"].Length > 0)
                        {
                            writer.WriteStartElement("e");
                            writer.WriteAttributeString("d", appt["e"]);
                            WriteTZAttr(writer, appt, bGotNewStyleTZ, bGotOldStyleTZ, false);
                            writer.WriteEndElement();
                       }

                        // <or>
                        writer.WriteStartElement("or");
                        writer.WriteAttributeString("d", appt["orName"]);
                        string theOrganizer = "";
                        if (appt["orAddr"].Length > 0)
                        {
                            theOrganizer = AccountName;
                            if (!IAmTheOrganizer(appt["orAddr"]))
                                theOrganizer = appt["orAddr"];
                        }
                        else
                        {
                            if (appt["orName"].Length > 0)
                                theOrganizer = appt["orName"];
                            else
                            {
                                theOrganizer = AccountName;
                                if (!IAmTheOrganizer(AccountName))
                                    theOrganizer = AccountName;
                            }
                        }
                        writer.WriteAttributeString("a", theOrganizer);
                        writer.WriteEndElement();


                        // attendees
                        if (appt.ContainsKey("attendees"))
                        {
                            string[] tokens = appt["attendees"].Split('~');
                            for (int i = 0; i < tokens.Length; i += 4)
                            {
                                writer.WriteStartElement("at");
                                writer.WriteAttributeString("d", tokens.GetValue(i).ToString());
                                writer.WriteAttributeString("a", tokens.GetValue(i + 1).ToString());
                                writer.WriteAttributeString("role", tokens.GetValue(i + 2).ToString());
                                writer.WriteAttributeString("rsvp", appt["rsvp"]);
                                if (appt["currst"] == "OR")
                                {
                                    if (tokens.GetValue(i + 3).ToString().Length > 0)   // FBS bug 75686 -- 6/27/12
                                        writer.WriteAttributeString("ptst", tokens.GetValue(i + 3).ToString());
                                    else
                                        writer.WriteAttributeString("ptst", "NE");
                                }
                                else
                                {
                                    if (appt["orAddr"] != tokens.GetValue(i + 1).ToString())
                                    {
                                        if (AccountID == tokens.GetValue(i).ToString())
                                            writer.WriteAttributeString("ptst", appt["currst"]);
                                        else
                                            writer.WriteAttributeString("ptst", "NE");
                                    }
                                }
                                writer.WriteEndElement();
                            }
                        }



                        if (isRecurring)
                        {
                            // <recur>
                            writer.WriteStartElement("recur");
                            {
                                // <add>
                                writer.WriteStartElement("add");
                                {
                                    // <rule>
                                    {
                                        writer.WriteStartElement("rule");
                                        writer.WriteAttributeString("freq", appt["freq"]);
                                        writer.WriteStartElement("interval");
                                        writer.WriteAttributeString("ival", appt["ival"]);
                                        writer.WriteEndElement();   // interval
                                        if (appt.ContainsKey("wkday"))
                                        {
                                            writer.WriteStartElement("byday");
                                            string wkday = appt["wkday"];
                                            int len = wkday.Length;
                                            for (int i = 0; i < len; i += 2)
                                            {
                                                writer.WriteStartElement("wkday");
                                                writer.WriteAttributeString("day", wkday.Substring(i, 2));
                                                writer.WriteEndElement();   //wkday
                                            }
                                            writer.WriteEndElement();   // byday
                                        }

                                        if (appt.ContainsKey("modaylist"))
                                        {
                                            writer.WriteStartElement("bymonthday");
                                            writer.WriteAttributeString("modaylist", appt["modaylist"]);
                                            writer.WriteEndElement();   // bymonthday
                                        }

                                        if (appt.ContainsKey("molist"))
                                        {
                                            writer.WriteStartElement("bymonth");
                                            writer.WriteAttributeString("molist", appt["molist"]);
                                            writer.WriteEndElement();   // bymonthday
                                        }

                                        if (appt.ContainsKey("poslist"))
                                        {
                                            writer.WriteStartElement("bysetpos");
                                            writer.WriteAttributeString("poslist", appt["poslist"]);
                                            writer.WriteEndElement();   // bymonthday
                                        }

                                        if (appt["count"].Length > 0)
                                        {
                                            writer.WriteStartElement("count");
                                            writer.WriteAttributeString("num", appt["count"]);
                                            writer.WriteEndElement();   // count
                                        }

                                        if (appt.ContainsKey("until"))
                                        {
                                            writer.WriteStartElement("until");
                                            writer.WriteAttributeString("d", appt["until"]);
                                            writer.WriteEndElement();   // until
                                        }
                                        writer.WriteEndElement();   // rule
                                    }
                                }
                                writer.WriteEndElement();   // add
                            }
                            writer.WriteEndElement();   // recur
                        }


                        // <alarm>
                        if (appt["m"].Length > 0)   // FBS bug 73665 -- 6/4/12
                        {
                            writer.WriteStartElement("alarm");
                            writer.WriteAttributeString("action", "DISPLAY");

                            {
                                writer.WriteStartElement("trigger");
                                {
                                    writer.WriteStartElement("rel");
                                    {
                                        writer.WriteAttributeString("related", "START");
                                        writer.WriteAttributeString("neg", "1");
                                        writer.WriteAttributeString("m", appt["m"]);
                                    }
                                    writer.WriteEndElement();   // rel
                                }
                                writer.WriteEndElement();   // trigger
                            }
                            writer.WriteEndElement();   // alarm
                        }

                        writer.WriteEndElement();   // inv
                    }

                    // <su>
                    WriteNVPair(writer, "su", appt["su"]);

                    // <mp>
                    {
                        writer.WriteStartElement("mp");
                        writer.WriteAttributeString("ct", "multipart/alternative");

                        // <mp>
                        {
                            writer.WriteStartElement("mp");
                            writer.WriteAttributeString("ct", appt["contentType0"]);
                            if (appt["content0"].Length > 0)
                                WriteNVPair(writer, "content", System.Text.Encoding.Unicode.GetString(File.ReadAllBytes(appt["content0"])));
                            writer.WriteEndElement();   // mp
                        }

                        // <mp>
                        {
                            writer.WriteStartElement("mp");
                            writer.WriteAttributeString("ct", appt["contentType1"]);
                            if (appt["content1"].Length > 0)
                                WriteNVPair(writer, "content", System.Text.Encoding.Unicode.GetString(File.ReadAllBytes(appt["content1"])));
                            writer.WriteEndElement();   // mp
                        }

                        writer.WriteEndElement();   // mp
                    }


                    // <attach>
                    {
                        // Upload attachments
                        int numAttachments = (appt.ContainsKey("numAttachments")) ? Int32.Parse(appt["numAttachments"]) : 0;
                        if (numAttachments > 0)
                        {
                            string aids = "";
                            for (int i = 0; i < numAttachments; i++)
                            {
                                string ContentTypeAttr        = AttachAttrName("attContentType", -1, i);
                                string ContentType            = appt[ContentTypeAttr];

                                string TempFileAttr           = AttachAttrName("attTempFile",    -1, i);
                                string TempFile               = appt[TempFileAttr];

                                string RealNameAttr           = AttachAttrName("attRealName",    -1, i);
                                string RealName               = appt[RealNameAttr];

                                string ContentDispositionAttr = AttachAttrName("attContentDisposition", -1, i);
                                string ContentDisposition     = appt[ContentDispositionAttr];

                                RealName = CleanInvalidChars(RealName);

                                // if contentType is message/rfc822, we'll rename the temp file to email_n and massage the content disposition
                                // if not, we'll just rename the temp file to the real name
                                string newfile = ""; string path = ""; string name = ""; int mode;
                                SplitFolderPathIntoParentAndChild("\\", TempFile, out path, out name);    // don't need name
                                if (ContentType == "message/rfc822")    // rename file to email_x_y and massage content disposition
                                {
                                    string newname = "email_" + appt["accountNum"] + "_" + i.ToString();
                                    newfile = path + "\\" + newname;  // accountNum for threading
                                    string oldValue = "\"" + RealName + "\"";
                                    string newValue = "\"" + newname + "\"";
                                    mode = APPT_EMB_MODE;
                                    ContentDisposition = ContentDisposition.Replace(oldValue, newValue);
                                }
                                else
                                {
                                    newfile = path + "\\" + RealName;
                                    mode = APPT_VALUE_MODE;
                                }
                                File.Move(TempFile, newfile);

                                string uploadToken = ""; string tmp = "";
                                if (UploadFile(newfile, tmp, ContentDisposition, ContentType, mode, out uploadToken) == 0)
                                {
                                    aids += uploadToken;
                                    if (i < (numAttachments - 1))
                                        aids += ",";
                                }
                                File.Delete(newfile);
                            }

                            writer.WriteStartElement("attach");
                            writer.WriteAttributeString("aid", aids);
                            writer.WriteEndElement();
                        }
                    }
                }
                    
                writer.WriteEndElement();   // m
            }
            writer.WriteEndElement();   // default


            // =================================================================
            // Exceptions
            // =================================================================
            // <except>
            {
                for (int i = 0; i < numExceptions; i++)
                    AddExceptionToRequest(writer, appt, i, bGotNewStyleTZ, bGotOldStyleTZ);
            }

            writer.WriteEndElement();   // SetAppointmentRequest

            DeleteApptTempFiles(appt, numExceptions);
        }
    }

    private void AddExceptionToRequest(XmlWriter writer, Dictionary<string, string> appt, int numExcep, bool bGotNewStyleTZ, bool bGotOldStyleTZ)
    {
        //using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // <cancel> or <except>
            {
                string attr = ExceptionAttrName("exceptionType", numExcep);
                bool isCancel = appt[attr] == "cancel";
                if (isCancel)
                    writer.WriteStartElement("cancel");
                else
                    writer.WriteStartElement("except");

                // <ptst>
                attr = ExceptionAttrName("ptst", numExcep);
                writer.WriteAttributeString("ptst", appt[attr]);

                // <m>
                {
                    writer.WriteStartElement("m");

                    // <tz>
                    WriteTZElement(writer, appt, bGotNewStyleTZ, bGotOldStyleTZ);

                    // <inv>
                    {
                        writer.WriteStartElement("inv");
                        if (!isCancel)
                            writer.WriteAttributeString("method", "REQUEST");

                        attr = ExceptionAttrName("fb", numExcep);
                        writer.WriteAttributeString("fb", appt[attr]);

                        writer.WriteAttributeString("transp", "O"); 

                        attr = ExceptionAttrName("allDay", numExcep);
                        if (appt[attr].Length > 0)
                            writer.WriteAttributeString("allDay", appt[attr]);
                        else
                            writer.WriteAttributeString("allDay", "0");

                        attr = ExceptionAttrName("name", numExcep);
                        writer.WriteAttributeString("name", appt[attr]);

                        attr = ExceptionAttrName("loc", numExcep);
                        writer.WriteAttributeString("loc", appt[attr]);

                        if (appt["uid"].Length > 0)
                            writer.WriteAttributeString("uid", appt["uid"]);

                        // <s>
                        writer.WriteStartElement("s");
                        attr = ExceptionAttrName("s", numExcep);
                        writer.WriteAttributeString("d", appt[attr]);
                        WriteTZAttr(writer, appt, bGotNewStyleTZ, bGotOldStyleTZ, true);
                        writer.WriteEndElement();

                        // <e>
                        if (!isCancel)
                        {
                            writer.WriteStartElement("e");
                            attr = ExceptionAttrName("e", numExcep);
                            writer.WriteAttributeString("d", appt[attr]);
                            WriteTZAttr(writer, appt, bGotNewStyleTZ, bGotOldStyleTZ, false);
                            writer.WriteEndElement();
                        }

                        // <exceptId>                        
                        attr = (isCancel) ? ExceptionAttrName("s", numExcep) : ExceptionAttrName("rid", numExcep); // FBS bug 71050 -- used to compute recurrence id
                        if (appt[attr].Length > 0)
                        {
                            writer.WriteStartElement("exceptId");
                            string exceptId = ComputeExceptId(appt[attr], appt["s"]);
                            writer.WriteAttributeString("d", exceptId);
                            WriteTZAttr(writer, appt, bGotNewStyleTZ, bGotOldStyleTZ, true);
                            writer.WriteEndElement();   // exceptId
                        }

                        // <or>
                        {
                            writer.WriteStartElement("or");
                            attr = ExceptionAttrName("orName", numExcep);
                            writer.WriteAttributeString("d", appt[attr]);
                            attr = ExceptionAttrName("orAddr", numExcep);
                            string theOrganizer = "";
                            if (appt["orAddr"].Length > 0)
                            {
                                theOrganizer = AccountName;
                                if (!IAmTheOrganizer(appt["orAddr"]))
                                    theOrganizer = appt["orAddr"];
                            }
                            else
                            {
                                if (appt["orName"].Length > 0)
                                    theOrganizer = appt["orName"];
                                else
                                {
                                    theOrganizer = AccountName;
                                    if (!IAmTheOrganizer(AccountName))
                                        theOrganizer = AccountName;
                                }
                            }
                            writer.WriteAttributeString("a", theOrganizer);
                            writer.WriteEndElement();
                        }

                        // FBS Bug 71054 -- 4/11/12
                        attr = ExceptionAttrName("attendees", numExcep);
                        if (appt[attr].Length > 0)
                        {
                            string[] tokens = appt[attr].Split('~');
                            for (int i = 0; i < tokens.Length; i += 4)
                            {
                                writer.WriteStartElement("at");
                                writer.WriteAttributeString("d", tokens.GetValue(i).ToString());
                                writer.WriteAttributeString("a", tokens.GetValue(i + 1).ToString());
                                writer.WriteAttributeString("role", tokens.GetValue(i + 2).ToString());
                                if (appt["currst"] == "OR")
                                {
                                    if (tokens.GetValue(i + 3).ToString().Length > 0)   // FBS bug 75686 -- 6/27/12
                                        writer.WriteAttributeString("ptst", tokens.GetValue(i + 3).ToString());
                                    else
                                        writer.WriteAttributeString("ptst", "NE");
                                }
                                else
                                {
                                    if (appt["orAddr"] != tokens.GetValue(i + 1).ToString())
                                        writer.WriteAttributeString("ptst", appt["currst"]);
                                }
                                writer.WriteEndElement();
                            }
                        }
                        //


                        // <alarm>
                        {
                            if (!isCancel)
                            {
                                attr = ExceptionAttrName("m", numExcep);
                                if (appt[attr].Length > 0)   // FBS bug 73665 -- 6/4/12
                                {
                                    writer.WriteStartElement("alarm");
                                        writer.WriteAttributeString("action", "DISPLAY");
                                        writer.WriteStartElement("trigger");
                                            writer.WriteStartElement("rel");
                                                writer.WriteAttributeString("related", "START");
                                                writer.WriteAttributeString("neg", "1");
                                                writer.WriteAttributeString("m", appt[attr]);
                                            writer.WriteEndElement();   // rel
                                        writer.WriteEndElement();   // trigger
                                    writer.WriteEndElement();   // alarm
                                }
                            }
                        }
                        writer.WriteEndElement();   // inv
                    }

                    // <su>
                    attr = ExceptionAttrName("su", numExcep);
                    WriteNVPair(writer, "su", appt[attr]);

                    // <mp>
                    {

                        // <mp>
                        {
                            writer.WriteStartElement("mp");
                            writer.WriteAttributeString("ct", "multipart/alternative");
                            writer.WriteStartElement("mp");
                            attr = ExceptionAttrName("contentType0", numExcep);
                            writer.WriteAttributeString("ct", appt[attr]);
                            attr = ExceptionAttrName("content0", numExcep);  // Should be Content1?
                            if (appt[attr].Length > 0)
                                WriteNVPair(writer, "content", System.Text.Encoding.Unicode.GetString(File.ReadAllBytes(appt[attr])));
                            writer.WriteEndElement();   // mp
                        }

                        // <mp>
                        {
                            writer.WriteStartElement("mp");
                            attr = ExceptionAttrName("contentType1", numExcep);
                            writer.WriteAttributeString("ct", appt[attr]);
                            attr = ExceptionAttrName("content1", numExcep);
                            if (appt[attr].Length > 0)
                                WriteNVPair(writer, "content", System.Text.Encoding.Unicode.GetString(File.ReadAllBytes(appt[attr])));
                            writer.WriteEndElement();   // mp
                        }
                        writer.WriteEndElement();   // mp
                    }


                    // <attach>
                    if (!isCancel)
                    {
                        // Upload attachments
                        attr = ExceptionAttrName("numAttachments", numExcep);
                        int numAttachments = (appt.ContainsKey(attr)) ? Int32.Parse(appt[attr]) : 0;
                        if (numAttachments > 0)
                        {
                            string aids = "";
                            for (int i = 0; i < numAttachments; i++)
                            {
                                string ContentTypeAttr        = AttachAttrName("attContentType", numExcep, i); 
                                string ContentType            = appt[ContentTypeAttr];

                                string TempFileAttr           = AttachAttrName("attTempFile", numExcep, i);
                                string TempFile               = appt[TempFileAttr];

                                string RealNameAttr           = AttachAttrName("attRealName", numExcep, i);
                                string RealName               = appt[RealNameAttr];

                                string ContentDispositionAttr = AttachAttrName("attContentDisposition", numExcep, i);
                                string ContentDisposition     = appt[ContentDispositionAttr];

                                RealName = CleanInvalidChars(RealName);

                                // if contentType is message/rfc822, we'll rename the temp file to email_n and massage the content disposition
                                // if not, we'll just rename the temp file to the real name
                                string newfile = ""; string path = ""; string name = ""; int mode;
                                SplitFolderPathIntoParentAndChild("\\", TempFile, out path, out name);    // don't need name
                                if (ContentType == "message/rfc822")    // rename file to email_x_y and massage content disposition
                                {
                                    string newname = "email_" + appt["accountNum"] + "_" + i.ToString();
                                    newfile = path + "\\" + newname;  // accountNum for threading
                                    string oldValue = "\"" + RealName + "\"";
                                    string newValue = "\"" + newname + "\"";
                                    mode = APPT_EMB_MODE;
                                    ContentDisposition = ContentDisposition.Replace(oldValue, newValue);
                                }
                                else
                                {
                                    newfile = path + "\\" + RealName;
                                    mode = APPT_VALUE_MODE;
                                }
                                File.Move(TempFile, newfile);

                                string uploadToken = ""; string tmp = "";
                                if (UploadFile(newfile, tmp, ContentDisposition, ContentType, mode, out uploadToken) == 0)
                                {
                                    aids += uploadToken;
                                    if (i < (numAttachments - 1))
                                        aids += ",";
                                }
                                File.Delete(newfile);
                            }

                            writer.WriteStartElement("attach");
                            writer.WriteAttributeString("aid", aids);
                            writer.WriteEndElement();
                        }
                    }


                    writer.WriteEndElement();   // m
                }
                writer.WriteEndElement();   // except or cancel
            }
        }
    }

    public int AddAppointment(Dictionary<string, string> appt, string folderPath = "")
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";

            // Create in Calendar unless another folder was desired
            string folderId = "10";

            if (folderPath.Length > 0)
            {
                folderId = FolderPathToFolderId(folderPath);
                if (folderId.Length == 0)
                {
                    lastError = "Failed to find folder id from path '"+folderPath+"'";
                    return APPT_CREATE_FAILED_FLDR;
                }
            }

            // //////
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            int retval = 0;
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, true);

                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                SetAppointmentRequest(writer, appt, folderId);

                writer.WriteEndElement();           // soap body
                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            retval = SendRequest(client, sb, out rsp);

            if (retval != 0)
            {
                string soapReason = ParseSoapFault(client.errResponseMessage);
                if (soapReason.Length > 0)
                {
                    lastError = soapReason;
                    Log.err("Error on appointment", appt["su"], "--", soapReason);
                }
            }
            return retval;
        }
    }

    private void WriteTimezone(XmlWriter writer, Dictionary<string, string> appt, string sKind)
    {
        writer.WriteStartElement("tz");
        writer.WriteAttributeString("id",     appt[sKind+"tid"]);
        writer.WriteAttributeString("stdoff", appt[sKind+"stdoff"]);

        // FBS bug 73047 -- 4/24/12 -- don't write standard/daylight nodes if no DST
        if ((appt[sKind+"sweek"] != "0") && (appt[sKind+"smon"] != "0"))
        {
            writer.WriteAttributeString("dayoff", appt[sKind+"dayoff"]);

            writer.WriteStartElement("standard");
                writer.WriteAttributeString("week",     appt[sKind+"sweek"]);
                writer.WriteAttributeString("wkday",    appt[sKind+"swkday"]);
                writer.WriteAttributeString("mon",      appt[sKind+"smon"]);
                writer.WriteAttributeString("hour",     appt[sKind+"shour"]);
                writer.WriteAttributeString("min",      appt[sKind+"smin"]);
                writer.WriteAttributeString("sec",      appt[sKind+"ssec"]);
            writer.WriteEndElement();
        }

        if ((appt[sKind+"dweek"] != "0") && (appt[sKind+"dmon"] != "0"))
        {
            writer.WriteStartElement("daylight");
                writer.WriteAttributeString("week",     appt[sKind+"dweek"]);
                writer.WriteAttributeString("wkday",    appt[sKind+"dwkday"]);
                writer.WriteAttributeString("mon",      appt[sKind+"dmon"]);
                writer.WriteAttributeString("hour",     appt[sKind+"dhour"]);
                writer.WriteAttributeString("min",      appt[sKind+"dmin"]);
                writer.WriteAttributeString("sec",      appt[sKind+"dsec"]);
            writer.WriteEndElement();
        }
        writer.WriteEndElement();   // tz
    }

    private string ComputeExceptId(string exceptDate, string originalDate)
    {
        if (exceptDate.Length == 8) // already done -- must be allday
            return exceptDate;

        // Date from the exception
        string retval = exceptDate.Substring(0, 9);

        // Time from original, if available
        if (originalDate.Length>=9)
            retval += originalDate.Substring(9, 6);

        return retval;
    }

    private void SetTaskRequest(XmlWriter writer, Dictionary<string, string> task, string folderId)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            bool isRecurring = task.ContainsKey("freq");
            writer.WriteStartElement("SetTaskRequest", "urn:zimbraMail");
            writer.WriteAttributeString("l", folderId);

            if (task["tags"].Length > 0)
                writer.WriteAttributeString("t", task["tags"]);

            // <default>
            {
                writer.WriteStartElement("default");
                writer.WriteAttributeString("ptst", "NE");  // we don't support Task Requests
                writer.WriteStartElement("m");

                /*
                // Timezone nodes if recurring appt
                if (isRecurring)
                {
                    writer.WriteStartElement("tz");

                        writer.WriteAttributeString("id",       appt["tz_legacy_tid"]);
                        writer.WriteAttributeString("stdoff",   appt["tz_legacy_stdoff"]);
                        writer.WriteAttributeString("dayoff",   appt["tz_legacy_dayoff"]);

                        writer.WriteStartElement("standard");
                            writer.WriteAttributeString("week",     appt["tz_legacy_sweek"]);
                            writer.WriteAttributeString("wkday",    appt["tz_legacy_swkday"]);
                            writer.WriteAttributeString("mon",      appt["tz_legacy_smon"]);
                            writer.WriteAttributeString("hour",     appt["tz_legacy_shour"]);
                            writer.WriteAttributeString("min",      appt["tz_legacy_smin"]);
                            writer.WriteAttributeString("sec",      appt["tz_legacy_ssec"]);
                        writer.WriteEndElement();

                        writer.WriteStartElement("daylight");
                            writer.WriteAttributeString("week",     appt["tz_legacy_dweek"]);
                            writer.WriteAttributeString("wkday",    appt["tz_legacy_dwkday"]);
                            writer.WriteAttributeString("mon",      appt["tz_legacy_dmon"]);
                            writer.WriteAttributeString("hour",     appt["tz_legacy_dhour"]);
                            writer.WriteAttributeString("min",      appt["tz_legacy_dmin"]);
                            writer.WriteAttributeString("sec",      appt["tz_legacy_dsec"]);
                        writer.WriteEndElement();

                    writer.WriteEndElement();   // tz
                }
                */

                // <inv>
                {
                    writer.WriteStartElement("inv");
                    writer.WriteAttributeString("status", task["status"]);
                    writer.WriteAttributeString("method", "REQUEST");
                    writer.WriteAttributeString("priority", task["priority"]);
                    writer.WriteAttributeString("percentComplete", task["percentComplete"]);
                    writer.WriteAttributeString("name", task["name"]);
                    writer.WriteAttributeString("allDay", "1"); // hard code - probably fine
                    writer.WriteAttributeString("transp", "O"); // hard code - probably fine
                    writer.WriteAttributeString("fb", "B");     // hard code - probably fine

                    // private, if applicable
                    if (task["class"].Length > 0)
                    {
                        if (task["class"] == "1")
                            writer.WriteAttributeString("class", "PRI");
                    }

                    if (task.ContainsKey("uid"))     // for now
                        writer.WriteAttributeString("uid", task["uid"]);

                    // <s>
                    {
                        if (task["s"].Length > 0)   // FBS bug 71748 -- 3/19/12
                        {
                            writer.WriteStartElement("s");
                            writer.WriteAttributeString("d", task["s"]);
                            //if (isRecurring)
                            //    writer.WriteAttributeString("tz", task["tz_legacy_tid"]);
                            writer.WriteEndElement();
                        }
                    }

                    // <e>
                    {
                        if (task["e"].Length > 0)   // FBS bug 71748 -- 3/19/12
                        {
                            writer.WriteStartElement("e");
                            writer.WriteAttributeString("d", task["e"]);
                            //if (isRecurring)
                            //    writer.WriteAttributeString("tz", task["tz_legacy_tid"]);
                            writer.WriteEndElement();
                        }
                    }

                    // <or>
                    {
                        // hard code the organizer -- we don't support task requests
                        writer.WriteStartElement("or"); 
                        writer.WriteAttributeString("a", AccountName);
                        writer.WriteEndElement();
                    }

                    // <alarm>
                    {
                        if (task.ContainsKey("taskflagdueby"))
                        {
                            if (task["taskflagdueby"].Length > 0)   // FBS bug 73665 -- 6/4/12
                            {
                                writer.WriteStartElement("alarm");
                                    writer.WriteAttributeString("action", "DISPLAY");
                                        writer.WriteStartElement("trigger");
                                            writer.WriteStartElement("abs");
                                            writer.WriteAttributeString("d", task["taskflagdueby"]);
                                            writer.WriteEndElement();   // abs
                                        writer.WriteEndElement();   // trigger
                                writer.WriteEndElement();   // alarm
                            }
                        }
                    }

                    if (isRecurring)
                    {
                        // <recur>
                        writer.WriteStartElement("recur");
                        writer.WriteStartElement("add");
                        writer.WriteStartElement("rule");
                        writer.WriteAttributeString("freq", task["freq"]);
                        writer.WriteStartElement("interval");
                        writer.WriteAttributeString("ival", task["ival"]);
                        writer.WriteEndElement();   // interval

                        if (task.ContainsKey("wkday"))
                        {
                            writer.WriteStartElement("byday");
                            string wkday = task["wkday"];
                            int len = wkday.Length;
                            for (int i = 0; i < len; i += 2)
                            {
                                writer.WriteStartElement("wkday");
                                writer.WriteAttributeString("day", wkday.Substring(i, 2));
                                writer.WriteEndElement();   //wkday
                            }
                            writer.WriteEndElement();   // byday
                        }

                        if (task.ContainsKey("modaylist"))
                        {
                            writer.WriteStartElement("bymonthday");
                            writer.WriteAttributeString("modaylist", task["modaylist"]);
                            writer.WriteEndElement();   // bymonthday
                        }

                        if (task.ContainsKey("molist"))
                        {
                            writer.WriteStartElement("bymonth");
                            writer.WriteAttributeString("molist", task["molist"]);
                            writer.WriteEndElement();   // bymonthday
                        }

                        if (task.ContainsKey("poslist"))
                        {
                            writer.WriteStartElement("bysetpos");
                            writer.WriteAttributeString("poslist", task["poslist"]);
                            writer.WriteEndElement();   // bymonthday
                        }

                        if (task["count"].Length > 0)
                        {
                            writer.WriteStartElement("count");
                            writer.WriteAttributeString("num", task["count"]);
                            writer.WriteEndElement();   // count
                        }

                        if (task.ContainsKey("until"))
                        {
                            writer.WriteStartElement("until");
                            writer.WriteAttributeString("d", task["until"]);
                            writer.WriteEndElement();   // until
                        }
                        writer.WriteEndElement();   // rule
                        writer.WriteEndElement();   // add
                        writer.WriteEndElement();   // recur
                    }

                    // <xprop>
                    if (task["xp-TOTAL_WORK"].Length > 0)
                    {
                        writer.WriteStartElement("xprop");
                        writer.WriteAttributeString("name", "X-ZIMBRA-TASK-TOTAL-WORK");
                        writer.WriteAttributeString("value", task["xp-TOTAL_WORK"]);
                        writer.WriteEndElement();   // xprop
                    }

                    // <xprop>
                    if (task["xp-ACTUAL_WORK"].Length > 0)
                    {
                        writer.WriteStartElement("xprop");
                        writer.WriteAttributeString("name", "X-ZIMBRA-TASK-ACTUAL-WORK");
                        writer.WriteAttributeString("value", task["xp-ACTUAL_WORK"]);
                        writer.WriteEndElement();   // xprop
                    }

                    // <xprop>
                    if (task["xp-COMPANIES"].Length > 0)
                    {
                        writer.WriteStartElement("xprop");
                        writer.WriteAttributeString("name", "X-ZIMBRA-TASK-COMPANIES");
                        writer.WriteAttributeString("value", task["xp-COMPANIES"]);
                        writer.WriteEndElement();   // xprop
                    }

                    // <xprop>
                    if (task["xp-MILEAGE"].Length > 0)
                    {
                        writer.WriteStartElement("xprop");
                        writer.WriteAttributeString("name", "X-ZIMBRA-TASK-MILEAGE");
                        writer.WriteAttributeString("value", task["xp-MILEAGE"]);
                        writer.WriteEndElement();   // xprop
                    }

                    // <xprop>
                    if (task["xp-BILLING"].Length > 0)
                    {
                        writer.WriteStartElement("xprop");
                        writer.WriteAttributeString("name", "X-ZIMBRA-TASK-BILLING");
                        writer.WriteAttributeString("value", task["xp-BILLING"]);
                        writer.WriteEndElement();   // xprop
                    }

                    writer.WriteEndElement();   // inv
                }

                // <su>
                WriteNVPair(writer, "su", task["su"]);

                // <mp>
                {
                    // <mp>
                    {
                        writer.WriteStartElement("mp");
                        writer.WriteAttributeString("ct", "multipart/alternative");
                        writer.WriteStartElement("mp");
                        writer.WriteAttributeString("ct", task["contentType0"]);
                        if (task["content0"].Length > 0)
                        {
                            WriteNVPair(writer, "content", System.Text.Encoding.Unicode.GetString(File.ReadAllBytes(task["content0"])));
                            File.Delete(task["content0"]);
                        }
                        writer.WriteEndElement();   // mp
                    }

                    // <mp>
                    {
                        writer.WriteStartElement("mp");
                        writer.WriteAttributeString("ct", task["contentType1"]);
                        if (task["content1"].Length > 0)
                        {
                            WriteNVPair(writer, "content", System.Text.Encoding.Unicode.GetString(File.ReadAllBytes(task["content1"])));
                            File.Delete(task["content1"]);
                        }
                        writer.WriteEndElement();   // mp
                    }
                    writer.WriteEndElement();   // mp
                }

                // <attach>
                int numAttachments = (task.ContainsKey("numAttachments")) ? Int32.Parse(task["numAttachments"]) : 0;
                if (numAttachments > 0)
                {
                    string aids = "";
                    for (int i = 0; i < numAttachments; i++)
                    {
                        string ContentTypeAttr        = AttachAttrName("attContentType", -1, i); 
                        string ContentType            = task[ContentTypeAttr];

                        string TempFileAttr           = AttachAttrName("attTempFile", -1, i);
                        string TempFile               = task[TempFileAttr];

                        string RealNameAttr           = AttachAttrName("attRealName", -1, i);
                        string RealName               = task[RealNameAttr];

                        string ContentDispositionAttr = AttachAttrName("attContentDisposition", -1, i);
                        string ContentDisposition     = task[ContentDispositionAttr];

                        RealName = CleanInvalidChars(RealName);

                        // if contentType is message/rfc822, we'll rename the temp file to email_n and massage the content disposition
                        // if not, we'll just rename the temp file to the real name
                        string newfile = ""; string path = ""; string name = ""; int mode;
                        SplitFolderPathIntoParentAndChild("\\", TempFile, out path, out name);    // don't need name
                        if (ContentType == "message/rfc822")    // rename file to email_x_y and massage content disposition
                        {
                            string newname = "email_" + task["accountNum"] + "_" + i.ToString();
                            newfile = path + "\\" + newname;  // accountNum for threading
                            string oldValue = "\"" + RealName + "\"";
                            string newValue = "\"" + newname + "\"";
                            mode = APPT_EMB_MODE;
                            ContentDisposition = ContentDisposition.Replace(oldValue, newValue);
                        }
                        else
                        {
                            newfile = path + "\\" + RealName;
                            mode = APPT_VALUE_MODE;
                        }
                        File.Move(TempFile, newfile);

                        // Upload files and get attachment ids
                        string uploadToken = ""; string tmp = "";
                        if (UploadFile(newfile, tmp, ContentDisposition, ContentType, mode, out uploadToken) == 0)
                        {
                            aids += uploadToken;
                            if (i < (numAttachments - 1))
                                aids += ",";
                        }
                        File.Delete(newfile);
                    }

                    writer.WriteStartElement("attach");
                    writer.WriteAttributeString("aid", aids);
                    writer.WriteEndElement();
                }

                writer.WriteEndElement();   // m
                writer.WriteEndElement();   // default
            }
            writer.WriteEndElement();   // SetTaskRequest
        }
    }

    public int AddTask(Dictionary<string, string> appt, string folderPath = "")
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";

            // Create in Tasks unless another folder was desired
            string folderId = "15";

            if (folderPath.Length > 0)
            {
                folderId = FolderPathToFolderId(folderPath); // do these also
                if (folderId.Length == 0)
                {
                    lastError = "Failed to find folder id from path '"+folderPath+"'";
                    return TASK_CREATE_FAILED_FLDR;
                }
            }

            // //////
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            int retval = 0;
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, true);

                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                SetTaskRequest(writer, appt, folderId);

                writer.WriteEndElement();           // soap body
                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }
            string rsp = "";
            retval = SendRequest(client, sb, out rsp);

            return retval;
        }
    }

    private void SetModifyPrefsRequest(XmlWriter writer, bool isOOOEnabled, string OOOInfo)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string state = (isOOOEnabled) ? "TRUE" : "FALSE";
            string OOOmsg = OOOInfo.Substring(2);   // provide for the :

            writer.WriteStartElement("ModifyPrefsRequest", "urn:zimbraAccount");
            WriteAttrNVPair(writer, "pref", "name", "zimbraPrefOutOfOfficeReplyEnabled", state);
            WriteAttrNVPair(writer, "pref", "name", "zimbraPrefOutOfOfficeReply", OOOmsg);
            WriteAttrNVPair(writer, "pref", "name", "zimbraPrefOutOfOfficeFromDate", "");
            WriteAttrNVPair(writer, "pref", "name", "zimbraPrefOutOfOfficeUntilDate", "");
            writer.WriteEndElement();   // ModifyPrefsRequest
        }
    }

    public int AddOOO(string OOOInfo, bool isServer)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";

            if (OOOInfo.Length == 0)
            {
                return OOO_NO_TEXT;
            }

            bool isOOOEnabled = OOOInfo.Substring(0, 1) == "1";
            if ((!isOOOEnabled) && (OOOInfo.Length == 2))   // 0:
            {
                return 0;   // it's ok -- just no need to do anything
            }

            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            int retval = 0;
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, isServer);

                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                SetModifyPrefsRequest(writer, isOOOEnabled, OOOInfo);

                writer.WriteEndElement();           // soap body
                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }
            string rsp = "";
            retval = SendRequest(client, sb, out rsp);

            return retval;
        }
    }

    private void AddFilterRuleToRequest(XmlWriter writer, Dictionary<string, string> rules, int idx)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // ^^^ is the delimiter for multiple filterTests, and filterActions
            // `~  is the token delimiter for individual filterTests, and filterActions
            // see CRuleMap::WriteFilterRule, CRuleMap::WriteFilterTests,CRuleMap::WriteFilterActions

            int i, j;

            // in the map, we'll have nfilterRule, nFilterTests, nFilterActions where n is idx
            string filterRuleDictName = idx.ToString() + "filterRule";
            string filterTestsDictName = idx.ToString() + "filterTests";
            string filterActionsDictName = idx.ToString() + "filterActions";

            writer.WriteStartElement("filterRule");
            // string[] tokens = rules[filterRuleDictName].Split(',');
            string[] tokens = rules[filterRuleDictName].Split(';');
            writer.WriteAttributeString(tokens.GetValue(0).ToString(), tokens.GetValue(1).ToString());
            writer.WriteAttributeString(tokens.GetValue(2).ToString(), tokens.GetValue(3).ToString());

            writer.WriteStartElement("filterTests");

            // first get anyof or allof.  String starts with "anyof:" or "allof:"
            writer.WriteAttributeString("condition", rules[filterTestsDictName].Substring(0, 5));

            // now process the actual tests
            string[] allTests = rules[filterTestsDictName].Substring(6).Split(new string[] { "^^^" }, StringSplitOptions.None);
            for (i = 0; i < allTests.Length; i++)
            {
                string eachTest = allTests.GetValue(i).ToString();
                string[] testTokens = eachTest.Split(new string[] { "`~" }, StringSplitOptions.None);
                string theTest = testTokens.GetValue(0).ToString();
                writer.WriteStartElement(theTest);
                if (theTest == "inviteTest")    // special case since we have to create a node
                {
                    writer.WriteAttributeString(testTokens.GetValue(1).ToString(), testTokens.GetValue(2).ToString());  // index
                    WriteNVPair(writer, testTokens.GetValue(3).ToString(), testTokens.GetValue(4).ToString());
                }
                else
                {
                    for (j = 1; j < testTokens.Length; j++)
                    {
                        writer.WriteAttributeString(testTokens.GetValue(j++).ToString(), testTokens.GetValue(j).ToString());
                    }
                }
                writer.WriteEndElement();   // theTest
            }
            writer.WriteEndElement();   // filterTests

            writer.WriteStartElement("filterActions");
            string[] allActions = rules[filterActionsDictName].Split(new string[] { "^^^" }, StringSplitOptions.None);
            for (i = 0; i < allActions.Length; i++)
            {
                string eachAction = allActions.GetValue(i).ToString();
                if (eachAction.Length > 0)    // FBS bug 71271 -- 3/9/12
                {
                    string[] actionTokens = eachAction.Split(new string[] { "`~" }, StringSplitOptions.None); ;
                    writer.WriteStartElement(actionTokens.GetValue(0).ToString());
                    if ((actionTokens.GetValue(0).ToString() != "actionStop") &&
                        (actionTokens.GetValue(0).ToString() != "actionDiscard"))
                    {
                        for (j = 1; j < actionTokens.Length; j++)
                        {
                            writer.WriteAttributeString(actionTokens.GetValue(j++).ToString(), actionTokens.GetValue(j).ToString());
                        }
                    }
                    writer.WriteEndElement();   // actionTokens.GetValue(i)
                }
            }
            writer.WriteEndElement();   // filterActions
            writer.WriteEndElement();   // filterRule
        }
    }

    private void SetModifyFilterRulesRequest(XmlWriter writer, Dictionary<string, string> rules)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            writer.WriteStartElement("ModifyFilterRulesRequest", "urn:zimbraMail");
            writer.WriteStartElement("filterRules");
            int numRules = Int32.Parse(rules["numRules"]);
            for (int i = 0; i < numRules; i++)
            {
                AddFilterRuleToRequest(writer, rules, i);
            }
            writer.WriteEndElement();   // filterRules
            writer.WriteEndElement();   // ModifyFilterRulesRequest
        }
    }

    public int AddRules(Dictionary<string, string> rules)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";

            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            int retval = 0;
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, true);

                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");
                SetModifyFilterRulesRequest(writer, rules);

                writer.WriteEndElement();           // soap body
                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            retval = SendRequest(client, sb, out rsp);

            return retval;
        }
    }

    private void CreateFolderRequest(XmlWriter writer, ZimbraFolder folder, int requestId)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            writer.WriteStartElement("CreateFolderRequest", "urn:zimbraMail");
            if (requestId != -1)
                writer.WriteAttributeString("requestId", requestId.ToString());
            writer.WriteStartElement("folder");

            // specialCharacters.Any(s => s.Equals(folder.name));

            int indSpecialC = folder.name.IndexOfAny(specialCharacters);
            if (indSpecialC != -1)
            {
                StringBuilder sb = new StringBuilder(folder.name);
                if (ReplaceSlash == null)
                {
                    ReplaceSlash = "_";
                }
                sb[indSpecialC] = ReplaceSlash.ToCharArray().ElementAt(0)/*'_'*/;
                string newS = sb.ToString();
                writer.WriteAttributeString("name", newS);
            }
            else
                writer.WriteAttributeString("name", folder.name);

            writer.WriteAttributeString("l", folder.parent);
            writer.WriteAttributeString("fie", "1");        // return the existing ID instead of an error
            if (folder.view.Length > 0)
                writer.WriteAttributeString("view", folder.view);
            if (folder.color.Length > 0)
                writer.WriteAttributeString("color", folder.color);
            if (folder.flags.Length > 0)
                writer.WriteAttributeString("f", folder.flags);
            writer.WriteEndElement();               // folder
            writer.WriteEndElement();               // CreateFolderRequest
        }
    }

    private int DoCreateFolder(ZimbraFolder folder, out string folderID)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            folderID = "";
            lastError = "";

            int retval = 0;
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, true);

                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");

                CreateFolderRequest(writer, folder, -1);

                writer.WriteEndElement();           // soap body
                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            string rsp = "";
            retval = SendRequest(client, sb, out rsp);

            if (retval == 0)
            {
                ParseCreateFolder(rsp, out folderID);       // get the id
            }
            else
            {
                string soapReason = ParseSoapFault(client.errResponseMessage);
                if (soapReason.Length > 0)
                    lastError = soapReason;
                else
                    lastError = client.exceptionMessage;
            }
            return retval;
        }
    }

    private bool SplitFolderPathIntoParentAndChild(string slash, string fullPath, out string parent, out string child)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            parent = "";
            child = "";

            // break up the folder name and parent from the path
            int lastSlash = fullPath.LastIndexOf(slash);
            if (lastSlash == -1)
                return false;

            int folderNameStart = lastSlash + 1;
            int len = fullPath.Length;

            parent = fullPath.Substring(0, lastSlash);
            child = fullPath.Substring(folderNameStart, (len - folderNameStart));

            return true;
        }
    }

    public string FolderPathToFolderId(string folderPath)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (dictMapFolderPathToFolderId.ContainsKey(folderPath))
                return dictMapFolderPathToFolderId[folderPath];
            else
                return "";
        }
    }

    public int CreateFolder(string FolderPath, int nSpecialFolderId, string View = "", string Color = "", string Flags = "")
    //
    // Note how this takes a folder PATH. This method has to do 2 impt things with this:
    // - 1. Determine if its a special folder
    // - 2. Find the Zimbra folder ID of the parent folder
    //
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // ====================================================================
            // First, see if FolderPath is a special folder
            // ====================================================================
            if (nSpecialFolderId>0)
            {
                // It's a special folder - dont create it, but do add it to the folder map
                string sFolderId = nSpecialFolderId.ToString();
                dictMapFolderPathToFolderId.Add(FolderPath, sFolderId);
                Log.verbose("Not creating special folder ", FolderPath, " ", sFolderId);
                return 0;
            }

            // ====================================================================
            // Next, we need to convert the parent path into a folderid
            // ====================================================================


            /*
            // A folder with / in name e.g. 
            //  /MAPIRoot/FolderNameWith/InIt (where FolderNameWith/InIt is the folder name)
            // is now escaped to 
            //  /MAPIRoot/FolderNameWith_InIt.
            //
            // But its concevable that a folder called "FolderNameWith_InIt" already exists in the store
            // Following code is a failed attempt at addressing this.
            
            // Check the dict first - if a folder of this name been seen before, add some underscores
            // Breaks subfolders of the folder that has _ appended :-(
            // -> would probably have to do this at C++ layer
            while(dictMapFolderPathToFolderId.ContainsKey(FolderPath))
            {
                Log.trace("'"+FolderPath+"' already created -> appending _ to avoid name collision");
                FolderPath = FolderPath+"_";
            }
            */
        
            // ---------------------------------------------------------
            // Split folder name into parent folder and folder to create
            // ---------------------------------------------------------
            string parentPath = "";
            string folderName = "";
            if (!SplitFolderPathIntoParentAndChild("/", FolderPath, out parentPath, out folderName))
                return FOLDER_CREATE_FAILED_SYN;


            // ------------------------------------------------------
            // Get special folder num for the parent folder
            // ------------------------------------------------------

            // First look in the special folders array. If it's not there, look in the map
            string strParentNum = "";

            if (!dictMapFolderPathToFolderId.ContainsKey(parentPath))
            {
                // Parent folder has not been created...must have been skipped -> dont create this folder
                Log.trace("Suppressing creation of folder '" + FolderPath + "' because parent has not been created");
                return FOLDER_CREATE_FAILED_SYN;
            }

            // ----------------------------------------------------------------------
            // Set strParentNum to "1" if this is MAPIRoot - no longer required?
            // ----------------------------------------------------------------------
            if (parentPath.Contains("IPM_SUBTREE"))
            {
                if (dictMapFolderPathToFolderId.ContainsKey(parentPath))
                    strParentNum = dictMapFolderPathToFolderId[parentPath];
                else
                    strParentNum = "1";
            }


            // ----------------------
            // If not MAPI Root...
            // ----------------------
            if (strParentNum.Length == 0)
            {
                // Get it from the dictionary
                if (dictMapFolderPathToFolderId.ContainsKey(parentPath))
                    strParentNum = dictMapFolderPathToFolderId[parentPath];  // <---- the normal case
                else
                {
                    // I think this code used to deal with folder names that had slashes in them

                    //
                    // e.g. Suppose we have a path like this:
                    //
                    //     /MAPIRoot/FolderNameWith/InIt      where the folder we want to create is "FolderNameWith/InIt"
                    //
                    // SplitFolderPathIntoParentAndChild() above will have produced:
                    // Parent: /MAPIRoot/FolderNameWith
                    // Folder: InIt
                    //
                    // .... which is wrong

                    // However, we now escape / to _ in the C++ layer - see MAPIFolder::CalcFolderPath()
                    // So this should no longer get called

                    
                    // How does the following code cope with it?
                    string[] words = FolderPath.Split('/');

                    int mnIndex = 0;

                    // Check for special folders - these should no longer happen because we now add special folders to the dict.
                    // And if they do happen, they will not work anyway for localized accounts
                    if ((words[2] == ("Inbox")) || (words[2] == ("Calendar")) || (words[2] == ("Contacts")) || (words[2] == ("Tasks")) || (words[2] == ("Junk")) || (words[2] == ("Drafts")))
                        mnIndex = 3;
                    else
                        mnIndex = 2;


                    // mnIndex will generally be 2 here?
                    int idx = 0;
                    if (words[mnIndex] != "")
                        idx = FolderPath.IndexOf(words[mnIndex]);
                    else
                    {
                        idx = FolderPath.IndexOf(words[++mnIndex]);
                        idx = idx - 1;
                    }

                    int len = FolderPath.Length;
                    string newpath = FolderPath.Substring(idx, len-idx);

                    validatefoldernames(FolderPath, newpath, out parentPath);

                    if (ReplaceSlash == null)
                        ReplaceSlash = "_";

                    if (parentPath != "")
                    {
                        int newlen = parentPath.Length;

                        folderName = FolderPath.Substring(newlen + 1);
                        folderName = folderName.Replace("/", ReplaceSlash);

                        if (dictMapFolderPathToFolderId.ContainsKey(parentPath))
                            strParentNum = dictMapFolderPathToFolderId[parentPath];
                        else
                            return FOLDER_CREATE_FAILED_SEM;
                    }
                    else
                    {
                        folderName = newpath.Replace("/", ReplaceSlash);
                        string NewParentPath = FolderPath.Substring(0, idx-1);
                        strParentNum = "";
                        if (strParentNum.Length == 0)
                        {
                            if (dictMapFolderPathToFolderId.ContainsKey(NewParentPath))
                                strParentNum = dictMapFolderPathToFolderId[NewParentPath];
                            else
                                return FOLDER_CREATE_FAILED_SEM;
                        }
                    }
                }
            }

            // ====================================================================
            // Create the folder
            // ====================================================================
            string folderID = "";
            int dcfReturnVal = DoCreateFolder(new ZimbraFolder(folderName, strParentNum, View, Color, Flags), out folderID);

            
            // ====================================================================
            // Add to the map
            // ====================================================================
            if (dcfReturnVal == 0)
            {
                Log.verbose("Created folder ", FolderPath, " ", folderID);
                dictMapFolderPathToFolderId.Add(FolderPath, folderID);
            }

            return dcfReturnVal;

        } //LogBlock
    }

    private void validatefoldernames(string folderpath, string newfolderpath, out string currentpath)
    //
    //
    //
    {
        // Clear out param
        currentpath = "";

        string[] nwords = newfolderpath.Split('/');
        string currentfoldername = "";
        string np = "";

        int itercnt = 0;
        while (itercnt <= (nwords.Length - 1))
        {
            string temp = nwords[itercnt];
            if (temp != "")
            {
                int i = folderpath.LastIndexOf(temp);
                np = folderpath.Substring(0, (i - 1));
                currentfoldername = np + '/' + nwords[itercnt];
            }
            else
                currentfoldername = currentfoldername + '/' ;
          
             if (dictMapFolderPathToFolderId.ContainsKey(currentfoldername))
                 currentpath = currentfoldername;

             itercnt = itercnt + 1;
        }
    }

    private void CreateTagRequest(XmlWriter writer, string tag, string color, int requestId)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            writer.WriteStartElement("CreateTagRequest", "urn:zimbraMail");
            if (requestId != -1)
                writer.WriteAttributeString("requestId", requestId.ToString());
            writer.WriteStartElement("tag");
            writer.WriteAttributeString("name", tag);
            writer.WriteAttributeString("color", color);
            writer.WriteEndElement();               // tag
            writer.WriteEndElement();               // CreateTagRequest
        }
    }

    public int CreateTag(string tag, string color, out string tagID)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            tagID = "";
            lastError = "";
            int retval = 0;
            try
            {
                WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
                StringBuilder sb = new StringBuilder();
                XmlWriterSettings settings = new XmlWriterSettings();
                settings.OmitXmlDeclaration = true;

                using (XmlWriter writer = XmlWriter.Create(sb, settings))
                {
                    writer.WriteStartDocument();
                    writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                    WriteHeader(writer, true, true, true);

                    writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");

                    CreateTagRequest(writer, tag, color, -1);

                    writer.WriteEndElement();           // soap body
                    writer.WriteEndElement();           // soap envelope
                    writer.WriteEndDocument();
                }

                string rsp = "";
                retval = SendRequest(client, sb, out rsp);
                if (retval == 0)
                {
                    ParseCreateTag(rsp, out tagID);       // get the id
                }
                else
                {
                    string soapReason = ParseSoapFault(client.errResponseMessage);
                    if (soapReason.Length > 0)
                        lastError = soapReason;
                    else
                        lastError = client.exceptionMessage;
                }
            }
            catch (Exception e)
            {
                Log.err("ZimbraApi:Exception in CreateTag {0}: {1}", tag, e.Message);
            }
            return retval;
        }
    }

    private void GetTagRequest(XmlWriter writer, int requestId)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            writer.WriteStartElement("GetTagRequest", "urn:zimbraMail");
            if (requestId != -1)
                writer.WriteAttributeString("requestId", requestId.ToString());
            writer.WriteEndElement();               // CreateTagRequest
        }
    }

    public int GetTags(MigrationAccount acct)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lastError = "";
            int retval = 0;

            // ===================================================================
            // Build GetTagsRequest
            // ===================================================================
            WebServiceClient client = new WebServiceClient { Url = ZimbraValues.GetZimbraValues().Url, WSServiceType = WebServiceClient.ServiceType.Traditional };
            StringBuilder sb = new StringBuilder();
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.OmitXmlDeclaration = true;

            using (XmlWriter writer = XmlWriter.Create(sb, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("soap", "Envelope", "http://www.w3.org/2003/05/soap-envelope");

                WriteHeader(writer, true, true, true);

                writer.WriteStartElement("Body", "http://www.w3.org/2003/05/soap-envelope");

                // Build a GetTagRequest
                GetTagRequest(writer, -1);

                writer.WriteEndElement();           // soap body
                writer.WriteEndElement();           // soap envelope
                writer.WriteEndDocument();
            }

            // ===================================================================
            // Send to server
            // ===================================================================
            string rsp = "";
            retval = SendRequest(client, sb, out rsp);
            if (retval == 0)
            {
                // Parse GetTagResponse - stores results in ZimbraValues
                ParseGetTag(rsp, acct);
            }
            else
            {
                string soapReason = ParseSoapFault(client.errResponseMessage);
                if (soapReason.Length > 0)
                    lastError = soapReason;
                else
                    lastError = client.exceptionMessage;
            }
            return retval;
        }
    }

    private bool IAmTheOrganizer(string theOrganizer)
    {
        int idxAcc = AccountName.IndexOf("@");
        int idxOrg = theOrganizer.IndexOf("@");
        if (idxOrg == -1)
        {
            int idxCN = theOrganizer.LastIndexOf("CN=");
            string Name = theOrganizer.Substring((idxCN +3));
            string nameAcc1 = AccountName.Substring(0, idxAcc);
            return ((nameAcc1.ToUpper() == Name) || (AccountID.ToUpper() == Name));
        }

        if ((idxAcc == -1) || (idxOrg == -1))   // can happen if no recip table
            return false;
        
        string nameAcc = AccountName.Substring(0, idxAcc);
        string nameOrg = theOrganizer.Substring(0, idxOrg);
        return ((nameAcc == nameOrg) || (AccountID == nameOrg));
    }

    private void DeleteApptTempFiles(Dictionary<string, string> appt, int numExceptions)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string attr = "";

            if (appt["content0"].Length > 0)
                File.Delete(appt["content0"]);

            if (appt["content1"].Length > 0)
                File.Delete(appt["content1"]);

            for (int i = 0; i < numExceptions; i++)
            {
                attr = ExceptionAttrName("content0", i);
                if (appt[attr].Length > 0)
                {
                    if (File.Exists(appt[attr]))
                        File.Delete(appt[attr]);
                }

                attr = ExceptionAttrName("content1", i);
                if (appt[attr].Length > 0)
                {
                    if (File.Exists(appt[attr]))
                        File.Delete(appt[attr]);
                }
            }
        }
    }
}

}
