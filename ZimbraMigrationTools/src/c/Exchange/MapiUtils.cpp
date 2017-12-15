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
#include "common.h"
#include "Exchange.h"
#include <strsafe.h>
#pragma comment(lib, "netapi32.lib")
#include <lm.h>
#include <mspst.h>
#include <ntsecapi.h>

#include <Dsgetdc.h>
#include <lmcons.h>
#include <lmapibuf.h>
#include "psapi.h"

//#include <Ntstatus.h>

// Attach props
enum AttachPropIdx
{
    ATTACH_METHOD = 0, ATTACH_CONTENT_ID, ATTACH_LONG_FILENAME, WATTACH_LONG_FILENAME,
    ATTACH_FILENAME, ATTACH_MIME_TAG, ATTACH_DISPLAY_NAME, WATTACH_DISPLAY_NAME,
    ATTACH_CONTENT_LOCATION, ATTACH_DISPOSITION, ATTACH_EXTENSION, ATTACH_ENCODING,
    NATTACH_PROPS
};

SizedSPropTagArray(NATTACH_PROPS, attachProps) = {
    NATTACH_PROPS, {
        PR_ATTACH_METHOD, PR_ATTACH_CONTENT_ID_A, PR_ATTACH_LONG_FILENAME_A,
        PR_ATTACH_LONG_FILENAME_W, PR_ATTACH_FILENAME_A, PR_ATTACH_MIME_TAG_A,
        PR_DISPLAY_NAME_A, PR_DISPLAY_NAME_W, PR_ATTACH_CONTENT_LOCATION_A,
        PR_ATTACH_DISPOSITION_A, PR_ATTACH_EXTENSION_A, PR_ATTACH_ENCODING
    }
};

/** special folder stuff **/
inline void SFAddEidBin(UINT propIdx, LPSPropValue lpProps, ExchangeSpecialFolderId exchSFID, SBinaryArray *pEntryIds)
{
    if (PROP_TYPE(lpProps[propIdx].ulPropTag) == PT_ERROR)
    {
        wstring s;
        switch(exchSFID)
        {
            case EXSFID_INBOX:                 s = _T("INBOX");                break;
            case EXSFID_IPM_SUBTREE:           s = _T("IPM_SUBTREE");          break;
            case EXSFID_CALENDAR:              s = _T("CALENDAR");             break; 
            case EXSFID_CONTACTS:              s = _T("CONTACTS");             break;
            case EXSFID_DRAFTS:                s = _T("DRAFTS");               break;
            case EXSFID_JOURNAL:               s = _T("JOURNAL");              break;
            case EXSFID_NOTE:                  s = _T("NOTE");                 break;
            case EXSFID_TASK:                  s = _T("TASK");                 break;
            case EXSFID_OUTBOX:                s = _T("OUTBOX");               break;
            case EXSFID_SENTMAIL:              s = _T("SENTMAIL");             break;
            case EXSFID_TRASH:                 s = _T("TRASH");                break;
            case EXSFID_SYNC_CONFLICTS:        s = _T("SYNC_CONFLICTS");       break;
            case EXSFID_SYNC_ISSUES:           s = _T("SYNC_ISSUES");          break;
            case EXSFID_SYNC_LOCAL_FAILURES:   s = _T("SYNC_LOCAL_FAILURES");  break;
            case EXSFID_SYNC_SERVER_FAILURES:  s = _T("SYNC_SERVER_FAILURES"); break;
            case EXSFID_JUNK_MAIL:             s = _T("JUNK_MAIL");            break;
            case EXSFID_SUGGESTED_CONTACTS:    s = _T("SUGGESTED_CONTACTS");   break;
            default: _ASSERT(FALSE);
        }
        LOG_TRACE(_T("EID of special folder '%s' unavailable"), s.c_str());

        pEntryIds->lpbin[exchSFID].cb = 0;
        pEntryIds->lpbin[exchSFID].lpb = NULL;
    }
    else
    {
        ULONG size = lpProps[propIdx].Value.bin.cb;
        pEntryIds->lpbin[exchSFID].cb = size;

        MAPIAllocateBuffer(size, (LPVOID *)&(pEntryIds->lpbin[exchSFID].lpb));
        memcpy(pEntryIds->lpbin[exchSFID].lpb, lpProps[propIdx].Value.bin.lpb, size);
    }
}

inline void SFAddEidMVBin(UINT mvIdx, UINT propIdx, LPSPropValue lpProps, UINT folderId, SBinaryArray *pEntryIds)
{
    if (PROP_TYPE(lpProps[propIdx].ulPropTag) == PT_ERROR)
    {
        pEntryIds->lpbin[folderId].cb = 0;
        pEntryIds->lpbin[folderId].lpb = NULL;
    }
    else 
    if (lpProps[propIdx].Value.MVbin.cValues > mvIdx)
    {
        ULONG size = lpProps[propIdx].Value.MVbin.lpbin[mvIdx].cb;
        pEntryIds->lpbin[folderId].cb = size;

        MAPIAllocateBuffer(size, (LPVOID *)&(pEntryIds->lpbin[folderId].lpb));
        memcpy(pEntryIds->lpbin[folderId].lpb, (lpProps[propIdx].Value.MVbin.lpbin[mvIdx].lpb), size);
    }
    else
    {
        pEntryIds->lpbin[folderId].cb = 0;
        pEntryIds->lpbin[folderId].lpb = NULL;
    }
}

HRESULT SFGetEidRenExBin(WORD wSFType, LPSPropValue lpProp, ULONG &cbSFEid, LPBYTE& pSFEid)
//
// Reads a value out of PR_ADDITIONAL_REN_ENTRYIDS_EX
//
{
    HRESULT hr = MAPI_E_NOT_FOUND;

    cbSFEid = 0;
    pSFEid = NULL;

    if (lpProp->ulPropTag == PR_ADDITIONAL_REN_ENTRYIDS_EX)
    {
        LPBYTE pCurrent = lpProp->Value.bin.lpb;
        ULONG ulRemaining = lpProp->Value.bin.cb;

        WORD wBlockType = 0;
        WORD wBlockSize = 0;
        WORD wElementType = 0;
        WORD wElementSize = 0;

        while(ulRemaining > 0 && hr == MAPI_E_NOT_FOUND)
        {
            if (ulRemaining > sizeof (WORD))
            {
                wBlockType = MAKEWORD(*pCurrent, *(pCurrent+1));
                pCurrent += sizeof (WORD);
                ulRemaining -= sizeof (WORD);
            }

            if (ulRemaining > sizeof (WORD))
            {
                wBlockSize = MAKEWORD(*pCurrent, *(pCurrent+1));
                pCurrent += sizeof (WORD);
                ulRemaining -= sizeof (WORD);
            }

            if (ulRemaining < wBlockSize)
            {
                LOG_ERROR(_T("Parse error: wBlockSize=%d, ulRemaining=%d"), wBlockSize, ulRemaining);
                ulRemaining = 0;
            }
            else 
            if (wBlockType == REN_EX_BT_END)
            {
                ulRemaining = 0;
            }
            else 
            if (wBlockType != wSFType)
            {
                // If this is not what we are looking for, skip the whole block.
                pCurrent += wBlockSize;
                ulRemaining -= wBlockSize;
            }
            else
            {
                // We have a match.
                // Process the elements until we find the entryid.
                while (wBlockSize > 0 && hr == MAPI_E_NOT_FOUND)
                {
                    if (wBlockSize > sizeof (WORD))
                    {
                        wElementType = MAKEWORD(*pCurrent, *(pCurrent+1));
                        pCurrent += sizeof (WORD);
                        ulRemaining -= sizeof (WORD);
                        wBlockSize -= sizeof (WORD);
                    }

                    if (wBlockSize > sizeof (WORD))
                    {
                        wElementSize = MAKEWORD(*pCurrent, *(pCurrent+1));
                        pCurrent += sizeof (WORD);
                        ulRemaining -= sizeof (WORD);
                        wBlockSize -= sizeof (WORD);
                    }

                    if (wBlockSize < wElementSize)
                    {
                        LOG_ERROR(_T("Parse error: wBlockSize=%d, wElementSize=%d"), wBlockSize, wElementSize);
                        ulRemaining = 0;
                        wBlockSize = 0;
                    }
                    else if (wElementType == REN_EX_ET_END)
                    {
                        pCurrent += wBlockSize;
                        ulRemaining -= wBlockSize;
                        wBlockSize = 0;
                    }
                    else if (wElementType != REN_EX_ET_EID)
                    {
                        pCurrent += wElementSize;
                        ulRemaining -= wElementSize;
                        wBlockSize -= wElementSize;
                    }
                    else
                    {
                        cbSFEid = wElementSize;
                        pSFEid = pCurrent;
                        hr = S_OK;
                    }
                }
            }
        }
    }

    return hr;
}


void SFAddEidRenExBin(WORD wSFType, UINT propIdx, LPSPropValue lpProps, UINT folderId, SBinaryArray *pEntryIds)
{
    if (PROP_TYPE(lpProps[propIdx].ulPropTag) != PT_ERROR)
    {
        ULONG cbSFEid; LPBYTE pSFEid;
        HRESULT hr = SFGetEidRenExBin(wSFType, &lpProps[propIdx], cbSFEid, pSFEid);
        if (SUCCEEDED (hr))
        {
            hr = MAPIAllocateBuffer(cbSFEid, (LPVOID *)&(pEntryIds->lpbin[folderId].lpb));
            if (SUCCEEDED (hr))
            {
                pEntryIds->lpbin[folderId].cb = cbSFEid;
                memcpy(pEntryIds->lpbin[folderId].lpb, pSFEid, cbSFEid);
            }
        }
    }
}

HRESULT Zimbra::MAPI::Util::HrMAPIFindDefaultMsgStore(LPMAPISESSION lplhSession, SBinary &bin)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;
    ULONG cRows = 0;
    ULONG i = 0;

    bin.lpb = NULL;
    bin.cb = 0;

    Zimbra::Util::ScopedInterface<IMAPITable> lpTable;
    Zimbra::Util::ScopedRowSet lpRows(NULL);

    SizedSPropTagArray(2, rgPropTagArray) = {2, { PR_DEFAULT_STORE, PR_ENTRYID }};

    // Get the list of available message stores from MAPI
    LOG_TRACE(_T("Getting MsgStoresTable..."));
    if (FAILED(hr = lplhSession->GetMsgStoresTable(0, lpTable.getptr())))
        throw MapiUtilsException(hr, L"Util:: HrMAPIFindDefaultMsgStore(): GetMsgStoresTable Failed.", ERR_DEF_MSG_STORE,  __LINE__,  __FILE__);

    // Get the row count for the message recipient table
    LOG_TRACE(_T("Getting RowCount..."));
    if (FAILED(hr = lpTable->GetRowCount(0, &cRows)))
        throw MapiUtilsException(hr, L"Util:: HrMAPIFindDefaultMsgStore(): GetRowCount Failed.",ERR_DEF_MSG_STORE, __LINE__, __FILE__);

    LOG_TRACE(_T("...%d row(s)"), cRows);

    // Set the columns to return
    LOG_TRACE(_T("SetColumns..."));
    if (FAILED(hr = lpTable->SetColumns((LPSPropTagArray) & rgPropTagArray, 0)))
        throw MapiUtilsException(hr, L"Util:: HrMAPIFindDefaultMsgStore(): SetColumns Failed.",ERR_DEF_MSG_STORE, __LINE__, __FILE__);

    // Go to the beginning of the recipient table for the envelope
    if (FAILED(hr = lpTable->SeekRow(BOOKMARK_BEGINNING, 0, NULL)))
        throw MapiUtilsException(hr, L"Util:: HrMAPIFindDefaultMsgStore(): SeekRow Failed.",ERR_DEF_MSG_STORE, __LINE__, __FILE__);

    // Read all the rows of the table
    LOG_TRACE(_T("QueryRows..."));
    if (FAILED(hr = lpTable->QueryRows(cRows, 0, lpRows.getptr())))
        throw MapiUtilsException(hr, L"Util:: HrMAPIFindDefaultMsgStore(): QueryRows Failed.",ERR_DEF_MSG_STORE, __LINE__, __FILE__);

    if (lpRows->cRows == 0)
        return MAPI_E_NOT_FOUND;

    // Get EIDs
    LOG_TRACE(_T("Getting EIDs..."));
    for (i = 0; i < cRows; i++)
    {
        if (lpRows->aRow[i].lpProps[0].Value.b == TRUE)
        {
            bin.cb = lpRows->aRow[i].lpProps[1].Value.bin.cb;
            if (FAILED(MAPIAllocateBuffer(bin.cb, (void **)&bin.lpb)))
            {
                throw MapiUtilsException(hr, L"Util:: HrMAPIFindDefaultMsgStore(): MAPIAllocateBuffer Failed.", ERR_DEF_MSG_STORE, __LINE__,  __FILE__);
            }
            // Copy entry ID of message store
            CopyMemory(bin.lpb, lpRows->aRow[i].lpProps[1].Value.bin.lpb, bin.cb);
            break;
        }
    }

    if (bin.lpb == NULL)
        return MAPI_E_NOT_FOUND;

    return hr;
}

HRESULT Zimbra::MAPI::Util::MailboxLogon(LPMAPISESSION pSession, LPMDB pAdminStore, LPCWSTR pStoreDn, LPCWSTR pMailboxDn, LPMDB *ppMdb)
//
// Logs on to a mailbox using IExchangeManageStore to get the store entryID
//
{
    LOGFN_TRACE_NO;

    // ----------------------------------
    // Convert the dn's to ascii
    // ----------------------------------
    LPSTR pStoreDnA = NULL;
    LPSTR pMailboxDnA = NULL;
    WtoA(pStoreDn, pStoreDnA);
    WtoA(pMailboxDn, pMailboxDnA);

    // ----------------------------------------
    // QueryInterface IID_IExchangeManageStore
    // ----------------------------------------
    LPEXCHANGEMANAGESTORE pXManageStore = NULL;
    HRESULT hr = pAdminStore->QueryInterface(IID_IExchangeManageStore, (LPVOID *)&pXManageStore);
    if (FAILED(hr))
    {
        SafeDelete(pStoreDnA);
        SafeDelete(pMailboxDnA);
        throw MapiUtilsException(hr, L"Util:: MailboxLogon(): QueryInterface Failed.", ERR_MBOX_LOGON, __LINE__, __FILE__);
    }


    // See http://microsoft.public.win32.programmer.messaging.narkive.com/GggPw0Nb/iexchangemanagestore-createstoreentryid-problem-exchange-2003 for what CreateStoreEntryID needs

    // ----------------------------------
    // CreateStoreEntryID
    // ----------------------------------
    SBinary storeEID;
    storeEID.cb = 0;
    storeEID.lpb = NULL;
    Zimbra::Util::ScopedBuffer<BYTE> pB(storeEID.lpb); // ensure free
    hr = pXManageStore->CreateStoreEntryID(pStoreDnA, pMailboxDnA, OPENSTORE_HOME_LOGON | OPENSTORE_USE_ADMIN_PRIVILEGE | OPENSTORE_TAKE_OWNERSHIP, &storeEID.cb, (LPENTRYID *)&storeEID.lpb);

    SafeDelete(pStoreDnA);
    SafeDelete(pMailboxDnA);
    if (pXManageStore != NULL)
        pXManageStore->Release();

    if (FAILED(hr))
        throw MapiUtilsException(hr, L"Util:: MailboxLogon(): CreateStoreEntryID Failed.",ERR_MBOX_LOGON, __LINE__, __FILE__);

    // ----------------------------------
    // OpenMsgStore
    // ----------------------------------
    hr = pSession->OpenMsgStore(0, storeEID.cb, (LPENTRYID)storeEID.lpb, NULL, MDB_ONLINE | MAPI_BEST_ACCESS | MDB_NO_MAIL | MDB_TEMPORARY | MDB_NO_DIALOG, ppMdb);
    if (hr == MAPI_E_FAILONEPROVIDER)
        hr = pSession->OpenMsgStore(NULL, storeEID.cb, (LPENTRYID)storeEID.lpb, NULL, MDB_ONLINE | MAPI_BEST_ACCESS, ppMdb);
    else 
    if (hr == MAPI_E_UNKNOWN_FLAGS)
        hr = pSession->OpenMsgStore(0, storeEID.cb, (LPENTRYID)storeEID.lpb, NULL, MAPI_BEST_ACCESS | MDB_NO_MAIL | MDB_TEMPORARY | MDB_NO_DIALOG, ppMdb);

    if (FAILED(hr))
        throw MapiUtilsException(hr, L"Util:: MailboxLogon(): OpenMsgStore Failed.",ERR_MBOX_LOGON, __LINE__, __FILE__);

    return hr;
}

HRESULT Zimbra::MAPI::Util::LookupUserInActiveDirectory(LPCWSTR lpszADHostName, LPCWSTR lpszUser, LPCWSTR lpszPwd, wstring* psUserDN, wstring* psUserLegacyDN, wstring* psMSExchHomeServerName)
//
// lpszADServer: AD server to search
// lpszUser:     name of user whose mailbox we are going to want to open
//
// Search active directory lpszADHostName for container of user lpszUser and pulls out some attributes
// NB You can browse the contents of the active directory using SysInternals tool ADExplorer https://technet.microsoft.com/en-us/sysinternals/adexplorer.aspx
{
    LOGFN_TRACE_NO;

    // See https://en.wikipedia.org/wiki/Lightweight_Directory_Access_Protocol

    if (psUserDN)
        *psUserDN = L"";
    if (psUserLegacyDN)
        *psUserLegacyDN = L"";
    if (psMSExchHomeServerName)
        *psMSExchHomeServerName = L"";

    // -----------------------------------------------------------------------------------
    // Get IDirectorySearch Object
    // -----------------------------------------------------------------------------------
    wstring strADServer = L"LDAP://";
    strADServer += lpszADHostName;
    LOG_TRACE(L"AD Server:             '%s'", strADServer.c_str());
    LOG_TRACE(L"User:                  '%s'", lpszUser);

    // First try: NULL lpszUser
    CComPtr<IDirectorySearch> pDirSearch;
    HRESULT hr = ADsOpenObject(strADServer.c_str(), NULL /*lpszUser*/, lpszPwd, ADS_SECURE_AUTHENTICATION, IID_IDirectorySearch, (void **)&pDirSearch);
    if (FAILED(hr))
    {
        if (hr == 0x8007052e) // credentials are not valid
        {
            // Second try: specify lpszUser
            hr = ADsOpenObject(strADServer.c_str(), lpszUser, lpszPwd, ADS_SECURE_AUTHENTICATION, IID_IDirectorySearch, (void **)&pDirSearch);
            if (FAILED(hr)||(pDirSearch==NULL))
                throw MapiUtilsException(hr, L"Util::LookupUserInActiveDirectory(): ADsOpenObject Failed(With credentials).", ERR_ADOBJECT_OPEN, __LINE__, __FILE__);
        }
        else
            throw MapiUtilsException(hr, L"Util::LookupUserInActiveDirectory(): ADsOpenObject Failed.(W/o credentials)", ERR_ADOBJECT_OPEN, __LINE__, __FILE__);
    }
   
    // -----------------------------------------------------------------------------------
    // Set Filter that will locate the user's AD container
    // -----------------------------------------------------------------------------------
    // (&((&(objectCategory=user)(objectClass=user)))(|(cn=<USER>)(sAMAccountName=<USER>)))

    // i.e. (objectCategory=user) && (objectClass=user) && (cn=<USER> || sAMAccountName=<USER>)

    wstring strFilter = _T("(&((&(objectCategory=user)(objectClass=user)))(|(cn=");
    strFilter += lpszUser;
    strFilter += L")";
    strFilter += L"(sAMAccountName=";
    strFilter += lpszUser;
    strFilter += L")))";
    LOG_TRACE(L"Search Filter:         '%s'",strFilter.c_str());

    // -----------------------------------------------------------------------------------
    // Set Search Preferences
    // -----------------------------------------------------------------------------------
    ADS_SEARCHPREF_INFO searchPrefs[2];
    searchPrefs[0].dwSearchPref = ADS_SEARCHPREF_SEARCH_SCOPE;
    searchPrefs[0].vValue.dwType = ADSTYPE_INTEGER;
    searchPrefs[0].vValue.Integer = ADS_SCOPE_SUBTREE;
  
    searchPrefs[1].dwSearchPref = ADS_SEARCHPREF_SIZE_LIMIT;
    searchPrefs[1].vValue.dwType = ADSTYPE_INTEGER;
    searchPrefs[1].vValue.Integer = 1; // Ask for only one object that satisfies the criteria

    pDirSearch->SetSearchPreference(searchPrefs, 2);

    // ------------------------------------------------------------------------------------------
    // Retrieve the "distinguishedName", "legacyExchangeDN" and "msExchHomeServerName" attributes
    // ------------------------------------------------------------------------------------------
    // Notes on legacyExchangeDN:
    // - http://www.msexchange.org/articles-tutorials/exchange-server-2003/planning-architecture/Understanding-LegacyExchangeDN.html
    // - https://social.technet.microsoft.com/Forums/exchange/en-US/7e477b05-9a0c-4fb1-9470-f40338c8cfb9/what-does-legacyexchangedn-do?forum=exchangesvrgenerallegacy

    ADS_SEARCH_HANDLE hSearch;
    LPWSTR pAttributes[] = { L"distinguishedName", L"legacyExchangeDN", L"msExchHomeServerName" };
    hr = pDirSearch->ExecuteSearch((LPWSTR)strFilter.c_str(), pAttributes, 3, &hSearch);
    if (FAILED(hr))
        throw MapiUtilsException(hr, L"Util:: LookupUserInActiveDirectory(): ExecuteSearch() Failed.", ERR_AD_SEARCH, __LINE__, __FILE__);

    ADS_SEARCH_COLUMN dnCol;

    while (SUCCEEDED(hr = pDirSearch->GetNextRow(hSearch)))
    {
        if (S_OK == hr)
        {
            // distinguishedName
            if (psUserDN)
            {
                hr = pDirSearch->GetColumn(hSearch, pAttributes[0], &dnCol);
                if (FAILED(hr))
                {
                    LOG_ERROR(_T("distinguishedName NOT FOUND"));
                    break;
                }
                *psUserDN = dnCol.pADsValues->CaseIgnoreString;
                LOG_TRACE(L"distinguishedName:     '%s'", psUserDN->c_str());
            }

            // legacyExchangeDN
            if (psUserLegacyDN)
            {
                hr = pDirSearch->GetColumn(hSearch, pAttributes[1], &dnCol);
                if (FAILED(hr))
                {
                    LOG_ERROR(_T("legacyExchangeDN NOT FOUND"));
                    break;
                }
                *psUserLegacyDN = dnCol.pADsValues->CaseIgnoreString;
                LOG_TRACE(L"legacyExchangeDN:      '%s'", psUserLegacyDN->c_str());
            }

            // msExchHomeServerName
            if (psMSExchHomeServerName)
            {
                hr = pDirSearch->GetColumn(hSearch, pAttributes[2], &dnCol);
                if (FAILED(hr))
                {
                    LOG_ERROR(_T("msExchHomeServerName NOT FOUND"));
                    break;
                }
                *psMSExchHomeServerName = dnCol.pADsValues->CaseIgnoreString;
                LOG_TRACE(L"msExchHomeServerName:  '%s'", psMSExchHomeServerName->c_str());
            }

            pDirSearch->CloseSearchHandle(hSearch);
            return S_OK;
        }
        else 
        if (S_ADS_NOMORE_ROWS == hr)
        {
            // Call ADsGetLastError to see if the search is waiting for a response.
            DWORD dwError = ERROR_SUCCESS;
            WCHAR szError[512];
            WCHAR szProvider[512];

            ADsGetLastError(&dwError, szError, 512, szProvider, 512);
            if (ERROR_MORE_DATA != dwError)
                break;
        }
        else
            break;
    }

    pDirSearch->CloseSearchHandle(hSearch);

    if (psUserDN && psUserDN->empty())
    {
        wstring errorMessage = L"Util::LookupUserInActiveDirectory(): distinguishedName  not found!";			
        throw MapiUtilsException(hr, errorMessage.c_str(), ERR_AD_NOROWS, __LINE__, __FILE__);
    }
    if (psUserLegacyDN && psUserLegacyDN->empty())
    {
        wstring errorMessage = L"Util::LookupUserInActiveDirectory(): legacyExchangeDN  not found!";			
        throw MapiUtilsException(hr, errorMessage.c_str(), ERR_AD_NOROWS, __LINE__, __FILE__);
    }

    return S_OK;
}

