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

namespace MVVM.Model
{
using System;


// ============================================================
// IMPT: If you change any of the structs below, then
// please update the SchemaVersion in PopulateConfigForSaving()
// ============================================================

public class GeneralOptions
{
    public Int32   SchemaVersion     {get; set;}
    public Int32   MigType           {get; set;}
    public Int32   MaxThreadCount    {get; set;}
    public Int32   MaxErrorCount     {get; set;}
    public Int32   MaxWarningCount   {get; set;}
    public Boolean Enablelog         {get; set;}
    public string  LogFilelocation   {get; set;}
    public string  LogLevel          {get; set;}
}

public class SourceServer
{
    public string Hostname      {get; set;}
    public string AdminID       {get; set;}
    public string AdminPwd      {get; set;}
    public string Profile       {get; set;}
    public string DataFile      {get; set;}
    public bool   UseProfile    {get; set;}
}

public class ZimbraServer
{
    public string Hostname      {get; set;}
    public string Port          {get; set;}
    public bool   UseSSL        {get; set;}
    public string AdminID       {get; set;}
    public string AdminPwd      {get; set;}
    public string UserAccount   {get; set;}
    public string UserPassword  {get; set;}
}

public class UserProvision
{
    public string COS               {get; set;}
    public string DefaultPWD        {get; set;}
    public string DestinationDomain {get; set;}
}

public class ImportOptions
{
    public bool Mail            {get; set;}
    public bool Contacts        {get; set;}
    public bool Calendar        {get; set;}
    public bool Tasks           {get; set;}
    public bool DeletedItems    {get; set;}
    public bool Junk            {get; set;}
    public bool Sent            {get; set;}
    public bool Rules           {get; set;}
    public bool OOO             {get; set;}
}

public class AdvancedImportOptions
{
    public DateTime MigrateOnOrAfter      {get; set;}
    public bool IsOnOrAfter               {get; set;}
    public string MaxMessageSize          {get; set;}
    public string DateFilterItem          {get; set;}
    public bool IsMaxMessageSize          {get; set;}

    public Folder[] FoldersToSkip;
    public bool   IsSkipFolders           {get; set;}
    public bool   IsSkipPrevMigratedItems {get; set;}
    public string SpecialCharReplace      {get; set;}
    public string CSVDelimiter            {get; set;}
    public Int32  MaxRetries              {get; set;}
    public bool   IsPublicFolders         {get; set;}
    public bool   IsZDesktop              {get; set;}
}

public class Folder
{
    public string FolderName    {get; set;}
}


// Holds contents of a config XML file
public class Config
{
    public Config() {}
    public Config(string mailserver, string srcAdminId, string srcAdminPwd, string outlookProfile, 
                  string pstFile, string zimbraserverhostname, string zimbraport, string zimbraAdmin, 
                  string zimbrapasswd, string zimbradomain, string pstfile)
    {
        using (LogBlock logblock = Log.NotTracing() ? null : Log.NotTracing() ? null : new LogBlock("Config.Config"))
        {
            this.ZimbraServer  = new ZimbraServer();
            this.SourceServer  = new SourceServer();
            this.ImportOptions = new ImportOptions();
            this.UserProvision = new UserProvision();

            this.AdvancedImportOptions = new AdvancedImportOptions();
            //this.LoggingOptions = new LoggingOptions();
            this.AdvancedImportOptions.FoldersToSkip = null;

            this.ZimbraServer.Hostname      = zimbraserverhostname;
            this.ZimbraServer.Port          = zimbraport;
            this.ZimbraServer.AdminID       = zimbraAdmin;
            this.ZimbraServer.AdminPwd      = zimbrapasswd;
            // this.zimbraServer.Domain     = zimbradomain;
            this.SourceServer.Hostname      = mailserver;
            this.SourceServer.AdminID       = srcAdminId;
            this.SourceServer.AdminPwd      = srcAdminPwd;
            this.SourceServer.Profile       = outlookProfile;
            this.SourceServer.DataFile      = pstFile;
            this.SourceServer.UseProfile    = true;

            this.GeneralOptions = new GeneralOptions();

            // this.mailServer.PSTFile = pstfile;
            // this.mailServer.ProfileName = outlookProfile;
        }
    }

    public SourceServer SourceServer;
    public ZimbraServer ZimbraServer;
    public ImportOptions ImportOptions;
    public UserProvision UserProvision;
    public AdvancedImportOptions AdvancedImportOptions;
   // public LoggingOptions LoggingOptions;
    public GeneralOptions GeneralOptions;
}


}
