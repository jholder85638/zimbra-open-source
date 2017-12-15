/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2008, 2009, 2010, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.util.yab;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.zimbra.cs.offline.util.Xml;

/**
 * Structured YAB name field.
 */
public final class NameField extends Field {
    private String first;
    private String middle;
    private String last;
    private String prefix;
    private String suffix;
    private String firstSound;
    private String lastSound;

    public static final String NAME = "name";

    private static final String FIRST = "first";
    private static final String MIDDLE = "middle";
    private static final String LAST = "last";
    private static final String PREFIX = "prefix";
    private static final String SUFFIX = "suffix";
    private static final String FIRST_SOUND = "first-sound";
    private static final String LAST_SOUND = "last-sound";
    
    public NameField() {
        super(NAME);
    }

    public NameField(String first, String last) {
        this();
        setFirst(first);
        setLast(last);
    }

    @Override
    public Type getType() {
        return Type.NAME;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getMiddle() {
        return middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getFirstSound() {
        return firstSound;
    }

    public void setFirstSound(String firstSound) {
        this.firstSound = firstSound;
    }

    public String getLastSound() {
        return lastSound;
    }

    public void setLastSound(String lastSound) {
        this.lastSound = lastSound;
    }

    @Override
    public Element toXml(Document doc, String tag) {
        Element e = super.toXml(doc, tag);
        if (first != null) Xml.appendElement(e, FIRST, first);
        if (middle != null) Xml.appendElement(e, MIDDLE, middle);
        if (last != null) Xml.appendElement(e, LAST, last);
        if (prefix != null) Xml.appendElement(e, PREFIX, prefix);
        if (suffix != null) Xml.appendElement(e, SUFFIX, suffix);
        if (firstSound != null) Xml.appendElement(e, FIRST_SOUND, firstSound);
        if (lastSound != null) Xml.appendElement(e, LAST_SOUND, lastSound);
        return e;
    }

    @Override
    protected void parseXml(Element e) {
        super.parseXml(e);
        for (Element child : Xml.getChildren(e)) {
            String tag = child.getTagName();
            if (tag.equals(FIRST)) {
                first = getTextValue(child);
            } else if (tag.equals(MIDDLE)) {
                middle = getTextValue(child);
            } else if (tag.equals(LAST)) {
                last = getTextValue(child);
            } else if (tag.equals(PREFIX)) {
                prefix = getTextValue(child);
            } else if (tag.equals(SUFFIX)) {
                suffix = getTextValue(child);
            } else if (tag.equals(FIRST_SOUND)) {
                firstSound = getTextValue(child);
            } else if (tag.equals(LAST_SOUND)) {
                lastSound = getTextValue(child);
            }
        }
    }
}
