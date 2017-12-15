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

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// Exception class
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
MAPISessionException::MAPISessionException(HRESULT hrErrCode, LPCWSTR lpszDescription): 
    GenericException(hrErrCode, lpszDescription)
{
    //
}

MAPISessionException::MAPISessionException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, 
                                           int nLine, LPCSTR strFile): 
    GenericException(hrErrCode, lpszDescription, lpszShortDescription, nLine, strFile)
{
    //
}

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// MAPI Session Class
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

MAPISession::MAPISession(): m_pMAPISession(NULL)	
{
    LOGFN_TRACE;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);

    // CSessionGlobal::InitMAPI(); // Done by ExchangeAdmin ctor
}

MAPISession::~MAPISession()
{
    LOGFN_TRACE;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);

    if (m_pMAPISession != NULL)
    {
        m_pMAPISession->Logoff(NULL, 0, 0);
        UlRelease(m_pMAPISession);
        m_pMAPISession = NULL;
    }

    // CSessionGlobal::UninitMAPI(); // Done by ExchangeAdmin dtor
}

HRESULT MAPISession::_mapiLogon(LPWSTR strProfile, DWORD dwFlags, LPMAPISESSION &session)
{
    LOGFN_TRACE;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);
    HRESULT hr = S_OK;

   m_sProfileName = strProfile;

    if (FAILED(hr = MAPILogonEx(0, strProfile, NULL, dwFlags, &session)))
        throw MAPISessionException(hr, L"_mapiLogon(): MAPILogonEx Failed.",  ERR_MAPI_LOGON, __LINE__, __FILE__);

    return hr;
}

HRESULT MAPISession::Logon(LPWSTR strProfile)
{
    LOGFN_TRACE;

    DWORD dwFlags = MAPI_EXTENDED | MAPI_NEW_SESSION | MAPI_EXPLICIT_PROFILE | MAPI_NO_MAIL | MAPI_LOGON_UI | fMapiUnicode;
    return _mapiLogon(strProfile, dwFlags, m_pMAPISession);
}

HRESULT MAPISession::Logon(bool bDefaultProfile)
{
    LOGFN_TRACE;

    DWORD dwFlags = MAPI_EXTENDED | MAPI_NEW_SESSION | fMapiUnicode;

    if (bDefaultProfile)
        dwFlags |= MAPI_USE_DEFAULT;
    else
        dwFlags |= MAPI_LOGON_UI;

    return _mapiLogon(NULL, dwFlags, m_pMAPISession);
}

HRESULT MAPISession::OpenDefaultStore(MAPIStore &Store)
{
    LOGFN_TRACE;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);
    HRESULT hr = E_FAIL;
    SBinary defMsgStoreEID;
    LPMDB pDefaultMDB = NULL;

    if (m_pMAPISession == NULL)
        throw MAPISessionException(hr, L"OpenDefaultStore(): m_mapiSession is NULL.", ERR_STORE_ERR, __LINE__, __FILE__);

    if (FAILED(hr = Zimbra::MAPI::Util::HrMAPIFindDefaultMsgStore(m_pMAPISession, defMsgStoreEID)))
        throw MAPISessionException(hr, L"OpenDefaultStore(): HrMAPIFindDefaultMsgStore Failed.", ERR_STORE_ERR, __LINE__, __FILE__);

    Zimbra::Util::ScopedBuffer<BYTE> autoDeletePtr(defMsgStoreEID.lpb);

    LOG_SUMMARY(_T("Opening default store..."));
    hr = m_pMAPISession->OpenMsgStore(NULL, defMsgStoreEID.cb, (LPENTRYID)defMsgStoreEID.lpb, NULL, MDB_ONLINE | MAPI_BEST_ACCESS | MDB_NO_MAIL | MDB_TEMPORARY | MDB_NO_DIALOG, &pDefaultMDB);
    if (hr == MAPI_E_FAILONEPROVIDER)
    {
        LOG_SUMMARY(_T("Opening default store 2..."));
        hr = m_pMAPISession->OpenMsgStore(NULL, defMsgStoreEID.cb, (LPENTRYID)defMsgStoreEID.lpb, NULL, MDB_ONLINE | MAPI_BEST_ACCESS | MDB_NO_MAIL | MDB_TEMPORARY, &pDefaultMDB);
    }
    else 
    if (hr == MAPI_E_UNKNOWN_FLAGS)
    {
        LOG_SUMMARY(_T("Opening default store 3..."));
        hr = m_pMAPISession->OpenMsgStore(NULL, defMsgStoreEID.cb, (LPENTRYID)defMsgStoreEID.lpb, NULL, MAPI_BEST_ACCESS | MDB_NO_MAIL | MDB_TEMPORARY | MDB_NO_DIALOG, &pDefaultMDB);
    }

    if (FAILED(hr))
        throw MAPISessionException(hr, L"OpenDefaultStore(): OpenMsgStore Failed.", ERR_STORE_ERR, __LINE__, __FILE__);

    Store.InitMAPIStore(m_pMAPISession, pDefaultMDB, (LPWSTR)m_sProfileName.c_str());
    return S_OK;
}

