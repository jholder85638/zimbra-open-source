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

ExchangeAdminException::ExchangeAdminException(HRESULT hrErrCode, LPCWSTR lpszDescription): 
    GenericException(hrErrCode, lpszDescription)
{
    //
}

ExchangeAdminException::ExchangeAdminException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, 
                                               int nLine, LPCSTR strFile): 
    GenericException(hrErrCode, lpszDescription, lpszShortDescription, nLine, strFile)
{
    //
}

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// Exchange Admin class
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
ExchangeAdmin::ExchangeAdmin():
    m_bCreatedExchMailboxAndProfile(false)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;
    m_pProfAdmin = NULL;

    try
    {
        CSessionGlobal::InitMAPI();

        if (FAILED(hr = MAPIAdminProfiles(0, &m_pProfAdmin)))
            throw ExchangeAdminException(hr, L"Init(): MAPIAdminProfiles Failed.",  ERR_GEN_EXCHANGEADMIN, __LINE__, __FILE__);
    }
    catch (ExchangeAdminException &exc)
    {
        LOG_ERROR(_T("ExchangeAdmin::ExchangeAdmin exception: %s"), exc.Description().c_str());
    }
}

ExchangeAdmin::~ExchangeAdmin()
{
    LOGFN_TRACE_NO;

    m_pProfAdmin->Release();

    CSessionGlobal::UninitMAPI();
}

HRESULT ExchangeAdmin::CreateProfile(wstring strProfileName, wstring strMailboxName, wstring strPassword)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;
    Zimbra::Util::ScopedBuffer<char> strServer;
    Zimbra::Util::ScopedBuffer<char> strMBName;
    Zimbra::Util::ScopedBuffer<char> strProfName;
    Zimbra::Util::ScopedBuffer<char> strProfPwd;
    Zimbra::Util::ScopedInterface<IMsgServiceAdmin> pSvcAdmin;
    Zimbra::Util::ScopedInterface<IMAPITable> pMsgSvcTable;
    Zimbra::Util::ScopedRowSet pSvcRows;
    SPropValue rgval[2] = { 0 };
    SPropValue sProps = { 0 };
    SRestriction sres;
    WCHAR errDescrption[256] = {};

    // Columns to get from HrQueryAllRows.
    enum { iSvcName, iSvcUID, cptaSvc };

    SizedSPropTagArray(cptaSvc, sptCols) = { cptaSvc, PR_SERVICE_NAME, PR_SERVICE_UID };
    WtoA((LPWSTR)strProfileName.c_str(), strProfName.getref());
    WtoA((LPWSTR)strPassword.c_str(), strProfPwd.getref());

    // create new profile
    if (FAILED(hr = m_pProfAdmin->CreateProfile((LPTSTR)strProfName.get(), (LPTSTR)strProfPwd.get(), NULL, 0)))
    {
        throw ExchangeAdminException(hr, L"CreateProfile(): CreateProfile Failed.", ERR_CREATE_EXCHPROFILE, __LINE__, __FILE__);
    }

    // Get an IMsgServiceAdmin interface off of the IProfAdmin interface.
    if (FAILED(hr = m_pProfAdmin->AdminServices((LPTSTR)strProfName.get(), (LPTSTR)strProfPwd.get(), NULL, 0, pSvcAdmin.getptr())))
    {
        wcscpy(errDescrption, L"CreateProfile(): AdminServices Failed.");
        goto CRT_PROFILE_EXIT;
    }

    // Create the new message service for Exchange.
    if (FAILED(hr = pSvcAdmin->CreateMsgService((LPTSTR)"MSEMS", (LPTSTR)"MSEMS", NULL, NULL)))
    {
        wcscpy(errDescrption, L"CreateProfile(): CreateMsgService Failed.");
        goto CRT_PROFILE_EXIT;
    }

    // Need to obtain the entry id for the new service. This can be done by getting the message service table
    // and getting the entry that corresponds to the new service.
    if (FAILED(hr = pSvcAdmin->GetMsgServiceTable(0, pMsgSvcTable.getptr())))
    {
        wcscpy(errDescrption, L"CreateProfile(): GetMsgServiceTable Failed.");
        goto CRT_PROFILE_EXIT;
    }


    sres.rt = RES_CONTENT;
    sres.res.resContent.ulFuzzyLevel = FL_FULLSTRING;
    sres.res.resContent.ulPropTag = PR_SERVICE_NAME;
    sres.res.resContent.lpProp = &sProps;

    sProps.ulPropTag = PR_SERVICE_NAME;
    sProps.Value.lpszA = "MSEMS";

    // Query the table to obtain the entry for the newly created message service.
    if (FAILED(hr = HrQueryAllRows(pMsgSvcTable.get(), (LPSPropTagArray) & sptCols, NULL, NULL, 0, pSvcRows.getptr())))
    {
        wcscpy(errDescrption, L"CreateProfile(): HrQueryAllRows Failed.");
        goto CRT_PROFILE_EXIT;
    }

    // Set up a SPropValue array for the properties that you have to configure.
    if (pSvcRows->cRows > 0)
    {
        // First, the exchange server name.
        ZeroMemory(&rgval[0], sizeof (SPropValue));
        rgval[0].ulPropTag = PR_PROFILE_UNRESOLVED_SERVER;
        WtoA((LPWSTR)m_strExchServer.c_str(), strServer.getref());
        rgval[0].Value.lpszA = (LPSTR)strServer.get();

        // Next, the user's AD name.
        ZeroMemory(&rgval[1], sizeof (SPropValue));
        rgval[1].ulPropTag = PR_PROFILE_UNRESOLVED_NAME;
        WtoA((LPWSTR)strMailboxName.c_str(), strMBName.getref());
        rgval[1].Value.lpszA = (LPSTR)strMBName.get();

        // Configure the message service by using the previous properties.
        // int trials = 10;
        int trials = 2;
        int itrTrials = 0;

        hr = 0x81002746;                        // WSAECONNRESET
        while ((hr == 0x81002746) && (itrTrials < trials))
        {
            hr = pSvcAdmin->ConfigureMsgService((LPMAPIUID)pSvcRows->aRow->lpProps[iSvcUID].Value.bin.lpb, NULL, 0, 2, rgval);
            //if (hr == 0x81002746)
                // Sleep(30000);
                //Sleep(10000);
            itrTrials++;
        }
        if (FAILED(hr))
        {
            /* =
             * pSvcAdmin->ConfigureMsgService((LPMAPIUID)pSvcRows->aRow->lpProps[iSvcUID].
             *   Value.bin.lpb,NULL, 0, 2, rgval)))*/
            wcscpy(errDescrption, L"CreateProfile(): ConfigureMsgService Failed.");
            goto CRT_PROFILE_EXIT;
        }
    }

