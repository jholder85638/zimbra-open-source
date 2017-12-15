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
// ItemObject.cpp : Implementation of CItemObject

#include "common.h"
#include "ItemObject.h"


// CItemObject

STDMETHODIMP CItemObject::InterfaceSupportsErrorInfo(REFIID riid)
{
	static const IID* const arr[] = { &IID_IItemObject };

	for (int i=0; i < sizeof(arr) / sizeof(arr[0]); i++)
	{
		if (InlineIsEqualGUID(*arr[i],riid))
			return S_OK;
	}
	return S_FALSE;
}


STDMETHODIMP CItemObject::get_ID(BSTR *pVal)
{
    CComBSTR str(ID);
    *pVal = str.m_str;
    return S_OK;
}

STDMETHODIMP CItemObject::put_ID(BSTR newVal)
{
    ID = newVal;
    return S_OK;
}

STDMETHODIMP CItemObject::get_Subject(BSTR *pVal)
{
    CComBSTR str(Subject);
    *pVal = str.m_str;
    return S_OK;
}

STDMETHODIMP CItemObject::put_Subject(BSTR newVal)
{
    Subject = newVal;
    return S_OK;
}

STDMETHODIMP CItemObject::get_FilterDate(BSTR *pVal)
{
    CComBSTR str(FilterDate);
    *pVal = str.m_str;
    return S_OK;
}

STDMETHODIMP CItemObject::put_FilterDate(BSTR newVal)
{
    FilterDate = newVal;
    return S_OK;
}

STDMETHODIMP CItemObject::get_Type(FolderType *pVal)
{
    *pVal = TYPE;
    return S_OK;
}

STDMETHODIMP CItemObject::put_Type(FolderType newVal)
{
    TYPE = newVal;
    return S_OK;
}

STDMETHODIMP CItemObject::get_MessageSize(ULONG *pVal)
{
    *pVal = ulMessageSize;
    return S_OK;
}

STDMETHODIMP CItemObject::put_MessageSize(ULONG newVal)
{
    ulMessageSize = newVal;
    return S_OK;
}

STDMETHODIMP CItemObject::get_CreationDate(VARIANT *pVal)
{
    _variant_t vt;
    *pVal = vt;
    return S_OK;
}

STDMETHODIMP CItemObject::put_CreationDate(VARIANT newVal)
{
    _variant_t vt = newVal;
    return S_OK;
}

STDMETHODIMP CItemObject::get_Parentfolder(IFolderObject **pVal)
{
    *pVal = parentObj;
    (*pVal)->AddRef();

    return S_OK;
}

STDMETHODIMP CItemObject::put_Parentfolder(IFolderObject *newVal)
{
    parentObj = newVal;
    return S_OK;
}

STDMETHODIMP CItemObject::get_IDasString(BSTR *pVal)
{
    /*Zimbra::Util::ScopedArray<CHAR> pszEid(NULL);
    Zimbra::Util::HexFromBin(ItemID->lpb, ItemID->cb, pszEid.get());
    LPTSTR pszCount = ULongToString(ItemID.cb);
    LPTSTR pRetVal = NULL;*/
    //LPTSTR pszHexEncoded = HexEncode(ItemID.cb, ItemID.lpb);

   *pVal = IDasString;
    return S_OK;
}

STDMETHODIMP CItemObject::put_IDasString(BSTR newVal)
{
    IDasString = newVal;
    return S_OK;
}


STDMETHODIMP CItemObject::GetDataForItemID(IMapiAccount *pSrcAccount, VARIANT ItemId, FolderType type, VARIANT *pVarArrayOut)
//
// Called by C# layer to obtain a single item (e.g. appt/contact) from MAPI
// Returns the item's data in VARIANT array pVal
{
    LOGFN_TRACE_NO;

    FolderType ft;
    if (type == NULL)
        get_Type(&ft);
    else
        ft = type;

    HRESULT hr = pSrcAccount->GetItemData(L"", ItemId, ft, pVarArrayOut);
    if(FAILED(hr))
    {
        LOG_ERROR(_T("GetData failed %08X"), hr);
        return hr;
    }
  
    return hr;
}

STDMETHODIMP CItemObject::put_ItemID(VARIANT id)
{
    // FolderId = id;
    // Binary data is stored in the variant as an array of unsigned char
    if (id.vt == (VT_ARRAY | VT_UI1))           // (OLE SAFEARRAY)
    {
        // Retrieve size of array
        ItemID.cb = id.parray->rgsabound[0].cElements;
        ItemID.lpb = new BYTE[ItemID.cb];       // Allocate a buffer to store the data
        if (ItemID.lpb != NULL)
        {
            // Obtain safe pointer to the array
            void *pArrayData;
            SafeArrayAccessData(id.parray, &pArrayData);

            // Copy the bitmap into our buffer
            memcpy(ItemID.lpb, pArrayData, ItemID.cb);  // Unlock the variant data
            SafeArrayUnaccessData(id.parray);
        }
    }
    return S_OK;
}

STDMETHODIMP CItemObject::get_ItemID(VARIANT *id)
{
    HRESULT hr = S_OK;

    VariantInit(id);
    id->vt = VT_ARRAY | VT_UI1;

    SAFEARRAY *psa;
    SAFEARRAYBOUND bounds[1]; // ={1,0};

    bounds[0].cElements = ItemID.cb;
    bounds[0].lLbound = 0;

    psa = SafeArrayCreate(VT_UI1, 1, bounds);
    if (psa != NULL)
    {
        void *pArrayData = NULL;
        SafeArrayAccessData(psa, &pArrayData);
        memcpy(pArrayData, ItemID.lpb, ItemID.cb);
        // Unlock the variant data
        // SafeArrayUnaccessData(var.parray);
        SafeArrayUnaccessData(psa);
        id->parray = psa;
    }
    return hr;
}