void Zimbra::MAPI::Util::GetUserDnAndServerDnFromProfile(LPMAPISESSION pSession, 
                                                         wstring* psExchangeServerDn, 
                                                         wstring* psExchangeUserDn, 
                                                         wstring* psExchangeServerHostName)
//
// Called for a Server Migration
// Opens the global profile section for the current MAPI session, reads PR_PROFILE_HOME_SERVER_DN, PR_PROFILE_USER, PR_PROFILE_HOME_SERVER_ADDRS
// and computes the 3 out wstrings from these. If any not found, the corresponding string will be left empty.
{
    LOGFN_TRACE_NO;
    HRESULT hr = S_OK;

    if (psExchangeServerDn)
        *psExchangeServerDn = L""; 
    if (psExchangeUserDn)
        *psExchangeUserDn = L""; 
    if (psExchangeServerHostName)
        *psExchangeServerHostName = L""; 

    // ---------------------------------------
    // Get IMsgServiceAdmin
    // ---------------------------------------
    Zimbra::Util::ScopedInterface<IMsgServiceAdmin> pServiceAdmin;
    if (FAILED(hr = pSession->AdminServices(0, pServiceAdmin.getptr())))
        throw MapiUtilsException(hr, L"Util::GetUserDnAndServerDnFromProfile(): AdminServices.", ERR_PROFILE_DN, __LINE__, __FILE__);

    // ---------------------------------------
    // Open GLOBAL profile section
    // ---------------------------------------
    Zimbra::Util::ScopedInterface<IProfSect> pProfileSection;
    if (FAILED(hr = pServiceAdmin->OpenProfileSection((LPMAPIUID)GLOBAL_PROFILE_SECTION_GUID, NULL, 0, pProfileSection.getptr())))
        throw MapiUtilsException(hr, L"Util::GetUserDnAndServerDnFromProfile(): OpenProfileSection.", ERR_PROFILE_DN, __LINE__,  __FILE__);

    // ----------------------------------------------------------------------------------
    // Get props PR_PROFILE_HOME_SERVER_DN, PR_PROFILE_USER, PR_PROFILE_HOME_SERVER_ADDRS
    // ----------------------------------------------------------------------------------
    ULONG nVals = 0;
    Zimbra::Util::ScopedBuffer<SPropValue> pPropValues;
    SizedSPropTagArray(3, profileProps) = {3, { PR_PROFILE_HOME_SERVER_DN, PR_PROFILE_USER, PR_PROFILE_HOME_SERVER_ADDRS }}; // DCB_BUG_103896 For OL2016 - none of these props available in a profile created with OL2016 :-(
    if (FAILED(hr = pProfileSection->GetProps((LPSPropTagArray) & profileProps, 0, &nVals, pPropValues.getptr())))
        throw MapiUtilsException(hr, L"Util::GetUserDnAndServerDnFromProfile(): GetProps.", ERR_PROFILE_DN, __LINE__, __FILE__);

    if (nVals != 3)
        throw MapiUtilsException(hr, L"Util::GetUserDnAndServerDnFromProfile(): nVals not 3.", ERR_PROFILE_DN, __LINE__, __FILE__);

    DumpPropValues(LOGLEVEL_CMNT_GEN, nVals, pPropValues.get());

    // --------------------------------------------------------
    // ExchangeServerDn from PR_PROFILE_HOME_SERVER_DN
    // --------------------------------------------------------
    if (psExchangeServerDn)
    {
        if (pPropValues[0].ulPropTag == PR_PROFILE_HOME_SERVER_DN)  // Critical - reqd by IExchangeManageStore in OpenUserStore()
        {
            LPWSTR pwszExchangeServerDN = NULL;
            AtoW(pPropValues[0].Value.lpszA, pwszExchangeServerDN);
            *psExchangeServerDn = pwszExchangeServerDN;
            SafeDelete(pwszExchangeServerDN);
            LOG_TRACE(_T("ExchangeSvrDn:          '%s'"), psExchangeServerDn->c_str());
        }
        else
            LOG_WARNING(L"Util::GetUserDnAndServerDnFromProfile(): PR_PROFILE_HOME_SERVER_DN unavailable in profile.");
    }

    // --------------------------------------------------------
    // ExchangeUserDn from PR_PROFILE_USER
    // --------------------------------------------------------
    if (psExchangeUserDn)
    {
        if (pPropValues[1].ulPropTag == PR_PROFILE_USER) // Caller doesn't need this at all
        {
            LPWSTR pwszExchangeUserDn = NULL;
            AtoW(pPropValues[1].Value.lpszA, pwszExchangeUserDn);
            *psExchangeUserDn = pwszExchangeUserDn;
            SafeDelete(pwszExchangeUserDn);
            LOG_TRACE(_T("ExchangeUserDn:         '%s'"), psExchangeUserDn->c_str());
        }
        else
            LOG_WARNING(L"Util::GetUserDnAndServerDnFromProfile(): PR_PROFILE_USER unavailable in profile.");
    }

    // --------------------------------------------------------
    // ExchangeServerHostName from PR_PROFILE_HOME_SERVER_ADDRS
    // --------------------------------------------------------
    if (psExchangeServerHostName)
    {
        if (pPropValues[2].ulPropTag == PR_PROFILE_HOME_SERVER_ADDRS) // Caller only needs this if machine not on domain
        {
            size_t cVals = pPropValues[2].Value.MVszA.cValues;
            string strExchangeHomeServer;
            for (ULONG i = 0; i < cVals; ++i)
            {
                strExchangeHomeServer = pPropValues[2].Value.MVszA.lppszA[i];       
                //LOG_TRACE(_T("Examining '%hs'"), strExchangeHomeServer.c_str());

                string::size_type npos = strExchangeHomeServer.find("ncacn_ip_tcp:");
                if (string::npos != npos)
                {
                    strExchangeHomeServer = strExchangeHomeServer.substr(13);

                    LPWSTR pwszExchangeServerHostName = NULL;
                    AtoW(strExchangeHomeServer.c_str(),   pwszExchangeServerHostName);
                    *psExchangeServerHostName = pwszExchangeServerHostName;
                    SafeDelete(pwszExchangeServerHostName);

                    LOG_TRACE(_T("ExchangeServerHostName: '%s'"), psExchangeServerHostName->c_str());
                }
            } // for
        }
        else
            LOG_WARNING(L"Util::GetUserDnAndServerDnFromProfile(): PR_PROFILE_HOME_SERVER_ADDRS unavailable in profile.");
    }
}

HRESULT Zimbra::MAPI::Util::HrMAPIFindIPMSubtree(LPMDB lpMdb, SBinary &bin)
{
    LOGFN_TRACE_NO;

    Zimbra::Util::ScopedBuffer<SPropValue> lpEID;
    HRESULT hr = S_OK;

    if (FAILED(hr = HrGetOneProp(lpMdb, PR_IPM_SUBTREE_ENTRYID, lpEID.getptr())))
        throw MapiUtilsException(hr, L"Util::HrMAPIFindIPMSubtree(): HrGetOneProp Failed.", ERR_IPM_SUBTREE, __LINE__, __FILE__);

    bin.cb = lpEID->Value.bin.cb;
    if (FAILED(MAPIAllocateBuffer(lpEID->Value.bin.cb, (void **)&bin.lpb)))
        throw MapiUtilsException(hr, L"Util:: HrMAPIFindDefaultMsgStore(): MAPIAllocateBuffer Failed.", ERR_IPM_SUBTREE, __LINE__,  __FILE__);

    // Copy entry ID of message store
    CopyMemory(bin.lpb, lpEID->Value.bin.lpb, lpEID->Value.bin.cb);

    return S_OK;
}

ULONG Zimbra::MAPI::Util::IMAPHeaderInfoPropTag(LPMAPIPROP lpMapiProp)
{
    LOGFN_TRACE_NO;

    HRESULT hRes = S_OK;
    LPSPropTagArray lpNamedPropTag = NULL;
    MAPINAMEID NamedID = { 0 };
    LPMAPINAMEID lpNamedID = NULL;
    ULONG ulIMAPHeaderPropTag = PR_NULL;

    NamedID.lpguid = (LPGUID)&PSETID_COMMON;
    NamedID.ulKind = MNID_ID;
    NamedID.Kind.lID = DISPID_HEADER_ITEM;
    lpNamedID = &NamedID;

    hRes = lpMapiProp->GetIDsFromNames(1, &lpNamedID, NULL, &lpNamedPropTag);
    if (SUCCEEDED(hRes) && (PROP_TYPE(lpNamedPropTag->aulPropTag[0]) != PT_ERROR))
    {
        lpNamedPropTag->aulPropTag[0] = CHANGE_PROP_TYPE(lpNamedPropTag->aulPropTag[0], PT_LONG);
        ulIMAPHeaderPropTag = lpNamedPropTag->aulPropTag[0];
    }
    MAPIFreeBuffer(lpNamedPropTag);

    return ulIMAPHeaderPropTag;
}

wstring Zimbra::MAPI::Util::ReverseDelimitedString(wstring wstrString, WCHAR *delimiter)
{
    wstring wstrresult = L"";

    // get last pos
    wstring::size_type lastPos = wstrString.length();

    // get first last delimiter
    wstring::size_type pos = wstrString.rfind(delimiter, lastPos);

    // till npos
    while (wstring::npos != pos && wstring::npos != lastPos)
    {
        wstrresult = wstrresult + wstrString.substr(pos + 1, lastPos - pos) + delimiter;
        lastPos = pos - 1;
        pos = wstrString.rfind(delimiter, lastPos);
    }

    // add till last pos
    wstrresult = wstrresult + wstrString.substr(pos + 1, lastPos - pos);
    return wstrresult;
}





// ==================================================================================================
// Special Folders
// ==================================================================================================


HRESULT Zimbra::MAPI::Util::GetMdbSpecialFolders(IN LPMDB lpMdb, IN OUT SBinaryArray *pEntryIds)
//
// Reads SF props off store
//
{
    LOGFN_TRACE_NO;

    enum { FSUB, FOUT, FSENT, FTRASH, FNPROPS };
    SizedSPropTagArray(FNPROPS, rgSFProps) = {FNPROPS, {PR_IPM_SUBTREE_ENTRYID, PR_IPM_OUTBOX_ENTRYID, PR_IPM_SENTMAIL_ENTRYID, PR_IPM_WASTEBASKET_ENTRYID}};
    ULONG cValues = 0;
    LPSPropValue lpProps = 0;
    HRESULT hr = lpMdb->GetProps((LPSPropTagArray) & rgSFProps, 0, &cValues, &lpProps);
    if (FAILED(hr))
        goto cleanup;

    SFAddEidBin(FSUB,   lpProps, EXSFID_IPM_SUBTREE, pEntryIds);
    SFAddEidBin(FOUT,   lpProps, EXSFID_OUTBOX,      pEntryIds);
    SFAddEidBin(FSENT,  lpProps, EXSFID_SENTMAIL,    pEntryIds);
    SFAddEidBin(FTRASH, lpProps, EXSFID_TRASH,       pEntryIds);

cleanup: 
    MAPIFreeBuffer(lpProps);
    return hr;
}

HRESULT Zimbra::MAPI::Util::GetInboxSpecialFolders(LPMAPIFOLDER pInbox, SBinaryArray *pEntryIds)
//
// Reads SF props off inbox
//
{
    LOGFN_TRACE_NO;

    enum { FCAL, FCONTACT, FDRAFTS, FJOURNAL, FNOTE, FTASK, FADDITIONAL_REN_ENTRYIDS, FADDITIONAL_REN_ENTRYIDS_EX, FNPROPS };
    SizedSPropTagArray(FNPROPS, rgSFProps) = {
        FNPROPS, {
            PR_IPM_APPOINTMENT_ENTRYID, 
            PR_IPM_CONTACT_ENTRYID, 
            PR_IPM_DRAFTS_ENTRYID,
            PR_IPM_JOURNAL_ENTRYID, 
            PR_IPM_NOTE_ENTRYID, 
            PR_IPM_TASK_ENTRYID,
            PR_ADDITIONAL_REN_ENTRYIDS,
            PR_ADDITIONAL_REN_ENTRYIDS_EX
        }
    };

    ULONG cValues = 0;
    LPSPropValue lpProps = 0;
    HRESULT hr = pInbox->GetProps((LPSPropTagArray) & rgSFProps, 0, &cValues, &lpProps);
    if (FAILED(hr))
        goto cleanup;

    // Some EIDs are written directly onto the inbox
    SFAddEidBin(FCAL,     lpProps, EXSFID_CALENDAR, pEntryIds);
    SFAddEidBin(FCONTACT, lpProps, EXSFID_CONTACTS, pEntryIds);
    SFAddEidBin(FDRAFTS,  lpProps, EXSFID_DRAFTS,   pEntryIds);
    SFAddEidBin(FJOURNAL, lpProps, EXSFID_JOURNAL,  pEntryIds);
    SFAddEidBin(FNOTE,    lpProps, EXSFID_NOTE,     pEntryIds);
    SFAddEidBin(FTASK,    lpProps, EXSFID_TASK,     pEntryIds);

    // Some are stashed in PR_ADDITIONAL_REN_ENTRYIDS
    if (PROP_TYPE(lpProps[FADDITIONAL_REN_ENTRYIDS].ulPropTag != PT_ERROR))
    {
        SFAddEidMVBin(0, FADDITIONAL_REN_ENTRYIDS, lpProps, EXSFID_SYNC_CONFLICTS,       pEntryIds);
        SFAddEidMVBin(1, FADDITIONAL_REN_ENTRYIDS, lpProps, EXSFID_SYNC_ISSUES,          pEntryIds);
        SFAddEidMVBin(2, FADDITIONAL_REN_ENTRYIDS, lpProps, EXSFID_SYNC_LOCAL_FAILURES,  pEntryIds);
        SFAddEidMVBin(3, FADDITIONAL_REN_ENTRYIDS, lpProps, EXSFID_SYNC_SERVER_FAILURES, pEntryIds);
        SFAddEidMVBin(4, FADDITIONAL_REN_ENTRYIDS, lpProps, EXSFID_JUNK_MAIL,            pEntryIds);
    }

    // SUGGESTED_CONTACTS is stashed in PR_ADDITIONAL_REN_ENTRYIDS_EX
    if (PROP_TYPE(lpProps[FADDITIONAL_REN_ENTRYIDS_EX].ulPropTag != PT_ERROR))
        SFAddEidRenExBin(REN_EX_BT_SUGGESTED_CONTACTS, FADDITIONAL_REN_ENTRYIDS_EX, lpProps, EXSFID_SUGGESTED_CONTACTS, pEntryIds);

cleanup: 
    MAPIFreeBuffer(lpProps);
    return hr;
}

HRESULT Zimbra::MAPI::Util::GetAllSpecialFolders(IN LPMDB lpMdb, IN OUT SBinaryArray *pEntryIds)
//
// See https://msdn.microsoft.com/en-us/library/office/cc463910.aspx for how to locate special folders
//
{
    LOGFN_TRACE_NO;

    LOG_GEN_SUMMARY(_T("Reading special folder EIDs..."));

    LPMAPIFOLDER pInbox = NULL;

    // --------------------------------------------------------
    // Create the memory for the pointers to the entry ids...
    // --------------------------------------------------------
    pEntryIds->cValues = 0;
    pEntryIds->cValues = NULL;
    HRESULT hr = MAPIAllocateBuffer(TOTAL_NUM_SPECIAL_FOLDERS * sizeof (SBinary), (LPVOID *)&(pEntryIds->lpbin));
    memset(pEntryIds->lpbin, 0, TOTAL_NUM_SPECIAL_FOLDERS * sizeof (SBinary));
    if (FAILED(hr))
        goto cleanup;

    pEntryIds->cValues = TOTAL_NUM_SPECIAL_FOLDERS;

    // --------------------------------------------------------
    // Get the special folder EIDs that are stored on the root
    // --------------------------------------------------------
    // PR_IPM_SUBTREE_ENTRYID
    // PR_IPM_OUTBOX_ENTRYID
    // PR_IPM_SENTMAIL_ENTRYID
    // PR_IPM_WASTEBASKET_ENTRYID
    hr = GetMdbSpecialFolders(lpMdb, pEntryIds);
    if (FAILED(hr))
        goto cleanup;

    // --------------------------------------------------------
    // Get the Inbox's special folder EID
    // --------------------------------------------------------
    hr = lpMdb->GetReceiveFolder(NULL, 0, &(pEntryIds->lpbin[EXSFID_INBOX].cb), (LPENTRYID *)&(pEntryIds->lpbin[EXSFID_INBOX].lpb), NULL);
    if (FAILED(hr))
    {
        pEntryIds->lpbin[EXSFID_INBOX].cb = 0;
        pEntryIds->lpbin[EXSFID_INBOX].lpb = NULL;
        goto cleanup;
    }

    ULONG obj = 0;
    hr = lpMdb->OpenEntry(pEntryIds->lpbin[EXSFID_INBOX].cb, (LPENTRYID)(pEntryIds->lpbin[EXSFID_INBOX].lpb), NULL, MAPI_DEFERRED_ERRORS, &obj, (LPUNKNOWN *)&pInbox);
    if (FAILED(hr))
        goto cleanup;

    // --------------------------------------------------------
    // Get the special folder EIDs that are stored on the inbox
    // --------------------------------------------------------
    // PR_IPM_APPOINTMENT_ENTRYID
    // PR_IPM_CONTACT_ENTRYID
    // PR_IPM_DRAFTS_ENTRYID 
    // PR_IPM_JOURNAL_ENTRYID 
    // PR_IPM_NOTE_ENTRYID 
    // PR_IPM_TASK_ENTRYID 
    // PR_ADDITIONAL_REN_ENTRYIDS (for SYNC_CONFLICTS, SYNC_ISSUES, SYNC_LOCAL_FAILURES, SYNC_SERVER_FAILURES, JUNK_MAIL)
    // PR_ADDITIONAL_REN_ENTRYIDS_EX (for SUGGESTED_CONTACTS)
    hr = GetInboxSpecialFolders(pInbox, pEntryIds);
    if (FAILED(hr))
        goto cleanup;

cleanup: 
    UlRelease(pInbox);
    return hr;
}

HRESULT Zimbra::MAPI::Util::FreeAllSpecialFolders(IN SBinaryArray *lpSFIds)
{
    HRESULT hr = S_OK;

    if (lpSFIds == NULL)
        return hr;

    SBinary *pBin = lpSFIds->lpbin;

    for (ULONG i = 0; i < lpSFIds->cValues; i++, pBin++)
    {
        if ((pBin->cb > 0) && (pBin->lpb != NULL))
            hr = MAPIFreeBuffer(pBin->lpb);
    }
    hr = MAPIFreeBuffer(lpSFIds->lpbin);
    return hr;
}

