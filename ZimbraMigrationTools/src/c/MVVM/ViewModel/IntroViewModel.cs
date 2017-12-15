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

// Implements Migration Wizard Intro (first) page


using CssLib;
using MVVM.Model;
using Misc;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows;
using System;

namespace MVVM.ViewModel
{
public class IntroViewModel: BaseViewModel
{
    Intro m_intro = new Intro();
    public ObservableCollection<object> TheViews { get; set; }
    private bool m_isBrowser = false;

    private ConfigViewModelS        m_configViewModelS;
    private ConfigViewModelU        m_configViewModelU;
    private ConfigViewModelZU       m_configViewModelZU;
    private ConfigViewModelSDest    m_configViewModelSDest;
    private ConfigViewModelUDest    m_configViewModelUDest;
    private OptionsViewModel        m_optionsViewModel;
    private UsersViewModel          m_usersViewModel;
    private PublicfoldersViewModel  m_publicfoldersViewModel;
    private ScheduleViewModel       m_scheduleViewModel;
    private AccountResultsViewModel m_resultsViewModel;

    public CSMigrationWrapper mw;



    // =============================================================================================================
    // Log Level Combobox code
    // =============================================================================================================

    // From IntroView.xaml:
    /*
    <ComboBox 
              ItemsSource       = "{Binding LogLevelObjs}" 
              DisplayMemberPath = "LogLevelString"
              SelectedValuePath = "LogLevelEnum"
              SelectedValue     = "{Binding SelectedLogLevelCBItem.LogLevelEnum}">
    </ComboBox>
     */

    // Class used to bind the combobox selections to. Must implement 
    // INotifyPropertyChanged in order to get the data binding to 
    // work correctly.
    public class LogLevelCBItem : INotifyPropertyChanged
    {
        // Need a void constructor in order to use as an object element in the XAML.
        public LogLevelCBItem()
        {
            LogLevelEnum = Log.Level.Trace; // <- default log level
        }

        private Log.Level _loglevelEnum;// = Log.Level.Verbose;  
        public Log.Level LogLevelEnum
        {
            get { return _loglevelEnum; }
            set
            {
                if (_loglevelEnum != value)
                {
                    _loglevelEnum = value;
                    NotifyPropertyChanged("LogLevelEnum");
                    Log.SetLogLevel(value);

                }
            }
        }

        #region INotifyPropertyChanged Members

        // Need to implement this interface in order to get data binding
        // to work properly.
        private void NotifyPropertyChanged(string propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        #endregion
    }


    // This class provides us with objects to fill a ComboBox with                    
    public class LogLevelObj
    {
        public Log.Level LogLevelEnum { get; set; }
        public string LogLevelString { get; set; }
    }

    // Collection of the above objects
    public List<LogLevelObj> LogLevelObjs { get; set; }

    // Object to bind the current combobox selection to
    public LogLevelCBItem SelectedLogLevelCBItem { get; set; }

    private LogLevelCBItem m_LogLevelCBItem = new LogLevelCBItem();


    // =============================================================================================================
    // ctor
    // =============================================================================================================
    public IntroViewModel(ListBox lbMode)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("IntroViewModel.IntroViewModel"))
        {
            // ----------------------------------------
            // Set up Log Level combo box data binding
            // ----------------------------------------

            // Set the property to be used for the data binding to and from the ComboBox's.
            SelectedLogLevelCBItem = m_LogLevelCBItem;

            // Populate LogLevelObjs
            LogLevelObjs = new List<LogLevelObj>()
            {
             // new LogLevelObj(){ LogLevelEnum = Log.Level.Err,     LogLevelString = "Errors and Warnings Only" }, // No need - this one covered by the one below
                new LogLevelObj(){ LogLevelEnum = Log.Level.Warn,    LogLevelString = "Errors and Warnings Only" },
                new LogLevelObj(){ LogLevelEnum = Log.Level.Info,    LogLevelString = "Information" },
                new LogLevelObj(){ LogLevelEnum = Log.Level.Trace,   LogLevelString = "Trace" },
                new LogLevelObj(){ LogLevelEnum = Log.Level.Verbose, LogLevelString = "Verbose" },
            };


            // ----------------------------------------
            // lbMode
            // ----------------------------------------
            lb = lbMode;

            // ----------------------------------------
            // Set up Action Commands
            // ----------------------------------------
            this.GetIntroLicenseCommand     = new ActionCommand(this.GetIntroLicense,    () => true);
            this.GetIntroUserMigCommand     = new ActionCommand(this.GetIntroUserMig,    () => true);
            this.GetIntroServerMigCommand   = new ActionCommand(this.GetIntroServerMig,  () => true);
            this.GetIntroDesktopMigCommand  = new ActionCommand(this.GetIntroDesktopMig, () => true);

            this.NextCommand                = new ActionCommand(this.Next, () => true);
            this.LoadCommand                = new ActionCommand(this.Load, () => true);

            // Choose ShortDatePattern
            Application.Current.Properties["sdp"] = shortDatePattern;
        }
    }

