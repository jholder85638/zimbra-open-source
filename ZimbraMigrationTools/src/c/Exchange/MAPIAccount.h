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
#include "MAPIRules.h"

namespace Zimbra
{
namespace MAPI
{

typedef struct _Folder_Data
{
    wstring         name;
    SBinary         sbin;
    wstring         folderpath;
    wstring         containerclass;
    long            zimbraspecialfolderid;
    unsigned long   itemcount;
    ULONG           ulDepth;
} Folder_Data;

typedef struct _Item_Data
{
    SBinary         sbMessageID;
    long            lItemType;
    ULONG           ulMessageSize;
    __int64         i64SubmitDate;   // probably obsoleted by FilterDate
    string          strMsgEntryId;
    wstring         strSubject;
    wstring         strFilterDate;
} Item_Data;

typedef struct _MessagePart
{
    wstring contentType;
    wstring content;
} MessagePart;

// base item data
typedef struct _BaseItemData
{
    __int64 MessageDate;
} BaseItemData;

// folders to skip
enum
{
    TS_OUTBOX = 0, TS_SYNC_CONFLICTS, TS_SYNC_ISSUES, TS_SYNC_LOCAL_FAILURES, TS_SYNC_SERVER_FAILURES, TS_FOLDERS_MAX
};


// contact item data
typedef struct _ContactItemData: BaseItemData
{
    wstring AssistantPhone;
    wstring CallbackPhone;
    wstring CarPhone;
    wstring Company;
    wstring CompanyPhone;
    wstring Department;
    wstring Email1;
    wstring Email2;
    wstring Email3;
    wstring FileAs;
    wstring FirstName;
    wstring HomeCity;
    wstring HomeCountry;
    wstring HomeFax;
    wstring HomePhone;
    wstring HomePhone2;
    wstring HomePostalCode;
    wstring HomeState;
    wstring HomeStreet;
    wstring HomeURL;
    wstring JobTitle;
    wstring LastName;
    wstring MiddleName;
    wstring MobilePhone;
    wstring NamePrefix;
    wstring NameSuffix;
    wstring Notes;
    wstring OtherCity;
    wstring OtherCountry;
    wstring OtherFax;
    wstring OtherPhone;
    wstring OtherPostalCode;
    wstring OtherState;
    wstring OtherStreet;
    wstring OtherURL;
    wstring Pager;
    wstring PrimaryPhone;
    wstring WorkCity;
    wstring WorkCountry;
    wstring WorkFax;
    wstring WorkPhone;
    wstring WorkPhone2;
    wstring WorkPostalCode;
    wstring WorkState;
    wstring WorkStreet;
    wstring WorkURL;
    wstring Birthday;
    wstring UserField1;
    wstring UserField2;
    wstring UserField3;
    wstring UserField4;
    wstring NickName;
    wstring pDList;
    wstring Type;
    wstring PictureID;
    wstring IMAddress1;
    wstring Anniversary;
    wstring ContactImagePath;
    wstring ImageContenttype;
    wstring ImageContentdisp;
    vector<ContactUDFields> UserDefinedFields;
    vector<LPWSTR>* vTags;
} ContactItemData;

typedef struct
{
    LPTSTR buffer;
    unsigned long size;
} data_buffer;

typedef struct _MessageItemData: BaseItemData
{
    wstring Subject;
    bool IsFlagged;
    wstring Urlname;
    bool IsDraft;
    bool IsFromMe;
    bool IsUnread;
    bool IsForwarded;
    bool RepliedTo;
    bool HasAttachments;
    bool IsUnsent;
    bool HasHtml;
    bool HasText;
    __int64 deliveryDate;
    wstring DeliveryDateString;
    wstring DeliveryUnixString;
    __int64 Date;
    wstring DateUnixString;
    wstring DateString;
    vector<LPWSTR>* vTags;
    data_buffer textbody;
    data_buffer htmlbody;

    DWORD dwMimeSize;
    wstring MimeFile;
    LPWSTR pwsMimeBuffer;
} MessageItemData;

typedef struct _ApptItemData: BaseItemData
{
    wstring Subject;
    wstring Name;
    wstring Location;
    wstring Uid;
    wstring PartStat;
    wstring CurrStat;
    wstring FreeBusy;
    wstring Transparency;
    wstring AllDay;
    wstring StartDate;
    wstring CalFilterDate;
    wstring EndDate;
    wstring ApptClass;
    wstring AlarmTrigger;
    wstring RSVP;
    Organizer organizer;

    TzStrings tzLegacy;
    TzStrings tzStart;
    TzStrings tzEnd;

    vector<Attendee*> vAttendees;
    vector<AttachmentInfo*> vAttachments;
    vector<MessagePart> vMessageParts;
    vector<LPWSTR>* vTags;

    // recurrence stuff
    wstring recurPattern;
    wstring recurInterval;
    wstring recurWkday;
    wstring recurEndType;
    wstring recurCount;
    wstring recurEndDate;
    wstring recurDayOfMonth;
    wstring recurMonthOccurrence;
    wstring recurMonthOfYear;
    vector<MAPIAppointment*> vExceptions;

    // exception
    wstring ExceptionType;

    //data_buffer textbody;
    //data_buffer htmlbody;
} ApptItemData;

typedef struct _TaskItemData: BaseItemData
{
    wstring Subject;
    wstring Importance;
    wstring TaskStart;
    wstring TaskFilterDate;
    wstring TaskDue;
    wstring Status;
    wstring PercentComplete;
    wstring TotalWork;
    wstring ActualWork;
    wstring Companies;
    wstring Mileage;
    wstring BillingInfo;
    wstring TaskFlagDueBy;
    wstring ApptClass;
    vector<AttachmentInfo*> vAttachments;
    vector<MessagePart> vMessageParts;
    vector<LPWSTR>* vTags;

    // DCB Note that tasks lack Tz member - I think they need it!
    //TzStrings tzLegacy;
    //TzStrings tzStart;
    //TzStrings tzEnd;

    // recurrence stuff
    wstring recurPattern;
    wstring recurInterval;
    wstring recurWkday;
    wstring recurEndType;
    wstring recurCount;
    wstring recurEndDate;
    wstring recurDayOfMonth;
    wstring recurMonthOccurrence;
    wstring recurMonthOfYear;
} TaskItemData;

// ====================================================================================
// CMapiAccount
// ====================================================================================
//
// Provides C# layer with access to MAPI account items. C# accesses this via COMMapiAccount
//
// Typical calls:
// - CMapiAccount ctor
// - InitGlobalSessionAndStore()
// - InitMapiAccount() <- logs onto store and gets root folder
//
// - GetRootFolderHierarchy() <- returns vector<Folder_Data> (name, EID, folderpath, zimID, itemcount)
//
//

class CMapiAccount
{
public:
    CMapiAccount(wstring sSrcAccount, wstring sZCSAccount);
    ~CMapiAccount();

