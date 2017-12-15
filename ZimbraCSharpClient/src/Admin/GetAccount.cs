/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2006, 2007, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
using System;
using System.Xml;
using System.Collections;

using Zimbra.Client.Util;

namespace Zimbra.Client.Admin
{

	public class GetAccountRequest : AdminServiceRequest
	{
		public enum AccountBy{ ByName, ById };

		private String account;
		private AccountBy nameOrId;
		private bool applyCos;

		public GetAccountRequest( String account, AccountBy nameOrId, bool applyCos)
		{
			this.account = account;
			this.nameOrId = nameOrId;
			this.applyCos = applyCos;
		}

		public override String Name()
		{
			return AdminService.NS_PREFIX + ":" + AdminService.GET_ACCOUNT_REQUEST;
		}


		public override System.Xml.XmlDocument ToXmlDocument()
		{
			XmlDocument doc = new XmlDocument();

			//create the AuthRequest node
			XmlElement requestNode = doc.CreateElement( AdminService.GET_ACCOUNT_REQUEST, AdminService.NAMESPACE_URI);

			if( !applyCos ) //default is 1
				requestNode.SetAttribute( AdminService.A_APPLY_COS, "0" );

			//create & config the account node
			XmlElement accountNode = doc.CreateElement( AdminService.E_ACCOUNT, AdminService.NAMESPACE_URI );
			
			if( nameOrId == AccountBy.ByName )
				accountNode.SetAttribute( AdminService.A_BY, AdminService.A_NAME );
			else if( nameOrId == AccountBy.ById )
				accountNode.SetAttribute( AdminService.A_BY, AdminService.A_ID );

			accountNode.InnerText = account;


			//add em together...
			requestNode.AppendChild( accountNode );
			doc.AppendChild( requestNode );

			return doc;
		}
	}

	public class Account
	{
		private String name;
		private String id;
		private Hashtable attrs;
		
		public Account( String name, String id, Hashtable attrs )
		{
			this.name = name;
			this.id = id;
			this.attrs = attrs;
		}

		public String Name
		{
			get{ return name; }
			set{ name = value; }
		}

		public String Id
		{
			get{ return id; }
			set{ id = value; }
		}

		public Hashtable Attributes
		{
			get{ return attrs; }
			set{ attrs = value; }
		}
	}


	public class GetAccountResponse : Response
	{
		private Account acct;
		public GetAccountResponse( Account acct)
		{
			this.acct = acct;
		}

		public GetAccountResponse(){}

		public override String Name
		{
			get{return AdminService.NS_PREFIX + ":" + AdminService.GET_ACCOUNT_RESPONSE;}
		}

		public Account Acct
		{
			get{ return acct; }
		}

		public override Response NewResponse(XmlNode responseNode)
		{
			XmlNode accountNode = responseNode.SelectSingleNode( AdminService.NS_PREFIX + ":" + AdminService.E_ACCOUNT, XmlUtil.NamespaceManager );

			String name = XmlUtil.AttributeValue( accountNode.Attributes, AdminService.A_NAME );
			String id = XmlUtil.AttributeValue( accountNode.Attributes, AdminService.A_ID );

			Hashtable h = new Hashtable();
			for( int i = 0; i < accountNode.ChildNodes.Count; i++ )
			{
				XmlNode child = accountNode.ChildNodes[i];
				String attrName = XmlUtil.AttributeValue( child.Attributes, AdminService.A_ATTR_NAME );
				String attrValue = child.InnerText;

				if( h.Contains( attrName ) )
				{
					Object o = h[attrName];
					if( o is String )
					{
                        ArrayList al = new ArrayList();
						al.Add( (String)o );
						al.Add( attrValue );
						h.Remove(attrName);
						h.Add( attrName, al );
					}
					else //its a string collection
					{
						ArrayList al = (ArrayList)o;
						al.Add(attrValue);
					}
				}
				else
				{
					h.Add( attrName, attrValue );
				}
			}

			return new GetAccountResponse( new Account( name, id, h ) );
		}

	}
}
