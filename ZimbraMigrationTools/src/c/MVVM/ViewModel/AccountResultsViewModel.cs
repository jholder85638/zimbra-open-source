/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2012, 2014, 2015, 2016 Synacor, Inc.
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
using System.IO;
using System.Threading;
using System.Windows.Input;
using System.Windows;
using System.Windows.Threading;
using System;

namespace MVVM.ViewModel
{
public class AccountResultsViewModel: BaseViewModel
{
    readonly AccountResults m_accountResults = new AccountResults();
    ScheduleViewModel m_scheduleViewModel;
    int m_accountnum;

    public AccountResultsViewModel(ScheduleViewModel scheduleViewModel, int accountNum, string accountName, bool enableStop)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("AccountResultsViewModel.AccountResultsViewModel"))
        {
            this.m_scheduleViewModel = scheduleViewModel;

            this.AccountName = accountName;
            this.m_accountnum = accountNum;
            this.EnableStop = enableStop;

            this.PBValue = 0;

            this.AccountStatus = "Queued";
            this.AccountProcessed = "";

            this.NumAccountItemsToMigrate = 0;
            this.NumAccountItemsMigrated = 0;

            this.NumAccountSkipsFilter = 0;
            this.NumAccountSkipsHistory = 0;

            this.NumAccountErrs = 0;
            this.NumAccountWarns = 0;

            this.GlobalAcctProgressMsg = "";

            this.SelectedTab = "";

            this.OpenLogFileCommand = new ActionCommand(this.OpenLogFile, () => true);
            this.StopCommand        = new ActionCommand(this.Stop, () => true);
            this.ExitAppCommand     = new ActionCommand(this.ExitApp, () => true);
        }
    }

    public ScheduleViewModel GetScheduleViewModel()
    {
        return m_scheduleViewModel;
    }

    public int GetAccountNum()
    {
        return m_accountnum;
    }

    // Commands
    public ICommand OpenLogFileCommand 
    {
        get;
        private set;
    }

    private void OpenLogFile()
    {
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string logpath = Path.GetTempPath() + "ZimbraMigration\\Logs\\";

            string logwildcard;
            if (SelectedTab == "Accounts" || SelectedTab == "")
                logwildcard = "*Migrate.log";
            else
                logwildcard = "*Migrate * to [" + SelectedTab + "].log";

            // Actual log will be prefixed with a datetime....and there might be several
            // so we need to search for most recent
            string[] files = Directory.GetFiles(logpath, logwildcard, SearchOption.TopDirectoryOnly);

            string filenameLatestLog = "";
            DateTime fileLatestCreateDate = new DateTime(1900, 1, 1, 0, 0, 0);
            for (int i = 0; i < files.Length; i++)
            {
                string file = files[i].ToString();

                DateTime fileCreatedDate = File.GetCreationTime(file);
                if (fileCreatedDate >= fileLatestCreateDate)
                {
                    // Found a newer log - use it
                    filenameLatestLog = file;
                    fileLatestCreateDate = fileCreatedDate;
                }
            } // for

            if (filenameLatestLog != "")
            {
                Log.info("Opening log " + filenameLatestLog);
                try
                {
                    Process.Start(filenameLatestLog);
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message);
                }
            }
            else
                Log.warn("Can't find logfile");
        }
    }

    public ICommand StopCommand 
    {
        get;
        private set;
    }

    private void Stop()
    {
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            for (int i = 0; i < m_scheduleViewModel.BGWList.Count; i++)
            {
                m_scheduleViewModel.BGWList[i].CancelAsync();
            }
            m_scheduleViewModel.EnableMigrate = true;
            m_scheduleViewModel.EnablePreview = true;

            // Don't uninitialize -- this should eventually set the static stop in csslib.
            // Go through each thread and stop each user?

            // CSMigrationwrapper mw = ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]).mw;
            // string ret = mw.UninitializeMailClient();

            EnableStop = !m_scheduleViewModel.EnableMigrate;
        }
    }

    public ICommand ExitAppCommand 
    {
        get;
        private set;
    }
    private void ExitApp()
    {
        Application.Current.Shutdown();
    }

    //

    private ObservableCollection<AccountResultsViewModel> accountResultsList = new ObservableCollection<AccountResultsViewModel>();
    public ObservableCollection<AccountResultsViewModel> AccountResultsList 
    {
        get { return accountResultsList; }
    }

    private ObservableCollection<ProblemInfo> accountProblemsList = new ObservableCollection<ProblemInfo>();
    public ObservableCollection<ProblemInfo> AccountProblemsList 
    {
        get { return accountProblemsList; }
    }

    private ObservableCollection<FolderResultsViewModel> folderResultsList = new ObservableCollection<FolderResultsViewModel>();
    public ObservableCollection<FolderResultsViewModel> FolderResultsList
    {
        get { return folderResultsList; }
    }

    public int PBValue 
    {
        get { return m_accountResults.PBValue; }
        set
        {
            if (value == m_accountResults.PBValue)
                return;
            m_accountResults.PBValue = value;
            OnPropertyChanged(new PropertyChangedEventArgs("PBValue"));
        }
    }

    public string AccountStatus 
    {
        get { return m_accountResults.AccountStatus; }
        set
        {
            if (value == m_accountResults.AccountStatus)
                return;
            m_accountResults.AccountStatus = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AccountStatus"));
        }
    }

    public string AccountName 
    {
        get { return m_accountResults.AccountName; }
        set
        {
            if (value == m_accountResults.AccountName)
                return;
            m_accountResults.AccountName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AccountName"));
        }
    }

    public int NumAccountItemsToMigrate 
    {
        get { return m_accountResults.NumAccountItemsToMigrate; }
        set
        {
            if (value == m_accountResults.NumAccountItemsToMigrate)
                return;
            m_accountResults.NumAccountItemsToMigrate = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumAccountItemsToMigrate"));
        }
    }

    public int NumAccountItemsMigrated
    {
        get { return m_accountResults.NumAccountItemsMigrated; }
        set
        {
            if (value == m_accountResults.NumAccountItemsMigrated)
                return;
            m_accountResults.NumAccountItemsMigrated = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumAccountItemsMigrated"));
        }
    }

    public string AccountProcessed 
    {
        get { return m_accountResults.AccountProcessed; }
        set
        {
            if (value == m_accountResults.AccountProcessed)
                return;
            m_accountResults.AccountProcessed = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AccountProcessed"));
        }
    }

    public string GlobalAcctProgressMsg
    {
        get { return m_accountResults.GlobalAcctProgressMsg; }
        set
        {
            if (value == m_accountResults.GlobalAcctProgressMsg)
                return;
            m_accountResults.GlobalAcctProgressMsg = value;
            OnPropertyChanged(new PropertyChangedEventArgs("GlobalAcctProgressMsg"));
        }
    }

    public int NumAccountSkipsFilter 
    {
        get { return m_accountResults.NumAccountSkipsFilter; }
        set
        {
            if (value == m_accountResults.NumAccountSkipsFilter)
                return;
            m_accountResults.NumAccountSkipsFilter = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumAccountSkipsFilter"));
        }
    }

    public int NumAccountSkipsHistory 
    {
        get { return m_accountResults.NumAccountSkipsHistory; }
        set
        {
            if (value == m_accountResults.NumAccountSkipsHistory)
                return;
            m_accountResults.NumAccountSkipsHistory = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumAccountSkipsHistory"));
        }
    }

    public int NumAccountErrs 
    {
        get { return m_accountResults.NumAccountErrs; }
        set
        {
            if (value == m_accountResults.NumAccountErrs)
                return;
            m_accountResults.NumAccountErrs = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumAccountErrs"));
        }
    }

    public int NumAccountWarns 
    {
        get { return m_accountResults.NumAccountWarns; }
        set
        {
            if (value == m_accountResults.NumAccountWarns)
                return;
            m_accountResults.NumAccountWarns = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumAccountWarns"));
        }
    }

    public string AccountElapsed 
    {
        get { return m_accountResults.AccountElapsed; }
        set
        {
            if (value == m_accountResults.AccountElapsed)
                return;
            m_accountResults.AccountElapsed = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AccountElapsed"));
        }
    }

    public string AccountElapsedReadWriteRatio 
    {
        get { return m_accountResults.AccountElapsedReadWriteRatio; }
        set
        {
            if (value == m_accountResults.AccountElapsedReadWriteRatio)
                return;
            m_accountResults.AccountElapsedReadWriteRatio = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AccountElapsedReadWriteRatio"));
        }
    }

    public int CurrentAccountSelection 
    {
        get { return m_accountResults.CurrentAccountSelection; }
        set
        {
            if (value == m_accountResults.CurrentAccountSelection)
                return;
            m_accountResults.CurrentAccountSelection = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CurrentAccountSelection"));
        }
    }

    private bool openLogFileEnabled;
    public bool OpenLogFileEnabled 
    {
        get { return openLogFileEnabled; }
        set
        {
            openLogFileEnabled = value;
            OnPropertyChanged(new PropertyChangedEventArgs("OpenLogFileEnabled"));
        }
    }

    private bool enableStop;
    public bool EnableStop 
    {
        get { return enableStop; }
        set
        {
            enableStop = value;
            OnPropertyChanged(new PropertyChangedEventArgs("EnableStop"));
        }
    }

    public string SelectedTab 
    {
        get { return m_accountResults.SelectedTab; }
        set
        {
            if (value == m_accountResults.SelectedTab)
                return;
            m_accountResults.SelectedTab = value;
            OnPropertyChanged(new PropertyChangedEventArgs("SelectedTab"));
        }
    }

}
}
