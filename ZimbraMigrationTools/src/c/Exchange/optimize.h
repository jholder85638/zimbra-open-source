/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2015, 2016 Synacor, Inc.
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

#define ENABLE_MIGRATION_OPTIMIZATIONS

#ifdef ENABLE_MIGRATION_OPTIMIZATIONS
//#define DCB_PERFORMANCE_DEFER_GETCONTENTSTABLE  // Required by MAPIFOlder2FolderData, so need to make that get it from hierarchy table prop first
#endif


#define CASE_184490_DIAGNOSTICS
/*
 - Additional logging
 - De-inline AToW because it might be hindering DMP analysis
 - Removes DMP suppression code in GenerateCoreDump
*/
