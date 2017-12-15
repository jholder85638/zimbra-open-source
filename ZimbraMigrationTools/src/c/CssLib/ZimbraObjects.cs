/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2014, 2015, 2016 Synacor, Inc.
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
using System.IO;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using System.Xml;

namespace CssLib
{


public class ZimbraMessage
{
    public string MimeFile;
    public string folderId;
    public string flags;
    public string tags;
    public string rcvdDate;
    public string DateUnixString;

    public ZimbraMessage()
    {
        folderId = "";
        flags = "";
        tags = "";
        rcvdDate = "";
        DateUnixString = "";
    }

    public ZimbraMessage(string FilePath, string FolderId, string Flags, string Tags, string RcvdDate, string strdate)
    {
        MimeFile = FilePath;
        folderId = FolderId;
        flags = Flags;
        tags = Tags;
        rcvdDate = RcvdDate;
        DateUnixString = strdate;
    }
}

public class ZimbraFolder
{
    public string name;
    public string parent;
    public string view;
    public string color;
    public string flags;

    public ZimbraFolder()
    {
        name = "";
        parent = "";
        view = "";
        color = "";
        flags = "";
    }

    public ZimbraFolder(string Name, string Parent, string View, string Color, string Flags)
    {
        name = Name;
        parent = Parent;
        view = View;
        color = Color;
        flags = Flags;
    }
}
}
