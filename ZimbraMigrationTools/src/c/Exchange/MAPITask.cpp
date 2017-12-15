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
#include "common.h"
#include "Exchange.h"

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// MAPIAppointmentException
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
MAPITaskException::MAPITaskException(HRESULT hrErrCode, LPCWSTR lpszDescription): 
    GenericException(hrErrCode, lpszDescription)
{
    //
}

MAPITaskException::MAPITaskException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile): 
    GenericException(hrErrCode, lpszDescription, lpszShortDescription, nLine, strFile)
{
    //
}

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// MAPITask
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
MAPITask::MAPITask(Zimbra::MAPI::MAPISession &session, Zimbra::MAPI::MAPIMessage &mMessage)
    : MAPIRfc2445 (session, mMessage)
{
    LOGFN_TRACE_NO;

    // ----------------------------------------------
    // Init named prop tags
    // ----------------------------------------------
    m_PR_IS_RECURRING       = 0;
    m_PR_STATUS             = 0;
    m_PR_PERCENT_COMPLETE   = 0;
    m_PR_TASK_START         = 0;
    m_PR_TASK_DUE           = 0;
    m_PR_TOTAL_WORK         = 0;
    m_PR_ACTUAL_WORK        = 0;
    m_PR_COMPANIES          = 0;
    m_PR_MILEAGE            = 0;
    m_PR_BILLING_INFO       = 0;
    m_PR_REMINDER_SET       = 0;
    m_PR_TASK_FLAG_DUE_BY   = 0;
    m_PR_PRIVATE            = 0;
    InitNamedPropsForTask();

    // ----------------------------------------------
    // Set member defaults
    // ----------------------------------------------
    m_sSubject              = L"";
    m_sImportance           = L"";
    m_sTaskStart            = L"";
    m_sTaskDue              = L"";
    m_sStatus               = L"";
    m_sPercentComplete      = L"";
    m_sTotalWork            = L"";
    m_sActualWork           = L"";
    m_sCompanies            = L"";
    m_sMileage              = L"";
    m_sBillingInfo          = L"";
    m_sTaskFlagDueBy        = L"";
    m_sPrivate              = L"";

    SetMAPITaskValues();
}

MAPITask::~MAPITask()
{
    LOGFN_TRACE_NO;
    if (m_pPropVals)
    {
        MAPIFreeBuffer(m_pPropVals);
    }
    m_pPropVals = NULL;
}

