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

#include "cfg.h"
#include <fstream>
#include <iostream>

bool Config::Load(string &cfgfile) {
    ifstream infile(cfgfile.c_str());
    if (infile.is_open()) {
        string line, key, val;
        size_t pos;
        while (!infile.eof()) {
            getline(infile, line);
            if (line.empty()  || line[0] == '#' || 
                (pos = line.find_first_of('=')) < 1 || pos == (line.length() - 1))
                continue;

            key = line.substr(0, pos);
            val = line.substr(pos + 1);
            if (key.length() > 3 && key[0] == '$' && key[1] == '{') {
                vars[key] = val;
            } else {
                Expand(val);
                cfg[key] = val;
            }
        }
        infile.close();
        return true;
    } else {
        return false;
    }
}

string &Config::Get(const char *key) {
    static string empty_str("");
    CfgMap::iterator it = cfg.find(string(key));
    return it == cfg.end() ? empty_str : it->second;
}

void Config::Expand(string &val) {
    size_t spos, epos;
    size_t offset = 0;
    while ((spos = val.find("${", offset)) != string::npos &&
        (epos = val.find("}", spos + 2)) != string::npos) {
        size_t varlen = epos - spos + 1;
        CfgMap::iterator it = vars.find(val.substr(spos, varlen));
        if (it != vars.end()) {
            val.replace(spos, varlen, it->second);
        } else {
            offset = epos + 1;
        }
    }
}

#ifdef _DEBUG
void Config::Dump(ostream &out) {
    for (CfgMap::iterator it = cfg.begin(); it != cfg.end(); it++) {
        out << it->first << "=" << it->second << endl;
    }
}
#endif
