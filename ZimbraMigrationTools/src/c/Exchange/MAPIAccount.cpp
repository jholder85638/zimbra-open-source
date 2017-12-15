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

// ==========================================================================================================
// CMapiAccount
// ==========================================================================================================

CMapiAccount::CMapiAccount(wstring sSrcAccount, wstring sZCSAccount): 
    m_pUserStore(NULL), 
    m_pRootFolder(NULL),
    m_pPublicStore(NULL)
//
// One of these will be instantiated for each source account to be migrated.
// sSrcAccount only passed in for Server migration
//
{
    LOGFN_TRACE_NO;

    if (!sSrcAccount.empty())
        m_sSrcAccount = sSrcAccount;

    m_sZCSAccount = sZCSAccount;

    Zimbra::Mapi::Memory::SetMemAllocRoutines(NULL, MAPIAllocateBuffer, MAPIAllocateMore, MAPIFreeBuffer);
}

CMapiAccount::~CMapiAccount()
{
    LOGFN_TRACE_NO;

    if (m_pRootFolder)
    {
        delete m_pRootFolder;
        m_pRootFolder = NULL;
    }

    if (m_pUserStore && (CSessionGlobal::GetMigrationType()==MIGTYPE_SERVER))
    {
        if (!m_pPublicStore)
        {
            delete m_pUserStore;
            m_pUserStore = NULL;    
        }
        else
        {
            // m_pUserStore is a copy of m_pPublicStore in this case - so don't delete it!
            // See "m_pUserStore = m_pPublicStore" in CMapiAccount::OpenPublicStore()
        }
    }

    if (m_pPublicStore)
    {
        delete m_pPublicStore;
        m_pPublicStore = NULL;
    }
}

LONG WINAPI CMapiAccount::UnhandledExceptionFilter(LPEXCEPTION_POINTERS pExPtrs)
{
    LOGFN_TRACE_NO;
    LPWSTR strOutMessage = NULL;
    LONG lRetVal = Zimbra::Util::MiniDumpGenerator::GenerateCoreDump(pExPtrs,strOutMessage);
    if (strOutMessage)
        LOG_ERROR(strOutMessage);

    WCHAR pwszTempPath[MAX_PATH];
    GetTempPath(MAX_PATH, pwszTempPath);

    wstring strMsg;
    WCHAR strbuf[256];
    wsprintf(strbuf,L"The application has requested the Runtime to terminate it in an unusual way.\nThe core dump would get generated in %s.", pwszTempPath);
    //MessageBox(NULL, strbuf, _T("Runtime Error"), MB_OK);
    LOG_ERROR(strbuf);
    Zimbra::Util::FreeString(strOutMessage);
    return lRetVal;
}

LPCWSTR CMapiAccount::OpenPublicStore()
{
    LOGFN_TRACE_NO;

    LPWSTR lpwstrRetVal = NULL;
    LPWSTR lpwstrStatus = NULL;
    HRESULT hr = S_OK;
    try
    {
        // user store
        m_pPublicStore = new Zimbra::MAPI::MAPIStore(STORE_TYPE_PF); // Architecture is sub optimal - there is nothing to stop global session being uninitialised while this CMapiAccount is holding m_pPublicStore :-(

        LOG_SUMMARY(_T("Opening public store..."));
        Zimbra::MAPI::MAPISession* pGlobalMAPISession = CSessionGlobal::GetGlobalMAPISession();
        hr = pGlobalMAPISession->OpenPublicStore(*m_pPublicStore);
        if (hr == S_OK)
            m_pUserStore = m_pPublicStore;
        else
        {
            lpwstrStatus = FormatExceptionInfo(hr, L"CMapiAccount::OpenPublicStore() Failed", __FILE__, __LINE__);
            LOG_ERROR(lpwstrStatus);
            Zimbra::Util::CopyString(lpwstrRetVal, lpwstrStatus);
        }
    }
    catch (MAPISessionException &msse)
    {
        lpwstrStatus = FormatExceptionInfo(msse.ErrCode(), (LPWSTR)msse.Description().c_str(), (LPSTR)msse.SrcFile().c_str(), msse.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, msse.ShortDescription().c_str());
    }
    catch (MAPIStoreException &mste)
    {
        lpwstrStatus = FormatExceptionInfo(mste.ErrCode(), (LPWSTR)mste.Description().c_str(), (LPSTR)mste.SrcFile().c_str(), mste.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, mste.ShortDescription().c_str());
    }

    if(lpwstrStatus)
        Zimbra::Util::FreeString(lpwstrStatus);
    return lpwstrRetVal;
}

HRESULT CMapiAccount::EnumeratePublicFolders(std::vector<std::string> &pubFldrList)
{
    LOGFN_TRACE_NO;

    LPMAPITABLE pTable = NULL;
    HRESULT hr = m_pPublicStore->GetPublicFolderTable(&pTable);
    if (FAILED(hr))
        return E_FAIL;

    ULONG ulRows = 0;
    hr = pTable->GetRowCount(0, &ulRows);
    if (FAILED(hr))
    {
        pTable->Release();
        return E_FAIL;
    }
    if (ulRows > 0)
    {
        SPropTagArray columns;
        columns.cValues = 1;
        columns.aulPropTag[0] = PR_DISPLAY_NAME;

        hr = pTable->SetColumns(&columns, 0);
        if (FAILED(hr))
            pTable->Release();

        LPSRowSet pRowSet = NULL;
        hr = pTable->QueryRows(ulRows, 0, &pRowSet);
        if (FAILED(hr))
            pTable->Release();

        for (unsigned int i = 0; i < pRowSet->cRows; i++)
        {
            std::string strPubFldr;
            bool bwstrConv = false;
            LPWSTR lpwstrPubFldrName = pRowSet->aRow[i].lpProps[0].Value.lpszW;

            int bsz = WideCharToMultiByte(CP_ACP, 0, lpwstrPubFldrName, -1, 0, 0, 0, 0);
            if (bsz > 0)
            {
                char *p = new char[bsz];
                int rc = WideCharToMultiByte(CP_ACP, 0, lpwstrPubFldrName, -1, p, bsz, 0, 0);
                if (rc != 0)
                {
                    p[bsz - 1] = 0;
                    strPubFldr = p;
                    bwstrConv = true;
                }
                delete[] p;
            }
            if (bwstrConv)
                pubFldrList.push_back(strPubFldr);
        }
    }
    
    pTable->Release();
    return hr;
}


LPCWSTR CMapiAccount::InitializePublicFolders()
{
    LOGFN_TRACE_NO;

    LPWSTR lpwstrRetVal = NULL;
    LPWSTR lpwstrStatus = NULL;
    HRESULT hr = S_OK;

    try
    {
        // Open store
        lpwstrStatus = (LPWSTR)OpenPublicStore();

        // Get root folder
        Zimbra::MAPI::MAPISession* pGlobalMAPISession = CSessionGlobal::GetGlobalMAPISession();
        m_pRootFolder = new Zimbra::MAPI::MAPIFolder(*pGlobalMAPISession, *m_pPublicStore, L"");

        if (FAILED(hr = m_pPublicStore->GetRootFolder(*m_pRootFolder)))
        {
            lpwstrStatus = FormatExceptionInfo(hr, L"CMapiAccount::InitializePublicFolders() Failed", __FILE__, __LINE__);
            Zimbra::Util::CopyString(lpwstrRetVal, L"CMapiAccount::InitializePublicFolders() Failed");
        }
        Zimbra::Mapi::NamedPropsManager::SetNamedProps(m_pPublicStore->GetInternalMAPIStore());
    }
    catch (GenericException &ge)
    {
        lpwstrStatus = FormatExceptionInfo(ge.ErrCode(), (LPWSTR)ge.Description().c_str(), (LPSTR)ge.SrcFile().c_str(), ge.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,ge.ShortDescription().c_str());
    }

    if(lpwstrStatus)
        Zimbra::Util::FreeString(lpwstrStatus);

    return lpwstrRetVal;
}