HRESULT MAPITask::InitNamedPropsForTask()
{
    LOGFN_TRACE_NO;

    // -------------------------------------------
    // init named props
    // -------------------------------------------

    // Main
    nameIds[0] = 0x8126;  // PidLidTaskFRecurring
    nameIds[1] = 0x8101;  // PidLidTaskStatus
    nameIds[2] = 0x8102;  // PidLidPercentComplete
    nameIds[3] = 0x8104;  // PidLidTaskStartDate
    nameIds[4] = 0x8105;  // PidLidTaskDueDate
    nameIds[5] = 0x8111;  // PidLidTaskEstimatedEffort
    nameIds[6] = 0x8110;  // PidLidTaskActualEffort

    // Common
    nameIdsC[0] = 0x8539; // PidLidCompanies
    nameIdsC[1] = 0x8534; // PidLidMileage
    nameIdsC[2] = 0x8535; // PidLidBilling
    nameIdsC[3] = 0x8503; // PidLidReminderSet
    nameIdsC[4] = 0x8560; // PidLidReminderSignalTime
    nameIdsC[5] = 0x8506; // PidLidPrivate

    HRESULT hr = S_OK;
    Zimbra::Util::ScopedBuffer<SPropValue> pPropValMsgClass;
    if (FAILED(hr = HrGetOneProp(m_pMessage, PR_MESSAGE_CLASS, pPropValMsgClass.getptr())))
        throw MAPITaskException(hr, L"InitNamedPropsForTask(): HrGetOneProp Failed.",  ERR_MAPI_TASK, __LINE__, __FILE__);

    // initialize the MAPINAMEID structure GetIDsFromNames requires
    LPMAPINAMEID ppNames[N_NUMTASKPROPS] = { 0 };
    for (int i = 0; i < N_NUMTASKPROPS; i++)
    {
        MAPIAllocateBuffer(sizeof (MAPINAMEID), (LPVOID *)&(ppNames[i]));
        ppNames[i]->ulKind = MNID_ID;
        ppNames[i]->lpguid = (LPGUID)(&PS_OUTLOOK_TASK);
        ppNames[i]->Kind.lID = nameIds[i];
    }

    LPMAPINAMEID ppNamesC[N_NUMCOMMONTPROPS] = { 0 };
    for (int i = 0; i < N_NUMCOMMONTPROPS; i++)
    {
        MAPIAllocateBuffer(sizeof (MAPINAMEID), (LPVOID *)&(ppNamesC[i]));
        ppNamesC[i]->ulKind = MNID_ID;
        ppNamesC[i]->lpguid = (LPGUID)(&PS_OUTLOOK_COMMON);
        ppNamesC[i]->Kind.lID = nameIdsC[i];
    }

    // get the real prop tag ID's
    LPSPropTagArray pTaskTags  = NULL;
    if (FAILED(hr = m_pMessage->GetIDsFromNames(N_NUMTASKPROPS, ppNames, MAPI_CREATE, &pTaskTags)))
        throw MAPITaskException(hr, L"Init(): GetIDsFromNames on pTaskTags Failed.", ERR_MAPI_TASK,  __LINE__, __FILE__);

    LPSPropTagArray pTaskTagsC = NULL;
    if (FAILED(hr = m_pMessage->GetIDsFromNames(N_NUMCOMMONTPROPS, ppNamesC, MAPI_CREATE, &pTaskTagsC)))
        throw MAPITaskException(hr, L"Init(): GetIDsFromNames on pAppointmentTagsC Failed.", ERR_MAPI_TASK,  __LINE__, __FILE__);

    // give the prop tag ID's a type
    m_PR_IS_RECURRING       = SetPropType(pTaskTags->aulPropTag[N_ISRECURT],            PT_BOOLEAN);
    m_PR_STATUS             = SetPropType(pTaskTags->aulPropTag[N_STATUS],              PT_LONG);
    m_PR_PERCENT_COMPLETE   = SetPropType(pTaskTags->aulPropTag[N_PERCENTCOMPLETE],     PT_DOUBLE);
    m_PR_TASK_START         = SetPropType(pTaskTags->aulPropTag[N_TASKSTART],           PT_SYSTIME);
    m_PR_TASK_DUE           = SetPropType(pTaskTags->aulPropTag[N_TASKDUE],             PT_SYSTIME);
    m_PR_TOTAL_WORK         = SetPropType(pTaskTags->aulPropTag[N_TOTALWORK],           PT_LONG);
    m_PR_ACTUAL_WORK        = SetPropType(pTaskTags->aulPropTag[N_ACTUALWORK],          PT_LONG);

    m_PR_COMPANIES          = SetPropType(pTaskTagsC->aulPropTag[N_COMPANIES],          PT_MV_TSTRING);
    m_PR_MILEAGE            = SetPropType(pTaskTagsC->aulPropTag[N_MILEAGE],            PT_TSTRING);
    m_PR_BILLING_INFO       = SetPropType(pTaskTagsC->aulPropTag[N_BILLING],            PT_TSTRING);
    m_PR_REMINDER_SET       = SetPropType(pTaskTagsC->aulPropTag[N_TASKREMINDERSET],    PT_BOOLEAN);
    m_PR_TASK_FLAG_DUE_BY   = SetPropType(pTaskTagsC->aulPropTag[N_TASKFLAGDUEBY],      PT_SYSTIME);
    m_PR_PRIVATE            = SetPropType(pTaskTagsC->aulPropTag[N_TPRIVATE],           PT_BOOLEAN);

    // free the memory we allocated on the head
    for (int i = 0; i < N_NUMTASKPROPS; i++)
        MAPIFreeBuffer(ppNames[i]);
    for (int i = 0; i < N_NUMCOMMONTPROPS; i++)
        MAPIFreeBuffer(ppNamesC[i]);
    MAPIFreeBuffer(pTaskTags);
    MAPIFreeBuffer(pTaskTagsC);

    return S_OK;
}