void Zimbra::MAPI::Util::PadWithSpaces(wstring& s, size_t len)
{
    int nNumSpaces = static_cast<int>(len - s.size());
    if (nNumSpaces > 0)
    {
        wstring sPadding(nNumSpaces, L' ');
        s += sPadding;
    }
}

ExchangeSpecialFolderId Zimbra::MAPI::Util::GetExchangeSpecialFolderId(IN LPMDB userStore, IN ULONG cbEntryId, IN LPENTRYID pFolderEntryId, SBinaryArray *pEntryIds)
{
    SBinary *pCurr = pEntryIds->lpbin;

    for (ULONG i = 0; i < pEntryIds->cValues; i++, pCurr++)
    {
        ULONG bResult = 0;
        userStore->CompareEntryIDs(cbEntryId, pFolderEntryId, pCurr->cb, (LPENTRYID)pCurr->lpb, 0, &bResult);
        if (bResult && pCurr->cb)
            return (ExchangeSpecialFolderId)i;
    }
    return EXSFID_NONE;
}

/*
ExchangeSpecialFolderId Zimbra::MAPI::Util::GetExchangeSpecialFolderId(IN LPMDB , IN ULONG , IN LPENTRYID , SBinaryArray *)
{
    return EXSFID_NONE;
}
*/







// ==================================================================================================
// Object picker
// ==================================================================================================

HRESULT Zimbra::MAPI::Util::GetExchangeUsersUsingObjectPicker(vector<ObjectPickerData> &vUserList)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;
    wstring wstrExchangeDomainAddress;

    hr = MAPIInitialize(NULL);
    
    CComPtr<IDsObjectPicker> pDsObjectPicker = NULL;
    hr = CoCreateInstance(CLSID_DsObjectPicker, NULL, CLSCTX_INPROC_SERVER, IID_IDsObjectPicker, (LPVOID *)&pDsObjectPicker);
    if (FAILED(hr))
    {
        MAPIUninitialize();
        throw MapiUtilsException(hr, L"Util::GetExchangeUsersUsingObjectPicker(): CoCreateInstance Failed.", ERR_OBJECT_PICKER, __LINE__,  __FILE__);
    }

    DSOP_SCOPE_INIT_INFO aScopeInit[1];
    DSOP_INIT_INFO InitInfo;

    // Initialize the DSOP_SCOPE_INIT_INFO array.
    ZeroMemory(aScopeInit, sizeof (aScopeInit));

    // Combine multiple scope types in a single array entry.
    aScopeInit[0].cbSize = sizeof (DSOP_SCOPE_INIT_INFO);
    aScopeInit[0].flType = DSOP_SCOPE_TYPE_UPLEVEL_JOINED_DOMAIN | DSOP_SCOPE_TYPE_DOWNLEVEL_JOINED_DOMAIN;

    // Set up-level and down-level filters to include only computer objects.
    // Up-level filters apply to both mixed and native modes.
    // Be aware that the up-level and down-level flags are different.
    aScopeInit[0].FilterFlags.Uplevel.flBothModes = DSOP_FILTER_USERS | DSOP_FILTER_COMPUTERS | DSOP_FILTER_WELL_KNOWN_PRINCIPALS | DSOP_FILTER_DOMAIN_LOCAL_GROUPS_DL;
    aScopeInit[0].FilterFlags.flDownlevel = DSOP_DOWNLEVEL_FILTER_USERS;

    // Initialize the DSOP_INIT_INFO structure.
    ZeroMemory(&InitInfo, sizeof (InitInfo));

    InitInfo.cbSize = sizeof (InitInfo);
    InitInfo.pwzTargetComputer = NULL;          // Target is the local computer.
    InitInfo.cDsScopeInfos = sizeof (aScopeInit) / sizeof (DSOP_SCOPE_INIT_INFO);
    InitInfo.aDsScopeInfos = aScopeInit;
    InitInfo.flOptions = DSOP_FLAG_MULTISELECT;

    enum ATTRS
    {
        EX_SERVER, EX_STORE, PROXY_ADDRS, C, CO, COMPANY, DESCRIPTION, DISPLAYNAME, GIVENNAME,
        INITIALS, L, O, STREETADDRESS, POSTALCODE, SN, ST, PHONE, TITLE, OFFICE,
        USERPRINCIPALNAME, OBJECTSID, NATTRS
    };

    LPCWSTR pAttrs[NATTRS] = {
        L"msExchHomeServerName", L"legacyExchangeDN", L"proxyAddresses", L"c", L"co",
        L"company", L"description", L"displayName", L"givenName", L"initials", L"l", L"o",
        L"streetAddress", L"postalCode", L"sn", L"st", L"telephoneNumber", L"title",
        L"physicalDeliveryOfficeName", L"userPrincipalName", L"objectSID"
    };

    InitInfo.cAttributesToFetch = NATTRS;
    InitInfo.apwzAttributeNames = pAttrs;

    // Initialize can be called multiple times, but only the last call has effect.
    // Be aware that object picker makes its own copy of InitInfo.
    hr = pDsObjectPicker->Initialize(&InitInfo);
    if (FAILED(hr))
    {
        MAPIUninitialize();
        throw MapiUtilsException(hr,L"Util::GetExchangeUsersUsingObjectPicker(): pDsObjectPicker::Initialize Failed", ERR_OBJECT_PICKER, __LINE__, __FILE__);
    }

    // Supply a window handle to the application.
    // HWND hwndParent = GetConsoleWindow();
    HWND hwndParent = CreateWindow(L"STATIC", NULL, 0, 0, 0, 0, 0, NULL, NULL, GetModuleHandle( NULL), NULL);

    CComPtr<IDataObject> pdo = NULL;
    hr = pDsObjectPicker->InvokeDialog(hwndParent, &pdo);
    if (hr == S_OK)
    {
        // process the result set
        STGMEDIUM stm;
        FORMATETC fe;

        // Get the global memory block that contain the user's selections.
        fe.cfFormat = (CLIPFORMAT)RegisterClipboardFormat(CFSTR_DSOP_DS_SELECTION_LIST);
        fe.ptd = NULL;
        fe.dwAspect = DVASPECT_CONTENT;
        fe.lindex = -1;
        fe.tymed = TYMED_HGLOBAL;

        hr = pdo->GetData(&fe, &stm);
        if (FAILED(hr))
        {
            // if (hwndParent != NULL) {
            // DestroyWindow(hwndParent);
            // }
            MAPIUninitialize();
            throw MapiUtilsException(hr, L"Util::GetExchangeUsersUsingObjectPicker(): pdo::GetData Failed", ERR_OBJECT_PICKER, __LINE__, __FILE__);
        }
        else
        {
            PDS_SELECTION_LIST pDsSelList = NULL;

            // Retrieve a pointer to DS_SELECTION_LIST structure.
            pDsSelList = (PDS_SELECTION_LIST)GlobalLock(stm.hGlobal);
            if (NULL != pDsSelList)
            {
                if (NULL != pDsSelList->aDsSelection[0].pwzUPN)
                    wstrExchangeDomainAddress = wcschr(pDsSelList->aDsSelection[0].pwzUPN, '@') + 1;

                // TO Do: //use Zimbra domain here
                CString pDomain = wstrExchangeDomainAddress.c_str();
                int nDomain = pDomain.GetLength();

                // Loop through DS_SELECTION array of selected objects.
                for (ULONG i = 0; i < pDsSelList->cItems; i++)
                {
                    ObjectPickerData opdData;

                    if (pDsSelList->aDsSelection[i].pvarFetchedAttributes->vt == VT_EMPTY)
                        continue;

                    CString exStore(LPTSTR(pDsSelList->aDsSelection[i].pvarFetchedAttributes[1].bstrVal));
                    opdData.wstrExchangeStore = exStore;

                    // VT_ARRAY | VT_VARIANT
                    SAFEARRAY *pArray = pDsSelList->aDsSelection[i].pvarFetchedAttributes[PROXY_ADDRS].parray;

                    // pArray will be empty if the account is just created and never accessed
                    if (!pArray)
                        continue;

                    // Get a pointer to the elements of the array.
                    VARIANT *pbstr;
                    SafeArrayAccessData(pArray, (void **)&pbstr);

                    CString alias;

                    // Use USERPRINCIPALNAME to provison account on Zimbra. bug: 34846
                    BSTR bstrAlias = pDsSelList->aDsSelection[i].pvarFetchedAttributes[USERPRINCIPALNAME].bstrVal;
                    alias = bstrAlias;

                    opdData.wstrUsername = alias;
                    if (!alias.IsEmpty())
                    {
                        if (alias.Find(L"@") != -1)
                        {
                            alias.Truncate(alias.Find(L"@") + 1);
                            alias += pDomain;
                            opdData.vAliases.push_back(bstrAlias);
                        }
                        else                    // make it empty. we will try it with SMTP: address next.
                        {
                            alias.Empty();
                        }
                    }
                    try
                    {
                        for (unsigned int ipa = 0; ipa < pArray->rgsabound->cElements; ipa++)
                        {
                            LPWSTR pwszAlias = pbstr[ipa].bstrVal;

                            if (!pwszAlias)
                            {
                                // Skipping alias with value NULL
                                continue;
                            }
                            // Use the primary SMTP address to create user's account at Zimbra
                            // if "alias" is not currently set and "pwszAlias" refers to primaty SMTP address.
                            if (!alias.GetLength() && (wcsncmp(pwszAlias, L"SMTP:", 5) == 0))
                            {
                                alias = pwszAlias + 5;

                                LPWSTR pwszEnd = wcschr(pwszAlias, L'@');
                                DWORD_PTR nAlias = pwszEnd - (pwszAlias + 5);

                                if (!pwszEnd)
                                    nAlias = wcslen(alias) + 1;
                                alias = alias.Left((int)nAlias);

                                CString aliasName = alias;
                                int nAliasName = aliasName.GetLength();

                                alias += _T("@");
                                alias += pDomain;
                                UNREFERENCED_PARAMETER(nAliasName);

                                opdData.vAliases.push_back(pwszAlias + 5);

                                // Start again right from the first entry as some proxyaddresses might have
                                // appeared before primary SMTP address and got skipped
                                ipa = (unsigned int)-1;
                            }
                            // If "alias" is set and "pwszAlias" does not refer to primary SMTP address
                            // add it to the list of aliases
                            else 
                            if (alias.GetLength() && (wcsncmp(pwszAlias, L"smtp:", 5) == 0))
                            {
                                LPWSTR pwszStart = pwszAlias + 5;
                                LPWSTR pwszEnd = wcschr(pwszAlias, L'@');
                                DWORD_PTR nAlias = pwszEnd - pwszStart + 1;

                                if (!pwszEnd)
                                    nAlias = wcslen(pwszStart) + 1;

                                LPWSTR pwszZimbraAlias = new WCHAR[nAlias + nDomain + 1];

                                wcsncpy(pwszZimbraAlias, pwszStart, nAlias - 1);
                                pwszZimbraAlias[nAlias - 1] = L'\0';
                                wcscat_s(pwszZimbraAlias, (nAlias + nDomain + 1), L"@");
                                wcscat_s(pwszZimbraAlias, (nAlias + nDomain + 1), pDomain);

                                wstring pwszNameTemp = L"zimbraMailAlias";

                                // Zimbra::Util::CopyString( pwszNameTemp, L"zimbraMailAlias" );
                                std::pair<wstring, wstring> p;

                                p.first = pwszNameTemp;
                                p.second = pwszZimbraAlias;
                                opdData.pAttributeList.push_back(p);

                                opdData.vAliases.push_back(pwszAlias + 5);
                            }
                        }
                        if (alias.IsEmpty())
                            continue;
                    }
                    catch (...)
                    {
                        if (alias.IsEmpty())
                        {
                            SafeArrayUnaccessData(pArray);

                            // Unknown exception while processing aliases")
                            // Moving on to next entry
                            continue;
                        }
                    }
                    SafeArrayUnaccessData(pArray);
                    for (int j = C; j < NATTRS; j++)
                    {
                        if (pDsSelList->aDsSelection[i].pvarFetchedAttributes[j].vt == VT_EMPTY)
                            continue;

                        wstring pAttrName;
                        wstring pAttrVal;
                        std::pair<wstring, wstring> p;

                        // Get the objectSID for zimbraForeignPrincipal
                        if (j == OBJECTSID)
                        {
                            void HUGEP *pArray;
                            ULONG dwSLBound;
                            ULONG dwSUBound;
                            VARIANT var = pDsSelList->aDsSelection[i].pvarFetchedAttributes[j];

                            hr = SafeArrayGetLBound(V_ARRAY(&var), 1, (long FAR *)&dwSLBound);
                            hr = SafeArrayGetUBound(V_ARRAY(&var), 1, (long FAR *)&dwSUBound);
                            hr = SafeArrayAccessData(V_ARRAY(&var), &pArray);
                            if (SUCCEEDED(hr))
                            {
                                // Convert binary SID into String Format i.e. S-1-... Format
                                LPTSTR StringSid = NULL;

                                ConvertSidToStringSid((PSID)pArray, &StringSid);

                                // Get the name of the domain
                                TCHAR acco_name[512] = { 0 };
                                TCHAR domain_name[512] = { 0 };
                                DWORD cbacco_name = 512, cbdomain_name = 512;
                                SID_NAME_USE sid_name_use;
                                BOOL bRet = LookupAccountSid(NULL, (PSID)pArray, acco_name, &cbacco_name, domain_name, &cbdomain_name, &sid_name_use);

                                if (!bRet)
                                {
                                    // LookupAccountSid Failed: %u ,GetLastError()
                                }

                                // Convert the SID as per zimbraForeignPrincipal format
                                CString strzimbraForeignPrincipal;

                                strzimbraForeignPrincipal = CString(L"ad:") + CString(acco_name);

                                // Free the buffer allocated by ConvertSidToStringSid
                                LocalFree(StringSid);

                                // Add the SID into the attribute list vector for zimbraForeignPrincipal
                                LPCTSTR lpZFP = strzimbraForeignPrincipal;

                                // Zimbra::Util::CopyString( pAttrName, L"zimbraForeignPrincipal" );
                                // Zimbra::Util::CopyString( pAttrVal, ( LPWSTR ) lpZFP );

                                p.first = L"zimbraForeignPrincipal";
                                p.second = (LPWSTR)lpZFP;
                                opdData.pAttributeList.push_back(p);

                                break;
                            }
                        }

                        BSTR b = pDsSelList->aDsSelection[i].pvarFetchedAttributes[j].bstrVal;
                        if (b != NULL)
                        {
                            // Zimbra doesnt know USERPRINCIPALNAME. Skip it.#39286
                            if (j == USERPRINCIPALNAME)
                                continue;

                            // Change the attribute name to "street" if its "streetAddress"
                            if (j == STREETADDRESS)
                            {
                                pAttrName = L"street";

                                // Zimbra::Util::CopyString( pAttrName, L"street" );
                            }
                            else
                            {
                                pAttrName = (LPWSTR)pAttrs[j];

                                // Zimbra::Util::CopyString( pAttrName, (LPWSTR)pAttrs[j] );
                            }
                            // Zimbra::Util::CopyString( pAttrVal,  (LPWSTR)b );
                            pAttrVal = (LPWSTR)b;
                            p.first = pAttrName;
                            p.second = pAttrVal;
                            opdData.pAttributeList.push_back(p);
                        }
                    }
                    vUserList.push_back(opdData);
                }
                GlobalUnlock(stm.hGlobal);
            }
            ReleaseStgMedium(&stm);
        }
    }
    // if (hwndParent != NULL) {
    // DestroyWindow(hwndParent);
    // }
    MAPIUninitialize();
    return hr;
}

HRESULT Zimbra::MAPI::Util::HrMAPIGetSMTPAddress(IN MAPISession &session, IN RECIP_INFO &recipInfo, OUT wstring &strSmtpAddress)
{
    LOGFN_VERBOSE_NO;

    LPMAILUSER pUser = NULL;
    ULONG objtype = 0;
    ULONG cVals = 0;
    HRESULT hr = S_OK;
    LPSPropValue pPropVal = NULL;

    SizedSPropTagArray(1, rgPropTags) = { 1, PR_EMS_AB_PROXY_ADDRESSES };
    if (_tcsicmp(recipInfo.pAddrType, _TEXT("SMTP")) == 0)
    {
        if (recipInfo.pEmailAddr == NULL)
            strSmtpAddress = _TEXT("");
        else
            strSmtpAddress = recipInfo.pEmailAddr;
    }
    else 
    if (_tcsicmp(recipInfo.pAddrType, _TEXT("EX")) != 0)
    {
        // unsupported sender type
        hr = E_FAIL;
    }
    else
    {
        hr = session.OpenEntry(recipInfo.cbEid, recipInfo.pEid, NULL, 0, &objtype, (LPUNKNOWN *)&pUser);
        if (FAILED(hr))
            return hr;

        hr = pUser->GetProps((LPSPropTagArray) & rgPropTags, fMapiUnicode, &cVals, &pPropVal);
        if (FAILED(hr))
        {
            UlRelease(pUser);
            return hr;
        }

        // loop through the resulting array looking for the address of type SMTP
        int nVals = pPropVal->Value.MVSZ.cValues;

        hr = E_FAIL;
        for (int i = 0; i < nVals; i++)
        {
            LPTSTR pAdr = pPropVal->Value.MVSZ.LPPSZ[i];

            if (_tcsncmp(pAdr, _TEXT("SMTP:"), 5) == 0)
            {
                strSmtpAddress = (pAdr + 5);
                hr = S_OK;
                break;
            }
        }
    }
    if (pPropVal != NULL)
        MAPIFreeBuffer(pPropVal);
    if (pUser != NULL)
        UlRelease(pUser);
    return hr;
}

BOOL Zimbra::MAPI::Util::CompareRecipients(MAPISession &session, RECIP_INFO &r1, RECIP_INFO &r2)
{
    SBinary eid1, eid2;

    eid1.cb = r1.cbEid;
    eid2.cb = r2.cbEid;
    eid1.lpb = (LPBYTE)r1.pEid;
    eid2.lpb = (LPBYTE)r2.pEid;
    if ((eid1.lpb == NULL) || (eid2.lpb == NULL))
        return FALSE;

    ULONG ulResult = FALSE;

    session.CompareEntryIDs(&eid1, &eid2, ulResult);
    return ulResult;
}

bool Zimbra::MAPI::Util::NeedsEncoding(LPSTR pStr)
{
    for (unsigned int i = 0; i < strlen(pStr); i++)
    {
        unsigned char cVal = (unsigned char)(pStr[i]);
        if (((cVal > 0) && (cVal < 32)) || ((cVal > 127) && (cVal < 256)))
            return true;
    }                                           // for( int i = 0; i < strlen(pStr); i++ )
    return false;
}

// ===================================================================================================
// class CStreamingEncoder
// ===================================================================================================
Zimbra::MAPI::Util::CStreamingEncoder::CStreamingEncoder(DWORD dwTotBytesToEncode, DWORD dwInBuffSize, BOOL bMacEncoded):
    m_dwTotBytesToEncode(dwTotBytesToEncode),
    m_pEncoder(NULL),
    m_meType(Zimbra::Util::ME_7BIT),
    m_dwBytesEncoded(0),
    m_bFirstBuff(true),
    m_bom(Zimbra::MAPI::Util::BOM_NONE),
    m_bMacEncoded(bMacEncoded),
    m_bEncodingReqsOnlyOneStep(false)
{
    LOG_TRACE(_T("Using streaming encoder"));

    if (dwTotBytesToEncode <= dwInBuffSize)
        m_bEncodingReqsOnlyOneStep = true;

    // ============================================
    // Allocate input buff
    // ============================================

    // Caller specifies size of input buffer in dwInBuffSize, but we don't allocate the full amount of this if we
    // don't need to (i.e. if the thing we're ncoding is less than it)
    m_dwSizeInBuf = min(dwInBuffSize, dwTotBytesToEncode);

    m_inBuf.bytes = (unsigned char*)new CHAR[m_dwSizeInBuf]; // dwInBuffSize typically 5Mb
    if (!m_inBuf.bytes)
    {
        LOG_ERROR(_T("Failed to allocate %d bytes for input buffer"), m_dwSizeInBuf);
        _ASSERT(FALSE);
        throw MapiUtilsException(E_OUTOFMEMORY, L"CStreamingEncoder::CStreamingEncoder: Failed to allocate input buffer", ERR_OUT_OF_MEMORY, __LINE__, __FILE__);
    }

    // ============================================
    // Allocate output buff
    // ============================================
    // This just has to be large enough to hold worst case size bloat of input buff
    // See https://developer.gnome.org/glib/stable/glib-Base64-Encoding.html
    // Base64 takes approx 1.5*inputsize bytes so x 2 is plenty
    DWORD dwSize = 2 * m_dwSizeInBuf; 
    m_outBuf.chars = (CHAR*)malloc(dwSize);
    if (!m_outBuf.chars)
    {
        LOG_ERROR(_T("Failed to allocate %d bytes for output buffer"), dwSize);
        _ASSERT(FALSE);
        throw MapiUtilsException(E_OUTOFMEMORY, L"CStreamingEncoder::CStreamingEncoder: Failed to allocate output buffer", ERR_OUT_OF_MEMORY, __LINE__, __FILE__);
    }
    m_outBuf.endPos = dwSize;

}

Zimbra::MAPI::Util::CStreamingEncoder::~CStreamingEncoder()
{
    // Free input buffer
    if (m_inBuf.bytes)
        delete[] m_inBuf.bytes;

    // Free output buffer
    if (m_outBuf.chars)
        free(m_outBuf.chars);

    // Free encoder
    if (m_pEncoder)
        delete m_pEncoder;
}


