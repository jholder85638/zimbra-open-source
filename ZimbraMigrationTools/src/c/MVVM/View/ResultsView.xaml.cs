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
using MVVM.View.CTI;
using MVVM.ViewModel;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections;
using System.IO;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Input;
using System.Windows.Markup;
using System.Windows.Media;
using System.Windows;
using System;

namespace MVVM.View
{
public partial class ResultsView
{
    ListView[] urListView = new ListView[200];
    ListBox[]  lbErrors   = new ListBox[200];

    ProgressBar userProgressBar = null;
    Label userStatusMsg = null;
    Label userErrsMsg = null;

    int iTabCount = 0;

    public ResultsView()
    {
        InitializeComponent();
        this.AddHandler(CloseableTabItem.CloseTabEvent, new RoutedEventHandler(this.CloseTab));
    }

    private AccountResultsViewModel ViewModel 
    {
        get { return DataContext as AccountResultsViewModel; }
    }

    private int GetAcctNum(string hdr)
    {
        int accountnum = -1;

        for (int i = 0; i < ViewModel.AccountResultsList.Count; i++)
        {
            if (hdr == ViewModel.AccountResultsList[i].AccountName)
            {
                accountnum = ViewModel.AccountResultsList[i].GetAccountNum();
                break;
            }
        }
        return accountnum;
    }