    LPCWSTR LogonAndGetRootFolder();
    LPCWSTR GetRootFolderHierarchy(vector<Folder_Data> &vfolderlist);
    LPCWSTR GetFolderItems(SBinary sbFolderEID, vector<Item_Data> &ItemList);
    LPCWSTR GetItem(SBinary sbItemEID, BaseItemData &itemData);

    // OOO
    LPWSTR  GetOOOStateAndMsg();

    // Rules
    LPCWSTR GetExchangeRules(vector<CRule> &vRuleList);

    // Public folders
    LPCWSTR InitializePublicFolders();
    HRESULT EnumeratePublicFolders(std::vector<std::string> &pubFldrList);

private:
    LPCWSTR OpenUserStore();
    LPCWSTR OpenPublicStore();

    LPCWSTR _LogonAndGetRootFolder();
    LPCWSTR _GetRootFolderHierarchy(vector<Folder_Data> &vfolderlist);
    LPCWSTR _GetFolderItems(SBinary sbFolderEID, vector<Item_Data> &ItemList);
    LPCWSTR _GetItem(SBinary sbItemEID, BaseItemData &itemData);

    HRESULT CacheFolderDataAndIterateChildFoldersDepthFirst(Zimbra::MAPI::MAPIFolder &folder, vector<Folder_Data> &fd, ULONG& dwDepth);
    void MAPIFolder2FolderData(Zimbra::MAPI::MAPIFolder *folder, Folder_Data &flderdata);

    static LONG WINAPI UnhandledExceptionFilter(LPEXCEPTION_POINTERS pExPtrs);

    static void SetOOMRegistry();
    static void ResetOOMRegistry();

    HRESULT GetInternalFolder(SBinary sbFolderEID, MAPIFolder &folder);

    /*
    // OBSOLETE?
    // void traverse_folder(Zimbra::MAPI::MAPIFolder &folder);
    */

private:
    std::wstring m_sSrcAccount;
    std::wstring m_sZCSAccount;

    ExchangeSpecialFolderId m_aFoldersToSkip[TS_FOLDERS_MAX];

    Zimbra::MAPI::MAPIStore   *m_pUserStore;
    Zimbra::MAPI::MAPIFolder  *m_pRootFolder;
    Zimbra::MAPI::MAPIStore   *m_pPublicStore;

};
}
}
