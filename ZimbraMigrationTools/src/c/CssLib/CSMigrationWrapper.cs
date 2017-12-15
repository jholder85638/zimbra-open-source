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
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Collections;
using System.Xml;

namespace CssLib
{


    [Flags] public enum ItemsAndFoldersOptions
    {
        OOO = 0x100, Junk = 0x0080, DeletedItems = 0x0040, Sent = 0x0020, Rules = 0x0010,
        Tasks = 0x0008, Calendar = 0x0004, Contacts = 0x0002, Mail = 0x0001, None = 0x0000
    }

    public enum ZimbraFolders
    {
        Min = 0, UserRoot = 1, Inbox = 2, Trash = 3, Junk = 4, Sent = 5, Drafts = 6,
        Contacts = 7, Tags = 8, Conversations = 9, Calendar = 10, MailboxRoot = 11, Wiki = 12,
        EmailedContacts = 13, Chats = 14, Tasks = 15, Max = 16
    }

    public class MigrationOptions
    {
        public ItemsAndFoldersOptions ItemsAndFolders;

        public string   MigrateOnOrAfterDate;
        public string   DateFilterItem;

        public string   MessageSizeFilter;
        public string   SkipFolders;
        public bool     SkipPrevMigrated;
        public Int32    MaxErrorCnt;
        public string   SpecialCharRep;
        public bool     IsMaintainenceMode;
        public Int32    MaxRetries;
        public bool     IsPublicFolders;
        public bool     IsZDesktop;
    }

    // =======================================================================================================================
    // class CSMigrationWrapper
    // =======================================================================================================================
    //
    // Singleton - maintains ptr to source mailsystem in "MailSource"
    //
    public class CSMigrationWrapper
    {
        // ---------------------------------------------------------
        // Ptr to source mail system tools (to get profile list etc)
        // ---------------------------------------------------------
        dynamic csSourceTools;

        // ---------------------------------------------------------
        // Ptr to source account data
        // ---------------------------------------------------------
        // This is only here to address BUG 71047. See dynamic stack variable 
        // csSourceAccount in StartMigration for the csSourceAccount that is used
        // when migrating several users (each on a different thread)
        dynamic m_csSourceAccount = null;
        public dynamic csSourceAccount
        {
            get { return m_csSourceAccount; }
            set { m_csSourceAccount = value; }
        }

        // ---------------------------------------------------------
        // MigrationType: Currently always set to "MAPI"
        // ---------------------------------------------------------
        string m_MigrationType;
        public string MigrationType
        {
            get { return m_MigrationType; }
            set { m_MigrationType = value; }
        }

