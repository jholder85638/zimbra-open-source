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

#pragma once

// ReminderEventListner.h : header file for the CReminderEventListener class
class CReminderEventListener: public IDispatch
{
protected:
    int m_refCount;
    IConnectionPoint *m_pConnectionPoint;
    DWORD m_dwConnection;

public:
    // Constructor.
    CReminderEventListener();
    // Destructor.
    ~CReminderEventListener();

    // IUnknown Methods
    STDMETHODIMP QueryInterface(REFIID riid, void **ppvObj);

    STDMETHODIMP_(ULONG) AddRef();
    STDMETHODIMP_(ULONG) Release();

    // IDispatch Methods
    STDMETHODIMP GetTypeInfoCount(UINT *iTInfo);
    STDMETHODIMP GetTypeInfo(UINT iTInfo, LCID lcid, ITypeInfo **ppTInfo);
    STDMETHODIMP GetIDsOfNames(REFIID riid, OLECHAR **rgszNames, UINT cNames, LCID lcid, DISPID *rgDispId);
    STDMETHODIMP Invoke(DISPID dispIdMember, REFIID riid, LCID lcid, WORD wFlags, DISPPARAMS *pDispParams, VARIANT *pVarResult, EXCEPINFO *pExcepInfo, UINT *puArgErr);

    // Attach/Detach from event source i.e. outlook
    STDMETHODIMP AttachToOutlook(IUnknown *pEventSource);
    STDMETHODIMP DetachFromOutlook();
};
