#!/bin/sh
# 
# ***** BEGIN LICENSE BLOCK *****
# Zimbra Collaboration Suite Server
# Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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

timeout=20

while [ $timeout -gt 0 ]; do
  pid=`ps -fe | grep java | grep 'Launcher start com.zimbra.cs.offline.start.Main' | awk '{print $2}'`
  if [ -z $pid ]; then
    exit 0
  fi
  sleep 1
  timeout=$[timeout-1]
done

kill -9 $pid
