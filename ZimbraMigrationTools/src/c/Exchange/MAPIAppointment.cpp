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

#include "common.h"
#include "Exchange.h"

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// MAPIAppointmentException
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
MAPIAppointmentException::MAPIAppointmentException(HRESULT hrErrCode, LPCWSTR lpszDescription): GenericException(hrErrCode, lpszDescription)
{
    //
}

MAPIAppointmentException::MAPIAppointmentException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, 
                                                   int nLine, LPCSTR strFile): GenericException(hrErrCode, lpszDescription, lpszShortDescription, nLine, strFile)
{
    //
}

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// MAPIAppointment
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
MAPIAppointment::MAPIAppointment(Zimbra::MAPI::MAPISession &session, Zimbra::MAPI::MAPIStore &store, Zimbra::MAPI::MAPIMessage &mMessage, int nApptType, MAPIAppointment* pParent, WORD* pwOverrideFlags): 
    MAPIRfc2445 (session, mMessage),
    m_pParent(pParent),
    m_wOverrideFlags(0),
    m_bFilledMapRecurTimeToEmbeddedMsg(false),
    m_bSeriesGotRealAttachments(false)
//
// The main call is from MAPIAccount::_GetItem(), for which exceptionType==TOP_LEVEL. This is to construct the top-level appt
// Also called (recursively) from SetExceptions() for each exception. exceptionType will then be either NORMAL_EXCEPTION or CANCEL_EXCEPTION
//
{
    LOGFN_VERBOSE_NO;

    if (pwOverrideFlags)
        m_wOverrideFlags = *pwOverrideFlags;

    m_mapiStore = &store;
    m_nApptType = nApptType;
    SetExceptionType(nApptType);

    if (m_nApptType == NORMAL_EXCEPTION)
        m_sObjectID = _T("NORMAL_EXCEPTION ") + m_sObjectID;
    else
    if (m_nApptType == CANCEL_EXCEPTION)
        m_sObjectID = _T("CANCEL_EXCEPTION ") + m_sObjectID;

    m_pAddrBook = NULL;

    _pTzStringLegacy = NULL;
    memset(&_olkTzLegacy, 0, sizeof(_olkTzLegacy));
    m_pTzLegacy = NULL;
    m_bLegacyTZGotFromOS = FALSE;

    memset(&_olkTzStart, 0, sizeof(_olkTzStart));
    _pTzStringStart = NULL;
    m_pTzStart = NULL;

    memset(&_olkTzEnd, 0, sizeof(_olkTzEnd));
    _pTzStringEnd = NULL;
    m_pTzEnd = NULL;

    // ------------------------------------------------------
    // Init named prop tags (exceptions share series propids)
    // ------------------------------------------------------
    if (m_nApptType == TOP_LEVEL)
        InitNamedPropsForAppt();
    else
    {
        // Exception -> assume same mapping sig as top-level
        _ASSERT(m_pParent);
        m_PR_CLEAN_GLOBAL_UID           = m_pParent->m_PR_CLEAN_GLOBAL_UID;
        m_PR_APPT_START_WHOLE           = m_pParent->m_PR_APPT_START_WHOLE;
        m_PR_APPT_END_WHOLE             = m_pParent->m_PR_APPT_END_WHOLE  ;
        m_PR_LOCATION                   = m_pParent->m_PR_LOCATION        ;
        m_PR_BUSY_STATUS                = m_pParent->m_PR_BUSY_STATUS     ;
        m_PR_ALLDAY                     = m_pParent->m_PR_ALLDAY          ;
        m_PR_IS_RECURRING               = m_pParent->m_PR_IS_RECURRING    ;
        m_PR_REMINDER_MINUTES           = m_pParent->m_PR_REMINDER_MINUTES;
        m_PR_PRIVATE                    = m_pParent->m_PR_PRIVATE         ;
        m_PR_REMINDER_SET               = m_pParent->m_PR_REMINDER_SET    ;
        m_PR_RESPONSE_STATUS            = m_pParent->m_PR_RESPONSE_STATUS ;
    }

    // ----------------------------------------------
    // Open AB
    // ----------------------------------------------
    HRESULT hr = session.OpenAddressBook(&m_pAddrBook);
    if(FAILED(hr))
        m_pAddrBook=NULL;

    // ----------------------------------------------
    // Read MAPI props to members
    // ----------------------------------------------
    if (m_nApptType != CANCEL_EXCEPTION)
    {
        hr = SetMAPIAppointmentValues();
        if(FAILED(hr))
            LOG_WARNING(_T("SetMAPIAppointmentValues failed"));
    }
    else
        LOG_TRACE(_T("Bypassing processing for Cancel exception"));
}

MAPIAppointment::~MAPIAppointment()
{
    LOGFN_VERBOSE;

    if (m_nApptType == TOP_LEVEL)
    {
        while (!m_mapRecurTimeToEmbeddedMsg.empty())
        {
            LPMESSAGE pMsg = m_mapRecurTimeToEmbeddedMsg.begin()->second;
            pMsg->Release();
            m_mapRecurTimeToEmbeddedMsg.erase(m_mapRecurTimeToEmbeddedMsg.begin());
        }
    }

    if (m_pPropVals)
        MAPIFreeBuffer(m_pPropVals);
    m_pPropVals = NULL;

    if(m_pAddrBook)
        m_pAddrBook->Release();
    m_pAddrBook=NULL;

    if (m_nApptType == TOP_LEVEL)
    {
        if (m_pTzLegacy)
            delete m_pTzLegacy;

        if (m_pTzStart)
            delete m_pTzStart;
        if (m_pTzEnd)
            delete m_pTzEnd;
    }
    else
    {
        // The ptrs are just aliases for the ones in the parent appt so don't delete!
    }
}

HRESULT MAPIAppointment::InitNamedPropsForAppt()
{
    LOGFN_VERBOSE;

    HRESULT hr = S_OK;

    // ===============================================
    // Make sure got a PR_MESSAGE_CLASS
    // ===============================================
    Zimbra::Util::ScopedBuffer<SPropValue> pPropValMsgClass;
    if (FAILED(hr = HrGetOneProp(m_pMessage, PR_MESSAGE_CLASS, pPropValMsgClass.getptr())))
        throw MAPIAppointmentException(hr, L"InitNamedPropsForAppt(): HrGetOneProp Failed.",  ERR_MAPI_APPOINTMENT, __LINE__, __FILE__);


    // ===============================================
    // Init named props - See MS-OXPROPS.PDF
    // ===============================================

    // ---------------------------------------------
    // nameIds
    // ---------------------------------------------

    // Appt props
    m_nameIds[0]  = 0x0023; // PidLidCleanGlobalObjectId
    m_nameIds[1]  = 0x820D; // PidLidAppointmentStartWhole 
    m_nameIds[2]  = 0x820E; // PidLidAppointmentEndWhole
    m_nameIds[3]  = 0x8208; // PidLidLocation
    m_nameIds[4]  = 0x8205; // PidLidBusyStatus
    m_nameIds[5]  = 0x8215; // PidLidAppointmentSubType
    m_nameIds[6]  = 0x8223; // PidLidRecurring
    m_nameIds[7]  = 0x8218; // PidLidResponseStatus
    m_nameIds[8]  = 0x8228; // PidLidExceptionReplaceTime

    // Common props
    m_nameIdsC[0] = 0x8501; // PidLidReminderDelta
    m_nameIdsC[1] = 0x8506; // PidLidPrivate
    m_nameIdsC[2] = 0x8503; // PidLidReminderSet

    // -----------
    // MAPINAMEID 
    // -----------
    // Appt
    LPMAPINAMEID ppNames[N_NUMAPPTPROPS] = { 0 };
    for (int i = 0; i < N_NUMAPPTPROPS; i++)
    {
        MAPIAllocateBuffer(sizeof (MAPINAMEID), (LPVOID *)&(ppNames[i]));
        ppNames[i]->ulKind = MNID_ID;
        ppNames[i]->lpguid = (i == N_UID) ? (LPGUID)(&PS_OUTLOOK_MTG) : (LPGUID)(&PS_OUTLOOK_APPT);
        ppNames[i]->Kind.lID = m_nameIds[i];
    }

    // Common
    LPMAPINAMEID ppNamesC[N_NUMCOMMONPROPS] = { 0 };
    for (int i = 0; i < N_NUMCOMMONPROPS; i++)
    {
        MAPIAllocateBuffer(sizeof (MAPINAMEID), (LPVOID *)&(ppNamesC[i]));
        ppNamesC[i]->ulKind = MNID_ID;
        ppNamesC[i]->lpguid = (LPGUID)(&PS_OUTLOOK_COMMON);
        ppNamesC[i]->Kind.lID = m_nameIdsC[i];
    }

    // -------------------------------------------------------------
    // get the real prop tag ID's
    // -------------------------------------------------------------
    // Appt
    LPSPropTagArray pAppointmentTags = NULL;
    if (FAILED(hr = m_pMessage->GetIDsFromNames(N_NUMAPPTPROPS, ppNames, MAPI_CREATE, &pAppointmentTags)))
        throw MAPIAppointmentException(hr, L"Init(): GetIDsFromNames on pAppointmentTags Failed.", ERR_MAPI_APPOINTMENT, __LINE__, __FILE__);

    // Common
    LPSPropTagArray pAppointmentTagsC = NULL;
    if (FAILED(hr = m_pMessage->GetIDsFromNames(N_NUMCOMMONPROPS, ppNamesC, MAPI_CREATE, &pAppointmentTagsC)))
        throw MAPIAppointmentException(hr, L"Init(): GetIDsFromNames on pAppointmentTagsC Failed.",  ERR_MAPI_APPOINTMENT, __LINE__, __FILE__);

    // -------------------------------------------------------------
    // give the prop tag ID's a type
    // -------------------------------------------------------------
    // Appt
    m_PR_CLEAN_GLOBAL_UID               = SetPropType(pAppointmentTags->aulPropTag[N_UID],                      PT_BINARY);
    m_PR_APPT_START_WHOLE               = SetPropType(pAppointmentTags->aulPropTag[N_APPT_START_WHOLE],         PT_SYSTIME);
    m_PR_APPT_END_WHOLE                 = SetPropType(pAppointmentTags->aulPropTag[N_APPT_END_WHOLE],           PT_SYSTIME);
    m_PR_LOCATION                       = SetPropType(pAppointmentTags->aulPropTag[N_LOCATION],                 PT_TSTRING);
    m_PR_BUSY_STATUS                    = SetPropType(pAppointmentTags->aulPropTag[N_BUSYSTATUS],               PT_LONG);
    m_PR_ALLDAY                         = SetPropType(pAppointmentTags->aulPropTag[N_ALLDAY],                   PT_BOOLEAN);
    m_PR_IS_RECURRING                   = SetPropType(pAppointmentTags->aulPropTag[N_ISRECUR],                  PT_BOOLEAN);
    m_PR_RESPONSE_STATUS                = SetPropType(pAppointmentTags->aulPropTag[N_RESPONSESTATUS],           PT_LONG);

    // Common
    m_PR_REMINDER_MINUTES               = SetPropType(pAppointmentTagsC->aulPropTag[N_REMINDERMINUTES],         PT_LONG);
    m_PR_PRIVATE                        = SetPropType(pAppointmentTagsC->aulPropTag[N_PRIVATE],                 PT_BOOLEAN);
    m_PR_REMINDER_SET                   = SetPropType(pAppointmentTagsC->aulPropTag[N_REMINDERSET],             PT_BOOLEAN);

    // ------------------------------------------------------------------
    // free the memory we allocated on the heap
    // ------------------------------------------------------------------
    // Appt
    for (int i = 0; i < N_NUMAPPTPROPS; i++)
        MAPIFreeBuffer(ppNames[i]);
    MAPIFreeBuffer(pAppointmentTags);

    // Common
    for (int i = 0; i < N_NUMCOMMONPROPS; i++)
        MAPIFreeBuffer(ppNamesC[i]);
    MAPIFreeBuffer(pAppointmentTagsC);

    return S_OK;
}

