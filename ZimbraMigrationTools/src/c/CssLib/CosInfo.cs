/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2013, 2014, 2016 Synacor, Inc.
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
namespace CssLib
{
    public class CosInfo
    {
        public string CosName
        {
            get;
            set;
        }
        public string CosID
        {
            get;
            set;
        }
        public CosInfo(string cosname, string cosid)
        {
            CosName = cosname;
            CosID = cosid;
        }
    }

    public class DomainInfo
    {
        public string DomainName
        {
            get;
            set;
        }

        public string DomainID
        {
            get;
            set;
        }

        public string zimbraDomainDefaultCOSId
        {
            get;
            set;
        }

        public DomainInfo(string domainname, string domainid, string zimbradomaindefaultcosid)
        {
            DomainName = domainname;
            DomainID = domainid;
            zimbraDomainDefaultCOSId = zimbradomaindefaultcosid;
        }
    }
}