LPCWSTR CMapiAccount::OpenUserStore()
//
// Called to open the store that is being migrated
// For a PST migration, the store has already been opened by _InitGlobalSessionAndStore(), so we borrow that one   <- Don't think we ever get called for PST migration - see caller
// For a server migration, we have to use IExchangeManageStore to open it.
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;

    LPWSTR lpwstrRetVal             = NULL;
    LPWSTR lpwstrStatus             = NULL;

    try
    {
        // =============================================================
        // We need global MAPI sess and default store below
        // =============================================================
        Zimbra::MAPI::MAPISession* pGlobalMAPISession = CSessionGlobal::GetGlobalMAPISession();
        LPMAPISESSION pMapiSess = pGlobalMAPISession->GetMAPISessionObject();

        Zimbra::MAPI::MAPIStore* pGlobalDefaultStore  = CSessionGlobal::GetGlobalDefaultStore();
        LPMDB pAdminStore = pGlobalDefaultStore->GetInternalMAPIStore();

        // ======================================================================
        // Get Exchange Server details from profile - ServerDN, UserDN, HostName
        // ======================================================================
        wstring sExchangeServerDNFromProfile, sExchangeHostNameFromProfile;
        Zimbra::MAPI::Util::GetUserDnAndServerDnFromProfile(pMapiSess, &sExchangeServerDNFromProfile, NULL, &sExchangeHostNameFromProfile);

        // DCB_BUG_103896 For OL2016, the above is failing to read props from global profile section (they have been removed in OL2016). See following links.
        // Other users encountered this issue:
        //     https://social.technet.microsoft.com/Forums/office/en-US/79ab3396-2476-4cb9-9245-d4a545116bd0/prprofilehomeserverdn-property-does-not-exist-any-more-in-outlook-2016-profile?forum=exchangesvrclients)
        //
        // Maybe this contains a workaround via IExchangeManageStoreEx
        //     http://blogs.msdn.com/b/dvespa/archive/2014/01/15/new-mapi-interface-ignore-home-server.aspx
        

        // =========================================================================
        // Server migration -> Need to lookup user in AD + use OpenOtherStore()
        // =========================================================================

        // -----------------------------------------------
        // LookupUserInActiveDirectory() needs domain name
        // -----------------------------------------------
        wstring sDomainName;
        if (CSessionGlobal::HasJoinedDomain())
        {
            // Machine on domain -> use domain discovered in _InitGlobalSessionAndStore() 
            sDomainName = CSessionGlobal::GetDomainName();
            _ASSERT(sDomainName.size());
        }
        else
        {
            // Machine NOT on domain -> use Exchange server host name from profile
            // (note that we can get here if ZMT not being run as administrator since domain discovery fails in this case)
            if (sExchangeHostNameFromProfile.size())
            {
                LOG_WARNING(_T("Machine domain unknown-> falling back to ExchangeHostNameFromProfile '%s' (This fallback will only work if ADSvr on same host as ExchSvr)"), sExchangeHostNameFromProfile.c_str());
                CSessionGlobal::SetDomainName(sExchangeHostNameFromProfile.c_str());
                sDomainName = sExchangeHostNameFromProfile;

                // NOTE: In case of machine not on domain, if AD is on different host than Exchange Server, it will not work. 
                // m_sDomainName MUST point to AD. TODO: Find a way to get AD from profile or some other means.
            }
        }

        if (!sDomainName.size())
        {
            LOG_ERROR(_T("AD host unknown - unable to lookup user in AD. Cannot open store to migrate."));
            _ASSERT(FALSE);
            throw GenericException(hr, L"CMapiAccount::OpenUserStore. ExchangeHostNameFromProfile unavailable. Cannot open store to migrate.", ERR_OPEN_ENTRYID,  __LINE__, __FILE__);
        }

        // --------------------------------------------------------------------
        // Get LegacyDN, ExchangeServerDN using LookupUserInActiveDirectory()
        // --------------------------------------------------------------------
        wstring sUserLegacyDN; wstring sExchangeServerDNFromAD;
        Zimbra::MAPI::Util::LookupUserInActiveDirectory(sDomainName.c_str(), m_sSrcAccount.c_str(), NULL, NULL, &sUserLegacyDN, &sExchangeServerDNFromAD);

        // DCB_BUG_103896 - If exch server DN not available in profile, fall back to the one from the target user's object in AD  
        wstring sExchangeServerDN = sExchangeServerDNFromProfile; 
        if (!sExchangeServerDNFromProfile.size())
        {
            LOG_TRACE(_T("ExchangeServerDN not available from profile -> Falling back to ExchangeServerDN from AD"));
            sExchangeServerDN = sExchangeServerDNFromAD;
        }
        else
        {
            _ASSERT(sExchangeServerDNFromProfile == sExchangeServerDNFromAD);
        }

        // --------------------------------------------------------------------
        // Must have sExchangeServerDN
        // --------------------------------------------------------------------
        // We will need this in the OpenOtherStore() below - if we don't have it, there's no way to open the store we want to migrate so bomb out now
        if (!sExchangeServerDN.size())
        {
            LOG_ERROR(_T("ExchangeServerDN unavailable. Cannot open users store"));
            _ASSERT(FALSE);
            throw GenericException(hr, L"CMapiAccount::OpenUserStore. ExchangeServerDN unavailable. Cannot open store to migrate.", ERR_OPEN_ENTRYID,  __LINE__, __FILE__);
        }


        // ------------------------------
        // Open the store
        // ------------------------------
        m_pUserStore = new Zimbra::MAPI::MAPIStore(STORE_TYPE_USER);
        LOG_SUMMARY(_T("Opening user store..."));
        hr = pGlobalMAPISession->OpenOtherStore(pAdminStore,                            // Admin store
                                                sExchangeServerDN.c_str(),              // 
                                                (LPWSTR)sUserLegacyDN.c_str(),          // Name of account to open
                                                *m_pUserStore);                         // MAPIStore object to fill in
        if (hr != S_OK)
            goto CLEAN_UP;
    }
    catch (MAPISessionException &msse)
    {
        lpwstrStatus = FormatExceptionInfo(msse.ErrCode(), (LPWSTR)msse.Description().c_str(), (LPSTR)msse.SrcFile().c_str(), msse.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, msse.ShortDescription().c_str());
    }
    catch (MAPIStoreException &mste)
    {
        lpwstrStatus = FormatExceptionInfo(mste.ErrCode(), (LPWSTR)mste.Description().c_str(), (LPSTR)mste.SrcFile().c_str(), mste.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, mste.ShortDescription().c_str());
    }
    catch (Util::MapiUtilsException &muex)
    {
        lpwstrStatus = FormatExceptionInfo(muex.ErrCode(), (LPWSTR)muex.Description().c_str(), (LPSTR)muex.SrcFile().c_str(), muex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, muex.ShortDescription().c_str());
    }

CLEAN_UP: 
    if ((hr != S_OK) && (!lpwstrStatus))
    {
        lpwstrStatus = FormatExceptionInfo(hr, L"CMapiAccount::OpenSessionAndStore() Failed", __FILE__, __LINE__);
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,  L"CMapiAccount::OpenSessionAndStore() Failed");
    }

    if (lpwstrStatus)
        Zimbra::Util::FreeString(lpwstrStatus);

    return lpwstrRetVal;
}

LPCWSTR CMapiAccount::LogonAndGetRootFolder()
{
    LPWSTR exceptionmsg = NULL;
    __try
    {
        return _LogonAndGetRootFolder();
    }
    __except(Zimbra::Util::MiniDumpGenerator::GenerateCoreDump(GetExceptionInformation(),exceptionmsg))
    {
        LOG_ERROR(exceptionmsg);		
    }
    return exceptionmsg;
}

LPCWSTR CMapiAccount::_LogonAndGetRootFolder()
// Log onto store and get root folder
// If this is a USER migration, we use the store that's already been opened by CSessionGlobal
// If its a SERVER migration, we have to call OpenUserStore()
{
    LOGFN_TRACE_NO;

    LPWSTR lpwstrStatus = NULL;
    LPWSTR lpwstrRetVal = NULL;
    HRESULT hr = S_OK;

    try
    {
        // -----------------------------------
        // Get store ptr
        // -----------------------------------
        if (CSessionGlobal::GetMigrationType()!=MIGTYPE_SERVER)
        {
            // Single mailbox - use the store we've already opened in CSessionGlobal
            m_pUserStore = CSessionGlobal::GetGlobalDefaultStore();
        }
        else
        {
            // Server migration - need to open explicit store
            lpwstrStatus = (LPWSTR)OpenUserStore(); // Takes extended time
            if (lpwstrStatus)
                return lpwstrStatus;
        }

        // -----------------------------------
        // Get root folder
        // -----------------------------------
        // Construct empty folder
        Zimbra::MAPI::MAPISession* pGlobalMAPISession = CSessionGlobal::GetGlobalMAPISession();
        m_pRootFolder = new Zimbra::MAPI::MAPIFolder(*pGlobalMAPISession, *m_pUserStore, L"");

        // Fill it in
        if (FAILED(hr = m_pUserStore->GetRootFolder(*m_pRootFolder)))
        {
            lpwstrStatus = FormatExceptionInfo(hr, L"CMapiAccount::Initialize() Failed", __FILE__, __LINE__);
            Zimbra::Util::CopyString(lpwstrRetVal, L"CMapiAccount::Initialize() Failed");
        }

        // Set named props
        Zimbra::Mapi::NamedPropsManager::SetNamedProps(m_pUserStore->GetInternalMAPIStore());
    }
    catch (GenericException &ge)
    {
        lpwstrStatus = FormatExceptionInfo(ge.ErrCode(), (LPWSTR)ge.Description().c_str(), (LPSTR)ge.SrcFile().c_str(), ge.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,ge.ShortDescription().c_str());
    }

    if(lpwstrStatus)
        Zimbra::Util::FreeString(lpwstrStatus);
    return lpwstrRetVal;
}


