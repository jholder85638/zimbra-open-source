/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

#ifndef CFG_H
#define CFG_H

#include <string>
#include <map>

using namespace std;

class Config {
public:
    Config() {};
    ~Config() {};

    bool Load(string &cfgfile);
    string &Get(const char *key);
    string &Get(string &key) { return Get(key.c_str()); }

protected:
    typedef map<string, string> CfgMap;

    CfgMap cfg;
    CfgMap vars;

    void Expand(string &val);

#ifdef _DEBUG
public:
    void Dump(ostream &s);
#endif

};

#endif