void Zimbra::MAPI::Util::CStreamingEncoder::SetBytesRead(ULONG ulBytes)
//
// Allows caller to tell the encoder how many bytes are available in the input buffer for encoding
// For all but the last buffer, this will be m_dwSizeInBuf. Last buffer will mop up remaining.
//
{
    // --------------------------------------------------
    // Mark of relevant section of input buf
    // --------------------------------------------------
    m_inBuf.pos = 0;
    m_inBuf.endPos = ulBytes;

    // Receipt of first buffer is special case because we need to:
    // - Calc BOM + skip any preamble
    // - Choose and create most appropriate encoder (either B64, QP or 7BIT Encoder) 
    // - Reserve space in m_mppsEncodedResult
    if (m_bFirstBuff)
    {
        // --------------------------------------------
        // Find BOM and skip preamble for MAC
        // --------------------------------------------
        LPVOID pReqdData = NULL; size_t ReqdDataLen = 0;
        CalcBOMAndSkipSourceEncodingPreamble(m_bMacEncoded, m_inBuf.bytes, ulBytes, pReqdData, ReqdDataLen, m_bom);

        m_inBuf.pos = (int)((CHAR*)pReqdData - (CHAR*)m_inBuf.bytes);

        // ----------------------------------------------------
        // Choose the most appropriate encoder and outbuf size
        // ----------------------------------------------------
        DWORD dwPredictedEncodedSize = 0;
        Zimbra::Util::MIME_ENCODING me = ME_BASE64;
        if (m_bEncodingReqsOnlyOneStep)
            me = Zimbra::Util::CharsetUtil::FindBestEncoding((CHAR*)m_inBuf.bytes, (int)ulBytes, true, true/*DCB_BUG_104288*/);

        m_meType = me;
        switch (m_meType)
        {
            case ME_QUOTED_PRINTABLE:  // TODO original code tries this and if exception switches to B64. How to retain this behaviour? No need - I think that would only have happened for huge strings - not a single chunk like this :-)
            {
                m_sEncoderAbbrev = _T("QP");
                m_pEncoder = new mimepp::QuotedPrintableEncoder;
                dwPredictedEncodedSize = (DWORD)((float)m_dwTotBytesToEncode*1.5);
            }
            break;

            case ME_BASE64:
            {
                m_sEncoderAbbrev = _T("B64");
                m_pEncoder = new mimepp::Base64Encoder;
                dwPredictedEncodedSize = (DWORD)((float)m_dwTotBytesToEncode*1.5);
            }
            break;

            case ME_7BIT:
            {
                // No encoder reqd - just append to the output string
                m_sEncoderAbbrev = _T("7B");
                dwPredictedEncodedSize = m_dwTotBytesToEncode;
            }
            break;
        } // switch

        // ----------------------------------------------------
        // Start the encoder
        // ----------------------------------------------------
        if (m_pEncoder)
            m_pEncoder->start();

        // ----------------------------------------------------
        // Reserve mimepp output string
        // ----------------------------------------------------
        try
        {
            LOG_TRACE(_T("Reserving %d bytes for result string"), dwPredictedEncodedSize);
            m_mppsEncodedResult.reserve(dwPredictedEncodedSize); // Bah - causes AccessViolation in mimepp libs if it can't alloc the memory
        }
        catch(...)
        {
            LOG_ERROR(_T("Failed to reserve %d bytes for result string"), dwPredictedEncodedSize);
            _ASSERT(FALSE);
            throw MapiUtilsException(E_OUTOFMEMORY, L"CStreamingEncoder::SetBytesRead: Failed to allocate result string", ERR_OUT_OF_MEMORY, __LINE__, __FILE__);
        }

        m_bFirstBuff = false;
    }

    m_outBuf.pos = 0;
}

Zimbra::Util::MIME_ENCODING Zimbra::MAPI::Util::CStreamingEncoder::GetEncodingType()
{
    return m_meType;
};

void Zimbra::MAPI::Util::CStreamingEncoder::EncodeInputBuffer()
{
    bool bInBufConsumed = false;

    if (m_pEncoder)
    {
        while (!bInBufConsumed)
        {
            DWORD dwStartPos = m_inBuf.pos;

            // ----------------------------------------------------------------------------
            // Use whichever encoder we originally decided upon to encode the input buffer
            // ----------------------------------------------------------------------------
            if (m_pEncoder)
                m_pEncoder->encodeSegment(&m_inBuf, &m_outBuf);

            DWORD dwBytesConsumed = m_inBuf.pos - dwStartPos;
            m_dwBytesEncoded += dwBytesConsumed;

            // ----------------------------------------------------------------------------
            // See if mimepp encoded the entire buffer
            // ----------------------------------------------------------------------------
            // It will only fail if the out buffer was too small. But during initialization
            // we ensured it was large enough, so this will always succeed
            if (m_inBuf.pos == m_inBuf.endPos)
            {
                // inBuf is empty - need to fill it with more data before calling encodeSegment again
                bInBufConsumed = true; // fetch another inbuf
            }

            if (m_outBuf.pos == m_outBuf.endPos)
            {
                // outBuff is full -> need to make room
                // Can no longer happen now we're encoding to an outbuf that is twice the size of the in buf
                LOG_ERROR(_T("Output buffer overflow in EncodeInputBuffer()"));
                _ASSERT(FALSE);
            }
        } // while

        // ----------------------------------------------------------------------------
        // Write the output buffer to the  overall output mimepp string
        // ----------------------------------------------------------------------------
        m_mppsEncodedResult.append(m_outBuf.chars, m_outBuf.pos);
    }
    else
    {
        // 7 bit - no encoding reqd - just copy directly from input buff to result string
        DWORD dwBytes = m_inBuf.endPos-m_inBuf.pos;
        m_mppsEncodedResult.append((char*)m_inBuf.bytes, m_inBuf.pos, dwBytes);
        m_dwBytesEncoded += dwBytes;
    }

    LOG_GEN(_T("%s encoded %d bytes to %d bytes"), m_sEncoderAbbrev.c_str(), m_dwBytesEncoded, m_mppsEncodedResult.length());
}

void Zimbra::MAPI::Util::CStreamingEncoder::Finish()
{
    if (!m_pEncoder)
        return;

    bool bFinished = false;
    while (!bFinished)
    {
        int nPrevPos = m_outBuf.pos;
        _ASSERT(m_outBuf.pos < m_outBuf.endPos);
        m_pEncoder->finish(&m_outBuf);

        // If finish() advanced output ptr, be sure to add them to our overall result string
        if (m_outBuf.pos > nPrevPos)
             m_mppsEncodedResult.append(m_outBuf.chars, nPrevPos, m_outBuf.pos-nPrevPos);

        // See if finish() call above ran out of out buff
        if (m_outBuf.pos < m_outBuf.endPos)
            break;
        else
        {
            // outBuff is full -> need to make room and call finish() again
            // Can't happen since we're encoding to an outbuf that is twice the size of inbuf
            LOG_ERROR(_T("Output buffer overflow in Finish()"));
            _ASSERT(FALSE); 
            break;
        }
    } // while

    size_t nLenEncoded = m_mppsEncodedResult.length();
    LOG_GEN(_T("Encoding finished. %d input bytes produced %d encoded bytes"), m_dwBytesEncoded, nLenEncoded);
    if (nLenEncoded < m_dwBytesEncoded)
    {
        LOG_ERROR(_T("Encoded smaller than source"));
        _ASSERT(FALSE);
    }

}

void Zimbra::MAPI::Util::CStreamingEncoder::GetInBuf(CHAR*& pInBuf, DWORD& dwLen)
{
    pInBuf = (CHAR*)m_inBuf.bytes;
    dwLen = m_dwSizeInBuf;
}

// ===================================================================================================
// END class CStreamingEncoder
// ===================================================================================================

void Zimbra::MAPI::Util::CreateMimeSubject(IN LPTSTR pSubject, IN UINT codepage, IN OUT LPSTR *ppMimeSubject)
{
    LOGFN_TRACE_NO;

    LPSTR pMBSubject = NULL;

#if UNICODE
    // convert the wide string to the appropriate multi-byte string
    // get the length required
    int nLen = WideCharToMultiByte(codepage, 0, pSubject, (int)wcslen(pSubject), NULL, 0, NULL, NULL);

    // don't forget to free this buffer...
    pMBSubject = new CHAR[nLen + 1];
    ZeroMemory(pMBSubject, nLen + 1);

    // do the conversion
    WideCharToMultiByte(codepage, 0, pSubject, (int)wcslen(pSubject), pMBSubject, nLen + 1, NULL, NULL);
#else
    // no conversion required
    pMBSubject = pSubject;
#endif
    // do we need to do RFC2047 encoding on this string?
    if (NeedsEncoding(pMBSubject))
    {
        // get the string version of the codepage
        LPSTR pCharset = NULL;
        CharsetUtil::CharsetStringFromCodePageId(codepage, &pCharset);

        // use mimepp to encode the string
        mimepp::EncodedWord subj;
        subj.setDecodedText(pMBSubject);
        subj.setCharset(pCharset);
        subj.setEncodingType('q');
        subj.assemble();

        *ppMimeSubject = new CHAR[subj.getString().length() + 1];
        strcpy_s(*ppMimeSubject, subj.getString().length() + 1, subj.getString().c_str());
        if (pCharset != NULL)
            delete[] pCharset;
    }
    else
    {
        *ppMimeSubject = new CHAR[strlen(pMBSubject) + 1];
        strcpy_s(*ppMimeSubject, strlen(pMBSubject) + 1, pMBSubject);
    }
#if UNICODE
    delete[] pMBSubject;
#endif
}

void Zimbra::MAPI::Util::ReplaceLFWithCRLF(LPSTR pszMimeMsg, UINT mimeLength, LPSTR *ppszNewMsg, size_t *pNewLength)
{
    LOGFN_TRACE_NO;

    LPSTR pNewMsg = new CHAR[mimeLength * 2 + 2];
    size_t nNewLen = 0;

    *ppszNewMsg = pNewMsg;

    bool bLastCr = false;

    for (UINT i = 0; i < mimeLength; i++, pszMimeMsg++, pNewMsg++, nNewLen++)
    {
        if (!bLastCr && (*pszMimeMsg == '\n'))
        {
            *pNewMsg = '\r';
            pNewMsg++;
            nNewLen++;
        }
        else
            bLastCr = (*pszMimeMsg == '\r');

        *pNewMsg = *pszMimeMsg;
    }
    *pNewLength = nNewLen;

    LOG_VERBOSE(_T("LF->CRLF: InLen: %d OutLen:%d"), mimeLength, nNewLen);
    // DumpBuffer( *ppszNewMsg, *pNewLength );
}

void Zimbra::MAPI::Util::OptimallyEncodeBodyAndAddToPart(mimepp::BodyPart *pPart, LPSTR pStr, size_t length)
//
// Encodes pStr using most appropriate encoding, and adds to pPart
//
{
    if (!pStr)
    {
        LOG_ERROR(_T("Invalid param"));
        _ASSERT(FALSE);
        return;
    }

    LOGFN_TRACE_NO;

    // mimepp encoder reqs the src data to be in mimepp String
    mimepp::String bodyStr(pStr, length);
    try
    {
        if (length>1024*1024)
            PauseAfterLargeMemDelta();

        // Find encoding
        MIME_ENCODING me = Zimbra::Util::CharsetUtil::FindBestEncoding(pStr, (int)length, true); // Wow 701ms for 200Mb message!

        // Encode it!
        mimepp::String encodedBodyStr;

        switch (me)
        {
            case ME_QUOTED_PRINTABLE:
            {
                // TODO: scream at hunny soft.
                try
                {
                    mimepp::QuotedPrintableEncoder encoder;
                    encodedBodyStr = encoder.encode(bodyStr);

                    pPart->headers().contentTransferEncoding().setString("Quoted-printable");
                }
                catch (...)  // if it barfs, re-encode it as base64
                {
                    LOG_WARNING(_T("QP Failed - re-encoding as B64"));
                    mimepp::Base64Encoder encoder;
                    encodedBodyStr = encoder.encode(bodyStr);

                    pPart->headers().contentTransferEncoding().setString("Base64");
                }
            }
            break;

            case ME_BASE64:
            {
                pPart->headers().contentTransferEncoding().setString("Base64");

                mimepp::Base64Encoder encoder;
                encodedBodyStr = encoder.encode(bodyStr);

                PauseAfterLargeMemDelta();
            }
            break;

            case ME_7BIT:
            {
                pPart->headers().contentTransferEncoding().setString("7bit");
                encodedBodyStr = bodyStr;
            }
            break;
        }

        pPart->body().setString(encodedBodyStr);

    }
    catch(std::exception& e)
    {
        LOG_ERROR(_T("std::exception: %hs in AddBodyToPart"), e.what());
        _ASSERT(FALSE);
        throw;
    }
    catch(...)
    {
        LOG_ERROR(_T("Unknown exception in AddBodyToPart"));
        _ASSERT(FALSE);
        throw;
    }
}

bool GetApplicationFromCLSID(CLSID appCLSID, LPSTR szPath, ULONG cSize)
{
    LPOLESTR pwszClsid;
    WCHAR  szKey[128];
    CHAR  szCLSID[60];
    HKEY hKey;

    // Convert CLSID to String
    HRESULT hr = StringFromCLSID(appCLSID, &pwszClsid);
    if (FAILED(hr))
    {
        return FALSE;
    }
    LOG_INFO(L"OLE CLSID: %s", pwszClsid);

    // Convert result to ANSI
    WideCharToMultiByte(CP_ACP, 0, pwszClsid, -1, szCLSID, 60, NULL, NULL);

    // Format Registry Key string
    StringCbPrintf(szKey, 128, L"CLSID\\%s\\LocalServer32", pwszClsid);
    
    // Open key to find path of application
    LONG lRet = RegOpenKeyEx(HKEY_CLASSES_ROOT, szKey, 0, KEY_ALL_ACCESS, &hKey);
    if (lRet != ERROR_SUCCESS) 
    {
    // If LocalServer32 does not work, try with LocalServer
        StringCbPrintf(szKey, 128, L"CLSID\\%s\\LocalServer", pwszClsid);
        lRet = RegOpenKeyEx(HKEY_CLASSES_ROOT, szKey, 0, KEY_ALL_ACCESS, &hKey);
        if (lRet != ERROR_SUCCESS) 
        {
            LOG_WARNING(_T("No application info for CLSID found in LocalServer32 or LocalServer."));
            // Free memory used by StringFromCLSID
            CoTaskMemFree(pwszClsid);    
            return FALSE;
        }
    }
    
    // Free memory used by StringFromCLSID
    CoTaskMemFree(pwszClsid);
    
    // Query value of key to get Path and close the key
    lRet = RegQueryValueEx(hKey, NULL, NULL, NULL, (BYTE*)szPath, &cSize);
    RegCloseKey(hKey);
    if (lRet != ERROR_SUCCESS)
    {
        LOG_WARNING(_T("CLSID registry query failed."));
        return FALSE;
    }

    // Strip off the '/Automation' switch from the path
    char *x = strrchr(szPath, '/');
    if(0!= x) // If no /Automation switch on the path
    {
        int result = int(x - szPath); 
        szPath[result]  = '\0';  // If switch there, strip it
    }   
    return TRUE;
    
}

void Zimbra::MAPI::Util::CheckEncodingAndAddBodyToPart(BOOL bMacEncoded, LPVOID pAttachData, size_t attachDataLen, mimepp::BodyPart *pAttachPart, BOM& bom)
{
    LPSTR pStr = (LPSTR)pAttachData;

    // ------------------------------------------------------------------------
    // Check source encoding
    // ------------------------------------------------------------------------
    LPVOID pReqdData = NULL;
    size_t ReqdDataLen = 0;
    CalcBOMAndSkipSourceEncodingPreamble(bMacEncoded, pStr, attachDataLen, pReqdData, ReqdDataLen, bom);

    // ------------------------------------------------------------------------
    // Add pAttachData to the mimepp part
    // ------------------------------------------------------------------------
    try
    {
        OptimallyEncodeBodyAndAddToPart(pAttachPart, (LPSTR)pReqdData, ReqdDataLen);
    }
    catch(std::exception& e)
    {
        LOG_ERROR(_T("std::exception: %hs in CheckEncodingAndAddBodyToPart"), e.what());
        _ASSERT(FALSE);
        throw;
    }
    catch(...)
    {
        LOG_ERROR(_T("Failed to add body part"));
        _ASSERT(FALSE);
        throw;
    }
}

mimepp::BodyPart*  Zimbra::MAPI::Util::EncodeSingle(LPSTREAM pStream)
{
    LOGFN_TRACE_NO;
    (void)pStream;
    _ASSERT(FALSE);
    return NULL;
}

