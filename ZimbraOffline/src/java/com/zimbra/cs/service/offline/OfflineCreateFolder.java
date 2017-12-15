/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2009, 2010, 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.service.offline;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.offline.OfflineDataSource;
import com.zimbra.cs.account.offline.OfflineProvisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.ZcsMailbox;
import com.zimbra.cs.service.FeedManager;
import com.zimbra.cs.service.mail.CreateFolder;
import com.zimbra.soap.ZimbraSoapContext;

public class OfflineCreateFolder extends CreateFolder {
    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        Element t = request.getElement(MailConstants.E_FOLDER);
        String viewStr = t.getAttribute(MailConstants.A_DEFAULT_VIEW, null);
        if (viewStr != null) {
            if (MailItem.Type.of(viewStr) == MailItem.Type.CONTACT) {
                OfflineProvisioning prov = OfflineProvisioning.getOfflineInstance();
                OfflineDataSource dataSource = (OfflineDataSource) prov.getDataSource(mbox.getAccount());
                if (dataSource != null && (dataSource.isGmail() || dataSource.isYahoo() || dataSource.isLive())) {
                    throw ServiceException.FAILURE("ZD does not allow creation of Address book for Gmail, Yahoo and Live", null);
                }
            }
        }

        if (!(mbox instanceof ZcsMailbox)) {
            return super.handle(request, context);
        }

        String url = t.getAttribute(MailConstants.A_URL, null);

        if (url != null && !url.equals("")) {
            FeedManager.retrieveRemoteDatasource(mbox.getAccount(), url, null);
            t.addAttribute(MailConstants.A_SYNC, false); // for zimbra accounts don't load rss on folder creation
        }
        return super.handle(request, context);
    }
}
