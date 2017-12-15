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

using Zimbra.Client.Soap;

namespace Zimbra.Client
{

	public class RequestEnvelope
	{
		private RequestContext		context;
		private Request				apiRequest;

		public RequestEnvelope( RequestContext rc, Request ar )
		{
			context = rc;
			apiRequest = ar;
		}

		public RequestContext Context
		{
			get{ return context; }
			set{ context = value; }
		}

		public Request ApiRequest
		{
			get{ return apiRequest; }
			set{ apiRequest = value; }
		}

		public XmlDocument ToXmlDocument()
		{
			XmlDocument contextDoc = context.ToXmlDocument();
			XmlDocument apiDoc = apiRequest.ToXmlDocument();

			//wrap it in soap....
			XmlDocument soapDoc = new XmlDocument();
			XmlElement envelope = soapDoc.CreateElement( SoapService.E_ENVELOPE, SoapService.NAMESPACE_URI );
			XmlElement header   = soapDoc.CreateElement( SoapService.E_HEADER, SoapService.NAMESPACE_URI );
			XmlElement body     = soapDoc.CreateElement( SoapService.E_BODY, SoapService.NAMESPACE_URI );

			if( contextDoc.ChildNodes.Count > 0 )
				header.AppendChild( soapDoc.ImportNode( contextDoc.FirstChild, true ) );
			body.AppendChild( soapDoc.ImportNode( apiDoc.FirstChild, true ) );

			if( header.ChildNodes.Count > 0 )
				envelope.AppendChild( header );
			envelope.AppendChild( body );
			soapDoc.AppendChild( envelope );
			return soapDoc;
		}
	}



	public enum AccountFormat { ByName, ById };
	public enum RaceConditionType { Modify, New };


	public abstract class Request
	{
		public abstract String Name();
		public abstract XmlDocument ToXmlDocument();
		public abstract String ServicePath{ get; }
		public abstract String HttpMethod{ get; }
	}
}
