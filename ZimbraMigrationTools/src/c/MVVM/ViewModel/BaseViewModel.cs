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

using MVVM.Model;
using Misc;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Globalization;
using System.Threading;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows;
using System.Xml.Linq;
using System.Xml;
using System;
using CssLib;

namespace MVVM.ViewModel
{

// ==============================================================================
// class BaseViewModel
// ==============================================================================
// All pages in the wizard derive from this
// Contains funtionality shared by all pages e.g. Help handling

public class BaseViewModel: INotifyPropertyChanged
{
    public BaseViewModel()
    {
        //using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("BaseViewModel.BaseViewModel")) // Not terribly useful
        {
            this.ProcessHelpCommand = new ActionCommand(this.ProcessHelp, () => true);
            CultureInfo currentCulture = Thread.CurrentThread.CurrentCulture;
            shortDatePattern = currentCulture.DateTimeFormat.ShortDatePattern;
        }
    }

    // -----------------------------------------------------------------------------------------
    // Events
    // -----------------------------------------------------------------------------------------
    public event PropertyChangedEventHandler PropertyChanged;

    public void OnPropertyChanged(PropertyChangedEventArgs e)
    {
        //using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (PropertyChanged != null)
                PropertyChanged(this, e);
        }
    }

    // -----------------------------------------------------------------------------------------
    // Properties
    // -----------------------------------------------------------------------------------------
    public string Name              { get; set; }
    public string ViewTitle         { get; set; }
    public ListBox lb               { get; set; }
    public static bool isServer     { get; set; }
    public static bool isDesktop    { get; set; }
    public bool isBrowser           { get; set; }
    public string savedDomain       { get; set; }

    public enum ViewType {INTRO, SVRSRC, USRSRC, ZDSRC, SVRDEST, USRDEST, OPTIONS, USERS,PUBFLDS, SCHED, RESULTS, MAX}
    public static Object[] ViewModelPtrs = new Object[(int)ViewType.MAX];

    public string shortDatePattern;

    public ICommand ProcessHelpCommand 
    {
        get;
        private set;
    }

    // -----------------------------------------------------------------------------------------
    // Members
    // -----------------------------------------------------------------------------------------
    public Config m_config = new Config("", "", "", "", "", "", "", "", "", "", "");

    // -----------------------------------------------------------------------------------------
    // Methods
    // -----------------------------------------------------------------------------------------
    private void ProcessHelp()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // What the listbox shows

            //  Index       Server Mig          User Mig
            //  ----------------------------------------------
            //  0           Welcome             Welcome
            //  1           Source              Source
            //  2           Destination         Destination
            //  3           Options             Options
            //  4           Users               Results
            //  5           Public Folders
            //  6           Migrate
            //  7           Results

            if (isServer)
            {
                switch (lb.SelectedIndex)
                {
                    case 0:
                        DoHelp("welcome.html");
                        break;
                    case 1:
                        DoHelp("source_server.html");
                        break;
                    case 2:
                        DoHelp("destination_server.html");
                        break;
                    case 3:
                        DoHelp("options_server.html");
                        break;
                    case 4:
                        DoHelp("users_main.html");
                        break;
                    case 5:
                        DoHelp("public_folders.html");
                        break;
                    case 6:
                        DoHelp("migrate.html");
                        break;
                    case 7:
                        DoHelp("results.html");
                        break;
                    default:
                        break;
                }
            }
            else
            {
                switch (lb.SelectedIndex)
                {
                    case 0:
                        DoHelp("welcome.html");
                        break;
                    case 1:
                        DoHelp("source_user.html");
                        break;
                    case 2:
                        DoHelp("destination_user.html");
                        break;
                    case 3:
                        DoHelp("options_user.html");
                        break;
                    case 4:
                        DoHelp("results.html");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void UpdateXmlElement(string XmlfileName, string XmlelementName)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            System.Xml.Serialization.XmlSerializer xmlSerializer = new System.Xml.Serialization.XmlSerializer(typeof(MVVM.Model.Config));
            System.IO.StringWriter stringWriter = new System.IO.StringWriter();
            System.Xml.XmlWriter xmlWriter = new System.Xml.XmlTextWriter(stringWriter);

            xmlSerializer.Serialize(xmlWriter, m_config);

            string newSourceXml = stringWriter.ToString();
            XElement newSourceTypeElem = XElement.Parse(newSourceXml);
            XElement newimport = newSourceTypeElem.Element((XName)XmlelementName);
            XmlDocument xmlDoc = new XmlDocument();

            xmlDoc.PreserveWhitespace = true;

            try
            {
                xmlDoc.Load(XmlfileName);
            }
            catch (XmlException e)
            {
                Console.WriteLine(e.Message);
            }

            // Now create StringWriter object to get data from xml document.
            StringWriter sw = new StringWriter();
            XmlTextWriter xw = new XmlTextWriter(sw);

            xmlDoc.WriteTo(xw);

            string SourceXml = sw.ToString();

            // Replace the current view xml Sources node with the new one
            XElement viewXmlElem = XElement.Parse(SourceXml);
            XElement child = viewXmlElem.Element((XName)XmlelementName);

            // viewXmlElem.Element("importOptions").ReplaceWith(newSourcesElem);
            child.ReplaceWith(newimport);

            xmlDoc.LoadXml(viewXmlElem.ToString());
            xmlDoc.Save(XmlfileName);
        }
    }