mimepp::BodyPart* Zimbra::MAPI::Util::AttachPartFromIAttach(MAPISession &session, LPATTACH pMAPIAttach, LPSTR pCharset, LONG codepage)
//
// Builds s a mimepp attach part for the passed in MAPI attachment
//
{
    LOGFN_TRACE_NO;

    PauseAfterLargeMemDelta();

    if (pMAPIAttach == NULL)
    {
        _ASSERT(FALSE);
        return NULL;
    }

    // ===============================================================================
    // Read attachment props
    // ===============================================================================
    LPSPropValue pProps = NULL;
    ULONG cProps = 0;
    HRESULT hr = pMAPIAttach->GetProps((LPSPropTagArray) & attachProps, 0, &cProps, &pProps);
    if (FAILED(hr))
    {
        throw Zimbra::MAPI::Util::MapiUtilsException(hr, L"Util::AttachPartFromIAttach(): GetProps.",  ERR_GET_ATTCHMENT, __LINE__, __FILE__);
    }
    if (pProps[ATTACH_METHOD].ulPropTag != PR_ATTACH_METHOD)
    {
        MAPIFreeBuffer(pProps);
        return NULL;
    }

    // ===============================================================================
    // Allocated empty mimepp part to hold the attachment
    // ===============================================================================
    mimepp::BodyPart *pAttachPart = new mimepp::BodyPart;


    // ===============================================================================
    // Consume PR_ATTACH_ENCODING
    // ===============================================================================
    BOOL bMacEncoded = FALSE;
    BYTE OID_MAC_BINARY[] = { 0x2A, 0x86, 0x48, 0x86, 0xf7, 0x14, 0x03, 0x0B, 0x01 };
    if ((pProps[ATTACH_ENCODING].ulPropTag == PR_ATTACH_ENCODING) &&
        (pProps[ATTACH_ENCODING].Value.bin.cb == sizeof (OID_MAC_BINARY)))
    {
        bMacEncoded = (memcmp(OID_MAC_BINARY, pProps[ATTACH_ENCODING].Value.bin.lpb, pProps[ATTACH_ENCODING].Value.bin.cb) == 0);
    }

    // ===============================================================================
    // Consume PR_ATTACH_CONTENT_ID_A
    // ===============================================================================
    if (pProps[ATTACH_CONTENT_ID].ulPropTag == PR_ATTACH_CONTENT_ID_A)
    {
        mimepp::String contentId("<");
        contentId += pProps[ATTACH_CONTENT_ID].Value.lpszA;
        contentId += ">";
        pAttachPart->headers().contentId().setString(contentId);
    }

    // ===============================================================================
    // Consume PR_ATTACH_CONTENT_LOCATION_A
    // ===============================================================================
    if (pProps[ATTACH_CONTENT_LOCATION].ulPropTag == PR_ATTACH_CONTENT_LOCATION_A)
    {
        // add a custom header for content location to support rfc2557
        pAttachPart->headers().fieldBody("Content-Location").setText(pProps[ATTACH_CONTENT_LOCATION].Value.lpszA);
    }

    // ===============================================================================
    // Consume PR_ATTACH_DISPOSITION_A
    // ===============================================================================
    if (pProps[ATTACH_DISPOSITION].ulPropTag == PR_ATTACH_DISPOSITION_A)
        pAttachPart->headers().contentDisposition().setType(pProps[ATTACH_DISPOSITION].Value.lpszA);
    else
        pAttachPart->headers().contentDisposition().setType("attachment");


    // ===============================================================================
    // Consume ATTACH_LONG_FILENAME/FILENAME/DISPLAY_NAME
    // ===============================================================================
    LPSTR pAttachFilename = NULL;
    if (PROP_TYPE(pProps[WATTACH_LONG_FILENAME].ulPropTag) != PT_ERROR)
    {
        LPSTR pMimeAttName = NULL;
        int nLen = (int)_tcslen(pProps[WATTACH_LONG_FILENAME].Value.lpszW);
        UNREFERENCED_PARAMETER(nLen);

        LPTSTR pAttname = pProps[WATTACH_LONG_FILENAME].Value.lpszW;
        LOG_VERBOSE(_T("Attach name: '%s'"), pAttname);
        CreateMimeSubject(pAttname, codepage, &pMimeAttName);
        mimepp::String AttStr(pMimeAttName);

        pAttachPart->headers().contentDisposition().setFilename(AttStr); 
        pAttachPart->headers().contentDisposition().assemble();
        if (pMimeAttName != NULL)
        {
            WtoA(pAttname,pAttachFilename);
            delete[] pMimeAttName;
        }
    }
    else 
    if (pProps[ATTACH_LONG_FILENAME].ulPropTag == PR_ATTACH_LONG_FILENAME_A)
    {
        Zimbra::Util::CopyString(pAttachFilename, pProps[ATTACH_LONG_FILENAME].Value.lpszA);
        pAttachPart->headers().contentDisposition().setFilename(pAttachFilename);
        pAttachPart->headers().contentDisposition().assemble();
    }
    else 
    if (pProps[ATTACH_FILENAME].ulPropTag == PR_ATTACH_FILENAME_A)
    {
        Zimbra::Util::CopyString(pAttachFilename,pProps[ATTACH_FILENAME].Value.lpszA);
        pAttachPart->headers().contentDisposition().setFilename(pAttachFilename);
        pAttachPart->headers().contentDisposition().assemble();
    }
    else 
    if (pProps[ATTACH_DISPLAY_NAME].ulPropTag == PR_DISPLAY_NAME_A)
    {
        Zimbra::Util::CopyString(pAttachFilename,pProps[ATTACH_DISPLAY_NAME].Value.lpszA);
        pAttachPart->headers().contentDisposition().setFilename(pAttachFilename);
        pAttachPart->headers().contentDisposition().assemble();
    }

    // ===============================================================================
    // Add the attachment data - embedded or regular?
    // ===============================================================================
    // text/plain attachments may start with a byte order mark
    BOM bom = BOM_NONE;
    switch (pProps[0].Value.l)
    {
        case ATTACH_BY_VALUE:
        {
            // ------------------------------------------------------------------------
            // ATTACH_BY_VALUE -> data is in PR_ATTACH_DATA_BIN 
            // ------------------------------------------------------------------------
            size_t attachDataLen = 0;

            LPSPropValue pPropAttachDataBin = NULL;
            hr = HrGetOneProp(pMAPIAttach, PR_ATTACH_DATA_BIN, &pPropAttachDataBin);
            if (hr == S_OK)
            {
                // ------------------------------------------------------------------------
                // Small -> already read direct from the prop
                // ------------------------------------------------------------------------
                LPVOID pAttachData = pPropAttachDataBin->Value.bin.lpb;
                attachDataLen      = pPropAttachDataBin->Value.bin.cb;

                // ------------------------------------------------------------------------
                // Check encoding and add body to part
                // ------------------------------------------------------------------------
                CheckEncodingAndAddBodyToPart(bMacEncoded, pAttachData, attachDataLen, pAttachPart, bom);

                // ------------------------------------------------------------------------
                // pAttachPart has now been built -> Free the temp mem
                // ------------------------------------------------------------------------
                if (pPropAttachDataBin != NULL)
                    MAPIFreeBuffer(pPropAttachDataBin);
            }
            else
            if (hr == E_OUTOFMEMORY)
            {
                // ------------------------------------------------------------------------
                // Large -> need OpenProperty
                // ------------------------------------------------------------------------
                LPSTREAM pStream;
                hr = pMAPIAttach->OpenProperty(PR_ATTACH_DATA_BIN, &IID_IStream, STGM_READ, 0, (LPUNKNOWN FAR *)&pStream);
                if (!SUCCEEDED(hr))
                {
                    delete pAttachPart;
                    pAttachPart = NULL;
                    return NULL;
                }

                // ------------------------------------------------------------------------
                // Get size
                // ------------------------------------------------------------------------
                STATSTG statstg;
                hr = pStream->Stat(&statstg, STATFLAG_NONAME);
                if (!SUCCEEDED(hr))
                {
                    delete pAttachPart;
                    pAttachPart = NULL;
                    return NULL;
                }
                attachDataLen = statstg.cbSize.LowPart;


                // ------------------------------------------------------------------------
                // Encode with streaming encoder - 5MB at a  time
                // ------------------------------------------------------------------------
                DWORD dwChunkSizeMB = 5;
                CStreamingEncoder Encoder((DWORD)attachDataLen, dwChunkSizeMB*1024*1024, bMacEncoded);
                
                ULONG ulTotalBytesRead = 0;
                DWORD dwBytesToRead = (DWORD)attachDataLen;
                while (!FAILED(hr) && dwBytesToRead > 0)
                {
                    // Ask the encoder for the input buffer and stream data straight into it
                    CHAR* pInBuff; DWORD dwInBuffLen;
                    Encoder.GetInBuf(pInBuff, dwInBuffLen);

                    ULONG ulBytesRead = 0;
                    hr = pStream->Read(pInBuff, dwInBuffLen, &ulBytesRead);
                    if ((hr!=S_OK) || !ulBytesRead)
                        break;

                    // Tell the encoder ahow much data we just read
                    Encoder.SetBytesRead(ulBytesRead);

                    // Tell the encoder to encode that buffer
                    Encoder.EncodeInputBuffer();

                    // Update totals
                    dwBytesToRead -= ulBytesRead;
                    ulTotalBytesRead += ulBytesRead;

                } // while
                _ASSERT(ulTotalBytesRead == attachDataLen);

                // Finished with stream
                pStream->Release();
                pStream = NULL;

                // Finish
                Encoder.Finish();

                // Finally, grab the encoded string and write it into the body part
                switch (Encoder.GetEncodingType())
                {
                    case ME_QUOTED_PRINTABLE:
                        pAttachPart->headers().contentTransferEncoding().setString("Quoted-printable");
                        break;

                    case ME_BASE64:
                        pAttachPart->headers().contentTransferEncoding().setString("Base64");
                        break;

                    case ME_7BIT:
                        pAttachPart->headers().contentTransferEncoding().setString("7bit");
                        break;
                } // switch
                mimepp::String strEncoded = Encoder.GetEncodedString();
                pAttachPart->body().setString(strEncoded);

                bom = Encoder.GetBom();
            }
            else 
            if (FAILED(hr))
            {
                MAPIFreeBuffer(pPropAttachDataBin);
                delete pAttachPart;
                pAttachPart = NULL;
                return NULL;
            }
        }
        break;

        case ATTACH_OLE:
        {
            // ------------------------------------------------------------------------
            // ATTACH_OLE -> data is in PR_ATTACH_DATA_OBJ 
            // ------------------------------------------------------------------------
            LPSTORAGE lpStorageSrc = NULL;
            hr = pMAPIAttach->OpenProperty(PR_ATTACH_DATA_OBJ, &IID_IStorage, 0, 0, (LPUNKNOWN *)&lpStorageSrc);
            if (FAILED(hr))
            {
                LOG_ERROR(_T("OLE Storage cannot be opened. Skipping the attachment."));
                delete pAttachPart;
                pAttachPart = NULL;
                return NULL;
            }

            if (lpStorageSrc)
            {
                // Read the CLSID of storage to get the application info
                // which was used to create OLE attachment.
                // if no info retirved, extension of the attachment cannot be determined 
                // which results in attachment migartion with no extension
                CLSID storageCLSID;
                if (ReadClassStg(lpStorageSrc, &storageCLSID) == S_OK)
                {
                    CHAR szApp[50];
                    if(GetApplicationFromCLSID(storageCLSID, szApp, 50))
                        LOG_INFO(L"application from CLSID: %ls", szApp);
                    else
                        LOG_WARNING(_T("OLE attachment type cannot be determined. Attachment will be migrated without extension."));
                }

                IEnumSTATSTG *penum = NULL;
                hr = lpStorageSrc->EnumElements( NULL, NULL, NULL, &penum );
                if( SUCCEEDED(hr) ) 
                {
                    ULONG reqStatstg = 40;
                    ULONG fetchedStatstg = 0;
                    STATSTG *pstatstg = new STATSTG[reqStatstg];
                    hr = penum->Next( reqStatstg, pstatstg, &fetchedStatstg );
                
                    // Loop through all the child objects of this storage.
                    for (ULONG itr = 0; itr<fetchedStatstg; itr++)
                    {					
                        LOG_INFO(L"OLE pwcsName: '%s'", pstatstg[itr].pwcsName);

                        //we are interested only in content now
                        if((pstatstg[itr].type == STGTY_STREAM ) && (!wcscmp (pstatstg[itr].pwcsName, L"CONTENTS")))
                        {
                            LPSTREAM pStream = NULL;
                            hr = lpStorageSrc->OpenStream( pstatstg[itr].pwcsName, NULL, STGM_READ | STGM_SHARE_EXCLUSIVE, 0, &pStream );
                            if( SUCCEEDED(hr) ) 
                            {
                                // ------------------------------------------------------------------------
                                // Get size
                                // ------------------------------------------------------------------------
                                STATSTG statstg;
                                hr = pStream->Stat(&statstg, STATFLAG_NONAME);
                                if (!SUCCEEDED(hr))
                                {
                                    LOG_ERROR(_T("OLE Storage STATSTG cannot be fetched. Skipping the attachment."));
                                    delete pAttachPart;
                                    pAttachPart = NULL;
                                    return NULL;
                                }
                                size_t attachDataLen = statstg.cbSize.LowPart;

                                // ------------------------------------------------------------------------
                                // Allocate buffer for incoming body data
                                // ------------------------------------------------------------------------
                                LPVOID pMAPIBuffAttachData = NULL;
                                hr = MAPIAllocateBuffer((ULONG)attachDataLen, (LPVOID FAR *)&pMAPIBuffAttachData);
                                if (!SUCCEEDED(hr))
                                {
                                    LOG_ERROR(_T("OLE attachemnt: Memory allocation failed. Skipping the attachment."));
                                    delete pAttachPart;
                                    pAttachPart = NULL;
                                    return NULL;
                                }
                                ZeroMemory(pMAPIBuffAttachData, (int)attachDataLen);

                                // ------------------------------------------------------------------------
                                // Fill buffer
                                // ------------------------------------------------------------------------
                                ULONG cb;
                                hr = pStream->Read(pMAPIBuffAttachData, statstg.cbSize.LowPart, &cb);

                                // ------------------------------------------------------------------------
                                // Check encoding and add body to part
                                // ------------------------------------------------------------------------
                                CheckEncodingAndAddBodyToPart(bMacEncoded, pMAPIBuffAttachData, attachDataLen, pAttachPart, bom);

                                // ------------------------------------------------------------------------
                                // pAttachPart has now been built -> Free the temp mem
                                // ------------------------------------------------------------------------
                                if (pMAPIBuffAttachData != NULL)
                                    MAPIFreeBuffer(pMAPIBuffAttachData);

                                CoTaskMemFree( pstatstg[itr].pwcsName );
                                pstatstg[itr].pwcsName = NULL;

                                if (pStream)
                                {
                                    pStream->Release();
                                    pStream = NULL;
                                }
                            }
                        }
                    }
                                
                    delete[] pstatstg;
                }
                if( NULL != penum )
                    penum->Release();
                if(NULL != lpStorageSrc)
                    lpStorageSrc->Release();
            }
        }
        break;

        case ATTACH_EMBEDDED_MSG:
        {
            LPMESSAGE pMessage = NULL;
            hr = pMAPIAttach->OpenProperty(PR_ATTACH_DATA_OBJ, &IID_IMessage, 0, 0, (LPUNKNOWN *)&pMessage);
            if (FAILED(hr))
            {
                // TODO: error handling
            }

            // create a Message object
            MAPIMessage m;
            m.InitMAPIMessage(pMessage, session);

            mimepp::Message mimeMsg;
            m.ToMimePPMessage(mimeMsg);

            mimepp::String ct("message/rfc822; charset=utf-7;");

            pAttachPart->headers().contentType().setString(ct);
            pAttachPart->headers().contentType().parse();
            pAttachPart->headers().contentTransferEncoding().setString("7bit");
            pAttachPart->headers().contentTransferEncoding().assemble();
            pAttachPart->headers().contentDisposition().setType("attachment");
            if (pProps[ATTACH_DISPLAY_NAME].ulPropTag == PR_DISPLAY_NAME_A)
            {
                LPSTR pFilename = pProps[ATTACH_DISPLAY_NAME].Value.lpszA;
                pAttachPart->headers().contentDisposition().setFilename(pFilename);
            }
            pAttachPart->headers().contentDisposition().assemble();
            pAttachPart->body().setString(mimeMsg.getString());
        }
        MAPIFreeBuffer(pProps);
        return pAttachPart;

    } // switch 



    // ===============================================================================
    // Consume PR_ATTACH_MIME_TAG_A/ATTACH_EXTENSION
    // ===============================================================================
    LPSTR pBomCharsets[] = { "UTF-32BE", "UTF-32LE", "UTF-16BE", "UTF-16LE", "UTF-8" };
    mimepp::String strContentType("");
    if (          (pProps[ATTACH_MIME_TAG].ulPropTag == PR_ATTACH_MIME_TAG_A) 
        && stricmp(pProps[ATTACH_MIME_TAG].Value.lpszA, "multipart/appledouble"))
    {
        // set the content-type
        strContentType += pProps[ATTACH_MIME_TAG].Value.lpszA;
        strContentType += "; charset=";
        if (bom == BOM_NONE)
            strContentType += pCharset;
        else
            strContentType += pBomCharsets[bom];
        strContentType += ";";
        pAttachPart->headers().contentType().setString(strContentType);
        pAttachPart->headers().contentType().parse();
    }
    else 
    if (pAttachFilename != NULL)
    {
        if (pProps[ATTACH_EXTENSION].ulPropTag == PR_ATTACH_EXTENSION_A)
        {
            LPSTR pContentType = NULL;
            GetContentTypeFromExtension(pProps[ATTACH_EXTENSION].Value.lpszA, pContentType);
            if (pContentType == NULL)
            {
                strContentType += "application/octet-stream";
            }
            else
            {
                strContentType += pContentType;
                delete[] pContentType;
            }
        }
        else
        {
            strContentType += "application/octet-stream";
        }
        strContentType += "; charset=";
        if (bom == BOM_NONE)
            strContentType += pCharset;
        else
            strContentType += pBomCharsets[bom];
        strContentType += ";";
        pAttachPart->headers().contentType().setString(strContentType);
        pAttachPart->headers().contentType().parse();
    }

    if(pAttachFilename)
    {
        delete[] pAttachFilename;
        pAttachFilename = NULL;
    }

    MAPIFreeBuffer(pProps);
    return pAttachPart;
}

/* DCB Seems unused 
mimepp::BodyPart *Zimbra::MAPI::Util::AttachTooLargeAttachPart(ULONG attachSize, LPATTACH pAttach, LPSTR)
{
    LOGFN_TRACE_NO;

    mimepp::BodyPart *pAttachPart = NULL;

    if (pAttach == NULL)
        return NULL;

    HRESULT hr;
    LPSPropValue pProps = NULL;
    ULONG cProps = 0;

    hr = pAttach->GetProps((LPSPropTagArray) & attachProps, 0, &cProps, &pProps);

    mimepp::String textBody("Attachment excluded during import of message.  The maximum attachment size has been exceeded.  Attachment information follows.\r\n\r\n\r\n");
    LPSTR pTmp = new CHAR[64];

    ultoa(attachSize, pTmp, 10);

    textBody += "Attachment Size: ";
    textBody += pTmp;
    textBody += "\r\n";

    delete[] pTmp;

    mimepp::String strContentType("");

    if (FAILED(hr))
        throw Zimbra::MAPI::Util::MapiUtilsException(hr, L"Util::AttachTooLargeAttachPart(): GetProps.", ERR_GET_ATTCHMENT, __LINE__, __FILE__);

    if (pProps[ATTACH_METHOD].ulPropTag != PR_ATTACH_METHOD)
    {
        MAPIFreeBuffer(pProps);
        return NULL;
    }

    pAttachPart = new mimepp::BodyPart;
    if (pProps[ATTACH_CONTENT_ID].ulPropTag == PR_ATTACH_CONTENT_ID_A)
    {
        textBody += "Content-Id: ";
        textBody += pProps[ATTACH_CONTENT_ID].Value.lpszA;
        textBody += "\r\n";
    }

    if (pProps[ATTACH_CONTENT_LOCATION].ulPropTag == PR_ATTACH_CONTENT_LOCATION_A)
    {
        // add a custom header for content location to support rfc2557
        textBody += "Content-Location: ";
        textBody += pProps[ATTACH_CONTENT_LOCATION].Value.lpszA;
        textBody += "\r\n";
    }

    if (pProps[ATTACH_LONG_FILENAME].ulPropTag == PR_ATTACH_LONG_FILENAME_A)
    {
        textBody += "Filename: ";
        textBody += pProps[ATTACH_LONG_FILENAME].Value.lpszA;
        textBody += "\r\n";
    }
    else 
    if (pProps[ATTACH_FILENAME].ulPropTag == PR_ATTACH_FILENAME_A)
    {
        textBody += "Filename: ";
        textBody += pProps[ATTACH_FILENAME].Value.lpszA;
        textBody += "\r\n";
    }

    if (pProps[ATTACH_DISPLAY_NAME].ulPropTag == PR_DISPLAY_NAME_A)
    {
        textBody += "Content-Description: ";
        textBody += pProps[ATTACH_DISPLAY_NAME].Value.lpszA;
        textBody += "\r\n";
    }
    strContentType += "text/plain; charset=us-ascii;";
    pAttachPart->headers().contentType().setString(strContentType);
    pAttachPart->headers().contentType().parse();
    pAttachPart->headers().contentDisposition().setType("attachment");
    pAttachPart->headers().contentDisposition().setFilename("ImportAttachError.txt");
    pAttachPart->headers().contentDisposition().assemble();

    Zimbra::MAPI::Util::AddBodyToPart(pAttachPart, (LPSTR)textBody.c_str(), textBody.length());

    MAPIFreeBuffer(pProps);
    return pAttachPart;
}
*/

void Zimbra::MAPI::Util::CharsetUtil::CharsetStringFromCodePageId(UINT codePageId, LPSTR *ppCharset)
{
    LPSTR pCS = NULL;

    switch (codePageId)
    {
        case 708:
            pCS = "ASMO-708";
            break;
        case 720:
            pCS = "DOS-720";
            break;
        case 28596:
            pCS = "iso-8859-6";
            break;
        case 10004:
            pCS = "x-max-arabic";
            break;
        case 775:
            pCS = "ibm775";
            break;
        case 28594:
            pCS = "iso-8859-4";
            break;
        case 852:
            pCS = "ibm852";
            break;
        case 28592:
            pCS = "iso-8859-2";
            break;
        case 10029:
            pCS = "x-mac-ce";
            break;
        case 51936:
            pCS = "EUC-CN";
            break;
        case 936:
            pCS = "gb2312";
            break;
        case 52936:
            pCS = "hz-gb-2312";
            break;
        case 10008:
            pCS = "x-mac-chinesesimp";
            break;
        case 950:
            pCS = "big5";
            break;
        case 20000:
            pCS = "x-Chinese-CNS";
            break;
        case 20002:
            pCS = "x-Chinese-Eten";
            break;
        case 10002:
            pCS = "x-mac-chinesetrad";
            break;
        case 866:
            pCS = "cp866";
            break;
        case 28595:
            pCS = "iso-8859-5";
            break;
        case 20866:
            pCS = "koi8-r";
            break;
        case 21866:
            pCS = "koi8-u";
            break;
        case 10007:
            pCS = "x-mac-cyrillic";
            break;
        case 29001:
            pCS = "x-Europa";
            break;
        case 20106:
            pCS = "x-IA5-German";
            break;
        case 737:
            pCS = "ibm737";
            break;
        case 28597:
            pCS = "iso-8859-7";
            break;
        case 10006:
            pCS = "x-mac-greek";
            break;
        case 869:
            pCS = "ibm869";
            break;
        case 862:
            pCS = "DOS-862";
            break;
        case 38598:
            pCS = "iso-8859-8-i";
            break;
        case 28598:
            pCS = "iso-8859-8";
            break;
        case 10005:
            pCS = "x-mac-hebrew";
            break;
        case 20420:
            pCS = "x-EBCDIC-Arabic";
            break;
        case 20880:
            pCS = "x-EBCDIC-CyrillicRussian";
            break;
        case 21025:
            pCS = "x-EBCDIC-CyrillicSerbianBulgarian";
            break;
        case 20277:
            pCS = "x-EBCDIC-DenmarkNorway";
            break;
        case 1142:
            pCS = "x-ebcdic-denmarknorway-euro";
            break;
        case 20278:
            pCS = "x-EBCDIC-FinlandSweden";
            break;
        case 1143:
            pCS = "x-ebcdic-finlandsweden-euro";
            break;
        case 1147:
            pCS = "x-ebcdic-france-euro";
            break;
        case 20273:
            pCS = "x-EBCDIC-Germany";
            break;
        case 1141:
            pCS = "x-ebcdic-germany-euro";
            break;
        case 875:
            pCS = "x-EBCDIC-GreekModern";
            break;
        case 20423:
            pCS = "x-EBCDIC-Greek";
            break;
        case 20424:
            pCS = "x-EBCDIC-Hebrew";
            break;
        case 20871:
            pCS = "x-EBCDIC-Icelandic";
            break;
        case 1149:
            pCS = "x-ebcdic-icelandic-euro";
            break;
        case 1148:
            pCS = "x-ebcdic-international-euro";
            break;
        case 20280:
            pCS = "x-EBCDIC-Italy";
            break;
        case 1144:
            pCS = "x-ebcdic-italy-euro";
            break;
        case 50930:
            pCS = "x-EBCDIC-JapaneseAndKana";
            break;
        case 50939:
            pCS = "x-EBCDIC-JapaneseAndJapaneseLatin";
            break;
        case 50931:
            pCS = "x-EBCDIC-JapaneseAndUSCanada";
            break;
        case 20290:
            pCS = "x-EBCDIC-JapaneseKatakana";
            break;
        case 50933:
            pCS = "x-EBCDIC-KoreanAndKoreanExtended";
            break;
        case 20833:
            pCS = "x-EBCDIC-KoreanExtended";
            break;
        case 870:
            pCS = "CP870";
            break;
        case 50935:
            pCS = "x-EBCDIC-SimplifiedChinese";
            break;
        case 20284:
            pCS = "X-EBCDIC-Spain";
            break;
        case 1145:
            pCS = "x-ebcdic-spain-euro";
            break;
        case 20838:
            pCS = "x-EBCDIC-Thai";
            break;
        case 50937:
            pCS = "x-EBCDIC-TraditionalChinese";
            break;
        case 1026:
            pCS = "CP1026";
            break;
        case 20905:
            pCS = "x-EBCDIC-Turkish";
            break;
        case 20285:
            pCS = "x-EBCDIC-UK";
            break;
        case 1146:
            pCS = "x-ebcdic-uk-euro";
            break;
        case 37:
            pCS = "ebcdic-cp-us";
            break;
        case 1140:
            pCS = "x-ebcdic-cp-us-euro";
            break;
        case 861:
            pCS = "ibm861";
            break;
        case 10079:
            pCS = "x-mac-icelandic";
            break;
        case 57006:
            pCS = "x-iscii-as";
            break;
        case 57003:
            pCS = "x-iscii-be";
            break;
        case 57002:
            pCS = "x-iscii-de";
            break;
        case 57010:
            pCS = "x-iscii-gu";
            break;
        case 57008:
            pCS = "x-iscii-ka";
            break;
        case 57009:
            pCS = "x-iscii-ma";
            break;
        case 57007:
            pCS = "x-iscii-or";
            break;
        case 57011:
            pCS = "x-iscii-pa";
            break;
        case 57004:
            pCS = "x-iscii-ta";
            break;
        case 57005:
            pCS = "x-iscii-te";
            break;
        case 51932:
            pCS = "euc-jp";
            break;
        case 50220:
            pCS = "iso-2022-jp";
            break;
        case 50222:
            pCS = "iso-2022-jp";
            break;
        case 50221:
            pCS = "csISO2022JP";
            break;
        case 10001:
            pCS = "x-mac-japanese";
            break;
        case 932:
            pCS = "shift_jis";
            break;
        case 949:
            pCS = "ks_c_5601-1987";
            break;
        case 51949:
            pCS = "euc-kr";
            break;
        case 50225:
            pCS = "iso-2022-kr";
            break;
        case 1361:
            pCS = "Johab";
            break;
        case 10003:
            pCS = "x-mac-korean";
            break;
        case 28593:
            pCS = "iso-8859-3";
            break;
        case 28605:
            pCS = "iso-8859-15";
            break;
        case 20108:
            pCS = "x-IA5-Norwegian";
            break;
        case 437:
            pCS = "IBM437";
            break;
        case 20107:
            pCS = "x-IA5-Swedish";
            break;
        case 857:
            pCS = "ibm857";
            break;
        case 28599:
            pCS = "iso-8859-9";
            break;
        case 10081:
            pCS = "x-mac-turkish";
            break;
        case 1200:
            pCS = "unicode";
            break;
        case 1201:
            pCS = "unicodeFFFE";
            break;
        case 65000:
            pCS = "utf-7";
            break;
        case 65001:
            pCS = "utf-8";
            break;
        case 20127:
            pCS = "us-ascii";
            break;
        case 850:
            pCS = "ibm850";
            break;
        case 20105:
            pCS = "x-IA5";
            break;
        case 28591:
            pCS = "iso-8859-1";
            break;
        case 10000:
            pCS = "macintosh";
            break;
        default:
            break;
    }  // switch( codePageId )

    if (pCS != NULL)
    {
        *ppCharset = new char[(int)strlen(pCS) + 1];
        strcpy_s(*ppCharset, (int)strlen(pCS) + 1, pCS);
    }
    else
    {
        *ppCharset = new char[25];
        sprintf_s(*ppCharset, 25, "windows-%d", codePageId);
    }
}

