/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2011, 2014 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Common Public Attribution License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at: http://www.zimbra.com/license
 * The License is based on the Mozilla Public License Version 1.1 but Sections 14 and 15 
 * have been added to cover use of software over a computer network and provide for limited attribution 
 * for the Original Developer. In addition, Exhibit A has been modified to be consistent with Exhibit B. 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * See the License for the specific language governing rights and limitations under the License. 
 * The Original Code is Zimbra Open Source Web Client. 
 * The Initial Developer of the Original Code is Zimbra, Inc. 
 * All portions of the code are Copyright (C) 2007, 2008, 2009, 2011, 2014 Zimbra, Inc. All Rights Reserved. 
 * ***** END LICENSE BLOCK *****
 */
ZaZimbraAdmin._SAMBA_DOMAIN_LIST = ZaZimbraAdmin.VIEW_INDEX++;
ZaZimbraAdmin._SAMBA_DOMAIN_VIEW = ZaZimbraAdmin.VIEW_INDEX++;
if(ZaMsg) {
	ZaMsg.SMBDomains_view_title = zimbra_samba.SambaDomainsListViewTitle;
}
function Zambra() {
	
}
Zambra.ldapSuffix = "dc=com";
Zambra.ldapGroupSuffix = "ou=group";
Zambra.ldapMachineSuffix = "ou=machines";

ZaSettings.SAMBA_DOMAINS_LIST_VIEW = "sambaDomainsListView";
ZaSettings.ALL_UI_COMPONENTS.push({ value: ZaSettings.SAMBA_DOMAINS_LIST_VIEW, label: zimbra_samba.SambaDomainsListViewTitle });

Zambra.initSettings= function () {
	try {
		var soapDoc = AjxSoapDoc.create("GetAdminExtensionZimletsRequest", "urn:zimbraAdmin", null);	
		var command = new ZmCsfeCommand();
		var params = new Object();
		params.soapDoc = soapDoc;	
		var resp = command.invoke(params);
		var zimlets = null;
		try {
			if(resp && resp.Body && resp.Body.GetAdminExtensionZimletsResponse && resp.Body.GetAdminExtensionZimletsResponse.zimlets && resp.Body.GetAdminExtensionZimletsResponse.zimlets.zimlet) {
				zimlets = resp.Body.GetAdminExtensionZimletsResponse.zimlets.zimlet;
			}
		} catch (ex) {
			//go on
		}
		if(zimlets && zimlets.length > 0) {
			var cnt = zimlets.length;
			for(var ix = 0; ix < cnt; ix++) {
				if(zimlets[ix] && zimlets[ix].zimlet && zimlets[ix].zimlet[0] && zimlets[ix].zimletConfig && zimlets[ix].zimletConfig[0]) {
					var zimletConfig = zimlets[ix].zimletConfig[0];					
					if(zimletConfig.name=="zimbra_samba") {
						var global = zimletConfig.global[0];
						if(global) {
							var properties = global.property;
							var cnt2 = properties.length;							
							for (var j=0;j<cnt2;j++) {
								Zambra[properties[j].name] = properties[j]._content;
							}
						}
						break;
					}
				} else {
					continue;
				}
			}
		}	
	} catch (ex) {
		//do nothing, do not block the app from loading
	}
}

if(ZaSettings.initMethods)
	ZaSettings.initMethods.push(Zambra.initSettings);

	
Zambra.initOUs = function () {
	//check groups OU
	var soapDoc = AjxSoapDoc.create("GetLDAPEntriesRequest", "urn:zimbraAdmin", null);	
	soapDoc.set("ldapSearchBase", Zambra.ldapSuffix);
	soapDoc.set("query", Zambra.ldapGroupSuffix);	
	var getSambaDomainsCommand = new ZmCsfeCommand();
	var params = new Object();
	params.soapDoc = soapDoc;	
	var resp = getSambaDomainsCommand.invoke(params).Body.GetLDAPEntriesResponse;
	if(resp && resp.LDAPEntry && resp.LDAPEntry[0]) {
		//ou exists
	} else {
		try {
			//ou does not exist - create it
			var soapDoc = AjxSoapDoc.create("CreateLDAPEntryRequest", "urn:zimbraAdmin", null);		
			var dn = [Zambra.ldapGroupSuffix,Zambra.ldapSuffix];
			soapDoc.set("dn", dn.join(","));	
			var testCommand = new ZmCsfeCommand();
			var params = new Object();
			var attr = soapDoc.set("a", "organizationalRole");
			attr.setAttribute("n", "objectClass");		
			var attr = soapDoc.set("a", "groups");
			attr.setAttribute("n", "cn");		
			
			params.soapDoc = soapDoc;	
			var resp = testCommand.invoke(params).Body.CreateLDAPEntryResponse;
		} catch (e) {
			alert("Warning! Failed to create "+dn.join(",")+" for Samba groups!");
		}
		
	}

	//check machines OU
	soapDoc = AjxSoapDoc.create("GetLDAPEntriesRequest", "urn:zimbraAdmin", null);	
	soapDoc.set("ldapSearchBase", Zambra.ldapSuffix);
	soapDoc.set("query", Zambra.ldapMachineSuffix);	
	getSambaDomainsCommand = new ZmCsfeCommand();
	params = new Object();
	params.soapDoc = soapDoc;	
	resp = getSambaDomainsCommand.invoke(params).Body.GetLDAPEntriesResponse;
	if(resp && resp.LDAPEntry && resp.LDAPEntry[0]) {
		//ou exists
	} else {
		try {
			//ou does not exist - create it
			var soapDoc = AjxSoapDoc.create("CreateLDAPEntryRequest", "urn:zimbraAdmin", null);		
			var dn = [Zambra.ldapMachineSuffix,Zambra.ldapSuffix];
			soapDoc.set("dn", dn.join(","));	
			var testCommand = new ZmCsfeCommand();
			var params = new Object();
			var attr = soapDoc.set("a", "organizationalRole");
			attr.setAttribute("n", "objectClass");		
			var attr = soapDoc.set("a", "machines");
			attr.setAttribute("n", "cn");		
			
			params.soapDoc = soapDoc;	
			var resp = testCommand.invoke(params).Body.CreateLDAPEntryResponse;
			
		} catch (e) {
			alert("Warning! Failed to create "+dn.join(",")+" for Samba machine accounts!");
		}
		
	}

}

