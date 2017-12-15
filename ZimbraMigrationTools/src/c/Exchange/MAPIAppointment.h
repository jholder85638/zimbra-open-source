/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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

#define TOP_LEVEL           0           
#define NORMAL_EXCEPTION    1
#define CANCEL_EXCEPTION    2
// Note: For a recurring appt, TOP_LEVEL represents the SERIES

// MAPIAppointmentException class
class MAPIAppointmentException: public GenericException
{
public:
    MAPIAppointmentException(HRESULT hrErrCode, LPCWSTR lpszDescription);
    MAPIAppointmentException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile);
    virtual ~MAPIAppointmentException() {}
};

// MAPIAppointment class
class MAPIAppointment : public MAPIRfc2445
{
public:
    MAPIAppointment(Zimbra::MAPI::MAPISession &session, Zimbra::MAPI::MAPIStore &store, Zimbra::MAPI::MAPIMessage &mMessage, int nApptType, MAPIAppointment* pParent, WORD* pwOverrideFlags = NULL);
    ~MAPIAppointment();

    HRESULT InitNamedPropsForAppt();

    HRESULT SetMAPIAppointmentValues();
    void InheritSeriesValues(Zimbra::Mapi::COutlookRecurrenceException* lpException);
    void SetCancelExceptionValues(Zimbra::Mapi::CFileTime cancelDate);

    void SetSubject(LPTSTR pStr);

    void SetStartDate(FILETIME ftStartUTC);
    void SetEndDate  (FILETIME ftEndUTC);

    LPWSTR MakeDateFromExceptionPtr(FILETIME ftLCL, BOOL bIncludeTime);
    void SetInstanceUID(LPSBinary bin);
    void SetLocation(LPTSTR pStr);
    void SetBusyStatus(long busystatus);
    ULONG InterpretBusyStatus();
    void SetAllday(unsigned short usAllday);
    BOOL InterpretAllday();
    void SetTransparency(LPTSTR pStr);
    void SetResponseStatus(long responsestatus);
    wstring ConvertValueToRole(long role);
    wstring ConvertValueToPartStat(long ps);
    HRESULT SetOrganizerAndAttendees();
    void SetReminderMinutes(long reminderminutes);
    void SetPrivate(unsigned short usPrivate,bool bSensitivity=false);
    void SetReminderSet(unsigned short usReminderset);
    void SetResponseRequested(unsigned short usPrivate);
    void SetPlainTextFileAndContent();
    void SetHtmlFileAndContent();
    int SetRecurValues(Zimbra::Mapi::Appt& OlkAppt);
    void SetExceptions(Zimbra::Mapi::Appt& OlkAppt);
    void SetExceptionType(int type);
    void Timezone2TZStrings(const Zimbra::Mail::TimeZone& tz, TzStrings& tzs);
    void SetFallbackDefaults();

    Zimbra::Mail::TimeZone* SelectTZ();
    HRESULT AnalyzeSeriesAttachmentTable(LPMESSAGE pSeriesAppt); 
    void OpenExceptionMsg(Zimbra::Mapi::COutlookRecurrenceException *lpOLRE, LPMESSAGE *lppExceptionMessage);
    void ProcessExceptionNormal(Zimbra::Mapi::COutlookRecurrenceException *lpOLRE,    int nExcepNum);
    void ProcessExceptionCancel(Zimbra::Mapi::COutlookRecurrencePattern &refRecurPat, int nExcepNum);

    wstring GetSubject();
    wstring GetStartDate();
    wstring GetCalFilterDate();
    wstring GetStartDateForRecID();
    wstring GetEndDate();
    wstring GetInstanceUID();
    wstring GetLocation();
    wstring GetBusyStatus();
    wstring GetAllday();
    wstring GetTransparency();
    wstring GetReminderMinutes();
    wstring GetResponseStatus();
    wstring GetCurrentStatus();
    wstring GetResponseRequested();
    wstring GetOrganizerName();
    wstring GetOrganizerAddr();
    wstring GetPrivate();
    wstring GetReminderSet();
    wstring GetPlainTextFileAndContent();
    wstring GetHtmlFileAndContent();
    vector<Attendee*> GetAttendees();
    vector<MAPIAppointment*> GetExceptions();
    wstring GetExceptionType();

private:
    // ------------------------------------------
    // Couple of members only set for exceptions
    // ------------------------------------------
    MAPIAppointment* m_pParent; // points to corresponding TOP_LEVEL MAPIAppointment
    WORD m_wOverrideFlags;      // override flags for the exception (from its COutlookRecurrenceException)

