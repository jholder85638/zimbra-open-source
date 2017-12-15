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
// MapiWrapper.h : Declaration of the COMMapiTools

#pragma once
#include "folderObject.h"
#include "..\Exchange\MAPIAccount.h"

class ATL_NO_VTABLE COMMapiTools: public CComObjectRootEx<CComSingleThreadModel>, 
                                  public CComCoClass<COMMapiTools, &CLSID_MapiTools>, 
                                  public ISupportErrorInfo, 
                                  public IDispatchImpl<IMapiTools, &IID_IMapiTools, &LIBID_Exchange, /*wMajor =*/ 1, /*wMinor =*/ 0>
{
public:
    COMMapiTools():
        m_pExchAdmin(NULL)
    //
    // Instantiated for ALL migration types
    // Called ONCE per migration run -> useful place for one-time C++ initialization (or better still CSessionGlobal::_InitGlobalSessionAndStore)
    {
        InitCrashDumper();

        m_pExchAdmin = new Zimbra::MAPI::ExchangeAdmin();

        try
        {
            //m_pLeakedMem1 = new BYTE[250*1024*1024];
            //m_pLeakedMem2 = new BYTE[250*1024*1024];
            //m_pLeakedMem3 = new BYTE[250*1024*1024];
        }
        catch(std::exception& e)
        {
            LOG_ERROR(_T("std::exception: %hs"), e.what());
            _ASSERT(FALSE);
        }
        catch(...)
        {
            _ASSERT(FALSE);
        }
    }

    DECLARE_REGISTRY_RESOURCEID(IDR_COMMAPITOOLS) 
    
    // The COM map is the mechanism that exposes interfaces on an object to a client through QueryInterface.
    BEGIN_COM_MAP(COMMapiTools) 
        COM_INTERFACE_ENTRY(IMapiTools) 
        COM_INTERFACE_ENTRY(IDispatch) 
        COM_INTERFACE_ENTRY(ISupportErrorInfo) 
    END_COM_MAP() 
    
    // -----------------------------------------------
    // ISupportErrorInfo methods
    // -----------------------------------------------
    STDMETHOD(InterfaceSupportsErrorInfo) (REFIID riid);

    DECLARE_PROTECT_FINAL_CONSTRUCT() 
    HRESULT FinalConstruct() 
    { 
        LOGFN_TRACE_NO;
        return S_OK; 
    }

    void FinalRelease() 
    {
        LOGFN_TRACE_NO;
    }

    // -----------------------------------------------
    // IMapiTools methods
    // -----------------------------------------------
    STDMETHOD(InitMapiTools)        (BSTR pProfileOrServerName, BSTR pAdminUser, BSTR pAdminPassword, BSTR *pErrorText);
    STDMETHOD(UninitMapiTools)      (BSTR *pErrorText);

    STDMETHOD(GetProfileList)       (VARIANT * Profiles,BSTR* status);
    STDMETHOD(SelectExchangeUsers)  (VARIANT * Users, BSTR *pErrorText);

    STDMETHOD(AvoidInternalErrors)  (BSTR lpToCmp, LONG *lRetval);

private:
    std::wstring str_to_wstr(const std::string &str);
    void InitCrashDumper();

private:
    Zimbra::MAPI::ExchangeAdmin *m_pExchAdmin;
    LPVOID m_pLeakedMem1;
    LPVOID m_pLeakedMem2;
    LPVOID m_pLeakedMem3;

};

OBJECT_ENTRY_AUTO(__uuidof(MapiTools), COMMapiTools)
