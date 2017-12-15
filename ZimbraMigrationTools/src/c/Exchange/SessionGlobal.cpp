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
#include "common.h"
#include "Exchange.h"

// =================================
// CSessionGlobal statics
// =================================

// Locking
Zimbra::Util::CriticalSection CSessionGlobal::m_csSessionGlobal;

// CSessionGlobal maintains a global MAPI session pointer
MAPISession *CSessionGlobal::m_pGlobalMAPISession = NULL;

// And global store pointer
MAPIStore *CSessionGlobal::m_pGlobalDefaultStore = NULL;

wstring     CSessionGlobal::m_sSrcProfileName           = L"";
wstring     CSessionGlobal::m_sDomainName               = L"";
bool        CSessionGlobal::m_bHasJoinedDomain          = false;
bool        CSessionGlobal::m_bNeedResetOOMRegistry     = false;
MIG_TYPE    CSessionGlobal::m_MigType                   = MIGTYPE_SERVER;
int         CSessionGlobal::m_nInitMAPICount            = 0;


// ==========================================================================================================
// CSessionGlobal
// ==========================================================================================================
void CSessionGlobal::SetOOMRegistry()
// If outlook is one of the mail clients installed on machine,
// make an entry in HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows Messaging Subsystem\MSMapiApps
// corresponding to this application. This will cause the MAPI Stub library to route all
// MAPI calls made by this application to Microsoft Outlook, and not the default mail client.
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);

    // See if outlook is one of the mail clients
    HKEY hKey = NULL;
    HRESULT lRetCode = RegOpenKeyEx(HKEY_LOCAL_MACHINE, L"Software\\Clients\\Mail\\Microsoft Outlook", 0, KEY_READ, &hKey);
    if (ERROR_SUCCESS == lRetCode)
    {
        // Open the registry key HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows Messaging Subsystem\MSMapiApps
        HKEY hKeyMSMapiApps = NULL;
        LONG lRet = 0;
        lRetCode = RegOpenKeyEx(HKEY_LOCAL_MACHINE, _T( "SOFTWARE\\Microsoft\\Windows Messaging Subsystem\\MSMapiApps"), 0, KEY_QUERY_VALUE | KEY_SET_VALUE, &hKeyMSMapiApps); // Requires Run As Administrator
        if (ERROR_SUCCESS == lRetCode)
        {
            TCHAR exepath[MAX_PATH+1];
            CString strAppName = L"ZimbraMigration.exe";
            if (0 != GetModuleFileName(0, exepath, MAX_PATH+1))
            {
                strAppName = exepath;
                int npos = strAppName.ReverseFind('\\');
                strAppName= strAppName.Right(strAppName.GetLength()-npos-1);
            }

            lRet = RegSetValueEx(hKeyMSMapiApps, strAppName, 0, REG_SZ, (LPBYTE)_T("Microsoft Outlook"), 18 * (sizeof (TCHAR)));
            RegCloseKey(hKeyMSMapiApps);
            m_bNeedResetOOMRegistry = true;
        }
        else
        {
            LOG_ERROR(_T("Registry-SOFTWARE\\Microsoft\\Windows Messaging Subsystem\\MSMapiApps) error code: %d. Please ensure you are running as Administrator."), lRet);
        }
        RegCloseKey(hKey);
    }
    else
    {
        LOG_ERROR(_T("Registry-Software\\Clients\\Mail\\Microsoft Outlook) error code: %d"), lRetCode);
    }

    /*************Work around for bug 17687****************/
    hKey = NULL;
    DWORD dwDisposition = 0;
    LONG lRet = RegCreateKeyEx(HKEY_CURRENT_USER, _T("Software\\Microsoft\\Exchange\\Client\\Options"), 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey, &dwDisposition);
    if (hKey)
    {
        // Modify the key only if it hasnt been modified previously
        if (RegQueryValueEx(hKey, _T("OrgPickLogonProfile"), NULL, NULL, NULL, NULL))
        {
            TCHAR szOrgOpt[2] = { 0 };
            DWORD dwSize = DWORD(sizeof (szOrgOpt));
            if (!RegQueryValueEx(hKey, _T("PickLogonProfile"), NULL, NULL, (LPBYTE)szOrgOpt, &dwSize))
                RegSetValueEx(hKey, _T("OrgPickLogonProfile"), 0, REG_SZ, (LPBYTE)szOrgOpt, DWORD(sizeof (szOrgOpt)));

            lRet = RegSetValueEx(hKey, _T("PickLogonProfile"), 0, REG_SZ, (LPBYTE)_T("0"), DWORD(_tcslen(_T("0")) + 1) * sizeof (TCHAR));
        }
        RegCloseKey(hKey);
    }
    else
    {
        LOG_ERROR(_T("Registry-Software\\Microsoft\\Exchange\\Client\\Options error code: %d"), lRet);
    }
    /*****************************************************/
}

