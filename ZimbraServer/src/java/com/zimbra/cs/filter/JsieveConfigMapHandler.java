/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.util.ZimbraLog;

/**
 * Handler class for jSieve's configuration map, such as CommandMap & TestMap.
 * These are registered to Configuration Manger to create sieve factory.
 */
public class JsieveConfigMapHandler {

    /*
     * jSieve's command map
     */
    private static final Map<String, String> mCommandMap = createDefaultCommandMap();

    /*
     * jSieve's test map
     */
    private static final Map<String, String> mTestMap = createDefaultTestMap();

    private static Map<String, String> createDefaultCommandMap() {

        Map<String, String> mCommandMap =
                Collections.synchronizedMap(new HashMap<String, String>());
        mCommandMap.put("disabled_if", com.zimbra.cs.filter.jsieve.DisabledIf.class.getName());
        mCommandMap.put("tag", com.zimbra.cs.filter.jsieve.Tag.class.getName());
        mCommandMap.put("flag", com.zimbra.cs.filter.jsieve.Flag.class.getName());
        mCommandMap.put("reply", com.zimbra.cs.filter.jsieve.Reply.class.getName());
        mCommandMap.put("notify", com.zimbra.cs.filter.jsieve.Notify.class.getName());
        mCommandMap.put("discard", com.zimbra.cs.filter.jsieve.Discard.class.getName());

        return mCommandMap;
    }

    private static Map<String, String> createDefaultTestMap() {

        Map<String, String> mTestMap =
                Collections.synchronizedMap(new HashMap<String, String>());
        mTestMap.put("date", com.zimbra.cs.filter.jsieve.DateTest.class.getName());
        mTestMap.put("body", com.zimbra.cs.filter.jsieve.BodyTest.class.getName());
        mTestMap.put("attachment", com.zimbra.cs.filter.jsieve.AttachmentTest.class.getName());
        mTestMap.put("addressbook", com.zimbra.cs.filter.jsieve.AddressBookTest.class.getName());
        mTestMap.put("contact_ranking", com.zimbra.cs.filter.jsieve.ContactRankingTest.class.getName());
        mTestMap.put("me", com.zimbra.cs.filter.jsieve.MeTest.class.getName());
        mTestMap.put("invite", com.zimbra.cs.filter.jsieve.InviteTest.class.getName());
        mTestMap.put("mime_header", com.zimbra.cs.filter.jsieve.MimeHeaderTest.class.getName());
        mTestMap.put("current_time", com.zimbra.cs.filter.jsieve.CurrentTimeTest.class.getName());
        mTestMap.put("current_day_of_week", com.zimbra.cs.filter.jsieve.CurrentDayOfWeekTest.class.getName());
        mTestMap.put("conversation", com.zimbra.cs.filter.jsieve.ConversationTest.class.getName());
        mTestMap.put("facebook", com.zimbra.cs.filter.jsieve.FacebookTest.class.getName());
        mTestMap.put("linkedin", com.zimbra.cs.filter.jsieve.LinkedInTest.class.getName());
        mTestMap.put("socialcast", com.zimbra.cs.filter.jsieve.SocialcastTest.class.getName());
        mTestMap.put("twitter", com.zimbra.cs.filter.jsieve.TwitterTest.class.getName());
        mTestMap.put("list", com.zimbra.cs.filter.jsieve.ListTest.class.getName());
        mTestMap.put("bulk", com.zimbra.cs.filter.jsieve.BulkTest.class.getName());
        mTestMap.put("importance", com.zimbra.cs.filter.jsieve.ImportanceTest.class.getName());
        mTestMap.put("flagged", com.zimbra.cs.filter.jsieve.FlaggedTest.class.getName());
        mTestMap.put("community_connections", com.zimbra.cs.filter.jsieve.CommunityConnectionsTest.class.getName());
        mTestMap.put("community_requests", com.zimbra.cs.filter.jsieve.CommunityRequestsTest.class.getName());
        mTestMap.put("community_content", com.zimbra.cs.filter.jsieve.CommunityContentTest.class.getName());
        return mTestMap;
    }

    /**
     * Register action name with action class name of that.
     * This is supposed to be invoked from the init() method of ZimbraExtension.
     */
    public static void registerCommand(String actionName, String actionClassName) {

        //  sanity check
        String registeredClassName = mCommandMap.get(actionName);
        if (registeredClassName != null) {
            // warning if something has been already registered with same actionName,
            ZimbraLog.filter.warn("action name " + actionName + " is already registered as action. registered " +
                    registeredClassName + " is overwritten with "+actionClassName);
        }
        mCommandMap.put(actionName, actionClassName);
    }

    public static Map<String, String> getCommandMap(){
        return mCommandMap;
    }

    public static Map<String, String> getTestMap(){
        return mTestMap;
    }


}
