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
#pragma once
#include "ReminderEventListener.h"
#include "mso.tlh"
#include "msoutl.tlh"

namespace Zimbra
{
namespace MAPI
{
class MAPIStoreException: public GenericException
{
public:
    MAPIStoreException(HRESULT hrErrCode, LPCWSTR lpszDescription);
    MAPIStoreException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile);
    virtual ~MAPIStoreException() {}
};

class MAPIFolder;

typedef enum STORE_TYPE {STORE_TYPE_GLOBALDEFAULT, STORE_TYPE_USER, STORE_TYPE_PF} STORE_TYPE;

// Mapi Store class
class MAPIStore:public Zimbra::Util::ZimObj
{
public:
    MAPIStore(STORE_TYPE StoreType);
    ~MAPIStore();

    void InitMAPIStore(LPMAPISESSION pMapiSession, LPMDB pMdb, LPWSTR lpwstrProfileName);

    HRESULT CompareEntryIDs(SBinary *pBin1, SBinary *pBin2, ULONG &lpulResult);

    HRESULT GetRootFolder(MAPIFolder &rootFolder);

    LPMDB GetInternalMAPIStore() { return m_pInnerStore; }

    SBinaryArray GetSpecialFolderIds() { return m_specialFolderIds; }

    HRESULT OpenEntry(ULONG cbEntryID, LPENTRYID lpEntryID, LPCIID lpInterface, ULONG ulFlags, ULONG FAR *lpulObjType, LPUNKNOWN FAR *lppUnk);

    HRESULT OpenMessageStoreGUID(_In_ LPMAPISESSION lpMAPISession, _In_z_ LPCSTR lpGUID, _Deref_out_opt_ LPMDB* lppMDB);
    
    HRESULT OpenPublicMessageStore(_In_ LPMAPISESSION lpMAPISession, ULONG ulFlags, /* Flags for CreateStoreEntryID*/ _Deref_out_opt_ LPMDB* lppPublicMDB);

    HRESULT HrMailboxLogon( _In_ LPMAPISESSION		lpMAPISession,	// MAPI session handle
                            _In_ LPMDB				lpMDB,			// open message store
                            _In_z_ LPCTSTR			lpszMsgStoreDN,	// desired message store DN
                            _In_opt_z_ LPCTSTR		lpszMailboxDN,	// desired mailbox DN or NULL
                            ULONG					ulFlags,		// desired flags for CreateStoreEntryID
                            _Deref_out_opt_ LPMDB*	lppMailboxMDB);	// ptr to mailbox message store ptr
    
    HRESULT GetPublicFolderTable(LPMAPITABLE *lpMapiTable);

private:
    MAPIStore();

    LPMDB m_pInnerStore;
    LPMAPISESSION m_pMapiSession;

    SBinaryArray m_specialFolderIds;
    Zimbra::Util::CriticalSection cs_store;
    bool StoreSupportsManageStore(_In_ LPMDB lpMDB);

    HRESULT BuildServerDN(_In_z_ LPCTSTR szServerName, _In_z_ LPCTSTR szPost, _Deref_out_z_ LPTSTR* lpszServerDN);

    HRESULT CallOpenMsgStore(_In_ LPMAPISESSION	lpSession, _In_ ULONG_PTR ulUIParam, _In_ LPSBinary	lpEID, ULONG ulFlags, _Deref_out_ LPMDB* lpMDB);

    HRESULT GetPublicFolderTableViaIExchManageStore(_In_ LPMDB lpMDB, _In_z_ LPCTSTR szServerDN,                 ULONG ulFlags,                            _Deref_out_opt_ LPMAPITABLE* lpPFTable);
    //HRESULT GetPublicFolderTableViaIExchManageStore4(_In_ LPMDB lpMDB, _In_z_ LPCTSTR szServerDN, ULONG ulOffset, ULONG ulFlags,                            _Deref_out_opt_ LPMAPITABLE* lpPFTable);
    //HRESULT GetPublicFolderTableViaIExchManageStore5(_In_ LPMDB lpMDB, _In_z_ LPCTSTR szServerDN, ULONG ulOffset, ULONG ulFlags, _In_opt_ LPGUID lpGuidMDB, _Deref_out_opt_ LPMAPITABLE* lpPFTable);

    HRESULT GetServerName(_In_ LPMAPISESSION lpSession, _Deref_out_opt_z_ LPTSTR* szServerName);

    STORE_TYPE m_StoreType;
};
}
}
