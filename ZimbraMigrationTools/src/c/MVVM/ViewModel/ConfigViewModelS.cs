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
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Input;
using System.Windows;
using System;

namespace MVVM.ViewModel
{
    public class ConfigViewModelS : BaseViewModel
    {
        internal const int PROFILE_MODE = 1;
        internal const int EXCHSVR_MODE = 2;

        public ConfigViewModelS()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock("ConfigViewModelS.ConfigViewModelS"))
            {
                // Install ActionCommands 
                this.BackCommand = new ActionCommand(this.Back, () => true);
                this.NextCommand = new ActionCommand(this.Next, () => true);

                Isprofile = true;
                IsmailServer = false;
                CSEnableNext = false;
                iMailSvrInitialized = -1;
                IscfgPublicFolder = false;
            }
        }

        public void LoadConfig(Config config)
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                if (config.SourceServer.UseProfile)
                {
                    Isprofile = true;
                    IsmailServer = false;
                    OutlookProfile = config.SourceServer.Profile;
                    if (ProfileList.Count > 0)
                        CurrentProfileSelection = (OutlookProfile == null) ? 0 : ProfileList.IndexOf(OutlookProfile);
                    else
                        ProfileList.Add(OutlookProfile);
                }
                else
                {
                    Isprofile = false;
                    IsmailServer = true;
                    MailServerHostName = config.SourceServer.Hostname;
                    MailServerAdminID = config.SourceServer.AdminID;
                    MailServerAdminPwd = config.SourceServer.AdminPwd;
                }
                savedDomain = config.UserProvision.DestinationDomain;
                IscfgPublicFolder = config.AdvancedImportOptions.IsPublicFolders;
            }
        }

        public ICommand BackCommand
        {
            get;
            private set;
        }

        private void Back()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                lb.SelectedIndex = 0;
            }
        }

        public ICommand NextCommand
        {
            get;
            private set;
        }

        private void Next()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                string ret = "";
                CSMigrationWrapper mw = ((IntroViewModel)ViewModelPtrs[(int)ViewType.INTRO]).mw;

                if (IsProfile)
                {
                    // -------------------------------------------------------------------------------------
                    // User opted for Profile
                    // -------------------------------------------------------------------------------------
                    if (CurrentProfileSelection == -1)
                    {
                        MessageBox.Show("Please select a valid profile", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }

                    OutlookProfile = ProfileList[CurrentProfileSelection];
                    if (iMailSvrInitialized == EXCHSVR_MODE)
                    {
                        MessageBox.Show("You are already logged in via Exchange Server credentials", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }
                    //  if ((!IscfgPublicFolder)&&(iMailSvrInitialized == -1))
                    //    ret = mw.InitCSMigrationWrapper(ProfileList[CurrentProfileSelection], "", "");
                }
                else
                {
                    // -------------------------------------------------------------------------------------
                    // User opted to enter exch credentials directly
                    // -------------------------------------------------------------------------------------
                    if (iMailSvrInitialized == PROFILE_MODE)
                    {
                        MessageBox.Show("You are already logged in via an Outlook Profile", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }

                    // Need to init CSMigrationWrapper
                    if (iMailSvrInitialized == -1)
                    {
                        if (   (MailServerHostName.Length == 0)
                            || (MailServerAdminID.Length  == 0)
                            || (MailServerAdminPwd.Length == 0))
                        {
                            MessageBox.Show("Please enter all source mail server credentials", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                            return;
                        }
                        ret = mw.InitCSMigrationWrapper(MailServerHostName, MailServerAdminID, MailServerAdminPwd);
                    }
                }
                if (ret.Length > 0)
                {
                    MessageBox.Show(ret, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    ret = mw.UninitCSMigrationWrapper();
                    return;
                }
                iMailSvrInitialized = (IsProfile) ? PROFILE_MODE : EXCHSVR_MODE;

                lb.SelectedIndex = 2;
            }
        }

        private int iMailSvrInitialized;
        private bool IsProfile;
        private bool IsMailServer;
        private bool IsCfgPublicFolder;
        public bool IsMailServerInitialized
        {
            get { return iMailSvrInitialized != -1; }
        }

        public bool IsmailServer
        {
            get { return IsMailServer; }
            set
            {
                IsMailServer = value;
                CSEnableNext = (IsMailServer) ? true : ProfileList.Count > 0;
                OnPropertyChanged(new PropertyChangedEventArgs("IsmailServer"));
            }
        }

        public bool IscfgPublicFolder
        {
            get { return IsCfgPublicFolder; }
            set
            {
                IsCfgPublicFolder = value;
            }
        }

        public bool Isprofile
        {
            get { return IsProfile; }
            set
            {
                IsProfile = value;
                CSEnableNext = (IsProfile) ? ProfileList.Count > 0 : true;
                OnPropertyChanged(new PropertyChangedEventArgs("Isprofile"));
            }
        }

        public string OutlookProfile
        {
            get { return m_config.SourceServer.Profile; }
            set
            {
                if (value == m_config.SourceServer.Profile)
                    return;
                m_config.SourceServer.Profile = value;
                OnPropertyChanged(new PropertyChangedEventArgs("OutlookProfile"));
            }
        }

        private ObservableCollection<string> profilelist = new ObservableCollection<string>();
        public ObservableCollection<string> ProfileList
        {
            get { return profilelist; }
            set
            {
                profilelist = value;
            }
        }

        public int CurrentProfileSelection
        {
            get { return profileselection; }
            set
            {
                profileselection = value;
                CSEnableNext = (value != -1);

                OnPropertyChanged(new PropertyChangedEventArgs("CurrentProfileSelection"));
            }
        }

        private int profileselection;
        public string MailServerHostName
        {
            get { return m_config.SourceServer.Hostname; }
            set
            {
                if (value == m_config.SourceServer.Hostname)
                    return;
                m_config.SourceServer.Hostname = value;

                OnPropertyChanged(new PropertyChangedEventArgs("MailServerHostName"));
            }
        }

        public string MailServerAdminID
        {
            get { return m_config.SourceServer.AdminID; }
            set
            {
                if (value == m_config.SourceServer.AdminID)
                    return;
                m_config.SourceServer.AdminID = value;

                OnPropertyChanged(new PropertyChangedEventArgs("MailServerAdminID"));
            }
        }

        public string MailServerAdminPwd
        {
            get { return m_config.SourceServer.AdminPwd; }
            set
            {
                if (value == m_config.SourceServer.AdminPwd)
                    return;
                m_config.SourceServer.AdminPwd = value;

                OnPropertyChanged(new PropertyChangedEventArgs("MailServerAdminPwd"));
            }
        }

        private bool csenableNext;
        public bool CSEnableNext
        {
            get { return csenableNext; }
            set
            {
                csenableNext = value;
                OnPropertyChanged(new PropertyChangedEventArgs("CSEnableNext"));
            }
        }
    }
}
