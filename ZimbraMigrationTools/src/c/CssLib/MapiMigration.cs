/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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
using System.IO;
using Microsoft.Win32;

namespace CssLib
{
    // ==================================================================================================================================================
    // class CompatibilityChk
    // ==================================================================================================================================================
    public class CompatibilityChk
    {
        public enum MachineType : ushort
        {
            IMAGE_FILE_MACHINE_UNKNOWN = 0x0,
            IMAGE_FILE_MACHINE_AM33 = 0x1d3,
            IMAGE_FILE_MACHINE_AMD64 = 0x8664,
            IMAGE_FILE_MACHINE_ARM = 0x1c0,
            IMAGE_FILE_MACHINE_EBC = 0xebc,
            IMAGE_FILE_MACHINE_I386 = 0x14c,
            IMAGE_FILE_MACHINE_IA64 = 0x200,
            IMAGE_FILE_MACHINE_M32R = 0x9041,
            IMAGE_FILE_MACHINE_MIPS16 = 0x266,
            IMAGE_FILE_MACHINE_MIPSFPU = 0x366,
            IMAGE_FILE_MACHINE_MIPSFPU16 = 0x466,
            IMAGE_FILE_MACHINE_POWERPC = 0x1f0,
            IMAGE_FILE_MACHINE_POWERPCFP = 0x1f1,
            IMAGE_FILE_MACHINE_R4000 = 0x166,
            IMAGE_FILE_MACHINE_SH3 = 0x1a2,
            IMAGE_FILE_MACHINE_SH3DSP = 0x1a3,
            IMAGE_FILE_MACHINE_SH4 = 0x1a6,
            IMAGE_FILE_MACHINE_SH5 = 0x1a8,
            IMAGE_FILE_MACHINE_THUMB = 0x1c2,
            IMAGE_FILE_MACHINE_WCEMIPSV2 = 0x169,
        }

        public static MachineType GetDllMachineType(string dllPath)
        {
            // http://www.microsoft.com/whdc/system/platform/firmware/PECOFF.mspx 

            FileStream fs = new FileStream(dllPath, FileMode.Open, FileAccess.Read);
            BinaryReader br = new BinaryReader(fs);
            fs.Seek(0x3c, SeekOrigin.Begin);
            Int32 peOffset = br.ReadInt32();
            fs.Seek(peOffset, SeekOrigin.Begin);
            UInt32 peHead = br.ReadUInt32();
            if (peHead != 0x00004550) // "PE\0\0", little-endian 
                throw new Exception("Can't find PE header");
            MachineType machineType = (MachineType)br.ReadUInt16();
            br.Close();
            fs.Close();
            return machineType;
        }

        // returns true if the dll is 64-bit, false if 32-bit, and null if unknown 
        public static bool? UnmanagedDllIs64Bit(string dllPath)
        {
            switch (GetDllMachineType(dllPath))
            {
                case MachineType.IMAGE_FILE_MACHINE_AMD64:
                case MachineType.IMAGE_FILE_MACHINE_IA64:
                    return true;
                case MachineType.IMAGE_FILE_MACHINE_I386:
                    return false;
                default:
                    return null;
            }
        }

        public static string CheckCompat(string path)
        {
            string status = "";
            string absolutepath = Path.GetFullPath("Exchange.dll");

            bool retval = UnmanagedDllIs64Bit(absolutepath).Value;

            string Bitness = (string)Microsoft.Win32.Registry.GetValue(@"HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Office\14.0\Outlook", "Bitness", null);
            if (Bitness != null)
            {
                if ((Bitness == "x64") && (!retval))
                    status = "Outlook is 64 bit and migration is 32 bit";
            }
            return status;
        }
    }

    // ==================================================================================================================================================
    // class CSMapiTools
    // ==================================================================================================================================================
    //
    // Tools to query MAPI info like profile list, exchange users etc
    //
    public class CSMapiTools : CSSourceTools
    {
        public Exchange.MapiTools cppTools;

        static public string  checkPrereqs()
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(/*GetType() + "." + */System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                string str = "";
                string path = Directory.GetDirectoryRoot(Directory.GetCurrentDirectory());
                string absolutepath = Path.GetFullPath("Exchange.dll");

                bool bitness = CompatibilityChk.UnmanagedDllIs64Bit(absolutepath).Value;

                string registryValue = string.Empty;
                RegistryKey localKey = null;
                if (Environment.Is64BitOperatingSystem)
                    localKey = RegistryKey.OpenBaseKey(Microsoft.Win32.RegistryHive.LocalMachine, RegistryView.Registry64);
                else
                    localKey = RegistryKey.OpenBaseKey(Microsoft.Win32.RegistryHive.LocalMachine, RegistryView.Registry32);

                try
                {
                    localKey = localKey.OpenSubKey(@"Software\\Microsoft\Office\\");
                    // registryValue = localKey.GetValue("TestKey").ToString();
                    if (localKey.SubKeyCount > 0)
                    {
                        if ((localKey.GetSubKeyNames()).Contains(@"Outlook"))
                        {
                        }
                        else
                        {
                            str = "Outlook is not installed on this system.Please Install Outlook"; // Still need this?
                            return str;
                        }
                    }
                }
                catch (Exception e)
                {
                    str = "Execption in reading regsitry for outlook prereq " + e.Message;
                    return str;
                }



                string InstallPath = (string)Registry.GetValue(@"HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Office\14.0\Outlook", "Bitness", null);
                if (InstallPath == null)
                    InstallPath =    (string)Registry.GetValue(@"HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Office\15.0\Outlook", "Bitness", null);
                if (InstallPath == null)
                    InstallPath =    (string)Registry.GetValue(@"HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Office\16.0\Outlook", "Bitness", null);

                if (InstallPath != null)
                {
                    //its 64 bit outlook and 64 bit migration
                    if (bitness == true)
                    {
                        if (InstallPath == "x64")
                        {
                            //64 bit mapi and 64 bit migration perfect
                        }
                        else
                        {
                            if (InstallPath == "x86")
                            {
                                //32 bit mapi and 64 bit migration cannot continue
                                str = "The 64 bit Migration wizard is not compatible with MAPI 32 bit libraries.  Please run the 32 bit Migration wizard.";
                                return str;
                            }
                        }
                    }
                    else
                    {
                        if (InstallPath == "x64")
                        {
                            //64 bit mapi and 32 bit bit migration cannot continue
                            str = "The 32 bit Migration wizard is not compatible with MAPI 64 bit libraries.  Please run the 64 bit Migration wizard.";
                            return str;
                        }
                        else
                        {
                            if (InstallPath == "x86")
                            {
                                //32 bit mapi and 32 bit migration perfect
                            }
                        }
                    }
                }
                else
                {
                    if (bitness == true)
                    {
                        //32 bit mapi and 64 bitmigration
                        str = "Older versions of Outlook are not compatible with the 64 bit Migration wizard.  Please run the 32 bit Migration wizard.";
                        return str;
                    }
                    else
                    {
                        //32 bit mapi and 32 bit migration.
                    }
                }
                return str;
            }
        }

