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

using System.ComponentModel;
using System.Threading;
using System;
using System.IO;
using CssLib;


namespace ZimbraMigrationConsole
{
    [System.ComponentModel.DesignerCategory("Code")]
    class Account : BackgroundWorker
    {
        string m_AccountName;
        public string AccountName
        {
            get { return m_AccountName; }
            set { m_AccountName = value; }
        }

        string m_AccountStatus;
        public string AccountStatus
        {
            get { return m_AccountStatus; }
            set { m_AccountStatus = value; }
        }

        string m_AccountOptions;
        public string AccountOptions
        {
            get { return m_AccountOptions; }
            set { m_AccountOptions = value; }
        }

        long highestPercentageReached;
        public long HighestPercentageReached
        {
            get { return highestPercentageReached; }
            set { highestPercentageReached = value; }
        }

        int Numoferrors;
        public int NumofErrors
        {
            get { return Numoferrors; }
            set { Numoferrors = value; }
        }

        string m_AccountID;
        public string AccountID
        {
            get { return m_AccountID; }
            set { m_AccountID = value; }
        }

        CssLib.CSMigrationWrapper TestObj;

        MVVM.Model.Users Currentuser;
        CssLib.MigrationOptions Mailoptions;
        int num = 0;
        // int Numoferrors = 0;
        int NumofWarns = 0;

        bool serverMigration = true;

        CountdownEvent countdown;
        public CountdownEvent Countdown
        {
            get { return countdown; }
            set { countdown = value; }
        }

        private bool _shouldCancel;
        public void RequestCancel()
        {
            _shouldCancel = true;
        }

        private volatile bool _shouldStop;
        public void RequestStop()
        {
            _shouldStop = true;
        }

        public void StartMigration(System.Collections.Generic.List<MVVM.Model.Users> userlist,
                                   string Domainname,
                                   CssLib.MigrationOptions MailOptions,
                                   CountdownEvent countdown,
                                   object wrapper,
                                   int maxThreads = 2,
                                   bool ServerMigrationflag = true,
                                   string pstaccountname = "",
                                   string pstfile = "")
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                int number = 0;

