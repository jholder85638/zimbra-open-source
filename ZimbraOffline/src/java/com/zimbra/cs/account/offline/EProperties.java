/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.account.offline;

import java.util.Properties;

class EProperties extends Properties {
    private static final long serialVersionUID = -8135956477865965194L;

    @Override
    public String getProperty(String key, String defaultValue) {
        String val = super.getProperty(key, defaultValue);
        return val == null ? null : val.trim();
    }

    @Override
    public String getProperty(String key) {
        String val = super.getProperty(key);
        return val == null ? null : val.trim();
    }

    public int getPropertyAsInteger(String key, int defaultValue) {
        String val = getProperty(key);
        if (val == null || val.length() == 0)
            return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException x) {
            return defaultValue;
        }
    }

    public boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        String val = getProperty(key);
        if (val == null || val.length() == 0)
            return defaultValue;
        return Boolean.parseBoolean(val);
    }

    public String getNumberedProperty(String prefix, int number, String suffix) {
        return getProperty(prefix + '.' + number + '.' + suffix);
    }

    public String getNumberedProperty(String prefix, int number, String suffix,
                                      String defaultValue) {
        return getProperty(prefix + '.' + number + '.' + suffix, defaultValue);
    }

    public int getNumberedPropertyAsInteger(String prefix, int number, String suffix, int defaultValue) {
        String val = getNumberedProperty(prefix, number, suffix);
        if (val == null || val.length() == 0)
            return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException x) {
            return defaultValue;
        }
    }

    public String getNumberedProperty(String prefix, int n1, String midfix, int n2) {
        return getProperty(prefix + '.' + n1 + '.' + midfix + '.' + n2);
    }
    
    public String getNumberedProperty(String prefix, int n1, String midfix, int n2, String suffix) {
        return getProperty(prefix + '.' + n1 + '.' + midfix + '.' + n2 + '.' + suffix);
    }

    public boolean getNumberedPropertyAsBoolean(String prefix, int n1, String midfix, int n2, String suffix, boolean defaultValue) {
        String val = getProperty(prefix + '.' + n1 + '.' + midfix + '.' + n2 + '.' + suffix);
        if (val == null || val.length() == 0)
            return defaultValue;
        return Boolean.parseBoolean(val);
    }
}
