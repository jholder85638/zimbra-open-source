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
// Exception class
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
MAPIFolderException::MAPIFolderException(HRESULT hrErrCode, LPCWSTR lpszDescription): 
    GenericException(hrErrCode, lpszDescription)
{
    //
}

MAPIFolderException::MAPIFolderException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile): 
    GenericException(hrErrCode, lpszDescription, lpszShortDescription, nLine, strFile)
{
    //
}

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// FolderIterator
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
FolderIterator::FolderIterator()
{
    m_pParentFolder = NULL;
}

FolderIterator::~FolderIterator() {}

FolderIterator::FolderIterPropTags FolderIterator::m_TableProps = {
    NFOLDERPROPS, { PR_DISPLAY_NAME, 
                    PR_ENTRYID, 
                    PR_LONGTERM_ENTRYID_FROM_TABLE,
                    PR_FOLDER_FLAGS,
                    PR_CONTAINER_CLASS,
                    PR_ATTR_HIDDEN,
                    PR_CONTENT_COUNT,
                    PR_SUBFOLDERS
    }
};

LPSPropTagArray FolderIterator::GetTableProps()
{
    return (LPSPropTagArray) & m_TableProps;
}

BOOL FolderIterator::GetNext(MAPIFolder &folder)
{
    LOGFN_TRACE;

    // ----------------------------------------
    // Get next "normal' table row
    // ----------------------------------------
    SRow *pRow;
    do
    {
        pRow = MAPITableIterator::GetNext();
        if (pRow == NULL)
            return FALSE;
    }
    while ((pRow->lpProps[FI_FLAGS].Value.l & MDB_FOLDER_NORMAL) == 0);

    /*
    #ifdef _DEBUG
        LOG_GEN(_T(" "));
        LOG_GEN(_T("------------------------"));
        LOG_GEN(_T("Row props"));
        LOG_GEN(_T("------------------------"));
        DumpPropValues(LOGLEVEL_CMNT_GEN, pRow->cValues, pRow->lpProps);
        LOG_GEN(_T(" "));
        LOG_GEN(_T(" "));
    #endif
    */


    // ----------------------------------------
    // OpenEntry() it
    // ----------------------------------------
    LPMAPIFOLDER pFolder = NULL;
    HRESULT hr = S_OK;
    ULONG objtype;
    ULONG cb = pRow->lpProps[FI_ENTRYID].Value.bin.cb;
    LPENTRYID peid = (LPENTRYID)(pRow->lpProps[FI_ENTRYID].Value.bin.lpb);
    if ((hr = m_pParentFolder->OpenEntry(cb, peid, NULL, MAPI_BEST_ACCESS, &objtype, (LPUNKNOWN *)&pFolder)) != S_OK)
        throw GenericException(hr, L"FolderIterator::GetNext():OpenEntry Failed.",  ERR_GET_NEXT, __LINE__, __FILE__);

    // ----------------------------------------
    // Read some props
    // ----------------------------------------
    wstring sDisplayName;
    if (PROP_TYPE(pRow->lpProps[FI_DISPLAY_NAME].ulPropTag) != PT_ERROR)
        sDisplayName = pRow->lpProps[FI_DISPLAY_NAME].Value.LPSZ;

    DWORD cNeedGetProps = 0;
    //bool bFetchContainerClassFromOpenObj = false;
    wstring sContainerClass;
    if (PROP_TYPE(pRow->lpProps[FI_CONTAINER_CLASS].ulPropTag) != PT_ERROR)
        sContainerClass = pRow->lpProps[FI_CONTAINER_CLASS].Value.LPSZ;
    else
    {
        //bFetchContainerClassFromOpenObj = true;
        //cNeedGetProps++;
        LOG_INFO(_T("PR_CONTAINER_CLASS unavailable in row for '%s'"), sDisplayName.c_str());
    }

    //BOOL bFetchAttrHiddenFromOpenObj = false;
    bool bAttrHidden = false;
    if (PROP_TYPE(pRow->lpProps[FI_ATTR_HIDDEN].ulPropTag) != PT_ERROR)
        bAttrHidden = pRow->lpProps[FI_ATTR_HIDDEN].Value.b==1;
    {
        //bFetchAttrHiddenFromOpenObj = true;
        //cNeedGetProps++;
        LOG_INFO(_T("PR_ATTR_HIDDEN unavailable in row for '%s'"), sDisplayName.c_str());
    }

    //BOOL bFetchSubfoldersFromOpenObj = false;
    bool bSubfolders = true;
    if (PROP_TYPE(pRow->lpProps[FI_SUBFOLDERS].ulPropTag) != PT_ERROR)
        bSubfolders = pRow->lpProps[FI_SUBFOLDERS].Value.b==1;
    {
        //bFetchSubfoldersFromOpenObj = true;
        //cNeedGetProps++;
        LOG_INFO(_T("PR_SUBFOLDERS unavailable in row for '%s'"), sDisplayName.c_str());
    }

    BOOL bFetchContentCountFromOpenObj = false;
    ULONG ulContentCount = 0;
    if (PROP_TYPE(pRow->lpProps[FI_CONTENT_COUNT].ulPropTag) != PT_ERROR)
        ulContentCount = pRow->lpProps[FI_CONTENT_COUNT].Value.ul;
    else
    {
        bFetchContentCountFromOpenObj = true;
        cNeedGetProps++;
        LOG_INFO(_T("PR_CONTENT_COUNT unavailable in row for '%s'"), sDisplayName.c_str());
    }


    // ----------------------------------------
    // DCB_BUG_100347 
    // ----------------------------------------
    // Some props missing in PUBLIC FOLDERS hierarchy table -> get them via GetProps()
    // BUG_100347 is only concerned with missing PR_CONTENT_COUNT, but while working on this I spotted
    // some other props that are missing too so extended this to a general mechanism for handling any
    // of the missing props. For now though, I'm leaving other than PR_CONTENT_COUNT commented out as we're 
    // close to 8.7 release and I don't want to risk breaking anything.
    if (cNeedGetProps)
    {
        LOG_INFO(_T("%d missing prop(s) in hierarchy table row for '%s'-> using GetProps to retrieve them"), cNeedGetProps, sDisplayName.c_str());

        enum { FCONTAINER_CLASS, FATTR_HIDDEN, FSUBFOLDERS, FCONTENT_COUNT, FNPROPS };
        SizedSPropTagArray(FNPROPS, rgProps) = {FNPROPS, {PR_CONTAINER_CLASS, PR_ATTR_HIDDEN, PR_SUBFOLDERS, PR_CONTENT_COUNT}};
        ULONG cValues = 0;
        LPSPropValue lpProps = 0;
        hr = pFolder->GetProps((LPSPropTagArray)& rgProps, 0, &cValues, &lpProps);
        if (SUCCEEDED(hr))
        {
            /*
            if (bFetchContainerClassFromOpenObj)
            {
                if (PROP_TYPE(lpProps[FCONTAINER_CLASS].ulPropTag) != PT_ERROR)
                    sContainerClass = lpProps[FCONTAINER_CLASS].Value.LPSZ;
                else
                    LOG_INFO(_T("PR_CONTAINER_CLASS not found even after opening object"));
            }

            if (bFetchAttrHiddenFromOpenObj)
            {
                if (PROP_TYPE(lpProps[FATTR_HIDDEN].ulPropTag) != PT_ERROR)
                    bAttrHidden = lpProps[FATTR_HIDDEN].Value.b==1;
                else
                    LOG_INFO(_T("PR_ATTR_HIDDEN not found even after opening object"));
            }

            if (bFetchSubfoldersFromOpenObj)
            {
                if (PROP_TYPE(lpProps[FSUBFOLDERS].ulPropTag) != PT_ERROR)
                    bSubfolders = lpProps[FSUBFOLDERS].Value.b==1;
                else
                    LOG_INFO(_T("PR_SUBFOLDERS not found even after opening object"));
            }
            */

            if (bFetchContentCountFromOpenObj)
            {
                if (PROP_TYPE(lpProps[FCONTENT_COUNT].ulPropTag) != PT_ERROR)
                    ulContentCount = lpProps[FCONTENT_COUNT].Value.ul;
                else
                    LOG_INFO(_T("PR_CONTENT_COUNT not found even after opening object"));
            }
        }
    }

    // ----------------------------------------
    // Init folder
    // ----------------------------------------
    folder.InitMAPIFolder(pFolder, pRow->lpProps[FI_DISPLAY_NAME].Value.LPSZ, &(pRow->lpProps[FI_ENTRYID].Value.bin), sContainerClass, bAttrHidden, bSubfolders, ulContentCount);

    return TRUE;
}

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// MAPIFolder
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
MAPIFolder::MAPIFolder(): 
    m_pMAPIFolder(NULL), 
    m_pMapiSession(NULL), 
    m_pStore(NULL),
    m_bHidden(false), 
    m_bSubfolders(false), 
    m_ulContentCount(0)
{
    LOGFN_VERBOSE;
    m_EntryID.cb = 0;
    m_EntryID.lpb = NULL;
    m_pContentsTable = NULL;
    m_pHierarchyTable = NULL;
}

