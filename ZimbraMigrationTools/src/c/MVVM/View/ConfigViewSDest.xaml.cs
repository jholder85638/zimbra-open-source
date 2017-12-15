/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2014, 2015, 2016 Synacor, Inc.
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

using MVVM.ViewModel;
using System.Windows;

namespace MVVM.View
{
public partial class ConfigViewSDest
{
    public ConfigViewSDest()
    {
        InitializeComponent();
    }

    // Kind of a drag that we have to put these next 2 methods in here, but PasswordBox is not a dependency property,
    // so we can't bind to the model.  Doing this for now -- should probably use an attached property later
    private ConfigViewModelSDest ViewModel 
    {
        get { return DataContext as ConfigViewModelSDest; }
    }

    private void pb_PasswordChanged(object sender, RoutedEventArgs e)
    {
        ViewModel.ZimbraAdminPasswd = passwordBox1.Password;
    }
}
}
