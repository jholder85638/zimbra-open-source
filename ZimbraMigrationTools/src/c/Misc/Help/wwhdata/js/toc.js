/*
 * ***** BEGIN LICENSE BLOCK *****
 *
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2012, 2014, 2016 Synacor, Inc.
 *
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at: https://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15
 * have been added to cover use of software over a computer network and provide for limited attribution
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 * The Original Code is Zimbra Open Source Web Client.
 * The Initial Developer of the Original Code is Zimbra, Inc.  All rights to the Original Code were
 * transferred by Zimbra, Inc. to Synacor, Inc. on September 14, 2015.
 *
 * All portions of the code are Copyright (C) 2012, 2014, 2016 Synacor, Inc. All Rights Reserved.
 *
 * ***** END LICENSE BLOCK *****
 */
function  WWHBookData_AddTOCEntries(P)
{
var A=P.fN("Starting the Migration","0");
A=P.fN("Server Migration Source Information","1");
A=P.fN("User Migration Source Information","2");
A=P.fN("Destination Server Configuration (Server)","3");
A=P.fN("Destination Server Configuration (User)","4");
A=P.fN("Selecting Options to Migrate (Server)","5");
A=P.fN("Selecting Options to Migrate (User)","6");
A=P.fN("Selecting Users to Migrate","7");
var B=A.fN("Use Load CSV","8");
B=A.fN("Use Object Picker","9");
B=A.fN("Use LDAP Browser","10");
B=A.fN("Add Users Manually","11");
A=P.fN("Selecting Public Folders to Migrate", "12");
A=P.fN("Scheduling the Migration", "13");
A=P.fN("Viewing Migration Results","14");
}
