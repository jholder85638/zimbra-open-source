/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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
#include "common.h"
#include "Exchange.h"
#include "Zimbra\MimeConverter.h"

MAPIRfc2445::MAPIRfc2445(Zimbra::MAPI::MAPISession &session, Zimbra::MAPI::MAPIMessage &mMessage) :
    m_session(&session), 
    m_mapiMessage(&mMessage)
{
    LOGFN_VERBOSE_NO;
    m_pMessage = m_mapiMessage->InternalMessageObject();

#ifdef _DEBUG
    if (m_pMessage)
    {
        Zimbra::Util::ScopedBuffer<SPropValue> spvSubj;
        HRESULT hResult = HrGetOneProp(m_pMessage, PR_SUBJECT_W, spvSubj.getptr());
        if (SUCCEEDED(hResult) && spvSubj[0].ulPropTag==PR_SUBJECT_W)
        {
            const int nMax = 256;
            WCHAR buff[nMax] = { 0 };
            _snwprintf(buff, nMax, L"p:%p '%s'", this, spvSubj->Value.lpszW);
            m_sObjectID = buff;
        }
    }
#endif

    m_pPropVals = NULL;
}

MAPIRfc2445::~MAPIRfc2445()
{
    LOGFN_VERBOSE_NO;
    if (m_pPropVals)
        MAPIFreeBuffer(m_pPropVals);
    m_pPropVals = NULL;
}

void MAPIRfc2445::IntToWstring(int src, wstring& dest)
{
    WCHAR pwszTemp[10];
    _ltow(src, pwszTemp, 10);
    dest = pwszTemp;
}

vector<AttachmentInfo*> MAPIRfc2445::GetAttachmentInfo() { return m_vAttachments; }

bool MAPIRfc2445::IsRecurring()                 { return m_bIsRecurring; }
wstring MAPIRfc2445::GetRecurPattern()          { return m_sRecurPattern; }
wstring MAPIRfc2445::GetRecurInterval()         { return m_sRecurInterval; }
wstring MAPIRfc2445::GetRecurCount()            { return m_sRecurCount; }
wstring MAPIRfc2445::GetRecurWkday()            { return m_sRecurWkday; }
wstring MAPIRfc2445::GetRecurEndType()          { return m_sRecurEndType; };
wstring MAPIRfc2445::GetRecurEndDate()          { return m_sRecurEndDate; };
wstring MAPIRfc2445::GetRecurDayOfMonth()       { return m_sRecurDayOfMonth; };
wstring MAPIRfc2445::GetRecurMonthOccurrence()  { return m_sRecurMonthOccurrence; };
wstring MAPIRfc2445::GetRecurMonthOfYear()      { return m_sRecurMonthOfYear; };

TzStrings MAPIRfc2445::GetTimezoneLegacy() { return m_tzsLegacy; };
TzStrings MAPIRfc2445::GetTimezoneStart()  { return m_tzsStart; };
TzStrings MAPIRfc2445::GetTimezoneEnd()    { return m_tzsEnd; };

HRESULT MAPIRfc2445::MAPIToMime(Zimbra::Util::ScopedInterface<IConverterSession>& pConvSess, LPMESSAGE pMsg, LPSTREAM pStream)
{
    LOGFN_TRACE_NO;
    UINT uFlags = CCSF_NO_MSGID |  CCSF_SMTP | CCSF_NOHEADERS ;
    HRESULT hr = pConvSess->MAPIToMime( pMsg, pStream, uFlags);
    return hr;
}