void CSessionGlobal::ResetOOMRegistry()
{
    LOGFN_TRACE_NO;

    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);
    if (!m_bNeedResetOOMRegistry)
        return;

    /******************Bug 17687*********************/
    HKEY hKey = NULL;
    DWORD dwDisposition = 0;
    LONG lRet = RegCreateKeyEx(HKEY_CURRENT_USER, _T("Software\\Microsoft\\Exchange\\Client\\Options"), 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hKey, &dwDisposition);
    if (hKey)
    {
        TCHAR szOrgOpt[2] = { 0 };
        DWORD dwSize = DWORD(sizeof (szOrgOpt));
        if (!RegQueryValueEx(hKey, _T("OrgPickLogonProfile"), NULL, NULL, (LPBYTE)szOrgOpt, &dwSize))
        {
            RegSetValueEx(hKey, _T("PickLogonProfile"), 0, REG_SZ, (LPBYTE)szOrgOpt, DWORD(sizeof (szOrgOpt)));
            RegDeleteValue(hKey, _T("OrgPickLogonProfile"));
        }
        RegCloseKey(hKey);
    }
    else
    {
        LOG_ERROR(_T("Registry key cannot be set while UnRegisterReminderHandler for OOM. Error: %d"), lRet);
    }
    /**************************************************/

    HKEY hKeyMSMapiApps = NULL;
    lRet = 0;

    // Open the registry key HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows Messaging Subsystem\MSMapiApps
    lRet = RegOpenKeyEx(HKEY_LOCAL_MACHINE, _T("SOFTWARE\\Microsoft\\Windows Messaging Subsystem\\MSMapiApps"), 0, KEY_READ | KEY_SET_VALUE, &hKeyMSMapiApps);
    // Delete the entry in the key corresponding to the application
    if (ERROR_SUCCESS == lRet)
    {
        TCHAR exepath[MAX_PATH+1];
        CString strAppName = L"ZimbraMigration.exe";
        if(0 != GetModuleFileName(0, exepath, MAX_PATH+1))
        {
            strAppName = exepath;
            int npos=strAppName.ReverseFind('\\');
            strAppName= strAppName.Right(strAppName.GetLength()-npos-1);
        }

        lRet = RegDeleteValue(hKeyMSMapiApps, strAppName);
        RegCloseKey(hKeyMSMapiApps);
    }
}

LPCWSTR Zimbra::MAPI::CSessionGlobal::InitGlobalSessionAndStore(LPCWSTR pProfileOrServerName, LPCWSTR userAccount, BOOL bIsPublicFoldersMigration )
//
// Called
// - Server migration
//  - 

// - PST migration
//  - At start of migration from InitMapiAccount

// - Public folders migration
//  - 
// Called twice for PF migration - once for Tools and Once for the Account - need to sort this!
//
{
    LPWSTR exceptionmsg = NULL;

    __try
    {
        return _InitGlobalSessionAndStore(pProfileOrServerName, userAccount, bIsPublicFoldersMigration);
    }
    __except(Zimbra::Util::MiniDumpGenerator::GenerateCoreDump(GetExceptionInformation(), exceptionmsg))
    {
        LOG_ERROR(exceptionmsg);
        Zimbra::Util::FreeString(exceptionmsg);
    }
    return NULL;
}