HRESULT MAPIAppointment::SetMAPIAppointmentValues()
//
// Read props from underlying MAPI obj and write to members of this class
//
// DCB NB: This also gets called for each exception message!
{
    // DCB_PERFORMANCE Instead of populating members here, and then the caller copying them into its struct, have the caller pass its struct in and put them straight in :-)
    LOGFN_TRACE;

    HRESULT hr = S_OK;
    m_bAllDay = false;
    m_bHasAttachments = false;
    m_bIsRecurring = false;	

    // ======================================================================================================
    // Read Timezone data from underlying mapi message using ZCO                                              <- DCB_PERFORMANCE Suppress the TZ code when creating MAPIAppointment for an exception (and just set it to parent MAPI_Appointment's TZ?)
    // ======================================================================================================
    // This populates m_pTzStart, m_pTzEnd, m_pTzLegacy. These are used in 2 places:
    // - SetStartOrEndDate() to decide how to format start and end date strings
    // - Also used by caller to return tz_* dictionary members

    // -------------------------------
    // For this we need a ZCO wrapper
    // -------------------------------
    Zimbra::Mapi::Appt appt(m_pMessage, NULL);
    // Above defaults COutlookRecurrencePattern::m_TimeZone to O/S TZ,  but we correct this below by 
    // passing the correct TZ into LoadRecurrence()
    

    if (m_nApptType == TOP_LEVEL)
    {
        // -------------------------------------------------------------------------------------------------------
        // ZCO can return both newstyle (Start and End) props, or legacy props. Let newstyle take precedence since
        // they contain non-localized olson ID
        // -------------------------------------------------------------------------------------------------------

        // ----------
        // Newstyle
        // ----------
        try
        {
            hr = appt.GetStartAndEndTimezones(_olkTzStart, &_pTzStringStart, 
                                                _olkTzEnd,   &_pTzStringEnd);
            // Notes: 
            // - these are available even on non-recurrent, and pTzStrings are ALWAYS available in Olson-style
            // - I don't think its possible for only one to be available - OL seems to set both or neither  
            // - They are also available on exception instances i.e. the MAPIAppointments that get constructed 
            //   by SetExceptions() (for both NORMAL_EXCEPTION and CANCEL_EXCEPTIONs)
        }
        catch(...)
        {
            hr = E_FAIL;
            _ASSERT(FALSE);
        }
        if (SUCCEEDED(hr))
        {
            // Save Start in a Zimbra::Mail::TimeZone object
            _ASSERT(_pTzStringStart && wcslen(_pTzStringStart));
            m_pTzStart = new Zimbra::Mail::TimeZone(_pTzStringStart);
            m_pTzStart->Initialize(_olkTzStart,     _pTzStringStart);  // Silly Init call deletes _pTzStringStart passed in above!

            // Save End in a Zimbra::Mail::TimeZone object   
            _ASSERT(_pTzStringEnd && wcslen(_pTzStringEnd));
            m_pTzEnd = new Zimbra::Mail::TimeZone(_pTzStringEnd);
            m_pTzEnd->Initialize(_olkTzEnd,       _pTzStringEnd);
        }
        else
        {
            // if  m_pTzStart/End unavailable, SetStartOrEndDate() is intelligent enough to fall back to legacy.
            // Legacy ALWAYS available because even if the prop not available on the appt, it falls back to using OS TZ
        }

        // ------------------
        // Fallback to Legacy
        // ------------------
        if (!m_pTzStart || !m_pTzEnd)
        {
            // Obtain _olkTzLegacy from PR_TIMEZONE_STRUCT_LEGACY and _pTzStringLegacy from PR_TIMEZONE_DESCRIPTION_LEGACY
            try
            {
                BOOL bDummy;
                hr = appt.GetLegacyTimezone(TRUE, _olkTzLegacy, &_pTzStringLegacy, &bDummy, &m_bLegacyTZGotFromOS); 
                // Notes: 
                // - Above call falls back to returning OS TZ info if it can't find props on the appt
                // - Also, _pTzStringLegacy will typically be non-olson in the case where the appt was created on non-english windows
            }
            catch(...)
            {
                hr = E_FAIL;
                _ASSERT(FALSE);
            }
            if (SUCCEEDED(hr))
            {
                // Replace time : with . as soon as we get it back. Why is this required?
                // - GetLegacyTimezone() does its best to return an OlsonID in _pTzStringLegacy
                // - But it will fail if the appt was created on a non-english-OS
                // - In this case we will end up passing a raw display string in SetAppointmentRequest SOAP
                // - The server deals with this by a lookup.via CALENDAR.ICS....and this requires "." in times rather than ":"
                if (_pTzStringLegacy && wcslen(_pTzStringLegacy))
                {
                    // An example of a raw display string we might have at this stage would be "(UTC+01.00) Brussels, Copenhagen, Madrid, Paris" (but localized)
                    LPTSTR pTimeColon = wcschr(_pTzStringLegacy, ':');
                    if (   pTimeColon 
                        && (pTimeColon-_pTzStringLegacy) < 10)  // Don't risk replacing : that might appear later in the string 
                    {                                           // e.g. Win XP has "(GMT) Greenwich Mean Time : Dublin, Edinburgh, Lisbon, London"
                        LOG_WARNING(_T("Replacing : in non-Olson TZID"));
                        *pTimeColon = '.';
                    }
                }

                // get the timezone info for this appt
                m_pTzLegacy = new Zimbra::Mail::TimeZone(_pTzStringLegacy);
                m_pTzLegacy->Initialize(_olkTzLegacy, _pTzStringLegacy);
            }
            else
            {
                LOG_WARNING(L"GetLegacyTimezone failed:%08X", hr); // Happens for most non-recurrent // Include subject/EID of failed item. Don't log warning - perfectly legit if it was created by OL2003
                _ASSERT(FALSE);
            }
        }
    }
    else
    {
        // Not TOP-LEVEL -> borrow TZ ptrs from parent
        m_pTzLegacy = m_pParent->m_pTzLegacy;

        m_pTzStart  = m_pParent->m_pTzStart;
        m_pTzEnd    = m_pParent->m_pTzEnd;
    }



    // ------------------------------------------------------------------------------------------------------
    // Read props of the MAPI message and cache as members
    // ------------------------------------------------------------------------------------------------------
    SizedSPropTagArray(C_NUMALLAPPTPROPS, appointmentProps) = 
    {
        C_NUMALLAPPTPROPS, 
        {
            PR_MESSAGE_FLAGS, 
            PR_SUBJECT, 
            PR_BODY, 
            PR_HTML, 
            m_PR_CLEAN_GLOBAL_UID,  // props starting 'm_' are named props
            m_PR_APPT_START_WHOLE, 
            m_PR_APPT_END_WHOLE, 
            m_PR_LOCATION, 
            m_PR_BUSY_STATUS, 
            m_PR_ALLDAY,
            m_PR_IS_RECURRING, 
            m_PR_RESPONSE_STATUS,
            PR_RESPONSE_REQUESTED,
            m_PR_REMINDER_MINUTES, 
            m_PR_PRIVATE, 
            m_PR_REMINDER_SET, 
            PR_SENSITIVITY
        }
    };
    ULONG cVals = 0;
    if (FAILED(hr = m_pMessage->GetProps((LPSPropTagArray) & appointmentProps, fMapiUnicode, &cVals, &m_pPropVals)))
        throw MAPIAppointmentException(hr, L"SetMAPIAppointmentValues(): GetProps Failed.", ERR_MAPI_APPOINTMENT, __LINE__, __FILE__);
    /*
    #ifdef _DEBUG
        DumpPropValues(LOGLEVEL_CMNT_GEN, cVals, m_pPropVals);
    #endif
    */

    // DCB Note: When called to populate an EXCEPTION MAPIAppointment the above m_pPropVals may lack certain props.
    // For example, an exception with unchanged subject will lack PR_SUBJECT.
    // This is not always true though. e.g. PR_SENSITIVITY appears even if the exception's value is unchanged from the series
    //
    // The bottom line is that after building a MAPIAppoinment for an exception, we must inherit missing props from the parent series.
    // InheritSeriesValues() does this.

    

    // ---------------------------------------------
    // Write string versions of the props to members
    // ---------------------------------------------

    // m_bHasAttachments. 
    if (m_pPropVals[C_MESSAGE_FLAGS].ulPropTag == appointmentProps.aulPropTag[C_MESSAGE_FLAGS])
        m_bHasAttachments = (m_pPropVals[C_MESSAGE_FLAGS].Value.l & MSGFLAG_HASATTACH) != 0;
    // Note: a recurrent series with exceptions will return TRUE for this even if the series
    // has no "user" attachments (because complex exceptions are stored as hidden attachments)

    // m_bIsRecurring, m_bAllDay
    if (m_nApptType == TOP_LEVEL)
    {
        // m_bIsRecurring
        if (m_pPropVals[C_ISRECUR].ulPropTag == appointmentProps.aulPropTag[C_ISRECUR])
        {
            m_bIsRecurring = (m_pPropVals[C_ISRECUR].Value.b == 1);

            if (!m_bIsRecurring)
                LOG_TRACE(_T("Single"));
            else
                LOG_TRACE(_T("Series"));
        }

        // m_bAllDay
        if (m_pPropVals[C_ALLDAY].ulPropTag == appointmentProps.aulPropTag[C_ALLDAY])
        {
            m_bAllDay = (m_pPropVals[C_ALLDAY].Value.b == 1); // <- Returns FALSE for a NORMAL_EXCEPTION even when the series is AllDayers
            SetAllday(m_bAllDay?1:0);
        }
    }
    else
        LOG_TRACE(_T("Exception"));

    // m_sSubject
    if (m_pPropVals[C_SUBJECT].ulPropTag == appointmentProps.aulPropTag[C_SUBJECT])
        SetSubject(m_pPropVals[C_SUBJECT].Value.lpszW);

    // m_sInstanceUID
    if (m_pPropVals[C_UID].ulPropTag == appointmentProps.aulPropTag[C_UID])
        SetInstanceUID(&m_pPropVals[C_UID].Value.bin);

    if (m_nApptType != NORMAL_EXCEPTION) // Start and end dates for NORMAL are done in InheritSeriesValues()
    {
        // StartDate
        if (m_pPropVals[C_APPT_START_WHOLE].ulPropTag == appointmentProps.aulPropTag[C_APPT_START_WHOLE])
            SetStartDate(m_pPropVals[C_APPT_START_WHOLE].Value.ft);

        // EndDate
        if (m_pPropVals[C_APPT_END_WHOLE].ulPropTag == appointmentProps.aulPropTag[C_APPT_END_WHOLE])
            SetEndDate(m_pPropVals[C_APPT_END_WHOLE].Value.ft);
    }
    
    // m_sLocation
    if (m_pPropVals[C_LOCATION].ulPropTag == appointmentProps.aulPropTag[C_LOCATION])
        SetLocation(m_pPropVals[C_LOCATION].Value.lpszW);

    // m_sBusyStatus
    if (m_pPropVals[C_BUSYSTATUS].ulPropTag == appointmentProps.aulPropTag[C_BUSYSTATUS])
        SetBusyStatus(m_pPropVals[C_BUSYSTATUS].Value.l);

    // m_sResponseStatus, m_sCurrentStatus
    if (m_pPropVals[C_RESPONSESTATUS].ulPropTag == appointmentProps.aulPropTag[C_RESPONSESTATUS])
        SetResponseStatus(m_pPropVals[C_RESPONSESTATUS].Value.l);

    // m_sResponseRequested
    if (m_pPropVals[C_RESPONSEREQUESTED].ulPropTag == appointmentProps.aulPropTag[C_RESPONSEREQUESTED])
        SetResponseRequested(m_pPropVals[C_RESPONSEREQUESTED].Value.b);

    // m_sReminderMinutes
    unsigned short usReminderSet = 1;
    if (m_pPropVals[C_REMINDERSET].ulPropTag == appointmentProps.aulPropTag[C_REMINDERSET])
        usReminderSet = m_pPropVals[C_REMINDERSET].Value.b;
    if(usReminderSet)
    {
        if (m_pPropVals[C_REMINDERMINUTES].ulPropTag == appointmentProps.aulPropTag[C_REMINDERMINUTES])
            SetReminderMinutes(m_pPropVals[C_REMINDERMINUTES].Value.l);
    }
    
    // m_sPrivate
    // Privacy will be over-ridden by sensitivity.
    // In many cases, C_PRIVATE is not available and PR_SENSITIVITY is present and have definite value
    // keeping C_PRIVATE due to historical presence where SENSITIVITY might not be available
    if (m_pPropVals[C_PRIVATE].ulPropTag == appointmentProps.aulPropTag[C_PRIVATE])
    {
        SetPrivate(m_pPropVals[C_PRIVATE].Value.b);
        LOG_TRACE(L"IsPrivate: %d", m_pPropVals[C_PRIVATE].Value.b);
    }
    
    if(m_pPropVals[C_SENSITIVITY].ulPropTag == appointmentProps.aulPropTag[C_SENSITIVITY])
    {
        SetPrivate((unsigned short)m_pPropVals[C_SENSITIVITY].Value.l,true);
        LOG_TRACE(L"Sensitivity: %d", m_pPropVals[C_SENSITIVITY].Value.l);
    }

    // m_sTransparency
    SetTransparency(L"O");

    if (   (m_nApptType==TOP_LEVEL) 
        || (m_wOverrideFlags & Zimbra::Mapi::efBody))
    {
        // m_sPlainTextFile
        SetPlainTextFileAndContent();

        // m_sHtmlFile
        SetHtmlFileAndContent();
    }

    // Attachments
    // Get contents of real attachments to temp files and record filenames in members of this class.
    //
    // Interpretation of m_bHasAttachments
    // - TOP_LEVEL:         this will be TRUE even if a series which has exceptions has no real attachments 
    //                      (because exceptions are represented by hidden attachments)
    // - NORMAL_EXCEPTIONS: m_bHasAttachments genuinely represents whether the exception has attachments
    // - CANCEL_EXCEPTIONS: This method no longer called for these
    if (m_bHasAttachments)
    {
        BOOL bGotRealAttachments = TRUE;

        // MSGFLAG_HASATTACH is set - but lets find out if we've actually got some REAL attachments
        if (m_nApptType == TOP_LEVEL)
        {
            if (m_bIsRecurring)
            {
                // This is the TOP_LEVEL (series) appt and it's recurring. To find out whether there
                // are real attachemtns, we have to examine the attachment table to see if
                // there are any rows left after weeding out the hidden rows that represent
                // the exceptions
                AnalyzeSeriesAttachmentTable((LPMESSAGE)appt.MapiMsg()); // Sets m_bSeriesGotRealAttachments
                bGotRealAttachments = m_bSeriesGotRealAttachments;
            }
            else
            {
                // TOP-LEVEL non-recurring -> m_bHasAttachments really tells whether series has attachments 
            }
        }
        else
        {
            // Must be a NORMAL_EXCEPTION. m_bHasAttachments is reliable (i.e. means REAL attachments)
            // for NORMAL_EXCEPTION, so go ahead and get them
        }

        if (bGotRealAttachments)
        {
            if (FAILED(ExtractRealAttachments(m_nApptType)))
                LOG_WARNING(_T("Could not extract attachments"));
        }
    }

    // m_sOrganizerName, m_sOrganizerAddr, m_vAttendees
    hr = SetOrganizerAndAttendees();
    if(FAILED(hr))
         LOG_WARNING(_T("SetOrganizerAndAttendees failed"));

    if (m_nApptType == TOP_LEVEL)
    {
        if (m_bIsRecurring)
        {
            // =================================================================================
            // We need to access the recurrence data
            // =================================================================================
            Zimbra::Mapi::COutlookRecurrencePattern &refRecurPat = appt.GetRecurrencePatternRef();

            // LoadRecurrence needs a TZ for its date calculations
            Zimbra::Mail::TimeZone::OlkTimeZone* pOlkTz = NULL;
            LPWSTR pszTzString = NULL;
            if (m_pTzStart)
            {
                pOlkTz = &_olkTzStart;   
                pszTzString = _pTzStringStart;
            }
            else
            if (m_pTzEnd)
            {
                pOlkTz = &_olkTzEnd;     
                pszTzString = _pTzStringEnd;
            }
            else
            if (m_pTzLegacy)
            {
                pOlkTz = &_olkTzLegacy;  
                pszTzString = _pTzStringLegacy;
            }

            // And load the recurrence data
            hr = refRecurPat.LoadRecurrence(pOlkTz, pszTzString, FALSE);
            if (SUCCEEDED(hr))
            {
                // Set recurrence values
                int numExceptions = SetRecurValues(appt);

                // Set exceptions
                if (numExceptions > 0)
                    SetExceptions(appt);
            }
        }

        SetFallbackDefaults();

        // -----------------------------------------------------------------------------------
        // New-style Start and End Timezones
        // -----------------------------------------------------------------------------------
        // Start
        if (m_pTzStart)
        {
            m_tzsStart.id = m_pTzStart->GetId();
            Timezone2TZStrings(*m_pTzStart, m_tzsStart);
        }

        // End
        if (m_pTzEnd)
        {
            m_tzsEnd.id = m_pTzEnd->GetId();
            Timezone2TZStrings(*m_pTzEnd, m_tzsEnd);
        }
    }

    return hr;
}

