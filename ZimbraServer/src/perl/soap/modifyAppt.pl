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

use Time::HiRes qw ( time );
use strict;

use lib '.';

use LWP::UserAgent;

use XmlElement;
use XmlDoc;
use Soap;

my $ACCTNS = "urn:zimbraAccount";
my $MAILNS = "urn:zimbraMail";

my $invId;
my $mode = 0; # >= 2: remove attendee (broken right now)
my $startTime;
my $endTime;


if (defined($ARGV[2]) && $ARGV[2] ne "") {
    $invId = $ARGV[0];
    $startTime = $ARGV[1];
    $endTime = $ARGV[2];
    $mode = $ARGV[3];
} else {
    die "USAGE: modifyAppointment INVITE-ID START END [MODE]";
}
    

my $url = "http://localhost:7070/service/soap/";

my $SOAP = $Soap::Soap12;
my $d = new XmlDoc;
$d->start('AuthRequest', $ACCTNS);
$d->add('account', undef, { by => "name"}, 'user1');
$d->add('password', undef, undef, "test123");
$d->end();

my $authResponse = $SOAP->invoke($url, $d->root());

print "AuthResponse = ".$authResponse->to_string("pretty")."\n";

my $authToken = $authResponse->find_child('authToken')->content;
#print "authToken($authToken)\n";

my $sessionId = $authResponse->find_child('sessionId')->content;
#print "sessionId = $sessionId\n";

my $context = $SOAP->zimbraContext($authToken, $sessionId);

my $contextStr = $context->to_string("pretty");
#print("Context = $contextStr\n");


#######################################################################
#
# ModifyAppointment
#
$d = new XmlDoc;

$d->start('ModifyAppointmentRequest', $MAILNS, { 'id' => $invId, 'comp' => '0'});

$d->start('m', undef, undef, undef);

    $d->add('e', undef,
            {
                'a' => "user2\@timbre.example.zimbra.com",
                't' => "t"
                } );
    
# $d->add('e', undef,
#         {
#             'a' => "user2\@domain.com",
#             't' => "t"
#             } );

# if ($mode < 2) {
#     $d->add('e', undef,
#             {
#                 'a' => "user3\@domain.com",
#                 't' => "t"
#                 } );


# $d->add('e', undef,
#         {
#             'a' => "tim\@example.zimbra.com",
#             't' => "t"
#             } );
# } 
$d->add('su', undef, undef, "MODIFIED: TEST MEETING2 (mode=$mode)");

    
$d->start('mp', undef, { 'ct' => "text/plain" });
$d->add('content', undef, undef, "This meeting has been changed...(mode=$mode)");
$d->end(); #mp

$d->start('inv', undef, { 'type' => "event",
                          'transp' => "O",
                          'allday' => "false",
                          'name' => "MODIFIED test name",
                          'loc' => "MODIFIED test location"
                          });


$d->add('s', undef, { 'd', => $startTime, });
$d->add('e', undef, { 'd', => $endTime, });


    $d->add('or', undef, { 'd' => "user1", 'a' => "user1\@example.zimbra.com" } );

    $d->add('at', undef, { 'd' => "user2",
                           'a' => "user2\@example.zimbra.com",
                           'role' => "REQ",
                           'ptst' => "NE",
                       });

    $d->add('at', undef, { 'd' => "user3",
                           'a' => "user3\@domain.com",
                           'role' => "REQ",
                           'ptst' => "NE",
                       });
    

$d->end(); # m
$d->end(); # ca


print "\nOUTGOING XML:\n-------------\n";
my $out =  $d->to_string("pretty")."\n";
$out =~ s/ns0\://g;
print $out."\n";

my $start = time;
my $firstStart = time;
my $response;

my $i = 0;
my $end;
my $avg;
my $elapsed;

#do {

$start = time;
#    $msgAttrs{'sortby'} = "subjasc";
$response = $SOAP->invoke($url, $d->root(), $context);
#    $end = time;
#    $elapsed = $end - $start;
#    $avg = $elapsed *1000;
#    print("Ran iter in $elapsed time ($avg ms)\n");

#    $start = time;
#    $msgAttrs{'sortby'} = "subjdesc";
#$response = $SOAP->invoke($url, $d->root(), $context);
#    $end = time;
#    $elapsed = $end - $start;
#    $avg = $elapsed *1000;
#    print("Ran iter in $elapsed time ($avg ms)\n");

#$i++;
#} while($i < 50) ;

#my $lastEnd = time;
#$avg = ($lastEnd - $firstStart) / $i * 1000;
print "\nRESPONSE:\n--------------\n";
$out =  $response->to_string("pretty")."\n";
#$out =~ s/ns0\://g;
print $out."\n";

# print("\nRan $i iters in $elapsed time (avg = $avg ms)\n");