Zimbra::MAPI::Util::StoreUtils *Zimbra::MAPI::Util::StoreUtils::stUtilsInst = NULL;


// HrGetRegMultiSZValueA
// Get a REG_MULTI_SZ registry value - allocating memory using new to hold it.
void Zimbra::MAPI::Util::StoreUtils::HrGetRegMultiSZValueA(IN HKEY hKey,        // the key.
                                                           IN LPCSTR lpszValue, // value name in key.
                                                           OUT LPVOID *lppData) // where to put the data.
{
    *lppData = NULL;

    DWORD dwKeyType = NULL;
    DWORD cb = NULL;
    LONG lRet = 0;

    // Get its size
    lRet = RegQueryValueExA(hKey, lpszValue, NULL, &dwKeyType, NULL, &cb);
    if ((ERROR_SUCCESS == lRet) && cb && (REG_MULTI_SZ == dwKeyType))
    {
        *lppData = new BYTE[cb];
        if (*lppData)
        {
            // Get the current value
            lRet = RegQueryValueExA(hKey, lpszValue, NULL, &dwKeyType, (unsigned char *)*lppData, &cb);
            if (ERROR_SUCCESS != lRet)
            {
                delete[] *lppData;
                *lppData = NULL;
            }
        }
    }
}

// HrGetRegMultiSZValueA
// Get a REG_MULTI_SZ registry value - allocating memory using new to hold it.
void Zimbra::MAPI::Util::StoreUtils::HrGetRegSZValueA(IN HKEY hKey,        // the key.
                                                      IN LPCSTR lpszValue, // value name in key.
                                                      OUT LPVOID *lppData) // where to put the data.
{
    *lppData = NULL;

    DWORD dwKeyType = NULL;
    DWORD cb = NULL;
    LONG lRet = 0;

    // Get its size
    lRet = RegQueryValueExA(hKey, lpszValue, NULL, &dwKeyType, NULL, &cb);
    if ((ERROR_SUCCESS == lRet) && cb && (REG_SZ == dwKeyType))
    {
        *lppData = new BYTE[cb];
        if (*lppData)
        {
            // Get the current value
            lRet = RegQueryValueExA(hKey, lpszValue, NULL, &dwKeyType, (unsigned char *)*lppData, &cb);
            if (ERROR_SUCCESS != lRet)
            {
                delete[] *lppData;
                *lppData = NULL;
            }
        }
    }
}

// /////////////////////////////////////////////////////////////////////////////
// Function name    : GetMAPIDLLPath
// Description      : This will get the correct path to the MAPIDIR with MAPI32.DLL file.
// Return type      : void
// Argument         : LPSTR szMAPIDir  - Buffer to hold the path to the MAPI32DLL file.
//                    ULONG cchMAPIDir - size of the buffer
void Zimbra::MAPI::Util::StoreUtils::GetMAPIDLLPath(LPSTR szMAPIDir, ULONG cchMAPIDir)
{
    LOGFN_TRACE_NO;

    BOOL bRet = true;
    szMAPIDir[0] = '\0';

    HRESULT hRes = S_OK;

    // Get the system directory path (mapistub.dll and mapi32.dll reside here)
    CHAR szSystemDir[MAX_PATH + 1] = { 0 };
    UINT uiRet = GetSystemDirectoryA(szSystemDir, MAX_PATH);
    if (uiRet > 0)
    {
        LOG_GEN(_T("System dir: '%hs'"), szSystemDir);

        CHAR szDLLPath[MAX_PATH + 1] = { 0 };
        hRes = StringCchPrintfA(szDLLPath, MAX_PATH + 1, "%s\\%s", szSystemDir, "mapistub.dll");
        if (SUCCEEDED(hRes))
        {
            // ===============================================================================
            // First get a ptr to FGetComponentPath
            // ===============================================================================

            // -----------------------------------------------------------------
            // Try to get the address of FGetComponentPath from the mapistub.dll
            // -----------------------------------------------------------------
            LPFGETCOMPONENTPATH pfnFGetComponentPath = NULL;
            HINSTANCE hmodMapi32 = NULL;
            HINSTANCE hmodStub = LoadLibraryA(szDLLPath);
            if (hmodStub)
            {
                LOG_GEN(_T("Found stub at '%hs'"), szDLLPath);
                pfnFGetComponentPath = (LPFGETCOMPONENTPATH)GetProcAddress(hmodStub, "FGetComponentPath");
            }

            // ---------------------------------------------------------------------------------
            // If we didn't get the address of FGetComponentPath, try getting it from mapi32.dll
            // ---------------------------------------------------------------------------------
            if (pfnFGetComponentPath)
                LOG_GEN(_T("Found ptr in stub"));
            else
            {
                hRes = StringCchPrintfA(szDLLPath, MAX_PATH + 1, "%s\\%s", szSystemDir, "mapi32.dll");
                if (SUCCEEDED(hRes))
                {
                    // Load mapi32.dll
                    hmodMapi32 = LoadLibraryA(szDLLPath);
                    if (hmodMapi32)
                    {
                        LOG_GEN(_T("Found dll at '%hs'"), szDLLPath);
                        pfnFGetComponentPath = (LPFGETCOMPONENTPATH)GetProcAddress(hmodMapi32, "FGetComponentPath");
                    }
                }
            }

            // ===============================================================================
            // Now use FGetComponentPath to get the path to MSMAPI32.DLL
            // ===============================================================================
            if (pfnFGetComponentPath)
            {
                LPSTR szComponentId = NULL;
                LPSTR szAppLCID = NULL;
                LPSTR szOfficeLCID = NULL;

                HKEY hMicrosoftOutlook = NULL;
                LONG lRet = RegOpenKeyEx(HKEY_LOCAL_MACHINE, _T("Software\\Clients\\Mail\\Microsoft Outlook"), NULL, KEY_READ, &hMicrosoftOutlook);
                if ((ERROR_SUCCESS == lRet) && hMicrosoftOutlook)
                {
                    HrGetRegSZValueA(hMicrosoftOutlook, "MSIComponentID",      (LPVOID *)&szComponentId);
                    if (szComponentId)
                        LOG_GEN(_T("MSIComponentID: '%hs'"), szComponentId);
                    else
                        LOG_WARNING(_T("Failed to find MSIComponentID"));

                    HrGetRegMultiSZValueA(hMicrosoftOutlook, "MSIApplicationLCID", (LPVOID *)&szAppLCID);
                    if (szAppLCID)
                        LOG_GEN(_T("AppLCID: '%hs'"), szAppLCID);
                    else
                        LOG_WARNING(_T("Failed to find AppLCID"));

                    HrGetRegMultiSZValueA(hMicrosoftOutlook, "MSIOfficeLCID",      (LPVOID *)&szOfficeLCID);
                    if (szOfficeLCID)
                        LOG_GEN(_T("OfficeLCID: '%hs'"), szOfficeLCID);
                    else
                        LOG_WARNING(_T("Failed to find OfficeLCID"));
                }

                if (szAppLCID)
                {
                    bRet = pfnFGetComponentPath(szComponentId, szAppLCID,    szMAPIDir, cchMAPIDir, true);
                    if (bRet)
                        LOG_GEN(_T("Found via AppLCID: '%hs'"), szMAPIDir);
                    else
                        LOG_GEN(_T("Not found via AppLCID"));
                }

                if ((!bRet || (szMAPIDir[0] == _T('\0'))) && szOfficeLCID)
                {
                    bRet = pfnFGetComponentPath(szComponentId, szOfficeLCID, szMAPIDir, cchMAPIDir, true);
                    if (bRet)
                        LOG_GEN(_T("Found via szOfficeLCID: '%hs'"), szMAPIDir);
                    else
                        LOG_GEN(_T("Not found via szOfficeLCID"));
                }

                if (!bRet || (szMAPIDir[0] == _T('\0')))
                {
                    bRet = pfnFGetComponentPath(szComponentId, NULL,         szMAPIDir, cchMAPIDir, true);
                    if (bRet)
                        LOG_GEN(_T("Found via NULL: '%hs'"), szMAPIDir);
                    else
                        LOG_GEN(_T("Not found via NULL"));
                }


                // We got the path to msmapi32.dll - need to strip it
                if (bRet && (szMAPIDir[0] != _T('\0')))
                {
                    LPSTR lpszSlash = NULL;
                    LPSTR lpszCur = szMAPIDir;

                    for (lpszSlash = lpszCur; *lpszCur; lpszCur = lpszCur++)
                        if (*lpszCur == _T('\\'))
                            lpszSlash = lpszCur;
                    *lpszSlash = _T('\0');
                }

                delete[] szOfficeLCID;
                delete[] szAppLCID;
                delete[] szComponentId;

                if (hMicrosoftOutlook)
                    RegCloseKey(hMicrosoftOutlook);
            }
            else
            {
                LOG_ERROR(_T("Failed to find proc"));
                _ASSERT(FALSE);
            }

            // If FGetComponentPath returns FALSE or if it returned nothing, or if we never found an
            // address of FGetComponentPath, then just default to the system directory
            if (!bRet || (szMAPIDir[0] == '\0'))
                hRes = StringCchPrintfA(szMAPIDir, cchMAPIDir, "%s", szSystemDir);

            // Append "msmapi32.dll"
            if (szMAPIDir[0] != _T('\0'))
                hRes = StringCchPrintfA(szMAPIDir, cchMAPIDir, "%s\\%s", szMAPIDir, "msmapi32.dll");

            if (szMAPIDir[0] != _T('\0'))
                LOG_GEN(_T("Returning '%hs'"), szMAPIDir);
            else
            {
                LOG_ERROR(_T("Not found"));
                _ASSERT(FALSE);
            }

            if (hmodStub)
                FreeLibrary(hmodStub);
            if (hmodMapi32)
                FreeLibrary(hmodMapi32);
        }
    }
    else
    {
        LOG_ERROR(_T("Failed to get system dir"));
        _ASSERT(FALSE);
    }
}

bool Zimbra::MAPI::Util::StoreUtils::isUnicodeStore(LPMESSAGE pMsg)
{
    #define STORE_UNICODE_OK ((ULONG)0x00040000)

    bool retval = false;
    LPSPropValue pPropSuppMask = NULL;
    HRESULT hr = HrGetOneProp(pMsg, PR_STORE_SUPPORT_MASK, &pPropSuppMask);
    if (hr == S_OK)
        retval = (pPropSuppMask->Value.l & STORE_UNICODE_OK) != 0;

    if (pPropSuppMask)
        MAPIFreeBuffer(pPropSuppMask);
    return retval;
}

bool Zimbra::MAPI::Util::StoreUtils::Init()
//
// Inits m_pWrapCompressedRTFEx (a ptr to fn in MAPI32.DLL)
//
{
    if (_hinstLib == NULL)
    {
        LOGFN_TRACE_NO;

        // Get path to MAPI DLLs
        char szMAPIDir[MAX_PATH];
        ULONG cchMAPIDir = MAX_PATH;
        GetMAPIDLLPath(szMAPIDir, cchMAPIDir);
        if (szMAPIDir[0] == _T('\0'))
            return false;

        // Convert szMAPIDir to wide
        LPWSTR pszMAPIDir = NULL;
        int cbuf = MultiByteToWideChar(NULL, 0, szMAPIDir, cchMAPIDir, NULL, 0);
        HRESULT hr = MAPIAllocateBuffer((sizeof (WCHAR) * cbuf) + 10, (LPVOID FAR *)&pszMAPIDir);
        ZeroMemory(pszMAPIDir, (sizeof (WCHAR) * cbuf) + 10);
        UNREFERENCED_PARAMETER(hr);
        int rbuf = MultiByteToWideChar(NULL, 0, szMAPIDir, cchMAPIDir, pszMAPIDir, cbuf);
        UNREFERENCED_PARAMETER(rbuf);

        // Load it
        _hinstLib = LoadLibrary(pszMAPIDir);
        MAPIFreeBuffer(pszMAPIDir);
        if (_hinstLib != NULL)
        {
            m_pWrapCompressedRTFEx = (WRAPCOMPRESSEDRTFSTREAMEX)GetProcAddress(_hinstLib, "WrapCompressedRTFStreamEx");
            if (m_pWrapCompressedRTFEx == NULL)
            {
                LOG_ERROR(_T("Failed to find WrapCompressedRTFStreamEx()"));
                _ASSERT(FALSE);
                return false;
            }
        }
        else
        {
            LOG_ERROR(_T("Failed to load %s"), pszMAPIDir);
            _ASSERT(FALSE);
            return false;
        }
    }
    return true;
}

bool Zimbra::MAPI::Util::StoreUtils::GetAnsiStoreMsgNativeType(LPMESSAGE pMsg, ULONG& ulStreamFlags)
//
// Returns true if pMsg is a non-unicode msg
//
{
    LOGFN_TRACE_NO;

    bool retval = false;
    ulStreamFlags = 0;
    
    // Return immediately if unicode store
    if (isUnicodeStore(pMsg))
        return false;

    LOG_VERBOSE(_T("Ansi store"));
    if (NULL != m_pWrapCompressedRTFEx)
    {
        HRESULT hRes = S_OK;

        RTF_WCSRETINFO retinfo = { 0 };
        retinfo.size = sizeof (RTF_WCSRETINFO);

        RTF_WCSINFO wcsinfo = { 0 };
        wcsinfo.size = sizeof (RTF_WCSINFO);
        wcsinfo.ulFlags = MAPI_NATIVE_BODY;
        wcsinfo.ulOutCodePage = 0;

        // Retrieve the value of the Internet code page.
        // Pass this value to the WrapCompressedRTFStreamEx function.
        // If the property is not found, the default is 0.
        LPSPropValue lpPropCPID = NULL;
        if (SUCCEEDED(hRes = HrGetOneProp(pMsg, PR_INTERNET_CPID, &lpPropCPID)))
            wcsinfo.ulInCodePage = lpPropCPID->Value.l;

        // Open the compressed RTF stream.
        LPSTREAM lpCompressed = NULL;
        LPSTREAM lpUncompressed = NULL;
        if (SUCCEEDED(hRes = pMsg->OpenProperty(PR_RTF_COMPRESSED, &IID_IStream, STGM_READ | STGM_DIRECT, 0, (LPUNKNOWN *)&lpCompressed)))
        {
            // Notice that the WrapCompressedRTFStreamEx function has been loaded
            // by using the GetProcAddress function into pfnWrapEx.
            // Call the WrapCompressedRTFStreamEx function.
            if (SUCCEEDED(hRes = m_pWrapCompressedRTFEx(lpCompressed, &wcsinfo, &lpUncompressed, &retinfo)))
            {
                // Check what the native body type is.
                ulStreamFlags = retinfo.ulStreamFlags;
                switch (ulStreamFlags)
                {
                    case MAPI_NATIVE_BODY_TYPE_RTF:
                        LOG_VERBOSE(_T("MAPI_NATIVE_BODY_TYPE_RTF"));
                        break;
                    case MAPI_NATIVE_BODY_TYPE_HTML:
                        LOG_VERBOSE(_T("MAPI_NATIVE_BODY_TYPE_HTML"));
                        break;
                    case MAPI_NATIVE_BODY_TYPE_PLAINTEXT:
                        LOG_VERBOSE(_T("MAPI_NATIVE_BODY_TYPE_PLAINTEXT"));
                        break;
                }
                retval = true;
            }
        }

        MAPIFreeBuffer(lpPropCPID);
        if (lpUncompressed)
            lpUncompressed->Release();
        if (lpCompressed)
            lpCompressed->Release();
    }

    LOG_VERBOSE(_T("Returning %s; Stream flags:%X"), retval?_T("true"):_T("false"), ulStreamFlags);

    return retval;
}

void Zimbra::MAPI::Util::StoreUtils::UnInit()
{
    LOGFN_TRACE_NO;

    if (_hinstLib != NULL)
    {
        BOOL fFreeResult = FreeLibrary(_hinstLib);
        UNREFERENCED_PARAMETER(fFreeResult);
    }
    _hinstLib = NULL;
}

BOOL Zimbra::MAPI::Util::CreateAppTemporaryDirectory()
{
    LOGFN_TRACE_NO;

    BOOL bRet = FALSE;
    wstring wstrTempDirPath;
    if (!GetAppTemporaryDirectory(wstrTempDirPath))
        return bRet;

    SECURITY_ATTRIBUTES secAttr;
    secAttr.bInheritHandle = FALSE;
    secAttr.lpSecurityDescriptor = NULL;
    secAttr.nLength = sizeof (SECURITY_ATTRIBUTES);

    bRet = CreateDirectory(wstrTempDirPath.c_str(), &secAttr);

    return bRet;
}

BOOL Zimbra::MAPI::Util::GetAppTemporaryDirectory(wstring &wstrTempAppDirPath)
{
    //LOGFN_VERBOSE_NO;

    WCHAR pwszTempDir[MAX_PATH];
    if (!GetTempPath(MAX_PATH, pwszTempDir))
        return FALSE;

    wstrTempAppDirPath = pwszTempDir;
    wstring wstrAppName;
    if (!GetAppName(wstrAppName))
        return FALSE;

    wstrTempAppDirPath += wstrAppName;
    return TRUE;
}

#ifdef CASE_184490_DIAGNOSTICS
void AtoW2(LPCSTR pStrA, LPWSTR &pStrW) // note not inlined
{
    int nAChars = (int)strlen(pStrA);
    LOG_GEN(_T("AtoW nAChars %d"), nAChars);

    int nWChars = MultiByteToWideChar(CP_ACP, 0, pStrA, nAChars, NULL, 0);
    LOG_GEN(_T("AtoW nWChars %d"), nWChars);

    LOG_GEN(_T("AtoW operator new..."));
    pStrW = new WCHAR[nWChars + 1];
    Zimbra::MAPI::Util::PauseAfterLargeMemDelta();

    LOG_GEN(_T("AtoW MultiByteToWideChar..."));
    int n = MultiByteToWideChar(CP_ACP, 0, pStrA, nAChars, pStrW, nWChars);
    LOG_GEN(_T("AtoW n  %d"), n);

    pStrW[nWChars] = 0;
}
#endif

