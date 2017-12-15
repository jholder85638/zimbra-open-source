/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2014, 2015, 2016 Synacor, Inc.
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
#include "exchange.h"
#include "ReminderEventListener.h"

// Outlook Reminder Events GUID
const IID IID_ReminderEvents = {
    0x000630b2, 0x0000, 0x0000, { 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 }
};

/*
 *   CReminderEventListener class implementation
 */

// Constructor.
CReminderEventListener::CReminderEventListener(): 
    m_pConnectionPoint(NULL), 
    m_dwConnection(0),
    m_refCount(1) 
{
    LOGFN_TRACE_NO;
}

// Destructor.
CReminderEventListener::~CReminderEventListener() 
{
    LOGFN_TRACE_NO;
}

/*
 *   IUnknown Interface methods
 */
STDMETHODIMP CReminderEventListener::QueryInterface(REFIID riid, void **ppvObj)
{
    if (riid == IID_IUnknown)
    {
        *ppvObj = static_cast<IUnknown *>(this);
    }
    else if (riid == IID_IDispatch)
    {
        *ppvObj = static_cast<IDispatch *>(this);
    }
    else if (riid == IID_ReminderEvents)
    {
        *ppvObj = static_cast<IDispatch *>(this);
    }
    else
    {
        *ppvObj = NULL;
        return E_NOINTERFACE;
    }
    static_cast<IUnknown *>(*ppvObj)->AddRef();
    return S_OK;
}

STDMETHODIMP_(ULONG) CReminderEventListener::AddRef() 
{
    return ++m_refCount;
}

STDMETHODIMP_(ULONG) CReminderEventListener::Release() 
{
    if (--m_refCount == 0)
    {
        delete this;
        return 0;
    }
    return m_refCount;
}

/*
 *   IDispatch Interface methods
 */

/*
 *   GetTypeInfoCount -- This function determines if the class supports type
 *   information interfaces or not. It places 1 in iTInfo if the class supports
 *   type information and 0 if it does not.
 */
STDMETHODIMP CReminderEventListener::GetTypeInfoCount(UINT *iTInfo)
{
    *iTInfo = 0;
    return S_OK;
}

/*
 *   GetTypeInfo -- Returns the type information for the class. For classes
 *   that do not support type information, this function returns E_NOTIMPL;
 */
STDMETHODIMP CReminderEventListener::GetTypeInfo(UINT, LCID, ITypeInfo **)
{
    return E_NOTIMPL;
}

/*
 *   GetIDsOfNames -- Takes an array of strings and returns an array of DISPIDs
 *   that correspond to the methods or properties indicated. If the name is not
 *   recognized, returns DISP_E_UNKNOWNNAME.
 */
STDMETHODIMP CReminderEventListener::GetIDsOfNames(REFIID, OLECHAR **, UINT, LCID, DISPID *)
{
    return E_NOTIMPL;
}

/*
 *   Invoke -- Takes a dispid and uses it for further operations
 */
STDMETHODIMP CReminderEventListener::Invoke(DISPID dispId, REFIID, LCID, WORD,
                                            DISPPARAMS *pDispParams, VARIANT *, EXCEPINFO *, UINT *)
{
    switch (dispId)
    {
        case 0x0000fa93:                            // BeforeReminderShow
            if (1 == pDispParams->cArgs)            // CANCEL ReminderShow
            {
                // Making out parameter TRUE will supress the Remindres dialog box
                *pDispParams->rgvarg[0].pboolVal = TRUE;
                return S_OK;
            }
            break;
        case 0x0000fa94:                            // ReminderAdd
        case 0x0000fa95:                            // ReminderChange
        case 0x0000fa96:                            // ReminderFire
        case 0x0000fa97:                            // ReminderRemove
        case 0x0000fa98:                            // Snooze
            break;
    }
    return S_OK;
}

/*
 *  AttachToSource -- This method attaches to an Reminder event source.
 */
STDMETHODIMP CReminderEventListener::AttachToOutlook(IUnknown *pEventSource)
{
    HRESULT hr = S_OK;
    IConnectionPointContainer *pConnPtContainer = NULL;

    hr = pEventSource->QueryInterface(IID_IConnectionPointContainer, (void **)&pConnPtContainer);
    if (SUCCEEDED(hr))
    {
        hr = pConnPtContainer->FindConnectionPoint(IID_ReminderEvents, &m_pConnectionPoint);
        if (SUCCEEDED(hr))
            hr = m_pConnectionPoint->Advise(this, &m_dwConnection);
        pConnPtContainer->Release();
    }
    return hr;
}

/*
 *  DetachFromSource -- This method detaches from an Reminder event source.
 */
STDMETHODIMP CReminderEventListener::DetachFromOutlook()
{
    HRESULT hr = S_OK;

    if (m_pConnectionPoint != NULL)
    {
        m_pConnectionPoint->Unadvise(m_dwConnection);
        m_pConnectionPoint->Release();
        m_pConnectionPoint = NULL;
    }
    return hr;
}
