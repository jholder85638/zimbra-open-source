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
    public class ConfigViewModelSDest : BaseViewModel
    {
        public ConfigViewModelSDest()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock("ConfigViewModelSDest.ConfigViewModelSDest"))
            {
                this.BackCommand = new ActionCommand(this.Back, () => true);
                this.NextCommand = new ActionCommand(this.Next, () => true);
            }
        }

        public void LoadConfig(Config config)
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                ZimbraServerHostName    = config.ZimbraServer.Hostname;
                ZimbraPort              = config.ZimbraServer.Port;
                ZimbraAdmin             = config.ZimbraServer.AdminID;
                ZimbraAdminPasswd       = config.ZimbraServer.AdminPwd;
                ZimbraSSL               = config.ZimbraServer.UseSSL;
                savedDomain             = config.UserProvision.DestinationDomain;
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
                lb.SelectedIndex = 1;
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
                if ((this.ZimbraServerHostName.Length == 0) || (this.ZimbraPort.Length == 0))
                {
                    MessageBox.Show("Please fill in the host name and port", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }
                Log.info("ZimbraHostName: " + this.ZimbraServerHostName +"  ZimbraPort: " + this.ZimbraPort);
                Log.info("ZimbraAdmin: " + this.ZimbraAdmin + "  SSL: " + this.ZimbraSSL);

                ZimbraAPI zimbraAPI = new ZimbraAPI(true);
                int stat = -1;
                try
                {
                    stat = zimbraAPI.Logon(this.ZimbraServerHostName, this.ZimbraPort, this.ZimbraAdmin, this.ZimbraAdminPasswd, this.ZimbraSSL, true);
                }
                catch (Exception e)
                {
                    MessageBox.Show(e.Message, "Logon", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }

                if (stat == 0)
                {
                    string authToken = ZimbraValues.GetZimbraValues().AuthToken;
                    if (authToken.Length > 0)
                    {
                        UsersViewModel usersViewModel                 = ((UsersViewModel)ViewModelPtrs[(int)ViewType.USERS]);
                        ScheduleViewModel scheduleViewModel           = ((ScheduleViewModel)ViewModelPtrs[(int)ViewType.SCHED]);
                        PublicfoldersViewModel publicfoldersViewModel = ((PublicfoldersViewModel)ViewModelPtrs[(int)ViewType.PUBFLDS]);


                        string currentDomain = (usersViewModel.DomainInfoList.Count > 0) ? usersViewModel.DomainInfoList[usersViewModel.CurrentDomainSelection].DomainName : "";
                        currentDomain = (publicfoldersViewModel.DomainInfoList.Count > 0) ? publicfoldersViewModel.DomainInfoList[publicfoldersViewModel.CurrentDomainSelection].DomainName : "";

                        usersViewModel.DomainInfoList.Clear();
                        usersViewModel.DomainList.Clear();
                        publicfoldersViewModel.DomainInfoList.Clear();
                        publicfoldersViewModel.DomainList.Clear();
                        scheduleViewModel.CosList.Clear();

                        // ----------------------------------------------
                        // Get domains
                        // ----------------------------------------------
                        zimbraAPI.GetAllDomains();
                        foreach (DomainInfo domaininfo in ZimbraValues.GetZimbraValues().ZimbraDomains)
                        {
                            string dName = domaininfo.DomainName;
                            usersViewModel.DomainInfoList.Add(new DomainInfo(domaininfo.DomainName, domaininfo.DomainID, domaininfo.zimbraDomainDefaultCOSId));
                            usersViewModel.DomainList.Add(dName);

                            publicfoldersViewModel.DomainInfoList.Add(new DomainInfo(domaininfo.DomainName, domaininfo.DomainID, domaininfo.zimbraDomainDefaultCOSId));
                            publicfoldersViewModel.DomainList.Add(dName);

                            if (dName == currentDomain)
                            {
                                usersViewModel.CurrentDomainSelection = usersViewModel.DomainInfoList.Count;
                                publicfoldersViewModel.CurrentDomainSelection = publicfoldersViewModel.DomainInfoList.Count;
                            }
                        }

                        usersViewModel.DomainsFilledIn = true;
                        publicfoldersViewModel.DomainsFilledIn = true;

                        // ----------------------------------------------
                        // Get COS
                        // ----------------------------------------------
                        zimbraAPI.GetAllCos();
                        foreach (CosInfo cosinfo in ZimbraValues.GetZimbraValues().COSes)
                        {
                            scheduleViewModel.CosList.Add(new CosInfo(cosinfo.CosName, cosinfo.CosID));
                        }
                        lb.SelectedIndex = 3;
                    }
                }
                else
                {
                    MessageBox.Show(string.Format("Logon Unsuccessful: {0}", zimbraAPI.LastError), "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
        }

        public string ZimbraPort
        {
            get { return m_config.ZimbraServer.Port; }
            set
            {
                if (value == m_config.ZimbraServer.Port)
                    return;
                m_config.ZimbraServer.Port = value;

                OnPropertyChanged(new PropertyChangedEventArgs("ZimbraPort"));
            }
        }

        public string ZimbraServerHostName
        {
            get { return m_config.ZimbraServer.Hostname; }
            set
            {
                if (value == m_config.ZimbraServer.Hostname)
                    return;
                m_config.ZimbraServer.Hostname = value;

                OnPropertyChanged(new PropertyChangedEventArgs("ZimbraServerHostName"));
            }
        }

        public string ZimbraAdmin
        {
            get { return m_config.ZimbraServer.AdminID; }
            set
            {
                if (value == m_config.ZimbraServer.AdminID)
                    return;
                m_config.ZimbraServer.AdminID = value;

                OnPropertyChanged(new PropertyChangedEventArgs("ZimbraAdmin"));
            }
        }

        public string ZimbraAdminPasswd
        {
            get { return m_config.ZimbraServer.AdminPwd; }
            set
            {
                if (value == m_config.ZimbraServer.AdminPwd)
                    return;
                m_config.ZimbraServer.AdminPwd = value;

                OnPropertyChanged(new PropertyChangedEventArgs("ZimbraAdminPasswd"));
            }
        }

        public bool ZimbraSSL
        {
            get { return m_config.ZimbraServer.UseSSL; }
            set
            {
                if (value == m_config.ZimbraServer.UseSSL)
                    return;
                m_config.ZimbraServer.UseSSL = value;

                OnPropertyChanged(new PropertyChangedEventArgs("ZimbraSSL"));
            }
        }
    }
}
