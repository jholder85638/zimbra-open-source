/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2009, 2010, 2011, 2013, 2014, 2016 Synacor, Inc.
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

package com.zimbra.analyzersample;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;

import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.cs.index.ZimbraAnalyzer;
import com.zimbra.common.service.ServiceException;

/**
 * A sample Zimbra Extension which provides a custom Lucene analyzer.
 * <p>
 * The extension must call {@link ZimbraAnalyzer#registerAnalyzer(String, org.apache.lucene.analysis.Analyzer)})
 * on startup to register itself with the system. The custom analyzer is invoked based on the COS or Account setting
 * {@code zimbraTextAnalyzer}.
 */
public class SampleAnalyzerExtension implements ZimbraExtension {
    private static final Log LOG = LogFactory.getLog(SampleAnalyzerExtension.class);

    @Override
    public synchronized void init() {
        LOG.info("Initializing %s", getName());
        try {
            // The extension can provide any name, however that name must be unique or else the registration will fail.
            ZimbraAnalyzer.registerAnalyzer(getName(), new SampleAnalyzer());
        } catch (ServiceException e) {
            LOG.error("Error while registering extension %s", getName(), e);
        }
    }

    @Override
    public synchronized void destroy() {
        LOG.info("Destroying %s", getName());
        ZimbraAnalyzer.unregisterAnalyzer(getName());
    }

    @Override
    public String getName() {
        return "SampleAnalyzer";
    }

}
