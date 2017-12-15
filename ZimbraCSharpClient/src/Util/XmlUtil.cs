/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2006, 2007, 2009, 2010, 2013, 2014 Zimbra, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
using System;
using System.Xml;


namespace Zimbra.Client.Util
{
	public class XmlUtil
	{
		public static XmlNamespaceManager NamespaceManager;

		static XmlUtil()
		{
			NamespaceManager = new XmlNamespaceManager( new NameTable() );
		}


		public static String GetNodeText( XmlDocument d, String xPath )
		{
			XmlNode n = d.SelectSingleNode( xPath, NamespaceManager );
			if( n != null ) 
				return n.InnerText;
			return null;
		}

		public static String GetNodeText( XmlNode d, String xPath )
		{
			XmlNode n = d.SelectSingleNode( xPath, NamespaceManager );
			if( n != null ) 
				return n.InnerText;
			return null;
		}

		public static String AttributeValue( XmlAttributeCollection attrs, String name )
		{
			XmlNode n = attrs[name];
			if( n != null )
				return n.Value;
			return null;
		}

		public static String GetAttributeValue( XmlNode n, String nodeSelector, String attrName )
		{
			XmlNode s = n.SelectSingleNode( nodeSelector, NamespaceManager );
			if( s == null )
				return null;
			return AttributeValue( s.Attributes, attrName );
		}

	}
}
