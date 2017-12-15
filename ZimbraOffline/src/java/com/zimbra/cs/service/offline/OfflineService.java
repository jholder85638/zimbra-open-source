/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Zimbra, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.service.offline;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.offline.common.OfflineConstants;
import com.zimbra.soap.DocumentDispatcher;
import com.zimbra.soap.DocumentService;
import com.zimbra.soap.SoapContextExtension;

public class OfflineService implements DocumentService {

    public void registerHandlers(DocumentDispatcher dispatcher) {
        // sync
        dispatcher.registerHandler(OfflineConstants.SYNC_REQUEST, new OfflineSync());
        
        dispatcher.registerHandler(MailConstants.NO_OP_REQUEST, new OfflineNoOp());

        // fetching external data
        dispatcher.registerHandler(MailConstants.FOLDER_ACTION_REQUEST, new OfflineFolderAction());
        dispatcher.registerHandler(MailConstants.CREATE_FOLDER_REQUEST, new OfflineCreateFolder());
        dispatcher.registerHandler(MailConstants.CREATE_MOUNTPOINT_REQUEST, new OfflineCreateMountpoint());
        dispatcher.registerHandler(MailConstants.GET_IMPORT_STATUS_REQUEST, new OfflineGetImportStatus());
        dispatcher.registerHandler(MailConstants.IMPORT_DATA_REQUEST, new OfflineImportData());
        dispatcher.registerHandler(AccountConstants.GET_INFO_REQUEST, new OfflineGetInfo());
        
        dispatcher.registerHandler(MailConstants.DIFF_DOCUMENT_REQUEST, new OfflineDocumentHandlers.DiffDocument());
        dispatcher.registerHandler(MailConstants.LIST_DOCUMENT_REVISIONS_REQUEST, new OfflineDocumentHandlers.ListDocumentRevisions());
        dispatcher.registerHandler(MailConstants.SAVE_DOCUMENT_REQUEST, new OfflineSaveDocument());
        
        dispatcher.registerHandler(MailConstants.SEARCH_REQUEST, new OfflineSearch());
        dispatcher.registerHandler(MailConstants.GET_MINI_CAL_REQUEST, new OfflineGetMiniCal());
        dispatcher.registerHandler(MailConstants.AUTO_COMPLETE_REQUEST, new OfflineAutoComplete());        
        dispatcher.registerHandler(AccountConstants.SEARCH_GAL_REQUEST, new OfflineSearchGal());
        dispatcher.registerHandler(MailConstants.GET_FREE_BUSY_REQUEST, new OfflineGetFreeBusy());
        dispatcher.registerHandler(AccountConstants.SEARCH_CALENDAR_RESOURCES_REQUEST, new OfflineSearchCalendarResources());
        dispatcher.registerHandler(MailConstants.CHECK_RECUR_CONFLICTS_REQUEST, OfflineServiceProxy.CheckRecurConflictsRequest());
        dispatcher.registerHandler(MailConstants.CREATE_APPOINTMENT_REQUEST, new OfflineCreateAppointment());
        dispatcher.registerHandler(MailConstants.MODIFY_APPOINTMENT_REQUEST, new OfflineModifyAppointment());
        dispatcher.registerHandler(MailConstants.CREATE_APPOINTMENT_EXCEPTION_REQUEST, new OfflineCreateAppointmentException());
        dispatcher.registerHandler(MailConstants.CANCEL_APPOINTMENT_REQUEST, new OfflineCancelAppointment());
        dispatcher.registerHandler(MailConstants.CREATE_TASK_REQUEST, new OfflineCreateTask());
        dispatcher.registerHandler(MailConstants.MODIFY_TASK_REQUEST, new OfflineModifyTask());
        dispatcher.registerHandler(MailConstants.CONV_ACTION_REQUEST, new OfflineConvAction());
        dispatcher.registerHandler(MailConstants.GET_PERMISSION_REQUEST, OfflineServiceProxy.GetPermission());
        dispatcher.registerHandler(MailConstants.GRANT_PERMISSION_REQUEST, OfflineServiceProxy.GrantPermission());
        dispatcher.registerHandler(MailConstants.REVOKE_PERMISSION_REQUEST, OfflineServiceProxy.RevokePermission());
        dispatcher.registerHandler(MailConstants.CHECK_PERMISSION_REQUEST, OfflineServiceProxy.CheckPermission());
        dispatcher.registerHandler(AccountConstants.GET_SHARE_INFO_REQUEST, OfflineServiceProxy.GetShareInfoRequest());
        dispatcher.registerHandler(AccountConstants.AUTO_COMPLETE_GAL_REQUEST, OfflineServiceProxy.AutoCompleteGalRequest());
        dispatcher.registerHandler(MailConstants.SAVE_DRAFT_REQUEST, new OfflineSaveDraft());
        dispatcher.registerHandler(OfflineConstants.CHANGE_PASSWORD_REQUEST, new OfflineChangePassword());
        
        dispatcher.registerHandler(OfflineConstants.CLIENT_EVENT_NOTIFY_REQUEST, new OfflineClientEventNotify());
        dispatcher.registerHandler(OfflineConstants.GET_EXTENSIONS_REQUEST, new OfflineGetExtensions());

        dispatcher.registerHandler(MailConstants.SEND_INVITE_REPLY_REQUEST, new OfflineSendInviteReply());
        dispatcher.registerHandler(MailConstants.SEND_REPORT_REQUEST, new OfflineSendDeliveryReport());
        dispatcher.registerHandler(OfflineConstants.ACCOUNT_BACKUP_REQUEST, new OfflineAccountBackupService());
        dispatcher.registerHandler(OfflineConstants.ACCOUNT_RESTORE_REQUEST, new OfflineAccountRestoreService());
        dispatcher.registerHandler(OfflineConstants.ACCOUNT_BACKUP_ENUM_REQUEST, new OfflineBackupEnumService());
        dispatcher.registerHandler(AccountConstants.GET_DISTRIBUTION_LIST_MEMBERS_REQUEST, OfflineServiceProxy.GetDLMembersRequest());
        dispatcher.registerHandler(MailConstants.MODIFY_CONTACT_REQUEST, new OfflineModifyContact());
        dispatcher.registerHandler(MailConstants.CREATE_CONTACT_REQUEST, new OfflineCreateContact());
        dispatcher.registerHandler(AccountConstants.MODIFY_PROPERTIES_REQUEST, new OfflineModifyProperties());
        dispatcher.registerHandler(OfflineConstants.DIALOG_ACTION_REQUEST, new OfflineDialogAction());

        // not the most suitable place to do this, but it's just too easy.
        SoapContextExtension.register(OfflineContextExtension.ZDSYNC, new OfflineContextExtension());
    }
}