    protected void HandleDoubleClick(object sender, MouseButtonEventArgs e)
    // Opens a new tab for the account that was dbl-clicked
    {
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            if (iTabCount == 16)
            {
                MessageBox.Show(string.Format("Only 16 tabs may be open at a time", MessageBoxButton.OK, MessageBoxImage.Error));
                return;
            }

            ListViewItem lvi = sender as ListViewItem;
            var content = lvi.Content as AccountResultsViewModel;
            TabControl tabCtrl = FindParent(lvi, typeof(TabControl)) as TabControl;

            // ===================================================================
            // Create the new tab
            // ===================================================================
            CloseableTabItem userItem = new CloseableTabItem();

            userItem.Header = content.AccountName;

            // get accountnum so we can keep the listboxes and listviews straight
            int accountnum = GetAcctNum((string)userItem.Header);
            AccountResultsViewModel ar = ViewModel.AccountResultsList[accountnum];

            try
            {
                if (urListView[accountnum] != null)
                {
                    for (int i = 0; i < tabCtrl.Items.Count; i++)
                    {
                        TabItem item = (TabItem)tabCtrl.Items[i];
                        if (item.Header.ToString() == content.AccountName)
                        {
                            tabCtrl.SelectedIndex = i;
                            break;
                        }
                    }
                    return;
                }
                iTabCount++;


                // ===================================================================
                // Create controls on the new tab
                // ===================================================================

                // =================================
                // Client area = 4-row Grid 
                // =================================
                Grid urGrid = new Grid();

                // Row 1 holds top listview
                RowDefinition rowDef1 = new RowDefinition();
                rowDef1.Height = new GridLength(1, GridUnitType.Star);
                urGrid.RowDefinitions.Add(rowDef1);

                // Row 2 holds errors label
                RowDefinition rowDef2 = new RowDefinition();
                rowDef2.Height = GridLength.Auto;
                urGrid.RowDefinitions.Add(rowDef2);

                // Row 3 holds Problems listview
                RowDefinition rowDef3 = new RowDefinition();
                rowDef3.MaxHeight = 145;
                urGrid.RowDefinitions.Add(rowDef3);

                // Row 4 holds progress bar
                RowDefinition rowDef4 = new RowDefinition();
                rowDef4.Height = GridLength.Auto;
                urGrid.RowDefinitions.Add(rowDef4);

                // Row 5 holds status msg
                RowDefinition rowDef5 = new RowDefinition();
                rowDef5.Height = GridLength.Auto;
                urGrid.RowDefinitions.Add(rowDef5); 

                // =================================================================
                // Row 1 contains Folders ListView
                // =================================================================
                urListView[accountnum] = new ListView();
                urListView[accountnum].FontSize = 11;
                urListView[accountnum].SetValue(Grid.RowProperty, 0);
                urListView[accountnum].Margin = new Thickness(5);
                urListView[accountnum].Name = "lstFolderResults";

                // ListView contains a GridView
                GridView urGridView = new GridView();

                // ---------------------------------------------------------------------
                // Columns (set up columns widths so we won't get a horizontal scrollbar)
                // ---------------------------------------------------------------------
                // DCB How to add a new column
                //
                // 1. Add a new block below
                // 2. Add a source for the binding in "class FolderResultsViewModel" (name of the member must match binding name!)
                // 3. 2 reqs you add a member to "class FolderResults"
                // 4. Add member in class MigrationFolder in "MigrationAccount.cs"  (This is the member CSMigrationWrapper will assign to)
                // 5. Add a block to folder_onchanged (this is called when 4 is updated, and transfers the value to the UI)

                // FolderName Column
                GridViewColumnHeader gvc1H = new GridViewColumnHeader();
                gvc1H.FontSize = 11;
                gvc1H.Width = 190;
                gvc1H.Content = " Folder";
                gvc1H.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvc1 = new GridViewColumn();
                gvc1.DisplayMemberBinding = new Binding("FolderName");
                gvc1.Header = gvc1H;
                urGridView.Columns.Add(gvc1);

                // Processed Column
                GridViewColumnHeader gvchProgress = new GridViewColumnHeader();
                gvchProgress.FontSize = 11;
                gvchProgress.Width = 120;
                gvchProgress.Content = " Processed";
                gvchProgress.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcProgress = new GridViewColumn();
                gvcProgress.DisplayMemberBinding = new Binding("FolderProgress");
                gvcProgress.Header = gvchProgress;
                urGridView.Columns.Add(gvcProgress);

                // Migrated Column
                GridViewColumnHeader gvchMigrated = new GridViewColumnHeader();
                gvchMigrated.FontSize = 11;
                gvchMigrated.Width = 50;
                gvchMigrated.Content = " Migrated";
                gvchMigrated.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcMigrated = new GridViewColumn();
                gvcMigrated.DisplayMemberBinding = new Binding("NumFolderItemsMigrated");
                gvcMigrated.Header = gvchMigrated;
                urGridView.Columns.Add(gvcMigrated);

                // Filtered Skips Column
                GridViewColumnHeader gvchSkipFilter = new GridViewColumnHeader();
                gvchSkipFilter.FontSize = 11;
                gvchSkipFilter.Width = 90;
                gvchSkipFilter.Content = "Skipped by Filter";
                gvchSkipFilter.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcSkipFilter = new GridViewColumn();
                gvcSkipFilter.DisplayMemberBinding = new Binding("NumFolderSkipsFilter");
                gvcSkipFilter.Header = gvchSkipFilter;
                urGridView.Columns.Add(gvcSkipFilter);

                // History Skips Column
                GridViewColumnHeader gvchSkipHistory = new GridViewColumnHeader();
                gvchSkipHistory.FontSize = 11;
                gvchSkipHistory.Width = 100;
                gvchSkipHistory.Content = "Skipped by History";
                gvchSkipHistory.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcSkipHistory = new GridViewColumn();
                gvcSkipHistory.DisplayMemberBinding = new Binding("NumFolderSkipsHistory");
                gvcSkipHistory.Header = gvchSkipHistory;
                urGridView.Columns.Add(gvcSkipHistory);

                // Errs Column
                GridViewColumnHeader gvchErrs = new GridViewColumnHeader();
                gvchErrs.FontSize = 11;
                gvchErrs.Width = 40;
                gvchErrs.Content = " Errs";
                gvchErrs.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcErrs = new GridViewColumn();
                gvcErrs.DisplayMemberBinding = new Binding("NumFolderErrs");                             // Binding names MUST MATCH THE NAMES IN FolderResltsViewModel.cs
                gvcErrs.Header = gvchErrs;
                urGridView.Columns.Add(gvcErrs);

                // Warns Column
                GridViewColumnHeader gvchWarns = new GridViewColumnHeader();
                gvchWarns.FontSize = 11;
                gvchWarns.Width = 40;
                gvchWarns.Content = " Warns";
                gvchWarns.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcWarns = new GridViewColumn();
                gvcWarns.DisplayMemberBinding = new Binding("NumFolderWarns");
                gvcWarns.Header = gvchWarns;
                urGridView.Columns.Add(gvcWarns);

                // Min
                GridViewColumnHeader gvchMin = new GridViewColumnHeader();
                gvchMin.FontSize = 11;
                gvchMin.Width = 60;
                gvchMin.Content = " Min (ms)";
                gvchMin.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcMin = new GridViewColumn();
                gvcMin.DisplayMemberBinding = new Binding("FolderItemMillisecsMin");
                gvcMin.Header = gvchMin;
                urGridView.Columns.Add(gvcMin);

                // Avg
                GridViewColumnHeader gvchAvg = new GridViewColumnHeader();
                gvchAvg.FontSize = 11;
                gvchAvg.Width = 60;
                gvchAvg.Content = " Avg (ms)";
                gvchAvg.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcAvg = new GridViewColumn();
                gvcAvg.DisplayMemberBinding = new Binding("FolderItemMillisecsAvg");
                gvcAvg.Header = gvchAvg;
                urGridView.Columns.Add(gvcAvg);

                // Max
                GridViewColumnHeader gvchMax = new GridViewColumnHeader();
                gvchMax.FontSize = 11;
                gvchMax.Width = 60;
                gvchMax.Content = " Max (ms)";
                gvchMax.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcMax = new GridViewColumn();
                gvcMax.DisplayMemberBinding = new Binding("FolderItemMillisecsMax");
                //gvcMaxC.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Center;
                gvcMax.Header = gvchMax;
                urGridView.Columns.Add(gvcMax);

                // Elapsed
                GridViewColumnHeader gvchElapsed = new GridViewColumnHeader();
                gvchElapsed.FontSize = 11;
                gvchElapsed.Width = 70;
                gvchElapsed.Content = " Elapsed";
                gvchElapsed.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcElapsed = new GridViewColumn();
                gvcElapsed.DisplayMemberBinding = new Binding("FolderElapsed");
                gvcElapsed.Header = gvchElapsed;
                urGridView.Columns.Add(gvcElapsed);

                // FolderElapsedReadWriteRatio
                GridViewColumnHeader gvchElapsedReadWriteRatio = new GridViewColumnHeader();
                gvchElapsedReadWriteRatio.FontSize = 11;
                gvchElapsedReadWriteRatio.Width = 104;
                gvchElapsedReadWriteRatio.Content = " Read:Write (%time)";
                gvchElapsedReadWriteRatio.HorizontalContentAlignment = System.Windows.HorizontalAlignment.Left;
                GridViewColumn gvcElapsedReadWriteRatio = new GridViewColumn();
                gvcElapsedReadWriteRatio.DisplayMemberBinding = new Binding("FolderElapsedReadWriteRatio");
                gvcElapsedReadWriteRatio.Header = gvchElapsedReadWriteRatio;
                urGridView.Columns.Add(gvcElapsedReadWriteRatio);
 
                urListView[accountnum].View = urGridView;
                urGrid.Children.Add(urListView[accountnum]);

                // ----------------------------------
                // Bind listview to FolderResultsList
                // ----------------------------------
                // Wrap in NotifyCollectionChangedWrapper so we can update collection from a different thread
                Binding binding = new Binding();
                binding.Source = new NotifyCollectionChangedWrapper<FolderResultsViewModel>(ar.FolderResultsList);
                BindingOperations.SetBinding(urListView[accountnum], ListView.ItemsSourceProperty, binding);


                // =================================================================
                // Row 2 contains Problems label
                // =================================================================
                userErrsMsg = new Label();
                userErrsMsg.Visibility = System.Windows.Visibility.Visible;
                userErrsMsg.SetValue(Grid.RowProperty, 1);
                userErrsMsg.SetValue(Grid.ColumnProperty, 0);
                userErrsMsg.SetValue(Grid.ColumnSpanProperty, 2);
                userErrsMsg.MinWidth = 300;
                userErrsMsg.Margin = new Thickness(5, 0, 0, 0);
                userErrsMsg.HorizontalAlignment = System.Windows.HorizontalAlignment.Left;
                userErrsMsg.VerticalAlignment = System.Windows.VerticalAlignment.Bottom;
                userErrsMsg.Content = "Problems";
                urGrid.Children.Add(userErrsMsg);

                // =================================================================
                // Row 3 contains Problems Listbox
                // =================================================================
                lbErrors[accountnum] = new ListBox();
                lbErrors[accountnum].FontSize = 11;
                lbErrors[accountnum].SetValue(Grid.RowProperty, 2);
                lbErrors[accountnum].Margin = new Thickness(5, 5, 5, 5);
                lbErrors[accountnum].MinHeight = 120;
                lbErrors[accountnum].MaxHeight = 120;
                lbErrors[accountnum].MinWidth = 450;
                lbErrors[accountnum].HorizontalAlignment = System.Windows.HorizontalAlignment.Stretch;
                lbErrors[accountnum].VerticalAlignment = System.Windows.VerticalAlignment.Top;
                urGrid.Children.Add(lbErrors[accountnum]);
                // NB Data binding not currently set up for this, rather it is populated when the tab gains focus - see HandleGotFocus()

                // =================================================================
                // Row 4 contains Progressbar
                // =================================================================
                userProgressBar = new ProgressBar();
                userProgressBar.SetValue(Grid.RowProperty, 3);
                userProgressBar.SetValue(Grid.ColumnProperty, 0);
                userProgressBar.SetValue(Grid.ColumnSpanProperty, 2);
                userProgressBar.IsIndeterminate = false;
                userProgressBar.Orientation = Orientation.Horizontal;
                userProgressBar.Height = 18;
                userProgressBar.Margin = new Thickness(5, 5, 5, 5);
                userProgressBar.HorizontalAlignment = System.Windows.HorizontalAlignment.Stretch;

                // FBS bug 74960 -- 6/1/12
                ToolTip tooltip = new ToolTip();
                Binding tbBinding = new Binding("GlobalAcctProgressMsg");
                tbBinding.Source = ar;
                tooltip.SetBinding(ContentControl.ContentProperty, tbBinding);
                ToolTipService.SetToolTip(userProgressBar, tooltip);

                // Change the background and foreground colors
                SolidColorBrush scbBack = new SolidColorBrush();
                scbBack.Color = Color.FromArgb(255, 218, 227, 235);   // #FFDAE3EB
                userProgressBar.Background = scbBack;
                userProgressBar.Foreground = Brushes.DodgerBlue;

                Binding upbBinding = new Binding("PBValue");
                upbBinding.Source = ar;
                userProgressBar.SetBinding(ProgressBar.ValueProperty, upbBinding);
                if (!ViewModel.GetScheduleViewModel().IsPreviewMode())
                    urGrid.Children.Add(userProgressBar);

                // =================================================================
                // Row 5 contains Account Status message
                // =================================================================
                userStatusMsg = new Label();
                userStatusMsg.Visibility = System.Windows.Visibility.Visible;
                userStatusMsg.SetValue(Grid.RowProperty, 4);
                userStatusMsg.SetValue(Grid.ColumnProperty, 0);
                userStatusMsg.SetValue(Grid.ColumnSpanProperty, 2);
                userStatusMsg.MinWidth = 300;
                userStatusMsg.Margin = new Thickness(5, 0, 0, 0);
                userStatusMsg.HorizontalAlignment = System.Windows.HorizontalAlignment.Left;
                userStatusMsg.FontStyle = FontStyles.Italic;
                Binding usmBinding = new Binding("AccountStatus");
                usmBinding.Source = ar;
                userStatusMsg.SetBinding(Label.ContentProperty, usmBinding);
                urGrid.Children.Add(userStatusMsg);

                userItem.Content = urGrid;

                // ===========================================================================
                // Add the tab
                // ===========================================================================
                tabCtrl.Items.Add(userItem);
                userItem.IsSelected = true;

            }
            catch (Exception excep)
            {
                Log.err("error when get usermigration information " + excep.Message);
            }
        }
    }

