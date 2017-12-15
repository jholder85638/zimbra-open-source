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

namespace Zimbra.Client.Admin
{
	public class AdminService : IZimbraService
	{
		public static String SERVICE_PATH			= "/service/admin/soap";

		//this services namespace uri
		public static String NS_PREFIX				= "admin";
		public static String NAMESPACE_URI			= "urn:zimbraAdmin";
		
		//requests
		public static String AUTH_REQUEST			= "AuthRequest";
		public static String GET_ACCOUNT_REQUEST	= "GetAccountRequest";

		//responses
		public static String AUTH_RESPONSE			= "AuthResponse";
		public static String GET_ACCOUNT_RESPONSE	= "GetAccountResponse";

		//element names
		public static String E_NAME					= "name";
		public static String E_PASSWORD				= "password";
		public static String E_LIFETIME				= "lifetime";
		public static String E_SESSIONID			= "sessionId";
		public static String E_AUTHTOKEN			= "authToken";
		public static String E_ACCOUNT				= "account";

		//attribute names
		public static String A_NAME					= "name";
		public static String A_BY					= "by";
		public static String A_ID					= "id";
		public static String A_APPLY_COS			= "applyCos";
		public static String A_ATTR_NAME			= "n";

		//qualified names
		public static String Q_AUTHTOKEN			= NS_PREFIX + ":" + E_AUTHTOKEN;
		public static String Q_LIFETIME				= NS_PREFIX + ":" + E_LIFETIME;
		public static String Q_SESSIONID			= NS_PREFIX + ":" + E_SESSIONID;


		public static Response[] responses = { 
			new AuthResponse(),
			new GetAccountResponse()
		};

		//IZimbraService
		public String NamespacePrefix{ get{return NS_PREFIX; }}
		public String NamepsaceUri{ get{return NAMESPACE_URI; }}
		public Response[] Responses{ get{return responses; }}

	}


	public abstract class AdminServiceRequest : Request
	{
		public override String ServicePath{ get{ return AdminService.SERVICE_PATH;} }
		public override String HttpMethod{ get { return "POST"; } }
	}




}
