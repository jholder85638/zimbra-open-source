/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2015, 2016 Synacor, Inc.
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
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Collections;
using System.Xml;
using System.Diagnostics;

namespace CssLib
{



    // ============================================================================================================================
    // C# Logging code (for C++ logging, see CppLib::Logger.cpp)
    // ============================================================================================================================

    // Static object of type "class Log" is a wrapper for the C++ logging functions in CppLib::Logger.cpp
    public class Log
    {
        public enum Level { None, Err, Warn, Summary, Info, Trace, Verbose };
        // Above must match CppLogLevel in logger.h


        public static Level GlobalLogLevel
        {
            get;
            set;
        }

        // ---------------------------------------------------------
        // Initialization
        // ---------------------------------------------------------
        public static void InitializeLogging(Level lvl)
        //
        // Called from MainViewA (for migration wizard), Run() (for console)
        //
        {
            EnableLogging(true);
            CreateLogsDir();
            SetLogLevel(lvl);
        }

        public static void StartNewLogfileForThisThread(string str)
        {
            CppSetThreadName(str);
            CppStartNewLogfileForThisThread(str);
        }

        // Used by C# function tracing to decide whether to instantiate a LogBlock object
        public static bool NotTracing()
        {
            if (GlobalLogLevel >= Level.Trace)
                return false;
            else
                return true;
        }

        public static void SetLogLevel(Level lvl)
        {
            GlobalLogLevel = lvl;
            CppSetLogLevel(lvl);
        }

        // ---------------------------------------------------------
        // Methods that do the logging
        // ---------------------------------------------------------
        public static void log(Level level, object obj)
        {
            string msg = obj != null ? obj.ToString() : "(null)";
            foreach (string line in msg.Split(new string[] { "\r\n", "\n" }, System.StringSplitOptions.None))
            {
                // Call into C++ layer to do the logging
                CppLog(level, false, line);
            }
        }

        // version of the above for prettyprint
        public static void logpretty(Level level, object obj)
        {
            string msg = obj != null ? obj.ToString() : "(null)";
            foreach (string line in msg.Split(new string[] { "\r\n", "\n" }, System.StringSplitOptions.None))
            {
                // Call into C++ layer to do the logging
                CppLog(level, true, line);
            }
        }

        public static void log(Level level, params object[] objs)
        {
            StringBuilder s = new StringBuilder();
            String last = "";

            foreach (object obj in objs)
            {
                if (s.Length > 0 && !last.EndsWith("="))
                    s.Append(' ');

                last = obj != null ? obj.ToString() : "(null)";
                s.Append(last);
            }
            log(level, s);
        }

        public static Log.Level LogLevelStr2Enum(string logLevelStr)
        {
            Log.Level logLevelEnum = Log.Level.Info;
            switch (logLevelStr.ToLower())
            {
                /*case "err": // Not used. ("warn" below encompasses this)
                    logLevelEnum = Log.Level.Err;
                    break;*/
                case "warn":
                    logLevelEnum = Log.Level.Warn;
                    break;
                case "info":
                    logLevelEnum = Log.Level.Info;
                    break;
                case "trace":
                    logLevelEnum = Log.Level.Trace;
                    break;
                case "verbose":
                    logLevelEnum = Log.Level.Verbose;
                    break;
                default:
                    logLevelEnum = Log.Level.Info;
                    break;
            }
            return logLevelEnum;
        }

        // ---------------------------------------------------------
        // Helpers
        // ---------------------------------------------------------
        private static void EnableLogging(bool bEnable)
        {
            CppEnableLogging(bEnable);
        }

        private static void CreateLogsDir()
        {
            string path = Path.GetTempPath() + System.Diagnostics.Process.GetCurrentProcess().ProcessName +"\\Logs\\";
            Directory.CreateDirectory(path);
        }

        // ---------------------------------------------------------
        // Function logging
        // ---------------------------------------------------------
        public static long function_enter(string sFnName) { return CppLogFnEnter(sFnName); }
        public static void function_exit(long lCookie)    { CppLogFnExit(lCookie); }