                if (ServerMigrationflag)
                {
                    // InitializeBackgoundWorkers();

                    // Create an array containing maxThreads accounts
                    Account[] AccountArray = new Account[maxThreads];
                    for (int j = 0; j < maxThreads; j++)
                    {
                        AccountArray[j] = new Account();
                        AccountArray[j].WorkerSupportsCancellation = true;
                    }

                    // Allocate accounts to threads. Choose the first unused thread.
                    for (int f = 0; f < userlist.Count; f++)
                    {
                        if (userlist[f].IsProvisioned)
                        {
                            Account myAccount = new Account();
                            string uname = (userlist[f].MappedName != "") ? userlist[f].MappedName : userlist[f].UserName;

                            myAccount.AccountName = uname + "@" + Domainname;// AcctName;
                            myAccount.AccountID = userlist[f].UserName;
                            myAccount.Countdown = countdown;
                            myAccount.NumofErrors = 0;

                            Currentuser = new MVVM.Model.Users();
                            Currentuser.UserName = userlist[f].UserName;
                            myAccount.Currentuser = Currentuser;

                            myAccount.TestObj = (CssLib.CSMigrationWrapper)wrapper;

                            myAccount.serverMigration = ServerMigrationflag;
                            myAccount.Mailoptions = MailOptions;

                            number = number + 1;
                            myAccount.num = number;
                            myAccount.WorkerSupportsCancellation = true;


                            // choose the first unused thread.  
                            bool accountAllocatedToThread = false;
                            while ((!accountAllocatedToThread) && (!_shouldStop))
                            {
                                for (int threadNum = 0; threadNum < maxThreads; threadNum++)
                                {
                                    // This thread is available     
                                    if (!AccountArray[threadNum].IsBusy)
                                    {
                                        // System.Console.WriteLine("Starting worker thread: " + threadNum + "account" + myAccount.AccountName);

                                        AccountArray[threadNum] = myAccount;
                                        AccountArray[threadNum].DoWork += new DoWorkEventHandler(accountToMigrate_DoWork);
                                        AccountArray[threadNum].RunWorkerCompleted += new RunWorkerCompletedEventHandler(accountToMigrate_RunWorkerCompleted);
                                        //AccountArray[threadNum].ProgressChanged += new ProgressChangedEventHandler(accountToMigrate_ProgressChanged);
                                        AccountArray[threadNum].WorkerReportsProgress = true;
                                        AccountArray[threadNum].WorkerSupportsCancellation = true;
                                        AccountArray[threadNum].RunWorkerAsync(myAccount);

                                        accountAllocatedToThread = true;
                                        break;
                                    }
                                }

                                // If all threads are being used, sleep awhile before checking again  
                                if (!accountAllocatedToThread)
                                    Thread.Sleep(500);
                            }

                            if (_shouldStop)
                            {
                                for (int i = 0; i < maxThreads; i++)
                                {
                                    System.Console.WriteLine("cancelling in main callback");
                                    countdown.Signal();
                                    AccountArray[i].CancelAsync();
                                }
                            }

                        } // if (userlist[f].IsProvisioned)
                        else
                        {
                            string msg = userlist[f].MappedName + " is not provisioned. Exiting migration for '" + userlist[f].UserName + "'\n";
                            System.Console.WriteLine(msg);
                            countdown.Signal();
                        }
                    }
                }
                else
                {
                    Account myAccount = new Account();
                    myAccount.AccountName = pstaccountname;// AcctName;
                    myAccount.AccountID = pstfile;
                    myAccount.Countdown = countdown;
                    Currentuser = new MVVM.Model.Users();
                    Currentuser.UserName = pstfile;
                    myAccount.Currentuser = Currentuser;

                    myAccount.serverMigration = ServerMigrationflag;
                    myAccount.TestObj = (CssLib.CSMigrationWrapper)wrapper;
                    number = number + 1;
                    myAccount.num = number;

                    myAccount.DoWork += new DoWorkEventHandler(accountToMigrate_DoWork);
                    myAccount.RunWorkerCompleted += new RunWorkerCompletedEventHandler(accountToMigrate_RunWorkerCompleted);
                    //myAccount.ProgressChanged += new ProgressChangedEventHandler(accountToMigrate_ProgressChanged);
                    myAccount.WorkerReportsProgress = true;
                    myAccount.WorkerSupportsCancellation = true;
                    myAccount.Mailoptions = MailOptions;
                    myAccount.RunWorkerAsync(myAccount);
                }
            }
        }

        private void accountToMigrate_DoWork(object sender, System.ComponentModel.DoWorkEventArgs e)
        {
            Account accnt = e.Argument as Account;

            // ------------------------------------------------
            // Create a thread-specific log for this thread
            // ------------------------------------------------
            string AccountName = accnt.AccountName;
            int i = AccountName.IndexOf("@");
            if (i != -1)
                AccountName = AccountName.Substring(0, i);

            // Similar code in ScheduleViewModel.cs::worker_DoWork
            // Log suffix  [src accnt] to [targ accnt]
            string sSrc = Path.GetFileNameWithoutExtension(accnt.AccountID);
            Log.StartNewLogfileForThisThread("["+sSrc+"] to ["+AccountName+"]"); // If you change this, also change OpenLogFile()


            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name)) // LogBlock not available here...it is now logging moved to ZimbraAPI
            {

                // Get argument from DoWorkEventArgs argument.  Can use any type here with cast   
                // int myProcessArguments = (int)e.Argument;      
                // "ProcessFile" is the name of my method that does the main work.  Replace with your own method!       
                // Can return reulsts from this method, i.e. a status (OK, FAIL etc)   
                //e.Result = ProcessFile(myProcessArgument); 
                BackgroundWorker worker = sender as BackgroundWorker;

                // Assign the result of the computation
                // to the Result property of the DoWorkEventArgs
                // object. This is will be available to the
                // RunWorkerCompleted eventhandler.
                // e.Result = Accounworker, e);
                //int num = 0;

                //while (!_shouldStop)
                {
                    accnt.WorkerSupportsCancellation = true;

                    CssLib.MigrationAccount MyAcct = new CssLib.MigrationAccount();
                    MyAcct.AccountName      = accnt.AccountName;
                    MyAcct.AccountID        = accnt.AccountID;
                    MyAcct.MaxErrorCount    = accnt.Mailoptions.MaxErrorCnt;
                    MyAcct.AccountNum       = accnt.num;
                    MyAcct.OnChanged        += new CssLib.MigrationObjectEventHandler(Acct_OnChanged);

                    CssLib.MigrationFolder MyFolder = new CssLib.MigrationFolder();


                    //MyFolder.AccountNum = argumentTest.num;
                    int idx = MyAcct.AccountName.IndexOf("@");
                    if (idx != -1)
                        MyFolder.AccountID = MyAcct.AccountName.Substring(0, idx);


                    // MyFolder.AccountID = argumentTest.AccountID;
                    MyFolder.OnChanged += new CssLib.MigrationObjectEventHandler(Folder_OnChanged);

                    MyAcct.CurrFolder = MyFolder;


                    CssLib.CSMigrationWrapper mw = accnt.TestObj;

                    if (accnt.CancellationPending)
                        e.Cancel = true;
                    else
                    {
                        // TestObj.Migrate(MigrateOptions);

                        mw.StartMigration(MyAcct, accnt.Mailoptions, accnt.serverMigration);
                        if (accnt.Mailoptions.IsMaintainenceMode)
                            System.Console.WriteLine("Mailbox is in Maintainence mode. Try again Later");

                        accnt.NumofErrors = MyAcct.NumAccountErrs;

                        //Last item migration
                        string sAcccountID = "[" + MyFolder.AccountID.ToString() + "]";
                        string msg2 = sAcccountID.PadRight(25, ' ')  + MyFolder.FolderName.PadRight(25, ' ') + "{0,5:#} of {1,5:#} migrated successfully";
                        string msgF = String.Format(msg2, MyFolder.NumFolderItemsMigrated, MyFolder.NumFolderItemsToMigrate);
                        System.Console.WriteLine(msgF);
                    }
                }

                if (_shouldStop)
                {
                    worker.CancelAsync();
                    accnt.CancelAsync();
                    accnt.countdown.Signal();
                    e.Cancel = true;
                }

                if (_shouldCancel)
                {
                    //worker.CancelAsync();
                    // argumentTest.CancelAsync();
                    // argumentTest.NumofErrors = NumofErrors;
                    accnt.CancelAsync();
                }
            }
        }

        public void Acct_OnChanged(object sender, CssLib.MigrationObjectEventArgs e)     // See similar code in ScheduleViewModel......can we dedupe?
        {
            // while (!_shouldStop)
            {
                //string msg = "";
                CssLib.MigrationAccount a = (CssLib.MigrationAccount)sender;

                if (e.PropertyName == "NumAccountItemsToMigrate")
                {
                    /*System.Console.WriteLine();
                    ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Yellow, "TotalItems to Migrate For UserAccount   " + a.AccountID.ToString() + " is " + e.NewValue.ToString());
                    System.Console.WriteLine();*/
                    // a.TotalItems = Convert.ToInt32(e.NewValue);

                    Currentuser.StatusMessage = "Total Items to Migrate For UserAccount   " + a.AccountID + " is " + e.NewValue.ToString();
                    /*System.Console.WriteLine();
                    System.Console.WriteLine();*/

                }
                else
                if (e.PropertyName == "NumAccountErrs")
                {
                    this.NumofErrors = (int)a.NumAccountErrs + 1;      // this happens first
                    System.Console.WriteLine();
                    if (a.MaxErrorCount > 0)
                    {
                        if (this.NumofErrors >= a.MaxErrorCount)
                        {
                            System.Console.WriteLine();
                            if (a.LastProblemInfo != null)
                            {
                                System.Console.WriteLine();
                                string mesg = a.LastProblemInfo.Msg;
                                System.Console.WriteLine(mesg);
                                System.Console.WriteLine();

                            }
                            System.Console.WriteLine();
                            Currentuser.StatusMessage = "Total Errors For UserAccount " + a.AccountID.ToString() + " are " + this.NumofErrors.ToString();
                            System.Console.WriteLine(Currentuser.StatusMessage);
                            string Messg = "Migration For UserAccount Cancelled because MaxErrorCount has been reached";
                            // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, "Migration For UserAccount    Cancelled");
                            System.Console.WriteLine(Messg);
                            System.Console.WriteLine();
                            System.Console.WriteLine();
                            //this.countdown.Signal();

                            //this.RequestCancel();

                            // this.RequestStop();
                            //Countdown.Signal();
                            CancelAsync();
                            //Thread.CurrentThread.Abort();
                        }

                    }

                    if (a.LastProblemInfo != null)
                    {
                        System.Console.WriteLine();
                        string mesg = a.LastProblemInfo.Msg;
                        System.Console.WriteLine(mesg);
                        System.Console.WriteLine();

                    }

                    /* ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Yellow, "TotalErrors For UserAccount   " + a.AccountID.ToString() + Numoferrors.ToString());*/
                    //donot use progressutil we want to have consistent logging.

                    /*Currentuser.StatusMessage = "Total Errors For UserAccount " + a.AccountID.ToString() + "are" + NumofErrors.ToString();
                    System.Console.WriteLine(Currentuser.StatusMessage);
                    System.Console.WriteLine();
                    System.Console.WriteLine();*/

                }
                else 
                if (e.PropertyName == "NumAccountWarns")
                {
                    NumofWarns = (int)a.NumAccountWarns + 1;
                    System.Console.WriteLine();

                    /* ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Yellow, "TotalWarnings For UserAccount   " + a.AccountID.ToString() + NumofWarns.ToString());*/
                    //donot use progressutil we want to have consistent logging.

                    Currentuser.StatusMessage = "Total Warnings For UserAccount   " + a.AccountID.ToString() + NumofWarns.ToString();
                    System.Console.WriteLine(Currentuser.StatusMessage);
                    System.Console.WriteLine();

                }
                else
                {
                    /* System.Console.WriteLine();
                     ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Yellow, "TotalItems to Migrate For UserAccount   " + a.AccountName.ToString() + " is " + e.NewValue.ToString());
                     System.Console.WriteLine();/*
                     msg = "Begin {0} Migration";
                     string msgF = String.Format(msg, a.AccountName);
                     System.Console.WriteLine(msgF);*/

                }
            }
        }

        public void Folder_OnChanged(object sender, CssLib.MigrationObjectEventArgs e)     // See similar code in ScheduleViewModel......can we dedupe?
        {
            //while (!_shouldStop)
            {
                CssLib.MigrationFolder f = (CssLib.MigrationFolder)sender;
                string sAcccountID = "[" + f.AccountID.ToString() + "]";

                if (e.PropertyName == "NumFolderItemsMigrated")
                {
                    if (f.FolderName != null)
                    {
                        if (e.NewValue.ToString() != "0")
                        {
                            if (f.NumFolderItemsMigrated > 0)
                            {
                                string msg1 = sAcccountID.PadRight(25, ' ') + f.FolderName.PadRight(25, ' ') + "{0,5:#} of {1,5:#} migrated successfully";
                                string msgF = String.Format(msg1, f.NumFolderItemsMigrated, f.NumFolderItemsToMigrate);
                                System.Console.WriteLine(msgF);
                            }
                        }
                    }
                }

                if (e.PropertyName == "NumFolderItemsToMigrate")      // finish up with the last folder DCB I don't think it goes down this code path
                {
                    if (f.FolderName != null)
                    {
                        if (f.NumFolderItemsMigrated > 0)
                        {
                            string msg2 = sAcccountID.PadRight(25, ' ') + f.FolderName.PadRight(25, ' ') + "{0,5:#} of {1,5:#} migrated successfully";
                            string msgF = String.Format(msg2, f.NumFolderItemsMigrated, f.NumFolderItemsToMigrate);

                            System.Console.WriteLine(msgF);
                        }
                    }
                }

                if (e.PropertyName == "FolderName")
                {
                    if (e.NewValue != null)
                    {
                        string folderName = e.NewValue.ToString();

                        string msg3 = sAcccountID.PadRight(25, ' ') +"Starting migration of folder '{0}'";
                        string msgF = String.Format(msg3, folderName);
                        System.Console.WriteLine("");
                        System.Console.WriteLine(msgF);
                        System.Console.WriteLine("");
                    }

                    if (_shouldStop)
                    {
                        System.Console.WriteLine();
                        System.Console.WriteLine();
                        string Messg = "Migration For UserAccount   Cancelled";
                        // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, "Migration For UserAccount    Cancelled");
                        System.Console.WriteLine(Messg);
                        System.Console.WriteLine();
                        System.Console.WriteLine();
                        Countdown.Signal();
                        CancelAsync();
                        Thread.CurrentThread.Abort();
                    }
                }
            }

        }

        /*private void accountToMigrate_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            Account argumentTest = sender as Account;
            argumentTest.AccountStatus = e.ProgressPercentage.ToString();
            if (e.ProgressPercentage == 10)
            {
                // System.Console.WriteLine("Migrating messages For UserAccount   " + AccountName.ToString());
                System.Console.WriteLine();
                ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Yellow, "Migrating messages For UserAccount   " + argumentTest.AccountName.ToString());
                Currentuser.StatusMessage = "Migrating messages For UserAccount   " + argumentTest.AccountName.ToString();
                System.Console.WriteLine();
                System.Console.WriteLine();
            }
            if (e.ProgressPercentage == 40)
            {
                // System.Console.WriteLine("Migrating appointments For UserAccount   " + AccountName.ToString());
                System.Console.WriteLine();
                ProgressUtil.RenderConsoleProgress(40, '\u2591', ConsoleColor.Green, "Migrating appointments For UserAccount   " + argumentTest.AccountName.ToString());
                Currentuser.StatusMessage = "Migrating appointments For UserAccount   " + argumentTest.AccountName.ToString();
                System.Console.WriteLine();
                System.Console.WriteLine();
            }
            if (e.ProgressPercentage == 60)
            {
                // System.Console.WriteLine("Migrating contacts For UserAccount   " + AccountName.ToString());
                System.Console.WriteLine();
                ProgressUtil.RenderConsoleProgress(60, '\u2591', ConsoleColor.Yellow, "Migrating contacts For UserAccount   " + argumentTest.AccountName.ToString());
                Currentuser.StatusMessage = "Migrating Contacts For UserAccount   " + argumentTest.AccountName.ToString();
                System.Console.WriteLine();
                System.Console.WriteLine();
            }
            if (e.ProgressPercentage == 80)
            {
                // System.Console.WriteLine("Migrating rules For UserAccount   " + AccountName.ToString());
                System.Console.WriteLine();
                ProgressUtil.RenderConsoleProgress(60, '\u2591', ConsoleColor.Green, "Migrating Rules For UserAccount   " + argumentTest.AccountName.ToString());
                Currentuser.StatusMessage = "Migrating  Rules For UserAccount   " + argumentTest.AccountName.ToString();
                System.Console.WriteLine();
                System.Console.WriteLine();
            }
        }*/

        private void accountToMigrate_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            Account argumentTest = sender as Account;

            // First, handle the case where an exception was thrown.
            if (e.Error != null)
            {
                argumentTest.AccountStatus = (e.Error.Message);
                if (argumentTest.Countdown.CurrentCount != 0)
                    argumentTest.Countdown.Signal();
            }
            else
            if (e.Cancelled)
            {
                // Next, handle the case where the user canceled the operation.
                // Note that due to a race condition in the DoWork event handler, the Cancelled
                // flag may not have been set, even though CancelAsync was called.
                argumentTest.AccountStatus = "Canceled";
                System.Console.WriteLine();
                System.Console.WriteLine();
                string msg = "Migration For UserAccount   " + argumentTest.AccountName + " Cancelled";
                // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, "Migration For UserAccount   " + argumentTest.AccountName + " Cancelled");
                System.Console.WriteLine(msg);
                System.Console.WriteLine();
                System.Console.WriteLine();

                if (argumentTest.Countdown.CurrentCount != 0)
                    argumentTest.Countdown.Signal();
            }
            else
            {
                // Finally, handle the case where the operation succeeded.

                //signal the countdown event for the main thread exit.
                {
                    System.Console.WriteLine();
                    System.Console.WriteLine();

                    string s = "";
                    int idx = argumentTest.AccountName.IndexOf("@");
                    if (idx != -1)
                        s = argumentTest.AccountName.Substring(0, idx);

                    string sAcccountID = "[" + s + "]";
                    string mesg = sAcccountID.PadRight(25, ' ') + "Migration Complete. Total error(s): " + argumentTest.NumofErrors.ToString();
                    System.Console.WriteLine(mesg);

                    // ProgressUtil.RenderConsoleProgress(30, '\u2591', ConsoleColor.Red, "TotalErrors For UserAccount   " + argumentTest.AccountName + " are" + Numoferrors.ToString());

                    System.Console.WriteLine();
                    System.Console.WriteLine();

                    argumentTest.Countdown.Signal();
                }
                argumentTest.AccountStatus = "Completed";        // e.Result.ToString();
            }
        }

    }
}