LPCWSTR CMapiAccount::GetRootFolderHierarchy(vector<Folder_Data> &vfolderlist)
//
// NB Root folder == IPM subtree
//
{
    //LOGFN_TRACE_NO;

    LPWSTR exceptionmsg = NULL;
    __try
    {
        return _GetRootFolderHierarchy(vfolderlist);
    }
    __except(Zimbra::Util::MiniDumpGenerator::GenerateCoreDump(GetExceptionInformation(),exceptionmsg))
    {
        LOG_ERROR(exceptionmsg);		
    }
    return exceptionmsg;
}

LPCWSTR CMapiAccount::_GetRootFolderHierarchy(vector<Folder_Data> &vfolderlist)
//
// NB Root folder == IPM subtree
//
{
    LOGFN_TRACE_NO;

    LPWSTR lpwstrStatus = NULL;
    HRESULT hr = S_OK;

    try
    {
        ULONG ulDepth = 0;
        hr = CacheFolderDataAndIterateChildFoldersDepthFirst(*m_pRootFolder, vfolderlist, ulDepth);
    }
    catch (GenericException &ge)
    {
        lpwstrStatus = FormatExceptionInfo(ge.ErrCode(), (LPWSTR)ge.Description().c_str(), (LPSTR)ge.SrcFile().c_str(), ge.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::FreeString(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrStatus, ge.ShortDescription().c_str());
    }
    return lpwstrStatus;
}

void CMapiAccount::MAPIFolder2FolderData(Zimbra::MAPI::MAPIFolder *folder, // In
                                         Folder_Data &flderdata)           // Out
//
// We're building up a vector of Folder_Data items, so fill a Folder_Data for passed-in folder
//
{
    LOGFN_TRACE_NO;

    ULONG itemCount = 0;

    // folder item count
    folder->GetItemCount(itemCount);
    flderdata.itemcount = itemCount;

    // store Folder EntryID
    SBinary sbin = folder->EntryID();
    CopyEntryID(sbin, flderdata.sbin);

    // folder path
    flderdata.folderpath = folder->GetFolderPath();

    // folderName
    if (flderdata.zimbraspecialfolderid == ZM_ROOT)
        flderdata.name = CONST_ROOTFOLDERNAME;
    else
        flderdata.name = folder->DispName();
}

HRESULT CMapiAccount::CacheFolderDataAndIterateChildFoldersDepthFirst(Zimbra::MAPI::MAPIFolder &folder, vector<Folder_Data> &fd, ULONG& ulDepth)
//
// Walks all child folders of "folder", opens them and adds data to fd
//
// Recursive! Depth first.
//
{
    ulDepth++;

    LOG_TRACE(L" ");
    LOG_TRACE(L" ");
    LOG_TRACE(L" ");
    LOG_TRACE(L" ");
    LOG_TRACE(L"-------------------------------------------------------------------------------------------------------");
    LOG_TRACE(L"Iterating child folders of depth %d folder '%s' ", ulDepth, folder.GetFolderPath().c_str());
    LOG_TRACE(L"-------------------------------------------------------------------------------------------------------");
    LOGFN_TRACE_NO;

    // ================================================================
    // There are several things that might cause a folder to be skipped
    // ================================================================

    // Hidden?
    bool bHidden = folder.HiddenFolder();
    if (bHidden)
        LOG_TRACE(_T("Skipping hidden folder"));

    // In skip list?

    // Skip certain container classes
    wstring wstrContainerClass;
    folder.ContainerClass(wstrContainerClass);

    bool bUnrecognisedContainerClass =   (wstrContainerClass != L"IPF.Note")        
                                      && (wstrContainerClass != L"IPF.Contact")     
                                      && (wstrContainerClass != L"IPF.Appointment") 
                                      && (wstrContainerClass != L"IPF.Task")        
                                      && (wstrContainerClass != L"IPF.Imap")        
                                      && (wstrContainerClass != L"");               
    bool bUnsupportedContainerClass = bUnrecognisedContainerClass;
    if (bUnsupportedContainerClass)
        LOG_TRACE(_T("Skipping due to container class '%s'"), wstrContainerClass.c_str());

    // Skip certain exch special folders
    bool bUnsupportedExchSF = false;
    ExchangeSpecialFolderId exsfid = folder.GetExchangeSpecialFolderId();
    if (   exsfid == EXSFID_SYNC_ISSUES
        || exsfid == EXSFID_NOTE         )
    {
        LOG_TRACE(_T("Skipping due to unsupported special folder (exsfid: %d)"), exsfid);
        bUnsupportedExchSF = true;
    }

    // Skip folders in exclusion list, hidden folders and non-standard type folders
    bool bSkipFolder = (bHidden || bUnsupportedContainerClass || bUnsupportedExchSF);

    // ===============================================================
    // If not skipping, get it
    // ===============================================================
    if (!bSkipFolder)
    {
        ZimbraSpecialFolderId  zimsfid = folder.GetZimbraSFIdFromExchSFId(exsfid);

        // -----------------------------------------------------
        // Get folder data
        // -----------------------------------------------------
        Folder_Data flderdata;
        flderdata.containerclass = wstrContainerClass;   // Container class
        flderdata.ulDepth = ulDepth;                     // Depth
        flderdata.zimbraspecialfolderid = (long)zimsfid; // ZimbraSpecialFolderId
        MAPIFolder2FolderData(&folder, flderdata);       // Others

        // -----------------------------------------------------
        // Add to list
        // -----------------------------------------------------
        // Dont add root folder, if it has no data items
        if((flderdata.zimbraspecialfolderid != ZM_ROOT) || ((flderdata.zimbraspecialfolderid == ZM_ROOT)&&(flderdata.itemcount>0)))
            fd.push_back(flderdata);


        // -----------------------------------------------------------------
        // Get a folder iterator for curr folder so we can walk its folders
        // -----------------------------------------------------------------
        Zimbra::MAPI::FolderIterator *folderIter = new Zimbra::MAPI::FolderIterator;
        HRESULT hr = folder.GetFolderIterator(*folderIter);		
        if (hr == S_OK)
        {
            // -----------------------------------------------------------------
            // Walk its folders - this will be depth first
            // -----------------------------------------------------------------
            BOOL bMore = TRUE;
            while(bMore)
            {
                // Create a MAPIFolder for the child
                Zimbra::MAPI::MAPISession* pGlobalMAPISession = CSessionGlobal::GetGlobalMAPISession();
                Zimbra::MAPI::MAPIFolder *childFolder = new Zimbra::MAPI::MAPIFolder(*pGlobalMAPISession, *m_pUserStore, flderdata.folderpath);

                // And initialize it
                bMore = folderIter->GetNext(*childFolder);
                if (bMore)
                    CacheFolderDataAndIterateChildFoldersDepthFirst(*childFolder, fd, ulDepth);

                delete childFolder;
                childFolder = NULL;
            }
        }
        delete folderIter;
        folderIter = NULL;
    }

    ulDepth--;
    return S_OK;
}

HRESULT CMapiAccount::GetInternalFolder(SBinary sbFolderEID, MAPIFolder &folder)
{
    LOGFN_TRACE_NO;

    LPMAPIFOLDER pFolder = NULL;
    HRESULT hr = S_OK;
    ULONG objtype;
    if ((hr = m_pUserStore->OpenEntry(sbFolderEID.cb, (LPENTRYID)sbFolderEID.lpb, NULL, MAPI_BEST_ACCESS, &objtype, (LPUNKNOWN *)&pFolder)) != S_OK)
        throw GenericException(hr, L"CMapiAccount::GetInternalFolder OpenEntry Failed.", ERR_OPEN_ENTRYID,  __LINE__, __FILE__);

    // Get some props the MAPIFolder needs
    SizedSPropTagArray(5, tags) = {5, { PR_DISPLAY_NAME, PR_CONTAINER_CLASS, PR_ATTR_HIDDEN, PR_SUBFOLDERS, PR_CONTENT_COUNT }};
    ULONG cVals = 0;
    Zimbra::Util::ScopedBuffer<SPropValue> pVals;
    hr = pFolder->GetProps((LPSPropTagArray)& tags, MAPI_UNICODE, &cVals, pVals.getptr());
    if (FAILED(hr))
        throw GenericException(hr, L"CMapiAccount::GetInternalFolder GetProps() Failed.", ERR_OPEN_PROPERTY, __LINE__, __FILE__);

    wstring sDisplayName;
    if (pVals[0].ulPropTag == PR_DISPLAY_NAME)
        sDisplayName = pVals[0].Value.LPSZ;

    wstring sContainerClass;
    if (pVals[1].ulPropTag == PR_CONTAINER_CLASS)
        sContainerClass = pVals[1].Value.LPSZ;
    
    bool bHidden = false;
    if (pVals[2].ulPropTag == PR_ATTR_HIDDEN)
        bHidden = pVals[2].Value.b==1;
    
    bool bSubfolders = true;
    if (pVals[3].ulPropTag == PR_SUBFOLDERS)
        bSubfolders = pVals[3].Value.b==1;
        
    ULONG ulContentCount = 0;
    if (pVals[4].ulPropTag == PR_CONTENT_COUNT)
        ulContentCount = pVals[4].Value.ul;

    folder.InitMAPIFolder(pFolder, sDisplayName.c_str(), &sbFolderEID, sContainerClass.c_str(), bHidden, bSubfolders, ulContentCount);
    return hr;
}

LPCWSTR CMapiAccount::GetFolderItems(SBinary sbFolderEID, vector<Item_Data> &ItemList)
{
    //LOGFN_TRACE_NO;

    LPWSTR exceptionmsg = NULL;
    __try
    {
        _GetFolderItems(sbFolderEID, ItemList);
    }
    __except(Zimbra::Util::MiniDumpGenerator::GenerateCoreDump(GetExceptionInformation(),exceptionmsg))
    {
        LOG_ERROR(exceptionmsg);		
    }
    return exceptionmsg;
}

LPCWSTR CMapiAccount::_GetFolderItems(SBinary sbFolderEID, vector<Item_Data> &ItemList)
{
    LOGFN_TRACE_NO;

    LPWSTR lpwstrStatus = NULL;
    LPWSTR lpwstrRetVal = NULL;
    HRESULT hr = S_OK;

    MAPIFolder folder;
    try
    {
        if (FAILED(hr = GetInternalFolder(sbFolderEID, folder) != S_OK))
        {
            lpwstrStatus = FormatExceptionInfo(hr, L"CMapiAccount::GetFolderItems() Failed", __FILE__, __LINE__);
            Zimbra::Util::CopyString(lpwstrRetVal,L"CMapiAccount::GetFolderItems() Failed");
            goto ZM_EXIT;
        }

        Zimbra::MAPI::MessageIterator *msgIter = new Zimbra::MAPI::MessageIterator();
        hr = folder.GetMessageIterator(*msgIter);
        if (hr == S_OK)
        {
            // For every msg in the folder, build an itemdata struct and add it to ItemList
            BOOL bContinue = true;
            BOOL skip = false;
            DWORD dwCountMsgs = 0;
            wstring sSubj;
            while (bContinue)
            {
                skip = false;
                {
                    LOG_SUPPRESS_LOGGING_THIS_THREAD(0);
                    Zimbra::MAPI::MAPIMessage *msg = new Zimbra::MAPI::MAPIMessage();

                    try
                    {
                        bContinue = msgIter->GetNext(*msg);
                    }
                    catch (MAPIMessageException &msgex)
                    {
                        lpwstrStatus = FormatExceptionInfo(msgex.ErrCode(), (LPWSTR)msgex.Description().c_str(), (LPSTR)msgex.SrcFile().c_str(), msgex.SrcLine());
                        LOG_ERROR(lpwstrStatus);
                        Zimbra::Util::CopyString(lpwstrRetVal, msgex.ShortDescription().c_str());
                        bContinue = true;
                        skip = true;
                    }

                    if (bContinue && !skip)
                    {
                        // ----------------------------------------------
                        // Fill in Item_Data
                        // ----------------------------------------------
                        Item_Data itemdata;

                        // Item type
                        itemdata.lItemType = msg->ItemType();

                        // Message Size
                        itemdata.ulMessageSize = msg->MessageSize();

                        // EID
                        SBinary sbin = msg->EntryID();
                        CopyEntryID(sbin, itemdata.sbMessageID);

                        // EID in string format
                        itemdata.strMsgEntryId = "";
                        Zimbra::Util::ScopedArray<CHAR> spUid(new CHAR[(sbin.cb * 2) + 1]);
                        if (spUid.get() != NULL)
                        {
                            Zimbra::Util::HexFromBin(sbin.lpb, sbin.cb, spUid.get());
                            itemdata.strMsgEntryId = spUid.getref();
                        }

                        // Date
                        itemdata.i64SubmitDate = msg->SubmitDate();

                        // Subject
                        LPTSTR lpstrSubject;
                        if (msg->Subject(&lpstrSubject))
                        {
                            itemdata.strSubject = lpstrSubject;
                            sSubj = lpstrSubject;
                            SafeDelete(lpstrSubject);
                        }
                        else
                            itemdata.strSubject = L"";

                        // FilterDate
                        LPTSTR lpstrFilterDate;
                        if (msg->FilterDate(&lpstrFilterDate))
                        {
                            itemdata.strFilterDate = lpstrFilterDate;
                            SafeDelete(lpstrFilterDate);
                        }
                        else
                            itemdata.strFilterDate = L"";


                        // Add to list
                        ItemList.push_back(itemdata);
                    }
                    delete msg;
                }

                if (bContinue && !skip)
                    LOG_VERBOSE(_T("%-5d %s"), ++dwCountMsgs, sSubj.c_str());

            } // while
        }

        delete msgIter;
    }
    catch (MAPISessionException &mssex)
    {
        lpwstrStatus = FormatExceptionInfo(mssex.ErrCode(), (LPWSTR)mssex.Description().c_str(), (LPSTR)mssex.SrcFile().c_str(), mssex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, mssex.ShortDescription().c_str());
    }
    catch (MAPIFolderException &mfex)
    {
        lpwstrStatus = FormatExceptionInfo(mfex.ErrCode(), (LPWSTR)mfex.Description().c_str(), (LPSTR)mfex.SrcFile().c_str(), mfex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, mfex.ShortDescription().c_str());
    }
    catch (MAPIMessageException &msgex)
    {
        lpwstrStatus = FormatExceptionInfo(msgex.ErrCode(), (LPWSTR)msgex.Description().c_str(), (LPSTR)msgex.SrcFile().c_str(), msgex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, msgex.ShortDescription().c_str());
    }
    catch (GenericException &genex)
    {
        lpwstrStatus = FormatExceptionInfo(genex.ErrCode(), (LPWSTR)genex.Description().c_str(), (LPSTR)genex.SrcFile().c_str(), genex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, genex.ShortDescription().c_str());
    }

    if(lpwstrStatus)
        Zimbra::Util::FreeString(lpwstrStatus);
    
ZM_EXIT: 
    return lpwstrRetVal;
}

LPCWSTR CMapiAccount::GetItem(SBinary sbItemEID, BaseItemData &itemData)
{
    // LOGFN_TRACE_NO;

    LPWSTR exceptionmsg = NULL;
    __try
    {
        exceptionmsg = (LPWSTR)_GetItem(sbItemEID,itemData);
    }
    __except(Zimbra::Util::MiniDumpGenerator::GenerateCoreDump(GetExceptionInformation(),exceptionmsg))
    {
        LOG_ERROR(exceptionmsg);		
    }

    //g_logger.LogProcessMemoryUsage();

    return exceptionmsg;
}

LPCWSTR CMapiAccount::_GetItem(SBinary sbItemEID, BaseItemData &itemData)
//
// OpenEntry()s item "sbItemEID" and populates itemData from the MAPI obj
//
{
    LOGFN_TRACE_NO;

    LPWSTR lpwstrStatus = NULL;
    LPWSTR lpwstrRetVal = NULL;
    HRESULT hr = S_OK;
    
    LPTSTR pszBin = SBinToStr(sbItemEID);
    LOG_TRACE(L"Item EntryID: %s", pszBin);

    // ---------------------------------------------------------------------
    // OpenEntry() the MAPI item. We'll wrap this with a MAPIMessage below.
    // ---------------------------------------------------------------------
    LPMESSAGE pMessage = NULL;
    ULONG objtype;
    if (FAILED(hr = m_pUserStore->OpenEntry(sbItemEID.cb, (LPENTRYID)sbItemEID.lpb, NULL, MAPI_BEST_ACCESS, &objtype, (LPUNKNOWN *)&pMessage)))
    {
        lpwstrStatus = FormatExceptionInfo(hr, L"CMapiAccount::GetItem() Failed", __FILE__, __LINE__);		
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal, ERR_OPEN_ENTRYID);
        goto ZM_EXIT;
    }

    /* in case we want some retry logic sometime
    hr = 0x80040115;
    int tries = 10;
    int num = 0;
    while ((hr == 0x80040115) && (num < tries))
    {
        hr = m_pUserStore->OpenEntry(sbItemEID.cb, (LPENTRYID)sbItemEID.lpb, NULL, MAPI_BEST_ACCESS, &objtype, (LPUNKNOWN *)&pMessage);
        if (FAILED(hr))
            LOG_INFO(_T("got an 80040115"));
        num++;
    }
    if (FAILED(hr))
    {
        lpwstrStatus = FormatExceptionInfo(hr, L"CMapiAccount::GetItem() Failed", __FILE__, __LINE__);
        LOG_INFO(_T("CMapiAccount -- m_pUserStore->OpenEntry failed %s"), lpwstrStatus);
        goto ZM_EXIT;
    }
    */

    try
    {
        Zimbra::MAPI::MAPISession* pGlobalMAPISession = CSessionGlobal::GetGlobalMAPISession();

        // ---------------------------------------------------------------------
        // Wrap the opened item
        // ---------------------------------------------------------------------
        MAPIMessage msg;
        msg.InitMAPIMessage(pMessage, *pGlobalMAPISession); // Does the main GetProps(), GetRecipientTable() etc But note MAPIAppointment does its own GetProps() later too DCB_PERFORMANCE

        // Init keywords
        std::vector<LPWSTR>* pKeywords = msg.SetKeywords();

        Zimbra::MAPI::ZM_ITEM_TYPE nItemType = msg.ItemType();
        if ((nItemType == ZT_MAIL) || (nItemType == ZT_MEETREQ))
        {
           // printf("ITEM TYPE: ZT_MAIL \n");

            MessageItemData *msgdata = (MessageItemData *)&itemData;

            // Subject
            msgdata->Subject = L"";
            LPTSTR lpstrSubject;
            if (msg.Subject(&lpstrSubject))
            {
                msgdata->Subject = lpstrSubject;
                SafeDelete(lpstrSubject);
            }

            msgdata->IsFlagged = msg.IsFlagged();

            msgdata->Urlname = L"";

            LPTSTR lpstrUrlName;
            if (msg.GetURLName(&lpstrUrlName))
            {
                msgdata->Urlname = lpstrUrlName;
                SafeDelete(lpstrUrlName);
            }

            msgdata->IsDraft        = msg.IsDraft();
            msgdata->IsFromMe       = (msg.IsFromMe() == TRUE);
            msgdata->IsUnread       = (msg.IsUnread() == TRUE);
            msgdata->IsForwarded    = (msg.Forwarded() == TRUE);
            msgdata->RepliedTo      = msg.RepliedTo() == TRUE;
            msgdata->HasAttachments = msg.HasAttach();
            msgdata->IsUnsent       = msg.IsUnsent() == TRUE;
            msgdata->HasHtml        = msg.HasHtmlPart();
            msgdata->HasText        = msg.HasTextPart();
            msgdata->Date           = msg.SubmitDate();

            LPWSTR wstrDateString;
            AtoW(msg.SubmitDateString(), wstrDateString);
            msgdata->DateString = wstrDateString;
            SafeDelete(wstrDateString);

            LPWSTR wstrDateUnixString;
            AtoW(msg.SubmitDateUnixString(), wstrDateUnixString);
            msgdata->DateUnixString = wstrDateUnixString;
            SafeDelete(wstrDateUnixString);

            msgdata->deliveryDate = msg.DeliveryDate();

            LPWSTR wstrDelivUnixString;
            AtoW(msg.DeliveryUnixString(), wstrDelivUnixString);
            msgdata->DeliveryUnixString = wstrDelivUnixString;
            SafeDelete(wstrDelivUnixString);

            LPWSTR wstrDelivDateString;
            AtoW(msg.DeliveryDateString(), wstrDelivDateString);
            msgdata->DeliveryDateString = wstrDelivDateString;
            SafeDelete(wstrDelivDateString);

            msgdata->vTags = pKeywords;

            /*
            if (msgdata->HasText)
            {
                LPTSTR textMsgBuffer;
                unsigned int nTextchars;
                msg.TextBody(&textMsgBuffer, nTextchars);
                msgdata->textbody.buffer = textMsgBuffer;
                msgdata->textbody.size = nTextchars;
            }
            if (msgdata->HasHtml)
            {
                LPVOID pHtmlBodyBuffer = NULL;
                unsigned int nHtmlchars;
                msg.HtmlBody(&pHtmlBodyBuffer, nHtmlchars);
                msgdata->htmlbody.buffer = (LPTSTR)pHtmlBodyBuffer;
                msgdata->htmlbody.size = nHtmlchars;
            }
            */

            // =========================================================================================
            // Get message mime
            // =========================================================================================
            mimepp::String sMime;
            {   // This block is here to ensure mimeMsg destructs as soon as we've assigned sMime below
                // Necessary because for a 50MB msg, msg consumes several hundred MB.
                // Less of an issue since BUG 101334 fixed, but possibly still worth having this
                mimepp::Message mimeMsg;
                try
                {
                    msg.ToMimePPMessage(mimeMsg);
                }
                catch(...)
                {
                    lpwstrStatus = FormatExceptionInfo(hr, L"ToMimePPMessage Failed", __FILE__, __LINE__);
                    LOG_ERROR(_T("CMapiAccount -- exception"));
                    LOG_ERROR(lpwstrStatus);
                    Zimbra::Util::CopyString(lpwstrRetVal,L"MimePP conversion Failed.");
                    goto ZM_EXIT;
                }

                sMime = mimeMsg.getString();
                LOG_VERBOSE(_T("Mime conversion succeeded, producing %d bytes"), sMime.size());
                Zimbra::MAPI::Util::PauseAfterLargeMemDelta();
            }
            LOG_VERBOSE(_T("mimeMsg freed"));
            Zimbra::MAPI::Util::PauseAfterLargeMemDelta();



            // DCB Unit Test exception handling
            #ifdef _DEBUG
                bool bThrow = false;
                if (bThrow)
                {
                    int* pInt = NULL;
                    *pInt=10;
                }
            #endif

            // =========================================================
            // Decide whether mime should go to C# in file or buffer
            // =========================================================
            msgdata->dwMimeSize = (DWORD)sMime.size();

            if (msgdata->dwMimeSize > 3 * 1024*1024) // Bigger than 3 meg -> file
            {
                // =========================================================
                // FILE (file will contain non-unicode)
                // =========================================================
                HRESULT hr = S_OK;

                // ----------------------------------------------------
                // Calc a name for the temp file (in the app temp dir)
                // ----------------------------------------------------
                wstring wstrTempAppDirPath;
                if (!Zimbra::MAPI::Util::GetAppTemporaryDirectory(wstrTempAppDirPath))
                {
                    lpwstrStatus = FormatExceptionInfo(hr, L"GetAppTemporaryDirectory Failed", __FILE__, __LINE__);
                    LOG_ERROR(lpwstrStatus);
                    Zimbra::Util::CopyString(lpwstrRetVal, lpwstrStatus);
                    goto ZM_EXIT;
                }
                char *lpszDirName = NULL;
                WtoA((LPWSTR)wstrTempAppDirPath.c_str(), lpszDirName); // lpszDirName freed below
                string sFQFileName = lpszDirName;

                char *lpszUniqueName = NULL;
                WtoA((LPWSTR)Zimbra::MAPI::Util::GetUniqueName().c_str(), lpszUniqueName); // lpszUniqueName freed below

                sFQFileName += "\\";
                sFQFileName += lpszUniqueName;
                SafeDelete(lpszDirName);
                lpszDirName = NULL;
                SafeDelete(lpszUniqueName);
                lpszUniqueName = NULL;
                // sFQFileName now contains the mime file path


                // ----------------------------------------------------
                // Write mime to the file
                // ----------------------------------------------------
                if (FAILED(hr = Zimbra::MAPI::Util::StreamStringToFile(sMime, (LPTSTR)sFQFileName.c_str())))
                {
                    lpwstrStatus = FormatExceptionInfo(hr, L"Message Error: StreamStringToFile Failed.", __FILE__, __LINE__);
                    LOG_ERROR(lpwstrStatus);
                    Zimbra::Util::CopyString(lpwstrRetVal, lpwstrStatus);
                    goto ZM_EXIT;
                }

                // ----------------------------------------------------
                // Write sFQFileName into the field
                // ----------------------------------------------------
                WCHAR *lpwstrFQFileName = NULL;
                AtoW((LPSTR)sFQFileName.c_str(), lpwstrFQFileName);
                msgdata->MimeFile = lpwstrFQFileName;
                SafeDelete(lpwstrFQFileName);
                lpwstrFQFileName = NULL;

                // ----------------------------------------------------
                // Null out the mime buffer since we're using file instead for this item
                // ----------------------------------------------------
                msgdata->pwsMimeBuffer = NULL;
            }
            else
            {
                // =========================================================
                // BUFFER (Buffer gets converted to unicode first)
                // =========================================================

                #ifndef CASE_184490_DIAGNOSTICS
                    // DCB - It seems that we now place the MIME directly in a wstring
                    LPTSTR pwDes = NULL;
                    AtoW((LPSTR)mimeMsg.getString().c_str(),pwDes);   
                    Zimbra::MAPI::Util::PauseAfterLargeMemDelta();

                    msgdata->wsMimeBuffer = pwDes;
                    Zimbra::MAPI::Util::PauseAfterLargeMemDelta();

                    delete[] pwDes;
                    Zimbra::MAPI::Util::PauseAfterLargeMemDelta();
                #else
                    LOG_GEN(_T("Getting mime..."));
                    LPSTR pszMimeMsg = (LPSTR)sMime.c_str();
                    if (pszMimeMsg)
                    {
                        LOG_GEN(_T("mime len: %d"), strlen(pszMimeMsg));
                        LOG_GEN(_T(">AtoW..."));
                        AtoW2(pszMimeMsg, msgdata->pwsMimeBuffer);   
                        Zimbra::MAPI::Util::PauseAfterLargeMemDelta();
                        // no delete[] in this case - done later in CMapiAccount::GetData()
                    }
                    else
                        LOG_ERROR(_T("NULL mime"));
                #endif
            }

        }
        else 
        if (nItemType == ZT_CONTACTS)
        {
           // printf("ITEM TYPE: ZT_CONTACTS \n");
            MAPIContact mapicontact(*pGlobalMAPISession, msg);

            ContactItemData *cd = (ContactItemData *)&itemData;
            cd->AssistantPhone      = mapicontact.AssistantPhone();
            cd->Birthday            = mapicontact.Birthday();
            cd->CallbackPhone       = mapicontact.CallbackPhone();
            cd->CarPhone            = mapicontact.CarPhone();
            cd->Company             = mapicontact.Company();
            cd->CompanyPhone        = mapicontact.CompanyPhone();
            cd->Department          = mapicontact.Department();
            cd->Email1              = mapicontact.Email();
            cd->Email2              = mapicontact.Email2();
            cd->Email3              = mapicontact.Email3();
            cd->FileAs              = mapicontact.FileAs();
            cd->FirstName           = mapicontact.FirstName();
            cd->HomeCity            = mapicontact.HomeCity();
            cd->HomeCountry         = mapicontact.HomeCountry();
            cd->HomeFax             = mapicontact.HomeFax();
            cd->HomePhone           = mapicontact.HomePhone();
            cd->HomePhone2          = mapicontact.HomePhone2();
            cd->HomePostalCode      = mapicontact.HomePostalCode();
            cd->HomeState           = mapicontact.HomeState();
            cd->HomeStreet          = mapicontact.HomeStreet();
            cd->HomeURL             = mapicontact.HomeURL();
            cd->IMAddress1          = mapicontact.IMAddress1();
            cd->JobTitle            = mapicontact.JobTitle();
            cd->LastName            = mapicontact.LastName();
            cd->MiddleName          = mapicontact.MiddleName();
            cd->MobilePhone         = mapicontact.MobilePhone();
            cd->NamePrefix          = mapicontact.NamePrefix();
            cd->NameSuffix          = mapicontact.NameSuffix();
            cd->NickName            = mapicontact.NickName();
            cd->Notes               = mapicontact.Notes();
            cd->OtherCity           = mapicontact.OtherCity();
            cd->OtherCountry        = mapicontact.OtherCountry();
            cd->OtherFax            = mapicontact.OtherFax();
            cd->OtherPhone          = mapicontact.OtherPhone();
            cd->OtherPostalCode     = mapicontact.OtherPostalCode();
            cd->OtherState          = mapicontact.OtherState();
            cd->OtherStreet         = mapicontact.OtherStreet();
            cd->OtherURL            = mapicontact.OtherURL();
            cd->Pager               = mapicontact.Pager();
            cd->PrimaryPhone        = mapicontact.PrimaryPhone();
            cd->pDList              = mapicontact.DList();
            cd->PictureID           = mapicontact.Picture();
            cd->Type                = mapicontact.Type();
            cd->UserField1          = mapicontact.UserField1();
            cd->UserField2          = mapicontact.UserField2();
            cd->UserField3          = mapicontact.UserField3();
            cd->UserField4          = mapicontact.UserField4();
            cd->WorkCity            = mapicontact.WorkCity();
            cd->WorkCountry         = mapicontact.WorkCountry();
            cd->WorkFax             = mapicontact.WorkFax();
            cd->WorkPhone           = mapicontact.WorkPhone();
            cd->WorkPhone2          = mapicontact.WorkPhone2();
            cd->WorkPostalCode      = mapicontact.WorkPostalCode();
            cd->WorkState           = mapicontact.WorkState();
            cd->WorkStreet          = mapicontact.WorkStreet();
            cd->WorkURL             = mapicontact.WorkURL();
            cd->ContactImagePath    = mapicontact.ContactImagePath();
            cd->ImageContenttype    = mapicontact.ContactImageType();
            cd->ImageContentdisp    = mapicontact.ContactImageDisp();
            cd->vTags               = pKeywords;
            cd->Anniversary         = mapicontact.Anniversary();

            // UDFs
            vector<ContactUDFields>::iterator it;
            for (it= mapicontact.UserDefinedFields()->begin();it != mapicontact.UserDefinedFields()->end();it++)
                cd->UserDefinedFields.push_back(*it);
        }
        else 
        if (nItemType == ZT_APPOINTMENTS)
        {
            // Construct the MAPIAppointment obj
            MAPIAppointment mapiappointment(*pGlobalMAPISession, *m_pUserStore ,msg, TOP_LEVEL, NULL);

            // Copy data from the MAPIAppointment to the output struct
            // DCB_PERFORMANCE WASTEFUL! Why don't we put it directly into the output struct to begin with?
            ApptItemData *ad = (ApptItemData *)&itemData;

            // ---------------------------------------------------
            // Subject
            // ---------------------------------------------------
            ad->Subject = mapiappointment.GetSubject();
            ad->Name    = mapiappointment.GetSubject();
            LOG_INFO(L"Subject:   %s",ad->Subject.c_str());

            // ---------------------------------------------------
            // Dates
            // ---------------------------------------------------
            ad->StartDate = mapiappointment.GetStartDate();
            LOG_TRACE(L"StartDate: %s",ad->StartDate.c_str());

            ad->EndDate = mapiappointment.GetEndDate();
            LOG_TRACE(L"EndDate:   %s",ad->EndDate.c_str());

            ad->CalFilterDate = mapiappointment.GetCalFilterDate();

            // ---------------------------------------------------
            // Misc
            // ---------------------------------------------------
            ad->Location = mapiappointment.GetLocation();

            if(!mapiappointment.GetResponseStatus().empty())
                ad->PartStat = mapiappointment.GetResponseStatus();
            else
                ad->PartStat = L"NE";

            ad->CurrStat = mapiappointment.GetCurrentStatus();

            if(!mapiappointment.GetResponseRequested().empty())
                ad->RSVP = mapiappointment.GetResponseRequested();
            else
                ad->RSVP = L"0";

            if(!mapiappointment.GetBusyStatus().empty())
                ad->FreeBusy = mapiappointment.GetBusyStatus();
            else
                ad->FreeBusy = L"F";

            if(!mapiappointment.GetAllday().empty())
                ad->AllDay = mapiappointment.GetAllday();
            else
                ad->AllDay = L"0";

            ad->Transparency = mapiappointment.GetTransparency();
            ad->ApptClass = mapiappointment.GetPrivate();
            if(mapiappointment.GetPrivate().empty())
                LOG_WARNING(_T("Appointment's status (public/private) cannot be fetched. Default status (Public) will be used."));

            ad->AlarmTrigger    = mapiappointment.GetReminderMinutes();
            ad->organizer.nam   = mapiappointment.GetOrganizerName();
            ad->organizer.addr  = mapiappointment.GetOrganizerAddr();
            ad->Uid             = mapiappointment.GetInstanceUID();
            ad->vTags           = pKeywords;
        
            // ---------------------------------------------------
            // Attendees
            // ---------------------------------------------------
            vector<Attendee*> v = mapiappointment.GetAttendees();
            for (size_t i = 0; i < v.size(); i++)
                ad->vAttendees.push_back((Attendee*)v[i]);

            // ---------------------------------------------------
            // Recurrence data
            // ---------------------------------------------------
            if (mapiappointment.IsRecurring())
            {
                ad->recurPattern = mapiappointment.GetRecurPattern();

                if (mapiappointment.GetRecurWkday().length() > 0)
                    ad->recurWkday = mapiappointment.GetRecurWkday();

                ad->recurInterval           = mapiappointment.GetRecurInterval();
                ad->recurCount              = mapiappointment.GetRecurCount();
                ad->recurEndDate            = mapiappointment.GetRecurEndDate();
                ad->recurDayOfMonth         = mapiappointment.GetRecurDayOfMonth();
                ad->recurMonthOccurrence    = mapiappointment.GetRecurMonthOccurrence();
                ad->recurMonthOfYear        = mapiappointment.GetRecurMonthOfYear();

                // if there are exceptions, deal with them
                vector<MAPIAppointment*> ex = mapiappointment.GetExceptions();
                for (size_t i = 0; i < ex.size(); i++)
                {
                    ad->vExceptions.push_back((MAPIAppointment*)ex[i]);
                }
            }

            // ---------------------------------------------------
            // Timezone props
            // ---------------------------------------------------
            ad->tzStart = mapiappointment.GetTimezoneStart();
            ad->tzEnd   = mapiappointment.GetTimezoneEnd();
            if (mapiappointment.IsRecurring())
                ad->tzLegacy = mapiappointment.GetTimezoneLegacy();

            ad->ExceptionType = mapiappointment.GetExceptionType();

            // ---------------------------------------------------
            // Attachments
            // ---------------------------------------------------
            vector<AttachmentInfo*> va = mapiappointment.GetAttachmentInfo();
            for (size_t i = 0; i < va.size(); i++)
                ad->vAttachments.push_back((AttachmentInfo*)va[i]);

            // ---------------------------------------------------
            // Message Part
            // ---------------------------------------------------
            MessagePart mp;
            mp.contentType = L"text/plain";
            mp.content = mapiappointment.GetPlainTextFileAndContent();
            ad->vMessageParts.push_back(mp);
            mp.contentType = L"text/html";
            mp.content = mapiappointment.GetHtmlFileAndContent();
            ad->vMessageParts.push_back(mp);
        }
        else 
        if (nItemType == ZT_TASKS)
        {
            MAPITask mapitask(*pGlobalMAPISession, msg);
            TaskItemData *td    = (TaskItemData *)&itemData;
            td->Subject         = mapitask.GetSubject();
            td->Importance      = mapitask.GetImportance();
            td->TaskStart       = mapitask.GetTaskStart();
            td->TaskFilterDate  = mapitask.GetTaskFilterDate();
            td->TaskDue         = mapitask.GetTaskDue();
            td->Status          = mapitask.GetTaskStatus();
            td->PercentComplete = mapitask.GetPercentComplete();
            td->TotalWork       = mapitask.GetTotalWork();
            td->ActualWork      = mapitask.GetActualWork();
            td->Companies       = mapitask.GetCompanies();
            td->Mileage         = mapitask.GetMileage();
            td->BillingInfo     = mapitask.GetBillingInfo();
            td->ApptClass       = mapitask.GetPrivate();
            td->vTags           = pKeywords;

            if (mapitask.IsTaskReminderSet())
                td->TaskFlagDueBy = mapitask.GetTaskFlagDueBy();

            // ---------------------------------------------------
            // Recurrence data
            // ---------------------------------------------------
            if (mapitask.IsRecurring())
            {
                // timezone
                //td->tz = mapitask.GetRecurTimezoneLegacy();   // We need to add support for this!

                td->recurPattern = mapitask.GetRecurPattern();

                if (mapitask.GetRecurWkday().length() > 0)
                    td->recurWkday = mapitask.GetRecurWkday();

                td->recurInterval           = mapitask.GetRecurInterval();
                td->recurCount              = mapitask.GetRecurCount();
                td->recurEndDate            = mapitask.GetRecurEndDate();
                td->recurDayOfMonth         = mapitask.GetRecurDayOfMonth();
                td->recurMonthOccurrence    = mapitask.GetRecurMonthOccurrence();
                td->recurMonthOfYear        = mapitask.GetRecurMonthOfYear();
            }

            // ---------------------------------------------------
            // Attachments
            // ---------------------------------------------------
            vector<AttachmentInfo*> va = mapitask.GetAttachmentInfo();
            for (size_t i = 0; i < va.size(); i++)
            {
                td->vAttachments.push_back((AttachmentInfo*)va[i]);
            }

            // ---------------------------------------------------
            // Message Part
            // ---------------------------------------------------
            MessagePart mp;
            mp.contentType = L"text/plain";
            mp.content = mapitask.GetPlainTextFileAndContent();
            td->vMessageParts.push_back(mp);
            mp.contentType = L"text/html";
            mp.content = mapitask.GetHtmlFileAndContent();
            td->vMessageParts.push_back(mp);
        }
    }
    catch (MAPIMessageException &mex)
    {
        lpwstrStatus = FormatExceptionInfo(mex.ErrCode(), (LPWSTR)mex.Description().c_str(), (LPSTR)mex.SrcFile().c_str(), mex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,mex.ShortDescription().c_str());
    }
    catch (MAPIContactException &cex)
    {
        lpwstrStatus = FormatExceptionInfo(cex.ErrCode(), (LPWSTR)cex.Description().c_str(), (LPSTR)cex.SrcFile().c_str(), cex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,cex.ShortDescription().c_str());
    }
    catch (MAPIAppointmentException &aex)
    {
        lpwstrStatus = FormatExceptionInfo(aex.ErrCode(), (LPWSTR)aex.Description().c_str(), (LPSTR)aex.SrcFile().c_str(), aex.SrcLine());
        LOG_ERROR(lpwstrStatus);
        Zimbra::Util::CopyString(lpwstrRetVal,aex.ShortDescription().c_str());
    }

ZM_EXIT: 
    if(lpwstrStatus)
        Zimbra::Util::FreeString(lpwstrStatus);
    return lpwstrRetVal;
}

