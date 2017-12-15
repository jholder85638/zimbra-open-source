#!/usr/bin/perl
# 
# ***** BEGIN LICENSE BLOCK *****
# Zimbra Collaboration Suite Server
# Copyright (C) 2006, 2007, 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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

Migrate::verifySchemaVersion(26);

my @mailboxIds = Migrate::getMailboxIds();
my $sql = addContactCountColumn();
foreach my $id (@mailboxIds) {
    $sql .= resizeUnreadColumn($id);
}

Migrate::runSql($sql);

Migrate::updateSchemaVersion(26, 27);

exit(0);

#####################

sub addContactCountColumn() {
    my $sql = <<ADD_CONTACT_COUNT_COLUMN_EOF;
ALTER TABLE zimbra.mailbox
ADD COLUMN contact_count INTEGER UNSIGNED DEFAULT 0 AFTER item_id_checkpoint;

UPDATE zimbra.mailbox
SET contact_count = NULL;

ADD_CONTACT_COUNT_COLUMN_EOF

    return $sql;
}

sub resizeUnreadColumn($) {
    my ($mailboxId) = @_;
    my $sql = <<RESIZE_UNREAD_COLUMN_EOF;
ALTER TABLE mailbox$mailboxId.mail_item
MODIFY COLUMN unread INTEGER UNSIGNED;

RESIZE_UNREAD_COLUMN_EOF

    return $sql;
}
