/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2012, 2014, 2016 Synacor, Inc.
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
#pragma once

#define ERR_OPEN_ENTRYID            L"Item cannot be opened"
#define ERR_OPEN_PROPERTY           L"MAPI property cannot be opened"
#define ERR_SET_RESTRICTION         L"MAPI Table restriction failed"
#define ERR_GET_NEXT                L"Get next item failed"
#define ERR_DEF_MSG_STORE           L"Default message store cannot be opened"
#define ERR_MBOX_LOGON              L"Mailbox logon failed"
#define ERR_ADOBJECT_OPEN           L"Directory Object cannot be opened. Please verify the used admin credentials"
#define ERR_AD_SEARCH               L"Directory search failed"
#define ERR_AD_NOROWS               L"Please check the admin credentials for permissions/rights to query the directory"
#define ERR_PROFILE_DN              L"Failed to get data from Admin profile"
#define ERR_IPM_SUBTREE             L"Failed to get the IPM Tree"
#define ERR_OBJECT_PICKER           L"Failed to get data from Object Picker"
#define ERR_GET_ATTCHMENT           L"Failed to get the attachment"

#define ERR_CREATE_PSTPROFILE       L"Error: Failed to create temporary PST profile"
#define ERR_CREATE_EXCHPROFILE      L"Error: Failed to create temporary Exchange profile"
#define ERR_DELETE_PROFILE          L"Error: Failed to delete temporary profile"

#define ERR_MESSAGE_BODY            L"Message body error"
#define ERR_GEN_EXCHANGEADMIN       L"Exchange admin error"

#define ERR_GETALL_PROFILE          L"Error: Profiles could not be retrieved"
#define ERR_SET_DEFPROFILE          L"Error: Failed to set profile as default"

#define ERR_CREATE_EXCHMBX          L"Error: Failed to create temporary Exchange mailbox"
#define ERR_DELETE_MBOX             L"Error: Failed to delete temporary Exchange mailbox"

#define ERR_MAPI_LOGON              L"Error: Failed to log on to MAPI"

#define ERR_MAPI_APPOINTMENT        L"Error: Appointment item error"
#define ERR_MAPI_CONTACT            L"Error: Contact item error"
#define ERR_MAPI_FOLDER             L"Error: MAPI folder item error"
#define ERR_MAPI_MESSAGE            L"Error: MAPI message item error"
#define ERR_STORE_ERR               L"Error: Message store could not be opened"
#define ERR_ROOT_FOLDER             L"Error: Root folder cannot be opened"
#define ERR_MAPI_TASK               L"Error: MAPI Task item error"
#define ERR_MAPI_INIT               L"Error: MAPI Init Error"

#define ERR_OUT_OF_MEMORY           L"Error: Out of memory"

const WCHAR* const ERR_AVOID_LIST[] = {ERR_ADOBJECT_OPEN, ERR_AD_NOROWS};

class GenericException
{
private:
    HRESULT m_errcode;
    std::wstring m_strdescription;
    std::wstring m_strshortdescription;
    int m_srcLine;
    std::string m_srcFile;

public:
    GenericException(HRESULT hrErrCode, LPCWSTR lpszDescription)
    {
        LOG_ERROR(_T("Throwing exception hr:%08X Description:'%s'"), hrErrCode, lpszDescription);
        m_errcode = hrErrCode;
        m_strdescription = lpszDescription;
    }

    GenericException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile)
    {
        LOG_ERROR(_T("Throwing exception hr:%08X, Description:'%s', ShortDescription:'%s', Line:%d, File:'%hs'"), hrErrCode, lpszDescription, lpszShortDescription, nLine, strFile);
        m_errcode             = hrErrCode;
        m_strdescription      = lpszDescription;
        m_strshortdescription = lpszShortDescription;
        m_srcLine             = nLine;
        m_srcFile             = strFile;
    }

    HRESULT ErrCode() { return m_errcode; }

    std::wstring Description()      { return m_strdescription; }
    std::wstring ShortDescription() { return m_strshortdescription; }
    int SrcLine()                   { return m_srcLine; }
    std::string SrcFile()           { return m_srcFile; }
    virtual ~GenericException() {}
};