    public void PopulateConfigForSaving(bool isServer)
    // Called from from ViewModel Save() methods to populate m_config before serializing to disk
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            // Get ptr to each page from ViewModelPtrs
            ConfigViewModelS serverSourceModel      = (ConfigViewModelS)    ViewModelPtrs[(int)ViewType.SVRSRC];
            ConfigViewModelSDest serverDestModel    = (ConfigViewModelSDest)ViewModelPtrs[(int)ViewType.SVRDEST];

            ConfigViewModelU userSourceModel        = (ConfigViewModelU)    ViewModelPtrs[(int)ViewType.USRSRC];
            ConfigViewModelUDest userDestModel      = (ConfigViewModelUDest)ViewModelPtrs[(int)ViewType.USRDEST];

            OptionsViewModel optionsModel           = (OptionsViewModel)    ViewModelPtrs[(int)ViewType.OPTIONS];
            UsersViewModel usersModel               = (UsersViewModel)      ViewModelPtrs[(int)ViewType.USERS];
            ScheduleViewModel scheduleModel         = (ScheduleViewModel)   ViewModelPtrs[(int)ViewType.SCHED];

            m_config.GeneralOptions.SchemaVersion = 1; // Schema version - increment if you change any of the options
            m_config.SourceServer.Profile = "";

            if (isServer)
            {
                // ==============================================================
                // Server Migration
                // ==============================================================
                m_config.GeneralOptions.MigType = 1;

                // ------------------------------
                // SourceServer
                // ------------------------------
                int sel = serverSourceModel.CurrentProfileSelection;
                if (sel != -1)
                {
                    if (serverSourceModel.ProfileList.Count > 0)
                        m_config.SourceServer.Profile = serverSourceModel.ProfileList[sel];
                }

                m_config.SourceServer.Hostname      = serverSourceModel.MailServerHostName;
                m_config.SourceServer.AdminID       = serverSourceModel.MailServerAdminID;
                m_config.SourceServer.AdminPwd      = serverSourceModel.MailServerAdminPwd;
                m_config.SourceServer.UseProfile    = serverSourceModel.Isprofile;

                // ------------------------------
                // ZimbraServer (= dest server)
                // ------------------------------
                m_config.ZimbraServer.Hostname      = serverDestModel.ZimbraServerHostName;
                m_config.ZimbraServer.Port          = serverDestModel.ZimbraPort;
                m_config.ZimbraServer.AdminID       = serverDestModel.ZimbraAdmin;
                m_config.ZimbraServer.AdminPwd      = serverDestModel.ZimbraAdminPasswd;
                m_config.ZimbraServer.UseSSL        = serverDestModel.ZimbraSSL;


                // ------------------------------
                // UserProvision
                // ------------------------------

                // FBS bug 73500 -- 5/18/12
                if (usersModel.ZimbraDomain.Length == 0)
                {
                    if (usersModel.DomainsFilledIn)
                    {
                        m_config.UserProvision.DestinationDomain = usersModel.DomainList[usersModel.CurrentDomainSelection];
                        m_config.UserProvision.COS = scheduleModel.CosList[scheduleModel.CurrentCOSSelection].CosName;
                    }
                    else
                    if (savedDomain != null)
                    {
                        if (savedDomain.Length > 0)
                            m_config.UserProvision.DestinationDomain = savedDomain;
                    }
                }
                else
                {
                    m_config.UserProvision.DestinationDomain = usersModel.ZimbraDomain;
                    m_config.UserProvision.COS = scheduleModel.COS;
                }
            }
            else
            {
                // ==============================================================
                // User Migration (or ZD)
                // ==============================================================
                m_config.GeneralOptions.MigType = 2;

                // ------------------------------
                // SourceServer
                // ------------------------------
                int sel = userSourceModel.CurrentProfileSelection;
                if (sel != -1)
                {
                    if (userSourceModel.ProfileList.Count > 0)
                        m_config.SourceServer.Profile = userSourceModel.ProfileList[sel];
                }

                m_config.SourceServer.DataFile      = userSourceModel.PSTFile;
                m_config.SourceServer.UseProfile    = userSourceModel.Isprofile;

                // ------------------------------
                // ZimbraServer (= dest server)
                // ------------------------------
                m_config.ZimbraServer.Hostname      = userDestModel.ZimbraServerHostName;
                m_config.ZimbraServer.Port          = userDestModel.ZimbraPort;
                m_config.ZimbraServer.UserAccount   = userDestModel.ZimbraUser;
                m_config.ZimbraServer.UserPassword  = userDestModel.ZimbraUserPasswd;
                m_config.ZimbraServer.UseSSL        = userDestModel.ZimbraSSL;
            }
            /*
            DCB - Where is the block for ZD settings?!
            */