HRESULT MAPISession::OpenPublicStore(MAPIStore &Store)
{
    LOGFN_TRACE;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);
    HRESULT hr = S_OK;
    LPMDB pMdb = NULL;
    hr=Store.OpenPublicMessageStore(m_pMAPISession, 0x0000, &pMdb);
    if (FAILED(hr))
        throw MAPISessionException(hr, L"OpenPublicMessageStore() Failed.", ERR_STORE_ERR, __LINE__,  __FILE__);

    Store.InitMAPIStore(m_pMAPISession, pMdb, (LPWSTR)m_sProfileName.c_str());
    return hr;
}

HRESULT MAPISession::OpenOtherStore(LPMDB pAdminStore, LPCWSTR pExchServerDn, LPCWSTR pUserDn, MAPIStore &OtherStore)
//
// Opens another user's store given the Administrator store 
//
{
    LOGFN_TRACE;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);
    HRESULT hr = E_FAIL;

    // -----------------------------------------------------------------
    // Must have a MAPI session
    // -----------------------------------------------------------------
    if (m_pMAPISession == NULL)
        throw MAPISessionException(hr, L"OpenDefaultStore(): m_mapiSession is NULL.", ERR_STORE_ERR, __LINE__, __FILE__);

    // -----------------------------------------------------------------
    // Add suffix to serverDN
    // -----------------------------------------------------------------
    wstring sExchServerDNWithSuffix = (wstring)pExchServerDn + L"/cn=Microsoft Private MDB";

    // -----------------------------------------------------------------
    // Open the store
    // -----------------------------------------------------------------
    LPMDB pMdb = NULL;
    hr = Zimbra::MAPI::Util::MailboxLogon(m_pMAPISession, pAdminStore, sExchServerDNWithSuffix.c_str(), pUserDn, &pMdb);
    if (FAILED(hr))
        throw MAPISessionException(hr, L"OpenDefaultStore(): MailboxLogon Failed.", ERR_STORE_ERR, __LINE__,  __FILE__);

    // -----------------------------------------------------------------
    // Fill in the passed-in MAPIStore object
    // -----------------------------------------------------------------
    OtherStore.InitMAPIStore(m_pMAPISession, pMdb, (LPWSTR)m_sProfileName.c_str());

    return S_OK;
}

HRESULT MAPISession::OpenAddressBook(LPADRBOOK *ppAddrBook)
{
    LOGFN_TRACE;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);
    HRESULT hr = E_FAIL;

    if (m_pMAPISession)
        hr = m_pMAPISession->OpenAddressBook(NULL, NULL, AB_NO_DIALOG, ppAddrBook);
    return hr;
}

HRESULT MAPISession::OpenEntry(ULONG cbEntryID, LPENTRYID lpEntryID, LPCIID lpInterface, ULONG ulFlags, ULONG FAR *lpulObjType, LPUNKNOWN FAR *lppUnk)
{
    LOGFN_TRACE;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);

    return m_pMAPISession->OpenEntry(cbEntryID, lpEntryID, lpInterface, ulFlags, lpulObjType, lppUnk);
}

HRESULT MAPISession::CompareEntryIDs(SBinary *pBin1, SBinary *pBin2, ULONG &lpulResult)
{
    // LOGFN_TRACE_NO; // Clutters log

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);
    HRESULT hr = S_OK;

    hr = m_pMAPISession->CompareEntryIDs(pBin1->cb, (LPENTRYID)(pBin1->lpb), pBin2->cb, (LPENTRYID)(pBin2->lpb), 0, &lpulResult);
    return hr;
}
