/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.ab.gab;

import com.google.gdata.client.GoogleService.AccountDeletedException;
import com.google.gdata.client.GoogleService.AccountDisabledException;
import com.google.gdata.client.GoogleService.CaptchaRequiredException;
import com.google.gdata.client.GoogleService.InvalidCredentialsException;
import com.google.gdata.client.GoogleService.NotVerifiedException;
import com.google.gdata.client.GoogleService.ServiceUnavailableException;
import com.google.gdata.client.GoogleService.SessionExpiredException;
import com.google.gdata.client.GoogleService.TermsNotAgreedException;
import com.zimbra.common.service.ServiceException;

@SuppressWarnings("serial")
public class GDataServiceException extends ServiceException {
	
	public static final String ACCOUNT_DELETED     = "gdata.ACCOUNT_DELETED";
	public static final String ACCOUNT_DISABLED    = "gdata.ACCOUNT_DISABLED";
	public static final String CAPTCHA_REQUIRED    = "gdata.CAPTCHA_REQUIRED";
	public static final String INVALID_CREDENTIALS = "gdata.INVALID_CREDENTIALS";
	public static final String NOT_VERIFIED        = "gdata.NOT_VERIFIED";
	public static final String SERVICE_UNAVAILABLE = "gdata.SERVICE_UNAVAILABLE";
	public static final String SESSION_EXPIRED     = "gdata.SESSION_EXPIRED";
	public static final String TERMS_NOT_AGREED    = "gdata.TERMS_NOT_AGREED";
	
    private GDataServiceException(String message, String code, boolean isReceiversFault) {
        super(message, code, isReceiversFault);
    }
    
    private GDataServiceException(String message, String code, boolean isReceiversFault, Throwable cause) {
        super(message, code, isReceiversFault, cause);
    }

    public static String getErrorCode(com.google.gdata.util.ServiceException x) {
    	if (x instanceof AccountDeletedException)
    		return ACCOUNT_DELETED;
    	if (x instanceof AccountDisabledException)
    		return ACCOUNT_DISABLED;
    	if (x instanceof CaptchaRequiredException)
    		return CAPTCHA_REQUIRED;
    	if (x instanceof InvalidCredentialsException)
    		return INVALID_CREDENTIALS;
    	if (x instanceof NotVerifiedException)
    		return NOT_VERIFIED;
    	if (x instanceof ServiceUnavailableException)
    		return SERVICE_UNAVAILABLE;
    	if (x instanceof SessionExpiredException)
    		return SESSION_EXPIRED;
    	if (x instanceof TermsNotAgreedException)
    		return TERMS_NOT_AGREED;
    	return null;
    }
    
    public static void doFailures(com.google.gdata.util.ServiceException x) throws GDataServiceException {
    	String code = getErrorCode(x);
    	if (code != null)
    		throw new GDataServiceException(x.getMessage(), code, false, x);
    }
}