LPWSTR CMapiAccount::GetOOOStateAndMsg()
{
    LOGFN_TRACE_NO;

    HRESULT hr;
    BOOL bIsOOO = FALSE;
    Zimbra::Util::ScopedInterface<IMAPIFolder> spInbox;
    ULONG objtype;

    LPWSTR lpwstrOOOInfo = new WCHAR[3];
    lstrcpy(lpwstrOOOInfo, L"0:");

    // first get the OOO state -- if TRUE, put a 1: in the return val, else put 0:
    Zimbra::Util::ScopedBuffer<SPropValue> pPropValues;
    hr = HrGetOneProp(m_pUserStore->GetInternalMAPIStore(), PR_OOF_STATE, pPropValues.getptr());
    if (SUCCEEDED(hr))
    {
        bIsOOO = pPropValues->Value.b;
    }

    Zimbra::Util::ScopedInterface<IMAPITable> pContents;
    SBinaryArray specialFolderIds = m_pUserStore->GetSpecialFolderIds();
    SBinary sbin = specialFolderIds.lpbin[EXSFID_INBOX];
    hr = m_pUserStore->OpenEntry(sbin.cb, (LPENTRYID)sbin.lpb, NULL, MAPI_BEST_ACCESS, &objtype, (LPUNKNOWN *)spInbox.getptr());
    if (FAILED(hr))
    {
        return lpwstrOOOInfo;
    }
    
    hr = spInbox->GetContentsTable(MAPI_ASSOCIATED | fMapiUnicode, pContents.getptr());
    if (FAILED(hr))
    {
        //LOG_ERROR(_T("could not get the contents table %x"), hr);
        return lpwstrOOOInfo;
    }
    // set the columns because we are only interested in the body
    SizedSPropTagArray(1, tags) = {1, { PR_BODY }};
 
    pContents->SetColumns((LPSPropTagArray) &tags, 0);

    // restrict the table only to IPM.Note.Rules.OofTemplate.Microsoft
    SPropValue propVal;
    propVal.dwAlignPad = 0;
    propVal.ulPropTag = PR_MESSAGE_CLASS;
    propVal.Value.lpszW = L"IPM.Note.Rules.OofTemplate.Microsoft";

    SRestriction r;
    r.rt = RES_PROPERTY;
    r.res.resProperty.lpProp = &propVal;
    r.res.resProperty.relop = RELOP_EQ;
    r.res.resProperty.ulPropTag = propVal.ulPropTag;

    // lets get the instance key for the store provider
    pContents->Restrict(&r, 0);

    // iterate over the rows looking for the folder in question
    ULONG ulRows = 0;

    pContents->GetRowCount(0, &ulRows);
    if (ulRows == 0)
    {
        return lpwstrOOOInfo;
    }

    Zimbra::Util::ScopedRowSet pRows;

    pContents->QueryRows(ulRows, 0, pRows.getptr());
    for (unsigned int i = 0; i < pRows->cRows; i++)
    {
        if (pRows->aRow[i].lpProps[0].ulPropTag == PR_BODY)
        {
            LPWSTR pwszOOOMsg = pRows->aRow[i].lpProps[0].Value.lpszW;
            int iOOOLen = lstrlen(pwszOOOMsg);
            delete lpwstrOOOInfo;
            lpwstrOOOInfo = new WCHAR[iOOOLen + 3];  // for the 0: or 1: and null terminator
            if (bIsOOO)
            {
                lstrcpy(lpwstrOOOInfo, L"1:");
            }
            else
            {
                lstrcpy(lpwstrOOOInfo, L"0:");
            }           
            lstrcat(lpwstrOOOInfo, pwszOOOMsg);
            break;
        }
    }
    return lpwstrOOOInfo;
}

