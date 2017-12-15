/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2012, 2014, 2015, 2016 Synacor, Inc.
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
using System.IO;
using System.Net.Security;
using System.Net;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System;

namespace CssLib
{


public class WebServiceClient
{
    public enum ServiceType
    {
        Traditional = 0, WCF = 1
    }

    public ServiceType WSServiceType { get; set; }
    public string Url { get; set; }
    public int status;
    public HttpStatusCode httpStatusCode;
    public string httpStatusDescription;
    public string exceptionMessage;
    public string errResponseMessage;

    private void setErrors(System.Net.WebException wex)
    {
        status = (int)wex.Status;
        exceptionMessage = wex.Message;
        if (wex.Response != null)
        {
            httpStatusCode = ((HttpWebResponse)wex.Response).StatusCode;
            httpStatusDescription = ((HttpWebResponse)wex.Response).StatusDescription;

            HttpWebResponse errResponse = (HttpWebResponse)wex.Response;
            long rlen = errResponse.ContentLength;
            Stream ReceiveStream = errResponse.GetResponseStream();
            Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
            StreamReader readStream = new StreamReader(ReceiveStream, encode);

            Char[] utf8Msg = new Char[rlen];

            int count = readStream.Read(utf8Msg, 0, (int)rlen);

            errResponseMessage = new string(utf8Msg);
        }
    }

    private HttpWebRequest CreateWebRequest()
    {
        // using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            HttpWebRequest webRequest = (HttpWebRequest)WebRequest.Create(this.Url);

            webRequest.ContentType = "application/soap+xml; charset=\"utf-8\"";
            webRequest.UserAgent = "ZimbraMigration" + "/" + ZimbraValues.GetZimbraValues().ClientVersion;
            webRequest.Method = "POST";
            webRequest.Proxy = null;
            return webRequest;
        }
    }