LPCWSTR CSessionGlobal::_InitGlobalSessionAndStore(LPCWSTR pProfileOrServerName, LPCWSTR userAccount, BOOL bIsPublicFoldersMigration)
//
// Primary call is when user clicks the Migrate button, but also from InitExchangeAdmin (not called for GetProfileList!)
//
// userAccount will only be set for server migration (whether single account, multiple accounts, or public folders mig)
// -> use this to discern the migration type
//
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);

    LPWSTR lpwstrStatus = NULL;
    LPWSTR lpwstrRetVal = NULL;

    // =========================================================
    // Init mimepp
    // =========================================================
    mimepp::Initialize();

    #ifndef WIN64
        LOG_TRACE(_T("Adding support for mem-mapped strings"));
        // Init mem-mapped strings control variables
        const size_t lThreshold = 1<<24; // 16MB is the max amount of heap mem a single string will ever consume. Larger than that -> mem mapped
        mimepp::setLargeStringsMaxHeapStringSize(lThreshold);
        mimepp::setLargeStringsBuffSizeBytes(lThreshold/4);
    #endif

    // =========================================================
    // Set migration type control vars
    // =========================================================
    if (wcscmp(userAccount, L"") == 0)
    {
        SetMigrationType(MIGTYPE_USER_PROFILE); // MIGTYPE_USER_PROFILE means this is a USER migration
    }                                           // This will be "upgraded" to MIGTYPE_USER_PST if the user mig is being 
                                                // done by user having specified a PST rather than profile
    else
    {
        SetMigrationType(MIGTYPE_SERVER);       // SERVER_MIG means user has entered details of exchange admin 
                                                // (either via ExchAdmin profile, or direct details entry) to  
                                                // migrate 1 or more users, or public folders
    }

    // =========================================================
    // Get domain name
    // =========================================================
    wstring sDomainName;
    m_bHasJoinedDomain = Zimbra::MAPI::Util::GetDomainName(sDomainName);
    LOG_TRACE(_T("Domain: %s"), m_bHasJoinedDomain?sDomainName.c_str():_T("<unknown>"));
    SetDomainName(sDomainName.c_str());

    try
    {
        // =========================================================
        // If pst file, autocreate MAPI profile wrapper
        // =========================================================
        wstring sProfileOrServerName = pProfileOrServerName;
        std::transform(sProfileOrServerName.begin(), sProfileOrServerName.end(), sProfileOrServerName.begin(), ::toupper);
        if (sProfileOrServerName.find(L".PST") != std::wstring::npos)
        {
            LOG_INFO(_T("PST migration -> need temp profile"));

            LPSTR lpstrProfileOrServerName;
            WtoA((LPWSTR)pProfileOrServerName, lpstrProfileOrServerName);

            SetMigrationType(MIGTYPE_USER_PST);

            // ----------------------------------------------------------
            // Delete any left over profiles from previous migration
            // ----------------------------------------------------------
            Zimbra::MAPI::Util::DeleteAlikeProfiles(Zimbra::MAPI::Util::PSTMIG_PROFILE_PREFIX.c_str());

            // ----------------------------------------------------------
            // Create name for new profile
            // ----------------------------------------------------------
            string strPSTProfileName = Zimbra::MAPI::Util::PSTMIG_PROFILE_PREFIX;
            
            // Add timestamp to make it unique
            char timeStr[9];
            _strtime(timeStr);
            string strTmpProfile(timeStr);
            replace(strTmpProfile.begin(), strTmpProfile.end(), ':', '_');
            strPSTProfileName += strTmpProfile;

            // ----------------------------------------------------------
            // Create the profile
            // ----------------------------------------------------------
            if (!Zimbra::MAPI::Util::CreatePSTProfile((LPSTR)strPSTProfileName.c_str(), lpstrProfileOrServerName))
            {
                SafeDelete(lpstrProfileOrServerName);
                lpwstrStatus = FormatExceptionInfo(E_FAIL, ERR_CREATE_PSTPROFILE, __FILE__, __LINE__);
                LOG_ERROR(lpwstrStatus);
                Zimbra::Util::CopyString(lpwstrRetVal,(LPWSTR)ERR_CREATE_PSTPROFILE);
                goto CLEAN_UP;
            }
            SafeDelete(lpstrProfileOrServerName);

            LPWSTR wstrProfileName;
            AtoW((LPSTR)strPSTProfileName.c_str(), wstrProfileName);
            m_sSrcProfileName = wstrProfileName;
            SafeDelete(wstrProfileName);

            // Set registry for PST migration
            SetOOMRegistry();
        }
        else 
        if (bIsPublicFoldersMigration)
        {
            LOG_TRACE(_T("Public folders migration"));
            m_sSrcProfileName = pProfileOrServerName;
        }
        else
        {
            LOG_TRACE(_T("Profile migration"));
            m_sSrcProfileName = pProfileOrServerName;
        }
        
        LOG_TRACE(_T("Migration Type: %d"), GetMigrationType());


        // =========================================================
        // Log on to MAPI
        // =========================================================
        m_pGlobalMAPISession = new Zimbra::MAPI::MAPISession();
        HRESULT hr = m_pGlobalMAPISession->Logon((LPWSTR)m_sSrcProfileName.c_str());
        if (hr != S_OK)
            goto CLEAN_UP;

        // =========================================================
        // Open source default store
        // =========================================================
        m_pGlobalDefaultStore = new Zimbra::MAPI::MAPIStore(STORE_TYPE_GLOBALDEFAULT);
        hr = m_pGlobalMAPISession->OpenDefaultStore(*m_pGlobalDefaultStore);
        if (hr != S_OK)
            goto CLEAN_UP;
    }
    catch (MAPISessionException &msse)
    {
        lpwstrStatus = FormatExceptionInfo(msse.ErrCode(), (LPWSTR)msse.Description().c_str(), (LPSTR)msse.SrcFile().c_str(), msse.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,msse.ShortDescription().c_str());
    }
    catch (MAPIStoreException &mste)
    {
        lpwstrStatus = FormatExceptionInfo(mste.ErrCode(), (LPWSTR)mste.Description().c_str(), (LPSTR)mste.SrcFile().c_str(), mste.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,mste.ShortDescription().c_str());
    }
    catch (Util::MapiUtilsException &muex)
    {
        lpwstrStatus = FormatExceptionInfo(muex.ErrCode(), (LPWSTR)muex.Description().c_str(), (LPSTR)muex.SrcFile().c_str(), muex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,muex.ShortDescription().c_str());
    }
    
    // Create Temporary dir for temp files
    Zimbra::MAPI::Util::CreateAppTemporaryDirectory();

