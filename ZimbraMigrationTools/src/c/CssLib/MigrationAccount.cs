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
using System;
using System.Collections.Generic;


// ==============================================================================================
// DCB BINDING NOTES
// ==============================================================================================

// Two main classes are defined in here
// - MigrationAccount
// - MigrationFolder

// Many of the properties in these classes are hooked up to UI elements.
// For example, when MigrationAccount.NumAccountErrs is modified, it updates
// the Errs column for that account on the accounts grid.

// How does this data binding work?
// -------------------------------

// To find UI definition, look in "MVVM->View" folder in Solution Explorer

// The accounts grid is defined in ResultsView.xaml. It looks like this:
//
//    <ListView ... Name="lstAccountResults"... ItemsSource="{Binding AccountResultsList} ... >
//
// -> so the binding tells the list to display items taken from "AccountResultsList"
//
// AccountResultsList defined as:
//
//    public ObservableCollection<AccountResultsViewModel> AccountResultsList 
//
// i.e. its a collection of AccountResultsViewModel
//
// So we know what populates the list rows. What about the columns?
//
// Looking closer at the ListView, we see it contains:
//
// <ListView.View> 
//     <GridView>
//         <GridView.Columns>
//              <GridViewColumn ... "DisplayMemberBinding="{Binding Path=  AccountName            }"  ... > 
//              <GridViewColumn ... "DisplayMemberBinding="{Binding Path=  AccountStatus          }"  ... > 
//              <GridViewColumn ... "DisplayMemberBinding="{Binding Path=  AccountProcessed       }"  ...> 
//              <GridViewColumn ... "DisplayMemberBinding="{Binding Path=  NumAccountItemsMigrated}"  ... >
//              etc
//
// So each "GridViewColumn"'s binding path tells which property of AccountResultsViewModel to display
//
// When a property of MigrationAccount is changed, how is the corresponding prop in AccountResultsViewModel updated?
// -----------------------------------------------------------------------------------------------------------------
//
// The Set() of a typical property looks like this (see below in this file):
//
//        public int NumAccountItemsToMigrate
//        {
//            get { return numAccountItemsToMigrate; }
//            set
//            {
//                if (value == numAccountItemsToMigrate)
//                    return;
//
//                object oldval = numAccountItemsToMigrate;
//                numAccountItemsToMigrate = value;
//                if (OnChanged != null)
//                    OnChanged(this, new MigrationObjectEventArgs("NumAccountItemsToMigrate", oldval, value));   <- OnChanged
//            }
//        }
//
//
// OnChanged is a delegate, and looks like this:
//
//    public void MigrationAccount_OnChanged(object sender, MigrationObjectEventArgs e)
//    {
//            // ================================================
//            // Logging
//            // ================================================
//            string sOld = e.OldValue!=null?e.OldValue.ToString():"null";
//            string sNew = e.NewValue!=null?e.NewValue.ToString():"null";
//            Log.trace(e.PropertyName + ": " + sOld + "->" + sNew);
//
//            // ================================================
//            // Sender is the account for which to update the UI
//            // ================================================
//            MigrationAccount a = (MigrationAccount)sender;
//
//            // ================================================
//            // Find the account's views
//            // ================================================
//            AccountResultsViewModel accountResultsViewModel = ((AccountResultsViewModel)ViewModelPtrs[(int)ViewType.RESULTS]);
//
//            AccountResultsViewModel ar = accountResultsViewModel.AccountResultsList[a.AccountNum];
//
//
//            // ---------------------------------
//            // NumAccountItemsToMigrate
//            // ---------------------------------
//            if (e.PropertyName == "NumAccountItemsToMigrate")
//            {
//                // NumAccountItems column
//                ar.NumAccountItemsToMigrate = a.NumAccountItemsToMigrate;    <- pass NumAccountItemsToMigrate to ar
//
//                // Processed column
//                ar.AccountProcessed = "0 of " + a.NumAccountItemsToMigrate.ToString();
//            }
//
//
// 
//
//

namespace CssLib
{

    // ==================================================================================
    // MigrationAccount
    // ==================================================================================
    // Represents a single account being migrated
    // Instantiated in worker_DoWork() for ZMT and accountToMigrate_DoWork() for ZMC
    public class MigrationAccount
    {
        public MigrationAccount()
        {
            // Dont know the account num yet
            AccountNum = -1;

            // Holds info about the folder currently being migrated
            CurrFolder = new MigrationFolder();

            // Track tags for this account
            TagDict = new Dictionary<string, string>();

            AccountStartTicks = Environment.TickCount;
        }

