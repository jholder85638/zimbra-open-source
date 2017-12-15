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

#ifdef CPPLIB_EXPORTS
#define CPPLIB_DLLAPI   __declspec(dllexport)
#else
#define CPPLIB_DLLAPI   __declspec(dllimport)
#endif

// Aliases for ZCOLogging LOG_* macros

#define LOGFN_TRACE      LOGFN_INTERNAL
#define LOGFN_TRACE_NO   LOGFN_INTERNAL_NO

#define LOGFN_VERBOSE    LOGFN_DETAILED
#define LOGFN_VERBOSE_NO LOGFN_DETAILED_NO

#define LOG_INFO         LOG_GEN
#define LOG_TRACE        LOG_DETAILED
#define LOG_VERBOSE      LOG_EXTREME


enum CppLogLevel { CppNone, CppErr, CppWarn, CppSummary, CppInfo, CppTrace, CppVerbose};
// Above must match class Log.Level in Logging.cs