MAPIFolder::MAPIFolder(MAPISession &session, MAPIStore &store, const wstring& sParentFolderPath): 
    m_pMAPIFolder(NULL), 
    m_bHidden(false), 
    m_bSubfolders(false), 
    m_ulContentCount(0),
    m_sParentFolderPath(sParentFolderPath)
{
    LOGFN_VERBOSE;
    m_EntryID.cb = 0;
    m_EntryID.lpb = NULL;
    m_pContentsTable = NULL;
    m_pHierarchyTable=NULL;
    m_pMapiSession = &session;
    m_pStore = &store;

    LOG_VERBOSE(_T("Caller passed in ParentFolderPath '%s'"), m_sParentFolderPath.c_str());
    if (m_sParentFolderPath == L"/MAPIRoot/RootFolderItems")
        m_sParentFolderPath = L"/MAPIRoot";
}

MAPIFolder::~MAPIFolder()
{
    LOGFN_VERBOSE;

    if (m_pMAPIFolder != NULL)
    {
        UlRelease(m_pMAPIFolder);
        m_pMAPIFolder = NULL;
    }

    if (m_EntryID.lpb != NULL)
    {
        MAPIFreeBuffer(m_EntryID.lpb);
        m_EntryID.lpb = NULL;
        m_EntryID.cb = 0;
    }

    if (m_pContentsTable != NULL)
    {
        UlRelease(m_pContentsTable);
        m_pContentsTable = NULL;
    }

    if (m_pHierarchyTable!=NULL)
    {
        UlRelease(m_pHierarchyTable);
        m_pHierarchyTable = NULL;
    }
}