        // -----------------------------------------------------------------------------------------
        // Methods
        // -----------------------------------------------------------------------------------------
        public void AddProblem(string objectName, string theMsg, int msgType, ref bool bError)
        {
            LastProblemInfo = new ProblemInfo(this, objectName, theMsg, msgType, ref bError);

            if (bError)
            {
                NumAccountErrs++;
                CurrFolder.NumFolderErrs++;
            }
            else
            {
                NumAccountWarns++;
                CurrFolder.NumFolderWarns++;
            }
        }

        // -----------------------------------------------------------------------------------------
        // Members
        // -----------------------------------------------------------------------------------------
        public string ProfileName;
        public string AccountID;
        public MigrationFolder CurrFolder;
        public Dictionary<string, string> TagDict;

        // -----------------------------------------------------------------------------------------
        // Events
        // -----------------------------------------------------------------------------------------
        public event MigrationObjectEventHandler OnChanged;
        // Above is initialized in DoWork() so that a call to OnChanged() will call ScheduleViewModel.Acct_OnChanged()

        // -----------------------------------------------------------------------------------------
        // Properties
        // -----------------------------------------------------------------------------------------
        private string accountName;
        public string AccountName
        {
            get { return this.accountName; }
            set
            {
                if (value == accountName)
                    return;

                object oldval = accountName;
                accountName = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("AccountName", oldval, value));
            }
        }

