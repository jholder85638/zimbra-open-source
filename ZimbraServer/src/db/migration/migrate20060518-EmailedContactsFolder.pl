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

Migrate::verifySchemaVersion(23);

my @mailboxIds = Migrate::getMailboxIds();
foreach my $id (@mailboxIds) {
    createEmailedContactFolder($id);
}

Migrate::updateSchemaVersion(23, 24);

exit(0);

#####################

sub createEmailedContactFolder($) {
    my ($mailboxId) = @_;
    my $timestamp = time();
    my $sql = <<EOF_RENAME_EMAILED_CONTACT_FOLDER;
    
UPDATE mailbox$mailboxId.mail_item mi, zimbra.mailbox mbx
SET subject = "Emailed Contacts_1",
    mod_metadata = change_checkpoint + 100,
    mod_content = change_checkpoint + 100,
    change_checkpoint = change_checkpoint + 200
WHERE subject = "Emailed Contacts" AND folder_id = 1 AND mi.id != 13 AND mbx.id = $mailboxId;

EOF_RENAME_EMAILED_CONTACT_FOLDER
    Migrate::runSql($sql);

    my $sql = <<EOF_CREATE_EMAILED_CONTACT_FOLDER;
    
INSERT INTO mailbox$mailboxId.mail_item
  (subject, id, type, parent_id, folder_id, mod_metadata, mod_content, metadata, date, change_date)
VALUES
  ("Emailed Contacts", 13, 1, 1, 1, 1, 1, "d1:ai1e1:vi9e2:vti6ee", $timestamp, $timestamp)
ON DUPLICATE KEY UPDATE id = 13;

UPDATE mailbox$mailboxId.mail_item mi, zimbra.mailbox mbx
SET mod_metadata = change_checkpoint + 100,
    mod_content = change_checkpoint + 100,
    change_checkpoint = change_checkpoint + 200
WHERE mi.id = 13 AND mbx.id = $mailboxId;

EOF_CREATE_EMAILED_CONTACT_FOLDER
    Migrate::runSql($sql);
}
