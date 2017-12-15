/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2012, 2014, 2015, 2016 Synacor, Inc.
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
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


// ==============================================================================================
// Abstract base classes for CSSourceTools and CSSourceAccount
// ==============================================================================================


namespace CssLib
{
    // ==============================================================================================
    // abstract class CSSourceTools
    // ==============================================================================================
    //
    // Abstracts a set of tools on the source mail system
    //
    public abstract class CSSourceTools
    {
       public abstract string InitSourceTools(string ProfileOrServerName, string AdminUser, string AdminPassword);
       public abstract string UninitSourceTools();

       public abstract string GetProfileList(out object var);
       public abstract string SelectExchangeUsers(out object var);
       public abstract bool   AvoidInternalErrors(string strErr);
    }

    // ==============================================================================================
    // abstract class CSSourceAccount
    // ==============================================================================================
    //
    // Abstracts an account on the source mail system to be migrated
    //
    public abstract class CSSourceAccount
    {
       public abstract string InitSourceAccount(bool bIsServerMigration, string sSrcAccount, string sZCSAccount, bool bIsPublicFoldersMigration);
       public abstract void UninitSourceAccount();

       public abstract dynamic[] GetFolders();

       public abstract dynamic[] GetFolderItems(dynamic folderobject, double Date);

       public abstract string[,]  GetRules();
       public abstract string GetOOO();
    }
}