HRESULT MAPITask::SetMAPITaskValues()
{
    LOGFN_TRACE_NO;
    SizedSPropTagArray(T_NUMALLTASKPROPS, taskProps) = {
    T_NUMALLTASKPROPS, {
        PR_MESSAGE_FLAGS, 
        PR_SUBJECT, 
        PR_BODY, 
        PR_HTML, 
        PR_IMPORTANCE, 
        m_PR_IS_RECURRING, 
        m_PR_STATUS,
        m_PR_PERCENT_COMPLETE, 
        m_PR_TASK_START, 
        m_PR_TASK_DUE, 
        m_PR_TOTAL_WORK,
        m_PR_ACTUAL_WORK, 
        m_PR_COMPANIES, 
        m_PR_MILEAGE, 
        m_PR_BILLING_INFO,
        m_PR_REMINDER_SET, 
        m_PR_TASK_FLAG_DUE_BY, 
        m_PR_PRIVATE
    }
    };

    HRESULT hr = S_OK;
    ULONG cVals = 0;
    m_bHasAttachments = false;
    m_bIsRecurring = false;
    m_bIsTaskReminderSet = false;

    if (FAILED(hr = m_pMessage->GetProps((LPSPropTagArray) & taskProps, fMapiUnicode, &cVals, &m_pPropVals)))
        throw MAPITaskException(hr, L"SetMAPITaskValues(): GetProps Failed.",  ERR_MAPI_TASK, __LINE__, __FILE__);

    if (m_pPropVals[T_MESSAGE_FLAGS].ulPropTag == taskProps.aulPropTag[T_MESSAGE_FLAGS])
        m_bHasAttachments = (m_pPropVals[T_MESSAGE_FLAGS].Value.l & MSGFLAG_HASATTACH) != 0;

    if (m_pPropVals[T_ISRECURT].ulPropTag == taskProps.aulPropTag[T_ISRECURT]) // do this first to set dates correctly
        m_bIsRecurring = (m_pPropVals[T_ISRECURT].Value.b == 1);

    if (m_pPropVals[T_TASKREMINDERSET].ulPropTag == taskProps.aulPropTag[T_TASKREMINDERSET]) // do this first to set dates correctly
        m_bIsTaskReminderSet = (m_pPropVals[T_TASKREMINDERSET].Value.b == 1);

    if (m_pPropVals[T_SUBJECT].ulPropTag == taskProps.aulPropTag[T_SUBJECT])
        SetSubject(m_pPropVals[T_SUBJECT].Value.lpszW);

    if (m_pPropVals[T_IMPORTANCE].ulPropTag == taskProps.aulPropTag[T_IMPORTANCE])
        SetImportance(m_pPropVals[T_IMPORTANCE].Value.l);

    if (m_pPropVals[T_STATUS].ulPropTag == taskProps.aulPropTag[T_STATUS])
        SetTaskStatus(m_pPropVals[T_STATUS].Value.l);

    if (m_pPropVals[T_PERCENTCOMPLETE].ulPropTag == taskProps.aulPropTag[T_PERCENTCOMPLETE])
        SetPercentComplete(m_pPropVals[T_PERCENTCOMPLETE].Value.dbl);

    if (m_pPropVals[T_TASKSTART].ulPropTag == taskProps.aulPropTag[T_TASKSTART])
        SetTaskStart(m_pPropVals[T_TASKSTART].Value.ft);

    if (m_pPropVals[T_TASKDUE].ulPropTag == taskProps.aulPropTag[T_TASKDUE])
        SetTaskDue(m_pPropVals[T_TASKDUE].Value.ft);

    if (m_pPropVals[T_TOTALWORK].ulPropTag == taskProps.aulPropTag[T_TOTALWORK])
        SetTotalWork(m_pPropVals[T_TOTALWORK].Value.l);

    if (m_pPropVals[T_ACTUALWORK].ulPropTag == taskProps.aulPropTag[T_ACTUALWORK])
        SetActualWork(m_pPropVals[T_ACTUALWORK].Value.l);

    if (m_pPropVals[T_COMPANIES].ulPropTag == taskProps.aulPropTag[T_COMPANIES])
        SetCompanies(m_pPropVals[T_COMPANIES].Value.MVszW.lppszW[0]);	// get first one for now

    if (m_pPropVals[T_MILEAGE].ulPropTag == taskProps.aulPropTag[T_MILEAGE])
        SetMileage(m_pPropVals[T_MILEAGE].Value.lpszW);

    if (m_pPropVals[T_BILLING].ulPropTag == taskProps.aulPropTag[T_BILLING])
        SetBillingInfo(m_pPropVals[T_BILLING].Value.lpszW);

    if (m_pPropVals[T_TASKFLAGDUEBY].ulPropTag == taskProps.aulPropTag[T_TASKFLAGDUEBY])
        SetTaskFlagDueBy(m_pPropVals[T_TASKFLAGDUEBY].Value.ft);

    if (m_pPropVals[T_PRIVATE].ulPropTag == taskProps.aulPropTag[T_PRIVATE])
        SetPrivate(m_pPropVals[T_PRIVATE].Value.b);

    SetPlainTextFileAndContent();
    SetHtmlFileAndContent();

    if (m_bHasAttachments)
    {
        if (FAILED(ExtractRealAttachments(0)))
            LOG_WARNING(_T("Could not extract attachments"));
    }

    if (m_bIsRecurring)
        SetRecurValues();

    return hr;
}