    public UsersViewModel GetUsersViewModel()
    {
        return m_usersViewModel;
    }

    // =============================================================================================================
    // Radio button handlers
    // =============================================================================================================
    public ICommand GetIntroServerMigCommand 
    {
        get;
        private set;
    }
    private void GetIntroServerMig()
    {
        // "Server Migration" radio clicked
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // Remove existing views
            for (int i = (TheViews.Count - 1); i > 0; i--)
                TheViews.RemoveAt(i);

            // Set base class control migtype members
            BaseViewModel.isServer = true;
            BaseViewModel.isDesktop = false;
            Application.Current.Properties["migrationmode"] = "server"; // Not read anywhere - can probably remove

            // Set radio buttons - obsolete?
            rbServerMigration  = true;
            rbUserMigration    = false;
            rbDesktopMigration = false;

            m_optionsViewModel.OEnableRulesAndOOO = true;

            // BUG 104613
            m_optionsViewModel.OEnablePF = true;

            // Set up following pages for this mig type
            AddViews(m_isBrowser);
        }
    }

    public ICommand GetIntroUserMigCommand 
    {
        get;
        private set;
    }
    private void GetIntroUserMig()
    {
        // "User Migration" radio clicked
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // Remove existing views
            for (int i = (TheViews.Count - 1); i > 0; i--)
                TheViews.RemoveAt(i);

            BaseViewModel.isServer = false;
            BaseViewModel.isDesktop = false;
            Application.Current.Properties["migrationmode"] = "user";  // Not read anywhere - can probably remove

            // Set radio buttons - obsolete?
            rbUserMigration    = true;
            rbServerMigration  = false;
            rbDesktopMigration = false;

            // Set up following pages for this mig type
            AddViews(m_isBrowser);

            if (m_usersViewModel.UsersList.Count > 0)
                m_usersViewModel.UsersList.Clear();

            if (m_scheduleViewModel.SchedList.Count > 0)
                m_scheduleViewModel.SchedList.Clear();

            m_optionsViewModel.OEnableRulesAndOOO = m_configViewModelU.Isprofile;

            // BUG 104613
            m_optionsViewModel.IsPublicFolders = false;  // ... turns off PF mig
            m_optionsViewModel.OEnablePF = false;        // ... disables control to prevent user turning it back on

            m_optionsViewModel.OEnableNext = !m_scheduleViewModel.IsComplete();
        }
    }

    public ICommand GetIntroDesktopMigCommand
    {
        get;
        private set;
    }
    private void GetIntroDesktopMig()
    {
        // "Desktop Migration" radio clicked
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // Remove existing views
            for (int i = (TheViews.Count - 1); i > 0; i--)
                TheViews.RemoveAt(i);

            BaseViewModel.isServer = false;
            BaseViewModel.isDesktop = true;
            Application.Current.Properties["migrationmode"] = "user";   // Not read anywhere - can probably remove

            // Set radio buttons - obsolete?
            rbUserMigration    = false;
            rbDesktopMigration = true;
            rbServerMigration  = false;

            // Set up following pages for this mig type
            AddViews(m_isBrowser);

            if (m_usersViewModel.UsersList.Count > 0)
                m_usersViewModel.UsersList.Clear();

            if (m_scheduleViewModel.SchedList.Count > 0)
                m_scheduleViewModel.SchedList.Clear();

            m_optionsViewModel.OEnableRulesAndOOO = false;

            // BUG 104613
            m_optionsViewModel.IsPublicFolders = false;  // ... turns off PF mig
            m_optionsViewModel.OEnablePF = false;        // ... disables control to prevent user turning it back on

            m_optionsViewModel.ImportRuleOptions = false;
            m_optionsViewModel.ImportOOOOptions = false;

            m_optionsViewModel.OEnableNext = !m_scheduleViewModel.IsComplete();
        }
    }

    // =============================================================================================================
    // Load button handler
    // =============================================================================================================
    public ICommand LoadCommand
    {
        get;
        private set;
    }

    private void Load()
    {
        // User has clicked the Load button
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // ===============================================
            // Prompt for config file
            // ===============================================
            Config config;
            string filename = PromptAndReadConfigFromDisk(out config);
            if (filename != "")
            {
                // ===============================================
                // Have each page pull data it needs from 'config'
                // ===============================================
                try
                {
                    // ---------------------------------
                    // Select appropriate log level
                    // ---------------------------------
                    SelectedLogLevelCBItem.LogLevelEnum = Log.LogLevelStr2Enum(config.GeneralOptions.LogLevel);

                    // ---------------------------------
                    // Select appropriate radio button
                    // ---------------------------------
                    // DCB_BUG_101904 Use SchemaVersion and MigType to decide what type of migration this is
                    int nMigType = 0;
                    if (config.GeneralOptions.SchemaVersion == 0)
                    {
                        // It's an old XML file - best we can do is look at DataFile
                        if (config.SourceServer.DataFile == "")
                            nMigType = 1; // Assume its "Server Migration"
                        else
                            nMigType = 2;
                    }
                    else
                        nMigType = config.GeneralOptions.MigType;

                    if (nMigType == 1)
                    {
                        // -> Server migration
                        rbServerMigration = true;
                        rbUserMigration = false;

                        GetIntroServerMig();

                        // Tell the pages to grab their settings from xml
                        ((ConfigViewModelS)ViewModelPtrs[(int)ViewType.SVRSRC]).LoadConfig(config);
                        ((ConfigViewModelSDest)ViewModelPtrs[(int)ViewType.SVRDEST]).LoadConfig(config);
                    }
                    else
                    if (nMigType == 2)
                    {
                        // -> User migration
                        rbServerMigration = false;
                        rbUserMigration = true;

                        GetIntroUserMig();

                        // Tell the pages to grab their settings from xml
                        ((ConfigViewModelU)    ViewModelPtrs[(int)ViewType.USRSRC]).LoadConfig(config);
                        ((ConfigViewModelUDest)ViewModelPtrs[(int)ViewType.USRDEST]).LoadConfig(config);
                    }
                    else
                    {
                        // TODO ZD!
                    }

                    // Tell the other pages to grab their settings from config xml

                    ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]).LoadConfig(config);

                    ((UsersViewModel)ViewModelPtrs[(int)ViewType.USERS]).LoadDomain(config);

                    ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]).LoadConfig(config);
                    ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]).SetConfigFile(filename);


                    // Check profile specified in file still exists
                    /*
                    if ((IsProfile) && (CurrentProfileSelection == -1))
                    {
                        MessageBox.Show("The profile listed in the file does not exist on this system.  Please select a valid profile",      THIS SHOULD JUST CLEAR THE PROFILE FROM THE SELECTION
                                        "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }
                    */
                }
                catch (Exception e)
                {
                    DisplayLoadError(e);
                    return;
                }
            }
        }
    }

    // =============================================================================================================
    // Next button handler
    // =============================================================================================================
    public ICommand NextCommand 
    {
        get;
        private set;
    }
    public void Next()
    // "Next" has been clicked on the Intro page:
    // - Create MigrationWrapper (used later to do migration)
    // - Populate profiles combo (its on next page)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // ================================================================
            // Create IntroViewModel's migration wrapper if not already done
            // ================================================================
            if (mw == null)
            {
                try
                {
                    mw = new CssLib.CSMigrationWrapper("MAPI");
                }
                catch (Exception e)
                {
                    string error = "Migration cannot be initialized.  ";
                    error += e.Message;
                    MessageBox.Show(error, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }

                // Store mw in the app as well as in IntroViewModel. Why?
                Application.Current.Properties["mw"] = mw;
            }
            
            if (!isDesktop)
            {
                // ================================================================
                // Initialize the profile combo boxes
                // ================================================================

                // ------------------------
                // Grab from C++ layer
                // ------------------------
                string[] profiles = mw.GetListofMapiProfiles();
                if (profiles == null) // FBS bug 74917 -- 6/1/12
                {
                    profiles = new string[1];
                    profiles[0] = "No profiles";
                }

                if (profiles[0].IndexOf("No profiles") != -1)
                {
                    // -----------------------------------
                    // No profiles -> show messagebox
                    // -----------------------------------
                    string msg = "No Exchange profiles exist.  ";
                    if (isServer)
                    {
                        msg += "Please enter the Exchange Server information manually.";
                        m_configViewModelS.Isprofile = false;
                        m_configViewModelS.IsmailServer = true;
                    }
                    else
                    {
                        msg += "Please enter a PST file.";
                        m_configViewModelU.Isprofile = false;
                        m_configViewModelU.IspST = true;
                    }
                    MessageBox.Show(msg, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Information);
                    m_configViewModelS.CSEnableNext = true;
                }
                else
                {
                    // --------------------------------------------
                    // Got some profiles -> populate droplist
                    // --------------------------------------------

                    // Clear existing
                    if (isServer)
                        m_configViewModelS.ProfileList.Clear();
                    else
                        m_configViewModelU.ProfileList.Clear();

                    // Fill with values from C++ layer
                    foreach (string s in profiles)
                    {
                        if (s.IndexOf("GetListofMapiProfiles Exception") != -1)
                        {
                            MessageBox.Show(s, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                            return;
                        }

                        if (isServer)
                            m_configViewModelS.ProfileList.Add(s);
                        else
                            m_configViewModelU.ProfileList.Add(s);
                    }

                    // ------------------------------------------------------------
                    // If user did Load config, select the one from the config file 
                    // ------------------------------------------------------------
                    if (isServer)
                    {
                        m_configViewModelS.CurrentProfileSelection = (m_configViewModelS.OutlookProfile == null) ? 0 : m_configViewModelS.ProfileList.IndexOf(m_configViewModelS.OutlookProfile);
                        m_configViewModelS.CSEnableNext = (m_configViewModelS.ProfileList.Count > 0);
                    }
                    else
                    {
                        m_configViewModelU.CurrentProfileSelection = (m_configViewModelU.OutlookProfile == null) ? 0 : m_configViewModelU.ProfileList.IndexOf(m_configViewModelU.OutlookProfile);
                        m_configViewModelU.CSEnableNext = (m_configViewModelU.ProfileList.Count > 0);
                    }
                }
                lb.SelectedIndex = 1;
            }
            else
            {
                // Its ZD
                m_configViewModelZU.CSEnableNext = true;
                m_configViewModelZU.Isprofile = false;
                m_configViewModelZU.IspST = true;

                lb.SelectedIndex = 1;
            }

        } //LogBlock
    }

    // =============================================================================================================
    // License click handler
    // =============================================================================================================
    public ICommand GetIntroLicenseCommand 
    {
        get;
        private set;
    }
    private void GetIntroLicense()
    {
        // "Zimbra License" text clicked
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string urlString = "https://files.zimbra.com/website/docs/Zimbra-Network-Edition-EULA.pdf";
            Process.Start(new ProcessStartInfo(urlString));
        }
    }


    // =============================================================================================================
    // Members bound to UI elements
    // =============================================================================================================
    public string BuildNum 
    {
        get { return m_intro.BuildNum; }
        set
        {
            if (value == m_intro.BuildNum)
                return;
            m_intro.BuildNum = value;

            OnPropertyChanged(new PropertyChangedEventArgs("BuildNum"));
        }
    }

    public string WelcomeMsg 
    {
        get { return m_intro.WelcomeMsg; }
        set
        {
            if (value == m_intro.WelcomeMsg)
                return;
            m_intro.WelcomeMsg = value;

            OnPropertyChanged(new PropertyChangedEventArgs("WelcomeMsg"));
        }
    }

    public bool rbServerMigration 
    // Bound to Server radio button
    {
        get { return m_intro.rbServerMigration; }
        set
        {
            if (value == m_intro.rbServerMigration)
                return;

            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                m_intro.rbServerMigration = value;
                Log.trace(value);
                OnPropertyChanged(new PropertyChangedEventArgs("rbServerMigration"));
            }
        }
    }

    public bool rbUserMigration
    // Bound to User radio button
    {
        get { return m_intro.rbUserMigration; }
        set
        {
            if (value == m_intro.rbUserMigration)
                return;

            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                m_intro.rbUserMigration = value;
                Log.trace(value);
                OnPropertyChanged(new PropertyChangedEventArgs("rbUserMigration"));
            }
        }
    }

    public bool rbDesktopMigration
    // Bound to ZD radio button
    {
        get { return m_intro.rbDesktopMigration; }
        set
        {
            if (value == m_intro.rbDesktopMigration)
                return;

            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                m_intro.rbDesktopMigration = value;
                Log.trace(value);
                OnPropertyChanged(new PropertyChangedEventArgs("rbDesktopMigration"));
            }
        }
    }

    public string InstallDir 
    {
        get;
        set;
    }

    // =============================================================================================================
    // Helpers
    // =============================================================================================================
    public void SetupViewModelPtrs()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            for (int i = 0; i < (int)ViewType.MAX; i++)
            {
                ViewModelPtrs[i] = null;
            }

            ViewModelPtrs[(int)ViewType.INTRO]      = this;
            ViewModelPtrs[(int)ViewType.SVRSRC]     = m_configViewModelS;
            ViewModelPtrs[(int)ViewType.USRSRC]     = m_configViewModelU;
            ViewModelPtrs[(int)ViewType.ZDSRC]      = m_configViewModelZU;
            ViewModelPtrs[(int)ViewType.SVRDEST]    = m_configViewModelSDest;
            ViewModelPtrs[(int)ViewType.USRDEST]    = m_configViewModelUDest;
            ViewModelPtrs[(int)ViewType.OPTIONS]    = m_optionsViewModel;
            ViewModelPtrs[(int)ViewType.USERS]      = m_usersViewModel;
            ViewModelPtrs[(int)ViewType.PUBFLDS]    = m_publicfoldersViewModel;
            ViewModelPtrs[(int)ViewType.SCHED]      = m_scheduleViewModel;
            ViewModelPtrs[(int)ViewType.RESULTS]    = m_resultsViewModel;
        }
    }

    public void SetupViews(bool isBrowser)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            m_isBrowser = isBrowser;

            BaseViewModel.isServer = true;          // because we start out with Server on -- wouldn't get set by command

            rbServerMigration = true;
            rbUserMigration = false;

            savedDomain = "";
            ZimbraValues.GetZimbraValues().ClientVersion = BuildNum;

            // ------------------------------------------------------------
            // Source pages
            // ------------------------------------------------------------

            // Server mig version
            m_configViewModelS = new ConfigViewModelS();                        
            m_configViewModelS.Name                 = "ConfigViewModelS";
            m_configViewModelS.ViewTitle            = "Source";
            m_configViewModelS.lb                   = lb;
            m_configViewModelS.isBrowser            = isBrowser;
            m_configViewModelS.OutlookProfile       = "";
            m_configViewModelS.MailServerHostName   = "";
            m_configViewModelS.MailServerAdminID    = "";
            m_configViewModelS.MailServerAdminPwd   = "";

            // User mig version
            m_configViewModelU = new ConfigViewModelU();                        
            m_configViewModelU.Name                 = "ConfigViewModelU";
            m_configViewModelU.ViewTitle            = "Source";
            m_configViewModelU.lb                   = lb;
            m_configViewModelU.isBrowser            = isBrowser;
            m_configViewModelU.OutlookProfile       = "";
            m_configViewModelU.PSTFile              = "";
            m_configViewModelU.OutlookProfile       = "";

            // ZD mig version
            m_configViewModelZU = new ConfigViewModelZU();                      
            m_configViewModelZU.Name                = "ConfigViewModelZU";
            m_configViewModelZU.ViewTitle           = "Source";
            m_configViewModelZU.lb                  = lb;
            m_configViewModelZU.isBrowser           = isBrowser;
            m_configViewModelZU.OutlookProfile      = "";
            m_configViewModelZU.PSTFile             = "";
            m_configViewModelZU.OutlookProfile      = "";


            // ------------------------------------------------------------
            // Destination pages
            // ------------------------------------------------------------

            // Server mig version
            m_configViewModelSDest = new ConfigViewModelSDest();
            m_configViewModelSDest.Name                 = "ConfigViewModelSDest";
            m_configViewModelSDest.ViewTitle            = "Destination";
            m_configViewModelSDest.lb                   = lb;
            m_configViewModelSDest.isBrowser            = isBrowser;
            m_configViewModelSDest.ZimbraServerHostName = "";
            m_configViewModelSDest.ZimbraPort           = "";
            m_configViewModelSDest.ZimbraAdmin          = "";
            m_configViewModelSDest.ZimbraAdminPasswd    = "";
            m_configViewModelSDest.ZimbraSSL            = true;

            // User mig version
            m_configViewModelUDest = new ConfigViewModelUDest();
            m_configViewModelUDest.Name                 = "ConfigViewModelUDest";
            m_configViewModelUDest.ViewTitle            = "Destination";
            m_configViewModelUDest.lb                   = lb;
            m_configViewModelUDest.isBrowser            = isBrowser;
            m_configViewModelUDest.ZimbraServerHostName = "";
            m_configViewModelUDest.ZimbraPort           = "";
            m_configViewModelUDest.ZimbraUser           = "";
            m_configViewModelUDest.ZimbraUserPasswd     = "";
            m_configViewModelUDest.ZimbraSSL            = true;

            // ZD mig version
            // (No dest page for ZD)

            // ------------------------------------------------------------
            // Option page
            // ------------------------------------------------------------
            m_optionsViewModel = new OptionsViewModel();
            m_optionsViewModel.Name                     = "OptionsViewModel";
            m_optionsViewModel.ViewTitle                = "Options";
            m_optionsViewModel.lb                       = lb;
            m_optionsViewModel.isBrowser                = isBrowser;
            m_optionsViewModel.ImportMailOptions        = true;
            m_optionsViewModel.ImportTaskOptions        = true;
            m_optionsViewModel.ImportCalendarOptions    = true;
            m_optionsViewModel.ImportContactOptions     = true;
            m_optionsViewModel.ImportRuleOptions        = true;
            m_optionsViewModel.ImportOOOOptions         = true;
            m_optionsViewModel.ImportJunkOptions        = false;
            m_optionsViewModel.ImportDeletedItemOptions = false;
            m_optionsViewModel.ImportSentOptions        = false;

            m_optionsViewModel.MaxThreadCount           = 0;
            m_optionsViewModel.MaxErrorCount            = 0;
            m_optionsViewModel.OEnableRulesAndOOO       = true;
            m_optionsViewModel.OEnablePF                = true;
            m_optionsViewModel.OEnableNext              = true;
            m_optionsViewModel.MigrateONRAfter          = (DateTime.Now.AddMonths(-3)).ToShortDateString();
            m_optionsViewModel.IsMaxMessageSize         = false;
            m_optionsViewModel.IsSkipPrevMigratedItems  = false;
            m_optionsViewModel.MaxMessageSize           = "";
            m_optionsViewModel.IsSkipFolders            = false;

            // ------------------------------------------------------------
            // Scedule page
            // ------------------------------------------------------------
            m_scheduleViewModel = new ScheduleViewModel();
            m_scheduleViewModel.Name                    = "Schedule";
            m_scheduleViewModel.ViewTitle               = "Migrate";
            m_scheduleViewModel.lb                      = lb;
            m_scheduleViewModel.isBrowser               = isBrowser;
            m_scheduleViewModel.COS                     = "default";
            m_scheduleViewModel.DefaultPWD              = "";
            m_scheduleViewModel.ScheduleDate            = DateTime.Now.ToShortDateString();
            m_scheduleViewModel.EnableProvGB            = false;

            // ------------------------------------------------------------
            // Users page
            // ------------------------------------------------------------
            m_usersViewModel = new UsersViewModel("", "");
            m_usersViewModel.Name                       = "Users";
            m_usersViewModel.ViewTitle                  = "Users";
            m_usersViewModel.lb                         = lb;
            m_usersViewModel.ZimbraDomain               = "";
            m_usersViewModel.isBrowser                  = isBrowser;
            m_usersViewModel.CurrentUserSelection       = -1;
            m_usersViewModel.svm                        = m_scheduleViewModel;  // LDAP Browser needs to get to ScheduleView to set EnableMigrate 

            // ------------------------------------------------------------
            // Public folders page
            // ------------------------------------------------------------
            m_publicfoldersViewModel = new PublicfoldersViewModel("", "");
            m_publicfoldersViewModel.Name               = "PublicFolders";
            m_publicfoldersViewModel.ViewTitle          = "Public Folders";
            m_publicfoldersViewModel.lb                 = lb;
            m_publicfoldersViewModel.ZimbraDomain       = "";
            m_publicfoldersViewModel.isBrowser          = isBrowser;
            m_publicfoldersViewModel.svm                = m_scheduleViewModel;  // LDAP Browser needs to get to ScheduleView to set EnableMigrate 

            // ------------------------------------------------------------
            // Results page
            // ------------------------------------------------------------
            m_resultsViewModel = new AccountResultsViewModel(m_scheduleViewModel, -1, "", false);
            m_resultsViewModel.Name                     = "Results";
            m_resultsViewModel.ViewTitle                = "Results";
            m_resultsViewModel.isBrowser                = isBrowser;
            m_resultsViewModel.CurrentAccountSelection  = -1;
            m_resultsViewModel.OpenLogFileEnabled       = false;

            SetupViewModelPtrs();

            TheViews = new ObservableCollection<object>();
            TheViews.Add(this);
        }
    }

    public void AddViews(bool isBrowser)
    {
        // Adjusts items in the wizard left pane depending on migration type
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (BaseViewModel.isServer)
            {
                // Server migration
                Log.info("Server migration");

                BaseViewModel.isServer = true;

                rbServerMigration = true;
                rbUserMigration = false;

                TheViews.Add(m_configViewModelS);
                TheViews.Add(m_configViewModelSDest);
                TheViews.Add(m_optionsViewModel);
                TheViews.Add(m_usersViewModel);
                TheViews.Add(m_publicfoldersViewModel);
                TheViews.Add(m_scheduleViewModel);
                TheViews.Add(m_resultsViewModel);

                m_optionsViewModel.DateFormatLabelContent = "(" + shortDatePattern + ")";
                m_scheduleViewModel.DateFormatLabelContent2 = "(" + shortDatePattern + ")";
                m_optionsViewModel.ImportNextButtonContent = "Next >";
            }
            else 
            if (BaseViewModel.isDesktop)
            {
                // ZD migration
                Log.info("ZD migration");

                BaseViewModel.isServer = false;
                BaseViewModel.isDesktop = true;

                rbUserMigration = false;
                rbDesktopMigration = true;
                rbServerMigration = false;

                TheViews.Add(m_configViewModelZU);
                TheViews.Add(m_configViewModelUDest);
                TheViews.Add(m_optionsViewModel);
                TheViews.Add(m_resultsViewModel);

                m_optionsViewModel.DateFormatLabelContent = "(" + shortDatePattern + ")";
                m_optionsViewModel.ImportNextButtonContent = "Migrate";
            }
            else
            {
                // User migration
                Log.info("User migration");

                BaseViewModel.isServer = false;

                rbUserMigration = true;
                rbServerMigration = false;

                TheViews.Add(m_configViewModelU);
                TheViews.Add(m_configViewModelUDest);
                TheViews.Add(m_optionsViewModel);
                TheViews.Add(m_resultsViewModel);

                m_optionsViewModel.DateFormatLabelContent = "(" + shortDatePattern + ")";
                m_optionsViewModel.ImportNextButtonContent = "Migrate";
            }
        }
    }
}
}
