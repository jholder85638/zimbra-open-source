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
#include "..\Exchange\Exchange.h"
#include "..\Exchange\ExchangeAdmin.h"
#include "Zimbra/ZimbraRpc.h"

#pragma comment(lib, "netapi32.lib")

#include <lm.h>

#define PROFILE_MIGARTION 0

LPCWSTR lpProfileName       = L"testprofile";
LPCWSTR lpExchangeServer    = L"10.112.16.164";
LPCWSTR lpServerAddress     = L"10.117.82.163";
LPCWSTR lpAdminUser         = L"admin@zcs2.zmexch.in.zimbra.com";
LPCWSTR lpAccountUser       = L"av1@zcs2.zmexch.in.zimbra.com";
LPCWSTR lpAccountUserPwd    = L"test123";
LPCWSTR lpAdminPwd          = L"z1mbr4";
ULONG nAdminPort            = 7071;
ULONG nPort                 = 80;

Zimbra::Rpc::Connection *m_pConnection = NULL;

void Init()
{
    Zimbra::Mapi::Memory::SetMemAllocRoutines(NULL, MAPIAllocateBuffer, MAPIAllocateMore, MAPIFreeBuffer);
}

void AdminAuth()
{
    Zimbra::Rpc::AdminAuthRequest authRequest(lpAdminUser, lpAdminPwd, L"");
    Zimbra::Rpc::AdminConnection *m_pAdminConnection;
    Zimbra::Util::ScopedInterface<IXMLDOMDocument2> pResponseXml;

    m_pAdminConnection = new Zimbra::Rpc::AdminConnection(lpServerAddress, nAdminPort, TRUE, 0, L"");
    m_pAdminConnection->SetCurrentUser((LPWSTR)lpAccountUser);
    m_pAdminConnection->SendRequestSynchronous(authRequest, pResponseXml.getref(), __FUNCTIONW__, __LINE__);
}

void UserAuth()
{
    Zimbra::Util::ScopedInterface<IXMLDOMDocument2> pResponseXml;

    m_pConnection = new Zimbra::Rpc::Connection(L"migration", lpServerAddress, nPort, false, 0, L"");
    m_pConnection->SetCurrentUser((LPWSTR)lpAccountUser);

    Zimbra::Rpc::AuthRequest authRequest(lpAccountUser, lpAccountUserPwd, lpServerAddress);
    m_pConnection->SendRequestSynchronous(authRequest, pResponseXml.getref(), __FUNCTIONW__, __LINE__);

    Zimbra::Util::ScopedPtr<Zimbra::Rpc::Response> pResponse(
    Zimbra::Rpc::Response::Manager::NewResponse(pResponseXml.get()));
}

