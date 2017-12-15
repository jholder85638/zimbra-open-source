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
#include "Util.h"

namespace Zimbra
{
namespace MAPI
{
class ExchangeAdminException: public GenericException
{
public:
    ExchangeAdminException(HRESULT hrErrCode, LPCWSTR lpszDescription);
    ExchangeAdminException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile);
    virtual ~ExchangeAdminException() {}
};


// ====================================================================================================
// class ExchangeAdmin
// ====================================================================================================
class ExchangeAdmin
{
public:
    ExchangeAdmin();
    ~ExchangeAdmin();

    LPCWSTR InitExchangeAdmin(LPCWSTR lpProfileOrServerName, LPCWSTR lpAdminUsername = NULL, LPCWSTR lpAdminPassword = NULL);
    LPCWSTR UninitExchangeAdmin();

    void SetExchServerName(const wstring& sServerName){m_strExchServer = sServerName;};

    HRESULT GetAllProfiles(vector<string> &vProfileList);
    LPCWSTR SelectExchangeUsers(vector<ObjectPickerData> &vUserList);

    HRESULT CreateProfile(wstring strProfileName, wstring strMailboxName, wstring strPassword);
    HRESULT DeleteProfile(wstring strProfile);

    HRESULT CreateExchangeMailBox(LPCWSTR lpwstrNewUser, LPCWSTR lpwstrNewUserPwd, LPCWSTR lpwstrExchAdminName, LPCWSTR lpwstrExchAdminPW);
    HRESULT DeleteExchangeMailBox(LPCWSTR lpwstrMailBox,                           LPCWSTR lpwstrExchAdminName, LPCWSTR lpwstrExchAdminPW);

    HRESULT SetDefaultProfile(wstring strProfile);

private:
    HRESULT CreateMailboxAndProfile();
    HRESULT UnCreateMailboxAndProfile();
    LPCWSTR _InitExchangeAdmin(LPCWSTR lpProfileOrServerName, LPCWSTR lpAdminUsername = NULL, LPCWSTR lpAdminPassword = NULL);

private:
    bool m_bCreatedExchMailboxAndProfile;

    LPPROFADMIN m_pProfAdmin;

    wstring m_strExchServer;

    wstring m_ExchangeAdminName;
    wstring m_ExchangeAdminPwd;
};

const LPCWSTR TEMP_ADMIN_PROFILE_NAME = L"zmprof";
const LPCWSTR TEMP_ADMIN_MAILBOX_NAME = L"zmmbox";
}
}
