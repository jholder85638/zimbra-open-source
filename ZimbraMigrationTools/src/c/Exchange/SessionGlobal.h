/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
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
 * ***** END LICENSE BLOCK *****
 */
#pragma once
#include "Util.h"

namespace Zimbra
{
namespace MAPI
{


typedef enum MIG_TYPE {MIGTYPE_SERVER, MIGTYPE_USER_PST, MIGTYPE_USER_PROFILE} MIG_TYPE;


// ====================================================================================
// CSessionGlobal
// ====================================================================================
class CSessionGlobal
{
public:
    // -------------------------------------------------------------------------------
    // Mapi session management
    // -------------------------------------------------------------------------------
    static void InitMAPI();
    static void UninitMAPI();

    // -------------------------------------------------------------------------------
    // Static methods to be used by all mailboxes/profile/PST
    // -------------------------------------------------------------------------------
    // pProfileOrServerName -> Exchange Admin Profile for Exchange mailboxes migration
    // pProfileOrServerName -> Local Exchange profile migration
    // pProfileOrServerName -> PST file path for PST migration
    static LPCWSTR InitGlobalSessionAndStore(LPCWSTR pProfileOrServerName, LPCWSTR userAccount=L"", BOOL bIsPublicFoldersMigration = FALSE);
    static void UnInitGlobalSessionAndStore();

    // Helpers
    static Zimbra::MAPI::MAPISession* GetGlobalMAPISession();
    static Zimbra::MAPI::MAPIStore*   GetGlobalDefaultStore();

    static void SetDomainName(LPCWSTR pszDomainName);
    static wstring GetDomainName();

    static void SetMigrationType(MIG_TYPE migtype);
    static MIG_TYPE GetMigrationType();

    static bool HasJoinedDomain();

private:
    static LPCWSTR _InitGlobalSessionAndStore(LPCWSTR pProfileOrServerName, LPCWSTR userAccount=L"", BOOL bIsPublicFoldersMigration = FALSE);	

    static LONG WINAPI UnhandledExceptionFilter(LPEXCEPTION_POINTERS pExPtrs);

    static void SetOOMRegistry();
    static void ResetOOMRegistry();

private:
    // These are never called because never instantiated - static methods only
    CSessionGlobal(); 
    ~CSessionGlobal();

private:
    static Zimbra::Util::CriticalSection m_csSessionGlobal;

    static MIG_TYPE m_MigType;

    static wstring m_sSrcProfileName;

    static bool m_bHasJoinedDomain;
    static wstring m_sDomainName;

    static Zimbra::MAPI::MAPISession *m_pGlobalMAPISession;
    static Zimbra::MAPI::MAPIStore   *m_pGlobalDefaultStore;

    static bool m_bNeedResetOOMRegistry;

    static int m_nInitMAPICount;
};

}
}