if(ZaSettings.initMethods)
	ZaSettings.initMethods.push(Zambra.initOUs);


ZaApp.prototype.getSambaDomainList =
function(refresh) {
	if (refresh || this._sambaDomainList == null) {
		this._sambaDomainList = ZaSambaDomain.getAll(this);
	}
	return this._sambaDomainList;	
}

ZaApp.prototype.getSambaDomainListController =
function() {
	if (this._controllers[ZaZimbraAdmin._SAMBA_DOMAIN_LIST] == null) {
		this._controllers[ZaZimbraAdmin._SAMBA_DOMAIN_LIST] = new ZaSambaDomainListController(this._appCtxt, this._container, this);
	}
	return this._controllers[ZaZimbraAdmin._SAMBA_DOMAIN_LIST];
}

ZaApp.prototype.getSambaDomainController =
function() {
	var c = new ZaSambaDomainController(this._appCtxt, this._container, this);
	var ctrl = this.getSambaDomainListController();
	c.addChangeListener(new AjxListener(ctrl, ctrl.handleChange));
	c.addCreationListener(new AjxListener(ctrl, ctrl.handleCreation));	
	c.addRemovalListener(new AjxListener(ctrl, ctrl.handleRemoval));			
	return c;
}
Zambra.sambaDomainListTreeListener = function (ev) {
	if(ZaApp.getInstance().getCurrentController()) {
		ZaApp.getInstance().getCurrentController().switchToNextView(ZaApp.getInstance().getSambaDomainListController(),ZaSambaDomainListController.prototype.show, ZaSambaDomain.getAll());
	} else {					
		ZaApp.getInstance().getSambaDomainListController().show(ZaSambaDomain.getAll());
	}
}

Zambra.sambaDomainTreeListener = function (ev) {
	var currentSambaDomain = ZaApp.getInstance().getSambaDomainList().getItemById(ev.item.getData(ZaOverviewPanelController._OBJ_ID));	
	if(ZaApp.getInstance().getCurrentController()) {
		ZaApp.getInstance().getCurrentController().switchToNextView(ZaApp.getInstance().getSambaDomainController(),ZaSambaDomainController.prototype.show, currentSambaDomain);
	} else {					
		ZaApp.getInstance().getSambaDomainController().show(currentSambaDomain);
	}
}


Zambra.ovTreeModifier = function (tree) {
	if(ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.SAMBA_DOMAINS_LIST_VIEW] || ZaSettings.ENABLED_UI_COMPONENTS[ZaSettings.CARTE_BLANCHE_UI]) {
		if(!this._configTi) {
			this._configTi = new DwtTreeItem(tree, null, null, null, null, "overviewHeader");
			this._configTi.enableSelection(false);
			this._configTi.setText(ZaMsg.OVP_configuration);
			this._configTi.setData(ZaOverviewPanelController._TID, ZaZimbraAdmin._SYS_CONFIG);		
		}
		this._sambaTi = new DwtTreeItem({parent:this._configTi,className:"AdminTreeItem"});
		this._sambaTi.setText(zimbra_samba.SambaDomainsListViewTitle);
		this._sambaTi.setImage("Zimlet");
		this._sambaTi.setData(ZaOverviewPanelController._TID, ZaZimbraAdmin._SAMBA_DOMAIN_LIST);	
		
		try {
			//add server statistics nodes
			var sambaDomainList = ZaApp.getInstance().getSambaDomainList().getArray();
			if(sambaDomainList && sambaDomainList.length) {
				var cnt = sambaDomainList.length;
				for(var ix=0; ix< cnt; ix++) {
					var ti1 = new DwtTreeItem({parent:this._sambaTi,className:"AdminTreeItem"});			
					ti1.setText(sambaDomainList[ix].name);	
					ti1.setImage("Domain");
					ti1.setData(ZaOverviewPanelController._TID, ZaZimbraAdmin._SAMBA_DOMAIN_VIEW);
					ti1.setData(ZaOverviewPanelController._OBJ_ID, sambaDomainList[ix].id);
				}
			}
		} catch (ex) {
			this._handleException(ex, "ZaOverviewPanelController.prototype._buildFolderTree", null, false);
		}
		

		if(ZaOverviewPanelController.overviewTreeListeners) {
			ZaOverviewPanelController.overviewTreeListeners[ZaZimbraAdmin._SAMBA_DOMAIN_LIST] = Zambra.sambaDomainListTreeListener;
			ZaOverviewPanelController.overviewTreeListeners[ZaZimbraAdmin._SAMBA_DOMAIN_VIEW] = Zambra.sambaDomainTreeListener;							
		}
	}
}

if(ZaOverviewPanelController.treeModifiers) {
	ZaOverviewPanelController.treeModifiers.push(Zambra.ovTreeModifier);
}
	
