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
#include "exchange.h"


// ==============================================================================================================================
// class DllVersion 
// ==============================================================================================================================

Zimbra::Util::CriticalSection Zimbra::Util::MiniDumpGenerator::cs;
HINSTANCE Zimbra::Util::MiniDumpGenerator::m_hDbgHelpDll=NULL;
wstring Zimbra::Util::MiniDumpGenerator::m_wstrDbgHelpDllPath=L"";
Zimbra::Util::MiniDumpWriteDumpPtr_t Zimbra::Util::MiniDumpGenerator::m_MiniDumpWriteDumpPtr=NULL;
bool Zimbra::Util::MiniDumpGenerator::m_bInitializedMDG = false;

Zimbra::Util::DllVersion::DllVersion(HINSTANCE hDbgHelpDll)
{
    if (hDbgHelpDll)
    {
        m_hDbgHelpDll = hDbgHelpDll;
        GetModuleFileName(m_hDbgHelpDll, m_pwszDllDir, MAX_PATH);
    }
}

BOOL Zimbra::Util::DllVersion::GetDllVersion(DWORD &dwMajor, DWORD &dwMinor, DWORD &dwMajorBuildNumber, DWORD &dwMinorBuildNumber)
{
    if (!GetDllVersionFromDll(dwMajor, dwMinor, dwMajorBuildNumber, dwMinorBuildNumber))
        return GetDllVersionFromFileInfo(dwMajor, dwMinor, dwMajorBuildNumber, dwMinorBuildNumber);
    return TRUE;
}

BOOL Zimbra::Util::DllVersion::GetDllVersionFromDll(DWORD &dwMajor, DWORD &dwMinor,
                                                    DWORD &dwMajorBuildNumber, DWORD &dwMinorBuildNumber)
{
    DLLGETVERSIONPROC pDllGetVersion = NULL;
    pDllGetVersion = (DLLGETVERSIONPROC)GetProcAddress(m_hDbgHelpDll, "DllGetVersion");
    if (!pDllGetVersion)
    {
        return FALSE;
    }
    else
    {
        DLLVERSIONINFO dvi;
        ZeroMemory(&dvi, sizeof (dvi));
        dvi.cbSize = sizeof (dvi);

        HRESULT hr = (*pDllGetVersion)(&dvi);
        if (FAILED(hr))
        {
            return FALSE;
        }
        else
        {
            dwMajor = dvi.dwMajorVersion;
            dwMinor = dvi.dwMinorVersion;
            dwMajorBuildNumber = dvi.dwBuildNumber;
            dwMinorBuildNumber = 0;
            return TRUE;
        }
    }
}

BOOL Zimbra::Util::DllVersion::GetDllVersionFromFileInfo(DWORD &dwMajor, DWORD &dwMinor,
                                                         DWORD &dwMajorBuildNumber, DWORD &dwMinorBuildNumber)
{
    DWORD dwVerHnd = 0;
    DWORD dwVerInfoSize = GetFileVersionInfoSize(m_pwszDllDir, &dwVerHnd);

    if (!dwVerInfoSize)
        return FALSE;

    LPVOID lpVffInfo = malloc(dwVerInfoSize + 1);
    if (!GetFileVersionInfo(m_pwszDllDir, dwVerHnd, dwVerInfoSize, lpVffInfo))
    {
        free(lpVffInfo);
        return FALSE;
    }


    WCHAR dllVersion[256];
    wcscpy(dllVersion, L"\\VarFileInfo\\Translation");

    DWORD langD = 0, dwLen = 0;
    LPWSTR lpwszVersion = NULL;
    BOOL bRes = VerQueryValue(lpVffInfo, dllVersion, (LPVOID *)&lpwszVersion, (UINT *)&dwLen);
    if (bRes && (dwLen == 4))
    {
        memcpy(&langD, lpwszVersion, 4);
        wsprintf(dllVersion, L"\\StringFileInfo\\%02X%02X%02X%02X\\FileVersion", (langD & 0xff00) >> 8, langD & 0xff, (langD & 0xff000000) >> 24, (langD & 0xff0000) >> 16);
    }
    else
    {
        wsprintf(dllVersion, L"\\StringFileInfo\\%04X04B0\\FileVersion", GetUserDefaultLangID());
    }
    if (!VerQueryValue(lpVffInfo, dllVersion, (LPVOID *)&lpwszVersion, (UINT *)&dwLen))
    {
        free(lpVffInfo);
        return FALSE;
    }   

    // Now we have a string that looks like this :
    // "MajorVersion.MinorVersion.BuildNumber", so let's parse it
    bRes = ParseVersionString(lpwszVersion, dwMajor, dwMinor, dwMajorBuildNumber, dwMinorBuildNumber);

    free(lpVffInfo);

    return bRes;
}