BOOL Zimbra::MAPI::Util::GetAppName(wstring &wstrAppName)
{
    WCHAR szAppPath[MAX_PATH] = L"";

    if (!GetModuleFileName(0, szAppPath, MAX_PATH))
        return FALSE;

    // Extract name
    wstrAppName = szAppPath;
    wstrAppName = wstrAppName.substr(wstrAppName.rfind(L"\\") + 1);
    wstrAppName = wstrAppName.substr(0, wstrAppName.find(L"."));
    return TRUE;
}

wstring Zimbra::MAPI::Util::GetUniqueName()
{
    GUID guid;
    HRESULT hr = CoCreateGuid(&guid);

    if (hr != S_OK)
        return L"";

    BYTE *str;

    hr = UuidToString((UUID *)&guid, (RPC_WSTR *)&str);
    if (hr != RPC_S_OK)
        return L"";

    wstring unique = (LPTSTR)str;

    RpcStringFree((RPC_WSTR *)&str);
    replace(unique.begin(), unique.end(), '-', '_');
    unique += L"_migwiz";
    return unique;
}

bool Zimbra::MAPI::Util::GetDomainName(wstring &sDomain)
{
    LOGFN_TRACE_NO;

    sDomain = L"";

    /*
     *     wstring wDomain = L"";
     * DWORD dwLevel = 102;
     * LPWKSTA_INFO_102 pBuf = NULL;
     * NET_API_STATUS nStatus;
     * LPWSTR pszServerName = NULL;
     *
     * nStatus = NetWkstaGetInfo(pszServerName, dwLevel, (LPBYTE *)&pBuf);
     * if (nStatus == NERR_Success)
     * {
     *     wDomain = pBuf->wki102_langroup;
     *     // printf("\n\tPlatform: %d\n", pBuf->wki102_platform_id);
     *     // wprintf(L"\tName:     %s\n", pBuf->wki102_computername);
     *     // printf("\tVersion:  %d.%d\n", pBuf->wki102_ver_major,
     *     // pBuf->wki102_ver_minor);
     *     //wprintf(L"\tLan Root: %s\n", pBuf->wki102_lanroot);
     *     // wprintf(L"\t# Logged On Users: %d\n", pBuf->wki102_logged_on_users);
     * }
     * // Free the allocated memory.
     * if (pBuf != NULL)
     *     NetApiBufferFree(pBuf);
     *     wstrDomain = wDomain;
     */

    bool ret = false;

    LSA_OBJECT_ATTRIBUTES objectAttributes;
    ZeroMemory(&objectAttributes, sizeof (objectAttributes));

    LSA_HANDLE policyHandle;
    NTSTATUS status = LsaOpenPolicy(NULL, &objectAttributes, GENERIC_READ | POLICY_VIEW_LOCAL_INFORMATION, &policyHandle);
    if (!status) // STATUS_SUCCESS defined in ntstatus.h but including that causes compile issues. See https://msdn.microsoft.com/en-us/library/windows/desktop/aa378299(v=vs.85).aspx
    {
        PPOLICY_PRIMARY_DOMAIN_INFO info;
        status = LsaQueryInformationPolicy(policyHandle, PolicyPrimaryDomainInformation, (LPVOID *)&info);
        if (!status)
        {
            // If we have a Sid, machine is on domain
            if (info->Sid)
            {
                sDomain = info->Name.Buffer;
                LOG_INFO(_T("Machine is on domain '%s'"), sDomain.c_str());
                ret = true;
            }
            else
                LOG_INFO(_T("Machine is not on domain"));

            LsaFreeMemory(info);
        }
        else
            LOG_ERROR(_T("GetDomainName(): Failed to query policy. Error:%8X. Please run the migration tool as Administrator."), status);

        LsaClose(policyHandle);
    }
    else
        LOG_ERROR(_T("GetDomainName(): Failed to open policy. Error:%8X. Please run the migration tool as Administrator"), status);
    
    return ret;
}

BOOL Zimbra::MAPI::Util::CreatePSTProfile(LPSTR lpstrProfileName, LPSTR lpstrPSTFQPathName, bool bNoUI)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;
    
    // Get IProfAdmin interface pointer
    Zimbra::Util::ScopedInterface<IProfAdmin> iprofadmin;
    if (FAILED(hr = MAPIAdminProfiles(0, iprofadmin.getptr())))
        throw MapiUtilsException(hr, L"Util:: CreatePSTProfile(): MAPIAdminProfiles Failed.", ERR_CREATE_PSTPROFILE, __LINE__, __FILE__);

    if (FAILED(hr = iprofadmin->CreateProfile((LPTSTR)lpstrProfileName, NULL, NULL, 0)))
        throw MapiUtilsException(hr, L"Util:: CreatePSTProfile(): CreateProfile Failed.", ERR_CREATE_PSTPROFILE, __LINE__, __FILE__);

    Zimbra::Util::ScopedInterface<IMsgServiceAdmin> imsadmin;
    if (FAILED(hr = iprofadmin->AdminServices((LPTSTR)lpstrProfileName, NULL, NULL, 0, imsadmin.getptr())))
        throw MapiUtilsException(hr, L"Util:: CreatePSTProfile(): AdminServices Failed.", ERR_CREATE_PSTPROFILE, __LINE__, __FILE__);

    // Now create the message-store-service.
    hr = imsadmin->CreateMsgService((LPTSTR)"MSUPST MS", (LPTSTR)"ZimbraPSTMigration Message Store", NULL, !bNoUI ? SERVICE_UI_ALLOWED : 0);
    if (hr == MAPI_E_UNKNOWN_FLAGS)
        hr = imsadmin->CreateMsgService((LPTSTR)"MSUPST MS", (LPTSTR)"ZimbraPSTMigration Message Store", 0, 0);
    if (hr != S_OK)
        throw MapiUtilsException(hr, L"Util:: CreatePSTProfile(): CreateMsgService Failed.", ERR_CREATE_PSTPROFILE, __LINE__, __FILE__);

    // We need to get hold of the MAPIUID for this message-service. We do this
    // by enumerating the message-stores (there will be only one!) and picking it up.
    // Actually, we set up 'mscols' to retrieve the name as well as the MAPIUID, for
    // reasons that will become apparent in just a moment.
    Zimbra::Util::ScopedInterface<IMAPITable> mstable;
    if (FAILED(hr = imsadmin->GetMsgServiceTable(0, mstable.getptr())))
        throw MapiUtilsException(hr, L"Util:: CreatePSTProfile(): CreateMsgService Failed.", ERR_CREATE_PSTPROFILE, __LINE__, __FILE__);

    SizedSPropTagArray(2, mscols) = {2, { PR_SERVICE_UID, PR_DISPLAY_NAME }};
    mstable->SetColumns((SPropTagArray *)&mscols, 0);

    Zimbra::Util::ScopedRowSet msrows;
    if (FAILED(hr = mstable->QueryRows(1, 0, msrows.getptr())))
        throw MapiUtilsException(hr, L"Util:: CreatePSTProfile(): QueryRows Failed.", ERR_CREATE_PSTPROFILE, __LINE__, __FILE__);


    // Now we wish to configure our message-store to use the PST filename.
    SPropValue msprops[1];
    MAPIUID msuid = *((MAPIUID *)msrows->aRow[0].lpProps[0].Value.bin.lpb);
    msprops[0].ulPropTag = PR_PST_PATH;
    msprops[0].Value.lpszA = (char *)lpstrPSTFQPathName;
    if (FAILED(hr = imsadmin->ConfigureMsgService(&msuid, NULL, !bNoUI ? SERVICE_UI_ALLOWED : 0, 1, msprops)))
        throw MapiUtilsException(hr, L"Util:: CreatePSTProfile(): ConfigureMsgService Failed.", ERR_CREATE_PSTPROFILE, __LINE__, __FILE__);

    //Create supporting OL profile entries else crash may happen!
    LPWSTR lpwstrProfileName = NULL;
    AtoW(lpstrProfileName,lpwstrProfileName);
    if(!SetOLProfileRegistryEntries(lpwstrProfileName))
    {
        Zimbra::Util::SafeDelete(lpwstrProfileName);
        throw MapiUtilsException(hr, L"Util:: CreatePSTProfile()::SetOLProfileRegistryEntries Failed.", ERR_CREATE_PSTPROFILE, __LINE__, __FILE__);
    }

    Zimbra::Util::SafeDelete(lpwstrProfileName);
    return TRUE;
}

bool Zimbra::MAPI::Util::SetOLProfileRegistryEntries(LPCWSTR strProfileName)
{
    LOGFN_TRACE_NO;

    bool bRet = false;

    // ---------------------------------------------------------------------------------
    // Get OL version
    // ---------------------------------------------------------------------------------
    int iOLVersion = -1;
    LONG lRet = GetOutlookVersion(iOLVersion);
    if (lRet == ERROR_SUCCESS)
    {
        LOG_TRACE(_T("OL Version: %d"), iOLVersion);

        // ---------------------------------------------------------------------------------
        // Calc version dependent registry path
        // ---------------------------------------------------------------------------------
        wstring sRegistryKeyPath = L"";

        if (iOLVersion >= 15)
        {
            TCHAR szKey[MAX_PATH + 1] = { 0 };
            _sntprintf_s(szKey, MAX_PATH+1, _TRUNCATE, _T("Software\\Microsoft\\Office\\%d\\Outlook\\Profiles\\"), iOLVersion);
            sRegistryKeyPath = szKey;
        }
        else
            sRegistryKeyPath = L"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Windows Messaging Subsystem\\Profiles\\";
        
        sRegistryKeyPath += strProfileName;
        sRegistryKeyPath += L"\\0a0d020000000000c000000000000046";	

        // ---------------------------------------------------------------------------------
        // Create registry key
        // ---------------------------------------------------------------------------------
        DWORD dwDisposition = 0;
        HKEY hKey = NULL;
        LONG lRetCode = RegCreateKeyEx(HKEY_CURRENT_USER, (LPTSTR)sRegistryKeyPath.c_str(), 0, NULL, REG_OPTION_NON_VOLATILE, KEY_SET_VALUE, NULL, &hKey, &dwDisposition);

        // ---------------------------------------------------------------------------------
        // Outlook 2007 requires additional values
        // ---------------------------------------------------------------------------------
        // Without these key, OOM throws an exception
        if (iOLVersion == 12) // OL 2007
        {							
            if (lRetCode == ERROR_SUCCESS)
            {
                BYTE pData[4] = { 0x66, 0xe6, 0x01, 0x00 };
                RegSetValueEx(hKey, _T("0003036f"), 0, REG_BINARY, pData, sizeof (pData));

                BYTE pData2[4] = { 0x64, 0x00, 0x000, 0x00 };
                RegSetValueEx(hKey, _T("00030397"), 0, REG_BINARY, pData2, sizeof (pData2));

                BYTE pData3[4] = { 0x02, 0x00, 0x00, 0x00 };
                RegSetValueEx(hKey, _T("00030429"), 0, REG_BINARY, pData3, sizeof (pData3));

                bRet = true;
            }
        }
        else 
        if(iOLVersion >= 14) // >= OL2010
        {
            if( lRetCode == ERROR_SUCCESS )
            {
                BYTE pData[4] = {0x78, 0x35, 0x02, 0x00};
                long lRet = RegSetValueEx(hKey, _T("0003036f"), 0, REG_BINARY, pData, sizeof(pData)); 	
                UNREFERENCED_PARAMETER(lRet);
                bRet = true;
            }
        }

        RegCloseKey(hKey);
    }
    return bRet;
}

BOOL Zimbra::MAPI::Util::DeleteAlikeProfiles(LPCSTR lpstrProfileName)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;
        
    // Get IProfAdmin interface pointer
    Zimbra::Util::ScopedInterface<IProfAdmin> iprofadmin;
    if (FAILED(hr = MAPIAdminProfiles(0, iprofadmin.getptr())))
        throw MapiUtilsException(hr, L"Util:: DeleteAlikeProfiles(): MAPIAdminProfiles Failed.", ERR_DELETE_PROFILE, __LINE__, __FILE__);

    Zimbra::Util::ScopedInterface<IMAPITable> proftable;
    if (FAILED(hr = iprofadmin->GetProfileTable(0, proftable.getptr())))
        throw MapiUtilsException(hr, L"Util:: DeleteAlikeProfiles(): GetProfileTable Failed.", ERR_DELETE_PROFILE, __LINE__, __FILE__);

    SizedSPropTagArray(2, proftablecols) = {2, { PR_DISPLAY_NAME_A, PR_DEFAULT_PROFILE }};
    Zimbra::Util::ScopedRowSet profrows;
    if (SUCCEEDED(hr = HrQueryAllRows(proftable.get(), (SPropTagArray *)&proftablecols, NULL, NULL, 0, profrows.getptr())))
    {
        for (unsigned int i = 0; i < profrows->cRows; i++)
        {
            std::string name = "";

            if (profrows->aRow[i].lpProps[0].ulPropTag == PR_DISPLAY_NAME_A)
                name = profrows->aRow[i].lpProps[0].Value.lpszA;
            if (name.find(lpstrProfileName) != std::string::npos)
                hr = iprofadmin->DeleteProfile((LPTSTR)name.c_str(), 0);
        }
    }
    return TRUE;
}

LONG Zimbra::MAPI::Util::GetOutlookVersion(int &iVersion)
{
    LOGFN_TRACE_NO;

    // Get the outlook version if its installed
    HKEY hKey = NULL;
    LONG lRetCode = RegOpenKeyEx(HKEY_CLASSES_ROOT, _T("Outlook.Application\\CurVer"), 0, KEY_READ, &hKey);
    if (ERROR_SUCCESS == lRetCode)
    {
        DWORD cData = 0, dwType = REG_SZ;
        RegQueryValueEx(hKey, NULL, 0, &dwType, NULL, &cData);

        LPTSTR pszOlkVer = reinterpret_cast<LPTSTR>(new BYTE[cData]);
        lRetCode = RegQueryValueEx(hKey, NULL, 0, &dwType, reinterpret_cast<LPBYTE>(pszOlkVer), &cData);
        if (ERROR_SUCCESS == lRetCode)
        {
            int nOlkVer = 0;
            _stscanf(pszOlkVer, _T("Outlook.Application.%d"), &nOlkVer);

            if ((11 == nOlkVer) || (12 == nOlkVer) || (nOlkVer >= 14))
                iVersion = nOlkVer;
        }

        delete[] pszOlkVer;
        RegCloseKey(hKey);
    }
    return lRetCode;
}

bool TextBody(LPMESSAGE pMessage, LPSPropValue lpv, LPTSTR *ppBody, unsigned int &nTextChars)
{
    LOGFN_TRACE_NO;

    if (lpv->ulPropTag == PR_BODY)
    {
        // Get props returned body directly - use it
        #pragma warning(push)
        #pragma warning(disable: 4995)
            LPTSTR pBody = lpv->Value.LPSZ;
            int nLen = (int)_tcslen(pBody);

            MAPIAllocateBuffer((nLen + 1) * sizeof (TCHAR), (LPVOID FAR *)ppBody);
            _tcscpy(*ppBody, pBody);
            nTextChars = nLen;
            return true;
        #pragma warning(pop)
    }
    else 
    if ((PROP_TYPE(lpv->ulPropTag) == PT_ERROR) && (lpv->Value.l == E_OUTOFMEMORY))
    {
        // Too large to return directly - access via OpenProperty() + stream
        IStream *pStream = NULL;
        HRESULT hr = pMessage->OpenProperty(PR_BODY, &IID_IStream, STGM_READ, 0, (LPUNKNOWN FAR *)&pStream);
        if (FAILED(hr))
            return false;

        // discover the size of the incoming body
        STATSTG statstg;
        hr = pStream->Stat(&statstg, STATFLAG_NONAME);
        if (FAILED(hr))
        {
            pStream->Release();
            pStream = NULL;
            return false;
        }
        unsigned bodySize = statstg.cbSize.LowPart;

        // allocate buffer for incoming body data
        hr = MAPIAllocateBuffer(bodySize + 10, (LPVOID FAR *)ppBody);
        ZeroMemory(*ppBody, bodySize + 10);
        if (FAILED(hr))
        {
            pStream->Release();
            pStream = NULL;
            return false;
        }

        // Read the text
        ULONG cb;
        hr = pStream->Read(*ppBody, statstg.cbSize.LowPart, &cb);
        if (FAILED(hr))
        {
            pStream->Release();
            pStream = NULL;
            return false;
        }
        if (cb != statstg.cbSize.LowPart)
        {
            pStream->Release();
            pStream = NULL;
            return false;
        }

        // close the stream
        pStream->Release();
        pStream = NULL;
        nTextChars = (unsigned int)_tcslen(*ppBody);
        return true;
    }

    // some other error occurred?
    // i.e., some messages do not have a body
    *ppBody = NULL;
    return false;
}

bool HtmlBody(LPMESSAGE pMessage, LPSPropValue lpv, LPVOID *ppBody, unsigned int &nHtmlBodyLen)
{
    LOGFN_TRACE_NO;

    if (lpv->ulPropTag == PR_HTML)
    {
        LPVOID pBody = lpv->Value.bin.lpb;
        if (pBody)
        {
            size_t nLen = lpv->Value.bin.cb;

            MAPIAllocateBuffer((ULONG)(nLen + 10), (LPVOID FAR *)ppBody);
            ZeroMemory(*ppBody, (nLen + 10));
            memcpy(*ppBody, pBody, nLen);
            nHtmlBodyLen = (UINT)nLen;
            return true;
        }
    }

    // Try to extract HTML BODY using the stream property.
    IStream *pStream;
    HRESULT hr = pMessage->OpenProperty(PR_HTML, &IID_IStream, STGM_READ, 0, (LPUNKNOWN FAR *)&pStream);
    if (SUCCEEDED(hr))
    {
        // discover the size of the incoming body
        STATSTG statstg;
        hr = pStream->Stat(&statstg, STATFLAG_NONAME);
        if (FAILED(hr))
            throw MAPIMessageException(E_FAIL, L"HtmlBody(): pStream->Stat Failed.", ERR_MESSAGE_BODY, __LINE__, __FILE__);

        unsigned bodySize = statstg.cbSize.LowPart;
        nHtmlBodyLen = bodySize;

        // allocate buffer for incoming body data
        hr = MAPIAllocateBuffer(bodySize + 10, ppBody);
        ZeroMemory(*ppBody, bodySize + 10);
        if (FAILED(hr))
            throw MAPIMessageException(E_FAIL, L"HtmlBody(): ZeroMemory Failed.", ERR_MESSAGE_BODY, __LINE__, __FILE__);

        // read the text
        ULONG cb;
        hr = pStream->Read(*ppBody, statstg.cbSize.LowPart, &cb);
        if (FAILED(hr))
            throw MAPIMessageException(E_FAIL, L"HtmlBody(): pStream->Read Failed.", ERR_MESSAGE_BODY, __LINE__, __FILE__);
        if (cb != statstg.cbSize.LowPart)
            throw MAPIMessageException(E_FAIL, L"HtmlBody(): statstg.cbSize.LowPart Failed.", ERR_MESSAGE_BODY, __LINE__, __FILE__);

        // close the stream
        pStream->Release();
        return true;
    }

    // some other error occurred?
    // i.e., some messages do not have a body
    *ppBody = NULL;
    nHtmlBodyLen = 0;
    return false;
}

LPWSTR WriteUnicodeToFile(LPTSTR pBody)
{
    LPTSTR pTemp = pBody;
    int nBytesToBeWritten = (int)(wcslen(pTemp)*sizeof(WCHAR));
   
    wstring wstrTempAppDirPath;
    if (!Zimbra::MAPI::Util::GetAppTemporaryDirectory(wstrTempAppDirPath))
        return L"";

    char *lpszDirName = NULL;
    char *lpszUniqueName = NULL;
    Zimbra::Util::ScopedInterface<IStream> pStream;
    ULONG nBytesWritten = 0;
    ULONG nTotalBytesWritten = 0;
    HRESULT hr = S_OK;

    WtoA((LPWSTR)wstrTempAppDirPath.c_str(), lpszDirName);

    string strFQFileName = lpszDirName;

    WtoA((LPWSTR)Zimbra::MAPI::Util::GetUniqueName().c_str(), lpszUniqueName);
    strFQFileName += "\\";
    strFQFileName += lpszUniqueName;
    SafeDelete(lpszDirName);
    SafeDelete(lpszUniqueName);

    // Open stream on file
    if (FAILED(hr = OpenStreamOnFile(MAPIAllocateBuffer, MAPIFreeBuffer, STGM_CREATE | STGM_READWRITE, (LPTSTR)strFQFileName.c_str(), NULL, pStream.getptr())))
        return L"";

    // write to file
    while (!FAILED(hr) && nBytesToBeWritten > 0)
    {
        hr = pStream->Write(pTemp, nBytesToBeWritten, &nBytesWritten);
        pTemp += nBytesWritten;
        nBytesToBeWritten -= nBytesWritten;
        nTotalBytesWritten += nBytesWritten;
        nBytesWritten = 0;
    }
    if (FAILED(hr = pStream->Commit(0)))
        return L"";

    LPWSTR lpwstrFQFileName = NULL;

    AtoW((LPSTR)strFQFileName.c_str(), lpwstrFQFileName);
    return lpwstrFQFileName;
}