void MAPIAppointment::SetFallbackDefaults()
//
// Fall back to reasonable defaults if not found values by now
//
{
    if (m_sBusyStatus.length() == 0)
    {
        m_sBusyStatus = L"F";
        LOG_WARNING(_T("Defaulting m_sBusyStatus to F"));
        _ASSERT(FALSE);
    }
    if (m_sAllday.length() == 0)
    {
        m_sAllday = L"0";
        LOG_WARNING(_T("Defaulting m_sAllday to 0"));
        //_ASSERT(FALSE);
    }
    if (m_sTransparency.length() == 0)
    {
        m_sTransparency = L"O";
        LOG_WARNING(_T("Defaulting m_sTransparency to O"));
        _ASSERT(FALSE);
    }
    if (m_sCurrentStatus.length() == 0)  // Not used in SOAP
    {
        m_sCurrentStatus = L"F";
        LOG_VERBOSE(_T("Defaulting m_sCurrentStatus to F"));
        //_ASSERT(FALSE);
    }
    if (m_sResponseStatus.length() == 0)
    {
        m_sResponseStatus = L"NE";
        LOG_VERBOSE(_T("Defaulting m_sResponseStatus to NE"));
        //_ASSERT(FALSE);
    }
    if (m_sResponseRequested.length() == 0)
    {
        m_sResponseRequested = L"0";
        LOG_VERBOSE(_T("Defaulting m_sResponseRequested to 0"));
        //_ASSERT(FALSE);
    }
}

void MAPIAppointment::Timezone2TZStrings(const Zimbra::Mail::TimeZone& tz, TzStrings& tzs)
{
    IntToWstring(tz.GetStandardOffset(), tzs.standardOffset);
    IntToWstring(tz.GetDaylightOffset(), tzs.daylightOffset);

    SYSTEMTIME stdTime;
    tz.GetStandardStart(stdTime);
    IntToWstring(stdTime.wDay,           tzs.standardStartWeek);
    IntToWstring(stdTime.wDayOfWeek + 1, tzs.standardStartWeekday);  // note the + 1 -- bumping weekday
    IntToWstring(stdTime.wMonth,         tzs.standardStartMonth);
    IntToWstring(stdTime.wHour,          tzs.standardStartHour);
    IntToWstring(stdTime.wMinute,        tzs.standardStartMinute);
    IntToWstring(stdTime.wSecond,        tzs.standardStartSecond);

    SYSTEMTIME dsTime;
    tz.GetDaylightStart(dsTime);
    IntToWstring(dsTime.wDay,            tzs.daylightStartWeek);
    IntToWstring(dsTime.wDayOfWeek + 1,  tzs.daylightStartWeekday);   // note the + 1 -- bumping weekday
    IntToWstring(dsTime.wMonth,          tzs.daylightStartMonth);
    IntToWstring(dsTime.wHour,           tzs.daylightStartHour);
    IntToWstring(dsTime.wMinute,         tzs.daylightStartMinute);
    IntToWstring(dsTime.wSecond,         tzs.daylightStartSecond);
}

