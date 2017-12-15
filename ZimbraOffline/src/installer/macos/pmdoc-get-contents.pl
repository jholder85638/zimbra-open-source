#!/usr/bin/perl

#/*
# * ***** BEGIN LICENSE BLOCK *****
# * Zimbra Collaboration Suite Server
# * Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
# *
# * This program is free software: you can redistribute it and/or modify it under
# * the terms of the GNU General Public License as published by the Free Software Foundation,
# * version 2 of the License.
# *
# * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# * See the GNU General Public License for more details.
# * You should have received a copy of the GNU General Public License along with this program.
# * If not, see <https://www.gnu.org/licenses/>.
# * ***** END LICENSE BLOCK *****
# */
#
# A build tool that creates content files for PackageMaker project (pmdoc)
#
# Usage: pmdoc-get-contents.pl <content root> <output file> [<owner>] [<group>]
#
 
use strict;
use warnings;

my ($content_root, $output_file, $owner, $group) = @ARGV;
my $o = $owner ? " o=\"$owner\"" : "";
my $g = $group ? " g=\"$group\"" : "";

sub traverse {
    my ($dir, $indent, $xml) = @_;
    my ($dh, $path, $n, $p);
    
    if (!opendir($dh, $dir)){
        print "Warning: cannot open directory $dir\n";
        return;
    }
    
    while($n = readdir($dh)) {
        next if ($n eq '.' || $n eq '..');
        
        $path = "$dir/$n";
        (undef, undef, $p) = stat($path);
        $$xml .= "$indent<f n=\"$n\"$o$g p=\"$p\"";
        if (-d $path) {
            $$xml .= ">\n";
            traverse($path, "$indent  ", $xml);
            $$xml .= "$indent</f>\n";
        } else {
            $$xml .= "/>\n";
        }
    }
    
    close($dh);
}

open (FOUT, ">$output_file") || die("cannot open $output_file");

my $pos = rindex($content_root, "/");
my $n = $pos >= 0 ? substr($content_root, $pos + 1) : $content_root;
my (undef, undef, $p) = stat($content_root);

my $xml = "<pkg-contents spec=\"1.12\"><f n=\"$n\"$o$g p=\"$p\" pt=\"$content_root\" m=\"false\" t=\"file\">\n";
traverse($content_root, "", \$xml);
$xml .= "</pkg-contents>\n";

print FOUT $xml;
close(FOUT);