bool Zimbra::MAPI::Util::DumpContentsToFile(LPTSTR pBody, string strFilePath,bool isAscii)
{
    LPSTR pTemp = NULL;
    int nBytesToBeWritten;

    pTemp = (isAscii) ? (LPSTR)pBody : Zimbra::Util::UnicodeToAnsii(pBody);
    nBytesToBeWritten = (int)strlen(pTemp);
    Zimbra::Util::ScopedInterface<IStream> pStream;
    ULONG nBytesWritten = 0;
    ULONG nTotalBytesWritten = 0;
    HRESULT hr = S_OK;

    // Open stream on file
    if (FAILED(hr = OpenStreamOnFile(MAPIAllocateBuffer, MAPIFreeBuffer, STGM_CREATE | STGM_READWRITE, (LPTSTR)strFilePath.c_str(), NULL, pStream.getptr())))
        return false;

    // write to file
    while (!FAILED(hr) && nBytesToBeWritten > 0)
    {
        hr = pStream->Write(pTemp, nBytesToBeWritten, &nBytesWritten);
        pTemp += nBytesWritten;
        nBytesToBeWritten -= nBytesWritten;
        nTotalBytesWritten += nBytesWritten;
        nBytesWritten = 0;
    }
    if (FAILED(hr = pStream->Commit(0)))
        return false;

    return true;
}

LPWSTR WriteContentsToFile(LPTSTR pBody, bool isAscii)
{
    LPSTR pTemp = NULL;
    int nBytesToBeWritten;
    pTemp = (isAscii) ? (LPSTR)pBody : Zimbra::Util::UnicodeToAnsii(pBody);
    nBytesToBeWritten = (int)strlen(pTemp);
   
    wstring wstrTempAppDirPath;
    if (!Zimbra::MAPI::Util::GetAppTemporaryDirectory(wstrTempAppDirPath))
        return L"";

    char *lpszDirName = NULL;
    char *lpszUniqueName = NULL;
    Zimbra::Util::ScopedInterface<IStream> pStream;
    ULONG nBytesWritten = 0;
    ULONG nTotalBytesWritten = 0;
    HRESULT hr = S_OK;

    WtoA((LPWSTR)wstrTempAppDirPath.c_str(), lpszDirName);

    string strFQFileName = lpszDirName;

    WtoA((LPWSTR)Zimbra::MAPI::Util::GetUniqueName().c_str(), lpszUniqueName);
    strFQFileName += "\\";
    strFQFileName += lpszUniqueName;
    SafeDelete(lpszDirName);
    SafeDelete(lpszUniqueName);

    // Open stream on file
    if (FAILED(hr = OpenStreamOnFile(MAPIAllocateBuffer, MAPIFreeBuffer, STGM_CREATE | STGM_READWRITE, (LPTSTR)strFQFileName.c_str(), NULL, pStream.getptr())))
        return L"";

    // write to file
    while (!FAILED(hr) && nBytesToBeWritten > 0)
    {
        hr = pStream->Write(pTemp, nBytesToBeWritten, &nBytesWritten);
        pTemp += nBytesWritten;
        nBytesToBeWritten -= nBytesWritten;
        nTotalBytesWritten += nBytesWritten;
        nBytesWritten = 0;
    }
    if (FAILED(hr = pStream->Commit(0)))
        return L"";

    LPWSTR lpwstrFQFileName = NULL;

    AtoW((LPSTR)strFQFileName.c_str(), lpwstrFQFileName);
    return lpwstrFQFileName;
}

wstring Zimbra::MAPI::Util::SetPlainText(LPMESSAGE pMessage, LPSPropValue lpv)
{
    wstring retval = L"";
    LPTSTR pBody = NULL;
    UINT nText = 0;

    bool bRet = TextBody(pMessage, lpv, &pBody, nText);
    if (bRet)
    {
        LPWSTR lpwszTempFile = WriteUnicodeToFile(pBody);
        retval = lpwszTempFile;
        SafeDelete(lpwszTempFile);
    }
    return retval;
}

wstring Zimbra::MAPI::Util::SetHtml(LPMESSAGE pMessage, LPSPropValue lpv)
{
    wstring retval = L"";
    LPVOID pBody = NULL;
    UINT nText = 0;

    bool bRet = HtmlBody(pMessage, lpv, &pBody, nText);
    if (bRet)
    {
        LONG lCPID = CP_UTF8;
        LPSPropValue lpPropCPID = NULL;
        HRESULT hr = HrGetOneProp(pMessage, PR_INTERNET_CPID, &lpPropCPID);
        if (!SUCCEEDED(hr))
            hr = HrGetOneProp(pMessage, PR_MESSAGE_CODEPAGE, &lpPropCPID);
        if (SUCCEEDED(hr))
            lCPID = lpPropCPID->Value.l;

        int nChars = MultiByteToWideChar(lCPID, 0, (LPCSTR)pBody, -1, NULL, 0);
        LPWSTR pwszStr = new WCHAR[nChars + 1];
        MultiByteToWideChar(lCPID, 0, (LPCSTR)pBody, -1, pwszStr, nChars);
        pwszStr[nChars] = L'\0';

        LPWSTR lpwszTempFile = WriteUnicodeToFile(pwszStr);
        retval = lpwszTempFile;
        SafeDelete(lpwszTempFile);
    }
    return retval;
}

wstring Zimbra::MAPI::Util::CommonDateString(FILETIME ft)
{
    wstring retval = L"";
    SYSTEMTIME st;      // convert the filetime to a system time.

    FileTimeToSystemTime(&ft, &st);

    // build the GMT date/time string
    //int nWritten = GetDateFormatW(MAKELCID(MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US),
    //    SORT_DEFAULT), LOCALE_USE_CP_ACP, &st, L"ddd, d MMM yyyy", retval, 32);

    //GetTimeFormatW(MAKELCID(MAKELANGID(LANG_ENGLISH, SUBLANG_ENGLISH_US), SORT_DEFAULT),
    //    LOCALE_USE_CP_ACP, &st, L" HH:mm:ss -0000", (retval + nWritten - 1), 32 -
    //    nWritten + 1);

    WCHAR szLocalDate[254 + 1] = { 0 };
    GetDateFormat(LOCALE_USER_DEFAULT, DATE_LONGDATE, &st, NULL, szLocalDate, 254);

    WCHAR szLocalTime[254 + 1] = { 0 };
    GetTimeFormat(LOCALE_USER_DEFAULT, 0, &st, NULL, szLocalTime, 254);

    retval = szLocalDate;
    retval += L" ";
    retval += szLocalTime;

    return retval;
}

LPWSTR Zimbra::MAPI::Util::EscapeCategoryName(LPCWSTR pwszOrigCategoryName)
{
    // replace all colons with spaces
    // replace all double quotes with single quotes
    // replace all forward slashes with back slashes
    // remove all back slashes at the beginning of the string

    // worst case is this string is just as long as the original
    LPWSTR pwszString = new WCHAR[wcslen(pwszOrigCategoryName) + 1];

    int length = (int)wcslen(pwszOrigCategoryName);
    int counter = 0;
    for(counter = 0; counter < length; counter++)
    {
        WCHAR theChar = pwszOrigCategoryName[counter];

        // now build the new string as we want it
        if (theChar < 0x20) // replace control character with space
            pwszString[counter] = L' ';        
        else 
        if ((theChar == L'\\')&&(counter == 0)) // replace first backslash with a minus
            pwszString[counter] = L'-';  
        else 
        if (theChar == L':')     // replace colons with pipe
            pwszString[counter] = L'|';        
        else 
        if (theChar == L'\"')    // replace double quotes with single quotes
           pwszString[counter] = L'\'';        
        else 
        if(theChar == L'/')
        {
            // if the forward slash is the first, character
            // we need to change it to a minus instead
            // of a backslash
            if (counter == 0)
                pwszString[counter] = L'-';   
            else
                pwszString[counter] = L'\\';   // replace forward slashes with back slash
        }
        else
            pwszString[counter] = theChar;
    }
    
    // add the null terminator
    pwszString[counter] = L'\0';

    //trim leading and trailing spaces
    int nNonSpaceStart = 0;
    int nNonSpaceEnd = length - 1;

    while(pwszString[nNonSpaceStart] == L' ')
    nNonSpaceStart++;

    while(pwszString[nNonSpaceEnd] == L' ')
    nNonSpaceEnd--;

    if ((nNonSpaceStart == 0) && (nNonSpaceEnd == (length - 1)))
    {
        return pwszString;
    }
    else 
    if(nNonSpaceEnd < nNonSpaceStart)
    {
        delete[] pwszString;
        return NULL;
    }

    LPWSTR pwszNoLeadTrailSpace = new WCHAR[nNonSpaceEnd - nNonSpaceStart + 2];
    int i;
    for (i = 0, counter = nNonSpaceStart; counter <= nNonSpaceEnd; counter++, i++)
    {
        pwszNoLeadTrailSpace[i] = pwszString[counter];
    }
    
    // add the null terminator
    pwszNoLeadTrailSpace[i] = L'\0';

    delete[] pwszString ;

    return pwszNoLeadTrailSpace;
}

CString Zimbra::MAPI::Util::GetGUID()
{
    GUID guid;
    HRESULT hr = CoCreateGuid(&guid);
    if (hr != S_OK)
        return L"";

    BYTE * str;
    hr = UuidToString((UUID*)&guid, (RPC_WSTR*)&str);
    if(hr != RPC_S_OK)
        return L"";

    CString unique((LPTSTR)str);
    RpcStringFree((RPC_WSTR*)&str);
    unique.Replace(_T("-"), _T("_"));
    unique += "_migwiz";

    return unique;
}

void Zimbra::MAPI::Util::GetContentTypeFromExtension(LPSTR pExt, LPSTR &pContentType)
{
    pContentType = NULL;
    if (pExt == NULL)
        return;

    HKEY hExtKey;
    if (RegOpenKeyA(HKEY_CLASSES_ROOT, pExt, &hExtKey) != ERROR_SUCCESS)
        return;

    DWORD type;
    DWORD nBytes;
    if ((RegQueryValueExA(hExtKey, "Content Type", NULL, &type, NULL, &nBytes) != ERROR_SUCCESS) || (type != REG_SZ))
    {
        RegCloseKey(hExtKey);
        return;
    }

    pContentType = new CHAR[nBytes];
    if (RegQueryValueExA(hExtKey, "Content Type", NULL, &type, (LPBYTE)pContentType, &nBytes) != ERROR_SUCCESS)
    {
        RegCloseKey(hExtKey);
        delete[] pContentType;
        pContentType = NULL;
        return;
    }
    RegCloseKey(hExtKey);
}

CString ConvertToLDAPDN(CString dn)
{
    // dn = CString("/") + dn;
    CString out;
    int indx;

    while ((indx = dn.ReverseFind('/')) != -1)
    {
        int len = dn.GetLength();

        out += dn.Right(len - indx);
        dn.Delete(indx, len - indx);
    }
    out.Replace('/', ',');
    indx = out.Find(',');
    out.Delete(indx);
    return out;
}

HRESULT Zimbra::MAPI::Util::GetSMTPFromAD(Zimbra::MAPI::MAPISession &session, RECIP_INFO &recipInfo,
                                          wstring strUser, wstring strPsw, tstring &strSmtpAddress)
{
    LOGFN_INTERNAL_NO;

    UNREFERENCED_PARAMETER(session);
    HRESULT hr = S_OK;

    wstring strContainer = L"LDAP://";

    // Get the Domain Controller Name for the logged on user
    PDOMAIN_CONTROLLER_INFO pInfo;
    DWORD dwRes = DsGetDcName(NULL, NULL, NULL, NULL, NULL, &pInfo);
    if (ERROR_SUCCESS == dwRes)
    {
        // Store the Domain Controller name if we got it
        strContainer += pInfo->DomainControllerName + 2;
        NetApiBufferFree(pInfo);
    }

    if (strContainer.find(_T("LDAP://")) == -1)
    {
        wstring strContainerPath = _T("LDAP://");

        strContainerPath = strContainerPath + strContainer;
        strContainer = strContainerPath;
    }

    static std::map<tstring, tstring> mapExAddrSMTP;

    strSmtpAddress = mapExAddrSMTP[tstring(recipInfo.pEmailAddr)];
    if (!strSmtpAddress.size())
    {
        // Get IDirectorySearch Object
        CComPtr<IDirectorySearch> pDirSearch;
        if (strUser.length() || strPsw.length())
            hr = ADsOpenObject(strContainer.c_str(), strUser.c_str(), strPsw.c_str(), ADS_SECURE_AUTHENTICATION, IID_IDirectorySearch, (void **)&pDirSearch);
        else
            hr = ADsOpenObject(strContainer.c_str(), NULL, NULL, ADS_SECURE_AUTHENTICATION, IID_IDirectorySearch, (void **)&pDirSearch);
        if (FAILED(hr))
        {
            return E_FAIL;
        }

        // Set Search Preferences
        ADS_SEARCHPREF_INFO searchPrefs[2];

        searchPrefs[0].dwSearchPref = ADS_SEARCHPREF_SEARCH_SCOPE;
        searchPrefs[0].vValue.dwType = ADSTYPE_INTEGER;
        searchPrefs[0].vValue.Integer = ADS_SCOPE_SUBTREE;

        // Ask for only one object that satisfies the criteria
        searchPrefs[1].dwSearchPref = ADS_SEARCHPREF_SIZE_LIMIT;
        searchPrefs[1].vValue.dwType = ADSTYPE_INTEGER;
        searchPrefs[1].vValue.Integer = 1;

        pDirSearch->SetSearchPreference(searchPrefs, 2);

        CComBSTR filter;
        filter = _T("(|(distinguishedName=");

        CString strTempDN = ConvertToLDAPDN(recipInfo.pEmailAddr);

        filter.Append(strTempDN.GetBuffer());
        filter.Append(_T(")(legacyExchangeDN="));
        filter += recipInfo.pEmailAddr;
        filter.Append(_T("))"));

        ADS_SEARCH_HANDLE hSearch;

        // Retrieve the "mail" attribute for the specified dn
        LPWSTR pAttributes = L"mail";
        hr = pDirSearch->ExecuteSearch(filter, &pAttributes, 1, &hSearch);
        if (FAILED(hr))
            return E_FAIL;

        ADS_SEARCH_COLUMN mailCol;

        while (SUCCEEDED(hr = pDirSearch->GetNextRow(hSearch)))
        {
            if (S_OK == hr)
            {
                hr = pDirSearch->GetColumn(hSearch, pAttributes, &mailCol);
                if (FAILED(hr))
                    break;
                strSmtpAddress = mailCol.pADsValues->CaseIgnoreString;

                // Make an entry in the map
                mapExAddrSMTP[tstring(recipInfo.pEmailAddr)] = strSmtpAddress;

                pDirSearch->CloseSearchHandle(hSearch);

                return S_OK;
            }
            else 
            if (S_ADS_NOMORE_ROWS == hr)
            {
                // Call ADsGetLastError to see if the search is waiting for a response.
                DWORD dwError = ERROR_SUCCESS;
                WCHAR szError[512];
                WCHAR szProvider[512];

                ADsGetLastError(&dwError, szError, 512, szProvider, 512);
                if (ERROR_MORE_DATA != dwError)
                    break;
            }
            else
            {
                break;
            }
        }
        pDirSearch->CloseSearchHandle(hSearch);
        return E_FAIL;
    }
    else
    {
        return S_OK;
    }
}

bool Zimbra::MAPI::Util::CheckStringProp(_In_opt_ LPSPropValue lpProp, ULONG ulPropType)
{
    if (PT_STRING8 != ulPropType && PT_UNICODE != ulPropType)
        return false;

    if (!lpProp)
        return false;

    if (PT_ERROR == PROP_TYPE(lpProp->ulPropTag))
        return false;

    if (ulPropType != PROP_TYPE(lpProp->ulPropTag))
        return false;

    if (NULL == lpProp->Value.LPSZ)
        return false;

    if (PT_STRING8 == ulPropType && NULL == lpProp->Value.lpszA[0])
        return false;

    if (PT_UNICODE == ulPropType && NULL == lpProp->Value.lpszW[0])
        return false;

    return true;
} // CheckStringProp

BOOL Zimbra::MAPI::Util::IsOutlookRunning()
{
    LOGFN_TRACE_NO;

    // Get the list of process identifiers.
    DWORD arrProcesseIds[1024] = { 0 }, dwNeededBytes = 0, dwProcesses = 0;
    if (!EnumProcesses(arrProcesseIds, sizeof (arrProcesseIds), &dwNeededBytes))
        return FALSE;

    // Calculate how many process identifiers were returned.
    dwProcesses = dwNeededBytes / sizeof (DWORD);

    // Find outlook.exe
    for (DWORD i = 0; i < dwProcesses; i++)
    {
        TCHAR szProcessName[MAX_PATH] = { 0 };

        // Get a handle to the process.
        HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, arrProcesseIds[i]);
        if (hProcess)
        {
            // Get the process name.
            HMODULE hMod = NULL;
            if (EnumProcessModules(hProcess, &hMod, sizeof (hMod), &dwNeededBytes))
                GetModuleBaseName(hProcess, hMod, szProcessName, sizeof (szProcessName) / sizeof (TCHAR));

            CloseHandle(hProcess);
            if (!_tcsicmp(_T("OUTLOOK.EXE"), szProcessName))
            {
                LOG_INFO(_T("Outlook is running"));
                return TRUE;
            }
        }
    }
    LOG_INFO(_T("Outlook is not running"));
    return FALSE;
}

bool Zimbra::MAPI::Util::PauseAfterLargeMemDelta()
{
    bool bPaused = Zimbra::Util::IsTesting(_T("MigrationPauseAfterLargeMemDelta"));
    if(bPaused)
        Sleep(2000);
    return bPaused;
}

void Zimbra::MAPI::Util::CalcBOMAndSkipSourceEncodingPreamble(BOOL bMacEncoded, 
                                                              LPVOID  pAttachDataIn,  size_t  attachDataLenIn,
                                                              LPVOID& pAttachDataOut, size_t& attachDataLenOut,
                                                              BOM& bom)
{
    // ------------------------------------------------------------------------
    // Calculate out param "bom"
    // ------------------------------------------------------------------------
    bom = BOM_NONE;
    int nboms[] = { 4, 4, 2, 2, 3 };
    BYTE boms[][4] = {  { 0x00, 0x00, 0xFE, 0xFF }, 
                        { 0xFF, 0xFF, 0x00, 0x00 }, 
                        { 0xFE, 0xFF }, 
                        { 0xFF, 0xFE },
                        { 0xEF, 0xBB, 0xBF }};

    int i;
    for (i = 0; i < NBOMS; i++)
    {
        if (attachDataLenIn > (size_t)nboms[i])
        {
            if (memcmp(pAttachDataIn, boms[i], nboms[i]) == 0)
            {
                bom = (BOM)i;
                break;
            }
        }
    }

    // ------------------------------------------------------------------------
    // Calculate out params "pAttachDataOut" and "attachDataLenOut"
    // ------------------------------------------------------------------------
    if (bMacEncoded)
    {
        // Skip 128 bytes
        pAttachDataOut = (LPSTR)(pAttachDataIn) + 128;
        attachDataLenOut = attachDataLenIn - 128;
    }
    else 
    if (bom == BOM_NONE)
    {
        pAttachDataOut =  (LPSTR)(pAttachDataIn);
        attachDataLenOut = attachDataLenIn;
    }
    else
    {
        // Same as above, but skips the BOMs
        pAttachDataOut =  (LPSTR)(((BYTE *)pAttachDataIn) + nboms[i]);
        attachDataLenOut = (attachDataLenIn - nboms[i]);
    }
}

HRESULT Zimbra::MAPI::Util::StreamStringToFile(mimepp::String& s, LPCTSTR pszFilename)
{
    LOGFN_INTERNAL_NO;
    HRESULT hr = S_OK;

    // ----------------------------------------------------
    // Open a stream on the file
    // ----------------------------------------------------
    Zimbra::Util::ScopedInterface<IStream> pStream;
    hr = OpenStreamOnFile(MAPIAllocateBuffer, MAPIFreeBuffer, STGM_CREATE | STGM_READWRITE, (LPTSTR)pszFilename, NULL, pStream.getptr());
    if (FAILED(hr))
    {
        LOG_ERROR(_T("OpenStreamOnFile failed %08X"), hr);
        _ASSERT(FALSE);
        return hr;
    }

    // Don't do the following - causes the entire string to be mapped into mem
    //LPCSTR pszMime = mimeMsg.getString().c_str(); // Causes "Degraded" in log

    // Instead, use SubStr() to grab a portion of the string at a time
    size_t stBytesToWrite = s.size();
    size_t stTotalBytesWritten = 0;

    while (stBytesToWrite > 0)
    {
        // Grab a chunk - 5 meg at a time
        size_t ulChunkSize = 5*1024*1024;
        mimepp::String sSubstr = s.substr(stTotalBytesWritten, ulChunkSize);

        // Write the chunk to the stream
        const char* psz = sSubstr.c_str();      // Causes sSubstr to be assigned its own rep. Shame we can't access the data directly in "s"
        ULONG ulBytesToWrite = static_cast<ULONG>(sSubstr.size()); 

        ULONG ulBytesWritten = 0;
        hr = pStream->Write(psz, ulBytesToWrite, &ulBytesWritten);
        _ASSERT(ulBytesWritten == ulBytesToWrite);
        if (FAILED(hr))
        {
            LOG_ERROR(_T("Write failed %08X"), hr);
            _ASSERT(FALSE);
            return hr;
        }
                    
        // Keep track of what we've done
        stTotalBytesWritten += ulBytesWritten;
        stBytesToWrite      -= ulBytesWritten;

        // sSubstr leaves scope and destructs
    }

    // Commit the stream
    hr = pStream->Commit(0);
    if (FAILED(hr))
    {
        LOG_ERROR(_T("Commit failed %08X"), hr);
        _ASSERT(FALSE);
        return hr;
    }

    return S_OK;
}
