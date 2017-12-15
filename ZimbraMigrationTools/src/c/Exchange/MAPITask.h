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

// MAPITaskException class
class MAPITaskException: public GenericException
{
public:
    MAPITaskException(HRESULT hrErrCode, LPCWSTR lpszDescription);
    MAPITaskException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile);
    virtual ~MAPITaskException() {}
};

// MAPITask class
class MAPITask : public MAPIRfc2445
{
private:
    bool m_bIsTaskReminderSet;

    // task data members (represented both by regular and named props)
    wstring m_sSubject;
    wstring m_sImportance;
    wstring m_sTaskStart;
    wstring m_sTaskFilterDate;
    wstring m_sTaskDue;
    wstring m_sStatus;
    wstring m_sPercentComplete;
    wstring m_sTotalWork;
    wstring m_sActualWork;
    wstring m_sCompanies;
    wstring m_sMileage;
    wstring m_sBillingInfo;
    wstring m_sTaskFlagDueBy;
    wstring m_sPrivate;
    wstring m_sPlainTextFile;
    wstring m_sHtmlFile;

    // prop tags for named properties
    ULONG m_PR_IS_RECURRING;
    ULONG m_PR_STATUS;
    ULONG m_PR_PERCENT_COMPLETE;
    ULONG m_PR_TASK_START;
    ULONG m_PR_TASK_DUE;
    ULONG m_PR_TOTAL_WORK;
    ULONG m_PR_ACTUAL_WORK;
    ULONG m_PR_COMPANIES;
    ULONG m_PR_MILEAGE;
    ULONG m_PR_BILLING_INFO;
    ULONG m_PR_REMINDER_SET;
    ULONG m_PR_TASK_FLAG_DUE_BY;
    ULONG m_PR_PRIVATE;

    // index of props
    typedef enum _TaskPropIdx
    {
        N_ISRECURT, 
        N_STATUS, 
        N_PERCENTCOMPLETE, 
        N_TASKSTART, 
        N_TASKDUE, 
        N_TOTALWORK, 
        N_ACTUALWORK, 
        N_NUMTASKPROPS
    } TaskPropIdx;
    LONG nameIds[N_NUMTASKPROPS];

    typedef enum _CommonTPropIdx
    {
        N_COMPANIES, 
        N_MILEAGE, 
        N_BILLING, 
        N_TASKREMINDERSET, 
        N_TASKFLAGDUEBY, 
        N_TPRIVATE, 
        N_NUMCOMMONTPROPS
    } CommonTPropIdx;
    LONG nameIdsC[N_NUMCOMMONTPROPS];

    // this enum lists all the props
    enum
    {
        T_MESSAGE_FLAGS, 
        T_SUBJECT, 
        T_BODY, 
        T_HTMLBODY, 
        T_IMPORTANCE, 
        T_ISRECURT, 
        T_STATUS, 
        T_PERCENTCOMPLETE,
        T_TASKSTART, 
        T_TASKDUE, 
        T_TOTALWORK, 
        T_ACTUALWORK, 
        T_COMPANIES, 
        T_MILEAGE, 
        T_BILLING, 
        T_TASKREMINDERSET,
        T_TASKFLAGDUEBY, 
        T_PRIVATE, 
        T_NUMALLTASKPROPS
    };


public:
    MAPITask(Zimbra::MAPI::MAPISession &session, Zimbra::MAPI::MAPIMessage &mMessage);
    ~MAPITask();

    HRESULT InitNamedPropsForTask();
    HRESULT SetMAPITaskValues();
    void SetSubject(LPTSTR pStr);
    void SetImportance(long importance);
    void SetTaskStatus(long taskstatus);
    void SetPercentComplete(double percentcomplete);
    void SetTaskStart(FILETIME ft);
    void SetTaskDue(FILETIME ft);
    void SetTotalWork(long totalwork);
    void SetActualWork(long actualwork);
    void SetCompanies(LPTSTR pStr);
    void SetMileage(LPTSTR pStr);
    void SetBillingInfo(LPTSTR pStr);
    void SetTaskFlagDueBy(FILETIME ft);
    void SetPrivate(unsigned short usPrivate);
    void SetPlainTextFileAndContent();
    void SetHtmlFileAndContent();
    void SetRecurValues();
    bool IsTaskReminderSet();

    wstring GetSubject();
    wstring GetImportance();
    wstring GetTaskStatus();
    wstring GetPercentComplete();
    wstring GetTaskStart();
    wstring GetTaskFilterDate();
    wstring GetTaskDue();
    wstring GetTotalWork();
    wstring GetActualWork();
    wstring GetMileage();
    wstring GetCompanies();
    wstring GetBillingInfo();
    wstring GetTaskFlagDueBy();
    wstring GetPrivate();
    wstring GetPlainTextFileAndContent();
    wstring GetHtmlFileAndContent();
};
