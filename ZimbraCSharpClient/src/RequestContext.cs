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

namespace Zimbra.Client
{
	public class RequestContext
	{
		private String				authToken				= null;
		private String				sessionId				= null;
		private AccountFormat		targetAccountFormat		= AccountFormat.ByName;
		private String				targetAccount			= null;
		private Boolean				noNotifications			= false;
		private Boolean				noSession				= false;
		private String				targetServer			= null;
		private String				changeToken				= null;
		private RaceConditionType	raceType				= RaceConditionType.Modify;
		private String				notificationSequence	= null;

		public RequestContext()
		{}

		public String AuthToken
		{
			get{ return authToken; }
			set{ authToken = value; }
		}

		public String SessionId
		{
			get{ return sessionId; }
			set{ sessionId = value; }
		}

		public AccountFormat TargetAccountType
		{
			get{ return targetAccountFormat; }
			set{ targetAccountFormat = value; }
		}

		public String TargetAccount
		{
			get{ return targetAccount; }
			set{ targetAccount = value; }
		}

		public Boolean NoNotifications
		{
			get{ return noNotifications; }
			set{ noNotifications = value; }
		}

		public String NotificationSequence
		{
			get{ return notificationSequence;}
			set{ notificationSequence = value; }
		}

		public Boolean NoSession
		{
			get{ return noSession; }
			set{ noSession = value; }
		}

		public String TargetServer
		{
			get{ return targetServer; }
			set{ targetServer = value; }
		}

		public String ChangeToken
		{
			get{ return changeToken; }
			set{ changeToken = value; }
		}

		public RaceConditionType RaceType
		{
			get{ return raceType; }
			set{ raceType = value; }
		}

		//update this request context based on the response context
		public void Update( ResponseContext rc )
		{
			if( rc.SessionId != null )
				this.sessionId = rc.SessionId;

			if( rc.Notifications != null && rc.Notifications.SequenceToken != null )
				this.NotificationSequence = rc.Notifications.SequenceToken;
		}

		//update this request context based on the response context and auth response
		public void Update( ResponseContext rc, Zimbra.Client.Account.AuthResponse ar )
		{
			this.AuthToken = ar.AuthToken;
			Update( rc );
		}

		public void Update( ResponseContext rc, Zimbra.Client.Admin.AuthResponse ar )
		{
			this.AuthToken = ar.AuthToken;
			Update(rc);
		}


		public XmlDocument ToXmlDocument()
		{
			XmlDocument d = new XmlDocument();

			XmlElement contextElement = d.CreateElement( ZimbraService.E_CONTEXT, ZimbraService.NAMESPACE_URI );

			if( authToken != null )
			{
				XmlElement ate = d.CreateElement( ZimbraService.E_AUTHTOKEN, ZimbraService.NAMESPACE_URI );
				ate.InnerText = authToken;
				contextElement.AppendChild( ate );
			}

			if( sessionId != null )
			{
				XmlElement se = d.CreateElement( ZimbraService.E_SESSION, ZimbraService.NAMESPACE_URI );
				se.SetAttribute( ZimbraService.A_ID, sessionId );
				contextElement.AppendChild( se ); 
			} else if( noSession == false)
			{
				contextElement.AppendChild( d.CreateElement( ZimbraService.E_SESSION, ZimbraService.NAMESPACE_URI ) );
			} 

			if( noNotifications )
			{
				contextElement.AppendChild( d.CreateElement( ZimbraService.E_NONOTIFY, ZimbraService.NAMESPACE_URI ) );
			}

			if( notificationSequence != null ) 
			{
				XmlElement e = d.CreateElement( ZimbraService.E_NOTIFY, ZimbraService.NAMESPACE_URI );
				e.SetAttribute( ZimbraService.A_NOTIFY_SEQUENCE, notificationSequence );
				contextElement.AppendChild( e );
			}

			if( targetAccount != null )
			{
				XmlElement tae = d.CreateElement( ZimbraService.E_ACCOUNT, ZimbraService.NAMESPACE_URI );
				tae.SetAttribute( ZimbraService.A_BY, targetAccountFormat.ToString() );
				tae.InnerText = targetAccount;
				contextElement.AppendChild( tae );
			}

			if( targetServer != null )
			{
				XmlElement tse = d.CreateElement( ZimbraService.E_TARGET_SERVER, ZimbraService.NAMESPACE_URI );
				tse.InnerText = targetServer;
				contextElement.AppendChild( tse );
			}

			if( changeToken != null )
			{
				XmlElement cte = d.CreateElement( ZimbraService.E_CHANGE, ZimbraService.NAMESPACE_URI );
				cte.SetAttribute( ZimbraService.A_TOKEN, changeToken );
				cte.SetAttribute( ZimbraService.A_TYPE, raceType.ToString() );
			}

			if( contextElement.ChildNodes.Count > 0 )
				d.AppendChild( contextElement );
			return d;
		}
	}
}