        // ---------------------------------------------------------
        // Aliases for "log" with implicit level
        // ---------------------------------------------------------
        public static void err(string str/*,
                               [System.Runtime.CompilerServices.CallerMemberName] string memberName = "",
                               [System.Runtime.CompilerServices.CallerFilePath] string sourceFilePath = "",
                               [System.Runtime.CompilerServices.CallerLineNumber] int sourceLineNumber = 0*/) // DCB_BUG_101137 Needs .NET4.5 See https://msdn.microsoft.com/en-us/library/hh534540.aspx              
        {
            // DCB Following doesn't work either: See http://stackoverflow.com/questions/12556767/how-do-i-get-the-current-line-number
            /*
            StackFrame stackFrame = new System.Diagnostics.StackTrace(1).GetFrame(1);
            string fileName = stackFrame.GetFileName();
            string methodName = stackFrame.GetMethod().ToString();
            int lineNumber = stackFrame.GetFileLineNumber();
            */

            // These work but only when PDBs avail :-( So no good for customer sites
            /*
            int lineNumber1 = (new System.Diagnostics.StackFrame(0, true)).GetFileLineNumber();
            string method = stackFrame.GetMethod().ToString();
            */

            log(Level.Err, str); 
        }

        public static void err(params object[] objs)    { log(Level.Err, objs); }

        public static void warn(string str)             { log(Level.Warn, str); }
        public static void warn(params object[] objs)   { log(Level.Warn, objs); }

        public static void summary(string str)             { log(Level.Summary, str); }
        public static void summary(params object[] objs)   { log(Level.Summary, objs); }

        public static void info(string str)             { log(Level.Info, str); }
        public static void info(params object[] objs)   { log(Level.Info, objs); }

        public static void trace(string str)            { log(Level.Trace, str); }
        public static void trace(params object[] objs)  { log(Level.Trace, objs); }

        public static void verbose(string str)           { log(Level.Verbose, str); }
        public static void verbose(params object[] objs) { log(Level.Verbose, objs); }
        public static void verbosepretty(string str)     { logpretty(Level.Verbose, str); }

        public static void dump(string str, string data) { log(Level.Trace, str + "\r\n" + data);}


        public static void mem(string str)             { trace("## Memory Used ("+str+"): " + System.Diagnostics.Process.GetCurrentProcess().WorkingSet64 / (1024 * 1024) + "MB");  }


        // ---------------------------------------------------------
        // PInvokes
        // ---------------------------------------------------------
        #region PInvokes

        [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl, CharSet = CharSet.Unicode)]
        static private extern void CppEnableLogging(bool bEnable);

        [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl, CharSet = CharSet.Unicode)]
        static private extern void CppSetLogLevel(Level level);

        [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl, CharSet = CharSet.Unicode)]
        static private extern void CppLog(Level level, bool bPrettyPrint, string str);

        [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl, CharSet = CharSet.Unicode)]
        static private extern void CppSetThreadName(string str);

        [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl, CharSet = CharSet.Unicode)]
        static private extern void CppStartNewLogfileForThisThread(string str);

        [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl, CharSet = CharSet.Unicode)]
        static private extern long CppLogFnEnter(string sFnName);

        [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl, CharSet = CharSet.Unicode)]
        static private extern void CppLogFnExit(long lCookie);

        #endregion PInvokes
    }




    // ============================================================================================================================
    // LogBlock
    // ============================================================================================================================
    public class LogBlock : IDisposable
    {
        private long m_lCookie = 0;

        public LogBlock(string sFnName)
        {
            // When deeply nested there will be a whole pile of LogBlock objects on the heap (as opposed to on the stack as with ZCO)
            m_lCookie = Log.function_enter(sFnName);

            // DCB_BUG_100394 Make sure sFnName can't be garbage collected until above methods exited (not sure this is necessary,
            // but adding to be sure)
            GC.KeepAlive(sFnName);
        }

        ~LogBlock()
        {
            Dispose(false);
        }



        
        // -------------------------------------------------------------------------------------
        // Destructor control - see http://www.codeproject.com/Articles/7792/Destructors-in-C
        // -------------------------------------------------------------------------------------
        private bool m_bIsDisposed = false;
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected void Dispose(bool bDisposing)
        {
            if (!m_bIsDisposed)
            {
                if (bDisposing)
                {
                    // Cleanup managed resources
                    Log.function_exit(m_lCookie);
                }

                // Cleanup unmanaged resources
                /* none */
            }
            m_bIsDisposed = true;
        }
    }


    /*
    Usage
    -----
     
    using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
    {
       // Function body
    }
    */

    // Had previously used using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
    // but http://stackoverflow.com/questions/1466740/using-getcurrentmethod-in-supposedly-high-performance-code
    // implies GetType is much faster
    // LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
    // LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
}