    // For a single underlying appt, the first MAPIAppointment to be created will
    // set this to TOP_LEVEL, and m_pParent will be NULL.
    //
    // If the underlying appt has exceptions, then a new child MAPIAppointment is constructed
    // for each by SetExceptions(). These will either be NORMAL_EXCEPTION or CANCEL_EXCEPTION
    // and m_pParent will be set to the corresponding TOP_LEVEL MAPIAppointment
    int m_nApptType;

    bool m_bAllDay;

    // appointment data members (represented both by regular and named props)
    wstring m_sSubject;
    wstring m_sInstanceUID;
    wstring m_sLocation;
    wstring m_sStartDate;
    wstring m_sCalFilterDate;
    wstring m_sStartDateForRecID;
    wstring m_sEndDate;
    wstring m_sBusyStatus;
    wstring m_sAllday;
    wstring m_sTransparency;
    wstring m_sResponseStatus;
    wstring m_sCurrentStatus;
    wstring m_sOrganizerName;
    wstring m_sOrganizerAddr;
    wstring m_sReminderMinutes;
    wstring m_sPrivate;
    wstring m_sReminderSet;
    wstring m_sResponseRequested;
    wstring m_sPlainTextFile;
    wstring m_sHtmlFile;
    wstring m_sExceptionType;

    vector<Attendee*> m_vAttendees;
    vector<MAPIAppointment*> m_vExceptions;


    // ----------------------------------------------
    // Timezone stuff DCB_BUG_98927
    // ----------------------------------------------
    //
    // We read PR_APPT_START/END_DATE_WHOLE from appts. These are in UTC.
    // SetAppointmentRequest requires us to send them as LOCAL along with their Timezones
    // This means we must read TZ props off the underlying MAPI object.
    // There are several props to consider depending on OL version.
    // (Before bug 98927, migration only supported legacy TZ props)
    // 
    // TZ handling changed in OL2007 at which time MS added support for different TZs
    // for start and end dates
    //
    // Of course we don't know until we open the appt whether it was created pre-ol2007
    // so we have to support both schemes
    //
    // As a general rule we use new-style props if they're available, else fall back to legacy. 
    // And if legacy not available, we fall back to OS tz.
    // Newstyle are preferential because they contain non-localized WinTZIDs from which we
    // can derived Olson TZIDs.
    //
    // The two block of members below are initialized at top of SetMAPIAppointmentValues()
    // and are later consumed when:
    // - Creating string versions of the start and end dates
    // - adding TZ info to SetAppointmentRequest
    //
    // Summary of TZ props
    // -----------------------------------------------------------------------------------------------------------------------------------------------
    //
    // OL2003*                                                  Our name                            Notes
    // - PR_TIMEZONE_DESCRIPTION                                PR_TIMEZONE_DESCRIPTION_LEGACY      Localized in language of OS that created the appt
    // - PR_TIMEZONE_STRUCT**                                   PR_TIMEZONE_STRUCT_LEGACY           
    //
    // >=OL2007
    // - PR_TIMEZONE_DESCRIPTION                                PR_TIMEZONE_DESCRIPTION_LEGACY      Localized in language of OS that created the appt
    // - PR_TIMEZONE_STRUCT**
    // - PR_APPOINTMENT_TIMEZONE_DEFINITION_START_DISPLAY       PR_ApptTzStart
    // - PR_APPOINTMENT_TIMEZONE_DEFINITION_END_DISPLAY         PR_ApptTzEnd
    //                                                                          
    //
    // - TODO PR_APPOINTMENT_TIMEZONE_DEFINITION_RECUR          Whats this one - needs more investigation?
    // -----------------------------------------------------------------------------------------------------------------------------------------------
    //
    // *OL2003 SP3 seems to set the new style props like OL2007 too
    // **Recurrent appts only
    //