CRT_PROFILE_EXIT: 
    if (hr != S_OK)
    {
        DeleteProfile(strProfileName);
        throw ExchangeAdminException(hr, errDescrption, ERR_CREATE_EXCHPROFILE, __LINE__, __FILE__);
    }
    else
    {
        //Create supporting OL profile entries else crash may happen!
        if (!Zimbra::MAPI::Util::SetOLProfileRegistryEntries(strProfileName.c_str()))
            throw ExchangeAdminException(hr, L"ExchangeAdmin::CreateProfile()::SetOLProfileRegistryEntries Failed.", ERR_CREATE_EXCHPROFILE, __LINE__, __FILE__);

        /*		
        Zimbra::Util::ScopedBuffer<char> strProfName;
        WtoA((LPWSTR)strProfileName.c_str(), strProfName.getref());
        hr=m_pProfAdmin->SetDefaultProfile((LPTSTR)strProfName.get(),NULL);
        */
    }
    return hr;
}

HRESULT ExchangeAdmin::DeleteProfile(wstring strProfile)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;
    Zimbra::Util::ScopedBuffer<char> strProfName;
    WtoA((LPWSTR)strProfile.c_str(), strProfName.getref());
    if (FAILED(hr = m_pProfAdmin->DeleteProfile((LPTSTR)strProfName.get(), 0)) && (hr != MAPI_E_NOT_FOUND))
        throw ExchangeAdminException(hr, L"DeleteProfile(): DeleteProfile Failed.", ERR_DELETE_PROFILE, __LINE__, __FILE__);

    return hr;
}