void MAPIFolder::GetRestrictedContentsTable()
{
    LOGFN_TRACE;

    if (m_pContentsTable)
    {
        // Already got it
        _ASSERT(FALSE);
        return; 
    }

    HRESULT hr = S_OK;

    // ------------------------------------------------------
    // Get folder's non-assoc contents table
    // ------------------------------------------------------
    LOG_TRACE(_T("Getting ContentsTable..."));
    try
    {
        if (FAILED(hr = m_pMAPIFolder->GetContentsTable(fMapiUnicode, &m_pContentsTable)))
            throw MAPIFolderException(hr, L"GetRestrictedContentsTable(): GetContentsTable Failed.", ERR_MAPI_FOLDER, __LINE__, __FILE__);
    }
    catch(...)
    {
        throw MAPIFolderException(hr, L"GetRestrictedContentsTable(): GetContentsTable Failed.", ERR_MAPI_FOLDER, __LINE__, __FILE__);
    }

    // ------------------------------------------------------
    // Apply restriction
    // ------------------------------------------------------
    // DCB - currently this is restriction is no restricting very much at all
    // In particular its not filtering on start date or item type
    // I've raised BUG 99724 to sort this

    LOG_TRACE(_T("Restricting...")); // DCB_PERFORMANCE Why? Its not reqd yet. Particularly wasteful for folder pass? No - reqd for folder counts
    Zimbra::MAPI::MessageIteratorRestriction restriction;

    ULONG ulItemMask = ZCM_ALL;       // Setting these ditches the complex restriction set up in MessageIteratorRestriction() ctor
    FILETIME ftStartDate = { 0, 0 };
    LPSRestriction pRestriction = restriction.GetRestriction(ulItemMask, ftStartDate);
    #ifdef _DEBUG
        DumpRestriction(pRestriction, LOGLEVEL_CMNT_GEN);
    #endif


    if (FAILED(hr = m_pContentsTable->Restrict(pRestriction, 0)))
        throw MAPIFolderException(hr, L"MAPIFolder::GetRestrictedContentsTable():Restrict Failed.", ERR_MAPI_FOLDER, __LINE__, __FILE__);
}

