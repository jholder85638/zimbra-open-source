/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.ab;

import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.mailbox.Contact.Attachment;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.mailbox.ContactConstants;
import static com.zimbra.common.mailbox.ContactConstants.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class Ab {
    public static final List<String> NAME_FIELDS = Arrays.asList(
        A_firstName, A_middleName, A_lastName, A_namePrefix, A_nameSuffix);

    public static final List<String> WORK_ADDRESS_FIELDS = Arrays.asList(
        A_workStreet, A_workCity, A_workState, A_workPostalCode, A_workCountry);

    public static final List<String> HOME_ADDRESS_FIELDS = Arrays.asList(
        A_homeStreet, A_homeCity, A_homeState, A_homePostalCode, A_homeCountry);

    public static final List<String> OTHER_ADDRESS_FIELDS = Arrays.asList(
        A_otherStreet, A_otherCity, A_otherState, A_otherPostalCode, A_otherCountry);

    public static final List<String> EMAIL_FIELDS = Arrays.asList(
        A_email, A_email2, A_email3);

    public static final List<String> IM_FIELDS = Arrays.asList(
        A_imAddress1, A_imAddress2, A_imAddress3);
    
    public static String getFileAs(Map<String, String> fields) {
        if (!fields.containsKey(ContactConstants.A_firstName) &&
            !fields.containsKey(ContactConstants.A_lastName)) {
            String fileAs;
            if ((fileAs = fields.get(ContactConstants.A_fullName)) != null ||
                (fileAs = fields.get(ContactConstants.A_nickname)) != null ||
                (fileAs = fields.get(ContactConstants.A_email)) != null ||
                (fileAs = fields.get(ContactConstants.A_email2)) != null ||
                (fileAs = fields.get(ContactConstants.A_workEmail1)) != null ||
                (fileAs = fields.get(ContactConstants.A_imAddress1)) != null) {
                return fileAs;
            }
        }
        return null;
    }

    public static Attachment getPhoto(Contact contact) {
        for (Attachment attachment : contact.getAttachments()) {
            if (attachment.getName().equalsIgnoreCase(ContactConstants.A_image) &&
                attachment.getContentType().startsWith("image/")) {
                return attachment;
            }
        }
        return null;
    }

    public static byte[] getContent(Contact contact, Attachment attach)
        throws ServiceException {
        try {
            return attach.getContent();
        } catch (Exception e) {
            throw ServiceException.FAILURE(
                "Unable to get photo attachment for item id " + contact.getId(), e);
        }
    }

    public static byte[] readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> asMap(Object... args) {
        Map<K,V> map = new HashMap<K,V>();
        for (int i = 0; i < args.length; ) {
            map.put((K) args[i++], (V) args[i++]);
        }
        return map;
    }
}
