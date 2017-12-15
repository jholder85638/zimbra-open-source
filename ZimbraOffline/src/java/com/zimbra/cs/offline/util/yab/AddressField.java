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
 * Structured YAB address field.
 */
public final class AddressField extends Field {
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;

    public static final String ADDRESS = "address";

    private static final String STREET = "street";
    private static final String CITY = "city";
    private static final String STATE = "state";
    private static final String ZIP = "zip";
    private static final String COUNTRY = "country";
    
    public AddressField() {
        super(ADDRESS);
    }

    @Override
    public Type getType() {
        return Type.ADDRESS;
    }
    
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public Element toXml(Document doc, String tag) {
        Element e = super.toXml(doc, tag);
        if (street != null) Xml.appendElement(e, STREET, street);
        if (city != null) Xml.appendElement(e, CITY, city);
        if (state != null) Xml.appendElement(e, STATE, state);
        if (zip != null) Xml.appendElement(e, ZIP, zip);
        if (country != null) Xml.appendElement(e, COUNTRY, country);
        return e;
    }

    @Override
    protected void parseXml(Element e) {
        super.parseXml(e);
        for (Element child : Xml.getChildren(e)) {
            String tag = child.getTagName();
            if (tag.equals(STREET)) {
                street = getTextValue(child);
            } else if (tag.equals(CITY)) {
                city = getTextValue(child);
            } else if (tag.equals(STATE)) {
                state = getTextValue(child);
            } else if (tag.equals(ZIP)) {
                zip = getTextValue(child);
            } else if (tag.equals(COUNTRY)) {
                country = getTextValue(child);
            }
        }
    }
}