void MAPITask::SetRecurValues()
{
    LOGFN_TRACE_NO;

    Zimbra::Mapi::Task OlkTask(m_pMessage, NULL);
    Zimbra::Mapi::COutlookRecurrencePattern &refRecur = OlkTask.GetRecurrencePatternRef();
    HRESULT hr = refRecur.LoadRecurrence(NULL, NULL, TRUE);
    if (FAILED(hr))
        return;

    /*
    // Set Timezone info
    SYSTEMTIME stdTime;
    SYSTEMTIME dsTime;
    const Zimbra::Mail::TimeZone &tzone = recur.GetTimeZone();
    m_timezone.id = m_sTimezoneString;  // don't use m_timezone.id = tzone.GetId()
    IntToWstring(tzone.GetStandardOffset(), m_timezone.standardOffset);
    IntToWstring(tzone.GetDaylightOffset(), m_timezone.daylightOffset);
    tzone.GetStandardStart(stdTime);
    tzone.GetDaylightStart(dsTime);
    IntToWstring(stdTime.wDay, m_timezone.standardStartWeek);
    IntToWstring(stdTime.wDayOfWeek + 1, m_timezone.standardStartWeekday);  // note the + 1 -- bumping weekday
    IntToWstring(stdTime.wMonth, m_timezone.standardStartMonth);
    IntToWstring(stdTime.wHour, m_timezone.standardStartHour);
    IntToWstring(stdTime.wMinute, m_timezone.standardStartMinute);
    IntToWstring(stdTime.wSecond, m_timezone.standardStartSecond);
    IntToWstring(dsTime.wDay, m_timezone.daylightStartWeek);
    IntToWstring(dsTime.wDayOfWeek + 1, m_timezone.daylightStartWeekday);   // note the + 1 -- bumping weekday
    IntToWstring(dsTime.wMonth, m_timezone.daylightStartMonth);
    IntToWstring(dsTime.wHour, m_timezone.daylightStartHour);
    IntToWstring(dsTime.wMinute, m_timezone.daylightStartMinute);
    IntToWstring(dsTime.wSecond, m_timezone.daylightStartSecond);
    //
    */

    ULONG ulType = refRecur.GetRecurrenceType();
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

    IntToWstring(refRecur.GetInterval(), m_sRecurInterval);

    ULONG ulDayOfWeekMask = refRecur.GetDayOfWeekMask();
    if (ulDayOfWeekMask & wdmSunday)   m_sRecurWkday += L"SU";
    if (ulDayOfWeekMask & wdmMonday)   m_sRecurWkday += L"MO";
    if (ulDayOfWeekMask & wdmTuesday)  m_sRecurWkday += L"TU";
    if (ulDayOfWeekMask & wdmWednesday)m_sRecurWkday += L"WE";
    if (ulDayOfWeekMask & wdmThursday) m_sRecurWkday += L"TH";
    if (ulDayOfWeekMask & wdmFriday)   m_sRecurWkday += L"FR";
    if (ulDayOfWeekMask & wdmSaturday) m_sRecurWkday += L"SA";

    //if ((m_sRecurPattern == L"DAI") && (m_sRecurWkday.length() > 0))	// every weekday
        //m_sRecurPattern = L"WEE";

    if (m_sRecurPattern == L"MON")
    {
        if (ulType == oRecursMonthly)
            IntToWstring(refRecur.GetDayOfMonth(), m_sRecurDayOfMonth);
        else
        if (ulType == oRecursMonthNth)
        {
            ULONG ulMonthOccurrence = refRecur.GetInstance();
            if (ulMonthOccurrence == 5)	    // last
               m_sRecurMonthOccurrence = L"-1";
            else
                IntToWstring(ulMonthOccurrence,m_sRecurMonthOccurrence);
        }
    }

    if (m_sRecurPattern == L"YEA")
    {
        ULONG ulMonthOfYear = refRecur.GetMonthOfYear();
        IntToWstring(ulMonthOfYear,m_sRecurMonthOfYear);
        if (ulType == oRecursYearly)
            IntToWstring(refRecur.GetDayOfMonth(), m_sRecurDayOfMonth);
        else
        if (ulType == oRecursYearNth)
        {
            ULONG ulMonthOccurrence = refRecur.GetInstance();
            if (ulMonthOccurrence == 5)	    // last
               m_sRecurMonthOccurrence = L"-1";
            else
                IntToWstring(ulMonthOccurrence,m_sRecurMonthOccurrence);
        }
    }

    ULONG ulRecurrenceEndType = refRecur.GetEndType();
    Zimbra::Mapi::CRecurrenceTime rtEndDate = refRecur.GetEndDate();

    // TaskFilterDate
    Zimbra::Mapi::CFileTime ft = (FILETIME)rtEndDate;
    m_sTaskFilterDate = Zimbra::MAPI::Util::CommonDateString(ft);

    if (ulRecurrenceEndType == oetEndAfterN)
        IntToWstring(refRecur.GetOccurrences(), m_sRecurCount);
    else
    if (ulRecurrenceEndType == oetEndDate)
    {
        SYSTEMTIME st;
        FileTimeToSystemTime(&ft, &st);
        wstring temp = Zimbra::Util::FormatSystemTime(st, TRUE, TRUE);
       m_sRecurEndDate = temp.substr(0, 8);
    }
}

