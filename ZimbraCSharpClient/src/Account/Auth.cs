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

namespace Zimbra.Client.Account
{
	



	public class AuthRequest : AccountServiceRequest
	{
		private String accountName;
		private String password;

		public AuthRequest(String account, String password)
		{
			accountName = account;
			this.password = password;
		}

		public override XmlDocument ToXmlDocument()
		{
			XmlDocument doc = new XmlDocument();

			//create the AuthRequest node
			XmlElement requestNode = doc.CreateElement( AccountService.AUTH_REQUEST, AccountService.NAMESPACE_URI);

			//create & config the account node
			XmlElement accountNode = doc.CreateElement( AccountService.E_ACCOUNT, AccountService.NAMESPACE_URI );
			accountNode.SetAttribute( AccountService.A_BY, AccountService.A_NAME );
			accountNode.InnerText = accountName;

			//create and config the password node
			XmlElement pwdNode = doc.CreateElement( AccountService.E_PASSWORD, AccountService.NAMESPACE_URI );
			pwdNode.InnerText = password;

			//add em together...
			requestNode.AppendChild( accountNode );
			requestNode.AppendChild( pwdNode );
			doc.AppendChild( requestNode );

			return doc;
		}

		public override String Name()
		{
			return AccountService.NS_PREFIX + ":" + AccountService.AUTH_REQUEST;
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
			sessionId = s;
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
			get{ return AccountService.NS_PREFIX + ":" + AccountService.AUTH_RESPONSE;}
		}


		public override Response NewResponse(XmlNode responseNode)
		{
			String authToken = XmlUtil.GetNodeText( responseNode, AccountService.Q_AUTHTOKEN );
			String lifetime  = XmlUtil.GetNodeText( responseNode, AccountService.Q_LIFETIME );
			String sessionId = XmlUtil.GetNodeText( responseNode, AccountService.Q_SESSION);

			return new AuthResponse( authToken, lifetime, sessionId );
		}

		
	}
}
