#!/usr/bin/perl
# 
# ***** BEGIN LICENSE BLOCK *****
# Zimbra Collaboration Suite Server
# Copyright (C) 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
use Getopt::Long;
my $concurrent = 10;

sub usage() {
	print STDERR "Usage: migrateLargeMetadata.pl -g [all|N]\n";
	print STDERR "-g: migrate all or N groups without prompting\n";
	exit(1);
}
my $opt_g;
GetOptions("groups=s" => \$opt_g);
my $allGroups = 0;
my $numGroups = 0;
if (defined($opt_g)) {
	if ($opt_g eq 'all') {
		$allGroups = 1;
	} else {
    $numGroups = $opt_g;
  }
} else {
  usage();
}
my $versionInDb = Migrate::getSchemaVersion();

my @groups = Migrate::getMailboxGroups();
my $sqlGroupsWithSmallMailitem = <<_SQL_;
SELECT table_schema FROM information_schema.columns
WHERE table_name = 'mail_item' AND column_name = 'size' AND data_type = 'int'
ORDER BY table_schema;
_SQL_
my %mailItemGroups  = map { $_ => 1 } Migrate::runSql($sqlGroupsWithSmallMailitem);

my $sqlGroupsWithSmallRevisions = <<_SQL_;
SELECT table_schema FROM information_schema.columns
WHERE table_name = 'revision' AND column_name = 'size' AND data_type = 'int'
ORDER BY table_schema;
_SQL_
my %revisionGroups = map { $_ => 1 } Migrate::runSql($sqlGroupsWithSmallRevisions);

my %groupsToMigrate = (%mailItemGroups, %revisionGroups);
my @groupsToMigrate = sort keys %groupsToMigrate;

my $total = scalar(@groups);
my $totalToMigrate = scalar(@groupsToMigrate);
if ($totalToMigrate < 1) {
	print "No mailbox group needs to be migrated.\n";
	exit(0);
}
print "$totalToMigrate out of $total mailbox groups need to be migrated.\n";

my $toMigrate;
if ($allGroups) {
	$toMigrate = $totalToMigrate;
} else {
	$toMigrate = $numGroups;

	$toMigrate = $totalToMigrate
    if ($numGroups >= $totalToMigrate);
}
print "Migrating $toMigrate out of $totalToMigrate mailbox groups\n";

my $migrated = 0;
my @sql = ();
foreach my $group (@groupsToMigrate) {
	last if ($migrated >= $toMigrate);
	print "migrating $group\n";
  if (exists $mailItemGroups{$group}) {
    my $sql = <<_SQL_;
ALTER TABLE $group.mail_item MODIFY COLUMN size BIGINT UNSIGNED NOT NULL;
_SQL_
    push(@sql, $sql);
  }
  if (exists $revisionGroups{$group}) {
    my $sql = <<_SQL_;
ALTER TABLE $group.revision MODIFY COLUMN size BIGINT UNSIGNED NOT NULL;
_SQL_
    push(@sql, $sql);
  }
  $migrated++;
}
my $start = time();
Migrate::runSqlParallel($concurrent, @sql);
my $elapsed = time() - $start;
print "\nMigrated $toMigrate mailbox groups in $elapsed seconds\n";

exit(0);
