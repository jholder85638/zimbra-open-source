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

using MVVM.Model;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Threading;
using System.Windows.Input;
using System.Windows;
using System;
using CssLib;

namespace MVVM.ViewModel
{
public class FolderResultsViewModel: BaseViewModel
{
    readonly FolderResults m_folderResults = new FolderResults();

    public FolderResultsViewModel(string folderName)
    {
        using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("FolderResultsViewModel.FolderResultsViewModel"))
        {
            this.FolderName = folderName;
            this.FolderProgress = "";
            this.FolderElapsedReadWriteRatio = "";
        }
    }

    public string FolderName 
    {
        get { return m_folderResults.FolderName; }
        set
        {
            if (value == m_folderResults.FolderName)
                return;

            m_folderResults.FolderName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("FolderName"));
        }
    }

    public string FolderProgress 
    {
        get { return m_folderResults.FolderProgress; }
        set
        {
            if (value == m_folderResults.FolderProgress)
                return;

            m_folderResults.FolderProgress = value;
            OnPropertyChanged(new PropertyChangedEventArgs("FolderProgress"));
        }
    }

    public int NumFolderItemsMigrated
    {
        get { return m_folderResults.NumFolderItemsMigrated; }
        set
        {
            if (value == m_folderResults.NumFolderItemsMigrated)
                return;

            m_folderResults.NumFolderItemsMigrated = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumFolderItemsMigrated"));
        }
    }

    public int NumFolderSkipsFilter
    {
        get { return m_folderResults.NumFolderSkipsFilter; }
        set
        {
            if (value == m_folderResults.NumFolderSkipsFilter)
                return;

            m_folderResults.NumFolderSkipsFilter = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumFolderSkipsFilter"));
        }
    }

    public int NumFolderSkipsHistory
    {
        get { return m_folderResults.NumFolderSkipsHistory; }
        set
        {
            if (value == m_folderResults.NumFolderSkipsHistory)
                return;

            m_folderResults.NumFolderSkipsHistory = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumFolderSkipsHistory"));
        }
    }

    public int NumFolderErrs 
    {
        get { return m_folderResults.NumFolderErrs; }
        set
        {
            if (value == m_folderResults.NumFolderErrs)
                return;

            m_folderResults.NumFolderErrs = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumFolderErrs"));
        }
    }

    public int NumFolderWarns 
    {
        get { return m_folderResults.NumFolderWarns; }
        set
        {
            if (value == m_folderResults.NumFolderWarns)
                return;

            m_folderResults.NumFolderWarns = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumFolderWarns"));
        }
    }

    public int FolderItemMillisecsMin 
    {
        get { return m_folderResults.FolderItemMillisecsMin; }
        set
        {
            if (value == m_folderResults.FolderItemMillisecsMin)
                return;

            m_folderResults.FolderItemMillisecsMin = value;
            OnPropertyChanged(new PropertyChangedEventArgs("FolderItemMillisecsMin"));
        }
    }

    public int FolderItemMillisecsAvg 
    {
        get { return m_folderResults.FolderItemMillisecsAvg; }
        set
        {
            if (value == m_folderResults.FolderItemMillisecsAvg)
                return;

            m_folderResults.FolderItemMillisecsAvg = value;
            OnPropertyChanged(new PropertyChangedEventArgs("FolderItemMillisecsAvg"));
        }
    }

    public int FolderItemMillisecsMax 
    {
        get { return m_folderResults.FolderItemMillisecsMax; }
        set
        {
            if (value == m_folderResults.FolderItemMillisecsMax)
                return;

            m_folderResults.FolderItemMillisecsMax = value;
            OnPropertyChanged(new PropertyChangedEventArgs("FolderItemMillisecsMax"));
        }
    }

    public string FolderElapsed 
    {
        get { return m_folderResults.FolderElapsed; }
        set
        {
            if (value == m_folderResults.FolderElapsed)
                return;

            m_folderResults.FolderElapsed = value;
            OnPropertyChanged(new PropertyChangedEventArgs("FolderElapsed"));
        }
    }

    public string FolderElapsedReadWriteRatio
    {
        get { return m_folderResults.FolderElapsedReadWriteRatio; }
        set
        {
            if (value == m_folderResults.FolderElapsedReadWriteRatio)
                return;

            m_folderResults.FolderElapsedReadWriteRatio = value;
            OnPropertyChanged(new PropertyChangedEventArgs("FolderElapsedReadWriteRatio"));
        }
    }
}
}