int MAPIAppointment::SetRecurValues(Zimbra::Mapi::Appt& OlkAppt)
{
    LOGFN_TRACE;

    // =================================================================================
    // We need to access the recurrence data
    // =================================================================================
    Zimbra::Mapi::COutlookRecurrencePattern &refRecurPat = OlkAppt.GetRecurrencePatternRef();

    // =================================================================================
    // Now set members according to refRecurPat
    // =================================================================================
    if (m_pTzLegacy) // Dont pass legacy to c# layer if we're already passing start/end
    {
        // m_tzsLegacy: Read the TZ we stuffed into the OutlookRecurrencePattern earlier
        const Zimbra::Mail::TimeZone& tz = refRecurPat.GetRefOutlookRecurrencePatternTZ();
        m_tzsLegacy.id = tz.GetId();
        Timezone2TZStrings(tz, m_tzsLegacy);
    }

    // m_sRecurPattern
    ULONG ulType = refRecurPat.GetRecurrenceType();
    switch (ulType)
    {
        case oRecursDaily:
            m_sRecurPattern = L"DAI";
            break;
        case oRecursWeekly:
            m_sRecurPattern = L"WEE";
            break;
        case oRecursMonthly:
        case oRecursMonthNth:
            m_sRecurPattern = L"MON";
            break;
        case oRecursYearly:
        case oRecursYearNth:
            m_sRecurPattern = L"YEA";
            break;
        default: ;
    }

    // m_sRecurInterval
    IntToWstring(refRecurPat.GetInterval(), m_sRecurInterval);

    // m_sRecurWkday
    ULONG ulDayOfWeekMask = refRecurPat.GetDayOfWeekMask();
    if (ulDayOfWeekMask & wdmSunday)    
        m_sRecurWkday += L"SU";
    if (ulDayOfWeekMask & wdmMonday)    
        m_sRecurWkday += L"MO";
    if (ulDayOfWeekMask & wdmTuesday)   
        m_sRecurWkday += L"TU";
    if (ulDayOfWeekMask & wdmWednesday) 
        m_sRecurWkday += L"WE";
    if (ulDayOfWeekMask & wdmThursday)  
        m_sRecurWkday += L"TH";
    if (ulDayOfWeekMask & wdmFriday)    
        m_sRecurWkday += L"FR";
    if (ulDayOfWeekMask & wdmSaturday)  
        m_sRecurWkday += L"SA";

    // m_sRecurPattern 
    //bug 77574. not sure why this condition existed .probabaly server was not handling weekday option.
    //if ((m_sRecurPattern == L"DAI") && (m_sRecurWkday.length() > 0))	// every weekday
        //m_sRecurPattern = L"WEE";

    // m_sRecurDayOfMonth or m_sRecurMonthOccurrence
    if (m_sRecurPattern == L"MON")
    {
        if (ulType == oRecursMonthly)
            IntToWstring(refRecurPat.GetDayOfMonth(), m_sRecurDayOfMonth);
        else
        if (ulType == oRecursMonthNth)
        {
            ULONG ulMonthOccurrence = refRecurPat.GetInstance();
            if (ulMonthOccurrence == 5)	    // last
                m_sRecurMonthOccurrence = L"-1";
            else
                IntToWstring(ulMonthOccurrence, m_sRecurMonthOccurrence);
        }
    }
    if (m_sRecurPattern == L"YEA")
    {
        ULONG ulMonthOfYear = refRecurPat.GetMonthOfYear();
        IntToWstring(ulMonthOfYear, m_sRecurMonthOfYear);
        if (ulType == oRecursYearly)
            IntToWstring(refRecurPat.GetDayOfMonth(), m_sRecurDayOfMonth);
        else
        if (ulType == oRecursYearNth)
        {
            ULONG ulMonthOccurrence = refRecurPat.GetInstance();
            if (ulMonthOccurrence == 5)	    // last
                m_sRecurMonthOccurrence = L"-1";
            else
                IntToWstring(ulMonthOccurrence, m_sRecurMonthOccurrence);
        }
    }

    // m_sCalFilterDate
    Zimbra::Mapi::CRecurrenceTime rtEndDate = refRecurPat.GetEndDate();
    Zimbra::Mapi::CFileTime ft = (FILETIME)rtEndDate;
    m_sCalFilterDate = Zimbra::MAPI::Util::CommonDateString(ft);

    // m_sRecurCount or m_sRecurEndDate
    ULONG ulRecurrenceEndType = refRecurPat.GetEndType();
    if (ulRecurrenceEndType == oetEndAfterN)
        IntToWstring(refRecurPat.GetOccurrences(), m_sRecurCount);
    else
    if (ulRecurrenceEndType == oetEndDate)
    {
        SYSTEMTIME st;
        FileTimeToSystemTime(&ft, &st);
        wstring temp = Zimbra::Util::FormatSystemTime(st, TRUE, TRUE);
       m_sRecurEndDate = temp.substr(0, 8);
    }

    return refRecurPat.GetExceptionCount();  
}

void MAPIAppointment::ProcessExceptionNormal(Zimbra::Mapi::COutlookRecurrenceException *lpOLRE, int nExcepNum)
{
    LOGFN_TRACE;

    LOG_VERBOSE(_T("----------------------------------"));
    LOG_VERBOSE(_T("Processing NORMAL exception %d"), nExcepNum+1);
    LOG_VERBOSE(_T("----------------------------------"));

    // Log the override flags - these tell what changed in the exception
    WORD wOverrideFlags = lpOLRE->GetOverrideFlags();
    if (wOverrideFlags)
    {
        LOG_VERBOSE(_T("Overridden:"));
        if (wOverrideFlags & Zimbra::Mapi::efSubject)
            LOG_VERBOSE(_T("- efSubject"));
        if (wOverrideFlags & Zimbra::Mapi::efMeetingType)
            LOG_VERBOSE(_T("- efMeetingType"));
        if (wOverrideFlags & Zimbra::Mapi::efReminderDelta)
            LOG_VERBOSE(_T("- efReminderDelta"));
        if (wOverrideFlags & Zimbra::Mapi::efReminder)
            LOG_VERBOSE(_T("- efReminder"));
        if (wOverrideFlags & Zimbra::Mapi::efLocation)
            LOG_VERBOSE(_T("- efLocation"));
        if (wOverrideFlags & Zimbra::Mapi::efBusyStatus)
            LOG_VERBOSE(_T("- efBusyStatus"));
        if (wOverrideFlags & Zimbra::Mapi::efAttachment)
            LOG_VERBOSE(_T("- efAttachment"));
        if (wOverrideFlags & Zimbra::Mapi::efAllDay)
            LOG_VERBOSE(_T("- efAllDay"));
        if (wOverrideFlags & Zimbra::Mapi::efApptColor)
            LOG_VERBOSE(_T("- efApptColor"));
        if (wOverrideFlags & Zimbra::Mapi::efBody)
            LOG_VERBOSE(_T("- efBody"));
    }

    // ---------------------------------------------------------------------------------
    // NORMAL_EXCEPTIONs have a associated attachment (which contains an embedded appt)
    // ---------------------------------------------------------------------------------

    // FBS bug 70987 -- 5/27/12 -- since Exchange provider doesn't seem to support restriction on
    // attachment table, call OpenApptNoRestrict instead of OpenAppointment
    LPMESSAGE lpNormalExceptionAppt = NULL;
    OpenExceptionMsg(lpOLRE, &lpNormalExceptionAppt);
    if (!lpNormalExceptionAppt)
    {
        LOG_ERROR(L"OpenExceptionMsg failed - exception will be skipped");
        //_ASSERT(FALSE);
        return;
    }

    // lpNormalExceptionAppt is the exception message itself. Use it to build a MAPIAppointment for the exception.
    //
    // This is how things are organized for a recurrent series with exceptions
    //
    // IMessage for series. AttachmentTbl of this contains a row for each exception, and a row for each real attachment
    // The "exception" rows contain a prop PR_ATTACH_DATA_OBJ which contains an embedded appt (the exception) which 
    // can be treated as an appt in its own right. If the exception has attachments, it will have its own AttachmentTbl
    // which provides access to them.
    if (lpNormalExceptionAppt)
    {
        MAPIMessage exMAPIMsg;
        exMAPIMsg.InitMAPIMessage(lpNormalExceptionAppt, *m_session);  // exMAPIMsg now has custody of the MAPI msg underlying lpExceptionAppt and will free when this block leaves scope
        // DCB_PERFORMANCE Why do the above? It's yet another wrapper with associated mass-property read!

        // Wrap lpExceptionAppt in a MAPIAppointment. This calls SetMAPIAppointmentValues() which reads props from the exception appt
        MAPIAppointment* pApptEx = new MAPIAppointment(*m_session, *m_mapiStore, exMAPIMsg, NORMAL_EXCEPTION, this, &wOverrideFlags);

        // Inherit missing props from the TOP_LEVEL series
        pApptEx->InheritSeriesValues(lpOLRE);

        // And save for use by caller
        m_vExceptions.push_back(pApptEx);
    }
}

void MAPIAppointment::ProcessExceptionCancel(Zimbra::Mapi::COutlookRecurrencePattern &refRecurPat, int nExcepNum)
{
    LOGFN_TRACE;

    // Execution arrives here if an instance of the recurrent series has been deleted i.e. cancelled
    LOG_VERBOSE(_T("----------------------------------"));
    LOG_VERBOSE(_T("Processing CANCEL exception %d"), nExcepNum+1);
    LOG_VERBOSE(_T("----------------------------------"));

    Zimbra::Mapi::CRecurrenceTime rtDate = refRecurPat.GetExceptionOriginalDate(nExcepNum);
    Zimbra::Mapi::CFileTime ftOrigDate = (FILETIME)rtDate;

    MAPIAppointment* pApptEx = new MAPIAppointment(*m_session, *m_mapiStore, *m_mapiMessage, CANCEL_EXCEPTION, this); // Note that m_mapiMessage is the TOP_LEVEL msg here!
    pApptEx->SetCancelExceptionValues(ftOrigDate);
    m_vExceptions.push_back(pApptEx);
}

// FBS Bug 70987 (migration).  Need this method since some Exchange providers don't support
// restrictions on attachment tables.  Have to restrict after the fact
void MAPIAppointment::OpenExceptionMsg(Zimbra::Mapi::COutlookRecurrenceException *lpOLRE, LPMESSAGE *lppExceptionMessage)
//
// Returns the exception's attachment, and that attachment's embedded message (which will be an appt representing the exception)
//
{
    LOGFN_TRACE;
    _ASSERT(m_bFilledMapRecurTimeToEmbeddedMsg);

    // Want to find the LPMESSAGE which represents the attachment for time specified in lpOLRE
    ULONG ulMinutes = lpOLRE->GetOriginalDateTime(); // Convert to minutes because that's what we did when we populated the map

    MapRecurTimeToEmbeddedMsg::iterator it = m_mapRecurTimeToEmbeddedMsg.find(ulMinutes);
    if (it != m_mapRecurTimeToEmbeddedMsg.end())
        *lppExceptionMessage = m_mapRecurTimeToEmbeddedMsg[ulMinutes];
    else
        *lppExceptionMessage = NULL;
}

