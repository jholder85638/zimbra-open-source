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

public class SyncRequestEvent extends Entity {
    private final Type type;
    private final Object param;
    private Result result;
    
    private static enum Type {
        ADD_CONTACT, UPDATE_CONTACT, REMOVE_CONTACT,
        ADD_CATEGORY, RENAME_CATEGORY, REMOVE_CATEGORY
    }

    public static SyncRequestEvent addContact(Contact contact) {
        return new SyncRequestEvent(Type.ADD_CONTACT, contact);
    }

    public static SyncRequestEvent updateContact(ContactChange change) {
        return new SyncRequestEvent(Type.UPDATE_CONTACT, change);
    }

    public static SyncRequestEvent removeContact(int id) {
        return new SyncRequestEvent(Type.REMOVE_CONTACT, new Contact(id));
    }

    public static SyncRequestEvent addCategory(String name) {
        return new SyncRequestEvent(Type.ADD_CATEGORY, new Category(name));
    }

    public static SyncRequestEvent renameCategory(Category category, String newName) {
        return new SyncRequestEvent(Type.RENAME_CATEGORY,
            new Category[] { category, new Category(newName) });
    }

    public static SyncRequestEvent renameCategory(int id, String newName) {
        return SyncRequestEvent.renameCategory(new Category(id), newName);
    }

    public static SyncRequestEvent renameCategory(String oldName, String newName) {
        return SyncRequestEvent.renameCategory(new Category(oldName), newName);
    }
    
    public static SyncRequestEvent removeCategory(Category category) {
        return new SyncRequestEvent(Type.REMOVE_CATEGORY, category);
    }

    public static SyncRequestEvent removeCategory(int id) {
        return removeCategory(new Category(id));
    }

    public static SyncRequestEvent removeCategory(String name) {
        return removeCategory(new Category(name));
    }

    private SyncRequestEvent(Type type, Object param) {
        this.type = type;
        this.param = param;
    }

    private Contact getContact() {
        return (Contact) param;
    }

    private ContactChange getContactChange() {
        return (ContactChange) param;
    }

    private Category getCategory() {
        return (Category) param;
    }

    private Category getFromCategory() {
        return ((Category[]) param)[0];
    }

    private Category getToCategory() {
        return ((Category[]) param)[1];
    }

    public boolean isAddContact() {
        return type == Type.ADD_CONTACT;
    }

    public boolean isAddCategory() {
        return type == Type.ADD_CATEGORY;
    }

    public boolean isUpdateContact() {
        return type == Type.UPDATE_CONTACT;
    }

    public boolean isRenameCategory() {
        return type == Type.RENAME_CATEGORY;
    }

    public boolean isRemoveContact() {
        return type == Type.REMOVE_CONTACT;
    }

    public boolean isRemoveCategory() {
        return type == Type.REMOVE_CATEGORY;
    }
    
    public boolean isContact() {
        switch (type) {
        case ADD_CONTACT:
        case UPDATE_CONTACT:
        case REMOVE_CONTACT:
            return true;
        default:
            return false;
        }
    }

    public boolean isCategory() {
        return !isContact();
    }
    
    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public Element toXml(Document doc) {
        switch (type) {
        case ADD_CONTACT:
            return getContact().asContactChange().toXml(doc, "add-contact");
        case UPDATE_CONTACT:
            return getContactChange().toXml(doc, "update-contact");
        case REMOVE_CONTACT:
            return getContact().toXml(doc, "remove-contact");
        case ADD_CATEGORY:
            return getCategory().toXml(doc, "add-category");
        case RENAME_CATEGORY:
            Element e = doc.createElement("rename-category");
            e.appendChild(getFromCategory().toXml(doc, "old-category"));
            e.appendChild(getToCategory().toXml(doc, "new-category"));
            return e;
        case REMOVE_CATEGORY:
            return getCategory().toXml(doc, "remove-category");
        }
        return null;
    }
}
