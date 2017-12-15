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

namespace Zimbra
{
namespace MAPI
{
class MAPISessionException: public GenericException
{
public:
    MAPISessionException(HRESULT hrErrCode, LPCWSTR lpszDescription);
    MAPISessionException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile);
    virtual ~MAPISessionException() {}
};

// Item type consts
#define ZCM_NONE                0x00
#define ZCM_MAIL                0x01
#define ZCM_CONTACTS            0x02
#define ZCM_APPOINTMENTS        0x04
#define ZCM_TASKS               0x08
#define ZCM_MEETRQRS            0x10
#define ZCM_ALL                 0xFF
class MAPIStore;

// MAPI session class
class MAPISession:public Zimbra::Util::ZimObj
{
public:
    MAPISession();
    ~MAPISession();

    HRESULT Logon(LPWSTR strProfile);
    HRESULT Logon(bool bDefaultProfile = true);

    LPMAPISESSION GetMAPISessionObject() { return m_pMAPISession; }

    HRESULT OpenDefaultStore(MAPIStore &Store);
    HRESULT OpenOtherStore(LPMDB pAdminStore, LPCWSTR pServerDn, LPCWSTR pUserDn, MAPIStore &OtherStore);
    HRESULT OpenPublicStore(MAPIStore &Store);
    HRESULT OpenAddressBook(LPADRBOOK *ppAddrBook);
    HRESULT OpenEntry(ULONG cbEntryID, LPENTRYID lpEntryID, LPCIID lpInterface, ULONG ulFlags, ULONG FAR *lpulObjType, LPUNKNOWN FAR *lppUnk);
    HRESULT CompareEntryIDs(SBinary *pBin1, SBinary *pBin2, ULONG &lpulResult);

private:
    HRESULT _mapiLogon(LPWSTR strProfile, DWORD dwFlags, LPMAPISESSION &session);

private:
    Zimbra::Util::CriticalSection cs;

    IMAPISession *m_pMAPISession;
    wstring m_sProfileName;
};
}
}