CLEAN_UP: 
    
    if (lpwstrStatus)
    {
        if (m_pGlobalMAPISession)
        {
            delete m_pGlobalMAPISession;
            m_pGlobalMAPISession = NULL;
        }

        if (m_pGlobalDefaultStore)
        {
            delete m_pGlobalDefaultStore;
            m_pGlobalDefaultStore = NULL;
        }

        Zimbra::Util::FreeString(lpwstrStatus);
    }
    return lpwstrRetVal;
}

void CSessionGlobal::UnInitGlobalSessionAndStore()
{
    LOGFN_TRACE_NO;

    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);

    if (m_pGlobalDefaultStore)
    {
        delete m_pGlobalDefaultStore;
        m_pGlobalDefaultStore = NULL;
    }

    if (m_pGlobalMAPISession)
    {
        // Delete any PST migration profiles
        Zimbra::MAPI::Util::DeleteAlikeProfiles(Zimbra::MAPI::Util::PSTMIG_PROFILE_PREFIX.c_str());

        delete m_pGlobalMAPISession;
        m_pGlobalMAPISession = NULL;
    }

    ResetOOMRegistry();
}

wstring GetCNName(LPCWSTR pstrUserDN)
{
    LOGFN_TRACE_NO;

    wstring strUserDN = pstrUserDN;
    size_t npos = strUserDN.find_last_of(L"/");
    if (npos != string::npos)
    {
        strUserDN = strUserDN.substr(npos+1);
        npos = strUserDN.find_first_of(L"cn=");
        if (npos != string::npos)
            strUserDN = strUserDN.substr(3);
    }
    else
        strUserDN = L"";

    return strUserDN;
}

void CSessionGlobal::InitMAPI()
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);

    Zimbra::Mapi::Memory::SetMemAllocRoutines(NULL, MAPIAllocateBuffer, MAPIAllocateMore, MAPIFreeBuffer);

    m_nInitMAPICount++;

    LOG_INFO(_T("Initializing MAPI %d"), m_nInitMAPICount);
    MAPIINIT_0 MAPIInit;
    MAPIInit.ulFlags = MAPI_NO_COINIT | 0;
    MAPIInit.ulVersion = MAPI_INIT_VERSION;
    HRESULT hr = MAPIInitialize(&MAPIInit);
    if (FAILED(hr))
        throw GenericException(hr, L"InitMAPI(): MAPIInitialize Failed.", ERR_GEN_EXCHANGEADMIN, __LINE__, __FILE__);
}

void CSessionGlobal::UninitMAPI()
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);

    LOG_INFO(_T("Uninitializing MAPI %d"), m_nInitMAPICount);

    m_nInitMAPICount--;
    _ASSERT(m_nInitMAPICount >= 0);

    LOG_INFO(_T("Calling MAPIUninitialize"));
    MAPIUninitialize();
}

Zimbra::MAPI::MAPISession* CSessionGlobal::GetGlobalMAPISession()
{
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);
    _ASSERT(m_pGlobalMAPISession);
    return m_pGlobalMAPISession;
}

Zimbra::MAPI::MAPIStore* CSessionGlobal::GetGlobalDefaultStore()
{
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);
    _ASSERT(m_pGlobalDefaultStore);
    return m_pGlobalDefaultStore;
}

void CSessionGlobal::SetDomainName(LPCWSTR pszDomainName)
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);
    m_sDomainName = pszDomainName;
}

wstring CSessionGlobal::GetDomainName()
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);
    return m_sDomainName;
}

bool CSessionGlobal::HasJoinedDomain()
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);
    LOG_TRACE(_T("%s"), m_bHasJoinedDomain?_T("Yes"):_T("No"));
    return m_bHasJoinedDomain;
}

void CSessionGlobal::SetMigrationType(MIG_TYPE migtype)
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);
    m_MigType = migtype;
}

MIG_TYPE CSessionGlobal::GetMigrationType()
{
    LOGFN_TRACE_NO;
    Zimbra::Util::AutoCriticalSection acs(m_csSessionGlobal);
    return m_MigType;
}
