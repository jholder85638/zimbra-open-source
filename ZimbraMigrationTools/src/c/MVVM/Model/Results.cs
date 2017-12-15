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

namespace MVVM.Model
{
using System;

// ==============================================================================
// class AccountResults
// ==============================================================================
public class AccountResults
{
    internal AccountResults()
    {
        this.AccountName = "";
        this.AccountStatus = "";
        this.AccountProcessed = "";

        this.NumAccountItemsToMigrate = 0;
        this.NumAccountItemsMigrated = 0;

        this.NumAccountSkipsFilter = 0;
        this.NumAccountSkipsHistory = 0;

        this.NumAccountErrs = 0;
        this.NumAccountWarns = 0;

        this.AccountElapsed = "";

        this.PBValue = 0;
        this.EnableStop = false;

    }

    public string   AccountName                  { get;set; }
    public string   AccountStatus                { get;set; }
    public string   AccountProcessed             { get;set; }

    public int      NumAccountItemsToMigrate     { get;set; }
    public int      NumAccountItemsMigrated      { get;set; }

    public int      NumAccountSkipsFilter        { get;set; }
    public int      NumAccountSkipsHistory       { get;set; }

    public int      NumAccountErrs               { get;set; }
    public int      NumAccountWarns              { get;set; }

    public string   AccountElapsed               { get;set; }
    public string   AccountElapsedReadWriteRatio { get;set; }

    public string   GlobalAcctProgressMsg        { get;set; }
    public int      PBValue                      { get;set; }

    public int      CurrentAccountSelection      { get;set; }
    public bool     OpenLogFileEnabled           { get;set; }
    public bool     EnableStop                   { get;set; }
    public string   SelectedTab                  { get;set; }
}

// ==============================================================================
// class FolderResults
// ==============================================================================
public class FolderResults
{
    internal FolderResults()
    {
        this.FolderName = "";
        this.FolderProgress = "";

        this.NumFolderItemsMigrated = 0;

        this.NumFolderSkipsFilter   = 0;
        this.NumFolderSkipsHistory  = 0;

        this.NumFolderErrs          = 0;
        this.NumFolderWarns         = 0;

        this.FolderElapsed          = "";

        this.FolderItemMillisecsMin = 0;
        this.FolderItemMillisecsAvg = 0;
        this.FolderItemMillisecsMax = 0;
    }

    public string   FolderName                  { get;set; }
    public string   FolderProgress              { get;set; }

    public int      NumFolderItemsMigrated      { get;set; }

    public int      NumFolderSkipsFilter        { get;set; }
    public int      NumFolderSkipsHistory       { get;set; }

    public int      NumFolderErrs               { get;set; }
    public int      NumFolderWarns              { get;set; }

    public int      FolderItemMillisecsMin      { get;set; }
    public int      FolderItemMillisecsMax      { get;set; }
    public int      FolderItemMillisecsAvg      { get;set; }

    public string   FolderElapsed               { get;set; }
    public string   FolderElapsedReadWriteRatio { get;set; }
}
}