void MAPIAppointment::SetExceptions(Zimbra::Mapi::Appt& OlkAppt)
//
// Walks this TOP-LEVEL MAPIAppointment's exceptions, creating a child MAPIAppointment for each
// exception.
//
// The children will be of type NORMAL_EXCEPTION or CANCEL_EXCEPTION
//
// Note that SetMAPIAppointmentValues() is called for each exception MAPIApppointment to
// set default values for the string fields, which are then overwritten by a call to 
// InheritSeriesValues()/SetCancelExceptionValues()
{
    LOGFN_TRACE;

    _ASSERT(m_nApptType == TOP_LEVEL);

    // --------------------------------------------------------------------
    // Exception info is stored in this appt's COutlookRecurrencePattern
    // --------------------------------------------------------------------
    Zimbra::Mapi::COutlookRecurrencePattern &refRecurPat = OlkAppt.GetRecurrencePatternRef();
    LONG lExceptionCount = refRecurPat.GetExceptionCount();
    LOG_TRACE(_T("%d Exception(s)"), lExceptionCount);

    // --------------------------------------------------------------------
    // Walk the exceptions, creating a new (child) MAPIAppointment for each
    // --------------------------------------------------------------------
    // Each child MAPIAppointment is created from the main appt's attachment and
    // will be used to generate atttributes for the exception
    for (LONG i = 0; i < lExceptionCount; i++)
    {
        // ---------------------------------------------------
        // Get COutlookRecurrenceException for this exception
        // ---------------------------------------------------
        // If we GetException() returns non-NULL, its a NORMAL_EXCEPTION i.e. an instance of the
        // series that has been modified in some way. Else its a deleted instance aka a CANCEL_EXCEPTION
        Zimbra::Mapi::COutlookRecurrenceException *lpOLRE = refRecurPat.GetException(i);
        if (lpOLRE != NULL)    
            ProcessExceptionNormal(lpOLRE, i);
        else
            ProcessExceptionCancel(refRecurPat, i);

        // delete done for the above new MAPIAppointments in COMMapiAccount::GetData
    }
}

void MAPIAppointment::InheritSeriesValues(Zimbra::Mapi::COutlookRecurrenceException* lpOLRE)
//
// Some props e.g. PR_SUBJECT are not available on the exception attachment if they were not
// explicitly modified in the exception. When this happens we inherit them from the parent series.
//
// A couple of props, we choose to pull from COutlookRecurrenceException instead. Unclear why.
{
    LOGFN_TRACE;

    _ASSERT(m_nApptType == NORMAL_EXCEPTION);

    // ---------
    // m_bAllDay
    // ---------
    // Take from lpOLRE if override flags say so else inherit from parent
    if (m_wOverrideFlags & Zimbra::Mapi::efAllDay)
        m_bAllDay = lpOLRE->GetAllDay()==TRUE;
    else
        m_bAllDay = m_pParent->m_bAllDay;
    SetAllday(m_bAllDay?1:0);

    // --------------------
    // m_sStartDateForRecID
    // --------------------
    // DCB This one is ALWAYS pulled from lpOLRE. Section 2.2.1.44.2 of MS_OXOCAL.PDF implies its always available
    // FBS 4/12/12 -- set this up no matter what (so exceptId will be set)
    // FBS bug 71050 -- 4/9/12 -- recurrence id needs the original occurrence date
    Zimbra::Mapi::CRecurrenceTime rtOriginalDateLCL = lpOLRE->GetOriginalDateTime();  
    Zimbra::Mapi::CFileTime ftOriginalDateLCL = (FILETIME)rtOriginalDateLCL;
    m_sStartDateForRecID = MakeDateFromExceptionPtr(ftOriginalDateLCL, !m_pParent->m_bAllDay);


    // From here down, we just pull data from either the OLRE or the parent series


    // -------------
    // m_sStartDate
    // -------------
    // DCB This one is ALWAYS pulled from lpOLRE. Section 2.2.1.44.2 of MS_OXOCAL.PDF implies its always available
    Zimbra::Mapi::CRecurrenceTime rtStartDate = lpOLRE->GetStartDateTime();
    Zimbra::Mapi::CFileTime ftStartDate = (FILETIME)rtStartDate;
    m_sStartDate = MakeDateFromExceptionPtr(ftStartDate, !m_bAllDay);

    // ----------------
    // m_sCalFilterDate
    // ----------------
    m_sCalFilterDate = Zimbra::MAPI::Util::CommonDateString(ftStartDate);

    // ----------
    // m_sEndDate
    // ----------
    // DCB This one is ALWAYS pulled from lpOLRE. Section 2.2.1.44.2 of MS_OXOCAL.PDF implies its always available
    Zimbra::Mapi::CRecurrenceTime rtEndDate = lpOLRE->GetEndDateTime();

    // Subtract 1 day from end date for Zimbra friendliness. (All day appts in OL run from
    // 00:00 to 00:00 the following day; for Zimbra end date must be same day) 
    if (m_bAllDay)
        rtEndDate = rtEndDate - 1440/*minutes*/;  

    Zimbra::Mapi::CFileTime ftEndDate = (FILETIME)rtEndDate;
    m_sEndDate = MakeDateFromExceptionPtr(ftEndDate, !m_bAllDay);


    // ----------
    // m_sSubject
    // ----------
    if (m_wOverrideFlags & Zimbra::Mapi::efSubject)
        m_sSubject = lpOLRE->GetSubject(); 
    else
        m_sSubject = m_pParent->m_sSubject;

    // -----------
    // m_sLocation
    // -----------
    if (m_wOverrideFlags & Zimbra::Mapi::efLocation)
        m_sLocation = lpOLRE->GetLocation();
    else
        m_sLocation = m_pParent->m_sLocation;

    // -------------
    // m_sBusyStatus
    // -------------
    if (m_sBusyStatus.length() == 0)
    {
        if (this->InterpretBusyStatus() !=  lpOLRE->GetBusyStatus())
            SetBusyStatus(lpOLRE->GetBusyStatus());
        else
            m_sBusyStatus = m_pParent->m_sBusyStatus;
    }

    if (m_sResponseStatus.length() == 0)
        m_sResponseStatus = m_pParent->m_sResponseStatus;

    if (m_sCurrentStatus.length() == 0)
        m_sCurrentStatus = m_pParent->m_sCurrentStatus;

    if (m_sOrganizerName.length() == 0)
        m_sOrganizerName = m_pParent->m_sOrganizerName;

    if (m_sOrganizerAddr.length() == 0)
        m_sOrganizerAddr = m_pParent->m_sOrganizerAddr;

    if (m_sReminderMinutes.length() == 0)
        m_sReminderMinutes = m_pParent->m_sReminderMinutes;

    if (m_sPrivate.length() == 0)
        m_sPrivate = m_pParent->m_sPrivate;

    if (m_sReminderSet.length() == 0)
        m_sReminderSet = m_pParent->m_sReminderSet;

    if (m_sResponseRequested.length() == 0)
        m_sResponseRequested = m_pParent->m_sResponseRequested;

    if (m_sPlainTextFile.length() == 0)
        m_sPlainTextFile = m_pParent->m_sPlainTextFile;

    if (m_sHtmlFile.length() == 0)
        m_sHtmlFile = m_pParent->m_sHtmlFile;

    SetFallbackDefaults();
}

void MAPIAppointment::SetCancelExceptionValues(Zimbra::Mapi::CFileTime cancelDate)
//
// For CANCEL_EXCEPTIONs, we just copy all fields from the parent appt, except for
// m_sStartDate - which we set to cancellation date 
//
{
    LOGFN_VERBOSE;

    _ASSERT(m_nApptType == CANCEL_EXCEPTION);

    m_sStartDate           = MakeDateFromExceptionPtr(cancelDate, !m_pParent->m_bAllDay);

    m_sSubject             = m_pParent->m_sSubject;              // Does SetAppointmentRequest <cancel> node really need this data? Probably just needs cancel date for exceptId?
    m_sLocation            = m_pParent->m_sLocation;
    m_sBusyStatus          = m_pParent->m_sBusyStatus;
    m_sAllday              = m_pParent->m_sAllday;
    m_sResponseStatus      = m_pParent->m_sResponseStatus;
    m_sCurrentStatus       = m_pParent->m_sCurrentStatus;
    m_sOrganizerName       = m_pParent->m_sOrganizerName;
    m_sOrganizerAddr       = m_pParent->m_sOrganizerAddr;
    m_sReminderMinutes     = m_pParent->m_sReminderMinutes;
    m_sPrivate             = m_pParent->m_sPrivate;
    m_sReminderSet         = m_pParent->m_sReminderSet;
    m_sResponseRequested   = m_pParent->m_sResponseRequested;
    m_sPlainTextFile       = m_pParent->m_sPlainTextFile;
    m_sHtmlFile            = m_pParent->m_sHtmlFile;
}

