/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2013, 2014, 2016 Synacor, Inc.
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
// --MBLOGON.h------------------------------------------------------------------
// 
//  Header file for module containing message store logon/logoff functions.
// 
// Copyright 1986 - 1998 Microsoft Corporation.  All Rights Reserved.
// -----------------------------------------------------------------------------

#ifndef _MBLOGON_H
#define _MBLOGON_H

#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

//$--HrMailboxLogon------------------------------------------------------
// -----------------------------------------------------------------------------
HRESULT HrMailboxLogon(
    IN  LPMAPISESSION   lplhSession,                // ptr to MAPI session handle
    IN  LPMDB           lpMDB,                      // ptr to message store
    IN  LPSTR           lpszMsgStoreDN,             // ptr to message store DN
    IN  LPSTR           lpszMailboxDN,              // ptr to mailbox DN
    OUT LPMDB           *lppMailboxMDB);            // ptr to mailbox message store ptr

//$--HrMailboxLogoff-------------------------------------------------------
// -----------------------------------------------------------------------------
HRESULT HrMailboxLogoff(
    IN OUT LPMDB *lppMailboxMDB);                   // mailbox message store pointer

#ifdef __cplusplus
}
#endif // __cplusplus

#endif // _MBLOGON_H
