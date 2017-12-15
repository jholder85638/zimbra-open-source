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
using Zimbra.Client.Util;

namespace Zimbra.Client
{
	public class ResponseContext
	{
		private String			sessionId;
		private String			changeToken;
		private RefreshBlock	refreshBlock;
		private Notification	notification;

		public ResponseContext( XmlNode contextNode )
		{
			if( contextNode == null )
				return;
			sessionId = XmlUtil.GetNodeText( contextNode, ZimbraService.NS_PREFIX + ":" + ZimbraService.E_SESSION );
			changeToken = XmlUtil.GetAttributeValue( contextNode, ZimbraService.E_CHANGE, ZimbraService.A_TOKEN );

			XmlNode notifyNode = contextNode.SelectSingleNode( ZimbraService.NS_PREFIX + ":" + ZimbraService.E_NOTIFY, XmlUtil.NamespaceManager );
			if( notifyNode != null ) 
			{
				notification = new Notification(notifyNode);
			}
		}

		public ResponseContext( String sessionId, String changeToken, RefreshBlock rb, Notification n )
		{
			this.sessionId = sessionId;
			this.changeToken = changeToken;
			this.refreshBlock = rb;
			this.notification = n;
		}

		public String SessionId
		{
			get{ return sessionId; }
			set{ sessionId = value; }
		}

		public String ChangeToken
		{
			get{ return changeToken; }
			set{ changeToken = value; }
		}

		public RefreshBlock Refresh
		{
			get{ return refreshBlock; }
			set{ refreshBlock = value; }
		}

		public Notification Notifications
		{
			get{ return notification; }
			set{ notification = value; }
		}

	}

}