void MAPITask::SetSubject(LPTSTR pStr)
{
    LOGFN_TRACE_NO;
    m_sSubject = pStr;
}

void MAPITask::SetImportance(long importance)
{
    LOGFN_TRACE_NO;
    switch (importance)
    {
        case oImportanceLow:    m_sImportance = L"9";   break;
        case oImportanceNormal: m_sImportance = L"5";   break;
        case oImportanceHigh:   m_sImportance = L"1";   break;
        default:                m_sImportance = L"5";
    }
}

void MAPITask::SetTaskStatus(long taskstatus)
{
    LOGFN_TRACE_NO;
    switch (taskstatus)
    {
        case oTaskNotStarted:   m_sStatus = L"NEED";	    break;
        case oTaskInProgress:   m_sStatus = L"INPR";	    break;
        case oTaskComplete:     m_sStatus = L"COMP";	    break;
        case oTaskWaiting:      m_sStatus = L"WAITING";	    break;
        case oTaskDeferred:     m_sStatus = L"DEFERRED";    break;
        default:                m_sStatus = L"NEED";
    }
}

void MAPITask::SetPercentComplete(double percentcomplete)
{
    LOGFN_TRACE_NO;
    long lPercent = (long)(percentcomplete * 100);
    WCHAR pwszTemp[10];
    _ltow(lPercent, pwszTemp, 10);
    m_sPercentComplete = pwszTemp;
}

