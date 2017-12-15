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
using System.Net;
using System.Security.Cryptography.X509Certificates;

namespace Zimbra.Client.Util
{

	class AcceptAllCertsPolicy : ICertificatePolicy
	{
		public bool CheckValidationResult(ServicePoint p, X509Certificate c, WebRequest w, int certProb)
		{
			return true;
		}
	}


	public class ConnectionUtil
	{
		public static void AllowInvalidCerts()
		{
			ServicePointManager.CertificatePolicy = new AcceptAllCertsPolicy();
		}
	}

	public class DateUtil
	{
		public static Int64 DateTimeToGmtMillis( DateTime localDateTime )
		{
			DateTime t = new DateTime( 1970, 1, 1 );
			DateTime utc = localDateTime.ToUniversalTime();

			//100 nanoseconds
			Int64 deltaTicks = utc.Ticks - t.Ticks;
			Int64 deltaMillis = (Int64)(deltaTicks / 10000);

			return deltaMillis;
		}

		public static DateTime GmtSecondsToLocalTime( Int64 s )
		{
			DateTime t = new DateTime( 1970, 1, 1 );
			
			Int64 ticks = s * 10000;
			return new DateTime( t.Ticks + ticks ).ToLocalTime();
		}
	}
}
