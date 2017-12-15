﻿/*
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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Threading;
using System.Collections.Specialized;

namespace Misc
{
internal class CollectionChangedWrapperEventData
{
    public Dispatcher Dispatcher
    {
        get;
        set;
    }

    public Action<NotifyCollectionChangedEventArgs> Action
    {
        get;
        set;
    }
        
    public CollectionChangedWrapperEventData(Dispatcher dispatcher, Action<NotifyCollectionChangedEventArgs> action)
    {
        Dispatcher = dispatcher;
        Action = action;
    }
}
}