HRESULT MAPIRfc2445::ConvertIt(LPMESSAGE pMsg,                              // MAPI msg to convert
                               IStream** ppszMimeMsg, UINT& mimeLength )    // Output MIME                                                                   
{
    LOGFN_TRACE_NO;
    HRESULT hr = S_OK;

    // Create IConverterSession
    Zimbra::Util::ScopedInterface<IConverterSession> pConvSess;
    hr = CreateConverterSession( pConvSess.getptr() );
    if( FAILED(hr))
        return hr;
    pConvSess->SetSaveFormat( SAVE_RFC1521 );

    // Create stream to hold the MIME
    IStream * pStream = NULL;				
    hr = OpenStreamOnFile(Zimbra::Mapi::Memory::AllocateBuffer, Zimbra::Mapi::Memory::FreeBuffer, 
                          STGM_READWRITE | STGM_CREATE | SOF_UNIQUEFILENAME | STGM_DELETEONRELEASE,										  
                          NULL,  (LPWSTR)L"mig", &pStream);
    if( FAILED(hr))
        return hr;
    if( !pStream) 
    {
        _ASSERT(FALSE);
        return E_FAIL;
    }

    // Do the conversion
    hr = MAPIToMime(pConvSess, pMsg, pStream);
    if( FAILED(hr) ) 
        return hr;

    // get the stream size
    STATSTG stat;
    LARGE_INTEGER li = { 0 };
    hr = pStream->Seek( li, STREAM_SEEK_SET, NULL ); 	
    if( FAILED(hr) ) 
        return hr;

    hr = pStream->Stat( &stat, STATFLAG_DEFAULT );
    if( FAILED(hr) ) 
        return hr;

    ULONG size = (UINT)stat.cbSize.QuadPart;

    // Copy to output param
    *ppszMimeMsg = pStream;
    mimeLength = (UINT)size;
    
    return hr;
}

int MAPIRfc2445::GetNumHiddenAttachments()
{
    LOGFN_TRACE_NO;
    int retval = 0;
    HRESULT hr = S_OK;
    LPCWSTR errMsg;

    Zimbra::Util::ScopedInterface<IMAPITable> pAttachTable;
    hr = m_pMessage->GetAttachmentTable(MAPI_UNICODE, pAttachTable.getptr());
    if (SUCCEEDED(hr))
    {
        SizedSPropTagArray(1, attachProps) = {1, { PR_ATTACHMENT_HIDDEN }};
        if (FAILED(hr = pAttachTable->SetColumns((LPSPropTagArray) &attachProps, 0)))
        {
            errMsg = FormatExceptionInfo(hr, L"Error setting attachment table columns", __FILE__, __LINE__);
            LOG_WARNING(errMsg);
            return 0;
        }

        ULONG ulRowCount = 0;
        if (FAILED(hr = pAttachTable->GetRowCount(0, &ulRowCount)))
        {
            errMsg = FormatExceptionInfo(hr, L"Error getting attachment table row count", __FILE__, __LINE__);
            LOG_WARNING(errMsg);
            return 0;
        }

        Zimbra::Util::ScopedRowSet pAttachRows;
        if (FAILED(hr = pAttachTable->QueryRows(ulRowCount, 0, pAttachRows.getptr())))
        {
            errMsg = FormatExceptionInfo(hr, L"Error querying attachment table rows", __FILE__, __LINE__);
            LOG_WARNING(errMsg);
            return 0;
        }

        if (SUCCEEDED(hr))
        {
            hr = MAPI_E_NOT_FOUND;
            for (unsigned int i = 0; i < pAttachRows->cRows; i++)
            {
                // if property couldn't be found or returns error, skip it
                if ((pAttachRows->aRow[i].lpProps[0].ulPropTag != PT_ERROR) &&
                    (pAttachRows->aRow[i].lpProps[0].Value.err != MAPI_E_NOT_FOUND))
                {
                    if (pAttachRows->aRow[i].lpProps[0].Value.b)
                    {
                        retval++;
                    }
                }
            }
        }
    }
    else
    {
        errMsg = FormatExceptionInfo(hr, L"Error getting attachment tables", __FILE__, __LINE__);
        LOG_WARNING(errMsg);
        return 0;
    }
    return retval;
}

