/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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
using System.Collections.Generic;

namespace CssLib
{


public class ZimbraValues
{
    public static ZimbraValues zimbraValues;

    public ZimbraValues()
    {
        zimbraValues = null;
        sUrl = "";
        sAuthToken = "";
        sServerVersion = "";
        sDefaultCosIndex = -1;
        lDomains = new List<string>();
        lZimbraDomains = new List<DomainInfo>();
        lCOSes = new List<CosInfo>();
    }

    public static ZimbraValues GetZimbraValues()
    {
        if (zimbraValues == null)
            zimbraValues = new ZimbraValues();
        return zimbraValues;
    }

    private string sUrl;
    public string Url 
    {
        get { return sUrl; }
        set
        {
            sUrl = value;
        }
    }

    private string sAuthToken;
    public string AuthToken 
    {
        get { return sAuthToken; }
        set
        {
            sAuthToken = value;
        }
    }

    private string sClientVersion;
    public string ClientVersion
    {
        get { return sClientVersion; }
        set
        {
            sClientVersion = value;
        }
    }

    private string sServerVersion;
    public string ServerVersion 
    {
        get { return sServerVersion; }
        set
        {
            sServerVersion = value;
        }
    }

    private string sHostName;
    public string HostName 
    {
        get { return sHostName; }
        set
        {
            sHostName = value;
        }
    }

    private string sPort;
    public string Port 
    {
        get { return sPort; }
        set
        {
            sPort = value;
        }
    }

    private List<string> lDomains;
    public List<string> Domains 
    {
        get { return lDomains; }
        set
        {
            lDomains = value;
        }
    }

    private List<DomainInfo> lZimbraDomains;
    public List<DomainInfo> ZimbraDomains
    {
        get { return lZimbraDomains; }
        set
        {
            lZimbraDomains = value;
        }
    }

    private int sDefaultCosIndex;
    public int DefaultCosIndex 
    {
        get { return sDefaultCosIndex; }
        set
        {
            sDefaultCosIndex = value;
        }
    }

    private List<CosInfo> lCOSes;
    public List<CosInfo> COSes 
    {
        get { return lCOSes; }
        set
        {
            lCOSes = value;
        }
    }

    private string sAccountName;
    public string AccountName
    {
        get { return sAccountName; }
        set
        {
            sAccountName = value;
        }
    }

    private string zmuser;
    public string zmUser
    {
        get { return zmuser; }
        set
        {
            zmuser = value;
        }
    }

    private string zmuserpwd;
    public string zmUserPwd
    {
        get { return zmuserpwd; }
        set
        {
            zmuserpwd = value;
        }
    }

    private bool zmisssl;
    public bool zmIsSSL
    {
        get { return zmisssl; }
        set
        {
            zmisssl = value;
        }
    }

    private bool zmisadmin;
    public bool zmIsAdmin
    {
        get { return zmisadmin; }
        set
        {
            zmisadmin = value;
        }
    }

    private bool zmsessionexpired;
    public bool zmSessionExpired
    {
        get { return zmsessionexpired; }
        set
        {
            zmsessionexpired = value;
        }
    }

}
}