void MAPITask::SetTaskStart(FILETIME ft)
{
    LOGFN_TRACE_NO;
    SYSTEMTIME st;
    FileTimeToSystemTime(&ft, &st);
    m_sTaskStart = Zimbra::Util::FormatSystemTime(st, FALSE, FALSE);
    m_sTaskFilterDate = Zimbra::MAPI::Util::CommonDateString(m_pPropVals[T_TASKDUE].Value.ft);
}

void MAPITask::SetTaskDue(FILETIME ft)
{
    LOGFN_TRACE_NO;
    SYSTEMTIME st;
    FileTimeToSystemTime(&ft, &st);
    m_sTaskDue = Zimbra::Util::FormatSystemTime(st, FALSE, FALSE);
}

void MAPITask::SetTotalWork(long totalwork)
{
    LOGFN_TRACE_NO;
    WCHAR pwszTemp[10];
    _ltow(totalwork, pwszTemp, 10);
    m_sTotalWork = pwszTemp;
}

void MAPITask::SetActualWork(long actualwork)
{
    LOGFN_TRACE_NO;
    WCHAR pwszTemp[10];
    _ltow(actualwork, pwszTemp, 10);
    m_sActualWork = pwszTemp;
}

void MAPITask::SetCompanies(LPTSTR pStr)    // deal with more than one at some point
{
    LOGFN_TRACE_NO;
    m_sCompanies = pStr;
}

void MAPITask::SetMileage(LPTSTR pStr)
{
    LOGFN_TRACE_NO;
    m_sMileage = pStr;
}

void MAPITask::SetBillingInfo(LPTSTR pStr)
{
    LOGFN_TRACE_NO;
    m_sBillingInfo = pStr;
}

void MAPITask::SetTaskFlagDueBy(FILETIME ft)
{
    LOGFN_TRACE_NO;
    SYSTEMTIME st;
    FileTimeToSystemTime(&ft, &st);
    m_sTaskFlagDueBy = (st.wYear == 1601) ? L"" : Zimbra::Util::FormatSystemTime(st, TRUE, TRUE);
}

void MAPITask::SetPrivate(unsigned short usPrivate)
{
    LOGFN_TRACE_NO;
    m_sPrivate = (usPrivate == 1) ? L"1" : L"0";
}

void MAPITask::SetPlainTextFileAndContent()
{
    LOGFN_TRACE_NO;
    m_sPlainTextFile = Zimbra::MAPI::Util::SetPlainText(m_pMessage, &m_pPropVals[T_BODY]);
}

void MAPITask::SetHtmlFileAndContent()
{
    LOGFN_TRACE_NO;
    m_sHtmlFile = Zimbra::MAPI::Util::SetHtml(m_pMessage, &m_pPropVals[T_HTMLBODY]);
}

bool    MAPITask::IsTaskReminderSet()           { return m_bIsTaskReminderSet; }
wstring MAPITask::GetSubject()                  { return m_sSubject; }
wstring MAPITask::GetImportance()               { return m_sImportance; }
wstring MAPITask::GetTaskStatus()               { return m_sStatus; }
wstring MAPITask::GetPercentComplete()          { return m_sPercentComplete; }
wstring MAPITask::GetTaskStart()                { return m_sTaskStart; }
wstring MAPITask::GetTaskFilterDate()           { return m_sTaskFilterDate; }
wstring MAPITask::GetTaskDue()                  { return m_sTaskDue; }
wstring MAPITask::GetTotalWork()                { return m_sTotalWork; }
wstring MAPITask::GetActualWork()               { return m_sActualWork; }
wstring MAPITask::GetMileage()                  { return m_sMileage; }
wstring MAPITask::GetCompanies()                { return m_sCompanies; }
wstring MAPITask::GetBillingInfo()              { return m_sBillingInfo; }
wstring MAPITask::GetTaskFlagDueBy()            { return m_sTaskFlagDueBy; }
wstring MAPITask::GetPrivate()                  { return m_sPrivate; }
wstring MAPITask::GetPlainTextFileAndContent()  { return m_sPlainTextFile; }
wstring MAPITask::GetHtmlFileAndContent()       { return m_sHtmlFile; }

