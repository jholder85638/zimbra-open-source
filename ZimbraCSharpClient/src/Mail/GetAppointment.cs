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

namespace Zimbra.Client.Mail
{
	public class GetAppointmentRequest : MailServiceRequest
	{
		private String id;

		public GetAppointmentRequest(String id)
		{
			this.id = id;
		}

		public override String Name()
		{
			return MailService.NS_PREFIX + ":" + MailService.GET_APPT_REQUEST;
		}

		public override XmlDocument ToXmlDocument()
		{
			XmlDocument doc = new XmlDocument();
			XmlElement reqElem =doc.CreateElement( MailService.GET_APPT_REQUEST, MailService.NAMESPACE_URI );
			reqElem.SetAttribute(  MailService.A_ID, id );
			doc.AppendChild( reqElem );
			return doc;
		}
	}



	public class GetAppointmentResponse : Response
	{
		public GetAppointmentResponse()
		{}

		public override String Name
		{
			get { return MailService.NS_PREFIX + ":" + MailService.GET_APPT_RESPONSE; }
		}

		public override Response NewResponse(XmlNode responseNode)
		{
			return null;
		}


	}
}
