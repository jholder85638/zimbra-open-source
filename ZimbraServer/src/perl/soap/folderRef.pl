#!/usr/bin/perl -w
# 
# ***** BEGIN LICENSE BLOCK *****
# Zimbra Collaboration Suite Server
# Copyright (C) 2005, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc.
# 
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free Software Foundation,
# version 2 of the License.
# 
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with this program.
# If not, see <http://www.gnu.org/licenses/>.
# ***** END LICENSE BLOCK *****
# 

#
# Simple SOAP test-harness for the AddMsg API
#

use strict;

use lib '.';

use LWP::UserAgent;

use XmlElement;
use XmlDoc;
use Soap;

my $userId;
my $folderId;
my $refUrl;

if (defined $ARGV[1] && $ARGV[1] ne "") {
    $userId = $ARGV[0];
    $folderId = $ARGV[1];

    if (defined $ARGV[2]) {
        $refUrl = $ARGV[2];
    }
} else {
    print "USAGE: folderRef USERID FOLDERID [URL]\n";
    exit 1;
}

my $ACCTNS = "urn:zimbraAccount";
my $MAILNS = "urn:zimbraMail";

my $url = "http://localhost:7070/service/soap/";

my $SOAP = $Soap::Soap12;
my $d = new XmlDoc;
$d->start('AuthRequest', $ACCTNS);
$d->add('account', undef, { by => "name"}, $userId);
$d->add('password', undef, undef, "test123");
$d->end();

{
    print "\nOUTGOING XML:\n-------------\n";
    my $out =  $d->to_string("pretty");
    $out =~ s/ns0\://g;
    print $out."\n";
}


my $authResponse = $SOAP->invoke($url, $d->root());

print "AuthResponse = ".$authResponse->to_string("pretty")."\n";

my $authToken = $authResponse->find_child('authToken')->content;
print "authToken($authToken)\n";

my $sessionId = $authResponse->find_child('sessionId')->content;
print "sessionId = $sessionId\n";

my $context = $SOAP->zimbraContext($authToken, $sessionId);

my $contextStr = $context->to_string("pretty");
print("Context = $contextStr\n");

$d = new XmlDoc;
$d->start('FolderActionRequest', $MAILNS); {
    if (defined $refUrl) {
        $d->add('action', $MAILNS, { 'id' => $folderId, 'op' => 'urlRefresh', 'url' => $refUrl});
    } else {
        $d->add('action', $MAILNS, { 'id' => $folderId, 'op' => 'urlRefresh'});
    }
} $d->end();

print "\nOUTGOING XML:\n-------------\n";
my $out =  $d->to_string("pretty");
$out =~ s/ns0\://g;
print $out."\n";

my $response;

$response = $SOAP->invoke($url, $d->root(), $context);

print "\nRESPONSE:\n--------------\n";
$out =  $response->to_string("pretty");
$out =~ s/ns0\://g;
print $out."\n";