        public CSMapiTools()
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("CSMapiTools.CSMapiTools"))
            {
                // Check OL installed, and if so create aMAPI wrapper
                string message = CSMapiTools.checkPrereqs();
                if (message == "")
                    cppTools = new Exchange.MapiTools();   // Defined in Exchange.idl See DCB_NOTE_MAPIWRAPPER_DEF
                else
                    throw new Exception(message);
            }
        }

        public override string InitSourceTools(string ProfileOrServerName, string AdminUser, string AdminPassword)
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                string s = "";
                try
                {
                    s = cppTools.InitMapiTools(ProfileOrServerName, AdminUser, AdminPassword);
                }
                catch (Exception e)
                {
                    s = string.Format("Initialization Exception. Make sure to enter the proper credentials: {0}", e.Message);
                }
                return s;
            }
        }

        public override string UninitSourceTools()
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                try
                {
                    return cppTools.UninitMapiTools();
                }
                catch (Exception e)
                {
                    string msg = string.Format("UninitSourceTools Exception: {0}", e.Message);
                    return msg;
                }
            }
        }

        public override bool AvoidInternalErrors(string strErr)
        {
            return (cppTools.AvoidInternalErrors(strErr)!=0);
        }

        public override string GetProfileList(out object  var)
        {
            return cppTools.GetProfileList(out var);
        }

        public override string SelectExchangeUsers(out object var)
        {
            return cppTools.SelectExchangeUsers(out var);
        }
    }

    // ==================================================================================================================================================
    // class CSMapiAccount
    // ==================================================================================================================================================
    //
    // Represents a single MAPI account being migrated
    //
    public class CSMapiAccount : CSSourceAccount
    {
        public Exchange.MapiAccount cppAccount;
        public Exchange.MapiAccount GetInternalUser()
        {
            return cppAccount;
        }

        public CSMapiAccount()
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock("CSMapiAccount.CSMapiAccount"))
            {
                cppAccount = new Exchange.MapiAccount();
            }
        }

        public override string InitSourceAccount(bool bIsServerMigration, string sSrcAccount, string sZCSAccount, bool bIsPublicFoldersMigration)
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                string value = "";
                try
                {
                    // Tip: Following calls unmanaged C++ function CUserObject::Init()
                    // in UserObject.cpp. To step into it make sure unmanaged debugging enabled
                    // Solution Explorer -> Right click ZimbraMigration -> Properties -> Debug -> Enable unmanaged code debugging
                    value = cppAccount.InitMapiAccount(bIsServerMigration?1:0, sSrcAccount, sZCSAccount, bIsPublicFoldersMigration?1:0);
                }
                catch (Exception e)
                {
                    Log.err("CSMapiAccount::InitSourceAccount exception", e.Message);
                }
                return value;
            }
        }

        public override dynamic[] GetFolders()
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                dynamic[] folders = null;
                try
                {
                    folders = cppAccount.GetFolders();
                }
                catch (Exception e)
                {
                    Log.err("CSMapiAccount::Getfolders exception", e.Message);
                }
                return folders;
            }
        }

        public override dynamic[] GetFolderItems(dynamic folderobject, double Date)
        {
            using (LogBlock logblock = Log.NotTracing()?null: new LogBlock(GetType() + "." + System.Reflection.MethodBase.GetCurrentMethod().Name))
            {
                dynamic[] Itemsfolder = null;
                try
                {
                    Itemsfolder = cppAccount.GetFolderItems(folderobject, Date);
                }
                catch (Exception e)
                {
                    Log.err("CSMapiAccount::GetFolderItems exception", e.Message);
                }
                return Itemsfolder;
            }
        }

        public override string[,] GetRules()
        {
            try
            {
                return cppAccount.GetRules();
            }
            catch (Exception e)
            {
                Log.err("CSMapiAccount::GetRules exception", e.Message);
                return null;
            }
        }
        public override string GetOOO()
        {
            return cppAccount.GetOOO();
        }

        public override void UninitSourceAccount()
        {
            try
            {
                cppAccount.UninitMapiAccount();
            }
            catch(Exception e)
            {
                 Log.err("CSMapiAccount::UninitSourceAccount exception", e.Message);
            }
        }
    }
}
