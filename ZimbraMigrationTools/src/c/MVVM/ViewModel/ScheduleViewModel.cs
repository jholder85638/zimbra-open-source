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
using MVVM.Model;
using Misc;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections;
using System.ComponentModel;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Threading;
using System.Windows.Input;
using System.Windows;
using System;

namespace MVVM.ViewModel
{
public class ScheduleViewModel: BaseViewModel
{
    readonly Schedule m_schedule = new Schedule(false);
    string m_configFile;
    string m_usermapFile;
    bool m_isPreview;
    bool m_isComplete;

    public ScheduleViewModel()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("ScheduleViewModelScheduleViewModel"))
        {
            this.ScheduleTaskCommand = new ActionCommand(this.ScheduleTask, () => true);
            this.PreviewCommand = new ActionCommand(this.Preview, () => true);
            this.BackCommand = new ActionCommand(this.Back, () => true);
            this.MigrateCommand = new ActionCommand(this.Migrate, () => true);
            m_configFile = "";
            m_usermapFile = "";
            m_isPreview = false;
            m_isComplete = false;
        }
    }

    public string GetConfigFile()
    {
        return m_configFile;
    }

    public void SetConfigFile(string configFile)
    {
        this.m_configFile = configFile;
    }

    public void LoadConfig(Config config)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            CurrentCOSSelection = 0;
            string cos = config.UserProvision.COS;

            if (CosList.Count > 0)
            {
                for (int i = 0; i < CosList.Count; i++)
                {
                    if (cos == CosList[i].CosName)
                    {
                        CurrentCOSSelection = i;
                        // DomainsFilledIn = true;
                        break;
                    }
                }
            }
            else
            {
                CosInfo cs = new CosInfo("", "");
                cs.CosName = cos;
                CosList.Add(cs);
            }
        }
    }

    public void SetUsermapFile(string usermapFile)
    {
        this.m_usermapFile = usermapFile;
    }

    public bool IsPreviewMode()
    {
        return m_isPreview;
    }

    public bool IsComplete()
    {
        return m_isComplete;
    }

    // Commands
    public ICommand ScheduleTaskCommand 
    {
        get;
        private set;
    }

    private void ScheduleTask()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            /*
             * OperatingSystem os = System.Environment.OSVersion;
             * Version v = os.Version;
             * string strTaskScheduler = Environment.GetEnvironmentVariable("SYSTEMROOT");
             *
             * if (v.Major >= 6)
             * {
             *  strTaskScheduler += "\\system32\\taskschd.msc";
             *  System.Diagnostics.Process.Start(@strTaskScheduler);
             * }
             * else
             * {
             *  strTaskScheduler += "\\system32\\control";
             *  System.Diagnostics.Process proc = new System.Diagnostics.Process();
             *  proc.StartInfo.FileName = strTaskScheduler;
             *  proc.StartInfo.Arguments = "schedtasks";
             *  proc.Start();
             * }
             */
            const int TR_MAX_SIZE = 261;

            if ((m_configFile.Length == 0) && (m_usermapFile.Length == 0))
            {
                MessageBox.Show("There must be a config file (Use Load option in Source and Destination Screen) and usermap file (Use LoadCSV option in Users Screen)", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }
            if (m_configFile.Length == 0)
            {
                MessageBox.Show("Please use the Load option in Source, Destination or Options screen to load the configuration xml ", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }
            if (m_usermapFile.Length == 0)
            {
                MessageBox.Show("Please use the LoadCSV option in Users screen to load a usermap file", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }


            OperatingSystem os = System.Environment.OSVersion;
            Version v = os.Version;

            // -----------------------------------------------------------------------------
            // Create a Process to run "schtasks.exe" to ad the task to windows scheduler
            // -----------------------------------------------------------------------------
            System.Diagnostics.Process proc = new System.Diagnostics.Process();
            proc.StartInfo.FileName = "c:\\windows\\system32\\schtasks.exe";

            // set up date, time, and name for task scheduler
            // FBS Bug 74232 -- need to get the right format for Schtasks SD parameter (dtStr)
            // Has to be either MM/DD/YYYY, DD/MM/YYYY, or YYYY/MM/DD
            // C# formatting stuff and schtasks are both very fussy.  Only MM capitalized (because of minutes)
            string dtStr = Convert.ToDateTime(this.ScheduleDate).ToString("MM/dd/yyyy");
            CultureInfo currentCulture = Thread.CurrentThread.CurrentCulture;
            String shortDatePattern = currentCulture.DateTimeFormat.ShortDatePattern;
            if ((shortDatePattern.StartsWith("d")) || (shortDatePattern.StartsWith("D")))   // being safe with "D"
                dtStr = Convert.ToDateTime(this.ScheduleDate).ToString("dd/MM/yyyy");
            else
            if ((shortDatePattern.StartsWith("y")) || (shortDatePattern.StartsWith("Y")))   // being safe with "Y"
                dtStr = Convert.ToDateTime(this.ScheduleDate).ToString("yyyy/MM/dd");

            if (v.Major < 6)    // XP is a pain -- you have to make sure is has slashes.  W7 doesn't care
            {
                if (dtStr.Contains("."))
                    dtStr = dtStr.Replace(".", "/");
                else
                if (dtStr.Contains("-"))
                    dtStr = dtStr.Replace("-", "/");
            }

            string dtTime = MakeTimeStr();
            string dtName = "ZimbraMigrate" + dtTime.Substring(0, 2) + dtTime.Substring(3, 2);


            // FBS Bug 75004 -- 6/6/12
            string userEntry = dtStr + " " + dtTime;
            DateTime userDT = Convert.ToDateTime(userEntry);
            DateTime nowDT = DateTime.Now;
            if (DateTime.Compare(userDT, nowDT) < 0)
            {
                MessageBox.Show("You can't schedule a task in the past", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            proc.StartInfo.Arguments = "/Create /SC ONCE /TR ";
            proc.StartInfo.Arguments += @"""";
            proc.StartInfo.Arguments += @"\";
            proc.StartInfo.Arguments += @"""";
            proc.StartInfo.Arguments += ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]).InstallDir;
            proc.StartInfo.Arguments += @"\";
            proc.StartInfo.Arguments += "ZimbraMigrationConsole.exe";
            proc.StartInfo.Arguments += @"\";
            proc.StartInfo.Arguments += @"""";
            proc.StartInfo.Arguments += " ";

            // FBS bug 74232 -- 6/1/12 -- have to put \" around arguments since they might have spaces
            proc.StartInfo.Arguments += "\\\"" + "ConfigxmlFile=" + m_configFile + "\\\"" + " ";
            proc.StartInfo.Arguments += "\\\"" + "Users=" + m_usermapFile + "\\\"";
            proc.StartInfo.Arguments += @"""";

            // FBS bug 74232 -- make sure value for /TR option does not exceed 261 characters
            int trLen = proc.StartInfo.Arguments.Length - 21;  // 21 is length of "/Create /SC ONCE /TR "
            if (trLen > TR_MAX_SIZE)
            {
                MessageBox.Show("Taskrun argument string exceeds 261 characters.  Please use config files with smaller path sizes.", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            if (v.Major >= 6)
                proc.StartInfo.Arguments += " /F /Z /V1";
            proc.StartInfo.Arguments += " /TN " + dtName + " /SD " + dtStr + " /ST " + dtTime;
            proc.Start();

            string s = "Task '" + dtName + "' scheduled successfully. You can view this task in Windows Task Scheduler.";
            MessageBox.Show(s, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Information);
        }
    }

    // ========================================================================================================================================
    // Commands
    // ========================================================================================================================================
    public ICommand BackCommand 
    {
        get;
        private set;
    }

    private void Back()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            OptionsViewModel optionsViewModel = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);
            if (optionsViewModel.IsPublicFolders)
                lb.SelectedIndex = 5;
            else
                lb.SelectedIndex = 4;
        }
    }

    // ========================================================================================================================================
    // Migrate and Preview button click handlers
    // ========================================================================================================================================
    public ICommand PreviewCommand 
    {
        get;
        private set;
    }

    private void Preview()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            MigrateOrPreview(false);
        }
    }

    public ICommand MigrateCommand 
    {
        get;
        private set;
    }

    private void Migrate()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            MigrateOrPreview(true);
        }
    }

    // ========================================================================================================================================
    // Helpers
    // ========================================================================================================================================
    private int AvailableThread()
    // Called from worker_RunWorkerCompleted() after an account has been migrated to see if
    // a thread is available for the next account
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            int iThreadNum = -1;
            for (int i = 0; i < bgwlist.Count; i++)
            {
                Log.info("bgwlist count", bgwlist.Count);
                if (!bgwlist[i].IsBusy)
                {
                    iThreadNum = i;
                    Log.info(" Available Thread is number ", i);
                    break;
                }
            }
            return iThreadNum;
        }
    }

    private void MigrateOrPreview(bool bMigrateNotPreview)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            m_isPreview = !bMigrateNotPreview;

            // Init migration wrapper
            CSMigrationWrapper mw               = ((IntroViewModel)  ViewModelPtrs[(int)ViewType.INTRO]).mw;
            OptionsViewModel optionsViewModel   = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);
            ConfigViewModelS configViewModelSrc = ((ConfigViewModelS)ViewModelPtrs[(int)ViewType.SVRSRC]);
            if (!optionsViewModel.IsPublicFolders && configViewModelSrc.Isprofile)
            {
                string ret = mw.InitCSMigrationWrapper(configViewModelSrc.OutlookProfile, "", "");
                if (ret.Length > 0)
                {
                    MessageBox.Show("Outlook Profile Initialization Failed.", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }
            }

            if (optionsViewModel.IsPublicFolders)
                DoPublicMigrate(m_isPreview);
            else
                DoMigrate(m_isPreview);
        }
    }

    public void DoMigrate(bool isPreview)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            bgwlist.Clear();

            // ============================================
            // Some things we do for SERVER migration only
            // ============================================
            if (isServer)
            {
                if (CurrentCOSSelection == -1)
                    CurrentCOSSelection = 0;

                // Check logged onto to destination svr
                if (ZimbraValues.zimbraValues.AuthToken.Length == 0)
                {
                    MessageBox.Show("You must log on to the Zimbra server", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }

                // Check logged on to source server
                ConfigViewModelS sourceModel = ((ConfigViewModelS)ViewModelPtrs[(int)ViewType.SVRSRC]);
                if (!sourceModel.IsMailServerInitialized)
                {
                    MessageBox.Show("You must log on to Exchange", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }

                UsersViewModel usersViewModel = ((UsersViewModel)ViewModelPtrs[(int)ViewType.USERS]);
                string domainName = usersViewModel.ZimbraDomain;

                string defaultPWD = DefaultPWD;
                string tempMessage = "";
                bool bProvision = false;
                MessageBoxImage mbi = MessageBoxImage.Information;

                // ----------------------------------------------------------
                // Check all accounts in the schedule list
                // ----------------------------------------------------------
                for (int i = 0; i < SchedList.Count; i++)
                {
                    string userName = (usersViewModel.UsersList[i].MappedName.Length > 0) ? usersViewModel.UsersList[i].MappedName :
                                                                                            usersViewModel.UsersList[i].Username;
                    string accountName = userName + "@" + domainName;

                    // ----------------------------------
                    // Provision the account if necessary
                    // ----------------------------------
                    if (!SchedList[i].isProvisioned)
                    {
                        if (!isPreview)
                        {
                            bProvision = true;
                            if (defaultPWD.Length == 0)
                            {
                                MessageBox.Show("Please provide an initial password", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Exclamation);
                                return;
                            }

                            string cosID = CosList[CurrentCOSSelection].CosID;
                            ZimbraAPI zimbraAPI = new ZimbraAPI(isServer);

                            // FBS bug 71646 -- 3/26/12
                            string displayName = "";
                            string givenName = "";
                            string sn = "";
                            string zfp = "";
                            // FBS bug 73395 -- 4/25/12
                            ObjectPickerInfo opinfo = usersViewModel.GetOPInfo();
                            if (opinfo.DisplayName.Length > 0)
                                displayName = opinfo.DisplayName;
                            if (opinfo.GivenName.Length > 0)
                                givenName = opinfo.GivenName;
                            if (opinfo.Sn.Length > 0)
                                sn = opinfo.Sn;
                            if (opinfo.Zfp.Length > 0)
                                zfp = opinfo.Zfp;
                            // end 73395
                            // end 71646


                            // Since this account is not even provisioned yet, nothing can have been migrated so delete history file
                            string historyfile = Path.GetTempPath() + accountName.Substring(0, accountName.IndexOf('@')) + "history.log";
                            if (File.Exists(historyfile))
                            {
                                try
                                {
                                    File.Delete(historyfile);
                                }
                                catch (Exception e)
                                {
                                    string msg = "exception in deleteing the History file " + e.Message;
                                    System.Console.WriteLine(msg);
                                }
                            }

                            bool mustChangePW = usersViewModel.UsersList[i].MustChangePassword;
                            if (zimbraAPI.CreateAccount(accountName, displayName, givenName, sn, zfp, defaultPWD, mustChangePW, cosID) == 0)
                            {
                                tempMessage += string.Format("{0} Provisioned", userName) + "\n";
                                // MessageBox.Show(string.Format("{0} Provisioned", userName), "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Information);
                            }
                            else
                            {
                                // MessageBox.Show(string.Format("Provision unsuccessful for {0}: {1}", userName, zimbraAPI.LastError), "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                                tempMessage += string.Format("Provision unsuccessful for {0}: {1}", userName, zimbraAPI.LastError) + "\n";
                                mbi = MessageBoxImage.Error;
                            }
                        }
                    }
                }

                if (bProvision)
                    MessageBox.Show(tempMessage, "Zimbra Migration", MessageBoxButton.OK, mbi);

                if (mbi == MessageBoxImage.Error)
                    return;

                lb.SelectedIndex = 7;
            }
            else
            {
                lb.SelectedIndex = 4;
            }

            AccountResultsViewModel accountResultsViewModel = ((AccountResultsViewModel)ViewModelPtrs[(int)ViewType.RESULTS]);
            accountResultsViewModel.AccountResultsList.Clear();

            if (isServer)
            {
                EnableMigrate = false;
                EnablePreview = false;
            }
            else
                ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]).OEnableNext = false;

            accountResultsViewModel.EnableStop = !EnableMigrate;

            // ==========================================================
            // Add all accounts to be migrated to the accounts list view
            // ==========================================================
            int num = 0;
            foreach (SchedUser su in SchedList)
                accountResultsViewModel.AccountResultsList.Add(new AccountResultsViewModel(this, num++, su.username, accountResultsViewModel.EnableStop));

            accountResultsViewModel.OpenLogFileEnabled = true;


            // ==========================================================
            // Create migration worker threads
            // ==========================================================

            // TESTPOINT_MIGRATION_CREATE_MIGRATION_THREADS

            // FBS bug 71048 -- 4/16/12 -- use the correct number of threads.
            // If MaxThreadCount not specified, default to 4.  If fewer users than MaxThreadCount, numThreads = numUsers
            OptionsViewModel ovm = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);
            int maxThreads = (ovm.MaxThreadCount > 0) ? ovm.MaxThreadCount : 4;
            maxThreads = Math.Min(maxThreads, 8);   // let's make 8 the limit for now

            int numUsers = SchedList.Count;
            int numThreads = Math.Min(numUsers, maxThreads);

            Log.info("Schedule background workers with numusers " + numUsers + " and maxthreads " + numThreads);
            for (int i = 0; i < numUsers; i++)
            {
                if (i < numThreads)
                {
                    UserBW bgw = new UserBW(i);
                    bgw.DoWork                      += new System.ComponentModel.DoWorkEventHandler(worker_DoWork);
                    bgw.ProgressChanged             += new System.ComponentModel.ProgressChangedEventHandler(worker_ProgressChanged);
                    bgw.WorkerReportsProgress       = true;
                    bgw.WorkerSupportsCancellation  = true;
                    bgw.RunWorkerCompleted          += new System.ComponentModel.RunWorkerCompletedEventHandler(worker_RunWorkerCompleted);
                    bgw.usernum                     = i;

                    bgw.RunWorkerAsync(i);
                    bgwlist.Add(bgw);
                    Log.info("Background worker started number :", bgw.threadnum);
                }
                else
                {
                    Log.info("adding user number to overflow list", i);
                    overflowList.Add(i);
                }
            }
        }
    }

    public void DoPublicMigrate(bool isPreview)
    {
        // DCB_DUPLICATE_CODE: Very similar to DoMigrate!
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            bgwlist.Clear();

            // ============================================
            // Some things we do for SERVER migration only
            // ============================================
            if (isServer)
            {
                if (CurrentCOSSelection == -1)
                    CurrentCOSSelection = 0;

                // Check logged onto to destination svr
                if (ZimbraValues.zimbraValues.AuthToken.Length == 0)
                {
                    MessageBox.Show("You must log on to the Zimbra server", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }

                // Check logged on to source server
                ConfigViewModelS sourceModel = ((ConfigViewModelS)ViewModelPtrs[(int)ViewType.SVRSRC]);
                if (!sourceModel.IsMailServerInitialized)
                {
                    MessageBox.Show("You must log on to Exchange", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }

                PublicfoldersViewModel foldersViewModel = ((PublicfoldersViewModel)ViewModelPtrs[(int)ViewType.PUBFLDS]);
                string domainName = foldersViewModel.ZimbraDomain;

                UsersViewModel usersViewModel = ((UsersViewModel)ViewModelPtrs[(int)ViewType.USERS]);
                usersViewModel.ZimbraDomain = domainName;

                string defaultPWD = DefaultPWD;
                string tempMessage = "";
                bool bProvision = false;
                MessageBoxImage mbi = MessageBoxImage.Information;

                // ----------------------------------------------------------
                // Check all accounts in the schedule list
                // ----------------------------------------------------------
                for (int i = 0; i < SchedList.Count; i++)
                {
                    string userName = (usersViewModel.UsersList[i].MappedName.Length > 0) ? usersViewModel.UsersList[i].MappedName :
                                                                                            usersViewModel.UsersList[i].Username;
                    string accountName = userName + "@" + domainName;

                    // ----------------------------------
                    // Provision the account if necessary
                    // ----------------------------------
                    if (!SchedList[i].isProvisioned)
                    {
                        if (!isPreview)
                        {
                            bProvision = true;
                            if (defaultPWD.Length == 0)
                            {
                                MessageBox.Show("Please provide an initial password", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Exclamation);
                                return;
                            }

                            string cosID = CosList[CurrentCOSSelection].CosID;
                            ZimbraAPI zimbraAPI = new ZimbraAPI(isServer);

                            // FBS bug 71646 -- 3/26/12
                            string displayName = "";
                            string givenName = "";
                            string sn = "";
                            string zfp = "";
                            // FBS bug 73395 -- 4/25/12
                            ObjectPickerInfo opinfo = usersViewModel.GetOPInfo();
                            if (opinfo.DisplayName.Length > 0)
                                displayName = opinfo.DisplayName;
                            if (opinfo.GivenName.Length > 0)
                                givenName = opinfo.GivenName;
                            if (opinfo.Sn.Length > 0)
                                sn = opinfo.Sn;
                            if (opinfo.Zfp.Length > 0)
                                zfp = opinfo.Zfp;
                            // end 73395
                            // end 71646

                            // Since this account is not even provisioned yet, nothing can have been migrated so delete history file
                            string historyfile = Path.GetTempPath() + accountName.Substring(0, accountName.IndexOf('@')) + "history.log";
                            if (File.Exists(historyfile))
                            {
                                try
                                {
                                    File.Delete(historyfile);
                                }
                                catch (Exception e)
                                {
                                    string msg = "exception in deleteing the History file " + e.Message;
                                    System.Console.WriteLine(msg);
                                }

                            }

                            bool mustChangePW = usersViewModel.UsersList[i].MustChangePassword;
                            if (zimbraAPI.CreateAccount(accountName, displayName, givenName, sn, zfp, defaultPWD, mustChangePW, cosID) == 0)
                            {
                                tempMessage += string.Format("{0} Provisioned", userName) + "\n";
                                // MessageBox.Show(string.Format("{0} Provisioned", userName), "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Information);
                            }
                            else
                            {
                                // MessageBox.Show(string.Format("Provision unsuccessful for {0}: {1}", userName, zimbraAPI.LastError), "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                                tempMessage += string.Format("Provision unsuccessful for {0}: {1}", userName, zimbraAPI.LastError) + "\n";
                                mbi = MessageBoxImage.Error;
                            }
                        }
                    }
                }

                if (bProvision)
                    MessageBox.Show(tempMessage, "Zimbra Migration", MessageBoxButton.OK, mbi);

                if (mbi == MessageBoxImage.Error)
                    return;

                lb.SelectedIndex = 7;
            }
            else
                lb.SelectedIndex = 5;

            AccountResultsViewModel accountResultsViewModel = ((AccountResultsViewModel)ViewModelPtrs[(int)ViewType.RESULTS]);
            accountResultsViewModel.AccountResultsList.Clear();

            if (isServer)
            {
                EnableMigrate = false;
                EnablePreview = false;
            }
            else
                ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]).OEnableNext = false;

            accountResultsViewModel.EnableStop = !EnableMigrate;

            // ==========================================================
            // Add all accounts to be migrated to the accounts list view
            // ==========================================================
            int num = 0;
            foreach (SchedUser su in SchedList)
                accountResultsViewModel.AccountResultsList.Add(new AccountResultsViewModel(this, num++, su.username, accountResultsViewModel.EnableStop));

            accountResultsViewModel.OpenLogFileEnabled = true;


            // ==========================================================
            // Create migration worker threads
            // ==========================================================

            // FBS bug 71048 -- 4/16/12 -- use the correct number of threads.
            // If MaxThreadCount not specified, default to 4.  If fewer users than MaxThreadCount, numThreads = numUsers
            OptionsViewModel ovm = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);
            int maxThreads = (ovm.MaxThreadCount > 0) ? ovm.MaxThreadCount : 4;
            maxThreads = Math.Min(maxThreads, 8);   // let's make 8 the limit for now
            int numUsers = SchedList.Count;
            int numThreads = Math.Min(numUsers, maxThreads);
            Log.info("Schedule background workers with numusers " + numUsers + " and maxthreads " + numThreads);
            for (int i = 0; i < numUsers; i++)
            {
                if (i < numThreads)
                {
                    UserBW bgw = new UserBW(i);
                    bgw.DoWork += new System.ComponentModel.DoWorkEventHandler(worker_DoWork);
                    bgw.ProgressChanged += new System.ComponentModel.ProgressChangedEventHandler(worker_ProgressChanged);
                    bgw.WorkerReportsProgress = true;
                    bgw.WorkerSupportsCancellation = true;
                    bgw.RunWorkerCompleted += new System.ComponentModel.RunWorkerCompletedEventHandler(worker_RunWorkerCompleted);
                    bgw.usernum = i;
                    bgw.RunWorkerAsync(i);
                    bgwlist.Add(bgw);
                    Log.info("Background worker started number :", bgw.threadnum);
                }
                else
                {
                    Log.info("adding user number to overflow list", i);
                    overflowList.Add(i);
                }
            }
        }
    }

    private ObservableCollection<UserBW> bgwlist = new ObservableCollection<UserBW>();
    public ObservableCollection<UserBW> BGWList
    {
        get { return bgwlist; }
        set { bgwlist = value; }
    }

    private ObservableCollection<int> overflowList = new ObservableCollection<int>();
    public ObservableCollection<int> OverflowList
    {
        get { return overflowList; }
        set { overflowList = value; }
    }

    private ObservableCollection<DoWorkEventArgs> eventArglist = new ObservableCollection<DoWorkEventArgs>();
    public ObservableCollection<DoWorkEventArgs> EventArgList 
    {
        get { return eventArglist; }
        set { eventArglist = value; }
    }

    private ObservableCollection<SchedUser> schedlist = new ObservableCollection<SchedUser>();
    public ObservableCollection<SchedUser> SchedList 
    {
        get
        {
            schedlist.Clear();

            UsersViewModel usersViewModel = ((UsersViewModel)ViewModelPtrs[(int)ViewType.USERS]);

            foreach (UsersViewModel obj in usersViewModel.UsersList)
            {
                string NameToCheck = (obj.MappedName.Length > 0) ? obj.MappedName : obj.Username;
                int idx = NameToCheck.IndexOf("@");
                string NameToAdd = (idx != -1) ? NameToCheck.Substring(0, idx) : NameToCheck;

                schedlist.Add(new SchedUser(NameToAdd, obj.IsProvisioned));
            }
            return schedlist;
        }
    }

    private ObservableCollection<SchedUser> schedfolderlist = new ObservableCollection<SchedUser>();
    public ObservableCollection<SchedUser> SchedfolderList
    {
        get
        {
            schedfolderlist.Clear();

            PublicfoldersViewModel usersViewModel = ((PublicfoldersViewModel)ViewModelPtrs[(int)ViewType.PUBFLDS]);

            foreach (PublicfoldersViewModel obj in usersViewModel.UsersBKList)
            {
                string NameToCheck = (obj.ZimbraAccountName.Length > 0) ? obj.ZimbraAccountName : obj.ZimbraAccountName;
                int idx = NameToCheck.IndexOf("@");
                string NameToAdd = (idx != -1) ? NameToCheck.Substring(0, idx) : NameToCheck;

                schedfolderlist.Add(new SchedUser(NameToAdd, obj.IsProvisioned));
            }
            return schedfolderlist;
        }
    }
   
    public string COS 
    {
        get { return m_config.UserProvision.COS; }
        set
        {
            if (value == m_config.UserProvision.COS)
                return;
            m_config.UserProvision.COS = value;

            OnPropertyChanged(new PropertyChangedEventArgs("COS"));
        }
    }

    public string DefaultPWD 
    {
        get { return m_config.UserProvision.DefaultPWD; }
        set
        {
            if (value == m_config.UserProvision.DefaultPWD)
                return;
            m_config.UserProvision.DefaultPWD = value;

            OnPropertyChanged(new PropertyChangedEventArgs("DefaultPWD"));
        }
    }

    public bool EnableMigrate 
    {
        get { return m_schedule.EnableMigrate; }
        set
        {
            if (value == m_schedule.EnableMigrate)
                return;
            m_schedule.EnableMigrate = value;
           // m_schedule.EnableMigrate = m_isComplete ? false : value;
            OnPropertyChanged(new PropertyChangedEventArgs("EnableMigrate"));
        }
    }

    public bool EnablePreview
    {
        get { return m_schedule.EnablePreview; }
        set
        {
            if (value == m_schedule.EnablePreview)
                return;
            m_schedule.EnablePreview = m_isComplete ? false : value;
            OnPropertyChanged(new PropertyChangedEventArgs("EnablePreview"));
        }
    }

    public bool EnableProvGB
    {
        get { return m_schedule.EnableProvGB; }
        set
        {
            if (value == m_schedule.EnableProvGB)
                return;
            m_schedule.EnableProvGB = value;
            OnPropertyChanged(new PropertyChangedEventArgs("EnableProvGB"));
        }
    }

    private int cosSelection;
    public int CurrentCOSSelection 
    {
        get { return cosSelection; }
        set
        {
            cosSelection = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CurrentCOSSelection"));
        }
    }

    public string ScheduleDate 
    {
        get { return m_schedule.ScheduleDate.ToShortDateString(); }
        set
        {
            if (value == m_schedule.ScheduleDate.ToShortDateString())
                return;
            try
            {
                m_schedule.ScheduleDate = Convert.ToDateTime(value);
            }
            catch (Exception)
            {
                MessageBox.Show("Please enter a valid date in the indicated format", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }

            OnPropertyChanged(new PropertyChangedEventArgs("ScheduleDate"));
        }
    }

    private string dateFormatLabelContent2;
    public string DateFormatLabelContent2
    {
        get { return dateFormatLabelContent2; }
        set
        {
            if (value == dateFormatLabelContent2)
                return;
            dateFormatLabelContent2 = value;

            OnPropertyChanged(new PropertyChangedEventArgs("DateFormatLabelContent2"));
        }
    }

    public int HrSelection 
    {
        get { return m_schedule.HrSelection; }
        set
        {
            if (value == m_schedule.HrSelection)
                return;
            m_schedule.HrSelection = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HrSelection"));
        }
    }

    public int MinSelection 
    {
        get { return m_schedule.MinSelection; }
        set
        {
            if (value == m_schedule.MinSelection)
                return;
            m_schedule.MinSelection = value;
            OnPropertyChanged(new PropertyChangedEventArgs("MinSelection"));
        }
    }

    public int AMPMSelection 
    {
        get { return m_schedule.AMPMSelection; }
        set
        {
            if (value == m_schedule.AMPMSelection)
                return;
            m_schedule.AMPMSelection = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AMPMSelection"));
        }
    }

    private string MakeTimeStr()
    {
        string retval = "";
        bool bAdd12 = (AMPMSelection == 1);

        switch (HrSelection)
        {
            case 0:
                retval = (bAdd12) ? "13:" : "01:";
                break;
            case 1:
                retval = (bAdd12) ? "14:" : "02:";
                break;
            case 2:
                retval = (bAdd12) ? "15:" : "03:";
                break;
            case 3:
                retval = (bAdd12) ? "16:" : "04:";
                break;
            case 4:
                retval = (bAdd12) ? "17:" : "05:";
                break;
            case 5:
                retval = (bAdd12) ? "18:" : "06:";
                break;
            case 6:
                retval = (bAdd12) ? "19:" : "07:";
                break;
            case 7:
                retval = (bAdd12) ? "20:" : "08:";
                break;
            case 8:
                retval = (bAdd12) ? "21:" : "09:";
                break;
            case 9:
                retval = (bAdd12) ? "22:" : "10:";
                break;
            case 10:
                retval = (bAdd12) ? "23:" : "11:";
                break;
            case 11:
                retval = (bAdd12) ? "12:" : "00:";
                break;
            default:
                retval = "00:";
                break;
        }

        switch (MinSelection)
        {
            case 0:
                retval += "00:00";
                break;
            case 1:
                retval += "10:00";
                break;
            case 2:
                retval += "20:00";
                break;
            case 3:
                retval += "30:00";
                break;
            case 4:
                retval += "40:00";
                break;
            case 5:
                retval += "50:00";
                break;
            default:
                retval += "00:00";
                break;
        }
        return retval;
    }

    private MigrationOptions GetMigOptions()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            MigrationOptions migOpts = new MigrationOptions();


            // Read options from OptionsViewModel
            OptionsViewModel ovm = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);

            // Set itemFolderFlags
            ItemsAndFoldersOptions itemFolderFlags = ItemsAndFoldersOptions.None;
            if (ovm.ImportCalendarOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Calendar;

            if (ovm.ImportTaskOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Tasks;

            if (ovm.ImportContactOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Contacts;

            if (ovm.ImportMailOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Mail;

            if (ovm.ImportSentOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Sent;

            if (ovm.ImportDeletedItemOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.DeletedItems;

            if (ovm.ImportJunkOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Junk;

            if (ovm.ImportRuleOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.Rules;

            if (ovm.ImportOOOOptions)
                itemFolderFlags = itemFolderFlags | ItemsAndFoldersOptions.OOO;

            // and write to migOpts
            migOpts.ItemsAndFolders     = itemFolderFlags;
            migOpts.DateFilterItem      = ovm.DateFilterItem;
            migOpts.MigrateOnOrAfterDate= (ovm.IsOnOrAfter)      ? ovm.MigrateONRAfter : null;
            migOpts.MessageSizeFilter   = (ovm.IsMaxMessageSize) ? ovm.MaxMessageSize  : null;
            migOpts.SkipFolders         = (ovm.IsSkipFolders)    ? ovm.FoldersToSkip   : null;
            migOpts.SkipPrevMigrated    = ovm.IsSkipPrevMigratedItems;
            migOpts.MaxErrorCnt         = ovm.MaxErrorCount;

            migOpts.SpecialCharRep      = ovm.SpecialCharReplace;
            migOpts.MaxRetries          = ovm.MaxRetries;
            migOpts.IsPublicFolders     = ovm.IsPublicFolders;
            migOpts.IsZDesktop          = ovm.IsZDesktop;

            return migOpts;
        }
    }

    private string FormatTheLastMsg(MigrationFolder lastFolder, bool isOOOorRules)
    // FBS 4/13/12 -- rewrite to fix bug 71048
    {
        string retval = (isOOOorRules) ? "1 of 1" : ""; // if it's Out of Office or Rules, just say 1 of 1
        if (!isOOOorRules)
            retval = String.Format("{0} of {1}", lastFolder.NumFolderItemsMigrated, lastFolder.NumFolderItemsToMigrate);

        return retval;
    }

    private void FormatGlobalMsg(AccountResultsViewModel ar)
    {
        string msgG = String.Format("{0} of {1} ({2}%)", ar.NumAccountItemsToMigrate, ar.NumAccountItemsToMigrate, 100);
        ar.GlobalAcctProgressMsg = msgG;
    }

    private ObservableCollection<CosInfo> coslist = new ObservableCollection<CosInfo>();
    public ObservableCollection<CosInfo> CosList 
    {
        get { return coslist; }
        set { coslist = value; }
    }

    // Background thread stuff
    private void worker_DoWork(object sender, System.ComponentModel.DoWorkEventArgs e)
    {
        // ===========================================================================
        // MAIN ENTRY POINT
        // ===========================================================================
        // TESTPOINT_MIGRATION_MIGRATION_START Main entry point for start of migration
        eventArglist.Add(e);

        int num = (int)e.Argument;
        Log.info(" In DoWork for threads for user number", num);

        // --------------------------------------------------------------------
        // Instantiate MigrationAccount to represent the account being migrated
        // --------------------------------------------------------------------
        MigrationAccount MyAcct = new MigrationAccount();

        UsersViewModel usersViewModel = ((UsersViewModel)ViewModelPtrs[(int)ViewType.USERS]);

        // ------------------------------------------------
        // Create a thread-specific log for this thread
        // ------------------------------------------------
        AccountResultsViewModel accountResultsViewModel = ((AccountResultsViewModel)ViewModelPtrs[(int)ViewType.RESULTS]);    // main one
        string accountname = accountResultsViewModel.AccountResultsList[num].AccountName;

        // --------------------------------------------------
        // Compute account name and ID
        // --------------------------------------------------
        string accountid = "";

        ConfigViewModelS eparams = ((ConfigViewModelS)ViewModelPtrs[(int)ViewType.SVRSRC]);
        MyAcct.ProfileName = eparams.OutlookProfile;

        if (isServer)
        {
            // Server
            accountname = accountname + "@" + usersViewModel.ZimbraDomain;
            accountid = usersViewModel.UsersList[num].Username;
            int idx = accountid.IndexOf("@");
            if (idx != -1)                      // domain would be Exchange domain, not Zimbra domain
                accountid = accountid.Substring(0, idx);
        }
        else
        {
            if (!isDesktop)
            {
                // PST
                ConfigViewModelU   sourceModel = ((ConfigViewModelU)    ViewModelPtrs[(int)ViewType.USRSRC]);
                ConfigViewModelUDest destModel = ((ConfigViewModelUDest)ViewModelPtrs[(int)ViewType.USRDEST]);

                accountname = ZimbraValues.GetZimbraValues().AccountName;//accountname + "@" + destModel.ZimbraServerHostName;
                accountid = (sourceModel.IspST) ? sourceModel.PSTFile : sourceModel.ProfileList[sourceModel.CurrentProfileSelection];
            }
            else
            {
                // Zimbra Desktop
                ConfigViewModelZU  sourceModel = ((ConfigViewModelZU)   ViewModelPtrs[(int)ViewType.ZDSRC]);
                ConfigViewModelUDest destModel = ((ConfigViewModelUDest)ViewModelPtrs[(int)ViewType.USRDEST]);

                accountname = ZimbraValues.GetZimbraValues().AccountName;//accountname + "@" + destModel.ZimbraServerHostName;
                accountid = (sourceModel.IspST) ? sourceModel.PSTFile : sourceModel.ProfileList[sourceModel.CurrentProfileSelection];
            }
        }


        // ---------------------------------------------------------
        // Compute log filename suffix to show src and dest accounts
        // ---------------------------------------------------------
        // Src
        string sSrc = Path.GetFileNameWithoutExtension(accountid);

        // Dest 
        string AccountNameMinusDomain = accountname;
        int i = accountname.IndexOf("@"); // strip domain if there is one
        if (i != -1)
            AccountNameMinusDomain = accountname.Substring(0, i);

        // ---------------------------------------------------------
        // Create this thread's log
        // ---------------------------------------------------------
        Log.StartNewLogfileForThisThread("["+sSrc+"] to ["+AccountNameMinusDomain+"]"); // If you change this, also change OpenLogFile()



        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {

            // --------------------------------------------------
            // Write to MyAcct
            // --------------------------------------------------
            MyAcct.AccountName = accountname;
            MyAcct.AccountID   = accountid;
            MyAcct.AccountNum  = num;

            // Init the delegate. Calls to MyAcct.OnChanged() will now call MigrationAccount_OnChanged()
            MyAcct.OnChanged   += new MigrationObjectEventHandler(MigrationAccount_OnChanged);


            // -----------------------------------------------------
            // Instantiate folder to be migrated an store in Account
            // -----------------------------------------------------
            MigrationFolder MyFolder = new MigrationFolder();
            MyFolder.AccountNum = num;
            MyFolder.OnChanged += new MigrationObjectEventHandler(MigrationFolder_OnChanged);
            MyAcct.CurrFolder = MyFolder; // overwrites the one set in MigrationAccount ctor. Why?





            // ===============================================
            // Migrate this account 
            // ===============================================
            MigrationOptions migOpts = GetMigOptions();
            bool doRulesAndOOO = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]).OEnableRulesAndOOO;
            CSMigrationWrapper mw = ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]).mw;
            Log.info("Starting migration for account ", MyAcct.AccountNum);

            mw.StartMigration(MyAcct, migOpts, isServer, m_isPreview, doRulesAndOOO);

       




            // ------------------------------------------------
            // special case to format last user progress message
            // ------------------------------------------------
            int count = accountResultsViewModel.AccountResultsList[num].FolderResultsList.Count;
            if (count > 0)
            {
                if (!m_isPreview)
                {
                    string lastmsg = accountResultsViewModel.AccountResultsList[num].FolderResultsList[count - 1].FolderProgress;
                    int len = lastmsg.Length;
                    bool isOOOorRules = ((MyFolder.FolderView == "OOO") || (MyFolder.FolderView == "All Rules"));
                    accountResultsViewModel.AccountResultsList[num].FolderResultsList[count - 1].FolderProgress = FormatTheLastMsg(MyFolder, isOOOorRules);
                    accountResultsViewModel.AccountResultsList[num].PBValue = 100;  // to make sure

                    //if (accountResultsViewModel.AccountResultsList[num].CurrentItemNum != accountResultsViewModel.AccountResultsList[num].NumAccountItemsToMigrate)
                      //  FormatGlobalMsg(accountResultsViewModel.AccountResultsList[num]);
                }
                else
                {   // For preview, take the "foldername (n items)" message we constructed, extract the n, and make "Total n"
                    string msg = "";
                    string lastmsg = accountResultsViewModel.AccountResultsList[num].AccountStatus;
                    int idxParen = lastmsg.IndexOf("(");
                    int idxItems = lastmsg.IndexOf("items");
                    if ((idxParen != -1) && (idxItems != -1))
                    {
                        int numLen = idxItems - idxParen - 2;   // for the paren and the space
                        string numStr = lastmsg.Substring(idxParen + 1, numLen);
                        msg = "Total: " + numStr;
                        accountResultsViewModel.AccountResultsList[num].FolderResultsList[count - 1].FolderProgress = (msg.Length > 0) ? msg : "";
                    }
                }
            }
            /////

            if (!m_isPreview)
            {
                int tnum = GetThreadNum(MyAcct.AccountNum);
                //Log.info(" in worker_RunWorkerCompleted  for ThreadNum : " + tnum);

                if ((!(MyAcct.IsValid)) && (MyAcct.NumAccountErrs > 0))
                {
                    Log.info(" in DOWORK -- Migration failed for usernum: " + MyAcct.AccountNum + " and threadnum" + tnum);
                    accountResultsViewModel.AccountResultsList[num].AccountStatus = MyAcct.LastProblemInfo.Msg;//"Migration Failed - Invalid account";
                    accountResultsViewModel.AccountResultsList[num].AccountProcessed = "Failed";
                }
                else
                {
                    if ((!(MyAcct.IsCompletedMigration)) && (MyAcct.NumAccountErrs > 0))
                    {
                        Log.info("Migration Incomplete for usernum " + MyAcct.AccountNum + " and threadnum " + tnum);
                        accountResultsViewModel.AccountResultsList[num].AccountStatus = "Migration Incomplete - Please Re-Run Migration";
                        accountResultsViewModel.AccountResultsList[num].AccountProcessed = "Incomplete";
                    }
                    else
                    {
                        Log.info("Migration completed for usernum " + MyAcct.AccountNum + " and threadnum " + tnum);
                        accountResultsViewModel.AccountResultsList[num].AccountStatus = "Migration complete";
                        accountResultsViewModel.AccountResultsList[num].AccountProcessed = "Complete";
                    }
                }
            }
            else
                accountResultsViewModel.AccountResultsList[num].AccountStatus = String.Format("Total items: {0}", accountResultsViewModel.AccountResultsList[num].NumAccountItemsToMigrate);


            if (migOpts.IsMaintainenceMode)
            {
                accountResultsViewModel.AccountResultsList[num].AccountStatus = "Migration Incomplete";
                accountResultsViewModel.AccountResultsList[num].AccountProcessed = "InComplete";
            }

        } // using LogBlock
    }

    private void worker_ProgressChanged(object sender, System.ComponentModel.ProgressChangedEventArgs e)
    {
    }
    
    private void worker_RunWorkerCompleted(object sender, System.ComponentModel.RunWorkerCompletedEventArgs e)
    //
    // Called when a worker thread has finished migrating an account, or on cancel
    //
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // Check for cancel
            AccountResultsViewModel accountResultsViewModel = ((AccountResultsViewModel)ViewModelPtrs[(int)ViewType.RESULTS]);
            if (e.Cancelled)
            {
                Log.info("Thread cancelled  ");
                for (int i = 0; i < accountResultsViewModel.AccountResultsList.Count; i++)  // hate to set them all, but do it for now
                {
                    accountResultsViewModel.AccountResultsList[i].AccountStatus = "Migration canceled";
                }
                accountResultsViewModel.AccountStatus = "Migration canceled";
            }
            else
            if (e.Error != null)
            {
                accountResultsViewModel.AccountStatus = "Migration exception: " + e.Error.ToString();
                Log.err("Thread errored with message  ", e.Error.ToString());
            }
            else
            {
                if (!m_isPreview)
                {
                    accountResultsViewModel.AccountStatus = "Migration complete";
                    Log.info("Migration completed. Overflow count: ", overflowList.Count);
                    if (overflowList.Count == 0)
                    {
                        Log.info("All accounts migrated");
                        SchedList.Clear();
                        UsersViewModel usersViewModel = ((UsersViewModel)ViewModelPtrs[(int)ViewType.USERS]);
                        usersViewModel.UsersList.Clear();
                    }
                }
                accountResultsViewModel.EnableStop = false;
            }

            if (!m_isPreview)
            {
                Log.info("Migration completed ");
                m_isComplete = true;
            }

            //  EnablePreview = EnableMigrate = !m_isComplete;
            if (overflowList.Count > 0)
            {
                Log.info("Migration completed overflowcount is > 0", overflowList.Count);
                int usernum = overflowList[0];
                Log.info("Check availablethread for usernum ", usernum);
                int threadnum = AvailableThread();
                if (threadnum != -1)
                {
                    Log.info("Got availablethread for usernum" + usernum + " and threadnum" + threadnum);
                    bgwlist[threadnum].usernum = usernum;
                    bgwlist[threadnum].RunWorkerAsync(usernum);
                }

                Log.info("Remove the user who got a thread");
                overflowList.RemoveAt(0);
            }
        }
    }

    public int GetThreadNum(int usernum)
    {
        int ct = bgwlist.Count;
        for (int i = 0; i < ct; i++)
        {
            if (bgwlist[i].usernum == usernum)
            {
                return bgwlist[i].threadnum;
            }
        }
        return -1;
    }

    // =========================================================================================================================================
    // OnChanged handlers
    // =========================================================================================================================================

    public void MigrationAccount_OnChanged(object sender, MigrationObjectEventArgs e)
    //
    // Called when a property of MigrationAccount changes -> update associated UI
    //
    // sender will be the MigrationAccount whose prop changed
    // e contains useful info like the prop name, and the old and new prop values
    //
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // ================================================
            // Logging
            // ================================================
            string sOld = e.OldValue!=null?e.OldValue.ToString():"null";
            string sNew = e.NewValue!=null?e.NewValue.ToString():"null";
            Log.trace(e.PropertyName + ": " + sOld + "->" + sNew);

            // ================================================
            // Sender is the account for which to update the UI
            // ================================================
            MigrationAccount a = (MigrationAccount)sender;

            // ================================================
            // Find the account's views
            // ================================================

            // ViewModel for the entire results page
            AccountResultsViewModel accountResultsViewModel = ((AccountResultsViewModel)ViewModelPtrs[(int)ViewType.RESULTS]);

            // Drill down into the view model for this account
            AccountResultsViewModel ar = accountResultsViewModel.AccountResultsList[a.AccountNum];

            // ============================================================
            // Update associated view prop (which the UI is watching)
            // ============================================================
            bool bUpdateProgressColumn = false;

            if (e.PropertyName == "NumAccountItemsToMigrate")
            {
                // NumAccountItems column
                ar.NumAccountItemsToMigrate = a.NumAccountItemsToMigrate;

                // Processed column
                ar.AccountProcessed = "0 of " + a.NumAccountItemsToMigrate.ToString();
            }
            else
            if (e.PropertyName == "NumAccountItemsMigrated")
            {
                // Migrated column
                ar.NumAccountItemsMigrated = a.NumAccountItemsMigrated;

                // Processed column
                bUpdateProgressColumn = true;
            }
            else
            if (e.PropertyName == "NumAccountErrs")
            {
                // Errs column
                ar.NumAccountErrs = a.NumAccountErrs;

                // Add row to problems listbox
                ar.AccountProblemsList.Add(a.LastProblemInfo);

                // Cancel if hit max errors threshold
                OptionsViewModel ovm = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);
                if (ovm.MaxErrorCount > 0)
                {
                    if (ar.NumAccountErrs > ovm.MaxErrorCount)
                    {
                        for (int i = 0; i < this.BGWList.Count; i++)
                            this.BGWList[i].CancelAsync();
                    }
                }
            }
            else
            if (e.PropertyName == "NumAccountWarns")
            {
                // Warns column
                ar.NumAccountWarns = a.NumAccountWarns;

                // Add row to problems listbox
                ar.AccountProblemsList.Add(a.LastProblemInfo);
            }
            else
            if (e.PropertyName == "NumAccountSkipsFilter")
            {
                ar.NumAccountSkipsFilter = a.NumAccountSkipsFilter;
                bUpdateProgressColumn = true;
            }
            else
            if (e.PropertyName == "NumAccountSkipsHistory")
            {
                ar.NumAccountSkipsHistory = a.NumAccountSkipsHistory;
                bUpdateProgressColumn = true;
            }
            else
            if (e.PropertyName == "AccountStatus")
            {
                ar.AccountStatus = a.AccountStatus;
                System.Threading.Thread.Sleep(50); // Give UI time to show it
            }

            // ---------------------------------
            // Progress column, and progress bar
            // ---------------------------------
            if (bUpdateProgressColumn)
            {
                int nProcessed = a.NumAccountItemsMigrated + a.NumAccountSkipsFilter + a.NumAccountSkipsHistory;
                ar.AccountProcessed = String.Format("{0} of {1}", nProcessed, a.NumAccountItemsToMigrate);

                // Update progress bar
                ar.PBValue = (int)Math.Round(((Decimal)nProcessed / (Decimal)ar.NumAccountItemsToMigrate) * 100);

                // Message below progress bar
                string msgG = String.Format("{0} of {1} ({2}%)", nProcessed, ar.NumAccountItemsToMigrate, ar.PBValue);
                ar.GlobalAcctProgressMsg = msgG;

                int tnum = GetThreadNum(a.AccountNum);
                bgwlist[tnum].ReportProgress(ar.PBValue, a.AccountNum);
            }

            // ---------------------------------
            // AccountElapsed
            // ---------------------------------
            TimeSpan elapsed = TimeSpan.FromSeconds((Environment.TickCount-a.AccountStartTicks)/1000);
            ar.AccountElapsed = elapsed.ToString(@"hh\:mm\:ss");

            // ---------------------------------
            // Read/Write ratio
            // ---------------------------------
            if (a.AccountReadMillisecs+a.AccountWriteMillisecs>0)
            {
                Int32 nPercentRead = (Int32)((Decimal)(a.AccountReadMillisecs*100)/(a.AccountReadMillisecs+a.AccountWriteMillisecs));
                ar.AccountElapsedReadWriteRatio = nPercentRead.ToString()+"% "+(100-nPercentRead).ToString()+"%";
            }
        }
    }

    public void MigrationFolder_OnChanged(object sender, MigrationObjectEventArgs e)
    //
    // Called when a property of MigrationFolder changes -> update associated UI
    //
    // sender will be the MigrationFolder whose prop changed
    // e contains useful info like the prop name, and the old and new prop values
    //
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // ================================================
            // Logging
            // ================================================
            string sOld = e.OldValue!=null?e.OldValue.ToString():"null";
            string sNew = e.NewValue!=null?e.NewValue.ToString():"null";
            Log.trace(e.PropertyName + ": " + sOld + "->" + sNew);

            // ================================================
            // Sender is folder for which to update UI
            // ================================================
            MigrationFolder f = (MigrationFolder)sender;

            // ================================================
            // Find the folder's views
            // ================================================

            // ViewModel for the entire results page
            AccountResultsViewModel accountResultsViewModel = ((AccountResultsViewModel)ViewModelPtrs[(int)ViewType.RESULTS]);

            // Drill down into the view model for this account
            AccountResultsViewModel ar = accountResultsViewModel.AccountResultsList[f.AccountNum];

            // ================================================
            // Early exit if cancelling
            // ================================================
            int tnum = GetThreadNum(f.AccountNum);
            if (bgwlist[tnum].CancellationPending)
            {
                eventArglist[f.AccountNum].Cancel = true;
                return;
            }

            // =================================
            // FolderName
            // =================================
            // Folder name is set in StartMigration(). Causes a new row to be added
            // to the folders grid 
            if (e.PropertyName == "FolderName")
            {
                if ((e.NewValue != null) && (e.NewValue != e.OldValue))
                {
                    // New folder -> add new row to the FOLDERS grid
                    ar.FolderResultsList.Add(new FolderResultsViewModel(f.FolderName));

                    System.Threading.Thread.Sleep(50);    // to see the message
                }
            }

            // ============================================================
            // Update associated view prop (which the UI is watching)
            // ============================================================
            if (f.FolderName != null) // else theres no row for it
            {
                // ---------------------------------------------------------
                // Get view model for current folder
                // ---------------------------------------------------------
                // Assumes the current folder is the last one in the list!
                int count = ar.FolderResultsList.Count;
                FolderResultsViewModel fr = ar.FolderResultsList[count - 1];

                bool bUpdateProgressColumn = false;

                if (e.PropertyName == "NumFolderItemsMigrated")
                {
                    fr.NumFolderItemsMigrated = (int)f.NumFolderItemsMigrated;
                    bUpdateProgressColumn = true;
                }
                else
                if (e.PropertyName == "NumFolderSkipsFilter")
                {
                    fr.NumFolderSkipsFilter = f.NumFolderSkipsFilter;
                    bUpdateProgressColumn = true;
                }
                else
                if (e.PropertyName == "NumFolderSkipsHistory")
                {
                    fr.NumFolderSkipsHistory = f.NumFolderSkipsHistory;
                    bUpdateProgressColumn = true;
                }
                else
                if (e.PropertyName == "NumFolderErrs")
                    fr.NumFolderErrs = f.NumFolderErrs;
                else
                if (e.PropertyName == "NumFolderWarns")
                    fr.NumFolderWarns = f.NumFolderWarns;
                else
                if (e.PropertyName == "FolderItemMillisecsMin")
                    fr.FolderItemMillisecsMin = f.FolderItemMillisecsMin;
                else
                if (e.PropertyName == "FolderItemMillisecsAvg")
                    fr.FolderItemMillisecsAvg = f.FolderItemMillisecsAvg;
                else
                if (e.PropertyName == "FolderItemMillisecsMax")
                    fr.FolderItemMillisecsMax = f.FolderItemMillisecsMax;
                else
                if (e.PropertyName == "FolderStatus")
                {
                    fr.FolderProgress = f.FolderStatus;
                    System.Threading.Thread.Sleep(50);
                }

                // ---------------------------------
                // FolderProgress
                // ---------------------------------
                if (bUpdateProgressColumn && (e.NewValue.ToString() != "0"))
                {
                    // Update progress column in folders grid "x of y"
                    long nProcessed = f.NumFolderItemsMigrated + f.NumFolderSkipsFilter + f.NumFolderSkipsHistory;
                    if (f.NumFolderSkipsFilter + f.NumFolderSkipsHistory == f.NumFolderItemsToMigrate)
                        fr.FolderProgress = "Skipped";
                    else
                        fr.FolderProgress = String.Format("{0} of {1}", nProcessed.ToString(), f.NumFolderItemsToMigrate);
                }

                // ---------------------------------
                // FolderElapsed
                // ---------------------------------
                int nSecs = (Environment.TickCount-f.FolderStartTicks)/1000;
                if (nSecs > 0)
                {
                    TimeSpan elapsed = TimeSpan.FromSeconds(nSecs);
                    fr.FolderElapsed = elapsed.ToString(@"hh\:mm\:ss");
                }
                else
                    fr.FolderElapsed = "";

                // ---------------------------------
                // FolderReadWriteRatio
                // ---------------------------------
                if (f.FolderReadMillisecs+f.FolderWriteMillisecs>0)
                {
                    Int32 nPercentRead = (Int32)((Decimal)(f.FolderReadMillisecs*100)/(f.FolderReadMillisecs+f.FolderWriteMillisecs));
                    fr.FolderElapsedReadWriteRatio = nPercentRead.ToString()+"% "+(100-nPercentRead).ToString()+"%";
                }
                else
                    fr.FolderElapsedReadWriteRatio = "";
            }
        }
    }
}
}