    protected void HandleGotFocus(object sender, EventArgs e)
    {
        TabItem ti = sender as TabItem;
        string hdr = ti.Header.ToString();

        ViewModel.SelectedTab = hdr;

        // Need to figure this out
        int accountnum = -1;
        if (hdr != "Accounts")
            accountnum = GetAcctNum(hdr);

        if (accountnum != -1)
        {
            AccountResultsViewModel ar = ViewModel.AccountResultsList[accountnum];
            lbErrors[accountnum].Items.Clear();
            for (int i = 0; i < ar.AccountProblemsList.Count; i++)
            {
                ProblemInfo problemInfo = ar.AccountProblemsList[i];
                if (problemInfo != null)
                {
                    ListBoxItem item = new ListBoxItem();       // hack for now -- will do it right with binding later!
                    item.Content = problemInfo.FormattedMsg;

                    lbErrors[accountnum].Items.Add(item);
                }
            }
        }
    }

    private void CloseTab(object source, RoutedEventArgs args)
    {
        using (LogBlock logblock = Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
        {
            TabItem tabItem = args.OriginalSource as CloseableTabItem;

            if (tabItem != null)
            {
                TabControl tabControl = tabItem.Parent as TabControl;
                if (tabControl != null)
                {
                    int accountnum = GetAcctNum((string)tabItem.Header);

                    tabControl.Items.Remove(tabItem);
                    urListView[accountnum] = null;
                    lbErrors[accountnum] = null;
                    iTabCount--;
                }
            }
        }
    }

    public DependencyObject FindParent(DependencyObject o, Type parentType)
    {
        DependencyObject parent = o;

        while (parent != null)
        {
            if (parent.GetType() == parentType)
                break;
            else
                parent = VisualTreeHelper.GetParent(parent);
        }
        return parent;
    }
}
}