        // TESTPOINT_MIGRATION_INSTANTIATE_MIGRATION_WRAPPER
        public CSMigrationWrapper(string migrationType)
        //
        // Singleton CSMigrationWrapper instantiated when use clicks Next on intro page
        //
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("CSMigrationWrapper.CSMigrationWrapper"))
            {
                MigrationType = migrationType;

                if (MigrationType == "MAPI")
                {
                    Log.info("Initializing MapiTools");
                    csSourceTools = new CSMapiTools();
                }
            }
        }

        public string InitCSMigrationWrapper(string ProfileOrServerName, string AdminUser, string AdminPassword)
        {
            using (LogBlock logblock = Log.NotTracing()?null: Log.NotTracing() ? null : new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                string s = "";
                try
                {
                    Log.mem(">InitSourceTools");
                    s = csSourceTools.InitSourceTools(ProfileOrServerName, AdminUser, AdminPassword);
                    Log.mem("<InitSourceTools");
                }
                catch (Exception e)
                {
                    s = string.Format("Initialization Exception. Make sure to enter the proper credentials: {0}", e.Message);
                }
                return s;
            }
        }

        public string UninitCSMigrationWrapper()
        // For ZMT, called when user exits the app
        // For ZMC, called at end of migration from UninitCSMigrationWrapper()
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                try
                {
                    return csSourceTools.UninitSourceTools();
                }
                catch (Exception e)
                {
                    string msg = string.Format("UninitCSMigrationWrapper Exception: {0}", e.Message);
                    return msg;
                }
            }
        }

        public bool AvoidInternalErrors(string strErr)
        {
            return csSourceTools.AvoidInternalErrors(strErr);
        }

        public string[] GetListofMapiProfiles()
        {
            object var = new object();
            string msg = "";
            string[] s = { "" };

            try
            {
                msg = csSourceTools.GetProfileList(out var);
                s = (string[])var;
            }
            catch (Exception e)
            {
                // FBS bug 73020 -- 4/18/12
                string tmp = msg;

                msg = string.Format("GetListofMapiProfiles Exception: {0}", e.Message);
                if (tmp.Length > 0)
                {
                    if (tmp.Substring(0, 11) == "No profiles")
                    {
                        msg = "No profiles";
                    }
                }
                s[0] = msg;
            }
            return s;
        }

        public string[] GetListFromObjectPicker()
        {
            // Change this to above signature when I start getting the real ObjectPicker object back
            string[] s = { "" };
            string status = "";
            object var = new object();

            try
            {
                status = csSourceTools.SelectExchangeUsers(out var);
                s = (string[])var;
            }
            catch (Exception e)
            {
                status = string.Format("GetListFromObjectPicker Exception: {0}", e.Message);
                s[0] = status;
            }
            return s;
        }

        private bool SkipFolder(MigrationOptions options, List<string> skipList, dynamic folder)
        {
            //using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                // Note that Rules and OOO do not apply here

                if (folder.ZimbraSpecialFolderId == (int)ZimbraFolders.Junk &&
                    (options.ItemsAndFolders.ToString().Contains("Junk")))
                {
                    return false;
                }


                if ((folder.ZimbraSpecialFolderId == (int)ZimbraFolders.Calendar && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Calendar))      ||
                    (folder.ZimbraSpecialFolderId == (int)ZimbraFolders.Contacts && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Contacts))      ||

                    (folder.ZimbraSpecialFolderId == (int)ZimbraFolders.Junk     && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Junk))          ||
                    (folder.ZimbraSpecialFolderId == (int)ZimbraFolders.Junk     && !options.ItemsAndFolders.ToString().Contains("Junk"))                   ||

                    (folder.ZimbraSpecialFolderId == (int)ZimbraFolders.Sent     && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Sent))          ||
                    (folder.ZimbraSpecialFolderId == (int)ZimbraFolders.Tasks    && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Tasks))         ||
                    (folder.ZimbraSpecialFolderId == (int)ZimbraFolders.Trash    && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.DeletedItems))  ||
                    // FBS NOTE THAT THESE ARE EXCHANGE SPECIFIC and need to be removed
                    (folder.ContainerClass == "IPF.Contact"                      && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Contacts))      ||
                    (folder.ContainerClass == "IPF.Appointment"                  && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Calendar))      ||
                    (folder.ContainerClass == "IPF.Task"                         && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Tasks))         ||
                    (folder.ContainerClass == "IPF.Note"                         && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Mail))          ||
                    (folder.ContainerClass == "" /*no class->assume IPF.Note*/   && !options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Mail)))
                {
                    return true;
                }

                for (int i = 0; i < skipList.Count; i++)
                {
                    if (string.Compare(folder.Name, skipList[i], StringComparison.OrdinalIgnoreCase) == 0)
                    {
                        return true;
                    }
                }
                return false;
            }
        }

        // TODO - this is Exchange specific - should use folder ids
        private string GetFolderViewType(string containerClass)
        {
            string retval = "";                     // if it's a "message", blanks are cool

            if (containerClass == "IPF.Contact")
                retval = "contact";
            else
            if (containerClass == "IPF.Appointment")
                retval = "appointment";
            else
            if (containerClass == "IPF.Task")
                retval = "task";

            return retval;
        }

        
        private void TagNamesToTagIDs(Dictionary<string, string> dict, MigrationAccount acct, ZimbraAPI api)
        //
        // sTagNames is comma-sep list of tag names (returned by C++ layer)
        //
        // if the tag has already been created, just return it; if not, do the req and create it
        //
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                string sTagNames = dict["tags"];
                if (sTagNames.Length == 0)
                  return;

                string sTagIDs = "";
                string[] tokens = sTagNames.Split(',');
                for (int i = 0; i < tokens.Length; i++)
                {
                    if (i > 0)
                        sTagIDs += ",";

                    string sTagName = tokens.GetValue(i).ToString();

                    // See if key already exists. Tag names in acct.TagDict are mixed case, but we need case-insensitive
                    // because on ZCS, tag "GREEN" == "green"
                    bool bAlreadyExists = false;
                    string sTagID = "";
                    foreach (var keypair in acct.TagDict)
                    {
                        if (keypair.Key.ToString().ToUpper() == sTagName.ToUpper())
                        {
                            bAlreadyExists = true;
                            sTagID = keypair.Value;
                            break;
                        }
                    }


                    if (bAlreadyExists)
                        sTagIDs += sTagID;
                    else
                    {
                        // Color starts at 0, will keep bumping up by 1 (we don't yet support migrating correct color)
                        string sColour = (acct.TagDict.Count).ToString(); 

                        // Create tag on server - returns TagID
                        sTagID = "";
                        int stat = api.CreateTag(sTagName, sColour, out sTagID);
                        Log.info("Created tag '", sTagName, "' (", sTagID, ")");

                        // add to existing dict; token is Key, tokenNumstr is Val
                        acct.TagDict.Add(sTagName, sTagID);   
                        sTagIDs += sTagID;
                    }
                }

                bool bRet = dict.Remove("tags");
                dict.Add("tags", sTagIDs);
            }
        }

        enum ItemType {Mail = 1, Contacts = 2, Calendar = 3, Task = 4, MeetingReq = 5};
        private bool ShouldProcessType(MigrationOptions options, ItemType type)
        {
            //using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                bool retval = false;

                switch (type)
                {
                    case ItemType.Mail:
                    case ItemType.MeetingReq:
                        retval = options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Mail);
                        break;

                    case ItemType.Calendar:
                        retval = options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Calendar);
                        break;

                    case ItemType.Contacts:
                        retval = options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Contacts);
                        break;

                    case ItemType.Task:
                        retval = options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Tasks);
                        break;

                    default:
                        break;
                }
                return retval;
            }
        }

        private void VarArrayToCsDict(string[,] data, Dictionary<string,string> dict)
        {
            // C++ data is packaged up as a variant array for passing back to C# layer by COMMapiAccount::CppDictToVarArray() 
            // This method takes that array and converts to C# dictionary
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                int bound = data.GetUpperBound(1);
                if (bound > 0)
                {
                    for (int i = 0; i <= bound; i++)
                    {
                        string Key = data[0, i];
                        string Value = data[1, i];

                        try
                        {
                            dict.Add(Key, Value);
                            // Console.WriteLine("{0}, {1}", so1, so2);
                        }
                        catch (Exception e)
                        {
                            string s = string.Format("Exception adding {0}/{1}: {2}", Key, Value, e.Message);
                            Log.warn(s);
                            // Console.WriteLine("{0}, {1}", so1, so2);
                        }
                    } // for
                }
            }
        }

        private void ReclaimMemory()
        {
            Log.mem(">Collect");
            GC.Collect();
            GC.WaitForPendingFinalizers();
            Log.mem("<Collect");
            //System.Threading.Thread.Sleep(5000);
        }

        private void PushFolderItems(MigrationAccount Acct, dynamic csSourceAccount, dynamic folder, ZimbraAPI api, string sPathOfFolderBeingMigrated, MigrationOptions options)
        {
            // TESTPOINT_MIGRATION_PUSH_FOLDER_ITEMS Push items for single folder to ZCS
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType()+"."+System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                int trial = 0;

                do
                {
                    Acct.IsCompletedMigration = true;
                    trial++;

                    Log.summary(" ");
                    Log.summary("===================================================================================================");
                    Log.summary("Processing folder '" + folder.folderPath + "'");
                    Log.summary("===================================================================================================");

                    // ================================================================================
                    // GetFolderItems - Get the folder's contained items
                    // ================================================================================
                    Int32 nTicksGetFolderItemsStart = Environment.TickCount;

                    Log.mem("PushFolderItems > GetFolderItems");
                    Log.summary("Getting items list...");
                    dynamic[] itemobjectarray = null;
                    try
                    {
                        Acct.AccountStatus = "Reading '" + folder.Name +"'...";
                        Acct.CurrFolder.FolderStatus = "Reading folder...";

                        DateTime dtUtcNow = DateTime.UtcNow;
                        itemobjectarray = csSourceAccount.GetFolderItems(folder, dtUtcNow.ToOADate());

                        Acct.CurrFolder.FolderStatus = "Migrating...";
                        Acct.CurrFolder.NumFolderItemsToMigrate = itemobjectarray.GetLength(0);
                        Acct.CurrFolder.NumFolderItemsMigrated = 0;
                    }
                    catch (Exception e)
                    {
                        Log.err("exception in PushFolderItems->csSourceAccount.GetItemsFolder", e.Message);
                    }
                    Log.mem("PushFolderItems < GetFolderItems");
                    Int32 nFolderReadElapsed = Environment.TickCount-nTicksGetFolderItemsStart;
                    Acct.AccountReadMillisecs           += nFolderReadElapsed;
                    Acct.CurrFolder.FolderReadMillisecs += nFolderReadElapsed;


                    // ================================================================================
                    // See if a max Message Size has been specified
                    // ================================================================================
                    int nMaxMessageSizeBytes = 0;
                    if (options.MessageSizeFilter != null && options.MessageSizeFilter.Length > 0)
                    {
                        try
                        {
                            nMaxMessageSizeBytes = Int32.Parse(options.MessageSizeFilter)*1024*1024;
                        }
                        catch (Exception)
                        {
                            Log.err("Failed to parse MessageSizeFilter '" + options.MessageSizeFilter+"'");
                        }
                    }


                    // ================================================================================
                    // Process the contained items
                    // ================================================================================
                    if (itemobjectarray != null)
                    {
                        int iProcessedItems = 0;
                        string historyfile = Path.GetTempPath() + Acct.AccountName.Substring(0, Acct.AccountName.IndexOf('@')) + "history.log";
                        string historyid = "";


                        // Make sure there are some items to process in this folder
                        if (itemobjectarray.GetLength(0) > 0)
                        {
                            // DCB_BUG_97312
                            //
                            // "NumFolderItemsToMigrate" is set from the original GetFolderList call and "itemobjectarray.Count()" is obtained by
                            // requesting this folder's items. They should be the same.
                            //
                            // However, it appears from comment on changelist 425346 that they differ in some unknown case for Realogy.
                            // When they differ:
                            // - we loop to an upper bound of itemobjectarray.Count() rather than Acct.CurrFolder.NumFolderItemsToMigrate (probably we should always do this!)
                            // - We add even failed items to the history list.
                            //
                            // I'm not sure why it was done this way - seems questionable to me - but I am simply retaining the behavior when
                            // we had 2 while loops.

                            bool bWriteHistoryEvenIfError = false;
                            long nUpperWhileBound = Acct.CurrFolder.NumFolderItemsToMigrate;

                            if (itemobjectarray.Count() != Acct.CurrFolder.NumFolderItemsToMigrate)
                            {
                                string errMesg = "MAPI not be returning all the items for the folder?";
                                Log.err("CSmigrationwrapper MAPI edgecase", errMesg);

                                // Override nUpperWhileBound
                                nUpperWhileBound = itemobjectarray.Count();

                                bWriteHistoryEvenIfError = true;

                                Log.warn("Itemobjectarray.Count is not equal to Acct.CurrFolder.NumFolderItemsToMigrate ", itemobjectarray.Count(), Acct.CurrFolder.NumFolderItemsToMigrate);
                                Acct.NumAccountErrs++;

                                bool bError = false;
                                Acct.AddProblem(Acct.AccountName, errMesg, ProblemInfo.TYPE_ERR, ref bError);

                                Acct.IsCompletedMigration = false;
                                options.SkipPrevMigrated = true;
                            }


                            // ============================================
                            // See if we should filter on MigrateOnOrAfter
                            // ============================================
                            bool bEnableDateFilter = false;
                            DateTime dtMigrateOnOrAfter = new DateTime(1600, 1, 1, 0, 0, 0); // arbitrary ancient date
                            if (options.MigrateOnOrAfterDate != null)
                            {
                                try
                                {
                                    dtMigrateOnOrAfter = Convert.ToDateTime(options.MigrateOnOrAfterDate);
                                    bEnableDateFilter = true;
                                }
                                catch (Exception)
                                {
                                    Log.err("Failed to parse MigrateOnOrAfterDate '" + options.MigrateOnOrAfterDate+"'");
                                }
                            }





                            // ==================================================================================================
                            // ==================================================================================================
                            // Loop until done all items
                            // ==================================================================================================
                            // ==================================================================================================
                            Log.summary("Pushing folder items...");
                            Acct.AccountStatus = "Migrating '" + folder.Name +"'...";
                            Int32 nTicksPushFolderItemsStart = Environment.TickCount;
                            while (iProcessedItems < nUpperWhileBound)
                            {
                                foreach (dynamic itemobject in itemobjectarray)
                                {
                                    /*
                                    // Test results UI
                                    Acct.NumAccountSkipsFilter++;
                                    Acct.NumAccountSkipsHistory++;

                                    Acct.CurrFolder.NumFolderSkipsFilter++;
                                    Acct.CurrFolder.NumFolderSkipsHistory++;
                                    
                                    Acct.CurrFolder.NumFolderErrs++;
                                    Acct.CurrFolder.NumFolderWarns++;
                                    */

                                    // Exit if max permissable error count reached
                                    if (options.MaxErrorCnt > 0)
                                    {
                                        if (Acct.NumAccountErrs > options.MaxErrorCnt)
                                        {
                                            Log.err("Cancelling migration -- error threshold reached");
                                            return;
                                        }
                                    }

                                    Log.trace(" ");
                                    Log.trace(" ");
                                    Log.trace("----------------------------------------------------------------------------------------------------------------------------------------------------");
                                    Log.summary("Processing item " + (iProcessedItems + 1) + " of " +  nUpperWhileBound + " '" + itemobject.Subject +"'");
                                    Log.trace("----------------------------------------------------------------------------------------------------------------------------------------------------");
                                    bool bItemMigrated = false;
                                    bool bError = false;
                                    Int32 nTicksProcessItemStart = Environment.TickCount;


                                    // ==================================================================================================
                                    // Decide if we should push this item
                                    // ==================================================================================================
                                    // - Type filter
                                    // - History filter
                                    // - Size filer
                                    // - Date filter

                                    // -------------------------------------------------------
                                    // Type Filter
                                    // -------------------------------------------------------
                                    string hexItemID = "";
                                    ItemType nItemType = (ItemType)itemobject.Type;
                                    bool bShouldProcessType = ShouldProcessType(options, nItemType);
                                    if (bShouldProcessType)
                                    {
                                        if (options.IsMaintainenceMode)
                                        {
                                            Log.err("Cancelling migration -- Mailbox is in maintenance mode. Try back later");
                                            return;
                                        }

                                        bool bSkipMessage = false;
                                        bool bSkipMessageHistory = false;

                                        // --------------------------------------------------------
                                        // History filter
                                        // --------------------------------------------------------
                                        string sItemType = nItemType.ToString();
                                        try
                                        {
                                            hexItemID = BitConverter.ToString(itemobject.ItemID);
                                            hexItemID = hexItemID.Replace("-", "");
                                            historyid = sItemType + hexItemID;
                                        }
                                        catch (Exception e)
                                        {
                                            Log.err("exception in Bitconverter converting itemid to a hexstring", e.Message);
                                        }

                                        // Already in history (already pushed in previous session?)
                                        if (options.SkipPrevMigrated)
                                        {
                                            if (historyid != "")
                                            {
                                                Log.trace("Checking if already migrated");
                                                if (CheckifAlreadyMigrated(historyfile, historyid))
                                                {
                                                    bSkipMessageHistory = true;
                                                    iProcessedItems++;
                                                    Acct.NumAccountSkipsHistory++;
                                                    Acct.CurrFolder.NumFolderSkipsHistory++;
                                                    continue;
                                                }
                                            }
                                        }

                                        // --------------------------------------------------------
                                        //  Check options.DateFilterItem
                                        // --------------------------------------------------------
                                        // Looks like this contains a list of items for which the
                                        // MigrateOnOrAfterDate should be ignored - but there is no UI for it!
                                        bool bSuppressDateFilterForItemsOfThisType = false;
                                        if (options.DateFilterItem != null && options.DateFilterItem.Contains(sItemType))
                                            bSuppressDateFilterForItemsOfThisType = true;


                                        // --------------------------------------------------------
                                        // If C++ layer returned FilterDate, apply filter here
                                        // --------------------------------------------------------
                                        // NB For recurrent appts and tasks, C++ layer will NOT return FilterDate, since
                                        // for recurrent, we need to filter on last instance - and that is only possible by
                                        // opening the entire item
                                        bool bDateFilterAlreadyTested = false;
                                        string sFilterDate = itemobject.FilterDate;
                                        if (bEnableDateFilter && !bSuppressDateFilterForItemsOfThisType && sFilterDate!="")
                                        {
                                            try
                                            {
                                                DateTime dtFilterDate = DateTime.Parse(sFilterDate);
                                                if ((DateTime.Compare(dtFilterDate, dtMigrateOnOrAfter) < 0))
                                                {
                                                    bSkipMessage = true;
                                                    Log.summary("  Skipping because date (" + dtFilterDate + ") date precedes date filter (" + dtMigrateOnOrAfter + ")");
                                                }
                                                bDateFilterAlreadyTested = true;
                                            }
                                            catch (Exception)
                                            {
                                                Log.info(itemobject.Subject, ": unable to parse filter date");
                                                bError = true;
                                            }
                                        }

                                        // --------------------------------------------------------
                                        //  Check message size
                                        // --------------------------------------------------------
                                        Int64 iMessageSizeBytes = itemobject.MessageSize;
                                        if (nMaxMessageSizeBytes>0 && iMessageSizeBytes > nMaxMessageSizeBytes)
                                        {
                                            bSkipMessage = true;
                                            Log.summary("  Skipping because size ("+ iMessageSizeBytes + " bytes) exceeds size filter ("+ options.MessageSizeFilter+" MB)");
                                        }




                                        if (!bSkipMessage)
                                        {
                                            // ===========================================================================================================================
                                            // DCB_BUG_100750 Mem leak test: Repeatedly pull large item from C++ layer. Test this with a PST containing a 50Mb message, 
                                            // e.g. with 5 x 10MB attachments, and watch mem trace using process explorer.
                                            // Result: provided garbage is collected, it runs find for hours. Without, it often runs out of mem.
                                            /*
                                                {
                                                    Log.mem("BASE");
                                                    for (int i = 1; i < 1000000; i++)
                                                    {
                                                        {
                                                            Log.mem(">GetDataForItemID " + i);
                                                            string[,] dataTest = itemobject.GetDataForItemID(csSourceAccount.GetInternalUser(), itemobject.ItemID, itemobject.Type);
                                                            Log.mem("<GetDataForItemID " + i);

                                                    
                                                            Dictionary<string, string> dictTest = new Dictionary<string, string>();

                                                            VarArrayToCsDict(dataTest, dictTest);
                                                            string s = dataTest[0, 1];
                                                            Log.summary(s);
                                                    
                                                        }
                                                        //GCSettings.LargeObjectHeapCompactionMode = GCLargeObjectHeapCompactionMode.CompactOnce; // Not available :-(
                                                        GC.Collect();
                                                        Log.mem("Item " + i + " AFTER COLLECT");
                                                    }
                                                }
                                            */
                                            // ===========================================================================================================================

                                        
                                            bool bLastItemWasLarge = false;
                                            int nItemsSinceLastFreeMem = 0;
                                            {
                                                // -----------------------------------------------------------------------------
                                                // GetDataForItemID - Grab its full data set into "data" (a 2 dim variant array)
                                                // -----------------------------------------------------------------------------
                                                string[,] data = null;
                                                bLastItemWasLarge = false;

                                                Int32 nTicksStartGetItemData = Environment.TickCount;
                                                try
                                                {
                                                    Log.trace(" ");
                                                    Log.trace("--------------------");
                                                    Log.trace("Getting item data...");
                                                    Log.trace("--------------------");

                                                    Log.mem("PushFolderItems > GetDataForItemID");
                                                    data = itemobject.GetDataForItemID(csSourceAccount.GetInternalUser(), itemobject.ItemID, itemobject.Type); // data is freed by garbage collector
                                                    Log.mem("PushFolderItems < GetDataForItemID");
                                                }
                                                catch (Exception e)
                                                {
                                                    Log.err("exception in PushFolderItems->itemobject.GetDataForItemID", e.Message);
                                                    iProcessedItems++;
                                                    continue;
                                                }
                                                if (data == null)
                                                {
                                                    Acct.AddProblem(hexItemID, "NULL data - skipping item", ProblemInfo.TYPE_ERR, ref bError);
                                                    iProcessedItems++;
                                                    continue;
                                                }

                                                Int32 nItemReadElapsed = Environment.TickCount-nTicksStartGetItemData;
                                                Acct.AccountReadMillisecs           += nItemReadElapsed;
                                                Acct.CurrFolder.FolderReadMillisecs += nItemReadElapsed;

                                                Int32 nTicksStartWrite = Environment.TickCount;

                                                nItemsSinceLastFreeMem++;

                                                // ---------------------------------------------------------------------------
                                                // Convert "data" from string[,] to dict
                                                // ---------------------------------------------------------------------------
                                                Log.trace(" ");
                                                Log.trace("--------------------");
                                                Log.trace("Populating dict");
                                                Log.trace("--------------------");

                                                // -----------------------------------------
                                                // Create a dict to hold the item's data
                                                // -----------------------------------------
                                                Dictionary<string, string> dict = new Dictionary<string, string>();

                                                //Not significant Log.mem("PushFolderItems > VarArrayToCsDict");
                                                VarArrayToCsDict(data, dict);
                                                //Not significant Log.mem("PushFolderItems < VarArrayToCsDict");

                                                // "dict" now contains this item's data ready for pushing

                                                // ---------------------------------------------------------------------------
                                                // Log the dictionary
                                                // ---------------------------------------------------------------------------
                                                // if (IsLoggingAtVerbose)
                                                var list = dict.Keys.ToList();
                                                list.Sort();
                                                foreach (var key in list)
                                                {
                                                    if (key != "wstrmimeBuffer")
                                                        Log.verbose(key.PadRight(40), dict[key]);
                                                }

                                                // ---------------------------------------------------------------------------
                                                // Grab a string that can be used to identify the item in logs
                                                // ---------------------------------------------------------------------------
                                                string sItemID;
                                                if (dict.ContainsKey("Subject"))
                                                    sItemID = dict["Subject"];
                                                else
                                                if (dict.ContainsKey("su"))
                                                    sItemID = dict["su"];
                                                else
                                                    sItemID = "EntryID:"+hexItemID;

                                        
                                                // ---------------------------------------------------------------------------
                                                // Check mime size
                                                // ---------------------------------------------------------------------------
                                                int nMimeSize = 0;
                                                if (dict.ContainsKey("MimeSize"))
                                                {
                                                    nMimeSize = Int32.Parse(dict["MimeSize"]);
                                                    if (nMimeSize > 5*1024*1024 /*5Mb*/)
                                                    {
                                                        Log.trace("Large mime (" + dict["MimeSize"] + " bytes)");
                                                        bLastItemWasLarge = true;
                                                    }

                                                    // Make sure we've got some mime
                                                    if (nMimeSize == 0)
                                                    {
                                                        Acct.AddProblem(sItemID, "Mime conversion failure. Possibly insufficient memory to process large message/attachment. Please retry with 64bit migration tool.", ProblemInfo.TYPE_ERR, ref bError);
                                                        iProcessedItems++;
                                                        continue;
                                                    }
                                                }




                                                api.AccountID = Acct.AccountID;
                                                api.AccountName = Acct.AccountName;
                                                if (dict.Count > 0)
                                                {
                                                    // ===========================================================================
                                                    // ItemType-dependent code
                                                    // ===========================================================================
                                                    if ((nItemType == ItemType.Mail) || (nItemType == ItemType.MeetingReq))
                                                    {
                                                        // ***************************************************************
                                                        // Mail or MeetingReq
                                                        // ***************************************************************
                                                        Log.trace(" ");
                                                        Log.trace("----------------------------");
                                                        Log.trace("Creating destination msg...");
                                                        Log.trace("----------------------------");

                                                        // --------------------------------------
                                                        // Change the tag names into tag numbers
                                                        // --------------------------------------
                                                        TagNamesToTagIDs(dict, Acct, api);

                                                        // --------------------------------------
                                                        // Sort out folderid - TODO Move into AddMessage
                                                        // --------------------------------------
                                                        string sFolderId = api.FolderPathToFolderId(sPathOfFolderBeingMigrated);
                                                        if (sFolderId=="")
                                                            Log.warn("folderId is empty, using root (0)");
                                                        dict.Add("folderId", sFolderId);

                                                        // --------------------------------
                                                        // Create new msg on server
                                                        // --------------------------------
                                                        try
                                                        {
                                                            int stat = api.AddMessage(dict); // doesnt pass in "sPathOfFolderBeingMigrated" like we do for other types
                                                            if (stat != 0)
                                                            {
                                                                string errMsg = (api.LastError.IndexOf("upload ID: null") != -1)    // FBS bug 75159 -- 6/7/12
                                                                                ? "Unable to upload file. Please check server message size limits."
                                                                                : api.LastError;
                                                                if (errMsg.Contains("maintenance") || errMsg.Contains("not active"))
                                                                {
                                                                    errMsg = errMsg + " Try Back later";
                                                                    options.IsMaintainenceMode = true;
                                                                }

                                                                Acct.AddProblem(sItemID, errMsg, ProblemInfo.TYPE_ERR, ref bError);
                                                            }
                                                            else
                                                                bItemMigrated = true;
                                                        }
                                                        catch (Exception e)
                                                        {
                                                            Acct.AddProblem(sItemID, "Exception caught in PushFolderItems->api.AddMessage" + e.Message, ProblemInfo.TYPE_ERR, ref bError);
                                                        }
                                                    }
                                                    else
                                                    if (nItemType == ItemType.Contacts)
                                                    {
                                                        // ***************************************************************
                                                        // Contact
                                                        // ***************************************************************
                                                        Log.trace(" ");
                                                        Log.trace("-------------------------------");
                                                        Log.trace("Creating destination contact...");
                                                        Log.trace("-------------------------------");

                                                        // --------------------------------------
                                                        // Change the tag names into tag numbers
                                                        // --------------------------------------
                                                        TagNamesToTagIDs(dict, Acct, api);

                                                        // --------------------------------
                                                        // Create new contact on server
                                                        // --------------------------------
                                                        try
                                                        {
                                                            int stat = api.CreateContact(dict, sPathOfFolderBeingMigrated);
                                                            if (stat != 0)
                                                            {
                                                                string errMsg = api.LastError;
                                                                if (errMsg.Contains("maintenance") || errMsg.Contains("not active"))
                                                                {
                                                                    errMsg = errMsg + " Try Back later";
                                                                    options.IsMaintainenceMode = true;
                                                                }

                                                                Acct.AddProblem(sItemID, "Error in CreateContact " + errMsg, ProblemInfo.TYPE_ERR, ref bError);
                                                            }
                                                            else
                                                                bItemMigrated = true;
                                                        }
                                                        catch (Exception e)
                                                        {
                                                            Acct.AddProblem(sItemID, "Error in CreateContact " + e.Message, ProblemInfo.TYPE_ERR, ref bError);
                                                        }
                                                    }
                                                    else
                                                    if (nItemType == ItemType.Calendar)
                                                    {
                                                        // ***************************************************************
                                                        // Appt
                                                        // ***************************************************************
                                                        
                                                        // --------------------------------
                                                        // Check if skip due to date filter
                                                        // --------------------------------
                                                        if (!bDateFilterAlreadyTested && bEnableDateFilter && !bSuppressDateFilterForItemsOfThisType)
                                                        {
                                                            try
                                                            {
                                                                DateTime dtItem = DateTime.Parse(dict["sFilterDate"]);
                                                                if ((DateTime.Compare(dtItem, dtMigrateOnOrAfter) < 0))
                                                                {
                                                                    bSkipMessage = true;
                                                                    Log.summary("  Skipping because appt (" + dtItem + ") date precedes date filter (" + dtMigrateOnOrAfter + ")");
                                                                }
                                                            }
                                                            catch (Exception e )
                                                            {
                                                                Log.info(sItemID, ": unable to parse date " + e.Message);
                                                                bError = true;
                                                            }
                                                        }

                                                        if (!bSkipMessage)
                                                        {
                                                            Log.trace(" ");
                                                            Log.trace("-------------------------------");
                                                            Log.trace("Creating destination appt...");
                                                            Log.trace("-------------------------------");

                                                            try
                                                            {
                                                                // --------------------------------------
                                                                // Change the tag names into tag numbers
                                                                // --------------------------------------
                                                                TagNamesToTagIDs(dict, Acct, api);

                                                                dict.Add("accountNum", Acct.AccountNum.ToString());

                                                                // --------------------------------
                                                                // Create new appt on server
                                                                // --------------------------------
                                                                int stat = api.AddAppointment(dict, sPathOfFolderBeingMigrated);
                                                                if (stat != 0)
                                                                {
                                                                    string errMsg = api.LastError;
                                                                    if (errMsg.Contains("maintenance") || errMsg.Contains("not active"))
                                                                    {
                                                                        errMsg = errMsg + " Try Back later";
                                                                        options.IsMaintainenceMode = true;
                                                                    }
                                                                    Acct.AddProblem(sItemID, errMsg, ProblemInfo.TYPE_ERR, ref bError);
                                                                }
                                                                else
                                                                    bItemMigrated = true;
                                                            }
                                                            catch (Exception e)
                                                            {
                                                                Acct.AddProblem(sItemID, e.Message, ProblemInfo.TYPE_ERR, ref bError);
                                                            }
                                                        }
                                                    }
                                                    else
                                                    if (nItemType == ItemType.Task)
                                                    {
                                                        // ***************************************************************
                                                        // Task
                                                        // ***************************************************************

                                                        // --------------------------------
                                                        // Check if skip due to date filter
                                                        // --------------------------------
                                                        if (!bDateFilterAlreadyTested && bEnableDateFilter && !bSuppressDateFilterForItemsOfThisType)
                                                        {
                                                            try
                                                            {
                                                                DateTime dtItemFilter = DateTime.Parse(dict["sFilterDate"]);
                                                                if ((DateTime.Compare(dtItemFilter, dtMigrateOnOrAfter) < 0))
                                                                {
                                                                    bSkipMessage = true;
                                                                    Log.summary("  Skipping because task due date older than date filter value");
                                                                }
                                                            }
                                                            catch (Exception e )
                                                            {
                                                                Log.info(sItemID, ": unable to parse date " + e.Message);
                                                                bError = true;
                                                            }
                                                        }

                                                        if (!bSkipMessage)
                                                        {
                                                            Log.trace(" ");
                                                            Log.trace("-------------------------------");
                                                            Log.trace("Creating destination task...");
                                                            Log.trace("-------------------------------");

                                                            // --------------------------------------
                                                            // Change the tag names into tag numbers
                                                            // --------------------------------------
                                                            TagNamesToTagIDs(dict, Acct, api);

                                                            dict.Add("accountNum", Acct.AccountNum.ToString());  // Bug 101136
 
                                                            // --------------------------------
                                                            // Create new task on server
                                                            // --------------------------------
                                                            try
                                                            {
                                                                int stat = api.AddTask(dict, sPathOfFolderBeingMigrated);
                                                                if (stat != 0)
                                                                {
                                                                    string errMsg = api.LastError;
                                                                    if (errMsg.Contains("maintenance") || errMsg.Contains("not active"))
                                                                    {
                                                                        errMsg = errMsg + " Try Back later";
                                                                        options.IsMaintainenceMode = true;
                                                                    }

                                                                    Acct.AddProblem(sItemID, "Error in AddTask" + errMsg, ProblemInfo.TYPE_ERR, ref bError);
                                                                }
                                                                else
                                                                    bItemMigrated = true;
                                                            }
                                                            catch (Exception e)
                                                            {
                                                                Acct.AddProblem(sItemID, "Error in AddTask" + e.Message, ProblemInfo.TYPE_ERR, ref bError);
                                                            }
                                                        }
                                                    }
                                                }
                                                else
                                                    Acct.AddProblem(Acct.AccountName, "Error on message", ProblemInfo.TYPE_ERR, ref bError);

                                                // ------------------------------------------------------------------
                                                // Update  NumAccountItemsMigrated/NumFolderItemsMigrated
                                                // ------------------------------------------------------------------
                                                if (bItemMigrated)
                                                {
                                                    Acct.NumAccountItemsMigrated++;
                                                    Acct.CurrFolder.NumFolderItemsMigrated++;

                                                    // ------------------------------------------------------------------
                                                    // Update timing stats
                                                    // ------------------------------------------------------------------
                                                    {
                                                        Int32 nTicksNow = Environment.TickCount;
                                                        Int32 nTicksProcessItemElapsed = nTicksNow-nTicksProcessItemStart;

                                                        // Min
                                                        if ((nTicksProcessItemElapsed < Acct.CurrFolder.FolderItemMillisecsMin) || (Acct.CurrFolder.FolderItemMillisecsMin == 0))
                                                            Acct.CurrFolder.FolderItemMillisecsMin = nTicksProcessItemElapsed;
                                                
                                                        // Max
                                                        if (nTicksProcessItemElapsed > Acct.CurrFolder.FolderItemMillisecsMax)
                                                            Acct.CurrFolder.FolderItemMillisecsMax = nTicksProcessItemElapsed;

                                                        // Avg
                                                        Int32 nTicksElapsed = nTicksNow-nTicksPushFolderItemsStart;
                                                        Acct.CurrFolder.FolderItemMillisecsAvg = nTicksElapsed/(int)Acct.CurrFolder.NumFolderItemsMigrated;

                                                        // WriteMillisecs
                                                        Int32 nTicksWriteItemElapsed = Environment.TickCount-nTicksStartWrite;
                                                        Acct.AccountWriteMillisecs           += nTicksWriteItemElapsed;
                                                        Acct.CurrFolder.FolderWriteMillisecs += nTicksWriteItemElapsed;
                                                    }
                                                }
                                            }

                                            // Item processed - 
                                            bool bShouldFreeMem = bLastItemWasLarge || (nItemsSinceLastFreeMem > 100);
                                            if (bShouldFreeMem)
                                            {
                                                ReclaimMemory();
                                                nItemsSinceLastFreeMem = 0;
                                                //System.Threading.Thread.Sleep(5000);
                                            }

                                        } // if (!bSkipMessage) due to filter

                                        if (bSkipMessage && !bSkipMessageHistory)
                                        {
                                            Acct.NumAccountSkipsFilter++;
                                            Acct.CurrFolder.NumFolderSkipsFilter++;
                                        }

                                    }
                                    else
                                        Log.trace("Not processing items of this type");


                                    // ========================================== 
                                    // Add to history file to say we've pushed it
                                    // ========================================== 
                                    if ((historyid != "") && (!bError || bWriteHistoryEvenIfError))
                                    {
                                        try
                                        {
                                            File.AppendAllText(historyfile, historyid); //uncomment after more testing
                                            File.AppendAllText(historyfile, "\n");
                                        }
                                        catch (Exception e)
                                        {
                                            Acct.NumAccountErrs++;
                                            Log.err("Exception caught in PushFolderItems writing history to the history file", e.Message);
                                        }
                                    }

                                    // Update processed count
                                    iProcessedItems++;

                                } // foreach (dynamic itemobject in itemobjectarray)

                            } // while (iProcessedItems < nUpperWhileBound)

                            Log.summary("Pushing folder items complete");
                            Log.mem("folder items push complete");


                        } // if (itemobjectarray.GetLength(0) > 0)
                        else
                        {
                            // No items to process in this folder
                        }

                    } //if (itemobjectarray != null)
                    else
                    {
                        Log.err("GetFolderItems returned null for itemfolderlist");
                        return;
                    }

                    Log.trace("Trial # ", trial);


                } while ((!(Acct.IsCompletedMigration)) && (trial < options.MaxRetries));

                ReclaimMemory();

            } // LogBlock

        } // PushFolderItems

        public string[] GetPublicFolders(MigrationAccount Acct)
        {
            string accountName = "";
            dynamic[] folders = null;
            int idx = Acct.AccountName.IndexOf("@");
            string value = "";
            string[] s;
            bool bError = false;

            dynamic csSourceAccount = null;
            if (MigrationType == "MAPI")
                csSourceAccount = new CSMapiAccount();

            if (idx == -1)
            {
                Acct.AddProblem(Acct.AccountName, "Illegal account name", ProblemInfo.TYPE_ERR, ref bError);

                String status = string.Format("Error in Getpublicfolders");
                s = new string[1];
                s[0] = status;
                return s;
            }
            else
                accountName = Acct.AccountName.Substring(0, idx);

            try
            {
                value = csSourceAccount.InitSourceAccount(false, Acct.ProfileName, accountName, true);
            }
            catch (Exception e)
            {
                string serr = string.Format("Initialization Exception. {0}", e.Message);
                Acct.AddProblem(accountName, serr, ProblemInfo.TYPE_ERR, ref bError);
                s = new string[1];
                s[0] = serr;
                return s;
            }
            Log.info("Account name", accountName);
            Log.info("Account Id", Acct.AccountID);
            Log.info("Account Num", Acct.AccountNum.ToString());

            if (value.Length > 0)
            {
                Acct.IsValid = false;
                if (value.Contains("Outlook"))
                {
                    value = "Unable to initialize " + ". " + accountName + value;
                    Acct.AddProblem(accountName, value, ProblemInfo.TYPE_ERR, ref bError);
                }
                else
                {
                    value = "Unable to initialize " + accountName + ". " + value + " or verify if source mailbox exists.";
                    Acct.AddProblem(accountName, value , ProblemInfo.TYPE_ERR, ref bError);
                }

                string serr = string.Format("Unable to initialize");
                s = new string[1];
                s[0] = serr;
                return s;
            }
            else
            {
                Acct.IsValid = true;
                Acct.IsCompletedMigration = true;
                Log.info("Account '" + accountName + "' initialized");
            }


            Log.trace("Retrieving folders");
            try
            {
                folders = csSourceAccount.GetFolders();
            }
            catch (Exception e)
            {
                Log.err("exception in startmigration->csSourceAccount.GetFolders", e.Message);

            }
            if (folders != null)
            {
                if (folders.Count() == 0)
                {

                    Log.info("No folders for user to migrate");
                    String status = string.Format("No folders for user to migrate");
                    s = new string[1];
                    s[0] = status;
                    return s;
                }
                else
                {
                    s = new string[folders.Count()];
                    int i = 0;
                    foreach (dynamic folder in folders)
                    {
                        s[i] = folder.Name;
                        i++;
                    }
                }
            }
            else
            {
                string serrs = "CSmigrationwrapper get folders returned null for folders";
                Acct.AddProblem(accountName, serrs, ProblemInfo.TYPE_ERR, ref bError);

                Acct.IsCompletedMigration = false;
                Log.err(serrs);
                csSourceAccount.UninitSourceAccount();
                s = new string[1];
                s[0] = serrs;
                return s;
            }

            try
            {
                csSourceAccount.UninitSourceAccount();
            }
            catch (Exception e)
            {
                Log.err("Exception in UninitSourceAccount ", e.Message);

            }

            return s;
        }

        public void StartMigration(MigrationAccount Acct,
                                   MigrationOptions options,
                                   bool isServerMigration = true,
                                   bool isPreview = false,
                                   bool doRulesAndOOO = true)
        //
        // Called on multiple threads - one per account
        //
        {
            // TESTPOINT_MIGRATION_MAIN_MIGRATION_METHOD Main migration method
            dynamic[] folders = null;

            options.IsMaintainenceMode = false;
            bool bError = false;

            // =====================================================================================
            // Find the account name
            // =====================================================================================
            string accountName = "";
            int idx = Acct.AccountName.IndexOf("@");
            if (idx != -1)
                accountName = Acct.AccountName.Substring(0, idx);
            else
            {
                Acct.AddProblem(Acct.AccountName, "Illegal account name", ProblemInfo.TYPE_ERR, ref bError);
                return;
            }

            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                // =====================================================================================
                // KEYSTEP_01 Log on to source mail system PST/EXCH/ZD
                // =====================================================================================

                // TESTPOINT_MIGRATION_LOGON_TO_SOURCE Logon to datasource
                Acct.AccountStatus = "Opening source store";
                dynamic csSourceAccount = null;
                if (MigrationType == "MAPI")
                    csSourceAccount = new CSMapiAccount();

                if (!isServerMigration) // BUG 71047
                    m_csSourceAccount = csSourceAccount;


                bool bIsZDMig = options.IsZDesktop;

                Log.summary(" ");
                Log.summary("==========================================================================================================================");
                Log.summary("==========================================================================================================================");
                Log.summary("Initializing source account: " + accountName + " (ID: " + Acct.AccountID + ", Num:" + Acct.AccountNum.ToString()+ ")");
                Log.summary("==========================================================================================================================");
                Log.summary("==========================================================================================================================");

                string sRetVal = "";

                try
                {
                    bool IsPublic = options.IsPublicFolders;
                    if (IsPublic)
                    {
                        string sSrcAccount = isServerMigration?Acct.ProfileName:Acct.AccountID;
                        sRetVal = csSourceAccount.InitSourceAccount(false, sSrcAccount, accountName, true);
                    }
                    else
                        sRetVal = csSourceAccount.InitSourceAccount(isServerMigration, Acct.AccountID, accountName, false);
                }
                catch (Exception e)
                {
                    string s = string.Format("Initialization Exception. {0}", e.Message);
                    Acct.AddProblem(accountName, s, ProblemInfo.TYPE_ERR, ref bError);
                    return;
                }


                if (sRetVal.Length == 0)
                {
                    Acct.IsValid = true;
                    Acct.IsCompletedMigration = true;
                    Log.info("Account '" + accountName + "' initialized successfully");
                }
                else
                {
                    Acct.IsValid = false;
                    if (sRetVal.Contains("Outlook"))
                    {
                        sRetVal = "Unable to initialize " + accountName + ". " + sRetVal;
                        Acct.AddProblem(accountName, sRetVal, ProblemInfo.TYPE_ERR, ref bError);
                    }
                    else
                    {
                        sRetVal = "Unable to initialize " + accountName + ". " + sRetVal + " or verify if source mailbox exists.";
                        Acct.AddProblem(accountName, sRetVal, ProblemInfo.TYPE_ERR, ref bError);
                    }
                    return;
                }

                // ------------------------------------
                // Set up check for skipping folders
                // ------------------------------------
                List<string> skipList = new List<string>();
                if (options.SkipFolders != null && options.SkipFolders.Length > 0)
                {
                    string[] tokens = options.SkipFolders.Split(',');

                    for (int i = 0; i < tokens.Length; i++)
                    {
                        string token = tokens.GetValue(i).ToString();
                        skipList.Add(token.Trim());
                    }
                }

                // =====================================================================================
                // KEYSTEP_02 Get all folders from source store
                // =====================================================================================
                // TESTPOINT_MIGRATION_READ_FOLDERS Read folders from source mail system
                // This makes a managed->unmanaged COM call to obtain a vector containing ALL folders
                try
                {
                    Int32 nTicksStartGetFolders = Environment.TickCount;
                    Log.mem(">GetFolders");
                    Log.summary("Getting store's folder list...");
                    Acct.AccountStatus = "Reading folder tree";
                    folders = csSourceAccount.GetFolders();
                    Log.mem("<GetFolders");
                    Acct.AccountReadMillisecs += Environment.TickCount-nTicksStartGetFolders;
                }
                catch (Exception e)
                {
                    Log.err("exception in startmigration->csSourceAccount.GetFolders", e.Message);
                }

                if (folders != null)
                {
                    if (folders.Count() == 0)
                    {
                        Log.info("No folders for user to migrate");
                        return;
                    }
                }
                else
                {
                    string s = "CSmigrationwrapper get folders returned null for folders";
                    Acct.AddProblem(accountName, s, ProblemInfo.TYPE_ERR, ref bError);
                    Acct.IsCompletedMigration = false;
                    Log.err(s);
                    csSourceAccount.UninitSourceAccount();
                    return;
                }

                Log.trace("Retrieved folders.  Count:", folders.Count());

                // =====================================================================================
                // Initialize NumAccountItemsToMigrate, and zero NumAccountItemsMigrated
                // =====================================================================================
                // Acct.NumAccountItemsToMigrate. Note that this will include items which might later be 
                // skipped (because already migrated, or due to a filter)
                int nNumItemsToProcess = 0;
                foreach (dynamic folder in folders)
                    nNumItemsToProcess += folder.ItemCount;

                Acct.NumAccountItemsToMigrate = nNumItemsToProcess;
                Log.info("Total Items: ", nNumItemsToProcess.ToString());
                Acct.NumAccountItemsMigrated = 0;


                // =====================================================================================
                // KEYSTEP_03 Create ZimbraAPI (SOAP interface to ZCS)
                // =====================================================================================
                // TESTPOINT_MIGRATION_ZCS_LOGON Log on to ZCS
                ZimbraAPI api = new ZimbraAPI(isServerMigration, options.SpecialCharRep);

                api.AccountID = Acct.AccountID;
                api.AccountName = Acct.AccountName;


                // =====================================================================================
                // KEYSTEP_04 Send a GetTagRequest and save results to ZimbraValues
                // =====================================================================================
                Log.trace(" ");
                Log.trace(" ");
                Log.trace("===========================================================================================================");
                Log.info("Getting tags from destination account...");
                Log.trace("===========================================================================================================");
                api.GetTags(Acct);
                Log.trace(" ");
                
                Log.trace("Existing account Tags:");
                var list = Acct.TagDict.Keys.ToList();
                list.Sort();
                foreach (var key in list)
                     Log.trace(key.PadRight(40), Acct.TagDict[key]);


                // =================================================================================
                // For ZD, all folders are placed in a top-level folder which is the name of the PST
                // =================================================================================
                bool bInsertPSTFolderForZD = bIsZDMig;

                // To achieve this, we create that top-level folder here and later simply replace 
                // "/MAPIRoot" in the folder path returned by C++ layer with "/MAPIRoot/*.pst"
                string sMAPIRootSubst = "";
                if (bInsertPSTFolderForZD)
                {
                    if (Acct.AccountID.Contains(".pst"))
                    {
                        string sPSTname = Acct.AccountID.Substring(Acct.AccountID.LastIndexOf('\\') + 1);
                        Log.info("ZD -> create top-level folder", sPSTname);

                        sMAPIRootSubst = "/MAPIRoot/" + sPSTname;
                        int stat01 = api.CreateFolder(sMAPIRootSubst, 0, "", "", "");
                    }
                }

                // =====================================================================================
                // KEYSTEP_05 Process one folder at a time
                // =====================================================================================
                // TESTPOINT_MIGRATION_WALK_FOLDERS Migrate each folder
                foreach (dynamic folder in folders)
                {
                    Log.info(" ");
                    Log.info(" ");
                    Log.info("===========================================================================================================");
                    Log.info ("Processing folder '" + folder.FolderPath + "' " + " SFID:" + folder.ZimbraSpecialFolderId);
                    Log.info("===========================================================================================================");

                    // =====================================================================================
                    // Initialize Acct.CurrFolder. Event handler on this updates the UI. 
                    // =====================================================================================
                        
                    // Prevent assignments below overwriting current folder row
                    Acct.CurrFolder.FolderName = null;

                    Acct.CurrFolder.FolderStartTicks        = Environment.TickCount;
                    Acct.CurrFolder.NumFolderItemsToMigrate = folder.ItemCount;
                    Acct.CurrFolder.NumFolderItemsMigrated  = 0;
                    Acct.CurrFolder.NumFolderSkipsFilter    = 0;
                    Acct.CurrFolder.NumFolderSkipsHistory   = 0;
                    Acct.CurrFolder.NumFolderErrs           = 0;
                    Acct.CurrFolder.NumFolderWarns          = 0;
                    Acct.CurrFolder.FolderItemMillisecsMin  = 0;
                    Acct.CurrFolder.FolderItemMillisecsMax  = 0;
                    Acct.CurrFolder.FolderItemMillisecsAvg  = 0;
                    Acct.CurrFolder.FolderReadMillisecs     = 0;
                    Acct.CurrFolder.FolderWriteMillisecs    = 0;
                    Acct.CurrFolder.FolderView              = folder.ContainerClass;

                    // Causes a call to Folder_OnChanged() which causes new row to be added to results grid for this new folder
                    Acct.CurrFolder.FolderName              = folder.Name;


                    // =====================================================================================
                    // Do we need to stop?
                    // =====================================================================================
                    if (options.IsMaintainenceMode)
                    {
                        Log.err("Cancelling migration -- Mailbox is in maintainence  mode.try back later");
                        return;
                    }

                    if (options.MaxErrorCnt > 0)
                    {
                        if (Acct.NumAccountErrs > options.MaxErrorCnt)
                        {
                            Log.err("Cancelling migration -- error threshold reached");
                            return;
                        }
                    }

                    // =====================================================================================
                    // Is folder in skip list?
                    // =====================================================================================
                    if (SkipFolder(options, skipList, folder))
                    {
                        Log.summary(" ");
                        Log.summary("===================================================================================================");
                        Log.summary("Skipping folder '" + folder.folderPath + "'");
                        Log.summary("===================================================================================================");
                        Acct.CurrFolder.FolderStatus = "Skipped";
                        Acct.NumAccountSkipsFilter +=  folder.ItemCount;
                        Acct.CurrFolder.NumFolderSkipsFilter = folder.ItemCount;
                        continue;
                    }

                    // =====================================================================================
                    // First create the (empty) folder on ZCS
                    // =====================================================================================
                    // TESTPOINT_MIGRATION_CREATE_DEST_FOLDER Create destination folder

                    string sFolderPath = folder.FolderPath;
                    int nSFID = folder.ZimbraSpecialFolderId;
                    if (bInsertPSTFolderForZD)
                    {
                        // Replace MAPIRoot with UPath
                        if (sFolderPath.Contains("/MAPIRoot"))
                            sFolderPath = sFolderPath.Replace("/MAPIRoot", sMAPIRootSubst);  // \MAPIRoot\Inbox -> \MAPIRoot\xx.pst\Inbox

                        // And treat all special folders as non-special folders
                        nSFID = 0;
                    }

                    string ViewType = GetFolderViewType(folder.ContainerClass);
                    int stat = 0;
                    try
                    {
                        // Send CreateFolderRequest
                        Acct.CurrFolder.FolderStatus = "Creating folder";
                        stat = api.CreateFolder(sFolderPath, nSFID, ViewType, "", "");
                    }
                    catch (Exception e)
                    {
                        Log.err("Exception in api.CreateFolder in Startmigration ", e.Message);
                    }

                    // =====================================================================================
                    // Skip pushing contents of skipped folder 
                    // =====================================================================================
                    if (stat != 0)
                    {
                        Log.summary(" ");
                        Log.summary("===================================================================================================");
                        Log.summary("Skipping contents of skipped folder '" + folder.folderPath + "'");
                        Log.summary("===================================================================================================");
                        Acct.NumAccountSkipsFilter += (int)(Acct.CurrFolder.NumFolderItemsToMigrate);
                        Acct.CurrFolder.FolderStatus = "Parent Skipped";
                        Acct.CurrFolder.NumFolderSkipsFilter = (int)(Acct.CurrFolder.NumFolderItemsToMigrate);
                        continue;
                    }

                    // =====================================================================================
                    // Skip pushing contents of empty folder 
                    // =====================================================================================
                    if (folder.ItemCount == 0)
                    {
                        Log.summary(" ");
                        Log.summary("===================================================================================================");
                        Log.summary("Skipping empty folder '" + folder.folderPath + "'");
                        Log.summary("===================================================================================================");
                        Acct.CurrFolder.FolderStatus = "Empty";
                        continue;
                    }

                    // =====================================================================================
                    // Now populate the folder items on the server
                    // =====================================================================================
                    if (!isPreview)
                        PushFolderItems(Acct, csSourceAccount, folder, api, sFolderPath, options);

                } // foreach (dynamic folder in folders)



                // =====================================================================================
                // now do Rules
                // =====================================================================================
                Log.summary(" ");
                Log.summary(" ");
                Log.summary("===========================================================================================================");
                Log.summary("Processing Rules...");
                Log.summary("===========================================================================================================");

                Acct.CurrFolder.FolderName = "Rules Table"; // Causes it to add row to folder grid
                Acct.AccountStatus = "Migrating Rules";
                Acct.CurrFolder.FolderStatus = "Migrating...";
                Acct.CurrFolder.FolderView = "All Rules";
                Acct.CurrFolder.NumFolderItemsToMigrate = 1;
                Acct.CurrFolder.NumFolderItemsMigrated = 0;

                if (options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.Rules) && doRulesAndOOO)
                {
                    string[,] data = null;
                    try
                    {
                        // ---------------------------------------------
                        // Get rules
                        // ---------------------------------------------
                        data = csSourceAccount.GetRules();
                    }
                    catch (Exception e)
                    {
                        Log.err("Exception in StartMigration->csSourceAccount.Getrules", e.Message);
                    }

                    if (data != null)
                    {
                        Acct.NumAccountItemsToMigrate++;

                        api.AccountID = Acct.AccountID;
                        api.AccountName = Acct.AccountName;
                        if (!isPreview)
                        {
                            Dictionary<string, string> dict = new Dictionary<string, string>();
                            int bound0 = data.GetUpperBound(0);
                            if (bound0 > 0)
                            {
                                for (int i = 0; i <= bound0; i++)
                                {
                                    string Key = data[0, i];
                                    string Value = data[1, i];
                                    try
                                    {
                                        dict.Add(Key, Value);
                                    }
                                    catch (Exception e)
                                    {
                                        string s = string.Format("Exception adding {0}/{1}: {2}", Key, Value, e.Message);
                                        Log.warn(s);
                                        // Console.WriteLine("{0}, {1}", so1, so2);
                                    }
                                }
                            }
                            api.AccountID = Acct.AccountID;
                            api.AccountName = Acct.AccountName;
                            try
                            {
                                // ---------------------------------------------
                                // Push rules to ZCS
                                // ---------------------------------------------
                                Log.summary("Migrating Rules");
                                int stat = api.AddRules(dict);
                                Acct.NumAccountItemsMigrated++;
                            }
                            catch (Exception e)
                            {
                                Acct.NumAccountErrs++;
                                Log.err("CSmigrationWrapper: Exception in AddRules ", e.Message);
                            }
                            Acct.CurrFolder.NumFolderItemsMigrated = 1;
                        }
                    }
                    else
                    {
                        Log.summary("There are no rules to migrate");
                        Acct.CurrFolder.FolderStatus = "Empty";
                    }
                }
                else
                    Acct.CurrFolder.FolderStatus = "Skipped";

                // =====================================================================================
                // now do OOO
                // =====================================================================================
                Log.summary(" ");
                Log.summary(" ");
                Log.summary("===========================================================================================================");
                Log.summary("Processing OOO...");
                Log.summary("===========================================================================================================");

                Acct.CurrFolder.FolderName = "Out of Office"; // Causes it to add row to folder grid
                Acct.AccountStatus = "Migrating OOO";
                Acct.CurrFolder.FolderStatus = "Migrating...";
                Acct.CurrFolder.FolderView = "OOO";
                Acct.CurrFolder.NumFolderItemsToMigrate = 1;
                Acct.CurrFolder.NumFolderItemsMigrated = 0;


                if (options.ItemsAndFolders.HasFlag(ItemsAndFoldersOptions.OOO) && doRulesAndOOO)
                {
                    bool isOOO = false;
                    string ooo = "";
                    try
                    {
                        ooo = csSourceAccount.GetOOO();
                    }
                    catch (Exception e)
                    {
                        Log.err("Exception in StartMigration->csSourceAccount.GetOOO", e.Message);
                    }

                    if (ooo.Length > 0)
                        isOOO = (ooo != "0:");

                    if (isOOO)
                    {
                        Acct.NumAccountItemsToMigrate++;

                        api.AccountID = Acct.AccountID;
                        api.AccountName = Acct.AccountName;
                        if (!isPreview)
                        {
                            Log.summary("Migrating Out of Office");
                            try
                            {
                                api.AddOOO(ooo, isServerMigration);
                                Acct.NumAccountItemsMigrated++;
                            }
                            catch (Exception e)
                            {
                                Acct.NumAccountErrs++;
                                Log.err("CSmigrationWrapper: Exception in AddOOO ", e.Message);
                            }
                        }
                    }
                    else
                    {
                        Log.summary("Out of Office state is off, and there is no message");
                        Acct.CurrFolder.FolderStatus = "Empty";
                    }
                }
                else
                   Acct.CurrFolder.FolderStatus = "Skipped";


                Log.summary(" ");
                Log.summary(" ");
                Log.summary("===========================================================================================================");
                Log.summary("Cleaning up...");
                Log.summary("===========================================================================================================");
                try
                {
                    csSourceAccount.UninitSourceAccount();
                }
                catch (Exception e)
                {
                    Log.err("Exception in UninitSourceAccount ", e.Message);

                }

                if (!isServerMigration) // BUG 71047
                    m_csSourceAccount = null;

                Log.summary(" ");
                Log.summary(" ");
                Log.summary("===========================================================================================================");
                Log.summary("ACCOUNT MIGRATION COMPLETE");
                Log.summary("===========================================================================================================");

            } //LogBlock

        } // StartMigration

        private bool CheckifAlreadyMigrated(string filename, string itemid)
        {
            List<string> parsedData = new List<string>();

            try
            {
                if (File.Exists(filename))
                {
                    using (StreamReader readFile = new StreamReader(filename))
                    {
                        string line;
                        string row;
                        while ((line = readFile.ReadLine()) != null)
                        {
                            row = line;
                            if (row.CompareTo(itemid) == 0)
                            {
                                Log.trace("Already migrated (according to '", filename, "') -> skip");
                                return true;
                            }
                        }
                        readFile.Close();
                        return false;
                    }

                }
            }
            catch (Exception e)
            {
                Log.err("CSmigrationwrapper  CheckifAlreadyMigrated method errored out", e.Message);
            }

            return false;
        }
    }
}
