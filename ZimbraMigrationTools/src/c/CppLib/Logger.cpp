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
#include <stdarg.h>
#include <time.h>
#include "..\..\..\..\ZimbraMAPI\src\UTIL\ZimbraLogging.h"
#include "Logger.h"

/*

-------------------------------------------------------------------------------------------
             OLD                                             NEW
-------------------------------------------------------------------------------------------
C# layer            C++ layer                   C# layer                C++ layer
-------------------------------------------------------------------------------------------

log.err/warn        dlog.err/warn               LOG_ERROR/WARNING       LOG_ERROR/WARNING           Always logged -> impossible to turn logging off completely
Log.info            dlog.info                   Log.info                LOG_GEN                     NOTE: FNs turned OFF for this, so need to kill the indent
Log.trace           dlog.trace                  Log.trace               LOG_GEN+LOGFN               Enabled by editing XML file! Trace means FUNCTION TRACING. TRACE also enables SOAP logging (to sep file)                    
Log.debug           n/a                         inside _DEBUG           inside _DEBUG               Enabled by ticking VERBOSE. But its not really verbose at all - just enables a few log.debug lines

*/


extern "C" 
{
    CPPLIB_DLLAPI void CppLog(CppLogLevel level, bool bPrettyPrint, const wchar_t *str)
    {
        // enum CppLogLevel { CppNone, CppErr, CppWarn, CppInfo, CppTrace, CppVerbose };
        if (level == CppVerbose)
        {
            if (!bPrettyPrint)
                LOG_VERBOSE(L"%s", str);
            else
                LOG_HTTP(L"%s", str);
        }
        else
        if (level == CppTrace)
            LOG_TRACE(L"%s", str);
        else
        if (level == CppSummary)
            LOG_GEN_SUMMARY(L"%s", str);
        else
        if (level == CppInfo)
            LOG_INFO(L"%s", str);
        else
        if (level == CppWarn)
            LOG_WARNING(L"%s", str);
        else
        if (level == CppErr)
            LOG_ERROR(L"%s", str);    // Want to log the line the error ocurred on
        else
            LOG_INFO(L"%s", str);
    }

    CPPLIB_DLLAPI void CppEnableLogging(bool bEnable)
    {
        g_logger.EnableLogging(bEnable);
    }

    CPPLIB_DLLAPI void CppSetLogLevel(CppLogLevel level)
    {
        // Convert CppLogLevel to ZCOLogging levels
        // enum CppLogLevel { CppNone, CppErr, CppWarn, CppInfo, CppDebug, CppTrace };

        DWORD dwPermittedFnLogLevels = 0;
        DWORD dwPermittedCmtLogLevels = 0;

        if (level >= CppErr)
            dwPermittedCmtLogLevels |= LOGLEVEL_CMNT_ERROR;

        if (level >= CppWarn)
            dwPermittedCmtLogLevels |= LOGLEVEL_CMNT_WARNING;

        if (level >= CppInfo)
            dwPermittedCmtLogLevels |= LOGLEVEL_CMNT_GEN;

        if (level >= CppTrace)
        {
            dwPermittedFnLogLevels |= (LOGLEVEL_FN_API|LOGLEVEL_FN_INTERNAL);
            dwPermittedCmtLogLevels |= LOGLEVEL_CMNT_DETAILED;
        }

        if (level >= CppVerbose)
        {
            dwPermittedFnLogLevels |= LOGLEVEL_FN_DETAILED;
            dwPermittedCmtLogLevels |= (LOGLEVEL_CMNT_EXTREME|LOGLEVEL_CMNT_HTTP);
        }

        g_logger.SetLevel(dwPermittedFnLogLevels, dwPermittedCmtLogLevels, 0xFFFFFFFF /*All areas*/);
    }

    CPPLIB_DLLAPI void CppSetThreadName(const wchar_t *pszName)
    {
        g_logger.SetThreadName(THREADNAME_MIGRATE, TRUE, TRUE, TRUE, pszName);
    }

    CPPLIB_DLLAPI void CppStartNewLogfileForThisThread(const wchar_t *pszSuffix)
    {
        g_logger.StartNewLogfileForThisThread(pszSuffix);
    }

    CPPLIB_DLLAPI long CppLogFnEnter(const wchar_t* sFnName)
    {
        CLogBlock* pLogBlock = new CLogBlock(LOGAREA_GENERAL, LOGLEVEL_FN_INTERNAL, sFnName, _T(""));
        return (long)pLogBlock;
    }

    CPPLIB_DLLAPI void CppLogFnExit(long lCookie)
    {
        CLogBlock* pLogBlock = (CLogBlock*)lCookie;
        delete pLogBlock;
    }

}