    public void InvokeService(string req, out string rsp)
    {
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            WebResponse response = null;
            string strResponse = "";

            status = 0;
            exceptionMessage = "";
            errResponseMessage = "";

            ServicePointManager.ServerCertificateValidationCallback = new RemoteCertificateValidationCallback(delegate(object sender2, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors) { return true; });

            // Create the request
            HttpWebRequest webReq = this.CreateWebRequest();

            // write the soap envelope to request stream (req is the soap envelope)
            try
            {
                using (Stream stm = webReq.GetRequestStream())
                {
                    using (StreamWriter stmw = new StreamWriter(stm))
                    {
                        stmw.Write(req);
                        stmw.Close();
                    }
                }
            }
            catch (System.Net.WebException wex)
            {
                // catch (Exception ex)
                setErrors(wex);
                rsp = "";
                return;
            }

            // get the response from the web service
            try
            {
                response = webReq.GetResponse();
            }
            catch (System.Net.WebException wex)
            {
                // catch (Exception ex)
                setErrors(wex);
                rsp = "";
                return;
            }

            Stream str = response.GetResponseStream();
            StreamReader sr = new StreamReader(str);

            strResponse = sr.ReadToEnd();

            status = 0;
            rsp = strResponse;
        }
    }

    private HttpWebRequest CreateWebRequestRaw(string authtoken, bool isServerMig)
    {
        //using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // Get a cookie container containing ZM_ADMIN_AUTH_TOKEN or ZM_AUTH_TOKEN
            CookieContainer cookieContainer = new CookieContainer();
            Cookie cookie = (isServerMig) ? new Cookie("ZM_ADMIN_AUTH_TOKEN", authtoken) : new Cookie("ZM_AUTH_TOKEN", authtoken);
            cookieContainer.Add(new Uri(this.Url), cookie);

            // Create the webRequest
            HttpWebRequest webRequest = (HttpWebRequest)WebRequest.Create(this.Url);
            webRequest.CookieContainer = cookieContainer;
            webRequest.UserAgent = "ZimbraMigration" + "/" + ZimbraValues.GetZimbraValues().ClientVersion;
            webRequest.Method = "POST";
            webRequest.Proxy = null;

            webRequest.Timeout = 10 * 60 * 1000; // 10 mins
            Log.verbose("Connection timeout is", webRequest.Timeout, "ms");

            return webRequest;
        }
    }

    public void InvokeUploadService(string authtoken, bool isServerMig, string filePath, string mimebuffer,
                                    string theDisposition, string theType, int mode, out string rsp)
    {
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // --------------------------------------------------
            // Is the data in file, or in string mimebuffer?
            // --------------------------------------------------
            bool bIsBuffer = false;
            if (mimebuffer.Length > 0)
                bIsBuffer = true;

            // --------------------------------------------------
            // Cert callback
            // --------------------------------------------------
            ServicePointManager.ServerCertificateValidationCallback = new RemoteCertificateValidationCallback(delegate(object sender2, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors) { return true; });

            // --------------------------------------------------
            // Create the request
            // --------------------------------------------------
            HttpWebRequest webReq = this.CreateWebRequestRaw(authtoken, isServerMig);

            // -------------------------------
            // Get preamble for the request
            // -------------------------------
            string ct;
            if (theType.Length == 0)
                ct = "Content-Type: " + "application/octet-stream";
            else
                ct = "Content-Type: " + theType;

            string boundary             = "--B-00=_" + DateTime.Now.Ticks.ToString("x");
            string endBoundary          = Environment.NewLine + "--" + boundary + "--" + Environment.NewLine; // FBS bug 73727 -- 5/29/12 -- removed extra "Environment.NewLine +" 
            string contentDisposition1  = "--" + boundary + Environment.NewLine + "Content-Disposition: form-data; name=\"requestId\"" + Environment.NewLine + Environment.NewLine + "lsrpc32-client-id" + Environment.NewLine;
            string cd2                  = (theDisposition.Length > 0) ? theDisposition : "Content-Disposition : form-data; name=\"lslib32\"; filename=\"lslib32.bin\"";
            string contentDisposition2  = "--" + boundary + Environment.NewLine + cd2;
            string contentType          = Environment.NewLine + ct + Environment.NewLine;
            string contentTransfer      = "Content-Transfer-Encoding: binary" + Environment.NewLine + Environment.NewLine;

            // -------------------------------
            // Write data into webReq's stream
            // -------------------------------
            webReq.ContentType = "multipart/form-data; boundary=" + boundary;

            if (mode == ZimbraAPI.STRING_MODE)      // easier -- all text in the request
            {
                try
                {
                    if (bIsBuffer)
                    {
                        // =============================================================================================
                        // STREAM FROM BUFFER
                        // =============================================================================================
                        string sDataToUpload = mimebuffer;
                        int nDataLen = sDataToUpload.Length; // for view in debugger

                        // --------------------------------------------------
                        // Build the request stream
                        // --------------------------------------------------
                        using (Stream stm = webReq.GetRequestStream())
                        {
                            using (StreamWriter stmw = new StreamWriter(stm, System.Text.Encoding.Default))
                            {
                                stmw.Write(contentDisposition1);
                                stmw.Write(contentDisposition2);
                                stmw.Write(contentType);
                                stmw.Write(contentTransfer);
                                stmw.Write(sDataToUpload);
                                stmw.Write(endBoundary);
                                stmw.Close();
                            }
                        }
                    }
                    else
                    {

                        // =============================================================================================
                        // STREAM FROM FILE
                        // =============================================================================================


                        // -------------------------------
                        // Build the request stream
                        // -------------------------------
                        long lFileSize = new System.IO.FileInfo(filePath).Length;
                        using (FileStream fileStream = File.OpenRead(filePath))
                        {
                            // Send it off in chunks of 5MB
                            int bufferSize = 5 * 1024 * 1024;
                            byte[] buffer = new byte[bufferSize];


                            UTF8Encoding encoding = new UTF8Encoding();
                            byte[] bytecd1          = encoding.GetBytes(contentDisposition1);
                            byte[] bytecd2          = encoding.GetBytes(contentDisposition2);
                            byte[] byteContType     = encoding.GetBytes(contentType);
                            byte[] byteContTransfer = encoding.GetBytes(contentTransfer);
                            byte[] byteEndBoundary  = encoding.GetBytes(endBoundary);

                            long lContentLength =   bytecd1.Length
                                                  + bytecd2.Length
                                                  + byteContType.Length
                                                  + byteContTransfer.Length
                                                  + lFileSize 
                                                  + byteEndBoundary.Length;
                            Log.trace("Bytes to upload:" + lContentLength);

                            webReq.AllowWriteStreamBuffering = false; // Without this, the call to GetRequestStream will allocate ContentLength bytes   "Setting AllowWriteStreamBuffering to true might cause performance problems when uploading large datasets because the data buffer could use all available memory."    YES!

                            // If the AllowWriteStreamBuffering property of HttpWebRequest is set to false,the contentlength has to be set to length of data to be posted else Exception(411) is raised.
                            webReq.ContentLength = lContentLength;

                            using (Stream RqstStrm = webReq.GetRequestStream())
                            {

                                // Write preamble
                                RqstStrm.Write(bytecd1,          0, bytecd1.Length); 
                                RqstStrm.Write(bytecd2,          0, bytecd2.Length);
                                RqstStrm.Write(byteContType,     0, byteContType.Length);
                                RqstStrm.Write(byteContTransfer, 0, byteContTransfer.Length);


                                // Write file contents
                                long nDone = 0;
                                int nBytes = 1;
                                while (nBytes != 0)
                                {
                                    nBytes = fileStream.Read(buffer, 0, bufferSize);
                                    if (nBytes > 0)
                                    {
                                        RqstStrm.Write(buffer, 0, nBytes);
                                        nDone += nBytes;
                                        Log.trace((100*nDone/lContentLength) + "% uploaded " + nDone + " bytes");
                                    }
                                }

                                // Write end boundary
                                RqstStrm.Write(byteEndBoundary, 0, byteEndBoundary.Length);

                                // All done
                                RqstStrm.Close();
                            }
                        }
                    }   
                }
                catch (System.Net.WebException wex)
                {
                    // catch (Exception ex)
                    Log.err(wex);
                    setErrors(wex);
                    rsp = "";
                    return;
                }
            }
            else  // CONTACT, APPT_VALUE, or APPT_EMB.  Not distinguishing yet, but we might later.
            {
                try
                {
                    // ----------------------------------------------------------------
                    // first get the bytes from the file -- this is the attachment data
                    // ----------------------------------------------------------------
                    byte[] buf = null;
                    long datalen = 0;
                    if (bIsBuffer)
                    {
                        datalen = mimebuffer.Length;
                        buf = Encoding.ASCII.GetBytes(mimebuffer);
                    }
                    else
                    {
                        System.IO.FileStream fileStream = new System.IO.FileStream(filePath, System.IO.FileMode.Open, System.IO.FileAccess.Read);
                        System.IO.BinaryReader binaryReader = new System.IO.BinaryReader(fileStream);

                        datalen = new System.IO.FileInfo(filePath).Length;

                        buf = binaryReader.ReadBytes((Int32)datalen);
                        fileStream.Close();
                        fileStream.Dispose();
                        binaryReader.Close();
                    }

                    // ----------------------------------------------------------------
                    // now use a memory stream since we have mixed data
                    // ----------------------------------------------------------------
                    using (Stream memStream = new System.IO.MemoryStream())
                    {
                        // write the request data
                        byte[] cd1Bytes = System.Text.Encoding.UTF8.GetBytes(contentDisposition1);
                        memStream.Write(cd1Bytes, 0, cd1Bytes.Length);

                        byte[] cd2Bytes = System.Text.Encoding.UTF8.GetBytes(contentDisposition2);
                        memStream.Write(cd2Bytes, 0, cd2Bytes.Length);

                        byte[] cTypeBytes = System.Text.Encoding.UTF8.GetBytes(contentType);
                        memStream.Write(cTypeBytes, 0, cTypeBytes.Length);

                        byte[] cTransferBytes = System.Text.Encoding.UTF8.GetBytes(contentTransfer);
                        memStream.Write(cTransferBytes, 0, cTransferBytes.Length);
                        memStream.Write(buf, 0, (int)datalen);

                        byte[] cEndBoundaryBytes = System.Text.Encoding.UTF8.GetBytes(endBoundary);
                        memStream.Write(cEndBoundaryBytes, 0, cEndBoundaryBytes.Length);

                        // set up the web request to use our memory stream
                        webReq.ContentLength = memStream.Length;

                        memStream.Position = 0;
                        byte[] tempBuffer = new byte[memStream.Length];
                        memStream.Read(tempBuffer, 0, tempBuffer.Length);
                        memStream.Close();


                        // ----------------------------------------------------------------
                        // Send it to server
                        // ----------------------------------------------------------------
                        Stream requestStream = webReq.GetRequestStream();
                        requestStream.Write(tempBuffer, 0, tempBuffer.Length);
                        requestStream.Close();
                    }
                }
                catch (System.Net.WebException wex)
                {
                    // catch (Exception ex)
                    setErrors(wex);
                    rsp = "";
                    return;
                }
            }

            // =======================================================================
            // Get the response from the web service
            // =======================================================================
            WebResponse response = null;
            try
            {
                Log.verbose(">GetResponse");
                response = webReq.GetResponse();
                Log.verbose("<GetResponse");
            }
            catch (System.Net.WebException wex)
            {
                // catch (Exception ex)
                setErrors(wex);
                rsp = "";
                return;
            }

            Stream str = response.GetResponseStream();
            StreamReader sr = new StreamReader(str);
            rsp = sr.ReadToEnd();
            status = 0;
        }
    }

}
}