HRESULT ExchangeAdmin::GetAllProfiles(vector<string> &vProfileList)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;

    // ================================================
    // get profile table
    // ================================================
    Zimbra::Util::ScopedInterface<IMAPITable> pProftable;
    if ((hr = m_pProfAdmin->GetProfileTable(0, pProftable.getptr())) == S_OK)
    {
        // get all profile rows
        SizedSPropTagArray(3, proftablecols) = {3, { PR_DISPLAY_NAME_A, PR_DEFAULT_PROFILE, PR_SERVICE_NAME }};
        Zimbra::Util::ScopedRowSet profrows;
        if ((hr = HrQueryAllRows(pProftable.get(), (SPropTagArray *)&proftablecols, NULL, NULL, 0, profrows.getptr())) == S_OK)
        {
            for (unsigned int i = 0; i < profrows->cRows; i++)
            {
                if (profrows->aRow[i].lpProps[0].ulPropTag == PR_DISPLAY_NAME_A)
                {
                    Zimbra::Util::ScopedInterface<IMsgServiceAdmin> spServiceAdmin;
                    Zimbra::Util::ScopedInterface<IMAPITable> spServiceTable;
                    string strpname = profrows->aRow[i].lpProps[0].Value.lpszA;

                    // get profile's admin service
                    hr = m_pProfAdmin->AdminServices((LPTSTR)strpname.c_str(), NULL, NULL, 0, spServiceAdmin.getptr());
                    if (FAILED(hr))
                        throw ExchangeAdminException(hr,L"GetAllProfiles(): AdminServices Failed.", ERR_GETALL_PROFILE, __LINE__, __FILE__);

                    // get message service table
                    hr = spServiceAdmin->GetMsgServiceTable(0, spServiceTable.getptr());
                    if (FAILED(hr))
                        throw ExchangeAdminException(hr,L"GetAllProfiles(): GetMsgServiceTable Failed.", ERR_GETALL_PROFILE, __LINE__, __FILE__);

                    // lets get the service name and the service uid for the primary service
                    SizedSPropTagArray(2, tags) = {2, { PR_SERVICE_NAME, PR_SERVICE_UID }};
                    spServiceTable->SetColumns((LPSPropTagArray) & tags, 0);

                    DWORD dwCount = 0;
                    hr = spServiceTable->GetRowCount(0, &dwCount);
                    if (FAILED(hr))
                        throw ExchangeAdminException(hr, L"GetAllProfiles(): GetRowCount Failed.", ERR_GETALL_PROFILE, __LINE__, __FILE__);
                    else 
                    if (!dwCount)
                        return hr;

                    Zimbra::Util::ScopedRowSet pRows;
                    hr = spServiceTable->QueryRows(dwCount, 0, pRows.getptr());
                    if (FAILED(hr))
                        throw ExchangeAdminException(hr, L"GetAllProfiles(): QueryRows Failed.", ERR_GETALL_PROFILE, __LINE__, __FILE__);

                    for (ULONG j = 0; j < pRows->cRows; j++)
                    {
                        if (PR_SERVICE_NAME == pRows->aRow[j].lpProps[0].ulPropTag)
                        {
                            // if MSExchange service
                            if (0 == lstrcmpiW(pRows->aRow[j].lpProps[0].Value.LPSZ, L"MSEMS"))
                            {
                                if (profrows->aRow[i].lpProps[0].ulPropTag == PR_DISPLAY_NAME_A)
                                    vProfileList.push_back(profrows->aRow[i].lpProps[0].Value.lpszA);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    return hr;
}

HRESULT ExchangeAdmin::SetDefaultProfile(wstring strProfile)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;
    if ((hr = m_pProfAdmin->SetDefaultProfile((LPTSTR)strProfile.c_str(), 0)) != S_OK)
        throw ExchangeAdminException(hr, L"SetDefaultProfile(): SetDefaultProfile Failed.", ERR_SET_DEFPROFILE, __LINE__, __FILE__);

    return hr;
}

void ThrowSetInfoException(HRESULT hr, LPWSTR wstrmsg)
{
    LOG_ERROR(_T("SetInfoException : %s"), wstrmsg);
    throw ExchangeAdminException(hr,wstrmsg, ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
}

BOOL PutBinaryIntoVariant(CComVariant * ovData, BYTE * pBuf,unsigned long cBufLen)
{
    LOGFN_TRACE_NO;

    BOOL fRetVal = FALSE;
    
    VARIANT var;
    VariantInit(&var);
    var.vt = VT_ARRAY | VT_UI1; //Set the type to an array of unsigned chars (OLE SAFEARRAY)

    //Set up the bounds structure
    SAFEARRAYBOUND rgsabound[1];
    rgsabound[0].cElements = cBufLen;
    rgsabound[0].lLbound = 0;

    //Create an OLE SAFEARRAY
    var.parray = SafeArrayCreate(VT_UI1,1,rgsabound);
    if (var.parray != NULL)
    {
        void * pArrayData = NULL;
        SafeArrayAccessData(var.parray,&pArrayData);
        memcpy(pArrayData, pBuf, cBufLen);
        SafeArrayUnaccessData(var.parray);
        *ovData = var;
        VariantClear(&var);
        fRetVal = TRUE;
    }
    return fRetVal;
}

HRESULT ExchangeAdmin::CreateExchangeMailBox(LPCWSTR lpwstrNewUser, LPCWSTR lpwstrNewUserPwd, LPCWSTR lpwstrExchAdminName, LPCWSTR lpwstrExchAdminPW)
//
// Called when user does SERVER migration WITHOUT exchange admin profile
// Creates a new exch mailbox for user lpwstrNewUser. WHY?
//
{
    LOGFN_TRACE_NO;

    // ===============================================================================
    // Get UserDN, LegacyDN and HomeSvrName from admin's AD container
    // ===============================================================================
    wstring sAdminUserDN; wstring sLegacyName; wstring msExchHomeSvrName;
    Zimbra::MAPI::Util::LookupUserInActiveDirectory(m_strExchServer.c_str(), lpwstrExchAdminName, lpwstrExchAdminPW, 
                                                    &sAdminUserDN, &sLegacyName, &msExchHomeSvrName); //m_strExchServer should really be AD Server? so this assumes AD is on same host as exch?

    // Now that we have sAdminUserDN, we can home in diretcly on Admin container

    // ===============================================================================
    // Get mail, homeMDB and homeMTA attributes from admin's AD container
    // ===============================================================================

    // ------------------------------------------------
    // Open container
    // -----------------------------------------------
    wstring strAdminContainer = (wstring)(L"LDAP://") + sAdminUserDN.c_str();
    LOG_INFO(_T("Opening Admin Container: '%s'"), strAdminContainer.c_str());

    // First try without UN/PW
    Zimbra::Util::ScopedInterface<IDirectoryObject> pAdminContainer;
    HRESULT hr = ADsOpenObject(strAdminContainer.c_str(), NULL, NULL, ADS_SECURE_AUTHENTICATION, IID_IDirectoryObject, (void **)pAdminContainer.getptr());
    if (FAILED(hr))
    {
        if (hr == 0x8007052e) // credentials are not valid
        {
            // Try again with UN/PW
            hr = ADsOpenObject((LPTSTR)strAdminContainer.c_str(), lpwstrExchAdminName, lpwstrExchAdminPW, ADS_SECURE_AUTHENTICATION, IID_IDirectoryObject, (void **)pAdminContainer.getptr());
            if (FAILED(hr)||(pAdminContainer.get()==NULL))
                throw ExchangeAdminException(hr,L"CreateExchangeMailBox(): ADsOpenObject Failed.", ERR_ADOBJECT_OPEN, __LINE__, __FILE__);
        }
        else
            throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): ADsOpenObject Failed.", ERR_ADOBJECT_OPEN, __LINE__, __FILE__);
    }

    // ------------------------------------------------
    // Grab attributes
    // -----------------------------------------------
    ADS_ATTR_INFO *pAttrInfo = NULL;
    DWORD dwReturn;
    LPWSTR pAttrNames[] = { L"mail", L"homeMDB", L"homeMTA" };
    DWORD dwNumAttr = sizeof (pAttrNames) / sizeof (LPWSTR);
    if (FAILED(hr = pAdminContainer->GetObjectAttributes(pAttrNames, dwNumAttr, &pAttrInfo, &dwReturn)))
        throw ExchangeAdminException(hr,L"CreateExchangeMailBox(): GetObjectAttributes Failed.",  ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    wstring sAdminHomeMDB; wstring sAdminHomeMTA; wstring sAdminMail;
    for (DWORD idx = 0; idx < dwReturn; idx++)
    {
        if (_wcsicmp(pAttrInfo[idx].pszAttrName, L"mail") == 0)
        {
            sAdminMail = pAttrInfo[idx].pADsValues->Email.Address;
            LOG_TRACE(_T("mail: %s"), sAdminMail.c_str());
        }
        else 
        if (_wcsicmp(pAttrInfo[idx].pszAttrName, L"homeMTA") == 0)
        {
            sAdminHomeMTA = pAttrInfo[idx].pADsValues->DNString;
            LOG_TRACE(_T("HomeMTA: %s"), sAdminHomeMTA.c_str());
        }
        else 
        if (_wcsicmp(pAttrInfo[idx].pszAttrName, L"homeMDB") == 0)
        {
            sAdminHomeMDB = pAttrInfo[idx].pADsValues->DNString;
            LOG_TRACE(_T("HomeMDB: %s"), sAdminHomeMDB.c_str());
        }
    }

    // Use FreeADsMem for all memory obtained from the ADSI call.
    FreeADsMem(pAttrInfo);

    // --------------------------------------------------------------------
    // Extract serverDN from AdminUserDN
    // --------------------------------------------------------------------
    size_t nPos = sAdminUserDN.find(_T("DC="), 0);
    wstring sServerDN = sAdminUserDN.substr(nPos);
    LOG_TRACE(L"ServerDN:      '%s'", sServerDN.c_str());

    // -----------------------------------------
    // Extract Admin user name from AdminUserDN
    // -----------------------------------------
    wstring sAdminUserName(sAdminUserDN);
    size_t snPos = 0; size_t enPos = 0;
    if ((snPos = sAdminUserName.find(L"CN=")) != wstring::npos)
    {
        if ((enPos = sAdminUserName.find(L",", snPos)) != wstring::npos)
        {
            sAdminUserName = sAdminUserName.substr(snPos + 3, (enPos - (snPos + 3)));
            LOG_TRACE(L"AdminUserName: '%s'", sAdminUserName.c_str());
        }
    }

    // =============================================================================================================================
    // Get 'CN=USERS' container
    // =============================================================================================================================
    wstring sPathUsers = _T("LDAP://CN=Users,") + sServerDN;
    LOG_INFO(_T("UsersPath:     '%s'"), sPathUsers.c_str()); // Typically 'LDAP://CN=Users,DC=zmexch,DC=eng,DC=vmware,DC=com'

    Zimbra::Util::ScopedInterface<IDirectoryObject> pDirContainer;
    if (FAILED(hr = ADsOpenObject(sPathUsers.c_str(), NULL, NULL, ADS_SECURE_AUTHENTICATION, IID_IDirectoryObject, (void **)pDirContainer.getptr())))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): ADsOpenObject Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    // ====================================================================================
    // Create new AD object for new user 'zmmbox'
    // ====================================================================================

    // ------------------------------------------------------------------------------------------------
    // Create initial attrs to pass into CreateDSObject()
    // ------------------------------------------------------------------------------------------------
    ADSVALUE cnValue; ADSVALUE classValue; ADSVALUE sAMValue; ADSVALUE uPNValue; ADSVALUE controlValue;
    ADS_ATTR_INFO attrInfo[] = 
    {
        { L"objectClass",        ADS_ATTR_UPDATE, ADSTYPE_CASE_IGNORE_STRING, &classValue,   1 },
        { L"cn",                 ADS_ATTR_UPDATE, ADSTYPE_CASE_IGNORE_STRING, &cnValue,      1 },
        { L"sAMAccountName",     ADS_ATTR_UPDATE, ADSTYPE_CASE_IGNORE_STRING, &sAMValue,     1 },
        { L"userPrincipalName",  ADS_ATTR_UPDATE, ADSTYPE_CASE_IGNORE_STRING, &uPNValue,     1 },
        { L"userAccountControl", ADS_ATTR_UPDATE, ADSTYPE_INTEGER,            &controlValue, 1 },
    };
    DWORD dwAttrs = sizeof (attrInfo) / sizeof (ADS_ATTR_INFO);

    // classValue
    classValue.dwType = ADSTYPE_CASE_IGNORE_STRING;
    classValue.CaseIgnoreString = L"user";

    // controlValue
    #define UF_ACCOUNTDISABLE       0x0002
    #define UF_PASSWD_NOTREQD       0x0020
    #define UF_PASSWD_CANT_CHANGE   0x0040 
    #define UF_NORMAL_ACCOUNT       0x0200
    #define UF_DONT_EXPIRE_PASSWD   0x10000
    #define UF_PASSWORD_EXPIRED     0x800000
    controlValue.dwType = ADSTYPE_INTEGER;
    controlValue.Integer = UF_NORMAL_ACCOUNT | UF_PASSWD_NOTREQD | UF_DONT_EXPIRE_PASSWD;

    // cnValue
    cnValue.dwType = ADSTYPE_CASE_IGNORE_STRING;
    cnValue.CaseIgnoreString = (LPWSTR)lpwstrNewUser;

    // sAMValue
    sAMValue.dwType = ADSTYPE_CASE_IGNORE_STRING;
    sAMValue.CaseIgnoreString = (LPWSTR)lpwstrNewUser;

    // uPNValue
    wstring wstrMail;
    size_t nPosMail = sAdminMail.find(_T("@"), 0);
    wstrMail = sAdminMail.substr(nPosMail);
    wstrMail = lpwstrNewUser + wstrMail;
    LPWSTR upnval = (LPWSTR)wstrMail.c_str();
    uPNValue.dwType = ADSTYPE_CASE_IGNORE_STRING;
    uPNValue.CaseIgnoreString = upnval;


    // --------------------------------------
    // Create the object
    // --------------------------------------
    wstring wstrUserCN = wstring(L"CN=") + wstring(lpwstrNewUser);
    LOG_INFO(_T("CreateDSObject: %s"), wstrUserCN.c_str());
    Zimbra::Util::ScopedInterface<IDispatch> pDisp;
    if (FAILED(hr = pDirContainer->CreateDSObject((LPWSTR)wstrUserCN.c_str(), attrInfo, dwAttrs, pDisp.getptr())))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): CreateDSObject Failed.", ERR_CREATE_EXCHMBX,__LINE__, __FILE__);


    // =============================================================================================
    // Set some additional attrs. For that we need IADsUser
    // =============================================================================================
    Zimbra::Util::ScopedInterface<IADsUser> pIADNewUser;
    if (FAILED(hr = pDisp->QueryInterface(IID_IADsUser, (void **)pIADNewUser.getptr())))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): QueryInterface Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    // -----------------------------------------------
    // set sAMAccountName
    // -----------------------------------------------
    CComVariant varProp;
    varProp.Clear();
    varProp = lpwstrNewUser;
    if (FAILED(hr = pIADNewUser->Put(CComBSTR(L"sAMAccountName"), varProp)))
        throw ExchangeAdminException(hr,L"CreateExchangeMailBox(): Put(sAMAccountName) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if (FAILED(hr = pIADNewUser->SetInfo()))
        throw ExchangeAdminException(hr,L"CreateExchangeMailBox(): Put(sAMAccountName) Failed.",  ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    // -----------------------------------------------
    // set userAccountControl
    // -----------------------------------------------
    varProp.Clear();
    hr = pIADNewUser->Get(CComBSTR(L"userAccountControl"), &varProp);
    varProp = varProp.lVal & ~(ADS_UF_ACCOUNTDISABLE);
    if (FAILED(hr = pIADNewUser->Put(CComBSTR(L"userAccountControl"), varProp)))
        throw ExchangeAdminException(hr,L"CreateExchangeMailBox(): Put(userAccountControl) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - Put(userAccountControl) Failed.");

    // -----------------------------------------------
    // set Account enabled
    // -----------------------------------------------
    if (FAILED(hr = pIADNewUser->put_AccountDisabled(VARIANT_FALSE)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): put_AccountDisabled Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - put_AccountDisabled Failed.");

    // -----------------------------------------------
    // set password
    // -----------------------------------------------
    if (FAILED(hr = pIADNewUser->SetPassword(CComBSTR(lpwstrNewUserPwd))))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): SetPassword Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - SetPassword Failed.");

    // -----------------------------------------------
    // user account password does not expire
    // -----------------------------------------------
    varProp.Clear();
    VARIANT var;
    VariantInit(&var);
    if (!FAILED(hr = pIADNewUser->Get(CComBSTR(L"userAccountControl"), &var)))
    {
        V_I4(&var) |= ADS_UF_DONT_EXPIRE_PASSWD;
        if (FAILED(hr = pIADNewUser->Put(CComBSTR(L"userAccountControl"), var)))
            throw ExchangeAdminException(hr,L"CreateExchangeMailBox(): Put(userAccountControl) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    }
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - userAccountControl Failed.");
    varProp.Clear();

    // -----------------------------------------------
    // set the homeMDB
    // -----------------------------------------------
    if (!sAdminHomeMDB.empty())
    {
        varProp = sAdminHomeMDB.c_str();
        if (FAILED(hr = pIADNewUser->Put(CComBSTR("homeMDB"), varProp)))
            throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put(homeMDB) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    }
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - Put(homeMDB) Failed.");

    // -----------------------------------------------
    // set the homeMTA
    // -----------------------------------------------
    varProp.Clear();
    if (!sAdminHomeMTA.empty())
    {
        varProp = sAdminHomeMTA.c_str();
        if (FAILED(hr = pIADNewUser->Put(CComBSTR("homeMTA"), varProp)))
            throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put(homeMTA) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    }
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - Put(homeMTA) Failed.");

    // -----------------------------------------------
    // set the msExchHomeServerName
    // -----------------------------------------------
    varProp.Clear();
    if (!msExchHomeSvrName.empty())
    {
        varProp = msExchHomeSvrName.c_str();
        if (FAILED(hr = pIADNewUser->Put(CComBSTR("msExchHomeServerName"), varProp)))
            throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put(msExchHomeServerName) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    }
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - Put(msExchHomeServerName) Failed.");

    // -----------------------------------------------
    // set the legacyExchangeDN
    // -----------------------------------------------
    varProp.Clear();
    varProp.Clear();
    wstring newUsrLegacyName = sLegacyName;
    size_t nwpos = newUsrLegacyName.rfind(L"cn=");
    if(nwpos != wstring::npos)
    {
        newUsrLegacyName = newUsrLegacyName.substr(0,nwpos);
        newUsrLegacyName += L"cn=";
        newUsrLegacyName += lpwstrNewUser;
    }
    if (!newUsrLegacyName.empty())
    {
        varProp = newUsrLegacyName.c_str();
        if (FAILED(hr = pIADNewUser->Put(CComBSTR("legacyExchangeDN"), varProp)))
            throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put(legacyExchangeDN) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    }
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - Put(legacyExchangeDN) Failed.");

    // -----------------------------------------------
    // set nickname
    // -----------------------------------------------
    varProp.Clear();
    varProp = lpwstrNewUser;
    if (FAILED(hr = pIADNewUser->Put(CComBSTR("mailNickname"), varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put(mailNickname) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - Put(mailNickname) Failed.");

    // -----------------------------------------------
    // set the displayName
    // -----------------------------------------------
    varProp.Clear();
    varProp = lpwstrNewUser;
    if (FAILED(hr = pIADNewUser->Put(CComBSTR("displayName"), varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put(displayName) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - Put(displayName) Failed.");

    // -----------------------------------------------
    // set the mail atrribute
    // -----------------------------------------------
    varProp.Clear();
    varProp = wstrMail.c_str();
    if (FAILED(hr = pIADNewUser->Put(CComBSTR("mail"), varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put(mail) Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - Put(mail) Failed.");

    // -----------------------------------------------
    // set email
    // -----------------------------------------------
    if (FAILED(hr = pIADNewUser->put_EmailAddress(CComBSTR(wstrMail.c_str()))))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): put_EmailAddress Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - put_EmailAddress Failed.");

    // -----------------------------------------------
    // set proxyAddresses
    // -----------------------------------------------
    varProp.Clear();
    wstrMail=L"SMTP:"+wstrMail;
    varProp = wstrMail.c_str();
    if (FAILED(hr = pIADNewUser->Put(CComBSTR("proxyAddresses"),varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): proxyAddressess Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - proxyAddressess Failed.");

    // ================================================================================
    // Add it to Domain Admins group
    // ================================================================================
    BSTR bstrADSPath;
    if (FAILED(hr = pIADNewUser->get_ADsPath(&bstrADSPath)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): get_ADsPath Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    Zimbra::Util::ScopedInterface<IADsGroup> pGroup;

    // Try in CN=Users container
    wstring wstrGroup = _T("LDAP://CN=Domain Admins,CN=Users,") + sServerDN; 
    LOG_INFO(_T("DomainAdmin Group Path: %s"), wstrGroup.c_str());
    if (FAILED(hr = ADsGetObject(wstrGroup.c_str(), IID_IADsGroup, (void **)pGroup.getptr())))
    {
        // Try in CN=BuiltIn container
        wstrGroup = _T("LDAP://CN=Domain Admins,CN=Builtin,") + sServerDN; 
        LOG_INFO(_T("Failed CN=Users container %08X. Trying with DomainAdmin Group Path: %s"), hr, wstrGroup.c_str());

        if (FAILED(hr = ADsGetObject(wstrGroup.c_str(), IID_IADsGroup, (void **)pGroup.getptr())))
        {
            LOG_INFO(_T("Failed with Builtin container too. Exiting."));
            throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): ADsGetObject Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
        }
    }

    if (FAILED(hr = ADsOpenObject(wstrGroup.c_str(), NULL, NULL, ADS_SECURE_AUTHENTICATION, IID_IADsGroup, (void **)pGroup.getptr())))
    //if (FAILED(hr = ADsOpenObject(wstrGroup.c_str(), wstrLoggedUserName.c_str(), lpwstrLogonUsrPwd, ADS_SECURE_AUTHENTICATION, IID_IADsGroup, (void **)pGroup.getptr())))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): ADsOpenObject Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    if (SUCCEEDED(hr = pGroup->Add(bstrADSPath)))
    {
        if (FAILED(hr = pGroup->SetInfo()))
            throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): pGroup SetInfo Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    }
    else
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): pGroup Add Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);


    // -----------------------------------------------
    // Set msExchMailboxGuid
    // -----------------------------------------------
    GUID guid;
    if(FAILED(hr = CoCreateGuid(&guid)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): CoCreateGuid Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    BYTE *str;
    hr = UuidToString((UUID *)&guid, (RPC_WSTR *)&str);
    if (hr != RPC_S_OK)
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): UuidToString Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    varProp.Clear();
    //BYTE bytArr[]="3429bb3084703348b8023e94fabf16ea";
    PutBinaryIntoVariant(&varProp,str,16);
    RpcStringFree((RPC_WSTR *)&str);
    if (FAILED(hr = pIADNewUser->Put(CComBSTR("msExchMailboxGuid"), varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put msExchMailboxGuid Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
            ThrowSetInfoException(hr, L"SetInfo - msExchMailboxGuid Failed.");

    Zimbra::Util::ScopedInterface<IADsUser> pIAdUser;
    if (FAILED(hr = ADsOpenObject(strAdminContainer.c_str(), NULL, NULL, ADS_SECURE_AUTHENTICATION, IID_IDirectoryObject, (void **)pIAdUser.getptr())))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): ADsOpenObject2 Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    Zimbra::Util::ScopedInterface<IADs> pIAds;
    if (FAILED(hr = pIAdUser->QueryInterface(IID_IADs, (void**) pIAds.getptr())))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): pIAdUser->QueryInterface Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);

    // -----------------------------------------------
    // Set msExchMailboxSecurityDescriptor
    // -----------------------------------------------
    varProp.Clear();
    if( FAILED(hr= pIAds->Get(CComBSTR("msExchMailboxSecurityDescriptor"),&varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Get msExchMailboxSecurityDescriptor Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if (FAILED(hr = pIADNewUser->Put(CComBSTR("msExchMailboxSecurityDescriptor"), varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put msExchMailboxSecurityDescriptor Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
            ThrowSetInfoException(hr, L"SetInfo - msExchMailboxSecurityDescriptor Failed.");

    // -----------------------------------------------
    // Set msExchPoliciesIncluded
    // -----------------------------------------------
    varProp.Clear();
    if( FAILED(hr=pIAds->Get(CComBSTR("msExchPoliciesIncluded"),&varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Get msExchPoliciesIncluded Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);        
    if (FAILED(hr = pIADNewUser->Put(CComBSTR("msExchPoliciesIncluded"), varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put msExchPoliciesIncluded Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);     
    if(FAILED(hr = pIADNewUser->SetInfo()))
            ThrowSetInfoException(hr, L"SetInfo - msExchPoliciesIncluded Failed.");

    // -----------------------------------------------
    // Set msExchUserAccountControl
    // -----------------------------------------------
    varProp.Clear();
    if( FAILED(hr= pIAds->Get(CComBSTR("msExchUserAccountControl"),&varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Get msExchUserAccountControl Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);        
    if (FAILED(hr = pIADNewUser->Put(CComBSTR("msExchUserAccountControl"), varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put msExchUserAccountControl Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);    
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - msExchUserAccountControl Failed.");

    // -----------------------------------------------
    // Set showInAddressBook
    // -----------------------------------------------
    varProp.Clear();
    if(FAILED(hr = pIAds->GetEx(CComBSTR("showInAddressBook"), &varProp )))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Get showInAddressBook Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);		
    if(FAILED(hr = pIADNewUser->Put(CComBSTR("showInAddressBook"), varProp)))
        throw ExchangeAdminException(hr, L"CreateExchangeMailBox(): Put showInAddressBook Failed.", ERR_CREATE_EXCHMBX, __LINE__, __FILE__);
    if(FAILED(hr = pIADNewUser->SetInfo()))
        ThrowSetInfoException(hr, L"SetInfo - showInAddressBook Failed.");

    return hr;
}

HRESULT ExchangeAdmin::DeleteExchangeMailBox(LPCWSTR lpwstrMailBox, LPCWSTR lpwstrExchAdminName, LPCWSTR lpwstrExchAdminPW)
{
    LOGFN_TRACE_NO;
    HRESULT hr = S_OK;

    // =============================================================================
    // Need path to AD server
    // =============================================================================
    // To get this:
    // - grab the Admin users's DN
    // - from that calc the ServerDN
    // - from that, build the AD path

    // -----------------------------------------------------------
    // Get UserDN
    // -----------------------------------------------------------
    wstring sAdminUserDN;
    try
    {
        Zimbra::MAPI::Util::LookupUserInActiveDirectory(m_strExchServer.c_str(), lpwstrExchAdminName, lpwstrExchAdminPW, &sAdminUserDN, NULL, NULL);
    }
    catch (Zimbra::MAPI::ExchangeAdminException &ex)
    {
        LOG_ERROR(_T("LookupUserInActiveDirectory(ExchangeAdminException) exception: %s"), ex.Description().c_str());
        throw;
    }
    catch (Zimbra::MAPI::Util::MapiUtilsException &ex)
    {
        LOG_ERROR(_T("LookupUserInActiveDirectory(MapiUtilsException) exception: %s"), ex.Description().c_str());
        throw;
    }
    LOG_TRACE(_T("AdminUserDN: '%s'"), sAdminUserDN.c_str()); // e.g. CN=Administrator,CN=Users,DC=zmexch,DC=eng,DC=vmware,DC=com

    // -----------------------------------------------------------
    // Extract ServerDN
    // -----------------------------------------------------------
    size_t nPos = sAdminUserDN.find(_T("DC="), 0);
    wstring sServerDN = sAdminUserDN.substr(nPos);				// strip CN= RDNs i.e. DC=zmexch,DC=eng,DC=vmware,DC=com
    LOG_TRACE(_T("ServerDN:    '%s'"), sServerDN.c_str());

    // -----------------------------------------------------------
    // Calc ADS path
    // -----------------------------------------------------------
    wstring sADSPath = _T("LDAP://CN=Users,") + sServerDN;
    LOG_TRACE(_T("ADSPath:     '%s'"), sADSPath.c_str());       // e.g. LDAP://CN=Users,DC=zmexch,DC=eng,DC=vmware,DC=com

    // ===========================================================
    // Open ADS object
    // ===========================================================
    Zimbra::Util::ScopedInterface<IDirectoryObject> pDirContainer;
    if (FAILED(hr = ADsOpenObject(sADSPath.c_str(), NULL, NULL, ADS_SECURE_AUTHENTICATION, IID_IDirectoryObject, (void **)pDirContainer.getptr())))
    //if (FAILED(hr = ADsOpenObject(wstrADSPath.c_str(), lpwstrlogonuser, lpwstrLogonUsrPwd, ADS_SECURE_AUTHENTICATION, IID_IDirectoryObject, (void **)pDirContainer.getptr())))
        throw ExchangeAdminException(hr, L"DeleteExchangeMailBox(): ADsOpenObject Failed.", ERR_DELETE_MBOX, __LINE__, __FILE__);

    // ===========================================================
    // Delete mailbox
    // ===========================================================
    wstring sMailboxCN = wstring(L"CN=") + wstring(lpwstrMailBox);
    LPCWSTR pcsMailboxCN = sMailboxCN.c_str();

    LOG_TRACE(_T("Deleting temporary mailbox '%s'"), sMailboxCN.c_str());
    hr = pDirContainer->DeleteDSObject(const_cast<LPWSTR>(pcsMailboxCN));
    if (hr == NO_ERROR)
        LOG_TRACE(_T("Successfully deleted temporary mailbox '%s'"), pcsMailboxCN);
    else
    if (hr == 0x80072030 /*ERROR_DS_NO_SUCH_OBJECT*/)
    {
        LOG_TRACE(_T("Temporary mailbox '%s' not found. Was probably deleted at end of last session."), pcsMailboxCN);
    }
    else
        LOG_TRACE(_T("Failed to delete temporary mailbox '%s' hr:%08X"), pcsMailboxCN, hr);

    return hr;
}

HRESULT ExchangeAdmin::CreateMailboxAndProfile()
{
    LOGFN_TRACE_NO;

    // =====================================================================
    // First delete any existing temp profile
    // =====================================================================
    try
    {
        LOG_INFO(_T("UnCreateMailboxAndProfile..."));
        UnCreateMailboxAndProfile();
        LOG_INFO(_T("UnCreateMailboxAndProfile Done."));
     }
    catch (Zimbra::MAPI::ExchangeAdminException &ex)
    {
        LOG_ERROR(_T("Clean exception: %s"), ex.Description().c_str());
    }
    catch(...)
    {
        LOG_ERROR(_T("Unknown UnCreateMailboxAndProfile exception"));
    }

    // =====================================================================
    // Next, create new temp profile
    // =====================================================================
    try
    {
        LOG_INFO(_T("CreateExchangeMailbox..."));
        CreateExchangeMailBox(TEMP_ADMIN_MAILBOX_NAME, m_ExchangeAdminPwd.c_str(), m_ExchangeAdminName.c_str(), m_ExchangeAdminPwd.c_str());
        LOG_INFO(_T("CreateExchangeMailbox success."));
    }
    catch (Zimbra::MAPI::ExchangeAdminException &ex)
    {
        LOG_ERROR(_T("ExchangeAdminException exception: %s"), ex.Description().c_str());
        throw;
    }
    catch (Zimbra::MAPI::Util::MapiUtilsException &ex)
    {
        LOG_ERROR(_T("MapiUtilsException exception: %s"), ex.Description().c_str());
        throw;
    }
    
    try
    {
        LOG_INFO(_T("CreateProfile..."));
        CreateProfile(TEMP_ADMIN_PROFILE_NAME, TEMP_ADMIN_MAILBOX_NAME, m_ExchangeAdminPwd.c_str());
        LOG_INFO(_T("CreateProfile completed."));
    }
    catch (Zimbra::MAPI::ExchangeAdminException &ex)
    {
        LOG_ERROR(_T("ExchangeAdminException exception: %s"), ex.Description().c_str());
        throw;
    }
    catch(...)
    {
        LOG_ERROR(_T("Unknown exception"));
        throw;
    }

    return S_OK;
}

HRESULT ExchangeAdmin::UnCreateMailboxAndProfile()
{
    LOGFN_TRACE_NO;

    // -------------------------------------------
    // Delete profile
    // -------------------------------------------
    try
    {
        DeleteProfile(TEMP_ADMIN_PROFILE_NAME);
    }
    catch (Zimbra::MAPI::ExchangeAdminException &ex)
    {
        LOG_ERROR(_T("DeleteProfile exception: %s"), ex.Description().c_str());
        throw;
    }

    // -------------------------------------------
    // Delete mailbox
    // -------------------------------------------
    try
    {
        DeleteExchangeMailBox(TEMP_ADMIN_MAILBOX_NAME, m_ExchangeAdminName.c_str(), m_ExchangeAdminPwd.c_str());
    }
    catch (Zimbra::MAPI::ExchangeAdminException &ex)
    {
        LOG_ERROR(_T("DeleteExchangeMailBox exception: %s"), ex.Description().c_str());
        throw;
    }
    catch (Zimbra::MAPI::Util::MapiUtilsException &ex)
    {
        LOG_ERROR(_T("DeleteExchangeMailBox(MAPIUtils) exception: %s"), ex.Description().c_str());
        throw;
    }
    return S_OK;
}

LPCWSTR ExchangeAdmin::InitExchangeAdmin(LPCWSTR lpProfileOrServerName, LPCWSTR lpAdminUsername, LPCWSTR lpAdminPassword)
{
    LPWSTR exceptionmsg = NULL;

    __try
    {
        return _InitExchangeAdmin(lpProfileOrServerName, lpAdminUsername, lpAdminPassword);
    }
    __except(Zimbra::Util::MiniDumpGenerator::GenerateCoreDump(GetExceptionInformation(),exceptionmsg))
    {
        LOG_ERROR(exceptionmsg);
    }
    return exceptionmsg;
}

LPCWSTR ExchangeAdmin::_InitExchangeAdmin(LPCWSTR lpProfileOrServerName, LPCWSTR lpAdminUsername, LPCWSTR lpAdminPassword)
{
    LOGFN_TRACE_NO;

    LPWSTR lpwstrStatus = NULL;
    LPWSTR lpwstrRetVal = NULL;

    // ==================================================
    // Create Exch admin profile if necessary
    // ==================================================

    // if lpAdminUsername is NULL then we assume that Outlook admin profile exists and we should use it
    // else create a Admin mailbox and create corresponding profile on local machine
    if (lstrlen(lpAdminUsername) > 0)
    {
        // Need to create profile
        LOG_TRACE(_T("AdminUserName supplied -> need to create temp exch profile"));

        // -----------------------------------------------------
        // Create ExchangeAdmin
        // -----------------------------------------------------
        if (!m_bCreatedExchMailboxAndProfile)
        {
            m_ExchangeAdminName = lpAdminUsername;
            m_ExchangeAdminPwd = lpAdminPassword;
            SetExchServerName(lpProfileOrServerName);

            // -----------------------------------------------------
            // Create mailbox and profile
            // -----------------------------------------------------
            try
            {
                CreateMailboxAndProfile();
                lpProfileOrServerName = TEMP_ADMIN_PROFILE_NAME;
                LOG_INFO(_T("Exchange mig setup complete."));
                m_bCreatedExchMailboxAndProfile = true;
            }
            catch (Zimbra::MAPI::ExchangeAdminException &ex)
            {
                lpwstrStatus = FormatExceptionInfo(ex.ErrCode(), (LPWSTR)ex.Description().c_str(), (LPSTR)ex.SrcFile().c_str(), ex.SrcLine());
                LOG_ERROR(lpwstrStatus);
                Zimbra::Util::CopyString(lpwstrRetVal, ex.ShortDescription().c_str());
            }
            catch (Zimbra::MAPI::Util::MapiUtilsException &ex)
            {
                lpwstrStatus = FormatExceptionInfo(ex.ErrCode(), (LPWSTR)ex.Description().c_str(), (LPSTR)ex.SrcFile().c_str(), ex.SrcLine());
                LOG_ERROR(lpwstrStatus);
                Zimbra::Util::CopyString(lpwstrRetVal, ex.ShortDescription().c_str());
            }
        }

    }
    else
        LOG_TRACE(_T("AdminUserName not supplied -> using existing exch admin profile"));


    //check for any exception or error
    if(lpwstrStatus)
    {
        Zimbra::Util::FreeString(lpwstrStatus);
        return lpwstrRetVal;
    }

    // ==================================================
    // Create Session and Open admin store with profile
    // ==================================================
    lpwstrStatus = (LPWSTR)CSessionGlobal::InitGlobalSessionAndStore(lpProfileOrServerName, L"EXCH_ADMIN", FALSE);

    return lpwstrStatus;
}

LPCWSTR ExchangeAdmin::UninitExchangeAdmin()
{
    LOGFN_TRACE_NO;

    if (!m_bCreatedExchMailboxAndProfile)
        return NULL;

    LPWSTR lpwstrStatus = NULL;
    LPWSTR lpwstrRetVal = NULL;

    try
    {
        UnCreateMailboxAndProfile();
    }
    catch (Zimbra::MAPI::ExchangeAdminException &ex)
    {
        lpwstrStatus = FormatExceptionInfo(ex.ErrCode(), (LPWSTR)ex.Description().c_str(), (LPSTR)ex.SrcFile().c_str(), ex.SrcLine());
        Zimbra::Util::CopyString(lpwstrRetVal, ex.ShortDescription().c_str());
    }
    catch (Zimbra::MAPI::Util::MapiUtilsException &ex)
    {
        lpwstrStatus = FormatExceptionInfo(ex.ErrCode(), (LPWSTR)ex.Description().c_str(), (LPSTR)ex.SrcFile().c_str(), ex.SrcLine());
        Zimbra::Util::CopyString(lpwstrRetVal, ex.ShortDescription().c_str());
    }

    m_bCreatedExchMailboxAndProfile = false;

    if(lpwstrStatus)
        Zimbra::Util::FreeString(lpwstrStatus);

    return lpwstrRetVal;
}

LPCWSTR ExchangeAdmin::SelectExchangeUsers(vector<ObjectPickerData> &vUserList)
{
    LOGFN_TRACE_NO;

    LPWSTR lpwstrStatus = NULL;
    try
    {
        Zimbra::MAPI::Util::GetExchangeUsersUsingObjectPicker(vUserList);
    }
    catch (Zimbra::MAPI::Util::MapiUtilsException &ex)
    {
        lpwstrStatus = FormatExceptionInfo(ex.ErrCode(), (LPWSTR)ex.Description().c_str(), (LPSTR)ex.SrcFile().c_str(), ex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::FreeString(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrStatus, ex.ShortDescription().c_str());
    }
    return lpwstrStatus;
}
