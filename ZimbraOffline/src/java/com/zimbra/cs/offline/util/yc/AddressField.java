/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2016 Synacor, Inc.
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
package com.zimbra.cs.offline.util.yc;

import org.w3c.dom.Element;

import com.zimbra.cs.offline.util.Xml;
import com.zimbra.cs.offline.util.yc.Fields.Type;

/**
 * Structured YAB address field.
 */
public final class AddressField extends FieldValue {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String countryCode;

    private static final String STREET = "street";
    private static final String CITY = "city";
    private static final String STATE = "stateOrProvince";
    private static final String POSTALCODE = "postalCode";
    private static final String COUNTRY = "country";
    private static final String COUNTRYCODE = "countryCode";
    
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
        return postalCode;
    }

    public void setZip(String zip) {
        this.postalCode = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    protected void appendValues(Element parent) {
        if (street != null) Xml.appendElement(parent, STREET, street);
        if (city != null) Xml.appendElement(parent, CITY, city);
        if (state != null) Xml.appendElement(parent, STATE, state);
        if (postalCode != null) Xml.appendElement(parent, POSTALCODE, postalCode);
        if (country != null) Xml.appendElement(parent, COUNTRY, country);
        if (countryCode != null) Xml.appendElement(parent, COUNTRYCODE, countryCode);
    }

    @Override
    public void extractFromXml(Element e) {
        for (Element child : Xml.getChildren(e)) {
            String tag = child.getTagName();
            if (tag.equals(STREET)) {
                street = child.getTextContent();
            } else if (tag.equals(CITY)) {
                city = child.getTextContent();
            } else if (tag.equals(STATE)) {
                state = child.getTextContent();
            } else if (tag.equals(POSTALCODE)) {
                postalCode = child.getTextContent();
            } else if (tag.equals(COUNTRY)) {
                country = child.getTextContent();
            } else if (tag.equals(COUNTRYCODE)) {
                countryCode = child.getTextContent();
            }
        }
    }

    @Override
    public Type getType() {
        return Fields.Type.address;
    }
}