HRESULT MAPIRfc2445::ExtractRealAttachments(int nApptType)
//
// Looks for real attachments (i.e. not the hidden attachments that store exception data)
// and pulls to temp files
//
// DCB_PERFORMANCE: This method is very expensive when migrating exchange due to the call
// to ConvertIt() which spends around 55 secs in IConverterSession::MAPIToMime().
// I have done a fair bit to avoid calling this unnecessarily - c/l 546262 - but this only
// works when when the series has no attachments (arguably the most common case). It will 
// still get called for the series MAPIAppointment when series attachments present, AND
// for any exception MAPIAppointments which have attachments.
//
// To make significant performance improvements we need to do one of two things here:
// - Don't convert to MIME. Presumable everything this method does can be done at MAPI level?
// - Export the entire item to temp PST before migrating it? (This would eliminate the overhead
//   associated with individual reads to exch - which is presumable why MAPIToMime is so slow)
//
{
    LOGFN_TRACE;

    // ======================================================================================
    // Get MIME version of the MAPI msg
    // ======================================================================================

    // --------------------------------------------------------
    // Convert MAPI msg to mime stream using IConverterSession
    // --------------------------------------------------------
    LPCWSTR errMsg;
    Zimbra::Util::ScopedInterface<IStream> pIStream;
    UINT mimeLen = 0;
    HRESULT hr = ConvertIt( m_pMessage, pIStream.getptr(), mimeLen ); // DCB_PERFORMANCE This is super expensive on Exchange - takes 55 secs typically. Shouldn't need to do this - get AttachTable instead :-) DCB_PERFORMANCE_DONT_CALL_CONVERTIT
    if (FAILED(hr))
    {
        errMsg = FormatExceptionInfo(hr, L"Mime conversion of message with attachments failed", __FILE__, __LINE__);
        LOG_WARNING(errMsg);
        return hr;
    }

    // --------------------------------------------------------
    // Get mime to string
    // --------------------------------------------------------
    Zimbra::Util::ScopedBuffer<CHAR> pszMimeMsg; // ensure free
    LARGE_INTEGER li = { 0 };
    hr = pIStream->Seek(li, STREAM_SEEK_SET, NULL);
    if (FAILED(hr))
    {
        errMsg = FormatExceptionInfo(hr, L"Stream seek failed", __FILE__, __LINE__);
        LOG_WARNING(errMsg);
        return hr;
    }

    Zimbra::Mapi::Memory::AllocateBuffer(mimeLen + 1, (LPVOID *)pszMimeMsg.getptr()); // +1 for NULL terminator
    if (!pszMimeMsg.get())
    {
        errMsg = FormatExceptionInfo(S_OK, L"Mime msg Memory alloc failed", __FILE__, __LINE__);
        LOG_WARNING(errMsg);
        return hr;
    }

    ULONG ulNumRead = 0;
    hr = pIStream->Read((LPVOID)(pszMimeMsg.get()), mimeLen, &ulNumRead);
    if (FAILED(hr))
    {
        errMsg = FormatExceptionInfo(hr, L"Mime msg read failed", __FILE__, __LINE__);
        LOG_WARNING(errMsg);
        return hr;
    }
    if (ulNumRead != mimeLen)
    {
        errMsg = FormatExceptionInfo(hr, L"Mime msg read error", __FILE__, __LINE__);
        LOG_WARNING(errMsg);
        return hr;
    }

    pszMimeMsg.get()[mimeLen] = '\0'; // terminate string

    // --------------------------------------------------------
    // use mimepp to decode the msg
    // --------------------------------------------------------
    mimepp::Message mimeMsg;
    mimeMsg.setString(pszMimeMsg.get());

    // Parse the mime string to obtain the "broken down" version
    mimeMsg.parse();

    // ------------------------------------------------------------------------
    // Let's see if this message is a multipart alternative before we continue
    // ------------------------------------------------------------------------
    mimepp::Headers &theHeaders = mimeMsg.headers();
    LPSTR pszContentType;
    GetContentType(theHeaders, &pszContentType);
    if(strncmp(pszContentType, "multipart/mixed", strlen("multipart/mixed")) != 0) 
    {
        // not what we are looking for
        LOG_TRACE(_T("Not multipart/mixed -> no real attachments"));
        delete[] pszContentType;
        return S_OK;
    }


    // ======================================================================================
    // See if its got any REAL attachments (as opposed to ones which represent exceptions)
    // ======================================================================================
    // If none, nothing to do

    // FBS bug 73682 -- 5/23/12
    const mimepp::Body& theBody = mimeMsg.body();
    int numParts = theBody.numBodyParts();

    int numHiddenAttachments = GetNumHiddenAttachments();
    if (nApptType != TOP_LEVEL)
    {
        // Shouldn't be any hidden attachments on exceptions
        _ASSERT(numHiddenAttachments==0); // PST supplied in https://bugzilla.zimbra.com/show_bug.cgi?id=101625#c3 triggers this
    }

    int totalAttachments = numParts - 1;
    if (totalAttachments == numHiddenAttachments)
    {
        LOG_TRACE(_T("Only hidden attachments -> no real attachments"));
        return S_OK;
    }


    // ======================================================================================
    // Got some real attachments - grab them
    // ======================================================================================
    LOG_TRACE(_T("%d total, %d hidden attachments"), totalAttachments, numHiddenAttachments);

    // --------------------------------------------------------
    // Look for a multipart mixed
    // --------------------------------------------------------
    int ctr = numHiddenAttachments;
    int nAttNum = 0;
    for(int i = 0; i < numParts; i++) 
    {
        // now look for attachments
        const mimepp::BodyPart& thePart = theBody.bodyPartAt(i);
        mimepp::DispositionType& disposition = thePart.headers().contentDisposition();
        if(disposition.asEnum() == mimepp::DispositionType::ATTACHMENT) 
        {
            LPSTR pszAttachContentType; LPSTR pszCD;
            GetContentType(thePart.headers(), &pszAttachContentType);

            // FBS bug 73682 -- Exceptions are at the beginning.  Don't make attachments for those
            if (ctr > 0)
            {
                if (0 == strcmpi(pszAttachContentType, "message/rfc822"))
                {
                    ctr--;
                    continue;
                }
            }
            //

            ++nAttNum;

            LPSTR lpszRealName = new char[256];
            const mimepp::String& theFilename = disposition.filename();
            if((LPSTR)theFilename.length()>0)
            {
                GenerateContentDisposition(&pszCD, (LPSTR)theFilename.c_str());
                strcpy(lpszRealName, (LPSTR)theFilename.c_str());
            }
            else
            {
                char cfilename[64];
                sprintf(cfilename,"attachment-%d",i);
                GenerateContentDisposition(&pszCD, cfilename);
                strcpy(lpszRealName, cfilename);
            }

            // --------------------------------------------------------
            // Decode
            // --------------------------------------------------------
            LPSTR pContent = NULL;
            const mimepp::String &theContent = thePart.body().getString();
            mimepp::String outputString;
            UINT size = 0;

            mimepp::TransferEncodingType& transferEncoding = thePart.headers().contentTransferEncoding();

            if(transferEncoding.asEnum() == mimepp::TransferEncodingType::BASE64) 
            {
                // let's decode the buffer
                mimepp::Base64Decoder decoder;
                outputString = decoder.decode(theContent);
                pContent = (LPSTR)outputString.c_str();
                size = (UINT)outputString.size();
            } 
            else 
            if(transferEncoding.asEnum() == mimepp::TransferEncodingType::QUOTED_PRINTABLE) 
            {
                mimepp::QuotedPrintableDecoder decoder;
                outputString  = decoder.decode(theContent);
                pContent = (LPSTR)outputString.c_str();
                size = (UINT)outputString.size();
            } 
            else 
            {
                pContent = (LPSTR)theContent.c_str();
                size = (UINT)theContent.size();
            }

            // ----------------------------------------------------------------
            // Save stream to temp file in temp dir.  We'll delete in ZimbraAPI
            // ----------------------------------------------------------------
            LPCWSTR errMsg;
            HRESULT hr = S_OK;

            wstring wstrTempAppDirPath;

            if (!Zimbra::MAPI::Util::GetAppTemporaryDirectory(wstrTempAppDirPath))
            {
                errMsg = FormatExceptionInfo(S_OK, L"GetAppTemporaryDirectory Failed", __FILE__, __LINE__);
                LOG_ERROR(_T("MAPIRfc2445 -- exception"));
                LOG_ERROR(errMsg);
                return E_FAIL;
            }
            LPSTR lpszDirName = NULL;
            WtoA((LPWSTR)wstrTempAppDirPath.c_str(), lpszDirName);

            LPSTR lpszUniqueName = NULL;
            WtoA((LPWSTR)Zimbra::MAPI::Util::GetUniqueName().c_str(), lpszUniqueName);

            LPSTR lpszFQFileName = new char[256];
            strcpy(lpszFQFileName, lpszDirName);
            strcat(lpszFQFileName, "\\");
            strcat(lpszFQFileName, lpszUniqueName);

            SafeDelete(lpszDirName);
            SafeDelete(lpszUniqueName);

            // Open stream on file
            Zimbra::Util::ScopedInterface<IStream> pStream;
            if (FAILED(hr = OpenStreamOnFile(MAPIAllocateBuffer, MAPIFreeBuffer, STGM_CREATE | STGM_READWRITE, (LPTSTR)lpszFQFileName, NULL, pStream.getptr())))
            {
                errMsg = FormatExceptionInfo(hr, L"Error: OpenStreamOnFile Failed.", __FILE__, __LINE__);
                LOG_ERROR(_T("MAPIRfc2445 -- exception"));
                LOG_ERROR(errMsg);
                return hr;
            }

            // And write
            LOG_GEN_SUMMARY(_T("    Attachment %d %d bytes"), nAttNum, size);
            ULONG nBytesToWrite = size;
            ULONG nBytesWritten = 0;
            LPBYTE pCur = (LPBYTE)pContent;
            while (!FAILED(hr) && nBytesToWrite > 0) 
            {
                hr = pStream->Write(pCur, nBytesToWrite, &nBytesWritten);
                pCur += nBytesWritten;
                nBytesToWrite -= nBytesWritten;
            }
            if (FAILED(hr = pStream->Commit(0)))
            {
                errMsg = FormatExceptionInfo(hr, L"Error: Stream Write Failed.", __FILE__, __LINE__);
                LOG_ERROR(_T("MAPIRfc2445 -- exception"));
                LOG_ERROR(errMsg);
            }

            // ----------------------------------------------------------------
            // Save the attach data to m_vAttachments
            // ----------------------------------------------------------------
            AttachmentInfo* pAttachmentInfo = new AttachmentInfo();
            pAttachmentInfo->pszTempFile           = lpszFQFileName;
            pAttachmentInfo->pszRealName           = lpszRealName;
            pAttachmentInfo->pszContentDisposition = pszCD;
            pAttachmentInfo->pszContentType        = pszAttachContentType;
            m_vAttachments.push_back(pAttachmentInfo);

            #ifdef _DEBUG
                if (stricmp(pAttachmentInfo->pszRealName, "marketing_strategy.pptx")==0)
                    LOG_GEN(_T("Found"));
            #endif

            // Above is all deleted in COMMapiAccount.cpp
        }
    }
    delete[] pszContentType;
    return S_OK;
}

void MAPIRfc2445::GenerateContentDisposition(LPSTR *ppszCD, LPSTR pszFilename)
{
    LOGFN_TRACE_NO;
    mimepp::String theCD;
    theCD.append("Content-Disposition: form-data; name=\"");
    theCD.append(pszFilename);
    theCD.append("\"; filename=\"");
    theCD.append(pszFilename);
    theCD.append("\"");

    const char *pFinal = theCD.c_str();
    Zimbra::Util::CopyString(*ppszCD, (LPSTR)pFinal);
}

void MAPIRfc2445::GetContentType(mimepp::Headers& headers, LPSTR *ppStr)
{
    LOGFN_TRACE_NO;
    mimepp::MediaType &theMediaType = headers.contentType();
    const mimepp::String &contentType = theMediaType.type();
    const mimepp::String &contentSubType = theMediaType.subtype();

    mimepp::String finalContentType;
    finalContentType.append(contentType);
    finalContentType.append("/");
    finalContentType.append(contentSubType);

    const char *pType = finalContentType.c_str();
    Zimbra::Util::CopyString(*ppStr, (LPSTR)pType);
}
