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
// COMMapiTools.cpp : Implementation of COMMapiTools

#include "common.h"
#include "Exchange.h"
#include "COMMapiTools.h"
#include <ATLComTime.h>

// C5E4267B-AE6C-4E31-956A-06D8094D0CBE
const IID UDTVariable_IID = {0xC5E4267B, 0xAE6C, 0x4E31, {0x95, 0x6A, 0x06, 0xD8, 0x09, 0x4D, 0x0C, 0xBE}};
const IID UDTItem_IID     = {0xC5E4267A, 0xAE6C, 0x4E31, {0x95, 0x6A, 0x06, 0xD8, 0x09, 0x4D, 0x0C, 0xBE}};

std::string format_error(unsigned __int32 hr)
{
    std::stringstream ss;
    ss << "COM call Failed. Error code = 0x" << std::hex << hr << std::endl;
    return ss.str();
}

STDMETHODIMP COMMapiTools::InterfaceSupportsErrorInfo(REFIID riid)
{
    static const IID *const arr[] = {&IID_IMapiTools};

    for (int i = 0; i < sizeof (arr) / sizeof (arr[0]); i++)
    {
        if (InlineIsEqualGUID(*arr[i], riid))
            return S_OK;
    }
    return S_FALSE;
}

STDMETHODIMP COMMapiTools::InitMapiTools(BSTR pProfileOrServerName, BSTR pAdminUser, BSTR pAdminPassword, BSTR *pErrorText)
//
// Only called for Exch Server migration!
//
{
    LOGFN_TRACE_NO;

    if (!m_pExchAdmin)
    {
        InitCrashDumper();
        m_pExchAdmin = new Zimbra::MAPI::ExchangeAdmin();
    }

    LPCWSTR lpszErrorText = m_pExchAdmin->InitExchangeAdmin((LPCWSTR)pProfileOrServerName, (LPCWSTR)pAdminUser, (LPCWSTR)pAdminPassword);
    *pErrorText = (lpszErrorText) ? CComBSTR(lpszErrorText) : CComBSTR("");

    return S_OK;
}

STDMETHODIMP COMMapiTools::UninitMapiTools(BSTR *pErrorText)
{
    LOGFN_TRACE_NO;

    CSessionGlobal::UnInitGlobalSessionAndStore();

    LPCWSTR lpszErrorText = m_pExchAdmin->UninitExchangeAdmin(); // Uninitializes MAPI via ExchangeAdmin dtor
    *pErrorText = (lpszErrorText) ? CComBSTR(lpszErrorText) : CComBSTR("");

    if (m_pExchAdmin)
    {
        delete m_pExchAdmin;
        m_pExchAdmin = NULL;
    }

    Zimbra::Util::MiniDumpGenerator::UninitMiniDumpGenerator();

    return S_OK;
}

STDMETHODIMP COMMapiTools::GetProfileList(VARIANT* Profiles, BSTR* statusmessage)
//
// NB Returns only Exchange profiles
//
{
    LOGFN_TRACE_NO;

    if (statusmessage == NULL)
    {
        _ASSERT(FALSE);
        return E_POINTER;
    }

    HRESULT hr = S_OK;
    CComBSTR status = L"";

    // ============================================================================
    // Test error handling
    // ============================================================================
    #ifdef DEBUG
        // DCB Step onto E_FAIL to test error handling
        bool Fail = false;
        if (Fail)
        {
            hr = E_FAIL;
            status.Append(L" GetProfileList error ");
            status.Append(format_error(hr).c_str());
            LOG_ERROR(_T("%s"), status.m_str);
            *statusmessage = status.Detach();
            return hr;
        }
    #endif

    // ============================================================================
    // Use m_pExchAdmin to get the profile list
    // ============================================================================
    vector<string> vProfileList;
    hr = m_pExchAdmin->GetAllProfiles(vProfileList);
    if (hr != S_OK)
    {
        status.Append(" GetAllProfiles error ");
        status.Append(format_error(hr).c_str());
        LOG_ERROR(_T("%s"), status.m_str);

        *statusmessage = status.Detach();
        return hr;
    }

    if (vProfileList.size() == 0)
    {
        LOG_ERROR(_T("No profiles returned for GetAllProfiles"));
        status = L"No profiles";
        *statusmessage = status.Detach();
        return S_OK;
    }

    // ============================================================================
    // Transfer data to out param
    // ============================================================================
    vector<CComBSTR> tempvectors;
    std::vector<string>::iterator its;
    for (its = (vProfileList.begin()); its != vProfileList.end(); its++)
    {
        string str = (*its).c_str();
        CComBSTR temp = SysAllocString(str_to_wstr(str).c_str());
        tempvectors.push_back(temp);
    }

    // Copy tempvectors into out param
    VariantInit(Profiles);
    Profiles->vt = VT_ARRAY | VT_BSTR;
    SAFEARRAY *psa;
    SAFEARRAYBOUND bounds = { (ULONG)vProfileList.size(), 0 };
    psa = SafeArrayCreate(VT_BSTR, 1, &bounds);
    BSTR *bstrArray;
    SafeArrayAccessData(psa, (void **)&bstrArray);
    std::vector<CComBSTR>::iterator it;
    int i = 0;
    for (it = (tempvectors.begin()); it != tempvectors.end(); it++, i++)
        bstrArray[i] = SysAllocString((*it).m_str);

    SafeArrayUnaccessData(psa);
    Profiles->parray = psa;

    *statusmessage =  status;
    status.Detach();

    return hr;
}

