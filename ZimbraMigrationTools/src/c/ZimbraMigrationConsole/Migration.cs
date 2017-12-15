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
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System;

namespace ZimbraMigrationConsole
{

/*
class Migration
{
    public void MigrationTest(string accountname, object Test, string accountid, MigrationOptions opts, bool ServerMigration)
    {
        MigrationAccount MyAcct = new MigrationAccount();

        MyAcct.AccountName = accountname;
        MyAcct.AccountID = accountid;
        MyAcct.OnChanged += new MigrationObjectEventHandler(i_OnChanged1);

        MigrationFolder MyFolder = new MigrationFolder();
        MyFolder.OnChanged += new MigrationObjectEventHandler(i_OnChanged12);

        MyAcct.CurrFolder = MyFolder;

        //CSMigrationwrapper test = new CSMigrationwrapper();
        // test.StartMigration(MyAcct);

        CSMigrationWrapper test = (CSMigrationWrapper)Test;

        // test.StartMigration(MyAcct,opts);
        test.StartMigration(MyAcct, opts, ServerMigration);
    }

    public void i_OnChanged1(object sender, MigrationObjectEventArgs e)
    {
        MigrationAccount i = (MigrationAccount)sender;
        string Message = " Migration started for user : {0} with TotalContacts  {1} ,TotalMails {2}, TotalRules {3}";

        Console.WriteLine(String.Format(Message, i.AccountName, i.TotalContacts, i.TotalMails, i.TotalRules));
    }

    public void i_OnChanged12(object sender, MigrationObjectEventArgs e)
    {
        MigrationFolder i = (MigrationFolder)sender;
        string Message = "Migrating {0} folder \n " + "Migrating........................... {1} of {2} {0}";

        Console.WriteLine(String.Format(Message, i.FolderName, i.NumFolderItemsMigrated, i.NumFolderItemsToMigrate));
    }
}
*/

}
