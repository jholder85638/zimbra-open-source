/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2014, 2015, 2016 Synacor, Inc.
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
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System;

using System.Runtime.InteropServices;

namespace CssLib
{
public class CSUnmanagedTestClass: IDisposable
{
    #region PInvokes
    [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl)] static private extern
    IntPtr GetInstance();

    [DllImport("CppLib.dll", CallingConvention = CallingConvention.Cdecl)] static private extern
    void DisposeTestClass(IntPtr pTestClassObject);

    /*
     * [DllImport("ExampleUnmanagedDLL.dll",
     *      EntryPoint="?PassInt@CUnmanagedTestClass@@QAEXH@Z",
     *      CallingConvention=CallingConvention.ThisCall)]
     * static private extern void PassInt(IntPtr pClassObject, int nValue);
     */

    [DllImport("CppLib.dll", CharSet = CharSet.Ansi, CallingConvention =
    CallingConvention.Cdecl)] static private extern void CallDoSomething(IntPtr
        pTestClassObject, string strValue, int type);

    #endregion PInvokes

    #region Members

    private IntPtr m_pNativeObject;             // Variable to hold the C++ class's this pointer

    #endregion Members

    public CSUnmanagedTestClass()
    {
        // We have to Create an instance of this class through an exported function
        this.m_pNativeObject = GetInstance();
    }

    public void Dispose()
    {
        Dispose(true);
    }

    protected virtual void Dispose(bool bDisposing)
    {
        if (this.m_pNativeObject != IntPtr.Zero)
        {
            // Call the DLL Export to dispose this class
            DisposeTestClass(this.m_pNativeObject);
            this.m_pNativeObject = IntPtr.Zero;
        }
        if (bDisposing)
        {
            // No need to call the finalizer since we've now cleaned
            // up the unmanaged memory
            GC.SuppressFinalize(this);
        }
    }

    // This finalizer is called when Garbage collection occurs, but only if
    // the IDisposable.Dispose method wasn't already called.
    ~CSUnmanagedTestClass()
    {
        Dispose(false);
    }

    #region Wrapper methods

    public void DoSomething(string strValue, int type)
    {
        CallDoSomething(this.m_pNativeObject, strValue, type);
    }

    #endregion Wrapper methods
}
}