std::wstring COMMapiTools::str_to_wstr(const std::string &str)
{
    std::wstring wstr(str.length() + 1, 0);
    MultiByteToWideChar(CP_ACP, 0, str.c_str(), (int)str.length(), &wstr[0], (int)str.length());
    return wstr;
}

STDMETHODIMP COMMapiTools::AvoidInternalErrors(BSTR lpToCmp, LONG *lRetval)
{
    LOGFN_TRACE_NO;

    BOOL retval = FALSE;
    const size_t len = sizeof(ERR_AVOID_LIST) / sizeof(ERR_AVOID_LIST[0]);
    for (size_t i = 0; i < len; ++i)
    {
        if (wcscmp(lpToCmp, ERR_AVOID_LIST[i])==0)
        {
            retval = TRUE;
            break;
        }
    }    

    *lRetval = (LONG)retval;
    return S_OK;
}

STDMETHODIMP COMMapiTools::SelectExchangeUsers(VARIANT *Users, BSTR *pErrorText)
{
    LOGFN_TRACE_NO;

    vector<ObjectPickerData> vUserList;
    LPCWSTR lpszErrorText = m_pExchAdmin->SelectExchangeUsers(vUserList);
    if (lpszErrorText != NULL)
    {
        LOG_ERROR(_T("SelectExchangeUsers failed '%s'"), lpszErrorText);
        return S_FALSE;
    }

    if (vUserList.size() == 0)
    {
        LOG_ERROR(_T("SelectExchangeUsers returned no users"));
        return S_OK;
    }

    wstring str = L"", strDisplayName = L"", strGivenName = L"", strSN = L"", strZFP = L"";
    vector<CComBSTR> tempvectors;
    std::vector<ObjectPickerData>::iterator its;
    for (its = (vUserList.begin()); its != vUserList.end(); its++)
    {
        ObjectPickerData obj = (*its);

        // FBS bug 71646 -- 3/26/12
        str = (*its).wstrUsername;
        for (size_t j = 0; j < (*its).pAttributeList.size(); j++)
        {
            if (its->pAttributeList[j].first == L"displayName")
                strDisplayName = its->pAttributeList[j].second;

            if (its->pAttributeList[j].first == L"givenName")
                strGivenName = its->pAttributeList[j].second;

            if (its->pAttributeList[j].first == L"sn")
                strSN = its->pAttributeList[j].second;

            if (its->pAttributeList[j].first == L"zimbraForeignPrincipal")
                strZFP = its->pAttributeList[j].second;

        }

        str += L"~";
        str += strDisplayName;
        str += L"~";
        str += strGivenName;
        str += L"~";
        str += strSN;
        str += L"~";
        str += strZFP;
        //
        CComBSTR temp = SysAllocString(str.c_str());
        tempvectors.push_back(temp);
    }

    VariantInit(Users);
    Users->vt = VT_ARRAY | VT_BSTR;

    SAFEARRAYBOUND bounds = { (ULONG)vUserList.size(), 0 };
    SAFEARRAY *psa;
    psa = SafeArrayCreate(VT_BSTR, 1, &bounds);

    BSTR *bstrArray;
    SafeArrayAccessData(psa, (void **)&bstrArray);

    std::vector<CComBSTR>::iterator it;
    int i = 0;
    for (it = (tempvectors.begin()); it != tempvectors.end(); it++, i++)
        bstrArray[i] = SysAllocString((*it).m_str);

    SafeArrayUnaccessData(psa);

    Users->parray = psa;
    *pErrorText = (lpszErrorText) ? CComBSTR(lpszErrorText) : CComBSTR("");

    return S_OK;
}

void COMMapiTools::InitCrashDumper()
{
    LOGFN_TRACE_NO;

    wstring appdir = Zimbra::Util::GetAppDir();
    LPWSTR pwszTempPath = new WCHAR[MAX_PATH];
    wcscpy(pwszTempPath, appdir.c_str());
    Zimbra::Util::AppendString(pwszTempPath,L"dbghelp.dll");

    Zimbra::Util::MiniDumpGenerator::InitMiniDumpGenerator(pwszTempPath);

    SetUnhandledExceptionFilter(UnhandledExceptionFilter);
    delete []pwszTempPath;
}
