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
using Zimbra.Client.Soap;


namespace Zimbra.Client.Soap
{

	public class SoapFault : Response 
	{
		private String code;
		private String description;

		public SoapFault()
		{
		}

		public SoapFault( String c, String d )
		{
			code = c;
			description = d;
		}

		public String Code
		{
			get{ return code; }
		}

		public String Description
		{
			get{ return description; }
		}

		public override String Name
		{
			get{ return SoapService.NS_PREFIX + ":Fault";}
		}

		public override Response NewResponse(XmlNode responseNode)
		{
			String code  = XmlUtil.GetNodeText( responseNode, 
						SoapService.NS_PREFIX	+ ":" + SoapService.E_DETAIL + 
						"/" +	ZimbraService.NS_PREFIX + ":" + SoapService.E_ERROR  + 
						"/" +	ZimbraService.NS_PREFIX + ":" + ZimbraService.E_CODE );

			String descr = XmlUtil.GetNodeText( responseNode, 
				SoapService.NS_PREFIX + ":" + SoapService.E_REASON + 
						"/" + SoapService.NS_PREFIX + ":" + SoapService.E_TEXT );
			
			return new SoapFault(code, descr);
		}


	}
}


namespace Zimbra.Client
{
	public class ZimbraException : Exception
	{
		private RequestEnvelope req;
		private ResponseEnvelope fault;

		public ZimbraException( ResponseEnvelope sf, RequestEnvelope req )
		{
			fault = sf;
			this.req = req;
		}

		public ZimbraException( String msg, ResponseEnvelope sf, RequestEnvelope req ) : base(msg)
		{
			fault = sf;
			this.req = req;
		}

		public SoapFault Fault
		{
			get{ return (SoapFault)fault.ApiResponse; }
		}

		public ResponseEnvelope Response
		{
			get{ return fault; }
		}

		public RequestEnvelope Request
		{
			get{ return req; }
		}

	}

}
