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

namespace Zimbra.Client.Admin
{
	/// <summary>
	/// Summary description for Auth.
	/// </summary>
	public class AuthRequest : AdminServiceRequest
	{
		private String name;
		private String password;

		public AuthRequest(String name, String password)
		{
			this.name = name;
			this.password = password;
		}

		public override XmlDocument ToXmlDocument()
		{
			XmlDocument doc = new XmlDocument();

			//create the AuthRequest node
			XmlElement requestNode = doc.CreateElement( AdminService.AUTH_REQUEST, AdminService.NAMESPACE_URI);

			//create & config the name node
			XmlElement accountNode = doc.CreateElement( AdminService.E_NAME, AdminService.NAMESPACE_URI );
			accountNode.InnerText = name;

			//create and config the password node
			XmlElement pwdNode = doc.CreateElement( AdminService.E_PASSWORD, AdminService.NAMESPACE_URI );
			pwdNode.InnerText = password;

			//add em together...
			requestNode.AppendChild( accountNode );
			requestNode.AppendChild( pwdNode );
			doc.AppendChild( requestNode );

			return doc;
		}

		public override String Name()
		{
			return AdminService.NS_PREFIX + ":" + AdminService.AUTH_REQUEST;
		}
	}



	
	
	public class AuthResponse : Response
	{
		private String authToken;
		private String lifetime;
		private String sessionId;

		public AuthResponse(){}

		public AuthResponse( String a, String l, String s )
		{
			authToken = a;
			lifetime = l;
			sessionId =s;
		}

		public String AuthToken
		{
			get{ return authToken; }
		}

		public String LifeTime
		{
			get{ return lifetime; }
		}

		public String SessionId
		{
			get{ return sessionId; }
		}

		public override String Name
		{
			get{ return AdminService.NS_PREFIX + ":" + AdminService.AUTH_RESPONSE;}
		}


		public override Response NewResponse(XmlNode responseNode)
		{
			String authToken = XmlUtil.GetNodeText( responseNode, AdminService.Q_AUTHTOKEN );
			String lifetime  = XmlUtil.GetNodeText( responseNode, AdminService.Q_LIFETIME );
			String sessionId = XmlUtil.GetNodeText( responseNode, AdminService.Q_SESSIONID );

			return new AuthResponse( authToken, lifetime, sessionId );
		}
	}
}
