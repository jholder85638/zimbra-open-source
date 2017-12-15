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

namespace Zimbra.Client.Soap
{
	public class SoapService : IZimbraService
	{
		//this services namespace uri
		public static String NS_PREFIX		= "soap";
		public static String NAMESPACE_URI	= "http://www.w3.org/2003/05/soap-envelope";

		public static String E_ENVELOPE		= "Envelope";
		public static String E_HEADER		= "Header";
		public static String E_BODY			= "Body";
		public static String E_DETAIL		= "Detail";
		public static String E_REASON		= "Reason";
		public static String E_ERROR		= "Error";
		public static String E_TEXT			= "Text";

		public static Response[] responses = { new SoapFault() };
		public String NamespacePrefix{ get{ return NS_PREFIX; }}
		public String NamepsaceUri{ get{ return NAMESPACE_URI; }}
		public Response[] Responses{get{ return responses;}}


	}
}
