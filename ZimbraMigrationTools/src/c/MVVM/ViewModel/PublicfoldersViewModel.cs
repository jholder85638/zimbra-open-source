/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2014, 2015, 2016 Synacor, Inc.
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
using System.Linq;
using System.Windows.Input;
using System.Windows;
using System;

namespace MVVM.ViewModel
{
public class PublicfoldersViewModel: BaseViewModel
{
    readonly Users m_users = new Users("", "", -1);
    readonly Folder m_folders = new Folder();

    public PublicfoldersViewModel(string username, string mappedname)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("PublicfoldersViewModel.PublicfoldersViewModel"))
        {
            this.PubFoldersCommand  = new ActionCommand(this.PublicFolders, () => true);
            this.RemoveCommand      = new ActionCommand(this.Remove,        () => true);
            this.BackCommand        = new ActionCommand(this.Back,          () => true);

            this.NextCommand        = new ActionCommand(this.Next,          () => true);

            this.IsProvisioned = false;
            this.DomainsFilledIn = false;
            this.CsvDelimiter = ",";
            this.m_publicFolderName = username;
            this.PlusEnabled = true;
        }
    }

    // a bit of a hack, but with the LDAP Browser now being controlled by the UsersView,
    // we need a way for the view to get to the ScheduleViewModel to set EnableMigrate
    public ScheduleViewModel svm;

    public string ZimbraAccountName
    {
        get { return m_config.ZimbraServer.UserAccount; }
        set
        {
            if (value == m_config.ZimbraServer.UserAccount)
                return;

            m_config.ZimbraServer.UserAccount = value;

            OnPropertyChanged(new PropertyChangedEventArgs("ZimbraAccountName"));
        }
    }        

    // ------------------------------------------------------------------------------------
    // Click handler for Public Folders button
    // ------------------------------------------------------------------------------------
    public ICommand PubFoldersCommand
    {
        get;
        private set;
    }

    private void PublicFolders()
    {
        CSMigrationWrapper mw = ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]).mw;
        MigrationAccount Acct = new MigrationAccount();

        ConfigViewModelS eparams = ((ConfigViewModelS)ViewModelPtrs[(int)ViewType.SVRSRC]);
        Acct.ProfileName = eparams.OutlookProfile;

        // ----------------------------------------------------
        // User must enter target zimbra account first
        // ----------------------------------------------------
        if (ZimbraAccountName == null)
        {
            MessageBox.Show("Please specify a zimbra account name", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Warning);
            EnableNext = false;
            return;
        }
       
        string acctName = ZimbraAccountName + '@' + this.ZimbraDomain;
        Acct.AccountName = acctName;

        // ----------------------------------------------------
        // Get PFs from C++ layer - takes extended time
        // ----------------------------------------------------
        Mouse.OverrideCursor = System.Windows.Input.Cursors.Wait; // Set hourglass
        try
        {
            string[] folders = mw.GetPublicFolders(Acct); 
            if (folders != null) 
            {
                // FBS rewrite -- bug 71646 -- 3/26/12
                UsersViewModel userm;
                userm = new UsersViewModel(ZimbraAccountName, ZimbraAccountName);

                for (int i = 0; i < folders.Length; i++)
                {
                    string uname = "", displayname = "", givenname = "", sn = "", zfp = "";
                    uname       = folders.GetValue(i).ToString();
                    displayname = folders.GetValue(i).ToString();
                    givenname   = folders.GetValue(i).ToString();
                    sn          = folders.GetValue(i).ToString();
                    zfp         = folders.GetValue(i).ToString();

                    PublicfoldersViewModel uvm;
                    if (uname.CompareTo(displayname) == 0)
                        uvm = new PublicfoldersViewModel(displayname, uname);
                    else
                        uvm = new PublicfoldersViewModel(uname, uname);
               
                    uvm.ZimbraAccountName = ZimbraAccountName;

                    UsersBKList.Add(uvm);

                    UsersList.Add(userm);

                    ScheduleViewModel scheduleViewModel = ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]);

                    PlusEnabled = false;
                    //Username = ZimbraAccountName;
                    scheduleViewModel.SchedList.Add(new SchedUser(ZimbraAccountName, false));
                    scheduleViewModel.SchedfolderList.Add(new SchedUser(ZimbraAccountName, false));
                    scheduleViewModel.EnableMigrate = (scheduleViewModel.SchedfolderList.Count > 0);
                    scheduleViewModel.EnablePreview = scheduleViewModel.EnableMigrate;
                    EnableNext = (UsersList.Count > 0);
                }
                UsersViewModel usersViewModel = ((UsersViewModel)ViewModelPtrs[(int)ViewType.USERS]);

                usersViewModel.ZimbraDomain = this.ZimbraDomain;
            
                usersViewModel.UsersList.Add(userm);

            }
        }
        finally
        {
            Mouse.OverrideCursor = null;
        };
    }

    // ------------------------------------------------------------------------------------
    // Click handler for Remove button
    // ------------------------------------------------------------------------------------
    public ICommand RemoveCommand 
    {
        get;
        private set;
    }

    private void Remove()
    {
        OptionsViewModel ovm = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);
        ovm.IsSkipFolders = true;
           
        folderstoskip += UsersBKList[CurrentFolderSelection].MappedName.ToString();
        folderstoskip += ",";

        ovm.FoldersToSkip = folderstoskip;
        UsersBKList.RemoveAt(CurrentFolderSelection);
        EnableNext = (UsersBKList.Count > 0);

        ScheduleViewModel scheduleViewModel = ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]);
        scheduleViewModel.EnableMigrate = (scheduleViewModel.SchedList.Count > 0);
        scheduleViewModel.EnablePreview = scheduleViewModel.EnableMigrate;
    }


    public ICommand BackCommand 
    {
        get;
        private set;
    }

    private void Back()
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            lb.SelectedIndex = 3;
        }
    }

    public ICommand NextCommand 
    {
        get;
        private set;
    }

    private void Next()
    {
        if (!ValidateUsersList(true))
            return;

        ZimbraAPI zimbraAPI = new ZimbraAPI(isServer);
        if (ZimbraValues.zimbraValues.AuthToken.Length == 0)
        {
            MessageBox.Show("You must log on to the Zimbra server", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
            return;
        }
        SaveDomain();

        PublicfoldersViewModel publicfoldersViewModel = ((PublicfoldersViewModel)ViewModelPtrs[(int)ViewType.PUBFLDS]);
        ScheduleViewModel scheduleViewModel = ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]);

        scheduleViewModel.EnableProvGB = false;

        string acctName = ZimbraAccountName + '@' + ZimbraDomain;

        if (zimbraAPI.GetAccount(acctName) == 0)
        {
            UsersList[0].IsProvisioned = true;
            {
                SchedUser schd = new SchedUser(ZimbraAccountName, true);
                scheduleViewModel.SchedList.Add(schd);
                //scheduleViewModel.SchedList[0].isProvisioned = true;
            }
            scheduleViewModel.SchedfolderList[0].isProvisioned = true;    // get (SchedList) in schedule view model will set again
        }
        else 
        if (zimbraAPI.LastError.IndexOf("no such account") != -1)
        {
            UsersList[0].IsProvisioned = false;     // get (SchedList) in schedule view model will set again
            scheduleViewModel.SchedList[0].isProvisioned = false;
            scheduleViewModel.SchedfolderList[0].isProvisioned = false;
            if (!scheduleViewModel.EnableProvGB)
            {
                scheduleViewModel.EnableProvGB = true;
            }
        }
        else
        {
            MessageBox.Show(string.Format("Error accessing account {0}: {1}", acctName, zimbraAPI.LastError), "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
        }

        //Logic to get the index of defaulf COS from CosList.
        for (int i = 0; i < scheduleViewModel.CosList.Count; i++)
        {
            if (scheduleViewModel.CosList[i].CosName == "default")
            {
                ZimbraValues.GetZimbraValues().DefaultCosIndex = i;
                break;
            }
        }

        foreach (DomainInfo domaininfo in ZimbraValues.GetZimbraValues().ZimbraDomains)
        {
            if (domaininfo.DomainName == publicfoldersViewModel.DomainList[publicfoldersViewModel.CurrentDomainSelection])
            {
                if (domaininfo.zimbraDomainDefaultCOSId != "")
                {
                    for (int i = 0; i < scheduleViewModel.CosList.Count; i++)
                    {
                        if (domaininfo.zimbraDomainDefaultCOSId == scheduleViewModel.CosList[i].CosID)
                        {
                            scheduleViewModel.CurrentCOSSelection = i;
                            break;
                        }
                    }
                }
                else
                    scheduleViewModel.CurrentCOSSelection = ZimbraValues.GetZimbraValues().DefaultCosIndex;
            break;
            }
        }
        lb.SelectedIndex = 6;
    }

    public const int TYPE_USERNAME = 1;
    public const int TYPE_MAPNAME = 2;
    private bool isDuplicate(string nam, int type)
    {
        bool bRetval = false;
        int iHitCount = 0;

        for (int i = 0; i < UsersList.Count; i++)
        {
            string nam2 = (type == TYPE_USERNAME) ? UsersList[i].Username : UsersList[i].MappedName;

            if (nam == nam2)
            {
                iHitCount++;
                if (iHitCount == 2)
                {
                    bRetval = true;
                    break;
                }
            }
        }
        return bRetval;
    }

    public bool ValidateUsersList(bool bShowWarning)
    {
        // Make sure there are no blanks or duplicates in the list; remove them if there are.
        // If we get down to no items, disable the Next button.
         OptionsViewModel ovm = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);
         if (ovm.IsPublicFolders)
         {
             for (int i = UsersList.Count - 1; i >= 0; i--)
             {
                if (UsersList[i].MappedName.Length > 0)
                 {
                     if (isDuplicate(UsersList[i].MappedName, TYPE_MAPNAME))
                         UsersList.RemoveAt(i);
                 }
             }

             if (UsersList.Count == 0)
             {
                 if (bShowWarning)
                     MessageBox.Show("Please specify a source name", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Warning);

                 EnableNext = false;
                 return false;
             }
             return true;
         }
         else
         {
             for (int i = UsersList.Count - 1; i >= 0; i--)
             {
                 if (UsersList[i].Username.Length == 0)
                     UsersList.RemoveAt(i);
                 else 
                 if (isDuplicate(UsersList[i].Username, TYPE_USERNAME))
                     UsersList.RemoveAt(i);
                 else 
                 if (UsersList[i].MappedName.Length > 0)
                 {
                     if (isDuplicate(UsersList[i].MappedName, TYPE_MAPNAME))
                         UsersList.RemoveAt(i);
                 }
             }

             if (UsersList.Count == 0)
             {
                 if (bShowWarning)
                     MessageBox.Show("Please specify a source name", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Warning);

                 EnableNext = false;
                 return false;
             }
             return true;
         }
    }

    private void SaveDomain()
    {
        try
        {
            ScheduleViewModel scheduleViewModel = ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]);

            ZimbraDomain = DomainList[CurrentDomainSelection];
            if (scheduleViewModel.GetConfigFile().Length > 0)
            {
                if (CurrentDomainSelection > -1)
                {
                    if (File.Exists(scheduleViewModel.GetConfigFile()))
                    {
                        UpdateXmlElement(scheduleViewModel.GetConfigFile(), "UserProvision");
                    }
                    else
                    {
                        System.Xml.Serialization.XmlSerializer writer =
                            new System.Xml.Serialization.XmlSerializer(typeof(Config));

                        System.IO.StreamWriter file = new System.IO.StreamWriter(scheduleViewModel.GetConfigFile());
                        writer.Serialize(file, m_config);
                        file.Close();
                    }
                }
            }
        }
        catch (ArgumentOutOfRangeException)
        {
             MessageBox.Show("Please specify a domain", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Warning);
        }
    }


    // //////////////////////
    private ObservableCollection<PublicfoldersViewModel> usersbklist = new ObservableCollection<PublicfoldersViewModel>();
    public ObservableCollection<PublicfoldersViewModel> UsersBKList 
    {
        get { return usersbklist; }
    }

    private ObservableCollection<UsersViewModel> userslist = new ObservableCollection<UsersViewModel>();
    public ObservableCollection<UsersViewModel> UsersList
    {
        get { return userslist; }
    }

    private ObservableCollection<string> domainlist = new ObservableCollection<string>();
    public ObservableCollection<string> DomainList 
    {
        get { return domainlist; }
        set
        {
            domainlist = value;
        }
    }

    private ObservableCollection<DomainInfo> domaininfolist = new ObservableCollection<DomainInfo>();
    public ObservableCollection<DomainInfo> DomainInfoList 
    {
        get { return domaininfolist; }
        set
        {
            domaininfolist = value;
        }
    }

    public string MappedName 
    {
        get { return m_users.MappedName; }
        set
        {
            if (value == m_users.MappedName)
                return;
            m_users.MappedName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("MappedName"));
        }
    }

    public string ZimbraDomain 
    {
        get { return m_config.UserProvision.DestinationDomain; }
        set
        {
            if (value == m_config.UserProvision.DestinationDomain)
                return;
            m_config.UserProvision.DestinationDomain = value;

            OnPropertyChanged(new PropertyChangedEventArgs("ZimbraDomain"));
        }
    }
   
    public int CurrentFolderSelection
    {
        get { return folderselection; }
        set
        {
            folderselection = value;
            MinusEnabled = (value != -1);
            OnPropertyChanged(new PropertyChangedEventArgs("CurrentFolderSelection"));
        }
    }

    public int CurrentDomainSelection 
    {
        get { return domainselection; }
        set
        {
            domainselection = value;

            OnPropertyChanged(new PropertyChangedEventArgs("CurrentDomainSelection"));
        }
    }

    private int domainselection;
    private int folderselection;
    private string folderstoskip;

    private bool minusEnabled; // Controls whether Remove button is enabled
    public bool MinusEnabled 
    {
        get { return minusEnabled; }
        set
        {
            minusEnabled = value;
            OnPropertyChanged(new PropertyChangedEventArgs("MinusEnabled"));
        }
    }

    private bool plusEnabled; // Controls whether Get Public Folders button is enabled
    public bool PlusEnabled
    {
        get { return plusEnabled; }
        set
        {
            plusEnabled = value;
            OnPropertyChanged(new PropertyChangedEventArgs("PlusEnabled"));
        }
    }

    private string CsvDelimiter;

    private string m_publicFolderName;
    public string PublicFolderName
    {
        get { return m_publicFolderName; }
        set { m_publicFolderName = value; }

    }

    public string CSVDelimiter
    {
        get { return CsvDelimiter; }
        set { CsvDelimiter = value; }
    }

    private bool isProvisioned;
    public bool IsProvisioned 
    {
        get { return isProvisioned; }
        set
        {
            isProvisioned = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsProvisioned"));
        }
    }

    private bool enableNext;
    public bool EnableNext 
    {
        get { return enableNext; }
        set
        {
            enableNext = value;
            OnPropertyChanged(new PropertyChangedEventArgs("EnableNext"));
        }
    }

    private bool domainsFilledIn;
    public bool DomainsFilledIn
    {
        get { return domainsFilledIn; }
        set
        {
            domainsFilledIn = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DomainsFilledIn"));
        }
    }
}
}