        private int accountNum;
        public int AccountNum
        {
            get { return this.accountNum; }
            set
            {
                if (value == accountNum)
                    return;

                object oldval = accountNum;
                accountNum = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("AccountNum", oldval, value));
            }
        }


        // -----------------------------------------------------------------------------------------
        // Track various totals for the account
        // -----------------------------------------------------------------------------------------
        private int numAccountItemsToMigrate;
        public int NumAccountItemsToMigrate
        {
            get { return numAccountItemsToMigrate; }
            set
            {
                if (value == numAccountItemsToMigrate)
                    return;

                object oldval = numAccountItemsToMigrate;
                numAccountItemsToMigrate = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumAccountItemsToMigrate", oldval, value));
            }
        }

        private int numAccountItemsMigrated;
        public int NumAccountItemsMigrated
        {
            get { return numAccountItemsMigrated; }
            set
            {
                if (value == numAccountItemsMigrated)
                    return;

                object oldval = numAccountItemsMigrated;
                numAccountItemsMigrated = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumAccountItemsMigrated", oldval, value));
            }
        }

        private int numAccountErrs;
        public int NumAccountErrs
        {
            get { return numAccountErrs; }
            set
            {
                if (value == numAccountErrs)
                    return;

                object oldval = numAccountErrs;
                numAccountErrs = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumAccountErrs", oldval, value));
            }
        }

        private int numAccountWarns;
        public int NumAccountWarns
        {
            get { return numAccountWarns; }
            set
            {
                if (value == numAccountWarns)
                    return;

                object oldval = numAccountWarns;
                numAccountWarns = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumAccountWarns", oldval, value));
            }
        }

        private string accountStatus;
        public string AccountStatus
        {
            get { return accountStatus; }
            set
            {
                if (value == accountStatus)
                    return;

                object oldval = accountStatus;
                accountStatus = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("AccountStatus", oldval, value));
            }
        }

        private int numAccountSkipsFilter;
        public int NumAccountSkipsFilter
        {
            get { return numAccountSkipsFilter; }
            set
            {
                if (value == numAccountSkipsFilter)
                    return;

                object oldval = numAccountSkipsFilter;
                numAccountSkipsFilter = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumAccountSkipsFilter", oldval, value));
            }
        }

        private int numAccountSkipsHistory;
        public int NumAccountSkipsHistory
        {
            get { return numAccountSkipsHistory; }
            set
            {
                if (value == numAccountSkipsHistory)
                    return;

                object oldval = numAccountSkipsHistory;
                numAccountSkipsHistory = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumAccountSkipsHistory", oldval, value));
            }
        }

        private Int32 accountStartTicks;
        public Int32 AccountStartTicks
        {
            get { return accountStartTicks; }
            set
            {
                if (value == accountStartTicks)
                    return;

                object oldval = accountStartTicks;
                accountStartTicks = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("AccountStartTicks", oldval, value));
            }
        }

        private Int32 accountReadMillisecs;
        public Int32 AccountReadMillisecs
        {
            get { return accountReadMillisecs; }
            set
            {
                if (value == accountReadMillisecs)
                    return;

                object oldval = accountReadMillisecs;
                accountReadMillisecs = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("AccountReadMillisecs", oldval, value));
            }
        }

        private Int32 accountWriteMillisecs;
        public Int32 AccountWriteMillisecs
        {
            get { return accountWriteMillisecs; }
            set
            {
                if (value == accountWriteMillisecs)
                    return;

                object oldval = accountWriteMillisecs;
                accountWriteMillisecs = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("AccountWriteMillisecs", oldval, value));
            }
        }

        Int32 maxErrorCount;
        public Int32 MaxErrorCount
        {
            get { return maxErrorCount; }
            set { maxErrorCount = value; }
        }

        private ProblemInfo lastProblemInfo;
        public ProblemInfo LastProblemInfo
        {
            get { return lastProblemInfo; }
            set { lastProblemInfo = value; }
        }

        private bool IsValidAccount;
        public bool IsValid
        {
            get { return IsValidAccount; }
            set { IsValidAccount = value; }
        }

        private bool IsCompleted;
        public bool IsCompletedMigration
        {
            get { return IsCompleted; }
            set { IsCompleted = value; }
        }
    }


    // ==================================================================================
    // MigrationFolder
    // ==================================================================================
    // Holds info about the folder currently being migrated
    // Instantiated in worker_DoWork() and stored in MigrationAccount.CurrFolder
    public class MigrationFolder
    {
        public MigrationFolder() 
        { 
            FolderStartTicks = Environment.TickCount;
        }

        // -----------------------------------------------------------------------------------------
        // Events
        // -----------------------------------------------------------------------------------------
        public event MigrationObjectEventHandler OnChanged;
        // Above is initialized in DoWork() so that a call to OnChanged() will call ScheduleViewModel.Folder_OnChanged()

        // -----------------------------------------------------------------------------------------
        // Properties
        // -----------------------------------------------------------------------------------------
        private string accountID;
        public string AccountID
        {
            get { return this.accountID; }
            set { accountID = value; }
        }

        private int accountNum;
        public int AccountNum
        {
            get { return accountNum; }
            set
            {
                if (value == accountNum)
                    return;

                object oldval = accountNum;
                accountNum = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("AccountNum", oldval, value));
            }
        }

        private string folderName;
        public string FolderName
        {
            get { return folderName; }
            set
            {
                if (value == folderName)
                    return;

                object oldval = folderName;
                folderName = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("FolderName", oldval, value));
            }
        }

        private Int32 folderStartTicks;
        public Int32 FolderStartTicks
        {
            get { return folderStartTicks; }
            set
            {
                if (value == folderStartTicks)
                    return;

                object oldval = folderStartTicks;
                folderStartTicks = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("FolderStartTicks", oldval, value));
            }
        }

        private Int64 numFolderItemsToMigrate;
        public Int64 NumFolderItemsToMigrate
        {
            get { return numFolderItemsToMigrate; }
            set
            {
                if (value == numFolderItemsToMigrate)
                    return;

                object oldval = numFolderItemsToMigrate;
                numFolderItemsToMigrate = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumFolderItemsToMigrate", oldval, value));
            }
        }

        private Int64 numFolderItemsMigrated;
        public Int64 NumFolderItemsMigrated
        {
            get { return numFolderItemsMigrated; }
            set
            {
                if (value == numFolderItemsMigrated)
                    return;

                object oldval = numFolderItemsMigrated;
                numFolderItemsMigrated = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumFolderItemsMigrated", oldval, value));
            }
        }

        private string folderStatus;
        public string FolderStatus
        {
            get { return folderStatus; }
            set
            {
                if (value == folderStatus)
                    return;

                object oldval = folderStatus;
                folderStatus = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("FolderStatus", oldval, value));
            }
        }

        private int numFolderSkipsFilter;
        public int NumFolderSkipsFilter
        {
            get { return numFolderSkipsFilter; }
            set
            {
                if (value == numFolderSkipsFilter)
                    return;

                object oldval = numFolderSkipsFilter;
                numFolderSkipsFilter = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumFolderSkipsFilter", oldval, value));
            }
        }

        private int numFolderSkipsHistory;
        public int NumFolderSkipsHistory
        {
            get { return numFolderSkipsHistory; }
            set
            {
                if (value == numFolderSkipsHistory)
                    return;

                object oldval = numFolderSkipsHistory;
                numFolderSkipsHistory = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumFolderSkipsHistory", oldval, value));
            }
        }

        private int numFolderErrs;
        public int NumFolderErrs
        {
            get { return numFolderErrs; }
            set
            {
                if (value == numFolderErrs)
                    return;

                object oldval = numFolderErrs;
                numFolderErrs = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumFolderErrs", oldval, value));
            }
        }

        private int numFolderWarns;
        public int NumFolderWarns
        {
            get { return numFolderWarns; }
            set
            {
                if (value == numFolderWarns)
                    return;

                object oldval = numFolderWarns;
                numFolderWarns = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("NumFolderWarns", oldval, value));
            }
        }

        private int folderItemMillisecsMin;
        public int FolderItemMillisecsMin
        {
            get { return folderItemMillisecsMin; }
            set
            {
                if (value == folderItemMillisecsMin)
                    return;

                object oldval = folderItemMillisecsMin;
                folderItemMillisecsMin = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("FolderItemMillisecsMin", oldval, value));
            }
        }

        private int folderItemMillisecsAvg;
        public int FolderItemMillisecsAvg
        {
            get { return folderItemMillisecsAvg; }
            set
            {
                if (value == folderItemMillisecsAvg)
                    return;

                object oldval = folderItemMillisecsAvg;
                folderItemMillisecsAvg = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("FolderItemMillisecsAvg", oldval, value));
            }
        }

        private int folderItemMillisecsMax;
        public int FolderItemMillisecsMax
        {
            get { return folderItemMillisecsMax; }
            set
            {
                if (value == folderItemMillisecsMax)
                    return;

                object oldval = folderItemMillisecsMax;
                folderItemMillisecsMax = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("FolderItemMillisecsMax", oldval, value));
            }
        }

        private Int32 folderReadMillisecs;
        public Int32 FolderReadMillisecs
        {
            get { return folderReadMillisecs; }
            set
            {
                if (value == folderReadMillisecs)
                    return;

                object oldval = folderReadMillisecs;
                folderReadMillisecs = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("FolderReadMillisecs", oldval, value));
            }
        }

        private Int32 folderWriteMillisecs;
        public Int32 FolderWriteMillisecs
        {
            get { return folderWriteMillisecs; }
            set
            {
                if (value == folderWriteMillisecs)
                    return;

                object oldval = folderWriteMillisecs;
                folderWriteMillisecs = value;
                if (OnChanged != null)
                    OnChanged(this, new MigrationObjectEventArgs("FolderWriteMillisecs", oldval, value));
            }
        }

        private string folderView;
        public string FolderView
        {
            get { return folderView; }
            set { folderView = value; }
        }
    }

    // ==================================================================================
    // ProblemInfo
    // ==================================================================================
    public class ProblemInfo
    {
        public const int TYPE_ERR = 1;
        public const int TYPE_WARN = 2;

        public ProblemInfo(MigrationAccount Acct, string objectName, string theMsg, int msgType, ref bool bError)
        {
            bool bErrNotWarn = (msgType == ProblemInfo.TYPE_ERR);
            this.ObjectName = objectName;
            this.Msg = theMsg;
            this.MsgType = msgType;

            FormattedMsg = "["+DateTime.Now.ToString("MM-dd HH:mm:ss.fff") + "]    ";
            FormattedMsg += bErrNotWarn ? "Error" : "Warning";
            FormattedMsg += (" in ["+Acct.CurrFolder.FolderName+"\\");
            FormattedMsg += objectName + "]";
            FormattedMsg += ":     ";
            FormattedMsg += Msg;

            if (bErrNotWarn)
            {
                Log.err(FormattedMsg);
                bError = true;
            }
            else
            {
                Log.warn(FormattedMsg);
            }
        }

        // -----------------------------------------------------------------------------------------
        // Properties
        // -----------------------------------------------------------------------------------------
        public string ObjectName    { get; set; }
        public string Msg           { get; set; }
        public int    MsgType       { get; set; }
        public string FormattedMsg  { get; set; }
    }

    // ==================================================================================
    // MigrationObjectEventArgs
    // ==================================================================================
    public class MigrationObjectEventArgs : EventArgs
    {
        public MigrationObjectEventArgs(string propertyName, object oldValue, object newValue)
        {
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public string PropertyName
        {
            get { return this.propertyName; }
        }

        public object OldValue
        {
            get { return this.oldValue; }
        }

        public object NewValue
        {
            get { return this.newValue; }
        }

        private object newValue;
        private object oldValue;
        private string propertyName;
    }

    public delegate void MigrationObjectEventHandler(object sender, MigrationObjectEventArgs e);
}
