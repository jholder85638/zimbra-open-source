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

using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections;
using System.Diagnostics;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Markup;
using System.Windows.Media;
using System.Windows.Navigation;
using System.Windows;
using System.Security.Principal;
using CssLib;


using MVVM.Model;
using MVVM.ViewModel;

namespace MVVM.View
{
// / <summary>
// / The main view window (A = App, as opposed to MainViewW which is Web app?)
// / </summary>
public partial class MainViewA
{
    private BaseViewModel m_baseViewModel;
    private IntroViewModel m_introViewModel;

    public static bool RunningAsAdministrator()
    {
        return (new WindowsPrincipal(WindowsIdentity.GetCurrent())).IsInRole(WindowsBuiltInRole.Administrator);
    }  

    public MainViewA()
    {
        // TESTPOINT_MIGRATION_LOGGING_INIT_MIGRATION_LOG_WIZARD Initializes main log for MigrationWizard
        Log.InitializeLogging(Log.Level.Trace); // <- use Trace level for startup (and swicth to Info at bottom of this method)

        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("MainViewA.MainViewA"))
        {
            Log.info("Running as administrator?", RunningAsAdministrator()?"Yes":"No");

            InitializeComponent();

            // Default is "server migration"
            Application.Current.Properties["migrationmode"] = "server";

            m_baseViewModel = new BaseViewModel();

            // Setup intro page
            Intro intro = new Intro();

            m_introViewModel                = new IntroViewModel(lbMode);
            m_introViewModel.Name           = "IntroViewModel";
            m_introViewModel.ViewTitle      = "Welcome";
            m_introViewModel.lb             = lbMode;
            m_introViewModel.isBrowser      = false;
            m_introViewModel.WelcomeMsg     = intro.WelcomeMsg;
            m_introViewModel.InstallDir     = intro.InstallDir;

            m_introViewModel.SetupViews(false);
            m_introViewModel.AddViews(false);

            lbMode.SelectedIndex = 0;
            DataContext = m_introViewModel;

            // Switch back to Info log level
            m_introViewModel.SelectedLogLevelCBItem.LogLevelEnum = Log.Level.Info;
        }
    }

    private void ViewListTB_MouseDown(object sender, MouseButtonEventArgs e)
    {
        if (m_introViewModel.mw == null)
            m_introViewModel.Next();
        else 
        if (m_introViewModel.mw.MigrationType == null)
            m_introViewModel.Next();

        TextBlock tb = (TextBlock)sender;
        if (tb.Text == "Migrate")
        {
            UsersViewModel usersViewModel = m_introViewModel.GetUsersViewModel();
            usersViewModel.ValidateUsersList(false);
        }
    }
}
}