BOOL Zimbra::Util::DllVersion::ParseVersionString(LPWSTR lpwszVersion, DWORD &dwMajor,
                                                  DWORD &dwMinor, DWORD &dwMajorBuildNumber, DWORD &dwMinorBuildNumber)
{
    LPWSTR pwszMajor = NULL, pwszMinor = NULL, pwszMajorBuildNo = NULL, pwszMinorBuildNo = NULL;

    pwszMajor = wcstok(lpwszVersion, L".");     // Get first token (Major version number)
    pwszMinor = wcstok(NULL, L".");             // Get second token (Minor version number)
    pwszMajorBuildNo = wcstok(NULL, L".");      // Get third token (Major build number)
    pwszMinorBuildNo = wcstok(NULL, L".");      // Get fourth token (Minor build number)
    if (pwszMajor && pwszMinor && pwszMajorBuildNo && pwszMinorBuildNo)
    {
        dwMajor = _wtoi(pwszMajor);
        dwMinor = _wtoi(pwszMinor);
        dwMajorBuildNumber = _wtoi(pwszMajorBuildNo);
        dwMinorBuildNumber = _wtoi(pwszMinorBuildNo);
        return TRUE;
    }
    else
    {
        // It may be comma separated
        pwszMajor = wcstok(lpwszVersion, L","); // Get first token (Major version number)
        pwszMinor = wcstok(NULL, L",");         // Get second token (Minor version number)
        pwszMajorBuildNo = wcstok(NULL, L",");  // Get third token (Major build number)
        pwszMinorBuildNo = wcstok(NULL, L",");  // Get fourth token (Minor build number)
        if (pwszMajor && pwszMinor && pwszMajorBuildNo && pwszMinorBuildNo)
        {
            dwMajor = _wtoi(pwszMajor);
            dwMinor = _wtoi(pwszMinor);
            dwMajorBuildNumber = _wtoi(pwszMajorBuildNo);
            dwMinorBuildNumber = _wtoi(pwszMinorBuildNo);
            return TRUE;
        }
        else
        {
            return FALSE;
        }
    }
}

// ============================================================================================================================
// MiniDumpGenerator
// ============================================================================================================================
bool Zimbra::Util::MiniDumpGenerator::InitMiniDumpGenerator(LPTSTR strDbgHelpDllPath)
{
    LOGFN_TRACE_NO;

    if (m_bInitializedMDG)
        return true;

    m_wstrDbgHelpDllPath= strDbgHelpDllPath;
    m_hDbgHelpDll = LoadLibrary(m_wstrDbgHelpDllPath.c_str());
    if (!m_hDbgHelpDll)
    {
        //error loading dbghelp.dll
        LOG_ERROR(_T("Failed to load DbgHelp from '%s'. Crash dumps will not be generated."), m_wstrDbgHelpDllPath.c_str());
        _ASSERT(FALSE);
        return false;
    }

    DWORD dwMajor = 0, dwMinor = 0, dwMajorBuildNumber = 0, dwMinorBuildNumber = 0;
    Zimbra::Util::DllVersion dllversion(m_hDbgHelpDll);
    if (!dllversion.GetDllVersion(dwMajor, dwMinor, dwMajorBuildNumber, dwMinorBuildNumber))
        return false; //error getting dll version

    if (     (dwMajor < DBGHELP_MAJOR_VERSION) 
        || ((dwMajor == DBGHELP_MAJOR_VERSION) && (dwMinor < DBGHELP_MINOR_VERSION)) 
        || ((dwMajor == DBGHELP_MAJOR_VERSION) && (dwMinor == DBGHELP_MINOR_VERSION) && (dwMajorBuildNumber < DBGHELP_MAJOR_BUILD_NUMBER)) 
        || ((dwMajor == DBGHELP_MAJOR_VERSION) && (dwMinor == DBGHELP_MINOR_VERSION) && (dwMajorBuildNumber == DBGHELP_MAJOR_BUILD_NUMBER) && (dwMinorBuildNumber < DBGHELP_MINOR_BUILD_NUMBER)))
    {
        //dbghelp.dll has older version
        return false;
    }

    m_MiniDumpWriteDumpPtr = (Zimbra::Util::MiniDumpWriteDumpPtr_t)GetProcAddress(m_hDbgHelpDll, "MiniDumpWriteDump");
    if (!m_MiniDumpWriteDumpPtr)
        return false; //Error in getting MiniDumpWriteDump function from dbghelp.dll

    m_bInitializedMDG = true;
    return m_bInitializedMDG;
}

