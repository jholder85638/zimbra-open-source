-- 
-- ***** BEGIN LICENSE BLOCK *****
-- Zimbra Collaboration Suite Server
-- Copyright (C) 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
--
-- This program is free software: you can redistribute it and/or modify it under
-- the terms of the GNU General Public License as published by the Free Software Foundation,
-- version 2 of the License.
--
-- This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
-- without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
-- See the GNU General Public License for more details.
-- You should have received a copy of the GNU General Public License along with this program.
-- If not, see <https://www.gnu.org/licenses/>.
-- ***** END LICENSE BLOCK *****
-- 
.bail ON
.read "@ZIMBRA_INSTALL@db/db.sql"
.read "@ZIMBRA_INSTALL@db/directory.sql"
.read "@ZIMBRA_INSTALL@db/wildfire.sql"
.read "@ZIMBRA_INSTALL@db/versions-init.sql"
.read "@ZIMBRA_INSTALL@db/default-volumes.sql"
INSERT INTO config(name, value, description) VALUES ('offline.db.version', '5', 'offline db schema version');

.exit