HRESULT MAPIAppointment::AnalyzeSeriesAttachmentTable(LPMESSAGE lpSeriesAppt)
{
    LOGFN_TRACE;

    _ASSERT(m_nApptType == TOP_LEVEL);

    HRESULT hResult = S_OK;

    // First call in populates m_mapRecurTimeToEmbeddedMsg. Subsequent calls lookup up in the map.
    if (!m_bFilledMapRecurTimeToEmbeddedMsg)
    {
        // --------------------------
        // Open attachment table
        // --------------------------
        LOG_TRACE(_T("Opening attachment table for %p"), lpSeriesAppt);
        Zimbra::Util::ScopedInterface<IMAPITable> lpAttachTbl;
        Zimbra::Util::ScopedRowSet lpAttachRows;
        hResult = lpSeriesAppt->GetAttachmentTable(fMapiUnicode, lpAttachTbl.getptr());
        ReturnIfFailed(hResult, hResult);

        LOG_TRACE(_T("Setting columns..."));
        SizedSPropTagArray(4, ptaExceptions) = {4, {PR_ATTACH_NUM, PR_ATTACHMENT_FLAGS, PR_ATTACHMENT_HIDDEN, PR_ATTACH_METHOD}};
        hResult = lpAttachTbl->SetColumns((LPSPropTagArray) & ptaExceptions, 0);

        LOG_TRACE(_T("Getting row count..."));
        ULONG ulRowCount = 0;
        hResult = lpAttachTbl->GetRowCount(0, &ulRowCount);
        ReturnIfFailed(hResult, hResult);

        LOG_TRACE(_T("Querying rows..."));
        hResult = lpAttachTbl->QueryRows(ulRowCount, 0, lpAttachRows.getptr()); // Note, the rows we get are just those for NORMAL exceptions, not CANCELs
        ReturnIfFailed(hResult, hResult);
        LOG_TRACE(_T("%d row(s)"), ulRowCount);
        /*        
        #ifdef _DEBUG
            DumpRows(lpAttachRows.get(), LOGLEVEL_CMNT_GEN);
        #endif
        */
        

        // -----------------------------------------------------
        // Each row represents a real attachment or an exception
        // -----------------------------------------------------
        for (unsigned int i = 0; i < lpAttachRows->cRows; i++)
        {
            if ((lpAttachRows->aRow[i].lpProps[1].ulPropTag == PT_ERROR) ||
                (lpAttachRows->aRow[i].lpProps[2].ulPropTag == PT_ERROR))
                continue;

            // Only interested in rows that represent exceptions (not the rows that are genuine attachments on the series)
            if ( (lpAttachRows->aRow[i].lpProps[2].Value.b) &&
                 (lpAttachRows->aRow[i].lpProps[3].Value.l == ATTACH_EMBEDDED_MSG) &&
                ((lpAttachRows->aRow[i].lpProps[1].Value.l & afException) != 0))
            {
                // ---------------------------------
                // Open the attachment by ATTACH_NUM
                // ---------------------------------
                LOG_TRACE(_T("Series Attachment Row %d"), i);
                LOG_TRACE(_T("    Opening attachment..."));
                Zimbra::Util::ScopedInterface<IAttach> pAttach;
                ULONG ulNumber = lpAttachRows->aRow[i].lpProps[0].Value.ul;
                hResult = lpSeriesAppt->OpenAttach(ulNumber, NULL, MAPI_MODIFY, pAttach.getptr());
                ContinueIfFailed(hResult);

                // ----------------------------------------------------
                // Open the attachment's data obj - an embedded message
                // ----------------------------------------------------
                LOG_TRACE(_T("    Opening embedded msg..."));
                Zimbra::Util::ScopedInterface<IMessage> pEmbedMessage;
                hResult = pAttach->OpenProperty(PR_ATTACH_DATA_OBJ, &IID_IMessage, 0, MAPI_MODIFY, (LPUNKNOWN *)pEmbedMessage.getptr());
                ContinueIfFailed(hResult);

                /*
                #ifdef _DEBUG
                    DumpPropValues(LOGLEVEL_CMNT_GEN, pEmbedMessage.get());
                #endif
                */

                // ----------------------------------------------------
                // Grab PR_MESSAGE_CLASS and PidLidExceptionReplaceTime
                // ----------------------------------------------------
                LOG_TRACE(_T("    Checking PR_MESSAGE_CLASS and PidLidExceptionReplaceTime..."));

                // PidLidExceptionReplaceTime is named so get its ID
                MAPINAMEID namedProp= { 0 };
                namedProp.ulKind   = MNID_ID;
                namedProp.lpguid   = (LPGUID)(&PS_OUTLOOK_APPT);
                namedProp.Kind.lID = 0x8228; //PidLidExceptionReplaceTime
                LPMAPINAMEID lpNamedID = &namedProp;
                Zimbra::Util::ScopedBuffer<SPropTagArray> spTags;
                hResult = pEmbedMessage->GetIDsFromNames(1, &lpNamedID, MAPI_CREATE, spTags.getptr());
                ContinueIfFailed(hResult);
                ULONG ulPidLidExceptionReplaceTime = PROP_TAG(PT_SYSTIME, PROP_ID(spTags->aulPropTag[0]));
                ContinueIf(PROP_TYPE(ulPidLidExceptionReplaceTime) == PT_ERROR);

                SizedSPropTagArray(2, tags) = {2, { PR_MESSAGE_CLASS, PR_NULL/*named*/ }};
                tags.aulPropTag[1] = ulPidLidExceptionReplaceTime;
                ULONG cVals = 0;
                Zimbra::Util::ScopedBuffer<SPropValue> pVals;
                hResult = pEmbedMessage->GetProps((LPSPropTagArray)& tags, MAPI_UNICODE, &cVals, pVals.getptr());
                if (FAILED(hResult) || (cVals!=2))
                {
                    LOG_ERROR(_T("Failed to read PR_MESSAGE_CLASS and PidLidExceptionReplaceTime from exception %d %08X : %d"), i, hResult, cVals);
                    //_ASSERT(FALSE);
                    continue;
                }
                if (pVals[0].ulPropTag != PR_MESSAGE_CLASS)
                {
                    LOG_ERROR(_T("Failed to read PR_MESSAGE_CLASS from exception %d"), i);
                    //_ASSERT(FALSE);
                    continue;
                }
                if (pVals[1].ulPropTag != ulPidLidExceptionReplaceTime)
                {
                    LOG_ERROR(_T("Failed to read PidLidExceptionReplaceTime from exception %d"), i);
                    //_ASSERT(FALSE);
                    continue;
                }

                // Check msg class
                LPCTSTR pszMsgClass = pVals[0].Value.LPSZ;
                ContinueIf(!pszMsgClass);
                ContinueIf(0 != _tcsicmp(EXCEPTION_EMBED_MESSAGE_CLASS, pszMsgClass));

                Zimbra::Mail::TimeZone* pTZ = SelectTZ();
                Zimbra::Mapi::CFileTime ftDate = pVals[1].Value.ft;
                ftDate.MakeLocal(*pTZ); // DCB_BUG_99102 This is set to OS TZ. We need to it set to TZ from the appt
                Zimbra::Mapi::CRecurrenceTime rtTime = ftDate;
                ULONG ulMinutes = (ULONG)rtTime;

                // Finally, add to the map
                LPMESSAGE pMsg = pEmbedMessage.release();
                LOG_TRACE(_T("    Adding %d->%p to map..."), ulMinutes, pMsg);
                m_mapRecurTimeToEmbeddedMsg.insert(MapRecurTimeToEmbeddedMsg::value_type(ulMinutes, pMsg));
                pMsg->AddRef(); // Release() in ::~MAPIAppointment
            }
            else
                m_bSeriesGotRealAttachments = true;
        } // for

        if (m_bSeriesGotRealAttachments)
            LOG_TRACE(_T("Series has REAL attachments"));
        else
            LOG_TRACE(_T("Series has NO real attachments"));

        m_bFilledMapRecurTimeToEmbeddedMsg = true;
    }

    return hResult;
}

Zimbra::Mail::TimeZone* MAPIAppointment::SelectTZ()
{
    Zimbra::Mail::TimeZone* pTZ = NULL;

    if (m_pTzStart)
        pTZ = m_pTzStart;
    else
    if (m_pTzEnd)
        pTZ = m_pTzEnd;
    else
    if (m_pTzLegacy)
        pTZ = m_pTzLegacy;  

    return pTZ;
}

void MAPIAppointment::OffsetDays(SYSTEMTIME& st, int nDays)
{
    double dat = -1;
    if (SystemTimeToVariantTime(&st, &dat))
    {
        dat += nDays;
        VariantTimeToSystemTime(dat, &st);
    }
}

void MAPIAppointment::SetSubject(LPTSTR pStr)
{
    LOGFN_VERBOSE;
    m_sSubject = pStr;
}

void MAPIAppointment::SetStartDate(FILETIME ftStartUTC)
//
// ftStartUTC is a PR_APPT_START_DATE_WHOLE read off an appt. It is UTC
// We want a Zimbra string representation of this - m_sStartDate
//
{
    LOGFN_VERBOSE;

    SetStartOrEndDate(ftStartUTC, true);

    // Also set m_sCalFilterDate
    m_sCalFilterDate = Zimbra::MAPI::Util::CommonDateString(m_pPropVals[C_APPT_START_WHOLE].Value.ft);   // may have issue with recur/local
}

void MAPIAppointment::SetEndDate(FILETIME ftEndUTC)
// Like SetStartDate() but for the end date
{
    LOGFN_VERBOSE;
    SetStartOrEndDate(ftEndUTC, false);
}

LPWSTR MAPIAppointment::MakeDateFromExceptionPtr(FILETIME ftLCL, BOOL bIncludeTime)
{
    //LOGFN_VERBOSE;
    SYSTEMTIME stLCL;
    FileTimeToSystemTime(&ftLCL, &stLCL);
    return Zimbra::Util::FormatSystemTime(stLCL, FALSE /*NotUTC*/, bIncludeTime);
}

void MAPIAppointment::SetInstanceUID(LPSBinary bin)
{
    //LOGFN_VERBOSE;
    Zimbra::Util::ScopedArray<CHAR> spUid(new CHAR[(bin->cb * 2) + 1]);
    if (spUid.get() != NULL)
    {
        Zimbra::Util::HexFromBin(bin->lpb, bin->cb, spUid.get());
    }

    m_sInstanceUID = Zimbra::Util::AnsiiToUnicode(spUid.get());
    LOG_TRACE(L"UID: %d",m_sInstanceUID);
    if( m_sInstanceUID.length()> 255)
    {
        m_sInstanceUID = m_sInstanceUID.substr(0,255);
        LOG_WARNING(_T("UID is truncated to 255 characters to avoid ZCS rejection. \nNew UID: %s"), m_sInstanceUID);
    }
}

void MAPIAppointment::SetLocation(LPTSTR pStr)
{
    //LOGFN_VERBOSE;
    m_sLocation = pStr;
}

void MAPIAppointment::SetBusyStatus(long busystatus)
{
    //LOGFN_VERBOSE;
    switch (busystatus)
    {
        case oFree:		    m_sBusyStatus = L"F";	break;
        case oTentative:	m_sBusyStatus = L"T";	break;
        case oBusy:		    m_sBusyStatus = L"B";	break;
        case oOutOfOffice:	m_sBusyStatus = L"O";	break;
        default:		    m_sBusyStatus = L"T";
    }
}

ULONG MAPIAppointment::InterpretBusyStatus()
{
    //LOGFN_VERBOSE;
    long retval;
    if (m_sBusyStatus == L"F") 
        retval = oFree;
    else
    if (m_sBusyStatus == L"T") 
        retval = oTentative;
    else
    if (m_sBusyStatus == L"B") 
        retval = oBusy;
    else
    if (m_sBusyStatus == L"O") 
        retval = oOutOfOffice;
    else
        retval = oFree;

    return retval;
}

wstring MAPIAppointment::ConvertValueToRole(long role)
{
    //LOGFN_VERBOSE;
    wstring retval = L"REQ";
    switch (role)
    {
        case oOrganizer:    retval = L"CHAIR";	break;
        case oRequired:	    retval = L"REQ";	break;
        case oOptional:	    retval = L"OPT";	break;
        case oResource:	    retval = L"NON";	break;
        default:	    ;
    }
    return retval;
}

wstring MAPIAppointment::ConvertValueToPartStat(long ps)
{
    //LOGFN_VERBOSE;
    wstring retval = L"NE";
    switch (ps)
    {
        case oResponseNone:	        retval = L"NE";	break;
        case oResponseOrganized:    retval = L"OR";	break;
        case oResponseTentative:    retval = L"TE";	break;
        case oResponseAccepted:	    retval = L"AC";	break;
        case oResponseDeclined:	    retval = L"DE";	break;
        case oResponseNotResponded: retval = L"NE";	break;
        default:		    ;
    }
    return retval;
}

void MAPIAppointment::SetAllday(unsigned short usAllday)
{
    //LOGFN_VERBOSE;
    m_sAllday = (usAllday == 1) ? L"1" : L"0";
}

BOOL MAPIAppointment::InterpretAllday()
{
    //LOGFN_VERBOSE;
    return (m_sAllday == L"1");
}

void MAPIAppointment::SetTransparency(LPTSTR pStr)
{
    //LOGFN_VERBOSE;
    m_sTransparency = pStr;
}

void MAPIAppointment::SetResponseStatus(long responsestatus)
{
    //LOGFN_VERBOSE;
    switch (responsestatus)
    {
        case oResponseNone:		    m_sResponseStatus = L"NE";	break;
        case oResponseOrganized:	m_sResponseStatus = L"OR";	break;	    // OR????  -- temporary
        case oResponseTentative:	m_sResponseStatus = L"TE";	break;
        case oResponseAccepted:		m_sResponseStatus = L"AC";	break;
        case oResponseDeclined:		m_sResponseStatus = L"DE";	break;
        case oResponseNotResponded:	m_sResponseStatus = L"NE";	break;
        default:			        m_sResponseStatus = L"NE";
    }
    m_sCurrentStatus = m_sResponseStatus;
}