void Zimbra::Util::MiniDumpGenerator::UninitMiniDumpGenerator()
{
    LOGFN_TRACE_NO;

    if (m_hDbgHelpDll)
    {
        FreeLibrary(m_hDbgHelpDll);
        m_hDbgHelpDll = NULL;
    }

    m_bInitializedMDG = false;
}

LONG WINAPI Zimbra::Util::MiniDumpGenerator::GenerateCoreDump(LPEXCEPTION_POINTERS pExPtrs, LPWSTR &wstrOutMessage)
{
    LOGFN_TRACE_NO;

    _ASSERT(FALSE);

    Zimbra::Util::AutoCriticalSection acs(cs);
    if(!m_bInitializedMDG)
    {
        _ASSERT(FALSE);
        Zimbra::Util::CopyString(wstrOutMessage,L"MiniDumpGenerator not initialized.");
        return ZM_MINIDMP_UNINIT;
    }
    
    wstrOutMessage=NULL;
    LONG retVal= ZM_CORE_GENERATION_FAILED;
        
    if (pExPtrs)
    {
        static std::set<PVOID> setOccuredExcepAddrs;
        static std::set<DWORD> setOccuredExcepCodes;

//#ifndef CASE_184490_DIAGNOSTICS
        if (   (setOccuredExcepAddrs.find(pExPtrs->ExceptionRecord->ExceptionAddress) != setOccuredExcepAddrs.end()) 
            && (setOccuredExcepCodes.find(pExPtrs->ExceptionRecord->ExceptionCode)    != setOccuredExcepCodes.end()))
        {
            Zimbra::Util::CopyString(wstrOutMessage,L"Similar core dump already generated. Hence skipping this one.");
            LOG_WARNING(_T("Similar core dump already generated for address %p and code %d. Hence skipping this one."), pExPtrs->ExceptionRecord->ExceptionAddress, pExPtrs->ExceptionRecord->ExceptionCode);
            return ZM_CORE_ALREADY_GENERATED;
        }
//#endif

        if (pExPtrs->ExceptionRecord)
        {
            WCHAR strbuf[128];
            wsprintf(strbuf,L"Exception Address: 0x%x",            
            pExPtrs->ExceptionRecord->ExceptionAddress);
            Zimbra::Util::CopyString(wstrOutMessage,strbuf);
        }

        if (m_MiniDumpWriteDumpPtr)
        {
            MINIDUMP_EXCEPTION_INFORMATION mdExInfo;
            mdExInfo.ThreadId = GetCurrentThreadId();
            mdExInfo.ExceptionPointers = pExPtrs;
            mdExInfo.ClientPointers = TRUE;

            WCHAR pwszTempPath[MAX_PATH];
            GetTempPath(MAX_PATH, pwszTempPath);

            SYSTEMTIME st;
            GetLocalTime(&st);

            WCHAR szAppPath[MAX_PATH] = L"";
            ::GetModuleFileName(0, szAppPath, MAX_PATH);

            std::wstring strAppName;
            strAppName = szAppPath;
            strAppName = strAppName.substr(strAppName.rfind(L"\\") + 1);
            strAppName = strAppName.substr(0,strAppName.find(L"."));
            
            CString strCoreFileName;
            strCoreFileName.Format(L"%04d%02d%02d-%02d%02d%02d.%03d-%d %s %s.dmp", st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond, st.wMilliseconds, ::GetCurrentProcessId(), L"migration", g_logger.GetLogFileSuffix().c_str());

            WCHAR pwszDmpFile[MAX_PATH + 32];
            wcscpy(pwszDmpFile, pwszTempPath);
            wcscat(pwszDmpFile, strAppName.c_str());
            wcscat(pwszDmpFile, L"\\Logs\\");
            wcscat(pwszDmpFile, strCoreFileName.GetString());

            HANDLE hFile = CreateFile(pwszDmpFile, GENERIC_READ | GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
            if (hFile != INVALID_HANDLE_VALUE)
            {
                if (m_MiniDumpWriteDumpPtr(GetCurrentProcess(), GetCurrentProcessId(), hFile, (MINIDUMP_TYPE)(MiniDumpWithDataSegs | MiniDumpWithFullMemory), &mdExInfo, NULL, NULL))
                {
                    WCHAR strbuf[512];
                    wsprintf(strbuf,L"  CORE: Generated core dump: %s", pwszDmpFile);
                    if(wstrOutMessage)
                        Zimbra::Util::AppendString(wstrOutMessage,strbuf);
                    else
                        Zimbra::Util::CopyString(wstrOutMessage,strbuf);
                    setOccuredExcepAddrs.insert(pExPtrs->ExceptionRecord->ExceptionAddress);
                    setOccuredExcepCodes.insert(pExPtrs->ExceptionRecord->ExceptionCode);
                    CloseHandle(hFile);
                    retVal = ZM_CORE_GENERATED;
                }
                else
                {
                    if (wstrOutMessage)
                        Zimbra::Util::AppendString(wstrOutMessage,L"  CORE: Failed to generate core dump.");
                    else
                        Zimbra::Util::CopyString(wstrOutMessage,L"  CORE: Failed to generate core dump.");
                    CloseHandle(hFile);
                    DeleteFile(pwszDmpFile);
                }
            }
        }
    }
    else
    {
        if(wstrOutMessage)
            Zimbra::Util::AppendString(wstrOutMessage,L"  CORE: Failed to generate core dump. Invalid LPEXCEPTION_POINTERS.");
        else
            Zimbra::Util::CopyString(wstrOutMessage,L"  CORE: Failed to generate core dump. Invalid LPEXCEPTION_POINTERS.");
    }	
    return retVal;
}

LPTSTR ZMULongToString(ULONG ul)
{
    LPTSTR pRetVal = new TCHAR[34];
    _ultot(ul, pRetVal, 10);
    return pRetVal;
}


LPTSTR ZMHexEncode(ULONG cb, LPBYTE pb)
{
    LPTSTR pszHexEncoded = new TCHAR[(cb * 2) + 1];
    LPBYTE pSrc = pb;

    LPTSTR pDst = pszHexEncoded;
    for (ULONG i = 0; i < cb; i++, pSrc++, pDst += 2)
        wsprintf(pDst, _T("%02X"), *pSrc);

    *pDst = _T('\0');
    return pszHexEncoded;
}

LPTSTR Zimbra::Util::SBinToStr(SBinary &bin)
{
    if (bin.cb == 0)
    {
        LPTSTR pRetVal = new TCHAR[6];

        _tcscpy(pRetVal, _T("cb:0;"));
        return pRetVal;
    }

    LPTSTR pszCount = ZMULongToString(bin.cb);
    LPTSTR pRetVal = NULL;
    LPTSTR pszHexEncoded = ZMHexEncode(bin.cb, bin.lpb);

    pRetVal = Zimbra::Util::AppendString(pszCount, _T(";"), pszHexEncoded);

    delete[] pszHexEncoded;
    delete[] pszCount;

    return pRetVal;
}
