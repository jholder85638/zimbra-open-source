' * ***** BEGIN LICENSE BLOCK *****
' * Zimbra Collaboration Suite Server
' * Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
' *
' * This program is free software: you can redistribute it and/or modify it under
' * the terms of the GNU General Public License as published by the Free Software Foundation,
' * version 2 of the License.
' *
' * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
' * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
' * See the GNU General Public License for more details.
' * You should have received a copy of the GNU General Public License along with this program.
' * If not, see <https://www.gnu.org/licenses/>.
' * ***** END LICENSE BLOCK *****
' */
'
' A script that helps prism hide console window when calling zdctl.vbs
' This is to workaround a limitation of XPCOM's nsIProcess

Dim oFso, oShell, sCScript, sScriptPath, sZdCtl, sCmd

Set oFso = CreateObject("Scripting.FileSystemObject")
sCScript = Chr(34) & oFso.GetSpecialFolder(1).Path & "\cscript.exe" & Chr(34)

sScriptPath = WScript.ScriptFullName
sZdCtl = Chr(34) & Left(sScriptPath, InStrRev(sScriptPath, WScript.ScriptName) - 2) & "\zdctl.vbs" & Chr(34)

Set oShell = CreateObject("WScript.Shell")
sCmd = sCScript & " " & sZdCtl & " " & WScript.Arguments.Item(0)
oShell.Run sCmd, 0, false
