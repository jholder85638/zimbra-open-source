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
using System;
using System.Windows;

namespace ZimbraMigration
{
public partial class App: Application
{
    private void Application_Exit(object sender, ExitEventArgs e)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            CSMigrationWrapper mw = (CSMigrationWrapper)Properties["mw"];
            if (mw != null)
            {
                if (mw.csSourceAccount != null)  // FBS bug 71047 -- 3/20/12
                    mw.csSourceAccount.UninitSourceAccount();

                string s = mw.UninitCSMigrationWrapper();
                if (s.Length > 0)
                {
                    bool retval = mw.AvoidInternalErrors(s);
                    if (!retval)
                        MessageBox.Show(s, "Shutdown error", MessageBoxButton.OK, MessageBoxImage.Error);

                }
            }
        }
    }
}
}
