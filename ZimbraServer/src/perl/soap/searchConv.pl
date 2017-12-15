#!/usr/bin/perl -w
# 
# ***** BEGIN LICENSE BLOCK *****
# Zimbra Collaboration Suite Server
# Copyright (C) 2004, 2005, 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc.
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

my $ACCTNS = "urn:zimbraAccount";
my $MAILNS = "urn:zimbraMail";

my $url = "http://localhost:7070/service/soap/";

my $user;
my $convId;
my $searchString;

if (defined $ARGV[2] && $ARGV[2] ne "") {
    $user = $ARGV[0];
    $convId = $ARGV[1];
    $searchString = $ARGV[2];
} else {
    die "Usage search USER CONVID QUERYSTR";
}

my $SOAP = $Soap::Soap12;
my $d = new XmlDoc;
$d->start('AuthRequest', $ACCTNS);
$d->add('account', undef, { by => "name"}, $user);
$d->add('password', undef, undef, "test123");
$d->end();

my $authResponse = $SOAP->invoke($url, $d->root());

print "AuthResponse = ".$authResponse->to_string("pretty")."\n";

my $authToken = $authResponse->find_child('authToken')->content;

print "authToken($authToken)\n";

my $context = $SOAP->zimbraContext($authToken);

#
#<SearchRequest xmlns="urn:zimbraMail">
# <query>tag:\unseen</query>
#</SearchRequest>

my %msgAttrs;
#$msgAttrs{'l'} = "/sent mail";
#$msgAttrs{'t'} = "\\unseen ,34 , \\FLAGGED";

$d = new XmlDoc;
my %queryAttrs;
$queryAttrs{'cid'} = $convId;
$queryAttrs{'sortby'} = "datedesc";
$queryAttrs{'groupBy'} = "none";
$queryAttrs{'fetch'} = "1";
$d->start('SearchConvRequest', $MAILNS, \%queryAttrs);

$d->start('query', undef, undef, $searchString);

$d->end(); # 'query'
$d->end(); # 'SearchRequest'

print "\nOUTGOING XML:\n-------------\n";
print $d->to_string("pretty")."\n";

my $response = $SOAP->invoke($url, $d->root(), $context);


print "\nRESPONSE:\n--------------\n";
my $str = $response->to_string("pretty")."\n";
$str =~ s/<\/?ns0\:/</g;
print $str;

