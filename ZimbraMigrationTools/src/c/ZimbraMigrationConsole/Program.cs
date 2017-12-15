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

using CssLib;
using MVVM;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System;
using System.Runtime.ExceptionServices;
using System.Security;
using System.Diagnostics;
using System.Security.Principal;

namespace ZimbraMigrationConsole
{
    class CommandLineArgs
    {
        public static CommandLineArgs I
        {
            get
            {
                return m_instance;
            }
        }

        public object arg(string argName)
        {
            if (m_args.ContainsKey(argName))
                return m_args[argName];
            else
                return null;
        }

        public string argAsString(string argName)
        {
            if (m_args.ContainsKey(argName))
                return m_args[argName];
            else
                return "";
        }

        public long argAsLong(string argName)
        {
            if (m_args.ContainsKey(argName))
                return Convert.ToInt64(m_args[argName]);
            else
                return 0;
        }

        public int argAsInt(string argName)
        {
            if (m_args.ContainsKey(argName))
                return Convert.ToInt32(m_args[argName]);
            else
                return 0;
        }

        public double argAsDouble(string argName)
        {
            if (m_args.ContainsKey(argName))
                return Convert.ToDouble(m_args[argName]);
            else
                return 0;
        }

        public bool argAsBool(string argName)
        {
            if (m_args.ContainsKey(argName))
                return Convert.ToBoolean(m_args[argName]);
            else
                return false;
        }

        public void parseArgs(string[] args, string defaultArgs)
        {
            m_args = new Dictionary<string, string>();
            parseDefaults(defaultArgs);

            foreach (string arg in args)
            {
                string[] words = arg.Split('=');
                m_args[words[0]] = words[1];
            }
        }

        /*  public void parseArgs(string args, string defaultArgs)
          {
              string[] vargs = args.Split(';');
              m_args = new Dictionary<string, string>();
              parseDefaults(defaultArgs);

              foreach (string arg in vargs)
              {
                  string[] words = arg.Split('=');
                  if(words[1] != null)
                  m_args[words[0]] = words[1];
              }

          }*/

        private void parseDefaults(string defaultArgs)
        {
            if (defaultArgs == "") return;
            string[] args = defaultArgs.Split(';');

            foreach (string arg in args)
            {
                string[] words = arg.Split('=');
                m_args[words[0]] = words[1];
            }
        }

        private Dictionary<string, string> m_args = null;
        static readonly CommandLineArgs m_instance = new CommandLineArgs();
    }


    class Program
    {
        private static bool keepRunning = false;

        private static Account userAccts = new Account();

        public static CountdownEvent countdownEvent;


        /*******************************************************************************/
        #region GLOBAL VARS
        private static readonly Mutex mutex = new Mutex(true, System.Reflection.Assembly.GetExecutingAssembly().GetName().CodeBase);
        private static bool _userRequestExit = false;
        //private static bool _doIStop = false;   
        static HandlerRoutine consoleHandler;
        #endregion

        [System.Runtime.InteropServices.DllImport("Kernel32")]
        public static extern bool SetConsoleCtrlHandler(HandlerRoutine Handler, bool Add);

        // A delegate type to be used as the handler routine for SetConsoleCtrlHandler.      
        public delegate bool HandlerRoutine(CtrlTypes CtrlType);

        // An enumerated type for the control messages sent to the handler routine.      
        public enum CtrlTypes
        {
            CTRL_C_EVENT = 0,
            CTRL_BREAK_EVENT,
            CTRL_CLOSE_EVENT,
            CTRL_LOGOFF_EVENT = 5,
            CTRL_SHUTDOWN_EVENT
        }

        /// <summary>        ///         /// </summary>        /// <param name="ctrlType"></param>        /// <returns></returns>      
        /// 

        private static bool ConsoleCtrlCheck(CtrlTypes ctrlType)
        {
            // Put your own handler here         
            switch (ctrlType)
            {
                case CtrlTypes.CTRL_C_EVENT:
                    _userRequestExit = true;
                    keepRunning = true;
                    Console.WriteLine("CTRL+C received, shutting down");
                    //Account a = new Account();

                    userAccts.RequestStop();
                    Console.WriteLine("User cancelled, shutting down");

                    if (countdownEvent != null)
                    {
                        while ((countdownEvent.CurrentCount > 0))
                        {
                            Console.WriteLine("signaling background workers");
                            countdownEvent.Signal(1);
                            Thread.Sleep(2000);
                        }
                    }

                    Console.WriteLine("User cancelled, aborting down");
                    System.Console.WriteLine("press an key to continue");
                    Console.ReadKey(true);

                    Thread.CurrentThread.Abort();

                    break;

                case CtrlTypes.CTRL_BREAK_EVENT:
                    _userRequestExit = true;
                    Console.WriteLine("CTRL+BREAK received, shutting down");
                    break;

                case CtrlTypes.CTRL_CLOSE_EVENT:
                    _userRequestExit = true;
                    Console.WriteLine("Program being closed, shutting down");
                    break;

                case CtrlTypes.CTRL_LOGOFF_EVENT:
                case CtrlTypes.CTRL_SHUTDOWN_EVENT:
                    _userRequestExit = true;
                    Console.WriteLine("User is logging off!, shutting down");
                    break;
            }
            return true;
        }
        /********************************************************************************************/

