/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010, 2013, 2014, 2016 Synacor, Inc.
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

#ifndef JVM_H
#define JVM_H

#include "stdafx.h"
#include "cfg.h"
#include <jni.h>

typedef jint (JNICALL *CreateVMProc)(JavaVM **pvm, void **penv, void *args);

class VirtualMachine {
public:
    VirtualMachine(Config &c);
    ~VirtualMachine();
    bool Run();
    void Stop();
    bool IsRunning() { return state == Running; }
    string &LastError() { return last_err; }

protected:
    enum State {Running, Stopped, Failed};

    HANDLE thread_handle;
    DWORD  thread_id;
    JavaVM *jvm;
    JNIEnv *env;
    jclass mcls;
    jclass wcls;
    Config &cfg;
    State state;
    string last_err;
    static const char *err_file;

    CreateVMProc FindCreateJavaVM(const char *vmlibpath);
    void RedirectIO();
    void CallMain();
    static DWORD WINAPI JvmThreadMain(LPVOID lpParam);
    static jint JNICALL zd_vfprintf(FILE *fp, const char *format, va_list args);
};

#endif JVM_H