void MAPIAppointment::SetReminderMinutes(long reminderminutes)
{
    //LOGFN_VERBOSE;
    WCHAR pwszTemp[10];
    _ltow(reminderminutes, pwszTemp, 10);
    m_sReminderMinutes = pwszTemp;
}

void MAPIAppointment::SetPrivate(unsigned short usPrivate,bool bSensitivity)
{
    //LOGFN_VERBOSE;
    if(bSensitivity)
        m_sPrivate = (usPrivate == 2) ? L"1" : L"0";
    else
        m_sPrivate = (usPrivate == 1) ? L"1" : L"0";
}

void MAPIAppointment::SetReminderSet(unsigned short usReminderset)
{
    //LOGFN_VERBOSE;
    m_sReminderSet = (usReminderset == 1) ? L"1" : L"0";
}

void MAPIAppointment::SetResponseRequested(unsigned short usPrivate)
{
    //LOGFN_VERBOSE;
    m_sResponseRequested = (usPrivate == 1) ? L"1" : L"0";
}
void MAPIAppointment::SetPlainTextFileAndContent()
{
    //LOGFN_VERBOSE;
    m_sPlainTextFile = Zimbra::MAPI::Util::SetPlainText(m_pMessage, &m_pPropVals[C_BODY]);
}

void MAPIAppointment::SetHtmlFileAndContent()
{
    //LOGFN_VERBOSE;
    m_sHtmlFile = Zimbra::MAPI::Util::SetHtml(m_pMessage, &m_pPropVals[C_HTMLBODY]);
}

void MAPIAppointment::SetExceptionType(int type)
{
    //LOGFN_VERBOSE;
    if (type == NORMAL_EXCEPTION)
        m_sExceptionType = L"except";
    else
    if (type == CANCEL_EXCEPTION)
        m_sExceptionType = L"cancel";
    else
        m_sExceptionType = L"none";
}

HRESULT MAPIAppointment::UpdateAttendeeFromEntryId(Attendee &pAttendee, SBinary &eid)
{
    LOGFN_TRACE;
    Zimbra::Util::ScopedInterface<IMailUser> pUser;
    ULONG ulObjType = 0;
    if(!m_pAddrBook)
        return E_FAIL;

    HRESULT hr = m_pAddrBook->OpenEntry(eid.cb, (LPENTRYID)eid.lpb, NULL, MAPI_BEST_ACCESS, &ulObjType, (LPUNKNOWN *)pUser.getptr());
    if (FAILED(hr) || (ulObjType != MAPI_MAILUSER))
        return E_FAIL;

    SizedSPropTagArray(3, tags) = {3, { PR_ADDRTYPE_W, PR_DISPLAY_NAME_W, PR_EMAIL_ADDRESS_W }};
    ULONG cVals = 0;
    Zimbra::Util::ScopedBuffer<SPropValue> pVals;
    hr = pUser->GetProps((LPSPropTagArray) & tags, MAPI_UNICODE, &cVals, pVals.getptr());
    if (FAILED(hr))
        return hr;

    // no smtp address, bail
    if ((pVals->ulPropTag != PR_ADDRTYPE_W) || (wcsicmp(pVals->Value.lpszW, L"SMTP") != 0))
        return E_FAIL;
    if ((pVals[1].ulPropTag == PR_DISPLAY_NAME_W) && ((pAttendee.nam == L"")))
        pAttendee.nam = pVals[1].Value.lpszW;
    if ((pVals[2].ulPropTag == PR_EMAIL_ADDRESS_W) && (pAttendee.addr == L""))
        pAttendee.addr=pVals[2].Value.lpszW;

    return S_OK;
}

HRESULT MAPIAppointment::SetOrganizerAndAttendees()
{
    LOGFN_TRACE;

    // Open recipient table
    Zimbra::Util::ScopedInterface<IMAPITable> pRecipTable;
    HRESULT hr = m_pMessage->GetRecipientTable(fMapiUnicode, pRecipTable.getptr());
    if (FAILED(hr))
    {
        LOG_INFO(L"GetRecipientTable failed:%08X", hr);
        return hr;
    }

    typedef enum _AttendeePropTagIdx
    {
        AT_ADDRTYPE, AT_ENTRYID, AT_DISPLAY_NAME, AT_SMTP_ADDR, AT_RECIPIENT_FLAGS, AT_RECIPIENT_TYPE, AT_RECIPIENT_TRACKSTATUS,AT_EMAIL_ADDRESS, AT_NPROPS
    } AttendeePropTagIdx;

    SizedSPropTagArray(AT_NPROPS, reciptags) = {
        AT_NPROPS, { PR_ADDRTYPE, PR_ENTRYID, PR_DISPLAY_NAME_W, PR_SMTP_ADDRESS_W, PR_RECIPIENT_FLAGS, PR_RECIPIENT_TYPE, PR_RECIPIENT_TRACKSTATUS,PR_EMAIL_ADDRESS }
    };

    hr = pRecipTable->SetColumns((LPSPropTagArray) & reciptags, 0);
    if (FAILED(hr))
    {
        LOG_ERROR(_T("could not get the recipient table, hr:%08X"), hr);
        return hr;
    }

    ULONG ulRows = 0;
    hr = pRecipTable->GetRowCount(0, &ulRows);
    if (FAILED(hr))
    {
        LOG_ERROR(_T("could not get the recipient table row count, hr:%08X"), hr);
        return hr;
    }
    if(ulRows < 1)
    {
        LOG_VERBOSE(L"No recipients found. ulRows: %d", ulRows);
        return S_OK;
    }

    Zimbra::Util::ScopedRowSet pRecipRows;
    hr = pRecipTable->QueryRows(ulRows, 0, pRecipRows.getptr());
    if (FAILED(hr))
    {
        LOG_ERROR(_T("Failed to query table rows. hr:%08X"), hr);
        LOG_INFO(L"Recipient Count: %d", ulRows);
        return hr;
    }

    #ifdef _DEBUG
        //#define DEBUG_BUG_93364
    #endif


    // Walk the rows
    if (pRecipRows != NULL)
    {
        #ifdef DEBUG_BUG_93364
            DumpRows(pRecipRows.get(), LOGLEVEL_CMNT_GEN); // DCB_BUG_93364 debug

            // Above shows missing PR_SMTP_ADDRESS for the organizer.
            // Organizer has PR_RECIPIENT_FLAGS == 3 (see https://msdn.microsoft.com/en-us/library/office/cc815629.aspx)
        #endif

        for (ULONG iRow = 0; iRow < pRecipRows->cRows; iRow++)
        {
            if (pRecipRows->aRow[iRow].lpProps[AT_RECIPIENT_FLAGS].ulPropTag == reciptags.aulPropTag[AT_RECIPIENT_FLAGS])
            {
                if ((pRecipRows->aRow[iRow].lpProps[AT_RECIPIENT_FLAGS].Value.l) & (1 << 1)) //check for 2nd bit
                {
                    // ----------------------------------------------------------------------------------
                    // Organizer
                    // ----------------------------------------------------------------------------------

                    // m_sOrganizerName
                    if (PROP_TYPE(pRecipRows->aRow[iRow].lpProps[AT_DISPLAY_NAME].ulPropTag) != PT_ERROR)
                        m_sOrganizerName = pRecipRows->aRow[iRow].lpProps[AT_DISPLAY_NAME].Value.lpszW;

                    // m_sOrganizerAddr
                    if (PROP_TYPE(pRecipRows->aRow[iRow].lpProps[AT_SMTP_ADDR].ulPropTag) != PT_ERROR)
                         m_sOrganizerAddr = pRecipRows->aRow[iRow].lpProps[AT_SMTP_ADDR].Value.lpszW;


                    // If counldnt find in recip table, get from elsewhere
                    if ((lstrcmpiW(m_sOrganizerAddr.c_str(),L"") == 0) ||(m_sOrganizerName ==L""))
                    {
                        LOG_TRACE(L"Organizer not found in recip table - get from EID...");

                        Attendee* pAttendee = new Attendee(); 
                        if (UpdateAttendeeFromEntryId(*pAttendee,pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].Value.bin) !=S_OK)
                        {
                            LOG_TRACE(L"Organizer not found in EID - get from AD");

                            RECIP_INFO tempRecip;
                            tempRecip.pAddrType = NULL;
                            tempRecip.pEmailAddr = NULL;
                            tempRecip.cbEid = 0;
                            tempRecip.pEid = NULL;
                            tempRecip.pAddrType = pRecipRows->aRow[iRow].lpProps[AT_ADDRTYPE].Value.lpszW;
                            tempRecip.pEmailAddr = pRecipRows->aRow[iRow].lpProps[AT_EMAIL_ADDRESS].Value.lpszW;

                            if (pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].ulPropTag != PT_ERROR)
                            {
                                tempRecip.cbEid = pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].Value.bin.cb;
                                tempRecip.pEid = (LPENTRYID)pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].Value.bin.lpb;
                            }

                            std::wstring wstrEmailAddress;
                            try
                            {
                                Zimbra::MAPI::Util::GetSMTPFromAD(*m_session, tempRecip,L"" , L"",wstrEmailAddress);
                            }
                            catch(...)
                            {
                                LOG_WARNING(_T("mapiappointment::exception from MAPI::util::GetSMTPFromAD "));
                            }

                            pAttendee->addr = wstrEmailAddress;

                            LOG_TRACE(L"Email address(AD): %d", wstrEmailAddress);
                            LOG_TRACE(L"AD update end.");
                        }

                        LOG_TRACE(L"EID update end.");

                        if(m_sOrganizerAddr==L"") 
                            m_sOrganizerAddr = pAttendee->addr;
                        if(m_sOrganizerName==L"")
                            m_sOrganizerName = pAttendee->nam;
                    }

                    LOG_TRACE(L"OrganizerAddr: %s  OrganizerName: %s", m_sOrganizerAddr.c_str(), m_sOrganizerName.c_str());
                    if (m_sOrganizerAddr == L"")
                    {
                        LOG_ERROR(_T("Unknown OrganizerAddr"));
                        #ifdef DEBUG_BUG_93364
                            _ASSERT(FALSE);
                        #endif  
                    }
                    if (m_sOrganizerName == L"")
                    {
                        LOG_ERROR(_T("Unknown OrganizerName"));
                        #ifdef DEBUG_BUG_93364
                            _ASSERT(FALSE);
                        #endif  
                    }
                }
                else
                {
                    // ----------------------------------------------------------------------------------
                    // Attendee
                    // ----------------------------------------------------------------------------------
                    if (!(RECIP_FLAG_EXCEP_DELETED & pRecipRows->aRow[iRow].lpProps[AT_RECIPIENT_FLAGS].Value.l)) // make sure attendee wasn't deleted
                    {
                        Attendee* pAttendee = new Attendee();   // delete done in COMMapiAccount::GetData after we allocate dict string for ZimbraAPI
                        if (PROP_TYPE(pRecipRows->aRow[iRow].lpProps[AT_DISPLAY_NAME].ulPropTag) != PT_ERROR)
                            pAttendee->nam = pRecipRows->aRow[iRow].lpProps[AT_DISPLAY_NAME].Value.lpszW;
                
                        if (PROP_TYPE(pRecipRows->aRow[iRow].lpProps[AT_SMTP_ADDR].ulPropTag) != PT_ERROR)
                            pAttendee->addr = pRecipRows->aRow[iRow].lpProps[AT_SMTP_ADDR].Value.lpszW;
                        
                        wstring attaddress = pAttendee->addr;
                        if (lstrcmpiW(attaddress.c_str(),L"") == 0) 
                        {
                            if (((pAttendee->nam==L"") || (pAttendee->addr==L"")) && (PROP_TYPE(pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].ulPropTag) != PT_ERROR))
                            {
                                LOG_TRACE(L"Going to Update attendee from EID...");
                                if (UpdateAttendeeFromEntryId(*pAttendee,pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].Value.bin)!=S_OK)
                                {
                                    LOG_INFO(L"Going to update attendee from AD");				
                                    RECIP_INFO tempRecip;
                                    tempRecip.pAddrType = NULL;
                                    tempRecip.pEmailAddr = NULL;
                                    tempRecip.cbEid = 0;
                                    tempRecip.pEid = NULL;

                                    tempRecip.pAddrType = pRecipRows->aRow[iRow].lpProps[AT_ADDRTYPE].Value.lpszW;
                                    tempRecip.pEmailAddr = pRecipRows->aRow[iRow].lpProps[AT_EMAIL_ADDRESS].Value.lpszW;

                                    if (pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].ulPropTag != PT_ERROR)
                                    {
                                        tempRecip.cbEid = pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].Value.bin.cb;
                                        tempRecip.pEid = (LPENTRYID)pRecipRows->aRow[iRow].lpProps[AT_ENTRYID].Value.bin.lpb;
                                    }
                                    
                                    std::wstring wstrEmailAddress;
                                    try
                                    {
                                        Zimbra::MAPI::Util::GetSMTPFromAD(*m_session, tempRecip,L"" , L"",wstrEmailAddress);
                                    }
                                    catch(...)
                                    {
                                        LOG_WARNING(_T("Mapiappoinemtn::Exception in MAPI::Util::GetSMTPFromAD"));
                                    }
                                    pAttendee->addr = wstrEmailAddress;
                                    LOG_TRACE(L"Email address(AD): %s", wstrEmailAddress.c_str());
                                    LOG_TRACE(L"AD update end.");
                                }
                                LOG_TRACE(L"EID update end.");
                            }
                        }

                        LOG_TRACE(L"AttendeeAddr: '%s'  AttendeeName: '%s'", pAttendee->addr.c_str() ,pAttendee->nam.c_str());
                        
                        if (PROP_TYPE(pRecipRows->aRow[iRow].lpProps[AT_RECIPIENT_TYPE].ulPropTag) != PT_ERROR)
                            pAttendee->role = ConvertValueToRole(pRecipRows->aRow[iRow].lpProps[AT_RECIPIENT_TYPE].Value.l);
                        if (PROP_TYPE(pRecipRows->aRow[iRow].lpProps[AT_RECIPIENT_TRACKSTATUS].ulPropTag) != PT_ERROR)
                            pAttendee->partstat = ConvertValueToPartStat(pRecipRows->aRow[iRow].lpProps[AT_RECIPIENT_TRACKSTATUS].Value.l);

                        m_vAttendees.push_back(pAttendee);
                    }
                }
            }
        }
    }
    return hr;
}

