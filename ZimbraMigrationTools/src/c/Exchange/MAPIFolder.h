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

#include "optimize.h"

// use this flag on OpenMsgStore to force cached mode connections
// to read remote data and not local data
#define MDB_ONLINE              ((ULONG)0x00000100)

#define GLOBAL_PROFILE_SECTION_GUID  "\x13\xDB\xB0\xC8\xAA\x05\x10\x1A\x9B\xB0\x00\xAA\x00\x2F\xC4\x5A"
DEFINE_OLEGUID(PSETID_COMMON, MAKELONG(0x2000 + (8), 0x0006), 0, 0);

// A named property which specifies whether the mail is
// completely downloaded or in header only form in case of IMAP
#define DISPID_HEADER_ITEM      0x8578

#define CONST_ROOTFOLDERNAME	  L"RootFolderItems"

namespace Zimbra
{
namespace MAPI
{
class MAPIFolderException: public GenericException
{
public:
    MAPIFolderException(HRESULT hrErrCode, LPCWSTR lpszDescription);
    MAPIFolderException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile);
    virtual ~MAPIFolderException() {}
};

class MAPIFolder;

// Folder Iterator class
class FolderIterator: public MAPITableIterator
{
public:
    FolderIterator();
    ~FolderIterator();

    virtual LPSPropTagArray GetTableProps();
    virtual LPSSortOrderSet GetSortOrder() { return NULL; }
    virtual LPSRestriction GetRestriction(ULONG TypeMask, FILETIME startDate)
    {
        UNREFERENCED_PARAMETER(TypeMask);
        UNREFERENCED_PARAMETER(startDate);
        return NULL;
    }

    BOOL GetNext(MAPIFolder &folder);

private:
    typedef enum _FolderIterPropTagIdx
    {
        FI_DISPLAY_NAME, 
        FI_ENTRYID, 
        FI_PR_LONGTERM_ENTRYID_FROM_TABLE, 
        FI_FLAGS, 
        FI_CONTAINER_CLASS,
        FI_ATTR_HIDDEN,
        FI_CONTENT_COUNT,
        FI_SUBFOLDERS,

        NFOLDERPROPS
    } FolderIterPropTagIdx;

    typedef struct _FolderIterPropTags
    {
        ULONG cValues;
        ULONG aulPropTags[NFOLDERPROPS];
    } FolderIterPropTags;

protected:
    static FolderIterPropTags m_TableProps;
};

// Exchange System folder enumeration
// These are indices into MAPIStore::m_specialFolderIds
typedef enum _ExchangeSpecialFolderId
{
    EXSFID_INBOX                   = 0, 
    EXSFID_IPM_SUBTREE             = 1, 
    EXSFID_CALENDAR                = 2, 
    EXSFID_CONTACTS                = 3, 
    EXSFID_DRAFTS                  = 4, 
    EXSFID_JOURNAL                 = 5, 
    EXSFID_NOTE                    = 6,
    EXSFID_TASK                    = 7, 
    EXSFID_OUTBOX                  = 8, 
    EXSFID_SENTMAIL                = 9, 
    EXSFID_TRASH                   = 10, 
    EXSFID_SYNC_CONFLICTS          = 11,
    EXSFID_SYNC_ISSUES             = 12, 
    EXSFID_SYNC_LOCAL_FAILURES     = 13, 
    EXSFID_SYNC_SERVER_FAILURES    = 14,
    EXSFID_JUNK_MAIL               = 15, 
    EXSFID_SUGGESTED_CONTACTS      = 16,
    TOTAL_NUM_SPECIAL_FOLDERS      = 17, 
    EXSFID_NONE                    = 1000

} ExchangeSpecialFolderId;

// Zimbra system folder enumeration
typedef enum _ZimbraSpecialFolderId
{
    ZM_SFID_NONE        = 0, 
    ZM_ROOT             = 1, 
    ZM_INBOX,           // 2
    ZM_TRASH,           // 3
    ZM_SPAM,            // 4
    ZM_SENT_MAIL,       // 5
    ZM_DRAFTS,          // 6
    ZM_CONTACTS,        // 7
    ZM_TAGS,            // 8
    ZM_CONVERSATIONS,   // 9
    ZM_CALENDAR,        // 10
    ZM_MAILBOX_ROOT,    // 11
    ZM_WIKI,            // 12 
    ZM_EMAILEDCONTACTS, // 13
    ZM_CHATS,           // 14
    ZM_TASKS,           // 15
    
    ZM_UNSUPPORTED_OUTBOX,      // 16
    ZM_UNSUPPORTED_SYNC_ISSUES, // 17
    ZM_UNSUPPORTED_NOTES,       // 18

    ZM_SFID_MAX         // 19

} ZimbraSpecialFolderId;

// MapiFolder class
class MAPIFolder:public Zimbra::Util::ZimObj
{
public:
    MAPIFolder();
    MAPIFolder(MAPISession &session, MAPIStore &store, const wstring& sParentFolderPath);
    ~MAPIFolder();

    void InitMAPIFolder(LPMAPIFOLDER pFolder, LPCTSTR displayName, LPSBinary pEntryId, const wstring& sContainerClass, bool bHidden, bool bSubfolders, ULONG ulContentCount);

    HRESULT GetMessageIterator(MessageIterator &msgIterator);
    HRESULT GetFolderIterator(FolderIterator &folderIter);

    HRESULT GetItemCount(ULONG &ulCount);
    ExchangeSpecialFolderId GetExchangeSpecialFolderId();
    ZimbraSpecialFolderId GetZimbraSFIdFromExchSFId(ExchangeSpecialFolderId exsfid);

    bool HiddenFolder();
    bool HasSubfolders();

    HRESULT ContainerClass(wstring &wstrContainerClass);
    void GetRestrictedContentsTable();

    wstring GetFolderPath() { return m_sFolderpath; }
    wstring DispName() { return m_sDisplayName; }
    SBinary EntryID() { return m_EntryID; }

    wstring EscapeInvalidChar(const wstring& s, const char c);

private:
    void CalcFolderPath();

private:
    // Backpointers
    MAPISession *m_pMapiSession;
    MAPIStore *m_pStore;

    // Inner folder and its tables
    LPMAPIFOLDER m_pMAPIFolder;
    LPMAPITABLE m_pContentsTable;
    LPMAPITABLE m_pHierarchyTable;

    wstring m_sParentFolderPath;

    wstring m_sDisplayName;
    SBinary m_EntryID;
    wstring m_sFolderpath;
    wstring m_sContainerClass;
    bool    m_bHidden;
    ULONG   m_ulContentCount;
    bool    m_bSubfolders;
};

// global declaration
static ULONG g_ulIMAPHeaderInfoPropTag = PR_NULL;

} // namespace MAPI
} // namespace Zimbra