            // ==============================================================
            // Common to Server and User migration
            // ==============================================================


            // ------------------------------
            // GeneralOptions
            // ------------------------------
            m_config.GeneralOptions.LogLevel        = Log.GlobalLogLevel.ToString();

            m_config.GeneralOptions.MaxThreadCount  = optionsModel.MaxThreadCount;
            m_config.GeneralOptions.MaxErrorCount   = optionsModel.MaxErrorCount;

            // ------------------------------
            // ImportOptions
            // ------------------------------
            m_config.ImportOptions.Mail             = optionsModel.ImportMailOptions;
            m_config.ImportOptions.Calendar         = optionsModel.ImportCalendarOptions;
            m_config.ImportOptions.Contacts         = optionsModel.ImportContactOptions;
            m_config.ImportOptions.DeletedItems     = optionsModel.ImportDeletedItemOptions;
            m_config.ImportOptions.Junk             = optionsModel.ImportJunkOptions;
            m_config.ImportOptions.Tasks            = optionsModel.ImportTaskOptions;
            m_config.ImportOptions.Sent             = optionsModel.ImportSentOptions;
            m_config.ImportOptions.Rules            = optionsModel.ImportRuleOptions;
            m_config.ImportOptions.OOO              = optionsModel.ImportOOOOptions;

            // ------------------------------
            // AdvancedImportOptions
            // ------------------------------
            m_config.AdvancedImportOptions.IsOnOrAfter              = optionsModel.IsOnOrAfter;
            m_config.AdvancedImportOptions.MigrateOnOrAfter         = (optionsModel.IsOnOrAfter) ? DateTime.Parse(optionsModel.MigrateONRAfter) : DateTime.Now.AddMonths(-3);

            m_config.AdvancedImportOptions.IsSkipPrevMigratedItems  = optionsModel.IsSkipPrevMigratedItems;
            
            m_config.AdvancedImportOptions.IsMaxMessageSize         = optionsModel.IsMaxMessageSize;
            m_config.AdvancedImportOptions.MaxMessageSize           = (optionsModel.IsMaxMessageSize) ? optionsModel.MaxMessageSize : "";

            m_config.AdvancedImportOptions.DateFilterItem           = optionsModel.DateFilterItem;

            m_config.AdvancedImportOptions.SpecialCharReplace       = optionsModel.SpecialCharReplace;
            m_config.AdvancedImportOptions.CSVDelimiter             = optionsModel.CSVDelimiter;
            m_config.AdvancedImportOptions.IsPublicFolders          = optionsModel.IsPublicFolders;
            m_config.AdvancedImportOptions.IsZDesktop               = optionsModel.IsZDesktop;

