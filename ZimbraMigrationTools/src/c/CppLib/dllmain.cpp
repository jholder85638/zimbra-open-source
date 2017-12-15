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

// dllmain.cpp : Defines the entry point for the DLL application.
#include "common.h"
#include "Logger.h"

extern "C" 
{

BOOL APIENTRY DllMain(HMODULE module, DWORD reason, LPVOID reserved)
//
// Initialise logging
//
{
    (void)module;
    (void)reserved;
    (void)reason;
    return TRUE;
}

}