wstring MAPIAppointment::GetSubject()                       { return m_sSubject; }
wstring MAPIAppointment::GetStartDate()                     { return m_sStartDate; }
wstring MAPIAppointment::GetCalFilterDate()                 { return m_sCalFilterDate; }
wstring MAPIAppointment::GetStartDateForRecID()             { return m_sStartDateForRecID; }
wstring MAPIAppointment::GetEndDate()                       { return m_sEndDate; }
wstring MAPIAppointment::GetInstanceUID()                   { return m_sInstanceUID; }
wstring MAPIAppointment::GetLocation()                      { return m_sLocation; }
wstring MAPIAppointment::GetBusyStatus()                    { return m_sBusyStatus; }
wstring MAPIAppointment::GetAllday()                        { return m_sAllday; }
wstring MAPIAppointment::GetTransparency()                  { return m_sTransparency; }
wstring MAPIAppointment::GetReminderMinutes()               { return m_sReminderMinutes; }
wstring MAPIAppointment::GetResponseStatus()                { return m_sResponseStatus; }
wstring MAPIAppointment::GetCurrentStatus()                 { return m_sCurrentStatus; }
wstring MAPIAppointment::GetResponseRequested()             { return m_sResponseRequested; }
wstring MAPIAppointment::GetOrganizerName()                 { return m_sOrganizerName; }
wstring MAPIAppointment::GetOrganizerAddr()                 { return m_sOrganizerAddr; }
wstring MAPIAppointment::GetPrivate()                       { return m_sPrivate; }
wstring MAPIAppointment::GetReminderSet()                   { return m_sReminderSet;}
wstring MAPIAppointment::GetPlainTextFileAndContent()       { return m_sPlainTextFile; }
wstring MAPIAppointment::GetHtmlFileAndContent()            { return m_sHtmlFile; }
vector<Attendee*> MAPIAppointment::GetAttendees()           { return m_vAttendees; }
vector<MAPIAppointment*> MAPIAppointment::GetExceptions()   { return m_vExceptions; }
wstring MAPIAppointment::GetExceptionType()                 { return m_sExceptionType; }

void MAPIAppointment::SetStartOrEndDate(FILETIME ftUTC, bool bStartNotEnd)
//
// Only called for TOP_LEVEL (NORMAL_EXCEPTION takes dates from OLRecurrenceException)
//
// Populates m_sStartDate/m_sEndDate with a LOCAL (in-timezone) datetime string 
{
    LOGFN_VERBOSE;

    _ASSERT(m_nApptType != NORMAL_EXCEPTION);

    // Convert ftStartUTC to a SYSTEMTIME
    SYSTEMTIME stUTC;
    FileTimeToSystemTime(&ftUTC, &stUTC);

    // Init some ailiases to point to start or end data
    wstring sLog                = bStartNotEnd? _T("StartDate"):_T("EndDate");
    wstring& sDestDateTimeLCL   = bStartNotEnd? m_sStartDate   :m_sEndDate;
    Zimbra::Mail::TimeZone* pTz = bStartNotEnd? m_pTzStart     :m_pTzEnd;

    bool bUsingLegacy = false;
    if (!pTz)
    {
        LOG_TRACE(_T("Using legacy TZ prop %s for %s"), m_bLegacyTZGotFromOS?_T("(derived from OS timezone)"):_T(""), sLog.c_str());
        pTz = m_pTzLegacy;
        bUsingLegacy = true;
    }

    // if AllDay appt, subtract one from the end date. This is because an AllDay appt in OL for 1 Jan has
    // StartDate "1/Jan 00:00" and "EndDate 2/Jan 00:00". ZCS needs the EndDate to be 1/Jan
    if (!bStartNotEnd && m_bAllDay)
        OffsetDays(stUTC, -1);

    // We want to make appear as it would were we viewing it in the TZ in which it was
    // specified. m_pTzStart is this TZ, so extract TIME_ZONE_INFORMATION from this
    SYSTEMTIME stLocal;
    TIME_ZONE_INFORMATION tziLocal = {0};
    _ASSERT(pTz);

    if (!bUsingLegacy && !m_bLegacyTZGotFromOS)
    {
        // -------------------------------------------------------------
        // We've got a reliable TZ - so generate a LOCAL datetime string <- // This is the most common code path
        // -------------------------------------------------------------
        pTz->PopulateTimeZoneInfo(tziLocal);

        BOOL bConvToLclSucceeded = SystemTimeToTzSpecificLocalTime(&tziLocal, &stUTC, &stLocal);

        // Here's an example of the above conversion and why its necessary:
        //
        // An all-day appt for 9/March/2008 created in German TZ looks like this:
        //
        //               As UTC      After conversion
        //
        //      wYear    2008        2008
        //      wMonth   3           3
        //      wDay     9           10
        //      wHour    23          0           
        //      wMin     0           0
        //
        // Note how it looks odd in UTC - apparently the day before at 11pm!
        //
        // Thats because the user entered it in their LOCAL TIME - a German TZ - 
        // which is +60mins offset from UTC -> so 60 had to be subtracted when 
        // it was saved as UTC

        // Get get the Zimbra string form (note fall back to using UTC if the above failed)
        if (bConvToLclSucceeded)
        {
            //sDestDateTime = Zimbra::Util::FormatSystemTime(stLocal, FALSE/*Local*/, /*!bAllday*/TRUE);  // Why not include TIME for allday?
            sDestDateTimeLCL = Zimbra::Util::FormatSystemTime(stLocal, FALSE/*Local*/, !m_bAllDay);
            LOG_VERBOSE(_T("LOCAL %s: %s"), sLog.c_str(), sDestDateTimeLCL.c_str());
        }
        else
        {
            sDestDateTimeLCL = Zimbra::Util::FormatSystemTime(stUTC, TRUE/*IsUTC*/, /*!bAllday*/TRUE);
            LOG_WARNING(_T("Failed to convert to local -> using UTC %s: %s"), sLog.c_str(), sDestDateTimeLCL.c_str());
        }
    }
    else
    {
        // ---------------------------------------------------------------------------------------
        // We're using legacy TZ info but it was derived from OS TZ - we just guessed it; 
        // probably a corrupt appt e.g. see https://bugzilla.zimbra.com/show_bug.cgi?id=85955#c5
        // ----------------------------------------------------------------------------------------
        //_ASSERT(FALSE); // More work to do here ?
        LOG_WARNING(_T("TZ info unavailable for %s -> using OS TZ"), sLog.c_str());

        // Snap the start and end times of AllDay appt to the nearest day, and calc UTC string
        if (m_bAllDay)
        {
            if (stUTC.wHour > 12)
            {
                OffsetDays(stUTC, 1); // Bump into the next day
                stUTC.wHour = 0;
            }
            else
            {
                OffsetDays(stUTC, -1); // Bump into the previous day
                stUTC.wHour = 0;
            }


            // If its an Alldayer, can we guess the TZ from the offset??
            // TODO here:
            // reverse lookup to get list of TZs    Dont think we can do this - cos dont know if the missing TZ was in DLS or not.
            // create m_pTzLegacy


            sDestDateTimeLCL = Zimbra::Util::FormatSystemTime(stUTC, TRUE/*IsUTC*/, TRUE/*IncludeTime*/);
            LOG_WARNING(_T("Snapped AllDay %s: %s"), sLog.c_str(), sDestDateTimeLCL.c_str());
        }
        else
        {
            // Non-AllDay -> 2 choices: either use UTC, or use the OSTZ. Opted for former.
            sDestDateTimeLCL = Zimbra::Util::FormatSystemTime(stUTC, TRUE/*IsUTC*/, TRUE/*IncludeTime*/);
            LOG_WARNING(_T("Using Raw UTC %s: %s"), sLog.c_str(), sDestDateTimeLCL.c_str());
        }
    }
}
