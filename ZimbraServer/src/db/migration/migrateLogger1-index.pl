#!/usr/bin/perl
# 
# ***** BEGIN LICENSE BLOCK *****
# Zimbra Collaboration Suite Server
# Copyright (C) 2005, 2007, 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free Software Foundation,
# version 2 of the License.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with this program.
# If not, see <https://www.gnu.org/licenses/>.
# ***** END LICENSE BLOCK *****
# 


use strict;
use Migrate;


#Migrate::verifyLoggerSchemaVersion(0);

addIndices();

#Migrate::updateLoggerSchemaVersion(0,1);

exit(0);

#####################

sub addIndices() {
    Migrate::log("Adding Indices");

    my $sql = <<EOF;
alter table mta add index i_arrive_time (arrive_time);
alter table amavis add index i_arrive_time (arrive_time);
alter table mta_aggregate add index i_period_start (period_start);
alter table mta_aggregate add index i_period_end (period_end);
alter table amavis_aggregate add index i_period_start (period_start);
alter table amavis_aggregate add index i_period_end (period_end); 
CREATE TABLE config (
	name        VARCHAR(255) NOT NULL PRIMARY KEY,
	value       TEXT,
	description TEXT,
	modified    TIMESTAMP
) ENGINE = MyISAM;
EOF

    Migrate::runLoggerSql($sql);
}