        [HandleProcessCorruptedStateExceptions]
        [SecurityCritical]
        static int Main(string[] args)
        {
            // TESTPOINT_MIGRATION_CONSOLE_MAIN_ENTRY_POINT

            if (!mutex.WaitOne(TimeSpan.Zero, true))
            {
                Console.WriteLine("Another instance already running");
                Thread.Sleep(5000);
                return 2;
            }

            //save a reference so it does not get GC'd     
            consoleHandler = new HandlerRoutine(ConsoleCtrlCheck);

            //set our handler here that will trap exit          
            SetConsoleCtrlHandler(consoleHandler, true);

            int rc = 255;
            try
            {
                // Run main loop and convert result to exit code
                rc = Run(args) ? 0 : 1;
            }
            catch (Exception e)
            {
                // An unexpected Exception happened.  Try to handle it without crashing.

                // First, try to write the Exception with stack trace to the log file
                try
                {
                    Log.err("Unhandled Exception: " + e.ToString());
                }
                catch (Exception ex)
                {
                    // If an Exception happens here we ran into an issue with the
                    // log file.  Try to tell the user via the output about it.
                    // The actual exception will be written below.
                    try
                    {
                        Console.WriteLine("Unhandled Logging Exception: " + ex.ToString());
                    }
                    catch
                    {
                        // If both ways of talking to the user failed something
                        // is really wrong so just crash.
                        throw e;
                    }
                }

                // Now try to write the Exception to the output as well.
                try
                {
                    Console.WriteLine("Unhandled Exception: " + e.ToString());
                }
                catch (Exception ex)
                {
                    // Ok, this didn't work (yes, that did happen, cf. bug 87652),
                    // try to tell about the exception via the log file (which did
                    // work else we'd thrown an Exception above)
                    try
                    {
                        Log.err("Unhandled Output Exception: " + ex.ToString());
                    }
                    catch
                    {
                        // This can't really happen.  If it happens anyway, we most
                        // probably logged the exception already so lets just exit.
                    }
                }
                if (Debugger.IsAttached)
                {
                    throw e;
                }

                // Return a "something went seriously wrong" exit code
                return 255;
            }
            // Return a proper exit code.
            return rc;
        }

        public static bool RunningAsAdministrator()
        {
            return (new WindowsPrincipal(WindowsIdentity.GetCurrent())).IsInRole(WindowsBuiltInRole.Administrator);
        }  

