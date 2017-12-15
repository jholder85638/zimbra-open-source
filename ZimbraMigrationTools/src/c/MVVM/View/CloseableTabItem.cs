/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2013, 2014, 2015, 2016 Synacor, Inc.
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
using System.Text;
using System.Windows.Controls.Primitives;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using System.Windows.Media;
using System.Windows.Shapes;
using System.Windows;
using System;

namespace MVVM.View.CTI
{
public class CloseableTabItem: TabItem
{
    static CloseableTabItem()
    {
        // This OverrideMetadata call tells the system that this element wants to provide a style that is different than its base class.
        // This style is defined in themes\generic.xaml
        DefaultStyleKeyProperty.OverrideMetadata(typeof (CloseableTabItem), new FrameworkPropertyMetadata(typeof (CloseableTabItem)));
    }

    public static readonly RoutedEvent CloseTabEvent = EventManager.RegisterRoutedEvent("CloseTab", RoutingStrategy.Bubble, typeof (RoutedEventHandler),typeof (CloseableTabItem));

    public event RoutedEventHandler CloseTab 
    {
        add { AddHandler(CloseTabEvent, value); }
        remove { RemoveHandler(CloseTabEvent, value); }
    }

    public override void OnApplyTemplate()
    {
        base.OnApplyTemplate();

        Button closeButton = base.GetTemplateChild("PART_Close") as Button;

        if (closeButton != null)
            closeButton.Click += new System.Windows.RoutedEventHandler(closeButton_Click);
    }

    void closeButton_Click(object sender, System.Windows.RoutedEventArgs e)
    {
        this.RaiseEvent(new RoutedEventArgs(CloseTabEvent, this));
    }
}
}
