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

using System.Reflection;
using System.Resources;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Windows;

// General Information about an assembly is controlled through the following
// set of attributes. Change these attribute values to modify the information
// associated with an assembly.
[assembly: AssemblyTitle(Misc.Constants.ZIMBRA_MIG_TOOL_NAME + " XBAP")]
[assembly: AssemblyDescription(Misc.Constants.ZIMBRA_MIG_TOOL_NAME + " XBAP")]
[assembly: AssemblyConfiguration("")]
[assembly: AssemblyCompany(Misc.Constants.ZIMBRA_VERSION_COMPANY)]
[assembly: AssemblyProduct(Misc.Constants.ZIMBRA_MIG_TOOL_NAME)]
[assembly: AssemblyCopyright(Misc.Constants.ZIMBRA_VERSION_COPYRIGHT)]
[assembly: AssemblyTrademark("")]
[assembly: AssemblyCulture("")]

// Setting ComVisible to false makes the types in this assembly not visible
// to COM components.  If you need to access a type in this assembly from
// COM, set the ComVisible attribute to true on that type.
[assembly: ComVisible(false)]

// In order to begin building localizable applications, set
// <UICulture>CultureYouAreCodingWith</UICulture> in your .csproj file
// inside a <PropertyGroup>.  For example, if you are using US english
// in your source files, set the <UICulture> to en-US.  Then uncomment
// the NeutralResourceLanguage attribute below.  Update the "en-US" in
// the line below to match the UICulture setting in the project file.

// [assembly: NeutralResourcesLanguage("en-US", UltimateResourceFallbackLocation.Satellite)]

[assembly: ThemeInfo(ResourceDictionaryLocation.None,   // where theme specific resource dictionaries are located
                                                        // (used if a resource is not found in the page,
                                                        // or application resource dictionaries)
ResourceDictionaryLocation.SourceAssembly               // where the generic resource dictionary is located
                                                        // (used if a resource is not found in the page,
                                                        // app, or any theme specific resource dictionaries)
)]

// Version information for an assembly consists of the following four values:
//
// Major Version
// Minor Version
// Build Number
// Revision
//
// You can specify all the values or you can default the Build and Revision Numbers
// by using the '*' as shown below:
// [assembly: AssemblyVersion("1.0.*")]
[assembly: AssemblyVersion("1.0.0.0")]
[assembly: AssemblyFileVersion(Misc.Constants.ZIMBRA_VERSION_STRING)]