        private static bool Run(string[] args)
        {
            // TESTPOINT_MIGRATION_LOGGING_INIT_MIGRATION_LOG_CONSOLE Initializes main log for MigrationConsole

            // Set log level to Trace for initial startup
            Log.InitializeLogging(Log.Level.Trace);

            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock("Run"))
            {
                /*
                // ===============================================================================
                // Log levels test
                // ===============================================================================

                // Info
                Log.SetLogLevel(Log.Level.Info);
                using (LogBlock lbInfo = Log.NotTracing() ? null : new LogBlock("LogTestInfo"))
                {
                    Log.err("Error");
                    Log.warn("Warning");
                    Log.info("Info");
                    Log.trace("Trace");
                    Log.verbose("Verbose");
                }

                // Trace
                Log.SetLogLevel(Log.Level.Trace);
                using (LogBlock lbTrace = Log.NotTracing() ? null : new LogBlock("LogTestTrace"))
                {
                    Log.err("Error");
                    Log.warn("Warning");
                    Log.info("Info");
                    Log.trace("Trace");
                    Log.verbose("Verbose");
                }

                // Verbose
                Log.SetLogLevel(Log.Level.Verbose);
                using (LogBlock lbVerbose = Log.NotTracing() ? null : new LogBlock("LogTestVerbose"))
                {
                    Log.err("Error");
                    Log.warn("Warning");
                    Log.info("Info");
                    Log.trace("Trace");
                    Log.verbose("Verbose");
                }
                */

                /*
                string msg2 = string.Format("Completed {0,5:#} of {1,5:###.0}", 100, 120);
                */

                Log.mem("Run");

                // Opening banner including version
                Console.SetWindowSize(Math.Min(150, Console.LargestWindowWidth), Math.Min(80, Console.LargestWindowHeight));
                string BuildNum = new MVVM.Model.BuildNum().BUILD_NUM;
                System.Console.WriteLine("ZimbraMigrationConsole v" + BuildNum);
                System.Console.WriteLine("");

                //Migration Test = new Migration();

                while (!keepRunning)
                {
                    // =============================================================================
                    // Check command line args
                    // =============================================================================
                    if (args.Count() > 0)
                    {
                        // Show Help
                        if ((args[0].Equals("-Help", StringComparison.CurrentCultureIgnoreCase))
                            || (args[0].Equals("-h", StringComparison.CurrentCultureIgnoreCase)))
                        {
                            string builder = "Usage\n";
                            builder += "\n";
                            builder += "  ZimbraMigrationConsole.exe ConfigxmlFile=C:\\MyConfig.xml Users=C:\\users.csv MaxThreads= ... etc\n";
                            builder += "\n";
                            builder += "\n";
                            builder += "ConfigxmlFile=              Location of the xml file \n";
                            builder += "\n";
                            builder += "Users=                      Location of the csv file \n";
                            builder += "\n";
                            builder += "MaxThreads=                 Maximum number of threads by default it uses 4.\n";
                            builder += "\n";
                            builder += "MaxErrors=                  Maximum no of errors allowed for the each migration \n";
                            builder += "\n";
                            builder += "MaxWarn=                    Maximum no of warnings \n";
                            builder += "\n";
                            builder += "Profile=                    UserProfile to be migrated for user migration \n";
                            builder += "\n";
                            builder += "DataFile=                   PST file for the user to be migrated\n";
                            builder += "\n";
                            builder += "SourceHost=                 The Source server hostname \n";
                            builder += "\n";
                            builder += "SourceAdminID=              The Source AdminID \n";
                            builder += "\n";
                            builder += "ZimbraHost=                 The Zimbra server hostname \n";
                            builder += "\n";
                            builder += "ZimbraPort=                 The Zimbra port \n";
                            builder += "\n";
                            builder += "ZimbraID=                   The Zimbra ID. For server migration it’s the admin id\n";
                            builder += "                            and for user migration it’s the userid on Zimbra\n";
                            builder += "\n";
                            builder += "ZimbraPwd=                  Pwd for Zimbra \n";
                            builder += "\n";
                            builder += "ZimbraDomain=               The Zimbra Domain name \n";
                            builder += "\n";
                            builder += "Mail=                       true|false. True to migrate Mail.         Default false\n";
                            builder += "Calendar=                   true|false. True to migrate Calendar.     Default false\n";
                            builder += "Contacts=                   true|false. True to migrate Contacts.     Default false\n";
                            builder += "Sent=                       true|false. True to migrate Sent.         Default false\n";
                            builder += "DeletedItems=               true|false. True to migrate DeletedItems. Default false\n";
                            builder += "Junk=                       true|false. True to migrate Junk.         Default false\n";
                            builder += "Tasks=                      true|false. True to migrate Tasks.        Default false\n";
                            builder += "Rules=                      true|false. True to migrate Rules.        Default false\n";
                            builder += "OOO=                        true|false. True to migrate OOO.          Default false\n";
                            builder += "\n";
                            builder += "LogLevel=                   Debug|Info|Trace. This option provides various levels of logging \n";
                            builder += "\n";
                            builder += "IsSkipFolders=              true|false. This option provides skipping of folders \n";
                            builder += "\n";
                            builder += "FoldersToSkip=              comma separated folder names to be skipped \n";
                            builder += "\n";
                            builder += "IsOnOrAfter=                true|false. This option provides the date filter to migration \n";
                            builder += "\n";
                            builder += "MigrateOnOrAfter=           Date in the format YYYY-MM-DD. Items from this date and after get migrated \n";
                            builder += "\n";
                            builder += "IsMaxMessageSize=           true|false. This option provides the maxmessagesize filter to migration \n";
                            builder += "\n";
                            builder += "MaxMessageSize=             a numeric value. Items whose size falls into this category after get migrated \n";
                            builder += "\n";
                            builder += "IsSkipPrevMigratedItems=    true|false. To skip previously migrated items \n";
                            builder += "\n";
                            builder += "\n";
                            builder += "For more information see the help file distributed with the exe. \n";

                            System.Console.Write(builder);
                            Log.info(builder);
                            keepRunning = true;
                            Console.ReadKey(true);

                            return true;
                        }

                        try
                        {
                            CommandLineArgs.I.parseArgs(args, "myStringArg=defaultVal;someLong=12");
                            //CommandLineArgs.I.parseArgs(vargs, "myStringArg=defaultVal;someLong=12");
                            //CommandLineArgs.I.parseArgs(args[0] + args[1] +args[2] +args[3], "myStringArg=defaultVal;someLong=12");
                        }
                        catch (Exception e)
                        {
                            System.Console.WriteLine("Incorrect format of CmdLine arguments" + e.Message);
                            Log.err("Incorrect format of CmdLine arguments" + e.Message);
                            keepRunning = true;

                            if ((CommandLineArgs.I.arg("Silent") != null) && (CommandLineArgs.I.argAsBool("Silent") == false))
                                Console.ReadKey(true);
                            else
                            if (CommandLineArgs.I.arg("Silent") == null)
                                Console.ReadKey(true);

                            return false;
                        }

                        // =============================================================================
                        // Read command line args
                        // =============================================================================
                        string ConfigXmlFile    = CommandLineArgs.I.argAsString("ConfigxmlFile");
                        string UserMapFile      = CommandLineArgs.I.argAsString("Users");
                        string MaxWarns         = CommandLineArgs.I.argAsString("MaxWarn");
                        string ProfileOrAdminID = CommandLineArgs.I.argAsString("Profile");
                        string Pstfile          = CommandLineArgs.I.argAsString("DataFile");
                        string ZCSHost          = CommandLineArgs.I.argAsString("ZimbraHost");
                        string ZCSPort          = CommandLineArgs.I.argAsString("ZimbraPort");
                        string ZCSID            = CommandLineArgs.I.argAsString("ZimbraID");
                        string ZCSPwd           = CommandLineArgs.I.argAsString("ZimbraPwd");
                        string ZCSDomain        = CommandLineArgs.I.argAsString("ZimbraDomain");
                        string SourceHost       = CommandLineArgs.I.argAsString("SourceHost");
                        string SourceAdmin      = CommandLineArgs.I.argAsString("SourceAdminID");
                        //bool Mail             = CommandLineArgs.I.argAsBool("Mail");
                        string LoglevelStr      = CommandLineArgs.I.argAsString("LogLevel");
                        string Folderlist       = CommandLineArgs.I.argAsString("FoldersToSkip");
                        string MigrateDate      = CommandLineArgs.I.argAsString("MigrateOnOrAfter");
                        string MaxMessageSize   = CommandLineArgs.I.argAsString("MaxMessageSize");

                        bool Silent = false;
                        if (CommandLineArgs.I.arg("Silent") != null)
                            Silent = CommandLineArgs.I.argAsBool("Silent");
                        else
                            Silent = false;

                        Log.info("Running as administrator?", RunningAsAdministrator()?"Yes":"No");

                        // If we ever decided to block if non running as administrator, do something like this.
                        // But IIRC only server migs need to be run as admin, so this might be too Draconian? (For the GetDomainName() fn)
                        /*if (!RunningAsAdministrator())
                        {
                            System.Console.WriteLine("Not running as administrator. Cannot continue.");
                            if (!Silent)
                                Console.ReadKey(true);
                            return false;
                        }*/

                        // =============================================================================
                        // Read values in from config file
                        // =============================================================================
                        bool Mail               = false;
                        bool Calendar           = false;
                        bool Contacts           = false;
                        bool Sent               = false;
                        bool DeletedItems       = false;
                        bool Junk               = false;
                        bool Tasks              = false;
                        bool Rules              = false;
                        bool OOO                = false;

                        bool UseSSL             = false;
                        int MaxErrors           = 0;
                        int MaxThreads          = 0;
                        string COS              = "";

                        bool Datefilter         = false;
                        bool SkipFolder         = false;
                        bool SkipPreviousMigration = false;
                        bool IsMaxSize          = false;

                        bool ServerMigration    = false;

                        XmlConfig myXmlConfig = new XmlConfig();

                        if ((ConfigXmlFile != "") && (File.Exists(ConfigXmlFile)))
                        {
                            if (UserMapFile != "")
                            {
                                if (File.Exists(UserMapFile))
                                {
                                    // Read in Config xml AND List of users to migrate
                                    myXmlConfig = new XmlConfig(ConfigXmlFile, UserMapFile);
                                    try
                                    {
                                        myXmlConfig.InitializeConfig();
                                        myXmlConfig.GetUserList();
                                    }
                                    catch (Exception e)
                                    {
                                        Log.err("Exception in initializeconfig/Getuserlist \n" + e.Message);
                                        System.Console.WriteLine("Exception in initializeconfig/Getuserlist \n" + e.Message);

                                        if (!Silent)
                                            Console.ReadKey(true);

                                        return false;
                                    }
                                }
                                else
                                {
                                    Log.err("UserMap file not present. Please check the file name or path.");
                                    System.Console.WriteLine("UserMap file not present.please check the file name or path");

                                    if (!Silent)
                                        Console.ReadKey(true);

                                    return false;
                                }
                            }
                            else
                            {
                                // Read in values from ConfigFile (no user list)
                                myXmlConfig = new XmlConfig(ConfigXmlFile, "");
                                try
                                {
                                    myXmlConfig.InitializeConfig();
                                }
                                catch (Exception e)
                                {
                                    Log.err("Exception in InitializeConfig \n" + e.Message);
                                    System.Console.WriteLine("Exception in InitializeConfig \n" + e.Message);

                                    if (!Silent)
                                        Console.ReadKey(true);

                                    return false;
                                }
                            }

                            if (myXmlConfig.UserList.Count > 0)
                            {
                                ServerMigration = true;
                                if (ProfileOrAdminID == "")
                                    ProfileOrAdminID = (myXmlConfig.ConfigObj.SourceServer.AdminID != "") ? myXmlConfig.ConfigObj.SourceServer.AdminID : myXmlConfig.ConfigObj.SourceServer.Profile;

                                if (ZCSID == "")
                                    ZCSID = myXmlConfig.ConfigObj.ZimbraServer.AdminID;

                                if (ZCSPwd == "")
                                    ZCSPwd = myXmlConfig.ConfigObj.ZimbraServer.AdminPwd;

                                if (ZCSID == "")
                                {
                                    if ((myXmlConfig.ConfigObj.SourceServer.Profile != ""))
                                    {
                                        // if (myXmlConfig.ConfigObj.SourceServer.Hostname == "")
                                        {
                                            Log.err("Are you trying Server or User Migration? Please check the arguments.");
                                            System.Console.WriteLine("Are you trying Server or User Migration? Please check the arguments.");

                                            if (!Silent)
                                                Console.ReadKey(true);

                                            return false;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                if (ProfileOrAdminID == "")
                                    ProfileOrAdminID = myXmlConfig.ConfigObj.SourceServer.Profile;

                                if (ZCSID == "")
                                    ZCSID = myXmlConfig.ConfigObj.ZimbraServer.UserAccount;

                                if (ZCSPwd == "")
                                    ZCSPwd = myXmlConfig.ConfigObj.ZimbraServer.UserPassword;

                                if (Pstfile == "")
                                    Pstfile = myXmlConfig.ConfigObj.SourceServer.DataFile;
                            }

                            if ((ZCSHost == "") && (ZCSDomain == ""))
                            {
                                ZCSHost = myXmlConfig.ConfigObj.ZimbraServer.Hostname;
                                ZCSDomain = myXmlConfig.ConfigObj.UserProvision.DestinationDomain;
                            }
                            else
                            {
                                if (ZCSDomain == "")
                                {
                                    Log.err("ZimbraHost and ZimbraDomain go together. To override ZimbraHost, ZimbraDomain has to be overridden as well.\n");
                                    System.Console.WriteLine("ZimbraHost and ZimbraDomain go together. To override ZimbraHost, ZimbraDomain has to be overridden as well.\n");

                                    if (!Silent)
                                    {
                                        System.Console.WriteLine("Press any key to return \n");
                                        Console.ReadKey(true);
                                    }
                                    return false;
                                }

                                if (ZCSHost == "")
                                {
                                    Log.err("ZimbraHost and ZimbraDomain go together. To override ZimbraHost, ZimbraDomain has to be overridden as well.\n");
                                    System.Console.WriteLine("ZimbraHost and ZimbraDomain go together. To override ZimbraHost, ZimbraDomain has to be overridden as well.\n");

                                    if (!Silent)
                                    {
                                        System.Console.WriteLine("Press any key to return \n");
                                        Console.ReadKey(true);
                                    }
                                    return false;
                                }
                            }

                            if (ZCSPort == "")
                                ZCSPort = myXmlConfig.ConfigObj.ZimbraServer.Port;

                            if (LoglevelStr == "")
                                LoglevelStr = myXmlConfig.ConfigObj.GeneralOptions.LogLevel;

                            Log.SetLogLevel(Log.LogLevelStr2Enum(LoglevelStr));

                            if (ZCSDomain == "")
                                ZCSDomain = myXmlConfig.ConfigObj.UserProvision.DestinationDomain;

                            /* if (Mail == false)
                                 Mail = myXmlConfig.ConfigObj.ImportOptions.Mail;*/

                            if (CommandLineArgs.I.arg("MaxThreadCount") != null)
                                MaxThreads = CommandLineArgs.I.argAsInt("MaxThreadCount");
                            else
                                MaxThreads = myXmlConfig.ConfigObj.GeneralOptions.MaxThreadCount;
                            if (MaxThreads == 0)
                                MaxThreads = 4;

                            if (CommandLineArgs.I.arg("MaxErrorCount") != null)
                                MaxErrors = CommandLineArgs.I.argAsInt("MaxErrorCount");
                            else
                                MaxErrors = myXmlConfig.ConfigObj.GeneralOptions.MaxErrorCount;

                            if (CommandLineArgs.I.arg("IsSkipPrevMigratedItems") != null)
                                SkipPreviousMigration = CommandLineArgs.I.argAsBool("IsSkipPrevMigratedItems");
                            else
                                SkipPreviousMigration = myXmlConfig.ConfigObj.AdvancedImportOptions.IsSkipPrevMigratedItems;

                            if (CommandLineArgs.I.arg("IsMaxMessageSize") != null)
                                IsMaxSize = CommandLineArgs.I.argAsBool("IsMaxMessageSize");
                            else
                                IsMaxSize = myXmlConfig.ConfigObj.AdvancedImportOptions.IsMaxMessageSize;

                            if (CommandLineArgs.I.arg("IsSkipFolders") != null)
                                SkipFolder = CommandLineArgs.I.argAsBool("IsSkipFolders");
                            else
                                SkipFolder = myXmlConfig.ConfigObj.AdvancedImportOptions.IsSkipFolders;

                            if (CommandLineArgs.I.arg("IsOnOrAfter") != null)
                                Datefilter = CommandLineArgs.I.argAsBool("IsOnOrAfter");
                            else
                                Datefilter = myXmlConfig.ConfigObj.AdvancedImportOptions.IsOnOrAfter;

                            if (CommandLineArgs.I.arg("UseSSL") != null)
                                UseSSL = CommandLineArgs.I.argAsBool("UseSSL");
                            else
                                UseSSL = myXmlConfig.ConfigObj.ZimbraServer.UseSSL;

                            if (CommandLineArgs.I.arg("COS") != null)
                                COS = CommandLineArgs.I.argAsString("COS");
                            else
                                COS = myXmlConfig.ConfigObj.UserProvision.COS;

                            if (CommandLineArgs.I.arg("Mail") != null)
                                Mail = CommandLineArgs.I.argAsBool("Mail");
                            else
                                Mail = myXmlConfig.ConfigObj.ImportOptions.Mail;

                            if (CommandLineArgs.I.arg("Calendar") != null)
                                Calendar = CommandLineArgs.I.argAsBool("Calendar");
                            else
                                Calendar = myXmlConfig.ConfigObj.ImportOptions.Calendar;

                            if (CommandLineArgs.I.arg("Contacts") != null)
                                Contacts = CommandLineArgs.I.argAsBool("Contacts");
                            else
                                Contacts = myXmlConfig.ConfigObj.ImportOptions.Contacts;

                            if (CommandLineArgs.I.arg("Sent") != null)
                                Sent = CommandLineArgs.I.argAsBool("Sent");
                            else
                                Sent = myXmlConfig.ConfigObj.ImportOptions.Sent;

                            if (CommandLineArgs.I.arg("DeletedItems") != null)
                                DeletedItems = CommandLineArgs.I.argAsBool("DeletedItems");
                            else
                                DeletedItems = myXmlConfig.ConfigObj.ImportOptions.DeletedItems;

                            if (CommandLineArgs.I.arg("Junk") != null)
                                Junk = CommandLineArgs.I.argAsBool("Junk");
                            else
                                Junk = myXmlConfig.ConfigObj.ImportOptions.Junk;

                            if (CommandLineArgs.I.arg("Tasks") != null)
                                Tasks = CommandLineArgs.I.argAsBool("Tasks");
                            else
                                Tasks = myXmlConfig.ConfigObj.ImportOptions.Tasks;

                            if (CommandLineArgs.I.arg("Rules") != null)
                                Rules = CommandLineArgs.I.argAsBool("Rules");
                            else
                                Rules = myXmlConfig.ConfigObj.ImportOptions.Rules;

                            if (CommandLineArgs.I.arg("OOO") != null)
                                OOO = CommandLineArgs.I.argAsBool("OOO");
                            else
                                OOO = myXmlConfig.ConfigObj.ImportOptions.OOO;
                        }
                        else
                        {
                            if (ConfigXmlFile != "")
                            {
                                if (!File.Exists(ConfigXmlFile))
                                {
                                    Log.err("XML file not present. Please check the file name and path.");
                                    System.Console.WriteLine("XML file not present. Please check the file name and path.");

                                    if (!Silent)
                                        Console.ReadKey(true);

                                    return false;
                                }
                            }
                            else
                            {
                                Log.err("Config file or cmdline arguments are needed. Please check the arguments.");
                                System.Console.WriteLine("Config file or cmdline arguments are needed. Please check the arguments.");

                                if (!Silent)
                                    Console.ReadKey(true);

                                return false;
                            }
                        }

                        // ItemsAndFolders
                        ItemsAndFoldersOptions itemFolderFlags = ItemsAndFoldersOptions.None;
                        if (Calendar)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Calendar;

                        if (Contacts)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Contacts;

                        if (Mail)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Mail;

                        if (Sent)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Sent;

                        if (DeletedItems)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.DeletedItems;

                        if (Junk)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Junk;

                        if (Tasks)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Tasks;

                        if (Rules)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Rules;

                        if (OOO)
                            itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.OOO;

                        // Build a MigrationOptions object
                        MigrationOptions migOpts = new MigrationOptions();
                        migOpts.ItemsAndFolders = itemFolderFlags;

                        // DateFilter
                        if (MigrateDate == "")
                            MigrateDate = myXmlConfig.ConfigObj.AdvancedImportOptions.MigrateOnOrAfter.ToString();

                        if (Datefilter)
                            migOpts.MigrateOnOrAfterDate = MigrateDate;

                        // Folderlist
                        if (Folderlist == "")
                        {
                            MVVM.ViewModel.OptionsViewModel M = new MVVM.ViewModel.OptionsViewModel();
                            Folderlist = M.ConvertToCSV(myXmlConfig.ConfigObj.AdvancedImportOptions.FoldersToSkip, ",");
                        }

                        // SkipFolders
                        if (SkipFolder)
                            migOpts.SkipFolders = Folderlist;

                        // MessageSizeFilter
                        if (IsMaxSize)
                        {
                            if (MaxMessageSize == "")
                                MaxMessageSize = myXmlConfig.ConfigObj.AdvancedImportOptions.MaxMessageSize;

                            migOpts.MessageSizeFilter = MaxMessageSize;
                        }

                        // SkipPrevMigrated
                        migOpts.SkipPrevMigrated = SkipPreviousMigration;

                        if (SourceHost == "")
                            SourceHost = myXmlConfig.ConfigObj.SourceServer.Hostname;

                        if (SourceAdmin == "")
                            SourceAdmin = myXmlConfig.ConfigObj.SourceServer.AdminID;

                        /* if (MaxErrors == 0)
                         {
                             MaxErrors = myXmlConfig.ConfigObj.GeneralOptions.MaxErrorCount;
                         }*/

                        migOpts.MaxErrorCnt = MaxErrors;


                        // =====================================================================================================================
                        // Create and Initialize CSMigrationWrapper
                        // =====================================================================================================================
                        System.Console.WriteLine("Connecting to source store...");
                        Log.info("Opening source store");
                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Green, "  Migration Initialization ");
                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Green, "");

                        CssLib.CSMigrationWrapper MigWrapper;
                        try
                        {
                            MigWrapper = new CSMigrationWrapper("MAPI");
                        }
                        catch (Exception e)
                        {
                            string error = "Migrationwrapper cannot be initialised. Migration dll cannot be loaded.";
                            error += e.Message;
                            System.Console.WriteLine();
                            System.Console.WriteLine(error);
                            Log.err(error);
                            // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, error);
                            // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, "");

                            if (!Silent)
                                Console.ReadKey(true);

                            return false;
                        }

                        if (ProfileOrAdminID != "") // This is name of Exch profile for server migrate
                        {
                            if (Pstfile == "")
                            {
                                string retval = MigWrapper.InitCSMigrationWrapper(ProfileOrAdminID, "", "");
                                if (retval.Length > 0)
                                {
                                    System.Console.WriteLine();
                                    System.Console.WriteLine("Error in Migration Initialization. Please check Outlook is not already running.");
                                    Log.err("Error in Migration Initialization. Please check Outlook is not already running.");
                                    //ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, " Error in Migration Initialization ");
                                    //ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, retval);
                                    if (!Silent)
                                        Console.ReadKey(true);

                                    return false;
                                }
                            }
                        }
                        else
                        {
                            if ((SourceHost != "") && (SourceAdmin != ""))
                            {
                                string retval = MigWrapper.InitCSMigrationWrapper(SourceHost, SourceAdmin, "");
                                if (retval.Length > 0)
                                {
                                    System.Console.WriteLine();
                                    System.Console.WriteLine("Error in server Migration Initialization.");
                                    Log.err("Error in server Migration Initialization.");
                                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, " Error in Migration Initialization ");
                                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, retval);
                                    if (!Silent)
                                        Console.ReadKey(true);
                                    return false;
                                }
                            }
                        }


                        ZimbraValues.GetZimbraValues().ClientVersion = new MVVM.Model.BuildNum().BUILD_NUM;



                        if (ServerMigration)
                        {
                            // =====================================================================================================================
                            // **************************************  SERVER MIGRATION ************************************************************
                            // =====================================================================================================================

                            // ---------------------------------------------
                            // Logon to Zimbra server
                            // ---------------------------------------------
                            Account userAcct = new Account();

                            //ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Green, "Connecting to to Zimbra Server \n   ");
                            System.Console.WriteLine("Connecting to Zimbra Server...");
                            Log.summary("Connecting to Zimbra Server...");
                            System.Console.WriteLine();

                            ZimbraAPI zimbraAPI = new ZimbraAPI(true, migOpts.SpecialCharRep);
                            int stat = zimbraAPI.Logon(ZCSHost, ZCSPort, ZCSID, ZCSPwd, UseSSL, true);
                            if (stat != 0)
                            {
                                zimbraAPI.LastError.Count();

                                System.Console.WriteLine();
                                string message = "Failed to logon to Zimbra Server for adminAccount " + myXmlConfig.ConfigObj.ZimbraServer.AdminID + ". " +
                                                 System.Globalization.CultureInfo.CurrentCulture.TextInfo.ToTitleCase(zimbraAPI.LastError.ToLower())+".";
                                System.Console.WriteLine(message);
                                Log.err(message);
                                // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, "Logon to to Zimbra Server  for adminAccount failed " + myXmlConfig.ConfigObj.ZimbraServer.AdminID);
                                System.Console.WriteLine();

                                // return false;
                            }


                            // =============================================================================
                            // Make sure all accounts provisioned (only do this for server migration)
                            // =============================================================================
                            System.Console.WriteLine("Checking accounts...");
                            foreach (MVVM.Model.Users user in myXmlConfig.UserList)
                            {
                                string acctName;
                                if (user.MappedName == "")
                                    acctName = user.UserName + '@' + (ZCSDomain == "" ? ZCSHost : ZCSDomain);
                                else
                                    acctName = user.MappedName + '@' + (ZCSDomain == "" ? ZCSHost : ZCSDomain);


                                // ---------------------------------------------
                                // Provision account if not already provisioned
                                // ---------------------------------------------
                                if (zimbraAPI.GetAccount(acctName, true) == 0)
                                {
                                    // ... account already provisioned
                                    string mesg = "Previously provisioned: " + acctName;
                                    System.Console.WriteLine(mesg);
                                    Log.info(mesg);
                                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Green, " Migration to Zimbra Started  for UserAccount " + acctName);
                                    user.IsProvisioned = true;
                                }
                                else
                                {
                                    // ... not yet provisioned
                                    string err = "Provisioning:           " + acctName;
                                    System.Console.WriteLine(err);
                                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Yellow, " User is not provisioned on Zimbra Server " + acctName);
                                    Log.info(err);
                                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Green, " Provisioning user" + acctName);

                                    // Since account not provisioned we can drop any history file
                                    string historyfile = Path.GetTempPath() + acctName.Substring(0, acctName.IndexOf('@')) + "history.log";
                                    if (File.Exists(historyfile))
                                    {
                                        try
                                        {
                                            File.Delete(historyfile);
                                        }
                                        catch (Exception e)
                                        {
                                            string msg = "exception in deleteing the Histroy file " + e.Message;
                                            System.Console.WriteLine(msg);
                                            Log.err(msg);
                                        }
                                    }
                                    System.Console.WriteLine(err);
                                    System.Console.WriteLine();
                                    System.Console.WriteLine();

                                    string Defaultpwd = "";

                                    /************************************/
                                    //if csv file has a pwd use it else looks for the pwd in xml file.
                                    if ((user.PWDdefault != ""))
                                        Defaultpwd = user.PWDdefault;
                                    else
                                    {
                                        Defaultpwd = myXmlConfig.ConfigObj.UserProvision.DefaultPWD;
                                        if (Defaultpwd == null)
                                            Defaultpwd = "default";
                                    }

                                    bool mustChangePW = user.ChangePWD;

                                    String CosID = COS;
                                    int retsval = zimbraAPI.GetAllCos();

                                    foreach (CosInfo cosinfo in ZimbraValues.GetZimbraValues().COSes)
                                    {
                                        if (cosinfo.CosName == COS)
                                            CosID = cosinfo.CosID;
                                    }

                                    // Create Account
                                    if (zimbraAPI.CreateAccount(acctName, "", "", "", "", Defaultpwd, mustChangePW, CosID) == 0)
                                    {
                                        System.Console.WriteLine();
                                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Green, " Provisioning useraccount success " + acctName);
                                        err = "Provisioning useraccount success " + acctName;
                                        System.Console.WriteLine(err);
                                        System.Console.WriteLine();
                                        System.Console.WriteLine();
                                        Log.info(err);
                                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Green, " Migration to Zimbra Started  for UserAccount  " + user.UserName);
                                        err = "Migration to Zimbra Started  for UserAccount  " + user.UserName;
                                        System.Console.WriteLine(err);
                                        System.Console.WriteLine();
                                        Log.info(err);
                                        user.IsProvisioned = true;
                                    }
                                    else
                                    {
                                        System.Console.WriteLine();

                                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, " error provisioning user " + acctName);
                                        err = "error provisioning user " + acctName + ". " + System.Globalization.CultureInfo.CurrentCulture.TextInfo.ToTitleCase(zimbraAPI.LastError.ToLower()) + "\n";
                                        System.Console.WriteLine(err);
                                        Log.err(err);
                                        user.IsProvisioned = false;
                                    }
                                }

                                string final = user.StatusMessage;

                            } // for each user


                            // =============================================================================================
                            // All accounts now provisioned - Migrate the accounts on another thread and wait here 'til done
                            // ==============================================================================================
                            System.Console.WriteLine("");
                            System.Console.WriteLine("Starting migration to Zimbra...");
                            System.Console.WriteLine("");
                            if (myXmlConfig.UserList.Count > 0)
                            {
                                countdownEvent = new CountdownEvent(myXmlConfig.UserList.Count);
                                userAccts.StartMigration(myXmlConfig.UserList, ZCSDomain, migOpts, countdownEvent, MigWrapper, MaxThreads);
                                countdownEvent.Wait();

                                Console.WriteLine("Finished Migration");
                                Log.info("Finished Migration");
                                Log.info("UNinit Migration");
                            }

                            string retval = MigWrapper.UninitCSMigrationWrapper();
                            if (retval.Length > 0)
                            {
                                System.Console.WriteLine();
                                System.Console.WriteLine("Error in Migration UnInitialization ");
                                Log.err("Error in Migration UnInitialization ");
                                // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, " Error in Migration Initialization ");
                                // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, retval);
                                System.Console.WriteLine();
                                keepRunning = true;
                                return false;
                            }

                            keepRunning = true;

                        } // if (ServerMigration)
                        else
                        {
                            // =====================================================================================================================
                            // **************************************  USER MIGRATION **************************************************************
                            // =====================================================================================================================
                            if ((ProfileOrAdminID != "") || (Pstfile != ""))
                            {
                                // ---------------------------------------------
                                // Logon to Zimbra server
                                // ---------------------------------------------
                                // AccountName
                                string accountname = ZCSID;
                                if (!accountname.Contains("@"))
                                    accountname = accountname + "@" + (((ZCSDomain == "") || (ZCSDomain == null)) ? ZCSHost : ZCSDomain);

                                // AccountID
                                string accountid = (Pstfile != "") ? Pstfile : ProfileOrAdminID;

                                ZimbraAPI zimbraAPI = new ZimbraAPI(false);

                                // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Green, "Connecting to to Zimbra Server \n   ");
                                string err = "Connecting to Zimbra Server...";
                                System.Console.WriteLine(err);
                                Log.info(err);

                                int stat = zimbraAPI.Logon(ZCSHost, ZCSPort, accountname, ZCSPwd, UseSSL, false);
                                if (stat != 0)
                                {
                                    zimbraAPI.LastError.Count();

                                    System.Console.WriteLine();
                                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red,"Logon to to Zimbra Server  for userAccount failed " +ZCSID);
                                    err = "Logon to Zimbra Server for userAccount " + ZCSID + " Failed. " + System.Globalization.CultureInfo.CurrentCulture.TextInfo.ToTitleCase(zimbraAPI.LastError.ToLower());
                                    System.Console.WriteLine(err);
                                    Log.err(err);

                                    string val = MigWrapper.UninitCSMigrationWrapper();
                                    if (val.Length > 0)
                                    {
                                        System.Console.WriteLine();
                                        System.Console.WriteLine("Error in Migration UnInitialization ");
                                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red," Error in Migration Initialization ");
                                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, retval);
                                        Log.err("Error in Migration UnInitialization");
                                        System.Console.WriteLine();
                                        keepRunning = true;
                                        return false;
                                    }

                                    if (!Silent)
                                        Console.ReadKey(true);

                                    keepRunning = true;
                                    return false;
                                }


                                System.Console.WriteLine();
                                // ProgressUtil.RenderConsoleProgress( 30, '\u2591', ConsoleColor.Green, " Migration to Zimbra Started  for Profile/PST  " + accountid);
                                err = "Starting migration to Zimbra for Profile/PST " + accountid;
                                System.Console.WriteLine(err);
                                Log.info(err);


                                MVVM.Model.Users User = new MVVM.Model.Users();
                                User.UserName = ProfileOrAdminID;

                                List<MVVM.Model.Users> users = new List<MVVM.Model.Users>();
                                users.Add(User);


                                // =============================================================================
                                // Migrate the accounts on another thread and wait here 'til done
                                // =============================================================================
                                countdownEvent = new CountdownEvent(1);
                                userAccts.StartMigration(users, ZCSHost, migOpts, countdownEvent, MigWrapper, MaxThreads, false, accountname, accountid);
                                countdownEvent.Wait();


                                Log.info("Finished Migration");
                                Console.WriteLine();
                                Console.WriteLine("Finished Migration");
                                Console.WriteLine("UNinit Migration");
                                Log.info("UNinit Migration");

                                string retval = MigWrapper.UninitCSMigrationWrapper();
                                if (retval.Length > 0)
                                {
                                    System.Console.WriteLine();
                                    System.Console.WriteLine("Error in Migration UnInitialization ");
                                    Log.err("Error in Migration UnInitialization ");
                                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, " Error in Migration Initialization ");
                                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, retval);
                                    keepRunning = true;
                                    return false;
                                }

                                keepRunning = true;
                            }
                        }
                    }
                    else
                    {
                        System.Console.WriteLine();
                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, " Make sure the correct arguments (2) are passed \n");
                        System.Console.WriteLine("Make sure the correct arguments (2) are passed. Use '-Help' for more information\n");
                        System.Console.WriteLine();
                        Log.err("Make sure the correct arguments (2) are passed. Use '-Help' for more information\n");
                        return false;
                    }
                }


                /// Account userAccts = new Account();
                if (_userRequestExit)
                {
                    keepRunning = true;
                    //set flag to exit loop.  Other conditions could cause this too, which is why we use a seperate variable      
                    // Console.WriteLine("Shutting down, user requested exit");
                }

                Log.info("Migration finished");

                // ----------------------------------------------------
                // Wait for keypress if not silent
                // ----------------------------------------------------
                bool bSilent = false;
                if (CommandLineArgs.I.arg("Silent") != null)
                    bSilent = CommandLineArgs.I.argAsBool("Silent");
                else
                    bSilent = false;

                if (!bSilent)
                {
                    System.Console.WriteLine("Press any key to continue \n");
                    Console.ReadKey(true);
                }

                return true;
            }
        }
    }
}