void MAPIFolder::InitMAPIFolder(LPMAPIFOLDER pMAPIFolder, LPCTSTR displayName, LPSBinary pEntryId, const wstring& sContainerClass, bool bHidden, bool bSubfolders, ULONG ulContentCount)
{
    m_sObjectID = displayName;
    LOGFN_TRACE;

    // ------------------------------------------------------
    // Free any existing data - don't think there can be any
    // ------------------------------------------------------
    if (m_pMAPIFolder != NULL)
        UlRelease(m_pMAPIFolder);
    if (m_EntryID.lpb != NULL)
        FreeEntryID(m_EntryID);
    if(m_pContentsTable != NULL)
        UlRelease(m_pContentsTable);
    if(m_pHierarchyTable!=NULL)
        UlRelease(m_pHierarchyTable);

    // ------------------------------------------------------
    // Retain ptr to inner folder
    // ------------------------------------------------------
    m_pMAPIFolder = pMAPIFolder;

    // ------------------------------------------------------
    // Set display name
    // ------------------------------------------------------
    m_sDisplayName = displayName;    

    // ------------------------------------------------------
    // EntryID
    // ------------------------------------------------------
    CopyEntryID(*pEntryId, m_EntryID);

    // ------------------------------------------------------
    // Cache props from table rows
    // ------------------------------------------------------
    m_sContainerClass = sContainerClass;
    m_bHidden         = bHidden;
    m_bSubfolders     = bSubfolders;
    m_ulContentCount  = ulContentCount;

    // ------------------------------------------------------
    // Store folder-path
    // ------------------------------------------------------
    if (m_pMapiSession)
        CalcFolderPath();
}


ExchangeSpecialFolderId MAPIFolder::GetExchangeSpecialFolderId()
{
    LOGFN_VERBOSE;
    ExchangeSpecialFolderId exsfid = EXSFID_NONE;

    if (m_pStore && m_pMapiSession)
    {
        // The store cached SF EIDS when it instantiated -> get them from there
        SBinaryArray specialFolderIds = m_pStore->GetSpecialFolderIds();

        exsfid = Zimbra::MAPI::Util::GetExchangeSpecialFolderId(m_pStore->GetInternalMAPIStore(), m_EntryID.cb, (LPENTRYID)(m_EntryID.lpb), &specialFolderIds);
        // exsfid is the index in the array "specialFolderIds"

        return exsfid;
    }
    return exsfid;
}

ZimbraSpecialFolderId MAPIFolder::GetZimbraSFIdFromExchSFId(ExchangeSpecialFolderId exsfid)
{
    LOGFN_TRACE;

    if (exsfid == EXSFID_NONE)
        return ZM_SFID_NONE;

    // Maps exch folder id to to Zimbra Special folder ID. There must be
    // one entry in here for each entry in "enum _ExchangeSpecialFolderId"
    ZimbraSpecialFolderId ZimbraSpecialFolderIdArray[TOTAL_NUM_SPECIAL_FOLDERS] = 
    {
        ZM_INBOX, 
        ZM_ROOT, 
        ZM_CALENDAR, 
        ZM_CONTACTS, 
        ZM_DRAFTS, 
        ZM_SFID_NONE /*JOURNAL*/,
        ZM_SFID_NONE /*NOTES*/, 
        ZM_TASKS, 
        ZM_SFID_NONE /*OUTBOX*/, 
        ZM_SENT_MAIL, 
        ZM_TRASH,
        ZM_SFID_NONE /*SYNC_CONFLICTS*/, 
        ZM_SFID_NONE /*SYNC_ISSUES*/,
        ZM_SFID_NONE /*SYNC_LOCAL_FAILURES*/, 
        ZM_SFID_NONE /*SYNC_SERVER_FAILURES*/, 
        ZM_SPAM,
        ZM_EMAILEDCONTACTS
    };

    if (exsfid < TOTAL_NUM_SPECIAL_FOLDERS)
        return ZimbraSpecialFolderIdArray[exsfid];
    else
        return ZM_SFID_NONE;
}

bool MAPIFolder::HiddenFolder()
{
    LOGFN_VERBOSE;
    return m_bHidden;
}

bool MAPIFolder::HasSubfolders()
{
    LOGFN_VERBOSE;
    return m_bSubfolders;
}

HRESULT MAPIFolder::ContainerClass(wstring &wstrContainerClass)
{
    LOGFN_VERBOSE;
    wstrContainerClass = m_sContainerClass;
    return S_OK;
}