BOOL FileUpload(Zimbra::Rpc::ZimbraConnection *z_connection, LPWSTR *ppwszToken)
{
    LOGFN_INTERNAL_NO;

    LPSTR pszTestFile = "E:\\temp\\aa.log";
    LPSTREAM pStreamFile = NULL;
    HRESULT hr = OpenStreamOnFile(MAPIAllocateBuffer, MAPIFreeBuffer, STGM_READ, (LPWSTR)pszTestFile, NULL, &pStreamFile);
    if (FAILED(hr))
    {
        LOG_ERROR(_T("failed to OpenStreamOnFile call: %x"), hr);
        return FALSE;
    }

    HANDLE hFile = CreateFile(L"E:\\temp\\aa.log", GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    DWORD dwFileSize = GetFileSize(hFile, NULL);

    CloseHandle(hFile);
    _tprintf(_T("  File Upload size=%d bytes\n"), dwFileSize);

    BOOL bResult = z_connection->DoPutSynchronousZC(pStreamFile, dwFileSize, L"/service/upload?fmt=raw", ppwszToken, NULL, __FUNCTIONW__, __LINE__);

    pStreamFile->Release();

    return bResult;
}

void ZCFileUploadTest()
{
    Zimbra::Rpc::UserSession *session = Zimbra::Rpc::UserSession::CreateInstance(lpProfileName, lpAccountUser, lpAccountUserPwd, lpServerAddress, nPort, 0, 0, L"", false);
    //Zimbra::Rpc::UserSession::SetProfileName(lpProfileName);
    Zimbra::Rpc::ZimbraConnection *z_pConnection = new Zimbra::Rpc::ZimbraConnection(L"migration", lpServerAddress, nPort, false, 0, L"");

    z_pConnection->SetProfileName(lpProfileName);

    LPWSTR pwszToken = NULL;

    FileUpload(z_pConnection, &pwszToken);

    Zimbra::Rpc::SendMsgRequest request(pwszToken);

    // free the token right away
    z_pConnection->FreeBuff(pwszToken);

    request.SetAuthTokenNode(session->AuthToken());
    if (session->SetNoSession())
        request.SetNoSessionNode();
    Zimbra::Rpc::ScopedRPCResponse pResponse;

    try
    {
        z_pConnection->SendRequestSynchronousZC(request, pResponse.getref(), __FUNCTIONW__, __LINE__);
    }
    catch (Zimbra::Rpc::SoapFaultResponse &fault)
    {
        LOG_ERROR(_T("Response is soap error exception (%s)."), fault.ErrorCode());
        return;
    }
}

void CreateExchangeMailBox()
{
    Zimbra::MAPI::ExchangeAdmin *exchadmin = new Zimbra::MAPI::ExchangeAdmin();
    exchadmin->SetExchServerName(lpExchangeServer);

    try
    {
        try
        {
            exchadmin->DeleteProfile(Zimbra::MAPI::TEMP_ADMIN_PROFILE_NAME);
        }
        catch (Zimbra::MAPI::ExchangeAdminException &ex)
        {
            UNREFERENCED_PARAMETER(ex);
        }
        try
        {
            exchadmin->DeleteExchangeMailBox(Zimbra::MAPI::TEMP_ADMIN_MAILBOX_NAME, L"Administrator", L"z1mbr4Migration");
        }
        catch (Zimbra::MAPI::ExchangeAdminException &ex)
        {
            UNREFERENCED_PARAMETER(ex);
        }

        exchadmin->CreateExchangeMailBox(Zimbra::MAPI::TEMP_ADMIN_MAILBOX_NAME, L"z1mbr4Migration", L"Administrator", L"z1mbr4Migration");
        exchadmin->CreateProfile(Zimbra::MAPI::TEMP_ADMIN_PROFILE_NAME, Zimbra::MAPI::TEMP_ADMIN_MAILBOX_NAME,  L"z1mbr4Migration");
        exchadmin->SetDefaultProfile(Zimbra::MAPI::TEMP_ADMIN_PROFILE_NAME);
    }
    catch (Zimbra::MAPI::ExchangeAdminException &ex)
    {
        UNREFERENCED_PARAMETER(ex);
    }
    delete exchadmin;
}

void GetAllProfiles()
{
    Zimbra::MAPI::ExchangeAdmin *exchadmin = new Zimbra::MAPI::ExchangeAdmin();
    exchadmin->SetExchServerName(lpExchangeServer);

    vector<string> vProfileList;
    exchadmin->GetAllProfiles(vProfileList);
    vector<string>::iterator itr = vProfileList.begin();
    delete exchadmin;
}

void ExchangeMigrationSetupTest()
{
    Zimbra::MAPI::ExchangeAdmin *exchadmin = new Zimbra::MAPI::ExchangeAdmin();
    exchadmin->SetExchServerName(lpExchangeServer);

    // If Profile exists, rest are Optional else rest 3 params must be there!!!
    LPCWSTR lpwstrStatus = exchadmin->InitExchangeAdmin(L"Outlook");  // lpExchangeServer,L"Administrator",L"z1mbr4Migration");//(L"10.20.141.161", L"fbs",L"Test7777");//
    if (lpwstrStatus)
        delete[] lpwstrStatus;

    lpwstrStatus = exchadmin->UninitExchangeAdmin();
    if (lpwstrStatus)
        delete[] lpwstrStatus;

    vector<ObjectPickerData> vUserList;
    lpwstrStatus = exchadmin->SelectExchangeUsers(vUserList);
    if (lpwstrStatus)
        delete[] lpwstrStatus;
}

typedef struct
{
    wstring mailboxname;
} migrationThreadParams;

DWORD WINAPI AccountMigrationThread(LPVOID lpParameter)
{
    migrationThreadParams *mtparams = (migrationThreadParams *)lpParameter;

    vector<Folder_Data> vfolderlist;

    Zimbra::MAPI::CMapiAccount *pMapiAccount = NULL;

    if (PROFILE_MIGARTION)
    {
        pMapiAccount = new Zimbra::MAPI::CMapiAccount(L"", L"");
    }
    else
    {
        // Create class instance with Exchange mailbox to be migrated
        pMapiAccount = new Zimbra::MAPI::CMapiAccount(mtparams->mailboxname, L"");
        printf("MAILBOXNAME: %S\n", mtparams->mailboxname.c_str());
    }

    // Init user stores
    LPCWSTR lpStatus = pMapiAccount->LogonAndGetRootFolder();

    if (lpStatus)
        return 1;

    // Get all folders
    pMapiAccount->GetRootFolderHierarchy(vfolderlist);

    //
    vector<Item_Data> vItemDataList;
    vector<Folder_Data>::iterator it;

    vector<Item_Data>::iterator idItr;
    for (it = vfolderlist.begin(); it != vfolderlist.end(); it++)
    {
        if (!PROFILE_MIGARTION)
            printf("MailboxName: %S ", mtparams->mailboxname.c_str());
        printf("FolderName:  %S ", (*it).name.c_str());
        printf("FolderPath: %S ", (*it).folderpath.c_str());
        printf("ContainerClass: %S ", (*it).containerclass.c_str());
        printf("ItemCount: %d ", (*it).itemcount);
        printf("ZimbraId: %d\n", (*it).zimbraspecialfolderid);
        printf("\n\n");

        SBinary sbin = (*it).sbin;

        pMapiAccount->GetFolderItems(sbin, vItemDataList);
        for (idItr = vItemDataList.begin(); idItr != vItemDataList.end(); idItr++)
        {
            ContactItemData cd;

            if ((*idItr).lItemType == ZT_MAIL)
            {
                MessageItemData msgdata;

                printf("Got message item:");
                pMapiAccount->GetItem((*idItr).sbMessageID, msgdata);
                printf(
                    "Subject: %S Date: %I64X DateString:%S		\
                    DeliveryDate: %I64X deliveryDateString: %S		\
                    Has Attachments: %d Has HTML:%d Has Text:%d	\
                    Is Draft:%d Is Flagged: %d Is Forwarded: %d	\
                    IsFromMe:%d IsUnread:%d IsUnsent:%d IsRepliedTo:%d	\
                    URLName: %S\n"                                                                                                                                                                                                                                                   ,
                    msgdata.Subject.c_str(), msgdata.Date, msgdata.DateString.c_str(),
                    msgdata.deliveryDate, msgdata.DeliveryDateString.c_str(),
                    msgdata.HasAttachments, msgdata.HasHtml, msgdata.HasText, msgdata.IsDraft,
                    msgdata.IsFlagged, msgdata.IsForwarded, msgdata.IsFromMe, msgdata.IsUnread,
                    msgdata.IsUnsent, msgdata.RepliedTo, msgdata.Urlname.c_str());

                printf("MIME FILE PATH: %S\n\n\n\n", msgdata.MimeFile.c_str());
                // Delete the mime file
                // DeleteFile(msgdata.MimeFile.c_str());
            }
            else if ((*idItr).lItemType == ZT_CONTACTS)
            {
                printf("Got contact item:");
                pMapiAccount->GetItem((*idItr).sbMessageID, cd);
                printf("CONTACT_TYPE: %S\n\n", cd.Type.c_str());
                printf(
                    "%S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S			\
                    %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S		\
                    %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S\n "                                                                                                            ,
                    cd.Birthday.c_str(), cd.CallbackPhone.c_str(), cd.CarPhone.c_str(),
                    cd.Company.c_str(), cd.Email1.c_str(), cd.Email2.c_str(), cd.Email3.c_str(),
                    cd.FileAs.c_str(), cd.FirstName.c_str(), cd.HomeCity.c_str(),
                    cd.HomeCountry.c_str(), cd.HomeFax.c_str(), cd.HomePhone.c_str(),
                    cd.HomePhone2.c_str(), cd.HomePostalCode.c_str(), cd.HomeState.c_str(),
                    cd.HomeStreet.c_str(), cd.HomeURL.c_str(), cd.IMAddress1.c_str(),
                    cd.JobTitle.c_str(), cd.LastName.c_str(), cd.MiddleName.c_str(),
                    cd.MobilePhone.c_str(), cd.NamePrefix.c_str(), cd.NameSuffix.c_str(),
                    cd.NickName.c_str(), cd.Notes.c_str(), cd.OtherCity.c_str(),
                    cd.OtherCountry.c_str(), cd.OtherFax.c_str(), cd.OtherPhone.c_str(),
                    cd.OtherPostalCode.c_str(), cd.OtherState.c_str(), cd.OtherStreet.c_str(),
                    cd.OtherURL.c_str(), cd.Pager.c_str(), cd.pDList.c_str(),
                    cd.PictureID.c_str(), cd.Type.c_str(), cd.UserField1.c_str(),
                    cd.UserField2.c_str(), cd.UserField3.c_str(), cd.UserField4.c_str(),
                    cd.WorkCity.c_str(), cd.WorkCountry.c_str(), cd.WorkFax.c_str(),
                    cd.WorkPhone.c_str(), cd.WorkPostalCode.c_str(), cd.WorkState.c_str(),
                    cd.WorkStreet.c_str(), cd.WorkURL.c_str(), cd.Anniversary.c_str(), cd.Department.c_str(), cd.NickName.c_str(),
                    cd.AssistantPhone.c_str(), cd.WorkPhone2.c_str(), cd.CompanyPhone.c_str(), cd.PrimaryPhone.c_str(), cd.pDList.c_str());

                if(cd.UserDefinedFields.size())
                {
                    printf("User Defined Field:\n");
                    vector<ContactUDFields>::iterator it;
                    for (it= cd.UserDefinedFields.begin();it != cd.UserDefinedFields.end();it++)
                    {
                        printf("%S : %S \n", it->Name.c_str(), it->value.c_str());
                    }
                }
                
                printf("Contact Image Path: %S \n", cd.ContactImagePath.c_str());
            }
            else
            {
                printf("PSTMIG: %d Skipping it...\n", (*idItr).lItemType);
            }
            FreeEntryID((*idItr).sbMessageID);
        }
        FreeEntryID(sbin);
        vItemDataList.clear();
    }
    delete pMapiAccount;
    return 0;
}

void MAPIAccessAPITestV()
{
    if (PROFILE_MIGARTION)
    {
        // Outlook profile to be migrated
        CSessionGlobal::InitGlobalSessionAndStore(L"user1", FALSE);        // (L"E:\\temp\\PST\\OriginalEmailCalendar.pst");//
        HANDLE hThread = ::CreateThread(NULL, 0, AccountMigrationThread, NULL, 0L, NULL);
        WaitForSingleObject(hThread, INFINITE); // wait till all finished
        CloseHandle(hThread);
    }
    else
    {
        // Create Session and Open admin store.
        CSessionGlobal::InitGlobalSessionAndStore(L"Outlook", FALSE);
        DWORD const MAX_THREADS = 1;
        HANDLE hThreadArray[MAX_THREADS] = { 0 };
        migrationThreadParams mtparams[MAX_THREADS];

        mtparams[0].mailboxname = L"av1";

        /*	mtparams[1].mailboxname = L"av9 av9";
         *      mtparams[2].mailboxname = L"av1";
         *      mtparams[3].mailboxname = L"av2 av2";
         *      mtparams[4].mailboxname = L"av3 av3";
         *      mtparams[5].mailboxname = L"av4";
         *      mtparams[6].mailboxname = L"av5";
         *      mtparams[7].mailboxname = L"av7 av7";
         *      mtparams[8].mailboxname = L"appt1";
         */

        // One thread per mailbox.
        for (int i = 0; i < MAX_THREADS; i++)
        {
            hThreadArray[i] = ::CreateThread(NULL, 0, AccountMigrationThread, &mtparams[i], 0L, NULL);
        }
        // wait till all finished
        WaitForMultipleObjects(MAX_THREADS, hThreadArray, TRUE, INFINITE);

        // close handles
        for (int i = 0; i < MAX_THREADS; i++)
        {
            CloseHandle(hThreadArray[i]);
        }
    }

    // destroy session and admin store.
    CSessionGlobal::UnInitGlobalSessionAndStore();
}

void MigratePublicFolder()
{
    CoInitialize(NULL);
    MAPIInitialize(NULL);

    // Create Session and Open admin store.
    LPCWSTR lpwstrStatus = CSessionGlobal::InitGlobalSessionAndStore(L"Outlook",L"EXCH_ADMIN", TRUE);
    if(lpwstrStatus)
    {
        printf("InitGlobalSessionAndStore Failed: %S  exiting in 5 seconds.......", lpwstrStatus);
        Sleep(5000);
        return; 
    }

    Zimbra::MAPI::CMapiAccount *pMapiAccount = NULL;
    pMapiAccount = new Zimbra::MAPI::CMapiAccount(L"", L"");
    pMapiAccount->InitializePublicFolders();
    
    std::vector<std::string> pubFldrList;
    
    //enumerate public folder
    pMapiAccount->EnumeratePublicFolders(pubFldrList); 

    //print pblic folder
    vector<std::string>::iterator pfenumItr;
    printf("Enumerated Public folders:\n");
    for (pfenumItr = pubFldrList.begin(); pfenumItr != pubFldrList.end(); pfenumItr++)
    {
        printf("- %s \n", (*pfenumItr).c_str());
    }
    
    // Get data from public folders	
    vector<Folder_Data> vfolderlist;
    pMapiAccount->GetRootFolderHierarchy(vfolderlist);
    //
    vector<Item_Data> vItemDataList;
    vector<Folder_Data>::iterator it;

    vector<Item_Data>::iterator idItr;
    for (it = vfolderlist.begin(); it != vfolderlist.end(); it++)
    {
        printf("FolderName:  %S ", (*it).name.c_str());
        printf("FolderPath: %S ", (*it).folderpath.c_str());
        printf("ContainerClass: %S ", (*it).containerclass.c_str());
        printf("ItemCount: %d ", (*it).itemcount);
        printf("ZimbraId: %d\n", (*it).zimbraspecialfolderid);
        printf("\n\n");

        SBinary sbin = (*it).sbin;

        pMapiAccount->GetFolderItems(sbin, vItemDataList);

        for (idItr = vItemDataList.begin(); idItr != vItemDataList.end(); idItr++)
        {
            if ((*idItr).lItemType == ZT_MAIL)
            {
                MessageItemData msgdata;

                printf("Got message item:");
                pMapiAccount->GetItem((*idItr).sbMessageID, msgdata);
                printf(
                    "Subject: %S Date: %I64X DateString:%S		\
                    DeliveryDate: %I64X deliveryDateString: %S		\
                    Has Attachments: %d Has HTML:%d Has Text:%d	\
                    Is Draft:%d Is Flagged: %d Is Forwarded: %d	\
                    IsFromMe:%d IsUnread:%d IsUnsent:%d IsRepliedTo:%d	\
                    URLName: %S\n"                                                                                                                                                                                                                                                   ,
                    msgdata.Subject.c_str(), msgdata.Date, msgdata.DateString.c_str(),
                    msgdata.deliveryDate, msgdata.DeliveryDateString.c_str(),
                    msgdata.HasAttachments, msgdata.HasHtml, msgdata.HasText, msgdata.IsDraft,
                    msgdata.IsFlagged, msgdata.IsForwarded, msgdata.IsFromMe, msgdata.IsUnread,
                    msgdata.IsUnsent, msgdata.RepliedTo, msgdata.Urlname.c_str());

                //printf("MIME Buffer: %S\n\n\n\n",msgdata.wsMimeBuffer.c_str());
            }
            else if ((*idItr).lItemType == ZT_CONTACTS)
            {
                ContactItemData cd;
                printf("Got contact item:");
                pMapiAccount->GetItem((*idItr).sbMessageID, cd);
                printf("CONTACT_TYPE: %S\n\n", cd.Type.c_str());
                printf(
                    "%S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S			\
                    %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S		\
                    %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S %S\n "                                                                                                            ,
                    cd.Birthday.c_str(), cd.CallbackPhone.c_str(), cd.CarPhone.c_str(),
                    cd.Company.c_str(), cd.Email1.c_str(), cd.Email2.c_str(), cd.Email3.c_str(),
                    cd.FileAs.c_str(), cd.FirstName.c_str(), cd.HomeCity.c_str(),
                    cd.HomeCountry.c_str(), cd.HomeFax.c_str(), cd.HomePhone.c_str(),
                    cd.HomePhone2.c_str(), cd.HomePostalCode.c_str(), cd.HomeState.c_str(),
                    cd.HomeStreet.c_str(), cd.HomeURL.c_str(), cd.IMAddress1.c_str(),
                    cd.JobTitle.c_str(), cd.LastName.c_str(), cd.MiddleName.c_str(),
                    cd.MobilePhone.c_str(), cd.NamePrefix.c_str(), cd.NameSuffix.c_str(),
                    cd.NickName.c_str(), cd.Notes.c_str(), cd.OtherCity.c_str(),
                    cd.OtherCountry.c_str(), cd.OtherFax.c_str(), cd.OtherPhone.c_str(),
                    cd.OtherPostalCode.c_str(), cd.OtherState.c_str(), cd.OtherStreet.c_str(),
                    cd.OtherURL.c_str(), cd.Pager.c_str(), cd.pDList.c_str(),
                    cd.PictureID.c_str(), cd.Type.c_str(), cd.UserField1.c_str(),
                    cd.UserField2.c_str(), cd.UserField3.c_str(), cd.UserField4.c_str(),
                    cd.WorkCity.c_str(), cd.WorkCountry.c_str(), cd.WorkFax.c_str(),
                    cd.WorkPhone.c_str(), cd.WorkPostalCode.c_str(), cd.WorkState.c_str(),
                    cd.WorkStreet.c_str(), cd.WorkURL.c_str(), cd.Anniversary.c_str(), cd.Department.c_str(), cd.NickName.c_str(),
                    cd.AssistantPhone.c_str(), cd.WorkPhone2.c_str(), cd.CompanyPhone.c_str(), cd.PrimaryPhone.c_str(), cd.pDList.c_str());
                if(cd.UserDefinedFields.size())
                {
                    printf("User Defined Field:\n");
                    vector<ContactUDFields>::iterator it;
                    for (it= cd.UserDefinedFields.begin();it != cd.UserDefinedFields.end();it++)
                    {
                        printf("%S : %S \n", it->Name.c_str(), it->value.c_str());
                    }
                }
                
                printf("Contact Image Path: %S \n", cd.ContactImagePath.c_str());
            }
            else if ((*idItr).lItemType == ZT_APPOINTMENTS)
            {
                ApptItemData ad;
                printf("Got appointment item:");
                pMapiAccount->GetItem((*idItr).sbMessageID, ad);
                printf("%S %S %S %S %S \n\n\n\n", ad.Subject.c_str(), ad.Name.c_str(), ad.StartDate.c_str(), ad.EndDate.c_str(),ad.Location.c_str());
            }
            else if ((*idItr).lItemType == ZT_TASKS)
            {
                TaskItemData tid;
                printf("Got task item:");
                pMapiAccount->GetItem((*idItr).sbMessageID, tid);
                printf("%S %S %S %S %S \n\n\n\n", tid.Subject.c_str(), tid.Status.c_str(), tid.TaskStart.c_str(), tid.TaskDue.c_str(),tid.PercentComplete.c_str());
            }
            else
            {
                printf("PSTMIG: %d Skipping it...\n", (*idItr).lItemType);
            }
            FreeEntryID((*idItr).sbMessageID);
        }
        FreeEntryID(sbin);
        vItemDataList.clear();
    }

    delete pMapiAccount;

     // destroy session and admin store.
    CSessionGlobal::UnInitGlobalSessionAndStore();
    CSessionGlobal::UninitMAPI();
}

void GetDomainName()
{
    DWORD dwLevel = 102;
    LPWKSTA_INFO_102 pBuf = NULL;
    NET_API_STATUS nStatus;
    LPWSTR pszServerName = NULL;

    nStatus = NetWkstaGetInfo(pszServerName, dwLevel, (LPBYTE *)&pBuf);
    //
    // If the call is successful,
    // print the workstation data.
    //
    if (nStatus == NERR_Success)
    {
        printf("\n\tPlatform: %d\n", pBuf->wki102_platform_id);
        wprintf(L"\tName:     %s\n", pBuf->wki102_computername);
        printf("\tVersion:  %d.%d\n", pBuf->wki102_ver_major, pBuf->wki102_ver_minor);
        wprintf(L"\tDomain:   %s\n", pBuf->wki102_langroup);
        wprintf(L"\tLan Root: %s\n", pBuf->wki102_lanroot);
        wprintf(L"\t# Logged On Users: %d\n", pBuf->wki102_logged_on_users);
    }
    //
    // Otherwise, indicate the system error.
    //
    else
    {
        fprintf(stderr, "A system error has occurred: %d\n", nStatus);
    }
    //
    // Free the allocated memory.
    //
    if (pBuf != NULL)
        NetApiBufferFree(pBuf);
}

int main(int argc, TCHAR *argv[])
{
    UNREFERENCED_PARAMETER(argc);
    UNREFERENCED_PARAMETER(argv);

    // AdminAuth();
    // UserAuth();
    // ZCFileUploadTest();
    // CreateExchangeMailBox();
    // GetAllProfiles();
    // GetDomainName();
    //    MAPIAccessAPITestV();
    // Zimbra::MAPI::Util::ReverseDelimitedString(L"lb1/tv2/cr3/Inbox/TopFolder",L"/");
    // ExchangeMigrationSetupTest();
    // CreateExchangeMailBox();

    MigratePublicFolder();
    return 0;
}
