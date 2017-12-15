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

// COMMapiAccount.h : Declaration of the COMMapiAccount
// Places COM wrapper around MAPIAccessAPI so its accessible from C#

#pragma once
#include "resource.h"
#include "OaIdl.h"
#include "Wtypes.h"
#include "Exchange_i.h"
#include "MAPIDefs.h"
#include "Exchange.h"
#include "ExchangeAdmin.h"
#include "MAPIAccount.h"

#define NUM_ATTACHMENT_ATTRS      4
#define NUM_EXCEPTION_ATTRS      19

// COMMapiAccount

class ATL_NO_VTABLE COMMapiAccount :
    public CComObjectRootEx<CComMultiThreadModel>,
    public CComCoClass<COMMapiAccount, &CLSID_MapiAccount>,
    public ISupportErrorInfo,
    public IDispatchImpl<IMapiAccount, &IID_IMapiAccount, &LIBID_Exchange, /*wMajor =*/ 1, /*wMinor =*/ 0>
{
public:
    COMMapiAccount(){ m_pMapiAccount = NULL;}

    DECLARE_REGISTRY_RESOURCEID(IDR_COMMAPIACCOUNT)

    BEGIN_COM_MAP(COMMapiAccount)
        COM_INTERFACE_ENTRY(IMapiAccount)
        COM_INTERFACE_ENTRY(IDispatch)
        COM_INTERFACE_ENTRY(ISupportErrorInfo)
    END_COM_MAP()

    // ISupportsErrorInfo
    STDMETHOD(InterfaceSupportsErrorInfo)(REFIID riid);

    DECLARE_PROTECT_FINAL_CONSTRUCT()
    HRESULT FinalConstruct() {return S_OK;}
    void FinalRelease() {}

public:
    Zimbra::MAPI::CMapiAccount *m_pMapiAccount;

    // IMapiAccount methods - see exchange.idl "interface IMapiAccount"
    STDMETHOD   (InitMapiAccount)       (BOOL bIsServerMigration, BSTR bstrSrcAccount, BSTR bstrZCSAccount, BOOL bIsPublicFoldersMigration, BSTR *pErrorText);
    STDMETHOD   (UninitMapiAccount)     ();
    STDMETHOD   (GetFolders)            (VARIANT * folders);
    STDMETHOD   (GetFolderItems)        (IFolderObject * folderObj, VARIANT vCreationDate, VARIANT * vItems);
    STDMETHOD   (GetItemData)           (BSTR userId, VARIANT itemId, FolderType type, VARIANT* pRet);
    STDMETHODIMP GetOOO                 (BSTR *OOOInfo);
    STDMETHODIMP GetRules               (VARIANT *rules);

    void CreateAttachmentAttrs(BSTR attrs[], int numAtt, int numExcep);
    void CreateExceptionAttrs(BSTR attrs[], int num);

private:
    HRESULT InitAndEnumeratePublicFolders(BSTR * statusMsg);
    void LogFolders(vector<Folder_Data>& vFolders);
    HRESULT CppDictToVarArray(std::map<BSTR, BSTR>& mapDict, VARIANT *pVar);
    HRESULT InitCMapiAccount(BSTR bstrSrcAccount, BSTR bstrZCSAccount, BSTR *statusMsg);
    wstring GetZimSFStr(long lZimSFID);
    void CountSFs(vector<Folder_Data>& vFolders, ULONG& ulNumSpecialFolders, ULONG& ulNumDepth2Folders);
    void AddMissingSFIDs(vector<Folder_Data>& vFolders);
    void RemoveUnsupportedSpecialFolders(vector<Folder_Data>& vFolders);
};

OBJECT_ENTRY_AUTO(__uuidof(MapiAccount), COMMapiAccount)