            // Skipped folders
            m_config.AdvancedImportOptions.IsSkipFolders = optionsModel.IsSkipFolders;
            if (optionsModel.IsSkipFolders)
            {
                if (optionsModel.FoldersToSkip != null)
                {
                    if (optionsModel.FoldersToSkip.Length > 0)
                    {
                        string[] nameTokens = optionsModel.FoldersToSkip.Split(',');
                        int numToSkip = nameTokens.Length;
                        if (m_config.AdvancedImportOptions.FoldersToSkip == null)
                            m_config.AdvancedImportOptions.FoldersToSkip = new Folder[numToSkip];

                        for (int i = 0; i < numToSkip; i++)
                        {
                            Folder folder = new Folder();
                            folder.FolderName = nameTokens.GetValue(i).ToString();
                            m_config.AdvancedImportOptions.FoldersToSkip[i] = folder;
                        }
                    }
                }
            }
            else
                m_config.AdvancedImportOptions.FoldersToSkip = null;
        }
    }

    protected void DoHelp(string htmlFile)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string fileName;
            string urlString;
            bool bDoProcess;

            if (isBrowser)
            {
                fileName = urlString = "http://W764IIS.prom.eng.vmware.com/" + htmlFile;
                bDoProcess = true;                  // too lazy to check if xbap
            }
            else
            {
                fileName = ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]).InstallDir;
                fileName += "/Help/";
                fileName += htmlFile;
                urlString = "file:///" + fileName;
                bDoProcess = File.Exists(fileName);

                // FBS bug 76005 -- 7/13/12 -- build system does not use the extra level for help files
                if (!bDoProcess)
                {
                    fileName = ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]).InstallDir;
                    fileName += "/";
                    fileName += htmlFile;
                    urlString = "file:///" + fileName;
                    bDoProcess = File.Exists(fileName);
                }
                ///
            }
            if (bDoProcess)
            {
                // DJP_BUG_96640  Chrome can't display ZMT Help pages, so if Chrome is the default
                // browser then we explicitly use IE (which should always be present) instead.
                string strDefaultBrowser = (string) Microsoft.Win32.Registry.GetValue(
                                                @"HKEY_CURRENT_USER\SOFTWARE\Microsoft\Windows\Shell\Associations\UrlAssociations\http\UserChoice",
                                                "Progid",
                                                null);
                if ( strDefaultBrowser != null && strDefaultBrowser.Contains("ChromeHTML") )
                {
                    string strIEPath = Environment.GetEnvironmentVariable("PROGRAMFILES");
                    strIEPath += "\\Internet Explorer\\iexplore.exe";
                    if (File.Exists(strIEPath))
                        Process.Start(new ProcessStartInfo(strIEPath, urlString));
                    else
                        MessageBox.Show("Help file cannot be displayed with Chrome", "Open file error", MessageBoxButton.OK, MessageBoxImage.Error);
                }
                else
                    Process.Start(new ProcessStartInfo(urlString));
            }
            else
                MessageBox.Show("Help file not found", "Open file error", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    protected void DisplayLoadError(Exception e)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            string temp = string.Format("Load error: config file could be out of date.\n{0}", e.Message);
            MessageBox.Show(temp, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    protected void Save()
    {
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            Microsoft.Win32.SaveFileDialog fDialog = new Microsoft.Win32.SaveFileDialog();
            fDialog.Filter = "Config Files|*.xml";
            if (fDialog.ShowDialog() == true)
            {
                System.Xml.Serialization.XmlSerializer writer = new System.Xml.Serialization.XmlSerializer(typeof(Config));

                System.IO.StreamWriter file = new System.IO.StreamWriter(fDialog.FileName);

                // Populate m_config
                PopulateConfigForSaving(isServer);

                // Serialize m_config
                writer.Serialize(file, m_config);

                file.Close();

                ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]).SetConfigFile(fDialog.FileName);
            }
        }
    }

    protected string PromptAndReadConfigFromDisk(out Config config)
    {
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            config = new Config();

            // --------------------------------------
            // Prompt for config file
            // --------------------------------------
            Microsoft.Win32.OpenFileDialog fDialog = new Microsoft.Win32.OpenFileDialog();
            fDialog.Filter = "Config Files|*.xml";
            fDialog.CheckFileExists = true;
            fDialog.Multiselect = false;
            if (fDialog.ShowDialog() == true)
            {
                if (File.Exists(fDialog.FileName))
                {
                    // --------------------------------------
                    // Deserialize into "config"
                    // --------------------------------------
                    System.IO.StreamReader fileRead = new System.IO.StreamReader(fDialog.FileName);
                    System.Xml.Serialization.XmlSerializer reader = new System.Xml.Serialization.XmlSerializer(typeof(Config));
                    try
                    {
                        config = (Config)reader.Deserialize(fileRead);
                    }
                    catch (Exception e)
                    {
                        string temp = string.Format("Incorrect configuration file format.\n{0}", e.Message);
                        MessageBox.Show(temp, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        fileRead.Close();
                        return "";
                    }
                    fileRead.Close();

                    return fDialog.FileName;
                }
                else
                {
                    MessageBox.Show("There is no options configuration stored.  Please enter some options info",
                                    "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
            return "";
        }
    }
}
}