wstring MAPIFolder::EscapeInvalidChar(const wstring& s, const char c)
{
    wstring sResult = s;
    if (sResult.find(c) != std::wstring::npos) 
        std::replace( sResult.begin(), sResult.end(), c, '_');

    return sResult;
}

void MAPIFolder::CalcFolderPath()
{
    LOGFN_TRACE;

    if (m_sParentFolderPath.size() == 0)
        m_sFolderpath = L"/MAPIRoot/RootFolderItems";
    else
    {
        // Slashes become underscores
        wstring sEscapedDisplayName = EscapeInvalidChar(m_sDisplayName, '/');

        // Ditto quotes
        sEscapedDisplayName = EscapeInvalidChar(sEscapedDisplayName, '"');
        m_sFolderpath = m_sParentFolderPath + L"/" + sEscapedDisplayName;
    }

    LOG_TRACE(_T("FolderPath: '%s'"), m_sFolderpath.c_str());
}

HRESULT MAPIFolder::GetItemCount(ULONG &ulCount)
{
    LOGFN_TRACE;
    ulCount = 0;
    if (m_pMAPIFolder == NULL)
        throw MAPIFolderException(E_FAIL, L"GetItemCount(): Folder Object is NULL.", ERR_MAPI_FOLDER, __LINE__, __FILE__);

    HRESULT hr = S_OK;

    if (m_ulContentCount > 0)
    {
        if (!m_pContentsTable)
            GetRestrictedContentsTable();

        Zimbra::Util::ScopedBuffer<SPropValue> pPropValues;
        if (FAILED(hr = m_pContentsTable->GetRowCount(0, &ulCount)))
            throw MAPIFolderException(E_FAIL, L"GetItemCount(): GetRowCount() Failed.", ERR_MAPI_FOLDER, __LINE__, __FILE__);
    }
    return hr;
}

HRESULT MAPIFolder::GetFolderIterator(FolderIterator &folderIter)
{
    LOGFN_TRACE;
    if (m_pMAPIFolder == NULL)
        return MAPI_E_NOT_FOUND;

    if (!m_bSubfolders)
        return MAPI_E_NOT_FOUND;

    // ------------------------------------------------------
    // Get folder's hierarchy table
    // ------------------------------------------------------
    LOG_TRACE(_T("Getting HierarchyTable..."));
    HRESULT hr = S_OK;

    #if 0
        // DCB: See if we can take advantage of CONVENIENT_DEPTH....doesn't work for exchange
        if (FAILED(hr = m_pMAPIFolder->GetHierarchyTable(fMapiUnicode|CONVENIENT_DEPTH, &m_pHierarchyTable))) // CONVENIENT_DEPTH not supported on EXCH. Docs say convenient depth mainly for root IABConainer
            throw MAPIFolderException(E_FAIL, L"GetFolderIterator(): GetHierarchyTable Failed.", ERR_MAPI_FOLDER, __LINE__, __FILE__);

        LPSRowSet pRows;
        if (FAILED(hr = m_pHierarchyTable->QueryRows(100, 0, &pRows)))
            throw GenericException(hr, L"GetFolderIterator:QueryRows Failed.",ERR_SET_RESTRICTION, __LINE__, __FILE__);
        DumpRows(pRows, LOGLEVEL_CMNT_GEN);

    #endif

    if (FAILED(hr = m_pMAPIFolder->GetHierarchyTable(fMapiUnicode, &m_pHierarchyTable)))
        throw MAPIFolderException(E_FAIL, L"GetFolderIterator(): GetHierarchyTable Failed.", ERR_MAPI_FOLDER, __LINE__, __FILE__);

    folderIter.InitMAPITableIterator(m_sObjectID, m_pHierarchyTable, m_pMAPIFolder, *m_pMapiSession, ZCM_ALL);
    return S_OK;
}

HRESULT MAPIFolder::GetMessageIterator(MessageIterator &msgIterator)
{
    LOGFN_TRACE;
    if (m_pMAPIFolder == NULL)
        throw MAPIFolderException(E_FAIL, L"GetMessageIterator(): Folder Object is NULL.", ERR_MAPI_FOLDER, __LINE__, __FILE__);

    if (!m_ulContentCount)
        return MAPI_E_NOT_FOUND;

    GetRestrictedContentsTable();

    msgIterator.InitMAPITableIterator(m_sObjectID, m_pContentsTable, m_pMAPIFolder, *m_pMapiSession);
    return S_OK;
}