LPCWSTR CMapiAccount::GetExchangeRules(vector<CRule> &vRuleList)
{
    LOGFN_TRACE_NO;

    LPCWSTR lpwstrStatus = NULL;

    SBinaryArray specialFolderIds = m_pUserStore->GetSpecialFolderIds();
    SBinary sbin = specialFolderIds.lpbin[EXSFID_INBOX];

    // Open inbox
    Zimbra::Util::ScopedInterface<IMAPIFolder> spInbox;
    ULONG objtype;
    HRESULT hr = m_pUserStore->OpenEntry(sbin.cb, (LPENTRYID)sbin.lpb, NULL, MAPI_BEST_ACCESS, &objtype, (LPUNKNOWN *)spInbox.getptr());
    if (FAILED(hr))
    {
        lpwstrStatus = FormatExceptionInfo(hr, L"Unable to get Inbox.", __FILE__, __LINE__);
        return lpwstrStatus;                                  
    }

    // Get PR_RULES_TABLE prop
    LPEXCHANGEMODIFYTABLE lpExchangeTable;
    hr = spInbox->OpenProperty(PR_RULES_TABLE, (LPGUID)&IID_IExchangeModifyTable, 0, MAPI_DEFERRED_ERRORS, (LPUNKNOWN FAR *)&lpExchangeTable);
    if (FAILED(hr))
    {
        lpwstrStatus = FormatExceptionInfo(hr, L"Unable to get Rules table property.", __FILE__, __LINE__);
        return lpwstrStatus;                                  
    }

    // Get rules table
    Zimbra::Util::ScopedInterface<IMAPITable> spMAPIRulesTable;
    hr = lpExchangeTable->GetTable(0, spMAPIRulesTable.getptr());
    if (FAILED(hr))
    {
        lpExchangeTable->Release();
        lpwstrStatus = FormatExceptionInfo(hr, L"Unable to get Rules table.", __FILE__, __LINE__);
        return lpwstrStatus;                                  
    }

    // Row count
    ULONG ulCount = 0;
    hr = spMAPIRulesTable->GetRowCount(0, &ulCount);
    if (FAILED(hr))
    {
        lpExchangeTable->Release();
        lpwstrStatus = FormatExceptionInfo(hr, L"Unable to get Rules table row count.", __FILE__, __LINE__);
        return lpwstrStatus;                                  
    }

    // Read all rules rows
    LPSRowSet pRows = NULL;
    hr = HrQueryAllRows(spMAPIRulesTable.get(), NULL, NULL, NULL, ulCount, &pRows);
    if (SUCCEEDED(hr))
    {
        ULONG lPos = 0;
        LPSRestriction pRestriction = 0;
        LPACTIONS pActions = 0;
        ULONG ulWarnings = 0;

        LOG_INFO(L"Begin Rules Migration");

        Zimbra::MAPI::MAPISession* pGlobalMAPISession = CSessionGlobal::GetGlobalMAPISession();
        CRuleProcessor* pRuleProcessor = new CRuleProcessor(pGlobalMAPISession, m_pUserStore, m_sZCSAccount);

        vRuleList.clear();
        while (lPos < ulCount)
        {
            pRestriction = 0;
            pActions = 0;

            bool bRes = true;
            bool bAct = true;

            CRule rule;
            rule.Clear();

            // ----------------
            // Rule name
            // ----------------
            std::wstring rulename = CA2W(pRows->aRow[lPos].lpProps[8].Value.lpszA);
            rule.SetName(rulename.c_str());
            rule.SetActive(1);                      // if it was inactive in Exchange, we'd never see it
            if (rulename.length() == 0)
            {
                LOG_WARNING(_T("Rule %d has no name"), lPos);
                lPos++;
                continue;
            }

            LOG_SUMMARY(L"----------------------------------------------------------------");
            LOG_SUMMARY(L"Processing rule '%s'", rulename.c_str());
            LOG_SUMMARY(L"----------------------------------------------------------------");

            // ----------------
            // Conditions
            // ----------------
            // Conditions are restrictions - call a recursive method to process them
            // 99 means this is not being called by another restriction
            pRestriction = (LPSRestriction)pRows->aRow[lPos].lpProps[5].Value.x;
            bRes = pRuleProcessor->ProcessRestrictions(rule, pRestriction, FALSE, 99);

            // check if there were restrictions we couldn't support, possibly causing there to be no conditions
            CListRuleConditions listRuleConditions;
            rule.GetConditions(listRuleConditions);
            if (listRuleConditions.size() == 0)
            {
                LOG_WARNING(_T("Rule %s has no conditions supported by Zimbra -- skipping"), rulename.c_str());
                bRes = false;
            }

            int ulRuleState = pRows->aRow[lPos].lpProps[3].Value.l;
            if (ulRuleState & ST_EXIT_LEVEL)        // weird looking code to get around compiler warning C4800
                rule.ProcessAdditionalRules(false);
            else
                rule.ProcessAdditionalRules(true);

            // ----------------
            // Actions
            // ----------------
            pActions = (LPACTIONS)pRows->aRow[lPos].lpProps[6].Value.x;
            bAct = pRuleProcessor->ProcessActions(rule, pActions);

            // check if there were actions we couldn't support, possibly causing there to be none
            CListRuleActions listRuleActions;
            rule.GetActions(listRuleActions);
            if (listRuleActions.size() == 0)
            {
                LOG_WARNING(_T("Rule %s has no actions supported by Zimbra -- skipping"), rulename.c_str());
                bAct = false;
            }

            // -------------------------
            // Done - add to list
            // -------------------------
            if (bRes && bAct)
            {
                vRuleList.push_back(rule);
                LOG_INFO(L"Processed rule '%s'", rulename.c_str());
            }
            else
            {
                LOG_WARNING(L"Unable to import rule %s", rulename.c_str());
                ulWarnings++;
            }

            lPos++;
        }

        // Cleanup
        delete pRuleProcessor;
        MAPIFreeBuffer(pRestriction);
        MAPIFreeBuffer(pActions);
    }

    return lpwstrStatus;
}

/* 
// OBSOLETE
// Access MAPI folder items
void CMapiAccount::traverse_folder(Zimbra::MAPI::MAPIFolder &folder)
{
    LOGFN_TRACE_NO;

    Zimbra::MAPI::MessageIterator *msgIter = new Zimbra::MAPI::MessageIterator();

    folder.GetMessageIterator(*msgIter);

    BOOL bContinue = true;
    while (bContinue)
    {
        Zimbra::MAPI::MAPIMessage *msg = new Zimbra::MAPI::MAPIMessage();

        bContinue = msgIter->GetNext(*msg);
        if (bContinue)
        {
            Zimbra::Util::ScopedBuffer<WCHAR> subject;

            if (msg->Subject(subject.getptr()))
                printf("\tsubject--%S\n", subject.get());
        }
        delete msg;
    }
    delete msgIter;
}
*/

