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
// FolderObject.h : Declaration of the CFolderObject

#pragma once
#include "resource.h"                           // main symbols
#include "Exchange_i.h"
#include "MAPIDefs.h"

// ==========================================================================================================
// CFolderObject
// ==========================================================================================================

class ATL_NO_VTABLE CFolderObject :
    public CComObjectRootEx<CComMultiThreadModel>,
    public CComCoClass<CFolderObject, &CLSID_FolderObject>,
    public ISupportErrorInfo,
    public IDispatchImpl<IFolderObject, &IID_IFolderObject, &LIBID_Exchange, /*wMajor =*/ 1, /*wMinor =*/ 0>
{

public:
	CFolderObject(){}

    DECLARE_REGISTRY_RESOURCEID(IDR_FOLDEROBJECT)

    BEGIN_COM_MAP(CFolderObject)
	    COM_INTERFACE_ENTRY(IFolderObject)
	    COM_INTERFACE_ENTRY(IDispatch)
	    COM_INTERFACE_ENTRY(ISupportErrorInfo)
    END_COM_MAP()

    // ISupportsErrorInfo
	STDMETHOD(InterfaceSupportsErrorInfo)(REFIID riid);


	DECLARE_PROTECT_FINAL_CONSTRUCT()
	HRESULT FinalConstruct(){return S_OK;}
	void FinalRelease(){}

    STDMETHOD(get_Name) (BSTR *pVal);
    STDMETHOD(put_Name) (BSTR newVal);

    STDMETHOD(get_ZimbraSpecialFolderId) (LONG *pVal);
    STDMETHOD(put_ZimbraSpecialFolderId) (LONG newVal);

    STDMETHOD(get_FolderPath) (BSTR *pVal);
    STDMETHOD(put_FolderPath) (BSTR newVal);

    STDMETHOD(get_ContainerClass) (BSTR *pVal);
    STDMETHOD(put_ContainerClass) (BSTR newVal);

    STDMETHOD(put_EID) (VARIANT id);
    STDMETHOD(get_EID) (VARIANT * id);

    STDMETHOD(get_ItemCount) (LONG *pVal);
    STDMETHOD(put_ItemCount) (LONG newVal);

private:
    BSTR Strname;
    LONG LngID;
    BSTR folderPath;
    BSTR containerClass;
    SBinary FolderId;
    LONG Itemcnt;
};

OBJECT_ENTRY_AUTO(__uuidof(FolderObject), CFolderObject)
