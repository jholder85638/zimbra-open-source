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
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Input;
using System.Windows;
using System;
using System.Xml;

namespace MVVM.ViewModel
{


    public class ConfigViewModelUDest : BaseViewModel
    {
        public ConfigViewModelUDest()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock("ConfigViewModelUDest.ConfigViewModelUDest"))
            {
                if (isDesktop)
                    DesktopLoad();

                // Install ActionCommands 
                this.BackCommand = new ActionCommand(this.Back, () => true);
                this.NextCommand = new ActionCommand(this.Next, () => true);
            }
        }

        public void LoadConfig(Config config)
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                ZimbraServerHostName = config.ZimbraServer.Hostname;
                ZimbraPort = config.ZimbraServer.Port;
                ZimbraUser = config.ZimbraServer.UserAccount;
                ZimbraUserPasswd = config.ZimbraServer.UserPassword;
                ZimbraSSL = config.ZimbraServer.UseSSL;
            }
        }

        public void DesktopLoad()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                string appDataFolder = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
                string filePath = Path.Combine(appDataFolder, "Zimbra\\Zimbra Desktop\\conf\\localconfig.xml");

                if (File.Exists(filePath))
                {
                    try
                    {
                        XmlDocument xml = new XmlDocument();
                        xml.Load(filePath);  // suppose that str string contains "<Names>...</Names>"

                        XmlNodeList xnList = xml.SelectNodes("//localconfig/key[@name='zdesktop_installation_key']");
                        string pwd = "";
                        foreach (XmlNode xn in xnList)
                        {
                            pwd = xn.InnerText;
                        }

                        xnList = xml.SelectNodes("//localconfig/key[@name='zimbra_admin_service_port']");
                        string port = "";
                        foreach (XmlNode xn in xnList)
                        {
                            port = xn.InnerText;
                        }


                        ZimbraServerHostName = "localhost";
                        ZimbraPort = port;
                        ZimbraUser = "local@host.local";
                        ZimbraUserPasswd = pwd;
                        ZimbraSSL = false;

                        if ((this.ZimbraServerHostName.Length == 0) || (this.ZimbraPort.Length == 0))
                        {
                            MessageBox.Show("Please fill in the host name and port", "Zimbra Migration",
                                            MessageBoxButton.OK, MessageBoxImage.Error);
                            return;
                        }
                        try
                        {
                            System.Net.IPAddress address = System.Net.IPAddress.Parse(ZimbraServerHostName);
                            MessageBox.Show("Please enter a valid host name rather than an IP address",
                                            "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                            return;
                        }
                        catch (Exception)
                        {
                        }


                        ZimbraAPI zimbraAPI = new ZimbraAPI(false);
                        int stat = -1;
                        try
                        {
                            stat = zimbraAPI.Logon(this.ZimbraServerHostName, this.ZimbraPort, this.ZimbraUser, this.ZimbraUserPasswd, this.ZimbraSSL, false);
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
                                zimbraAPI.GetInfo();
                                lb.SelectedIndex = 3;
                            }
                        }
                        else
                        {
                            MessageBox.Show(string.Format("Logon Unsuccessful: {0}", zimbraAPI.LastError), "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        }

                    }
                    catch (Exception e)
                    {
                        string temp = string.Format("Incorrect configuration file format.\n{0}", e.Message);
                        MessageBox.Show(temp, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        //fileRead.Close();
                        return;
                    }
                }
                else
                {
                    MessageBox.Show("Please Install Zimbra Desktop before executing migration ", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    lb.SelectedIndex = 1;
                    throw new Exception("Can't find Zimbra Desktop installation");
                }
            }
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
                
                Log.info("ZimbraHostName: " + this.ZimbraServerHostName + "  ZimbraPort: " + this.ZimbraPort);
                Log.info("ZimbraUser: " + this.ZimbraUser + "  SSL: " + this.ZimbraSSL);

                try
                {
                    System.Net.IPAddress address = System.Net.IPAddress.Parse(ZimbraServerHostName);
                    MessageBox.Show("Please enter a valid host name rather than an IP address", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }
                catch (Exception)
                {
                }

                // ======================================================================
                // Initial server logon
                // ======================================================================
                //Debug.WriteLine("Connecting to server...");
                ZimbraAPI zimbraAPI = new ZimbraAPI(false);
                int stat = -1;
                try
                {
                    stat = zimbraAPI.Logon(this.ZimbraServerHostName, this.ZimbraPort, this.ZimbraUser, this.ZimbraUserPasswd, this.ZimbraSSL, false);
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
                        zimbraAPI.GetInfo();
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

        public string ZimbraUser
        {
            get { return m_config.ZimbraServer.UserAccount; }
            set
            {
                if (value == m_config.ZimbraServer.UserAccount)
                    return;
                m_config.ZimbraServer.UserAccount = value;

                OnPropertyChanged(new PropertyChangedEventArgs("ZimbraUser"));
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

        public string ZimbraUserPasswd
        {
            get { return m_config.ZimbraServer.UserPassword; }
            set
            {
                if (value == m_config.ZimbraServer.UserPassword)
                    return;
                m_config.ZimbraServer.UserPassword = value;

                OnPropertyChanged(new PropertyChangedEventArgs("ZimbraUserPasswd"));
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
