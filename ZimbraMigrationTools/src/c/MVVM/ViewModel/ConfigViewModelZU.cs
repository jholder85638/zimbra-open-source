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
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Input;
using System.Windows;
using System;

namespace MVVM.ViewModel
{
    public class ConfigViewModelZU : BaseViewModel
    {
        public ConfigViewModelZU()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock("ConfigViewModelZU.ConfigViewModelZU"))
            {
                this.GetPSTCommand = new ActionCommand(this.GetPST, () => true);
                this.BackCommand = new ActionCommand(this.Back, () => true);
                this.NextCommand = new ActionCommand(this.Next, () => true);

                Isprofile = true;
                IspST = false;

                CSEnableNext = false;
            }
        }

        public ICommand GetPSTCommand
        {
            get;
            private set;
        }
        private void GetPST()
        {
            Microsoft.Win32.OpenFileDialog pstDialog = new Microsoft.Win32.OpenFileDialog();
            pstDialog.Filter = "PST Files(*.pst)|*.pst";
            pstDialog.CheckFileExists = true;
            pstDialog.Multiselect = false;
            if (pstDialog.ShowDialog() == true)
            {
                string result = pstDialog.FileName;

                PSTFile = result;                   // update the UI
            }
        }

        public void LoadConfig(Config config)
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                if (config.SourceServer.UseProfile)
                {
                    Isprofile = true;
                    IspST = false;
                    OutlookProfile = config.SourceServer.Profile;

                    if (ProfileList.Count > 0)
                        CurrentProfileSelection = (OutlookProfile == null) ? 0 : ProfileList.IndexOf(OutlookProfile);
                    else
                        ProfileList.Add(OutlookProfile);
                }
                else
                {
                    Isprofile = false;
                    IspST = true;
                    PSTFile = config.SourceServer.DataFile;
                }
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

        private bool IsProfile;
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

        private bool IsPST;
        public bool IspST
        {
            get { return IsPST; }
            set
            {
                IsPST = value;
                CSEnableNext = (IsPST) ? true : ProfileList.Count > 0;
                OptionsViewModel ovm = ((OptionsViewModel)ViewModelPtrs[(int)ViewType.OPTIONS]);
                if (ovm != null)
                {
                    ovm.OEnableRulesAndOOO = !IsPST;
                    if (isDesktop)
                        ovm.IsZDesktop = true;
                }

                OnPropertyChanged(new PropertyChangedEventArgs("IspST"));
            }
        }

        private void Next()
        {
            using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                if (IsPST)
                {
                    if (PSTFile.Length == 0)
                    {
                        MessageBox.Show("Please enter a PST file", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }
                    else
                        if (!File.Exists(PSTFile))
                        {
                            string temp = string.Format("{0} does not exist", PSTFile);
                            MessageBox.Show(temp, "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                            return;
                        }
                    if (isDesktop)
                    {

                        try
                        {

                            ((ConfigViewModelUDest)ViewModelPtrs[(int)ViewType.USRDEST]).DesktopLoad();
                        }
                        catch (Exception)
                        {
                            lb.SelectedIndex = 1;
                            return;
                        }
                    }
                }
                else
                    if (CurrentProfileSelection == -1)
                    {
                        MessageBox.Show("Please select a valid profile", "Zimbra Migration", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }

                lb.SelectedIndex = 3;
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
                // m_config.mailServer.ProfileName= value;
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
        public string PSTFile
        {
            get { return m_config.SourceServer.DataFile; }
            set
            {
                if (value == m_config.SourceServer.DataFile)
                    return;
                m_config.SourceServer.DataFile = value;
                OnPropertyChanged(new PropertyChangedEventArgs("PSTFile"));
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