    // ---------------------------
    // New-style - take precedence
    // ---------------------------

    // Start
    Zimbra::Mail::TimeZone::OlkTimeZone _olkTzStart;
    LPWSTR _pTzStringStart;
    Zimbra::Mail::TimeZone *m_pTzStart;

    // End
    Zimbra::Mail::TimeZone::OlkTimeZone  _olkTzEnd;
    LPWSTR _pTzStringEnd;
    Zimbra::Mail::TimeZone *m_pTzEnd;

    void SetStartOrEndDate(FILETIME ftUTC, bool bStartNotEnd);

    // -------------------------
    // Legacy - fallback
    // -------------------------
    Zimbra::Mail::TimeZone::OlkTimeZone _olkTzLegacy;
    LPWSTR _pTzStringLegacy;
    Zimbra::Mail::TimeZone *m_pTzLegacy;
    BOOL m_bLegacyTZGotFromOS;

    Zimbra::MAPI::MAPIStore *m_mapiStore;

    IAddrBook *m_pAddrBook;
    HRESULT UpdateAttendeeFromEntryId(Attendee &pAttendee,SBinary &eid);
    void OffsetDays(SYSTEMTIME& st, int nDays);

    // Used by OpenExceptionMsg()
    typedef std::map<ULONG, LPMESSAGE> MapRecurTimeToEmbeddedMsg;
    MapRecurTimeToEmbeddedMsg m_mapRecurTimeToEmbeddedMsg;
    bool m_bFilledMapRecurTimeToEmbeddedMsg;
    bool m_bSeriesGotRealAttachments;

    // prop tags for named properties
    ULONG m_PR_CLEAN_GLOBAL_UID;
    ULONG m_PR_APPT_START_WHOLE;
    ULONG m_PR_APPT_END_WHOLE;
    ULONG m_PR_LOCATION;
    ULONG m_PR_BUSY_STATUS;
    ULONG m_PR_ALLDAY;
    ULONG m_PR_IS_RECURRING;
    ULONG m_PR_RESPONSE_STATUS;
    ULONG m_PR_REMINDER_MINUTES;
    ULONG m_PR_PRIVATE;
    ULONG m_PR_REMINDER_SET;

    // Appt props
    typedef enum _AppointmentPropIdx
    {
        N_UID, 
        N_APPT_START_WHOLE, 
        N_APPT_END_WHOLE, 
        N_LOCATION, 
        N_BUSYSTATUS, 
        N_ALLDAY, 
        N_ISRECUR, 
        N_RESPONSESTATUS, 
        N_NUMAPPTPROPS
    } AppointmentPropIdx;

    LONG m_nameIds[N_NUMAPPTPROPS];

    // Common props
    typedef enum _CommonPropIdx
    {
        N_REMINDERMINUTES, 
        N_PRIVATE, 
        N_REMINDERSET, 
        N_NUMCOMMONPROPS
    } CommonPropIdx;

    LONG m_nameIdsC[N_NUMCOMMONPROPS];

    // this enum lists all the props
    enum
    {
        C_MESSAGE_FLAGS, 
        C_SUBJECT, 
        C_BODY, 
        C_HTMLBODY, 
        C_UID, 
        C_APPT_START_WHOLE, 
        C_APPT_END_WHOLE, 
        C_LOCATION, 
        C_BUSYSTATUS, 
        C_ALLDAY, 
        C_ISRECUR,
        C_RESPONSESTATUS,
        C_RESPONSEREQUESTED, 
        C_REMINDERMINUTES, 
        C_PRIVATE, 
        C_REMINDERSET, 
        C_SENSITIVITY,
        C_NUMALLAPPTPROPS
    };
};
