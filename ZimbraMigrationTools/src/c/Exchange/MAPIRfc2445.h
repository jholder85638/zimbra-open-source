/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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

#include "Zimbra\MimeConverter.h"

DEFINE_GUID(PS_OUTLOOK_APPT, 0x00062002, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46);
DEFINE_GUID(PS_OUTLOOK_MTG,  0x6ED8DA90, 0x450B, 0x101B, 0x98, 0xDA, 0x00, 0xAA, 0x00, 0x3F, 0x13, 0x05);
DEFINE_GUID(PS_OUTLOOK_TASK, 0x00062003, 0x0000, 0x0000, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46);

enum OutlookBusyStatus
{
    oFree = 0,
    oTentative = 1,
    oBusy = 2,
    oOutOfOffice = 3
};

enum OutlookMeetingRecipientType
{
    oOrganizer = 0,
    oRequired = 1,
    oOptional = 2,
    oResource = 3
};

enum OutlookResponseStatus
{
    oResponseNone = 0,
    oResponseOrganized = 1,
    oResponseTentative = 2,
    oResponseAccepted = 3,
    oResponseDeclined = 4,
    oResponseNotResponded = 5
};

enum OutlookRecurrenceType
{
    oRecursDaily    = 0,
    oRecursWeekly   = 1,
    oRecursMonthly  = 2,
    oRecursMonthNth = 3,
    oRecursYearly   = 5,
    oRecursYearNth  = 6
};

enum OutlookRecurrenceEndType
{
    oetNotDefined = 0x00000000,
    oetEndDate	  = 0x00002021,
    oetEndAfterN  = 0x00002022,
    oetNoEnd      = 0x00002023,
    oetNoEnd2     = 0xFFFFFFFF
};

typedef enum
{
    etNotDefined = 0x00000000, 
    etEndDate    = 0x00002021, 
    etEndAfterN  = 0x00002022, 
    etNoEnd      = 0x00002023, 
    etNoEnd2     = 0xFFFFFFFF
} RecurrenceEndType;

enum OutlookMaskWeekday
{
    wdmUndefined    = 0x00000000,
    wdmSunday       = 0x00000001,
    wdmMonday       = 0x00000002,
    wdmTuesday      = 0x00000004,
    wdmWednesday    = 0x00000008,
    wdmThursday     = 0x00000010,
    wdmFriday       = 0x00000020,
    wdmSaturday     = 0x00000040
};

typedef struct _Organizer
{
    wstring nam;
    wstring addr;
} Organizer;

typedef struct _Attendee
{
    wstring nam;
    wstring addr;
    wstring role;
    wstring partstat;
} Attendee;

typedef struct _TzStrings
{
    wstring id;

    wstring standardOffset;
    wstring standardStartWeek;
    wstring standardStartWeekday;
    wstring standardStartMonth;
    wstring standardStartHour;
    wstring standardStartMinute;
    wstring standardStartSecond;

    wstring daylightOffset;
    wstring daylightStartWeek;
    wstring daylightStartWeekday;
    wstring daylightStartMonth;
    wstring daylightStartHour;
    wstring daylightStartMinute;
    wstring daylightStartSecond;
} TzStrings;

typedef struct _AttachmentInfo
{
    //wstring FilePath;
    LPSTR pszContentType;
    LPSTR pszTempFile;
    LPSTR pszRealName;
    LPSTR pszContentDisposition;
} AttachmentInfo;

enum OutlookTaskStatus
{
    oTaskNotStarted = 0,
    oTaskInProgress = 1,
    oTaskComplete = 2,
    oTaskWaiting = 3,
    oTaskDeferred = 4
};

enum OutlookImportance
{
    oImportanceLow = 0,
    oImportanceNormal = 1,
    oImportanceHigh = 2
};


class MAPIRfc2445:public Zimbra::Util::ZimObj
{
protected:
    Zimbra::MAPI::MAPIMessage *m_mapiMessage;
    Zimbra::MAPI::MAPISession *m_session;
    LPMESSAGE m_pMessage;
    LPSPropValue m_pPropVals;

    // attachment stuff
    bool m_bHasAttachments;
    vector<AttachmentInfo*> m_vAttachments;

    // recurrence stuff
    bool m_bIsRecurring;
    wstring m_sRecurPattern;
    wstring m_sRecurInterval;
    wstring m_sRecurWkday;
    wstring m_sRecurEndType;
    wstring m_sRecurCount;
    wstring m_sRecurEndDate;
    wstring m_sRecurDayOfMonth;
    wstring m_sRecurMonthOccurrence;
    wstring m_sRecurMonthOfYear;

    TzStrings m_tzsLegacy;
    TzStrings m_tzsStart;
    TzStrings m_tzsEnd;

    void IntToWstring(int src, wstring& dest);

public:
    MAPIRfc2445(Zimbra::MAPI::MAPISession &session, Zimbra::MAPI::MAPIMessage &mMessage);
    ~MAPIRfc2445();

    vector<AttachmentInfo*> GetAttachmentInfo();
    int GetNumHiddenAttachments();
    HRESULT ExtractRealAttachments(int nApptType);
    void GenerateContentDisposition(LPSTR *ppszCD, LPSTR pszFilename);
    void GetContentType(mimepp::Headers& headers, LPSTR *ppStr);

    bool IsRecurring();
    wstring GetRecurPattern();
    wstring GetRecurInterval();
    wstring GetRecurWkday();
    wstring GetRecurEndType();
    wstring GetRecurCount();
    wstring GetRecurEndDate();
    wstring GetRecurDayOfMonth();
    wstring GetRecurMonthOccurrence();
    wstring GetRecurMonthOfYear();

    TzStrings GetTimezoneLegacy();
    TzStrings GetTimezoneStart();
    TzStrings GetTimezoneEnd();

    LPMESSAGE GetInternalMessageObject(){return m_pMessage;}

    HRESULT MAPIToMime(Zimbra::Util::ScopedInterface<IConverterSession>& pConvSess, LPMESSAGE pMsg, LPSTREAM pStream);
    HRESULT ConvertIt(LPMESSAGE pMsg, IStream** ppszMimeMsg, UINT& mimeLength);                                                                       

};
