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
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Windows.Input;
using System.Windows;
using System.Xml.Linq;
using System.Xml.Serialization;
using System.Xml;
using System;

namespace MVVM.ViewModel
{
    public class OptionsViewModel : BaseViewModel
    {
        public OptionsViewModel()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock("OptionsViewModel.OptionsViewModel"))
            {
                this.SaveCommand = new ActionCommand(this.Save, () => true);
                this.BackCommand = new ActionCommand(this.Back, () => true);
                this.NextCommand = new ActionCommand(this.Next, () => true);
                //        this.PubFoldersCommand = new ActionCommand(this.PublicFolders , () => true);
            }
        }

        public ICommand LoadCommand
        {
            get;
            private set;
        }

        public ICommand SaveCommand
        {
            get;
            private set;
        }

        public ICommand BackCommand
        {
            get;
            private set;
        }

        public ICommand NextCommand
        {
            get;
            private set;
        }

        public ICommand PubFoldersCommand
        {
            get;
            private set;
        }

        public void LoadConfig(Config config)
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                // ========================
                // Import Options
                // ========================
                ImportMailOptions           = config.ImportOptions.Mail;
                ImportCalendarOptions       = config.ImportOptions.Calendar;
                ImportContactOptions        = config.ImportOptions.Contacts;
                ImportDeletedItemOptions    = config.ImportOptions.DeletedItems;
                ImportJunkOptions           = config.ImportOptions.Junk;
                ImportTaskOptions           = config.ImportOptions.Tasks;
                ImportSentOptions           = config.ImportOptions.Sent;
                ImportRuleOptions           = config.ImportOptions.Rules;
                ImportOOOOptions            = config.ImportOptions.OOO;
                SetNextState();

                // ========================
                // Advanced Import Options
                // ========================
                MigrateONRAfter         = config.AdvancedImportOptions.MigrateOnOrAfter.ToShortDateString();
                IsOnOrAfter             = config.AdvancedImportOptions.IsOnOrAfter;

                MaxMessageSize          = config.AdvancedImportOptions.MaxMessageSize;

                DateFilterItem          = config.AdvancedImportOptions.DateFilterItem;

                IsSkipPrevMigratedItems = config.AdvancedImportOptions.IsSkipPrevMigratedItems;
                IsMaxMessageSize        = config.AdvancedImportOptions.IsMaxMessageSize;
                IsSkipFolders           = config.AdvancedImportOptions.IsSkipFolders;
                SpecialCharReplace      = config.AdvancedImportOptions.SpecialCharReplace;
                MaxRetries              = config.AdvancedImportOptions.MaxRetries;


                // DCB_BUG_104613 If cfg file says "Public Folders Migration", override this if this is not a server mig
                bool bIsServerMig = true;
                IntroViewModel ivm = ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]);
                if (ivm != null)
                    bIsServerMig = ivm.rbServerMigration;
                IsPublicFolders         =    bIsServerMig && config.AdvancedImportOptions.IsPublicFolders;

                IsZDesktop              = config.AdvancedImportOptions.IsZDesktop;

                // ========================
                // General Options
                // ========================
                if (config.GeneralOptions != null)  // so old config files will work
                {
                    // Having read loglevel from the config XML, write it to the global log level
                    Log.SetLogLevel(Log.LogLevelStr2Enum(config.GeneralOptions.LogLevel));

                    MaxThreadCount = config.GeneralOptions.MaxThreadCount;
                    MaxErrorCount = config.GeneralOptions.MaxErrorCount;
                }

                // ========================
                // ========================
                string returnval = "";
                returnval = ConvertToCSV(config.AdvancedImportOptions.FoldersToSkip, ",");
                FoldersToSkip = returnval;

                // ========================
                // User provision
                // ========================
                savedDomain = config.UserProvision.DestinationDomain;
            }
        }

        private void PublicFolders()
        {

            /*  EnablePopButtons = false;

               CSMigrationWrapper mw = ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]).mw;

               mw.StartMigration(MyAcct, migOpts, isServer, migOpts.LogLevelEnum, m_isPreview, doRulesAndOOO);
              /* string[] users = mw.GetListFromObjectPicker();
               if(users != null)
               {
               // FBS rewrite -- bug 71646 -- 3/26/12
               for (int i = 0; i < users.Length; i++)
               {
                   string[] tokens = users[i].Split('~');
                   if (tokens.Length < 5)
                   {
                       MessageBox.Show("Object picker returned insufficient data", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                       EnablePopButtons = true;
                       return;
                   }
                   string uname = "", displayname = "", givenname = "", sn = "", zfp = "";
                   for (int j = 0; j < tokens.Length; j += 5)
                   {
                       uname = tokens.GetValue(j).ToString();
                       displayname = tokens.GetValue(j + 1).ToString();
                       givenname = tokens.GetValue(j + 2).ToString();
                       sn = tokens.GetValue(j + 3).ToString();
                       zfp = tokens.GetValue(j + 4).ToString();
                   }

                   if (uname.IndexOf("@") != -1)
                   {
                       uname = uname.Substring(0, uname.IndexOf("@"));
                   }

                   UsersViewModel uvm;

                   if (uname.CompareTo(displayname) == 0)
                   {
                        uvm = new UsersViewModel(displayname, uname);
                   }
                   else
                   {
                       uvm = new UsersViewModel(uname, uname);
                   }
                   uvm.AddOPInfo(new ObjectPickerInfo(displayname, givenname, sn, zfp));
                   UsersList.Add(uvm);

                   ScheduleViewModel scheduleViewModel =
                       ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]);

                   scheduleViewModel.SchedList.Add(new SchedUser(Username, false));
                   scheduleViewModel.EnableMigrate = (scheduleViewModel.SchedList.Count > 0);
                   scheduleViewModel.EnablePreview = scheduleViewModel.EnableMigrate;
                   EnableNext = (UsersList.Count > 0);
               }
           }
               EnablePopButtons = true;
           }

           */
        }

        private void Back()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                if (isDesktop)
                    lb.SelectedIndex = 1;
                else
                    lb.SelectedIndex = 2;
            }
        }

        private void Next()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                bool mmsError = false;
                if (IsMaxMessageSize)
                {
                    if (MaxMessageSize.Length == 0)
                    {
                        mmsError = true;
                    }
                    else
                    {
                        try
                        {
                            int msf = Int32.Parse(MaxMessageSize);
                        }
                        catch (Exception)
                        {
                            mmsError = true;
                        }
                    }
                    if (mmsError)
                    {
                        MessageBox.Show("Please enter an integer value for maximum message size", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }
                }

                if (isServer && IsPublicFolders)
                {
                    lb.SelectedIndex = 5;
                }
                else
                {
                    if (isServer)
                    {
                        lb.SelectedIndex = 4;
                    }
                    else
                    {
                        ConfigViewModelUDest configViewModelUDest   = ((ConfigViewModelUDest)ViewModelPtrs[(int)ViewType.USRDEST]);
                        UsersViewModel       usersViewModel         = ((UsersViewModel)      ViewModelPtrs[(int)ViewType.USERS]);
                        ScheduleViewModel    scheduleViewModel      = ((ScheduleViewModel)   ViewModelPtrs[(int)ViewType.SCHED]);

                        string name = configViewModelUDest.ZimbraUser;
                        usersViewModel.UsersList.Add(new UsersViewModel(name, ""));

                        scheduleViewModel.DoMigrate(false);
                    }
                }
            }
        }

        public bool ImportMailOptions
        {
            get { return m_config.ImportOptions.Mail; }
            set
            {
                if (value == m_config.ImportOptions.Mail)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.Mail = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportMailOptions"));
                    SetNextState();
                }
            }
        }

        public bool ImportTaskOptions
        {
            get { return m_config.ImportOptions.Tasks; }
            set
            {
                if (value == m_config.ImportOptions.Tasks)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.Tasks = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportTaskOptions"));
                    SetNextState();
                }
            }
        }

        public bool ImportCalendarOptions
        {
            get { return m_config.ImportOptions.Calendar; }
            set
            {
                if (value == m_config.ImportOptions.Calendar)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.Calendar = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportCalendarOptions"));
                    SetNextState();
                }
            }
        }

        public bool ImportContactOptions
        {
            get { return m_config.ImportOptions.Contacts; }
            set
            {
                if (value == m_config.ImportOptions.Contacts)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.Contacts = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportContactOptions"));
                    SetNextState();
                }
            }
        }

        public bool ImportDeletedItemOptions
        {
            get { return m_config.ImportOptions.DeletedItems; }
            set
            {
                if (value == m_config.ImportOptions.DeletedItems)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.DeletedItems = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportDeletedItemOptions"));
                }
            }
        }

        public bool ImportJunkOptions
        {
            get { return m_config.ImportOptions.Junk; }
            set
            {
                if (value == m_config.ImportOptions.Junk)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.Junk = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportJunkOptions"));
                }
            }
        }

        public bool ImportSentOptions
        {
            get { return m_config.ImportOptions.Sent; }
            set
            {
                if (value == m_config.ImportOptions.Sent)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.Sent = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportSentOptions"));
                }
            }
        }

        public bool ImportRuleOptions
        {
            get { return m_config.ImportOptions.Rules; }
            set
            {
                if (value == m_config.ImportOptions.Rules)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.Rules = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportRuleOptions"));
                    SetNextState();
                }
            }
        }

        public bool ImportOOOOptions
        {
            get { return m_config.ImportOptions.OOO; }
            set
            {
                if (value == m_config.ImportOptions.OOO)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.ImportOptions.OOO = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportOOOOptions"));
                    SetNextState();
                }
            }
        }

        private string importnextbuttoncontent;
        public string ImportNextButtonContent
        {
            get { return importnextbuttoncontent; }
            set
            {
                if (value == importnextbuttoncontent)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    importnextbuttoncontent = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("ImportNextButtonContent"));
                }
            }
        }

        private string dateFormatLabelContent;
        public string DateFormatLabelContent
        {
            get { return dateFormatLabelContent; }
            set
            {
                if (value == dateFormatLabelContent)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    dateFormatLabelContent = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("DateFormatLabelContent"));
                }
            }
        }

        public string MigrateONRAfter
        {
            get { return m_config.AdvancedImportOptions.MigrateOnOrAfter.ToShortDateString(); }
            set
            {
                if (value == m_config.AdvancedImportOptions.MigrateOnOrAfter.ToShortDateString())
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    try
                    {
                        m_config.AdvancedImportOptions.MigrateOnOrAfter = Convert.ToDateTime(value);
                    }
                    catch (Exception)
                    {
                        MessageBox.Show("Please enter a valid date in the indicated format", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }
                    OnPropertyChanged(new PropertyChangedEventArgs("MigrateONRAfter"));
                }
            }
        }

        public bool IsOnOrAfter
        {
            get { return m_config.AdvancedImportOptions.IsOnOrAfter; }
            set
            {
                if (value == m_config.AdvancedImportOptions.IsOnOrAfter)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.IsOnOrAfter = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("IsOnOrAfter"));
                }
            }
        }

        public string MaxMessageSize
        {
            get { return m_config.AdvancedImportOptions.MaxMessageSize; }
            set
            {
                if (value == m_config.AdvancedImportOptions.MaxMessageSize)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.MaxMessageSize = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("MaxMessageSize"));
                }
            }
        }

        public string DateFilterItem
        {
            get { return m_config.AdvancedImportOptions.DateFilterItem; }
            set
            {
                if (value == m_config.AdvancedImportOptions.DateFilterItem)
                    return;
                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.DateFilterItem = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("DateFilterItem"));
                }
            }
        }

        public bool IsMaxMessageSize
        {
            get { return m_config.AdvancedImportOptions.IsMaxMessageSize; }
            set
            {
                if (value == m_config.AdvancedImportOptions.IsMaxMessageSize)
                    return;
                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.IsMaxMessageSize = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("IsMaxMessageSize"));
                }
            }
        }

        public bool IsSkipPrevMigratedItems
        {
            get { return m_config.AdvancedImportOptions.IsSkipPrevMigratedItems; }
            set
            {
                if (value == m_config.AdvancedImportOptions.IsSkipPrevMigratedItems)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.IsSkipPrevMigratedItems = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("IsSkipPrevMigratedItems"));
                }
            }
        }

        public string SpecialCharReplace
        {
            get { return m_config.AdvancedImportOptions.SpecialCharReplace; }
            set
            {
                if (value == m_config.AdvancedImportOptions.SpecialCharReplace)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.SpecialCharReplace = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("SpecialCharReplace"));
                }
            }
        }

        public bool IsPublicFolders
        {
            get { return m_config.AdvancedImportOptions.IsPublicFolders; }
            set
            {
                if (value == m_config.AdvancedImportOptions.IsPublicFolders)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.IsPublicFolders = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("IsPublicFolders"));
                }
            }
        }

        public bool IsZDesktop
        {
            get { return m_config.AdvancedImportOptions.IsZDesktop; }
            set
            {
                if (value == m_config.AdvancedImportOptions.IsZDesktop)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.IsZDesktop = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("IsZDesktop"));
                }
            }
        }

        public string CSVDelimiter
        {
            get { return m_config.AdvancedImportOptions.CSVDelimiter; }
            set
            {
                if (value == m_config.AdvancedImportOptions.CSVDelimiter)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.CSVDelimiter = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("CSVDelimiter"));
                }
            }
        }

        public Int32 MaxRetries
        {
            get { return m_config.AdvancedImportOptions.MaxRetries; }
            set
            {
                if (value == m_config.AdvancedImportOptions.MaxRetries)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.MaxRetries = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("MaxRetries"));
                }
            }
        }

        private string placeholderstring;
        public string Placeholderstring
        {
            get { return placeholderstring; }
            set
            {
                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    placeholderstring = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("Placeholderstring"));
                }
            }
        }

        public string FoldersToSkip
        {
            get
            {
                return placeholderstring;
            }
            set
            {
                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    placeholderstring = value;
                    if (value != null)
                    {
                        string[] nameTokens = value.Split(',');
                        int numFolders = nameTokens.Length;
                        m_config.AdvancedImportOptions.FoldersToSkip = new Folder[numFolders];
                        int i;
                        for (i = 0; i < numFolders; i++)
                        {
                            Folder tempUser = new Folder();
                            tempUser.FolderName = nameTokens.GetValue(i).ToString();
                            m_config.AdvancedImportOptions.FoldersToSkip.SetValue(tempUser, i);
                        }
                    }
                    OnPropertyChanged(new PropertyChangedEventArgs("FoldersToSkip"));
                }
            }
        }

        public bool IsSkipFolders
        {
            get { return m_config.AdvancedImportOptions.IsSkipFolders; }
            set
            {
                if (value == m_config.AdvancedImportOptions.IsSkipFolders)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.AdvancedImportOptions.IsSkipFolders = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("IsSkipFolders"));
                }
            }
        }

        public Int32 MaxThreadCount
        {
            get { return m_config.GeneralOptions.MaxThreadCount; }
            set
            {
                if (value == m_config.GeneralOptions.MaxThreadCount)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.GeneralOptions.MaxThreadCount = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("MaxThreadCount"));
                }
            }
        }

        public Int32 MaxErrorCount
        {
            get { return m_config.GeneralOptions.MaxErrorCount; }
            set
            {
                if (value == m_config.GeneralOptions.MaxErrorCount)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    m_config.GeneralOptions.MaxErrorCount = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("MaxErrorCount"));
                }
            }
        }

        private bool oenableNext;
        public bool OEnableNext
        {
            get { return oenableNext; }
            set
            {
                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    if (!isServer)
                    {
                        ScheduleViewModel scheduleViewModel = ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]);
                        oenableNext = scheduleViewModel.IsComplete() ? false : value;
                    }
                    else
                    {
                        oenableNext = value;
                    }
                    OnPropertyChanged(new PropertyChangedEventArgs("OEnableNext"));
                }
            }
        }

        private bool oenableRulesAndOOO;
        public bool OEnableRulesAndOOO
        {
            get { return oenableRulesAndOOO; }
            set
            {
                if (value == oenableRulesAndOOO)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    oenableRulesAndOOO = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("OEnableRulesAndOOO"));
                }
            }
        }

        private bool oenablePF;
        public bool OEnablePF
        {
            get { return oenablePF; }
            set
            {
                if (value == oenablePF)
                    return;

                using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
                {
                    oenablePF = value;
                    OnPropertyChanged(new PropertyChangedEventArgs("OEnablePF"));
                }
            }
        }

        public string ConvertToCSV(Folder[] objectarray, string delimiter)
        {
            if (objectarray == null)
                return null;

            string result;

            System.Text.StringBuilder sb = new System.Text.StringBuilder();
            foreach (Folder i in objectarray)
            {
                if (i == null)
                    continue;
                sb.Append(i.FolderName);
                sb.Append(delimiter);
            }

            result = sb.ToString();
            if (result.Length > 0)
                return result.Substring(0, result.Length - delimiter.Length);
            else
                return "";
        }

        private void SetNextState()
        {
            OEnableNext = ImportMailOptions || ImportCalendarOptions ||
                          ImportContactOptions || ImportTaskOptions ||
                          ImportRuleOptions || ImportOOOOptions;
        }
    }
}
