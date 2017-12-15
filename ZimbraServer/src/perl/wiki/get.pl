#!/usr/bin/perl
# 
# ***** BEGIN LICENSE BLOCK *****
# Zimbra Collaboration Suite Server
# Copyright (C) 2006, 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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

use threads;
use LWP::UserAgent;
use HTTP::Request;
use Time::HiRes qw(gettimeofday);

my @buckets = (0,0,0,0,0,0,0,0,0,0);
my $num_threads = 0;
my $iteration = 0;
my $username = 'wikitest';
my $password = 'test123';

sub sendRequest($) {
    my ($url) = @_;
    my $ua, $req, $resp;

    $req = new HTTP::Request ('GET', "http://localhost:7070/home/$username/" . $url);

    $ua = new LWP::UserAgent;
    $ua->credentials('localhost:7070', 'Zimbra', $username, $password);

    $resp = $ua->request($req);
    return $resp;
}

sub runTests() {
    my $s0, $usec0, $s1, $usec1, $url, $resp, $nerror = 0, $min = 999999999, $max = 0, $sum = 0;

    for (my $i = 0; $i < $iteration; $i++) {
	$url = int(rand(10)) . "/" . int(rand(10)) . "/" . int(rand(10));
	($s0, $usec0) = gettimeofday();
	$resp = sendRequest($url);
	($s1, $usec1) = gettimeofday();

	if ($resp->code() != 200) {
		$nerror++;
	}

	my $interval;

	$interval = $usec1 - $usec0;
	if ($interval < 0) {
	    $interval = $interval + 1000000;
	}

	$sum += $interval;
	if ($interval < $min) {
		$min = $interval;
	}
	if ($interval > $max) {
		$max = $interval;
	}
	
	$interval = int ($interval / 20000);
	if ($interval > 9) {
	    $interval = 9;
	}

	{
	    lock($buckets);
#	    print "BEF: " . $buckets[$interval] ;
	    $buckets[$interval]++;
#	    printf "  CUR: $interval";
#	    print "  AFT: " . $buckets[$interval] . "\n";
        }
    }

    tabulate($nerror, $min / 1000, $max / 1000, ($sum / $iteration) / 1000, $buckets);
}

sub tabulate($$$$$) {
    my ($nerror, $min, $max, $avg, $buckets) = @_;
    printf "\nerror  min  max  avg | Latency   20   40   60   80  100  120  140  160  180  200\n";
    printf "\n--------------------------------------------------------------------------------\n";
	printf(" %4d %4d %4d %4d |        ", $nerror, $min, $max, $avg);
    foreach (@buckets) {
	printf(" %4d", $_);
    }
    printf "\n--------------------------------------------------------------------------------\n";
}

sub mergeArrays($$) {
    my (@arr1, @arr2) = @_;
    for (my $i = 0; $i < $#arr1; $i++) {
	$arr1[$i] += $arr2[$i];
    }
}

if (defined($ARGV[0])) {
    $num_threads = $ARGV[0];
}
if (defined($ARGV[1])) {
    $iteration = $ARGV[1];
}

my @th;

for (my $i = 0; $i < $num_threads; $i++) {
    $th[$i] = threads->create('runTests');
}
for (my $i = 0; $i < $num_threads; $i++) {
    $th[$i]->join();
}

#tabulate($buckets);
