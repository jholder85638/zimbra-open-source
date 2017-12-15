
package generated.zcsclient.ws.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import generated.zcsclient.account.testAuthRequest;
import generated.zcsclient.account.testAuthResponse;
import generated.zcsclient.account.testAutoCompleteGalRequest;
import generated.zcsclient.account.testAutoCompleteGalResponse;
import generated.zcsclient.account.testBootstrapMobileGatewayAppRequest;
import generated.zcsclient.account.testBootstrapMobileGatewayAppResponse;
import generated.zcsclient.account.testChangePasswordRequest;
import generated.zcsclient.account.testChangePasswordResponse;
import generated.zcsclient.account.testCheckLicenseRequest;
import generated.zcsclient.account.testCheckLicenseResponse;
import generated.zcsclient.account.testCheckRightsRequest;
import generated.zcsclient.account.testCheckRightsResponse;
import generated.zcsclient.account.testCreateAppSpecificPasswordRequest;
import generated.zcsclient.account.testCreateAppSpecificPasswordResponse;
import generated.zcsclient.account.testCreateDistributionListRequest;
import generated.zcsclient.account.testCreateDistributionListResponse;
import generated.zcsclient.account.testCreateIdentityRequest;
import generated.zcsclient.account.testCreateIdentityResponse;
import generated.zcsclient.account.testCreateSignatureRequest;
import generated.zcsclient.account.testCreateSignatureResponse;
import generated.zcsclient.account.testDeleteIdentityRequest;
import generated.zcsclient.account.testDeleteIdentityResponse;
import generated.zcsclient.account.testDeleteSignatureRequest;
import generated.zcsclient.account.testDeleteSignatureResponse;
import generated.zcsclient.account.testDisableTwoFactorAuthRequest;
import generated.zcsclient.account.testDisableTwoFactorAuthResponse;
import generated.zcsclient.account.testDiscoverRightsRequest;
import generated.zcsclient.account.testDiscoverRightsResponse;
import generated.zcsclient.account.testDistributionListActionRequest;
import generated.zcsclient.account.testDistributionListActionResponse;
import generated.zcsclient.account.testEnableTwoFactorAuthRequest;
import generated.zcsclient.account.testEnableTwoFactorAuthResponse;
import generated.zcsclient.account.testEndSessionRequest;
import generated.zcsclient.account.testEndSessionResponse;
import generated.zcsclient.account.testGenerateScratchCodesRequest;
import generated.zcsclient.account.testGenerateScratchCodesResponse;
import generated.zcsclient.account.testGetAccountDistributionListsRequest;
import generated.zcsclient.account.testGetAccountDistributionListsResponse;
import generated.zcsclient.account.testGetAccountInfoRequest;
import generated.zcsclient.account.testGetAccountInfoResponse;
import generated.zcsclient.account.testGetAllLocalesRequest;
import generated.zcsclient.account.testGetAllLocalesResponse;
import generated.zcsclient.account.testGetAppSpecificPasswordsRequest;
import generated.zcsclient.account.testGetAppSpecificPasswordsResponse;
import generated.zcsclient.account.testGetAvailableCsvFormatsRequest;
import generated.zcsclient.account.testGetAvailableCsvFormatsResponse;
import generated.zcsclient.account.testGetAvailableLocalesRequest;
import generated.zcsclient.account.testGetAvailableLocalesResponse;
import generated.zcsclient.account.testGetAvailableSkinsRequest;
import generated.zcsclient.account.testGetAvailableSkinsResponse;
import generated.zcsclient.account.testGetDistributionListMembersRequest;
import generated.zcsclient.account.testGetDistributionListMembersResponse;
import generated.zcsclient.account.testGetDistributionListRequest;
import generated.zcsclient.account.testGetDistributionListResponse;
import generated.zcsclient.account.testGetGcmSenderIdRequest;
import generated.zcsclient.account.testGetGcmSenderIdResponse;
import generated.zcsclient.account.testGetIdentitiesRequest;
import generated.zcsclient.account.testGetIdentitiesResponse;
import generated.zcsclient.account.testGetInfoRequest;
import generated.zcsclient.account.testGetInfoResponse;
import generated.zcsclient.account.testGetPrefsRequest;
import generated.zcsclient.account.testGetPrefsResponse;
import generated.zcsclient.account.testGetRightsRequest;
import generated.zcsclient.account.testGetRightsResponse;
import generated.zcsclient.account.testGetSMIMEPublicCertsRequest;
import generated.zcsclient.account.testGetSMIMEPublicCertsResponse;
import generated.zcsclient.account.testGetShareInfoRequest;
import generated.zcsclient.account.testGetShareInfoResponse;
import generated.zcsclient.account.testGetSignaturesRequest;
import generated.zcsclient.account.testGetSignaturesResponse;
import generated.zcsclient.account.testGetTrustedDevicesRequest;
import generated.zcsclient.account.testGetTrustedDevicesResponse;
import generated.zcsclient.account.testGetVersionInfoRequest;
import generated.zcsclient.account.testGetVersionInfoResponse;
import generated.zcsclient.account.testGetWhiteBlackListRequest;
import generated.zcsclient.account.testGetWhiteBlackListResponse;
import generated.zcsclient.account.testGrantRightsRequest;
import generated.zcsclient.account.testGrantRightsResponse;
import generated.zcsclient.account.testModifyIdentityRequest;
import generated.zcsclient.account.testModifyIdentityResponse;
import generated.zcsclient.account.testModifyPrefsRequest;
import generated.zcsclient.account.testModifyPrefsResponse;
import generated.zcsclient.account.testModifyPropertiesRequest;
import generated.zcsclient.account.testModifyPropertiesResponse;
import generated.zcsclient.account.testModifySignatureRequest;
import generated.zcsclient.account.testModifySignatureResponse;
import generated.zcsclient.account.testModifyWhiteBlackListRequest;
import generated.zcsclient.account.testModifyWhiteBlackListResponse;
import generated.zcsclient.account.testModifyZimletPrefsRequest;
import generated.zcsclient.account.testModifyZimletPrefsResponse;
import generated.zcsclient.account.testRegisterMobileGatewayAppRequest;
import generated.zcsclient.account.testRegisterMobileGatewayAppResponse;
import generated.zcsclient.account.testRenewMobileGatewayAppTokenRequest;
import generated.zcsclient.account.testRenewMobileGatewayAppTokenResponse;
import generated.zcsclient.account.testRevokeAppSpecificPasswordRequest;
import generated.zcsclient.account.testRevokeAppSpecificPasswordResponse;
import generated.zcsclient.account.testRevokeOtherTrustedDevicesRequest;
import generated.zcsclient.account.testRevokeOtherTrustedDevicesResponse;
import generated.zcsclient.account.testRevokeRightsRequest;
import generated.zcsclient.account.testRevokeRightsResponse;
import generated.zcsclient.account.testRevokeTrustedDeviceRequest;
import generated.zcsclient.account.testRevokeTrustedDeviceResponse;
import generated.zcsclient.account.testSearchCalendarResourcesRequest;
import generated.zcsclient.account.testSearchCalendarResourcesResponse;
import generated.zcsclient.account.testSearchGalRequest;
import generated.zcsclient.account.testSearchGalResponse;
import generated.zcsclient.account.testSubscribeDistributionListRequest;
import generated.zcsclient.account.testSubscribeDistributionListResponse;
import generated.zcsclient.account.testSyncGalRequest;
import generated.zcsclient.account.testSyncGalResponse;
import generated.zcsclient.mail.testAddAppointmentInviteRequest;
import generated.zcsclient.mail.testAddAppointmentInviteResponse;
import generated.zcsclient.mail.testAddCommentRequest;
import generated.zcsclient.mail.testAddCommentResponse;
import generated.zcsclient.mail.testAddMsgRequest;
import generated.zcsclient.mail.testAddMsgResponse;
import generated.zcsclient.mail.testAddTaskInviteRequest;
import generated.zcsclient.mail.testAddTaskInviteResponse;
import generated.zcsclient.mail.testAnnounceOrganizerChangeRequest;
import generated.zcsclient.mail.testAnnounceOrganizerChangeResponse;
import generated.zcsclient.mail.testApplyFilterRulesRequest;
import generated.zcsclient.mail.testApplyFilterRulesResponse;
import generated.zcsclient.mail.testApplyOutgoingFilterRulesRequest;
import generated.zcsclient.mail.testApplyOutgoingFilterRulesResponse;
import generated.zcsclient.mail.testAutoCompleteRequest;
import generated.zcsclient.mail.testAutoCompleteResponse;
import generated.zcsclient.mail.testBounceMsgRequest;
import generated.zcsclient.mail.testBounceMsgResponse;
import generated.zcsclient.mail.testBrowseRequest;
import generated.zcsclient.mail.testBrowseResponse;
import generated.zcsclient.mail.testCancelAppointmentRequest;
import generated.zcsclient.mail.testCancelAppointmentResponse;
import generated.zcsclient.mail.testCancelTaskRequest;
import generated.zcsclient.mail.testCancelTaskResponse;
import generated.zcsclient.mail.testCheckDeviceStatusRequest;
import generated.zcsclient.mail.testCheckDeviceStatusResponse;
import generated.zcsclient.mail.testCheckPermissionRequest;
import generated.zcsclient.mail.testCheckPermissionResponse;
import generated.zcsclient.mail.testCheckRecurConflictsRequest;
import generated.zcsclient.mail.testCheckRecurConflictsResponse;
import generated.zcsclient.mail.testCheckSpellingRequest;
import generated.zcsclient.mail.testCheckSpellingResponse;
import generated.zcsclient.mail.testCompleteTaskInstanceRequest;
import generated.zcsclient.mail.testCompleteTaskInstanceResponse;
import generated.zcsclient.mail.testContactActionRequest;
import generated.zcsclient.mail.testContactActionResponse;
import generated.zcsclient.mail.testConvActionRequest;
import generated.zcsclient.mail.testConvActionResponse;
import generated.zcsclient.mail.testCounterAppointmentRequest;
import generated.zcsclient.mail.testCounterAppointmentResponse;
import generated.zcsclient.mail.testCreateAppointmentExceptionRequest;
import generated.zcsclient.mail.testCreateAppointmentExceptionResponse;
import generated.zcsclient.mail.testCreateAppointmentRequest;
import generated.zcsclient.mail.testCreateAppointmentResponse;
import generated.zcsclient.mail.testCreateContactRequest;
import generated.zcsclient.mail.testCreateContactResponse;
import generated.zcsclient.mail.testCreateDataSourceRequest;
import generated.zcsclient.mail.testCreateDataSourceResponse;
import generated.zcsclient.mail.testCreateFolderRequest;
import generated.zcsclient.mail.testCreateFolderResponse;
import generated.zcsclient.mail.testCreateMountpointRequest;
import generated.zcsclient.mail.testCreateMountpointResponse;
import generated.zcsclient.mail.testCreateNoteRequest;
import generated.zcsclient.mail.testCreateNoteResponse;
import generated.zcsclient.mail.testCreateSearchFolderRequest;
import generated.zcsclient.mail.testCreateSearchFolderResponse;
import generated.zcsclient.mail.testCreateTagRequest;
import generated.zcsclient.mail.testCreateTagResponse;
import generated.zcsclient.mail.testCreateTaskExceptionRequest;
import generated.zcsclient.mail.testCreateTaskExceptionResponse;
import generated.zcsclient.mail.testCreateTaskRequest;
import generated.zcsclient.mail.testCreateTaskResponse;
import generated.zcsclient.mail.testCreateWaitSetRequest;
import generated.zcsclient.mail.testCreateWaitSetResponse;
import generated.zcsclient.mail.testDeclineCounterAppointmentRequest;
import generated.zcsclient.mail.testDeclineCounterAppointmentResponse;
import generated.zcsclient.mail.testDeleteDataSourceRequest;
import generated.zcsclient.mail.testDeleteDataSourceResponse;
import generated.zcsclient.mail.testDeleteDeviceRequest;
import generated.zcsclient.mail.testDeleteDeviceResponse;
import generated.zcsclient.mail.testDestroyWaitSetRequest;
import generated.zcsclient.mail.testDestroyWaitSetResponse;
import generated.zcsclient.mail.testDiffDocumentRequest;
import generated.zcsclient.mail.testDiffDocumentResponse;
import generated.zcsclient.mail.testDismissCalendarItemAlarmRequest;
import generated.zcsclient.mail.testDismissCalendarItemAlarmResponse;
import generated.zcsclient.mail.testDocumentActionRequest;
import generated.zcsclient.mail.testDocumentActionResponse;
import generated.zcsclient.mail.testEmptyDumpsterRequest;
import generated.zcsclient.mail.testEmptyDumpsterResponse;
import generated.zcsclient.mail.testEnableSharedReminderRequest;
import generated.zcsclient.mail.testEnableSharedReminderResponse;
import generated.zcsclient.mail.testExpandRecurRequest;
import generated.zcsclient.mail.testExpandRecurResponse;
import generated.zcsclient.mail.testExportContactsRequest;
import generated.zcsclient.mail.testExportContactsResponse;
import generated.zcsclient.mail.testFolderActionRequest;
import generated.zcsclient.mail.testFolderActionResponse;
import generated.zcsclient.mail.testForwardAppointmentInviteRequest;
import generated.zcsclient.mail.testForwardAppointmentInviteResponse;
import generated.zcsclient.mail.testForwardAppointmentRequest;
import generated.zcsclient.mail.testForwardAppointmentResponse;
import generated.zcsclient.mail.testGenerateUUIDRequest;
import generated.zcsclient.mail.testGetActivityStreamRequest;
import generated.zcsclient.mail.testGetActivityStreamResponse;
import generated.zcsclient.mail.testGetAllDevicesRequest;
import generated.zcsclient.mail.testGetAllDevicesResponse;
import generated.zcsclient.mail.testGetAppointmentRequest;
import generated.zcsclient.mail.testGetAppointmentResponse;
import generated.zcsclient.mail.testGetApptSummariesRequest;
import generated.zcsclient.mail.testGetApptSummariesResponse;
import generated.zcsclient.mail.testGetCalendarItemSummariesRequest;
import generated.zcsclient.mail.testGetCalendarItemSummariesResponse;
import generated.zcsclient.mail.testGetCommentsRequest;
import generated.zcsclient.mail.testGetCommentsResponse;
import generated.zcsclient.mail.testGetContactsRequest;
import generated.zcsclient.mail.testGetContactsResponse;
import generated.zcsclient.mail.testGetConvRequest;
import generated.zcsclient.mail.testGetConvResponse;
import generated.zcsclient.mail.testGetCustomMetadataRequest;
import generated.zcsclient.mail.testGetCustomMetadataResponse;
import generated.zcsclient.mail.testGetDataSourcesRequest;
import generated.zcsclient.mail.testGetDataSourcesResponse;
import generated.zcsclient.mail.testGetDocumentShareURLRequest;
import generated.zcsclient.mail.testGetEffectiveFolderPermsRequest;
import generated.zcsclient.mail.testGetEffectiveFolderPermsResponse;
import generated.zcsclient.mail.testGetFilterRulesRequest;
import generated.zcsclient.mail.testGetFilterRulesResponse;
import generated.zcsclient.mail.testGetFolderRequest;
import generated.zcsclient.mail.testGetFolderResponse;
import generated.zcsclient.mail.testGetFreeBusyRequest;
import generated.zcsclient.mail.testGetFreeBusyResponse;
import generated.zcsclient.mail.testGetICalRequest;
import generated.zcsclient.mail.testGetICalResponse;
import generated.zcsclient.mail.testGetImportStatusRequest;
import generated.zcsclient.mail.testGetImportStatusResponse;
import generated.zcsclient.mail.testGetItemRequest;
import generated.zcsclient.mail.testGetItemResponse;
import generated.zcsclient.mail.testGetMailboxMetadataRequest;
import generated.zcsclient.mail.testGetMailboxMetadataResponse;
import generated.zcsclient.mail.testGetMiniCalRequest;
import generated.zcsclient.mail.testGetMiniCalResponse;
import generated.zcsclient.mail.testGetMsgMetadataRequest;
import generated.zcsclient.mail.testGetMsgMetadataResponse;
import generated.zcsclient.mail.testGetMsgRequest;
import generated.zcsclient.mail.testGetMsgResponse;
import generated.zcsclient.mail.testGetNoteRequest;
import generated.zcsclient.mail.testGetNoteResponse;
import generated.zcsclient.mail.testGetNotificationsRequest;
import generated.zcsclient.mail.testGetNotificationsResponse;
import generated.zcsclient.mail.testGetOutgoingFilterRulesRequest;
import generated.zcsclient.mail.testGetOutgoingFilterRulesResponse;
import generated.zcsclient.mail.testGetPermissionRequest;
import generated.zcsclient.mail.testGetPermissionResponse;
import generated.zcsclient.mail.testGetRecurRequest;
import generated.zcsclient.mail.testGetRecurResponse;
import generated.zcsclient.mail.testGetSearchFolderRequest;
import generated.zcsclient.mail.testGetSearchFolderResponse;
import generated.zcsclient.mail.testGetShareDetailsRequest;
import generated.zcsclient.mail.testGetShareDetailsResponse;
import generated.zcsclient.mail.testGetShareNotificationsRequest;
import generated.zcsclient.mail.testGetShareNotificationsResponse;
import generated.zcsclient.mail.testGetSpellDictionariesRequest;
import generated.zcsclient.mail.testGetSpellDictionariesResponse;
import generated.zcsclient.mail.testGetSystemRetentionPolicyRequest;
import generated.zcsclient.mail.testGetSystemRetentionPolicyResponse;
import generated.zcsclient.mail.testGetTagRequest;
import generated.zcsclient.mail.testGetTagResponse;
import generated.zcsclient.mail.testGetTaskRequest;
import generated.zcsclient.mail.testGetTaskResponse;
import generated.zcsclient.mail.testGetTaskSummariesRequest;
import generated.zcsclient.mail.testGetTaskSummariesResponse;
import generated.zcsclient.mail.testGetWatchersRequest;
import generated.zcsclient.mail.testGetWatchersResponse;
import generated.zcsclient.mail.testGetWatchingItemsRequest;
import generated.zcsclient.mail.testGetWatchingItemsResponse;
import generated.zcsclient.mail.testGetWorkingHoursRequest;
import generated.zcsclient.mail.testGetWorkingHoursResponse;
import generated.zcsclient.mail.testGetYahooAuthTokenRequest;
import generated.zcsclient.mail.testGetYahooAuthTokenResponse;
import generated.zcsclient.mail.testGetYahooCookieRequest;
import generated.zcsclient.mail.testGetYahooCookieResponse;
import generated.zcsclient.mail.testGrantPermissionRequest;
import generated.zcsclient.mail.testGrantPermissionResponse;
import generated.zcsclient.mail.testICalReplyRequest;
import generated.zcsclient.mail.testICalReplyResponse;
import generated.zcsclient.mail.testImportAppointmentsRequest;
import generated.zcsclient.mail.testImportAppointmentsResponse;
import generated.zcsclient.mail.testImportContactsRequest;
import generated.zcsclient.mail.testImportContactsResponse;
import generated.zcsclient.mail.testImportDataRequest;
import generated.zcsclient.mail.testImportDataResponse;
import generated.zcsclient.mail.testInvalidateReminderDeviceRequest;
import generated.zcsclient.mail.testInvalidateReminderDeviceResponse;
import generated.zcsclient.mail.testItemActionRequest;
import generated.zcsclient.mail.testItemActionResponse;
import generated.zcsclient.mail.testListDocumentRevisionsRequest;
import generated.zcsclient.mail.testListDocumentRevisionsResponse;
import generated.zcsclient.mail.testModifyAppointmentRequest;
import generated.zcsclient.mail.testModifyAppointmentResponse;
import generated.zcsclient.mail.testModifyContactRequest;
import generated.zcsclient.mail.testModifyContactResponse;
import generated.zcsclient.mail.testModifyDataSourceRequest;
import generated.zcsclient.mail.testModifyDataSourceResponse;
import generated.zcsclient.mail.testModifyFilterRulesRequest;
import generated.zcsclient.mail.testModifyFilterRulesResponse;
import generated.zcsclient.mail.testModifyMailboxMetadataRequest;
import generated.zcsclient.mail.testModifyMailboxMetadataResponse;
import generated.zcsclient.mail.testModifyOutgoingFilterRulesRequest;
import generated.zcsclient.mail.testModifyOutgoingFilterRulesResponse;
import generated.zcsclient.mail.testModifySearchFolderRequest;
import generated.zcsclient.mail.testModifySearchFolderResponse;
import generated.zcsclient.mail.testModifyTaskRequest;
import generated.zcsclient.mail.testModifyTaskResponse;
import generated.zcsclient.mail.testMsgActionRequest;
import generated.zcsclient.mail.testMsgActionResponse;
import generated.zcsclient.mail.testNoOpRequest;
import generated.zcsclient.mail.testNoOpResponse;
import generated.zcsclient.mail.testNoteActionRequest;
import generated.zcsclient.mail.testNoteActionResponse;
import generated.zcsclient.mail.testPurgeRevisionRequest;
import generated.zcsclient.mail.testPurgeRevisionResponse;
import generated.zcsclient.mail.testRankingActionRequest;
import generated.zcsclient.mail.testRankingActionResponse;
import generated.zcsclient.mail.testRegisterDeviceRequest;
import generated.zcsclient.mail.testRegisterDeviceResponse;
import generated.zcsclient.mail.testRemoveAttachmentsRequest;
import generated.zcsclient.mail.testRemoveAttachmentsResponse;
import generated.zcsclient.mail.testRevokePermissionRequest;
import generated.zcsclient.mail.testRevokePermissionResponse;
import generated.zcsclient.mail.testSaveDocumentRequest;
import generated.zcsclient.mail.testSaveDocumentResponse;
import generated.zcsclient.mail.testSaveDraftRequest;
import generated.zcsclient.mail.testSaveDraftResponse;
import generated.zcsclient.mail.testSearchConvRequest;
import generated.zcsclient.mail.testSearchConvResponse;
import generated.zcsclient.mail.testSearchRequest;
import generated.zcsclient.mail.testSearchResponse;
import generated.zcsclient.mail.testSendDeliveryReportRequest;
import generated.zcsclient.mail.testSendDeliveryReportResponse;
import generated.zcsclient.mail.testSendInviteReplyRequest;
import generated.zcsclient.mail.testSendInviteReplyResponse;
import generated.zcsclient.mail.testSendMsgRequest;
import generated.zcsclient.mail.testSendMsgResponse;
import generated.zcsclient.mail.testSendShareNotificationRequest;
import generated.zcsclient.mail.testSendShareNotificationResponse;
import generated.zcsclient.mail.testSendVerificationCodeRequest;
import generated.zcsclient.mail.testSendVerificationCodeResponse;
import generated.zcsclient.mail.testSetAppointmentRequest;
import generated.zcsclient.mail.testSetAppointmentResponse;
import generated.zcsclient.mail.testSetCustomMetadataRequest;
import generated.zcsclient.mail.testSetCustomMetadataResponse;
import generated.zcsclient.mail.testSetMailboxMetadataRequest;
import generated.zcsclient.mail.testSetMailboxMetadataResponse;
import generated.zcsclient.mail.testSetTaskRequest;
import generated.zcsclient.mail.testSetTaskResponse;
import generated.zcsclient.mail.testSnoozeCalendarItemAlarmRequest;
import generated.zcsclient.mail.testSnoozeCalendarItemAlarmResponse;
import generated.zcsclient.mail.testSyncRequest;
import generated.zcsclient.mail.testSyncResponse;
import generated.zcsclient.mail.testTagActionRequest;
import generated.zcsclient.mail.testTagActionResponse;
import generated.zcsclient.mail.testTestDataSourceRequest;
import generated.zcsclient.mail.testTestDataSourceResponse;
import generated.zcsclient.mail.testUpdateDeviceStatusRequest;
import generated.zcsclient.mail.testUpdateDeviceStatusResponse;
import generated.zcsclient.mail.testVerifyCodeRequest;
import generated.zcsclient.mail.testVerifyCodeResponse;
import generated.zcsclient.mail.testWaitSetRequest;
import generated.zcsclient.mail.testWaitSetResponse;
import generated.zcsclient.replication.testBecomeMasterRequest;
import generated.zcsclient.replication.testBecomeMasterResponse;
import generated.zcsclient.replication.testBringDownServiceIPRequest;
import generated.zcsclient.replication.testBringDownServiceIPResponse;
import generated.zcsclient.replication.testBringUpServiceIPRequest;
import generated.zcsclient.replication.testBringUpServiceIPResponse;
import generated.zcsclient.replication.testReplicationStatusRequest;
import generated.zcsclient.replication.testReplicationStatusResponse;
import generated.zcsclient.replication.testStartCatchupRequest;
import generated.zcsclient.replication.testStartCatchupResponse;
import generated.zcsclient.replication.testStartFailoverClientRequest;
import generated.zcsclient.replication.testStartFailoverClientResponse;
import generated.zcsclient.replication.testStartFailoverDaemonRequest;
import generated.zcsclient.replication.testStartFailoverDaemonResponse;
import generated.zcsclient.replication.testStopFailoverClientRequest;
import generated.zcsclient.replication.testStopFailoverClientResponse;
import generated.zcsclient.replication.testStopFailoverDaemonRequest;
import generated.zcsclient.replication.testStopFailoverDaemonResponse;
import generated.zcsclient.sync.testCancelPendingRemoteWipeRequest;
import generated.zcsclient.sync.testCancelPendingRemoteWipeResponse;
import generated.zcsclient.sync.testGetDeviceStatusRequest;
import generated.zcsclient.sync.testGetDeviceStatusResponse;
import generated.zcsclient.sync.testRemoteWipeRequest;
import generated.zcsclient.sync.testRemoteWipeResponse;
import generated.zcsclient.sync.testRemoveDeviceRequest;
import generated.zcsclient.sync.testRemoveDeviceResponse;
import generated.zcsclient.sync.testResumeDeviceRequest;
import generated.zcsclient.sync.testResumeDeviceResponse;
import generated.zcsclient.sync.testSuspendDeviceRequest;
import generated.zcsclient.sync.testSuspendDeviceResponse;
import generated.zcsclient.voice.testChangeUCPasswordRequest;
import generated.zcsclient.voice.testChangeUCPasswordResponse;
import generated.zcsclient.voice.testGetUCInfoRequest;
import generated.zcsclient.voice.testGetUCInfoResponse;
import generated.zcsclient.voice.testGetVoiceFeaturesRequest;
import generated.zcsclient.voice.testGetVoiceFeaturesResponse;
import generated.zcsclient.voice.testGetVoiceFolderRequest;
import generated.zcsclient.voice.testGetVoiceFolderResponse;
import generated.zcsclient.voice.testGetVoiceInfoRequest;
import generated.zcsclient.voice.testGetVoiceInfoResponse;
import generated.zcsclient.voice.testGetVoiceMailPrefsRequest;
import generated.zcsclient.voice.testGetVoiceMailPrefsResponse;
import generated.zcsclient.voice.testModifyFromNumRequest;
import generated.zcsclient.voice.testModifyFromNumResponse;
import generated.zcsclient.voice.testModifyVoiceFeaturesRequest;
import generated.zcsclient.voice.testModifyVoiceFeaturesResponse;
import generated.zcsclient.voice.testModifyVoiceMailPinRequest;
import generated.zcsclient.voice.testModifyVoiceMailPinResponse;
import generated.zcsclient.voice.testModifyVoiceMailPrefsRequest;
import generated.zcsclient.voice.testModifyVoiceMailPrefsResponse;
import generated.zcsclient.voice.testResetVoiceFeaturesRequest;
import generated.zcsclient.voice.testResetVoiceFeaturesResponse;
import generated.zcsclient.voice.testSearchVoiceRequest;
import generated.zcsclient.voice.testSearchVoiceResponse;
import generated.zcsclient.voice.testUploadVoiceMailRequest;
import generated.zcsclient.voice.testUploadVoiceMailResponse;
import generated.zcsclient.voice.testVoiceMsgActionRequest;
import generated.zcsclient.voice.testVoiceMsgActionResponse;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.7-hudson-48-
 * Generated source version: 2.1
 * 
 */
@WebService(name = "zcsPortType", targetNamespace = "http://www.zimbra.com/wsdl/ZimbraService.wsdl")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    generated.zcsclient.zm.ObjectFactory.class,
    generated.zcsclient.sync.ObjectFactory.class,
    generated.zcsclient.account.ObjectFactory.class,
    generated.zcsclient.adminext.ObjectFactory.class,
    generated.zcsclient.voice.ObjectFactory.class,
    generated.zcsclient.admin.ObjectFactory.class,
    generated.zcsclient.mail.ObjectFactory.class,
    generated.zcsclient.replication.ObjectFactory.class
})
public interface ZcsPortType {


    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testAuthResponse
     */
    @WebMethod(action = "urn:zimbraAccount/Auth")
    @WebResult(name = "AuthResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testAuthResponse authRequest(
        @WebParam(name = "AuthRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testAuthRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testAutoCompleteGalResponse
     */
    @WebMethod(action = "urn:zimbraAccount/AutoCompleteGal")
    @WebResult(name = "AutoCompleteGalResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testAutoCompleteGalResponse autoCompleteGalRequest(
        @WebParam(name = "AutoCompleteGalRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testAutoCompleteGalRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testBootstrapMobileGatewayAppResponse
     */
    @WebMethod(action = "urn:zimbraAccount/BootstrapMobileGatewayApp")
    @WebResult(name = "BootstrapMobileGatewayAppResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testBootstrapMobileGatewayAppResponse bootstrapMobileGatewayAppRequest(
        @WebParam(name = "BootstrapMobileGatewayAppRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testBootstrapMobileGatewayAppRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testChangePasswordResponse
     */
    @WebMethod(action = "urn:zimbraAccount/ChangePassword")
    @WebResult(name = "ChangePasswordResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testChangePasswordResponse changePasswordRequest(
        @WebParam(name = "ChangePasswordRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testChangePasswordRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testCheckLicenseResponse
     */
    @WebMethod(action = "urn:zimbraAccount/CheckLicense")
    @WebResult(name = "CheckLicenseResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testCheckLicenseResponse checkLicenseRequest(
        @WebParam(name = "CheckLicenseRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testCheckLicenseRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testCheckRightsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/CheckRights")
    @WebResult(name = "CheckRightsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testCheckRightsResponse checkRightsRequest(
        @WebParam(name = "CheckRightsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testCheckRightsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testCreateAppSpecificPasswordResponse
     */
    @WebMethod(action = "urn:zimbraAccount/CreateAppSpecificPassword")
    @WebResult(name = "CreateAppSpecificPasswordResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testCreateAppSpecificPasswordResponse createAppSpecificPasswordRequest(
        @WebParam(name = "CreateAppSpecificPasswordRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testCreateAppSpecificPasswordRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testCreateDistributionListResponse
     */
    @WebMethod(action = "urn:zimbraAccount/CreateDistributionList")
    @WebResult(name = "CreateDistributionListResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testCreateDistributionListResponse createDistributionListRequest(
        @WebParam(name = "CreateDistributionListRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testCreateDistributionListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testCreateIdentityResponse
     */
    @WebMethod(action = "urn:zimbraAccount/CreateIdentity")
    @WebResult(name = "CreateIdentityResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testCreateIdentityResponse createIdentityRequest(
        @WebParam(name = "CreateIdentityRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testCreateIdentityRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testCreateSignatureResponse
     */
    @WebMethod(action = "urn:zimbraAccount/CreateSignature")
    @WebResult(name = "CreateSignatureResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testCreateSignatureResponse createSignatureRequest(
        @WebParam(name = "CreateSignatureRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testCreateSignatureRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testDeleteIdentityResponse
     */
    @WebMethod(action = "urn:zimbraAccount/DeleteIdentity")
    @WebResult(name = "DeleteIdentityResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testDeleteIdentityResponse deleteIdentityRequest(
        @WebParam(name = "DeleteIdentityRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testDeleteIdentityRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testDeleteSignatureResponse
     */
    @WebMethod(action = "urn:zimbraAccount/DeleteSignature")
    @WebResult(name = "DeleteSignatureResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testDeleteSignatureResponse deleteSignatureRequest(
        @WebParam(name = "DeleteSignatureRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testDeleteSignatureRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testDisableTwoFactorAuthResponse
     */
    @WebMethod(action = "urn:zimbraAccount/DisableTwoFactorAuth")
    @WebResult(name = "DisableTwoFactorAuthResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testDisableTwoFactorAuthResponse disableTwoFactorAuthRequest(
        @WebParam(name = "DisableTwoFactorAuthRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testDisableTwoFactorAuthRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testDiscoverRightsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/DiscoverRights")
    @WebResult(name = "DiscoverRightsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testDiscoverRightsResponse discoverRightsRequest(
        @WebParam(name = "DiscoverRightsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testDiscoverRightsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testDistributionListActionResponse
     */
    @WebMethod(action = "urn:zimbraAccount/DistributionListAction")
    @WebResult(name = "DistributionListActionResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testDistributionListActionResponse distributionListActionRequest(
        @WebParam(name = "DistributionListActionRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testDistributionListActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testEnableTwoFactorAuthResponse
     */
    @WebMethod(action = "urn:zimbraAccount/EnableTwoFactorAuth")
    @WebResult(name = "EnableTwoFactorAuthResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testEnableTwoFactorAuthResponse enableTwoFactorAuthRequest(
        @WebParam(name = "EnableTwoFactorAuthRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testEnableTwoFactorAuthRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testEndSessionResponse
     */
    @WebMethod(action = "urn:zimbraAccount/EndSession")
    @WebResult(name = "EndSessionResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testEndSessionResponse endSessionRequest(
        @WebParam(name = "EndSessionRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testEndSessionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGenerateScratchCodesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GenerateScratchCodes")
    @WebResult(name = "GenerateScratchCodesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGenerateScratchCodesResponse generateScratchCodesRequest(
        @WebParam(name = "GenerateScratchCodesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGenerateScratchCodesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetAccountDistributionListsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetAccountDistributionLists")
    @WebResult(name = "GetAccountDistributionListsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetAccountDistributionListsResponse getAccountDistributionListsRequest(
        @WebParam(name = "GetAccountDistributionListsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetAccountDistributionListsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetAccountInfoResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetAccountInfo")
    @WebResult(name = "GetAccountInfoResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetAccountInfoResponse getAccountInfoRequest(
        @WebParam(name = "GetAccountInfoRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetAccountInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetAllLocalesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetAllLocales")
    @WebResult(name = "GetAllLocalesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetAllLocalesResponse getAllLocalesRequest(
        @WebParam(name = "GetAllLocalesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetAllLocalesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetAppSpecificPasswordsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetAppSpecificPasswords")
    @WebResult(name = "GetAppSpecificPasswordsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetAppSpecificPasswordsResponse getAppSpecificPasswordsRequest(
        @WebParam(name = "GetAppSpecificPasswordsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetAppSpecificPasswordsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetAvailableCsvFormatsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetAvailableCsvFormats")
    @WebResult(name = "GetAvailableCsvFormatsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetAvailableCsvFormatsResponse getAvailableCsvFormatsRequest(
        @WebParam(name = "GetAvailableCsvFormatsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetAvailableCsvFormatsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetAvailableLocalesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetAvailableLocales")
    @WebResult(name = "GetAvailableLocalesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetAvailableLocalesResponse getAvailableLocalesRequest(
        @WebParam(name = "GetAvailableLocalesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetAvailableLocalesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetAvailableSkinsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetAvailableSkins")
    @WebResult(name = "GetAvailableSkinsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetAvailableSkinsResponse getAvailableSkinsRequest(
        @WebParam(name = "GetAvailableSkinsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetAvailableSkinsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetDistributionListMembersResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetDistributionListMembers")
    @WebResult(name = "GetDistributionListMembersResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetDistributionListMembersResponse getDistributionListMembersRequest(
        @WebParam(name = "GetDistributionListMembersRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetDistributionListMembersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetDistributionListResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetDistributionList")
    @WebResult(name = "GetDistributionListResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetDistributionListResponse getDistributionListRequest(
        @WebParam(name = "GetDistributionListRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetDistributionListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetGcmSenderIdResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetGcmSenderId")
    @WebResult(name = "GetGcmSenderIdResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetGcmSenderIdResponse getGcmSenderIdRequest(
        @WebParam(name = "GetGcmSenderIdRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetGcmSenderIdRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetIdentitiesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetIdentities")
    @WebResult(name = "GetIdentitiesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetIdentitiesResponse getIdentitiesRequest(
        @WebParam(name = "GetIdentitiesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetIdentitiesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetInfoResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetInfo")
    @WebResult(name = "GetInfoResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetInfoResponse getInfoRequest(
        @WebParam(name = "GetInfoRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetPrefsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetPrefs")
    @WebResult(name = "GetPrefsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetPrefsResponse getPrefsRequest(
        @WebParam(name = "GetPrefsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetPrefsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetRightsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetRights")
    @WebResult(name = "GetRightsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetRightsResponse getRightsRequest(
        @WebParam(name = "GetRightsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetRightsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetSMIMEPublicCertsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetSMIMEPublicCerts")
    @WebResult(name = "GetSMIMEPublicCertsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetSMIMEPublicCertsResponse getSMIMEPublicCertsRequest(
        @WebParam(name = "GetSMIMEPublicCertsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetSMIMEPublicCertsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetShareInfoResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetShareInfo")
    @WebResult(name = "GetShareInfoResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetShareInfoResponse getShareInfoRequest(
        @WebParam(name = "GetShareInfoRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetShareInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetSignaturesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetSignatures")
    @WebResult(name = "GetSignaturesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetSignaturesResponse getSignaturesRequest(
        @WebParam(name = "GetSignaturesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetSignaturesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetTrustedDevicesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetTrustedDevices")
    @WebResult(name = "GetTrustedDevicesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetTrustedDevicesResponse getTrustedDevicesRequest(
        @WebParam(name = "GetTrustedDevicesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetTrustedDevicesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetVersionInfoResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetVersionInfo")
    @WebResult(name = "GetVersionInfoResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetVersionInfoResponse getVersionInfoRequest(
        @WebParam(name = "GetVersionInfoRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetVersionInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGetWhiteBlackListResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GetWhiteBlackList")
    @WebResult(name = "GetWhiteBlackListResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGetWhiteBlackListResponse getWhiteBlackListRequest(
        @WebParam(name = "GetWhiteBlackListRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGetWhiteBlackListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testGrantRightsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/GrantRights")
    @WebResult(name = "GrantRightsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testGrantRightsResponse grantRightsRequest(
        @WebParam(name = "GrantRightsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testGrantRightsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testModifyIdentityResponse
     */
    @WebMethod(action = "urn:zimbraAccount/ModifyIdentity")
    @WebResult(name = "ModifyIdentityResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testModifyIdentityResponse modifyIdentityRequest(
        @WebParam(name = "ModifyIdentityRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testModifyIdentityRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testModifyPrefsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/ModifyPrefs")
    @WebResult(name = "ModifyPrefsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testModifyPrefsResponse modifyPrefsRequest(
        @WebParam(name = "ModifyPrefsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testModifyPrefsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testModifyPropertiesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/ModifyProperties")
    @WebResult(name = "ModifyPropertiesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testModifyPropertiesResponse modifyPropertiesRequest(
        @WebParam(name = "ModifyPropertiesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testModifyPropertiesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testModifySignatureResponse
     */
    @WebMethod(action = "urn:zimbraAccount/ModifySignature")
    @WebResult(name = "ModifySignatureResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testModifySignatureResponse modifySignatureRequest(
        @WebParam(name = "ModifySignatureRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testModifySignatureRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testModifyWhiteBlackListResponse
     */
    @WebMethod(action = "urn:zimbraAccount/ModifyWhiteBlackList")
    @WebResult(name = "ModifyWhiteBlackListResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testModifyWhiteBlackListResponse modifyWhiteBlackListRequest(
        @WebParam(name = "ModifyWhiteBlackListRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testModifyWhiteBlackListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testModifyZimletPrefsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/ModifyZimletPrefs")
    @WebResult(name = "ModifyZimletPrefsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testModifyZimletPrefsResponse modifyZimletPrefsRequest(
        @WebParam(name = "ModifyZimletPrefsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testModifyZimletPrefsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testRegisterMobileGatewayAppResponse
     */
    @WebMethod(action = "urn:zimbraAccount/RegisterMobileGatewayApp")
    @WebResult(name = "RegisterMobileGatewayAppResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testRegisterMobileGatewayAppResponse registerMobileGatewayAppRequest(
        @WebParam(name = "RegisterMobileGatewayAppRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testRegisterMobileGatewayAppRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testRenewMobileGatewayAppTokenResponse
     */
    @WebMethod(action = "urn:zimbraAccount/RenewMobileGatewayAppToken")
    @WebResult(name = "RenewMobileGatewayAppTokenResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testRenewMobileGatewayAppTokenResponse renewMobileGatewayAppTokenRequest(
        @WebParam(name = "RenewMobileGatewayAppTokenRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testRenewMobileGatewayAppTokenRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testRevokeAppSpecificPasswordResponse
     */
    @WebMethod(action = "urn:zimbraAccount/RevokeAppSpecificPassword")
    @WebResult(name = "RevokeAppSpecificPasswordResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testRevokeAppSpecificPasswordResponse revokeAppSpecificPasswordRequest(
        @WebParam(name = "RevokeAppSpecificPasswordRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testRevokeAppSpecificPasswordRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testRevokeOtherTrustedDevicesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/RevokeOtherTrustedDevices")
    @WebResult(name = "RevokeOtherTrustedDevicesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testRevokeOtherTrustedDevicesResponse revokeOtherTrustedDevicesRequest(
        @WebParam(name = "RevokeOtherTrustedDevicesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testRevokeOtherTrustedDevicesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testRevokeRightsResponse
     */
    @WebMethod(action = "urn:zimbraAccount/RevokeRights")
    @WebResult(name = "RevokeRightsResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testRevokeRightsResponse revokeRightsRequest(
        @WebParam(name = "RevokeRightsRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testRevokeRightsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testRevokeTrustedDeviceResponse
     */
    @WebMethod(action = "urn:zimbraAccount/RevokeTrustedDevice")
    @WebResult(name = "RevokeTrustedDeviceResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testRevokeTrustedDeviceResponse revokeTrustedDeviceRequest(
        @WebParam(name = "RevokeTrustedDeviceRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testRevokeTrustedDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testSearchCalendarResourcesResponse
     */
    @WebMethod(action = "urn:zimbraAccount/SearchCalendarResources")
    @WebResult(name = "SearchCalendarResourcesResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testSearchCalendarResourcesResponse searchCalendarResourcesRequest(
        @WebParam(name = "SearchCalendarResourcesRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testSearchCalendarResourcesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testSearchGalResponse
     */
    @WebMethod(action = "urn:zimbraAccount/SearchGal")
    @WebResult(name = "SearchGalResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testSearchGalResponse searchGalRequest(
        @WebParam(name = "SearchGalRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testSearchGalRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testSubscribeDistributionListResponse
     */
    @WebMethod(action = "urn:zimbraAccount/SubscribeDistributionList")
    @WebResult(name = "SubscribeDistributionListResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testSubscribeDistributionListResponse subscribeDistributionListRequest(
        @WebParam(name = "SubscribeDistributionListRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testSubscribeDistributionListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.account.testSyncGalResponse
     */
    @WebMethod(action = "urn:zimbraAccount/SyncGal")
    @WebResult(name = "SyncGalResponse", targetNamespace = "urn:zimbraAccount", partName = "params")
    public testSyncGalResponse syncGalRequest(
        @WebParam(name = "SyncGalRequest", targetNamespace = "urn:zimbraAccount", partName = "params")
        testSyncGalRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testAddAppointmentInviteResponse
     */
    @WebMethod(action = "urn:zimbraMail/AddAppointmentInvite")
    @WebResult(name = "AddAppointmentInviteResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testAddAppointmentInviteResponse addAppointmentInviteRequest(
        @WebParam(name = "AddAppointmentInviteRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testAddAppointmentInviteRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testAddCommentResponse
     */
    @WebMethod(action = "urn:zimbraMail/AddComment")
    @WebResult(name = "AddCommentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testAddCommentResponse addCommentRequest(
        @WebParam(name = "AddCommentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testAddCommentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testAddMsgResponse
     */
    @WebMethod(action = "urn:zimbraMail/AddMsg")
    @WebResult(name = "AddMsgResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testAddMsgResponse addMsgRequest(
        @WebParam(name = "AddMsgRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testAddMsgRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testAddTaskInviteResponse
     */
    @WebMethod(action = "urn:zimbraMail/AddTaskInvite")
    @WebResult(name = "AddTaskInviteResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testAddTaskInviteResponse addTaskInviteRequest(
        @WebParam(name = "AddTaskInviteRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testAddTaskInviteRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testAnnounceOrganizerChangeResponse
     */
    @WebMethod(action = "urn:zimbraMail/AnnounceOrganizerChange")
    @WebResult(name = "AnnounceOrganizerChangeResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testAnnounceOrganizerChangeResponse announceOrganizerChangeRequest(
        @WebParam(name = "AnnounceOrganizerChangeRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testAnnounceOrganizerChangeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testApplyFilterRulesResponse
     */
    @WebMethod(action = "urn:zimbraMail/ApplyFilterRules")
    @WebResult(name = "ApplyFilterRulesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testApplyFilterRulesResponse applyFilterRulesRequest(
        @WebParam(name = "ApplyFilterRulesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testApplyFilterRulesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testApplyOutgoingFilterRulesResponse
     */
    @WebMethod(action = "urn:zimbraMail/ApplyOutgoingFilterRules")
    @WebResult(name = "ApplyOutgoingFilterRulesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testApplyOutgoingFilterRulesResponse applyOutgoingFilterRulesRequest(
        @WebParam(name = "ApplyOutgoingFilterRulesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testApplyOutgoingFilterRulesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testAutoCompleteResponse
     */
    @WebMethod(action = "urn:zimbraMail/AutoComplete")
    @WebResult(name = "AutoCompleteResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testAutoCompleteResponse autoCompleteRequest(
        @WebParam(name = "AutoCompleteRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testAutoCompleteRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testBounceMsgResponse
     */
    @WebMethod(action = "urn:zimbraMail/BounceMsg")
    @WebResult(name = "BounceMsgResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testBounceMsgResponse bounceMsgRequest(
        @WebParam(name = "BounceMsgRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testBounceMsgRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testBrowseResponse
     */
    @WebMethod(action = "urn:zimbraMail/Browse")
    @WebResult(name = "BrowseResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testBrowseResponse browseRequest(
        @WebParam(name = "BrowseRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testBrowseRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCancelAppointmentResponse
     */
    @WebMethod(action = "urn:zimbraMail/CancelAppointment")
    @WebResult(name = "CancelAppointmentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCancelAppointmentResponse cancelAppointmentRequest(
        @WebParam(name = "CancelAppointmentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCancelAppointmentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCancelTaskResponse
     */
    @WebMethod(action = "urn:zimbraMail/CancelTask")
    @WebResult(name = "CancelTaskResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCancelTaskResponse cancelTaskRequest(
        @WebParam(name = "CancelTaskRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCancelTaskRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCheckDeviceStatusResponse
     */
    @WebMethod(action = "urn:zimbraMail/CheckDeviceStatus")
    @WebResult(name = "CheckDeviceStatusResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCheckDeviceStatusResponse checkDeviceStatusRequest(
        @WebParam(name = "CheckDeviceStatusRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCheckDeviceStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCheckPermissionResponse
     */
    @WebMethod(action = "urn:zimbraMail/CheckPermission")
    @WebResult(name = "CheckPermissionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCheckPermissionResponse checkPermissionRequest(
        @WebParam(name = "CheckPermissionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCheckPermissionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCheckRecurConflictsResponse
     */
    @WebMethod(action = "urn:zimbraMail/CheckRecurConflicts")
    @WebResult(name = "CheckRecurConflictsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCheckRecurConflictsResponse checkRecurConflictsRequest(
        @WebParam(name = "CheckRecurConflictsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCheckRecurConflictsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCheckSpellingResponse
     */
    @WebMethod(action = "urn:zimbraMail/CheckSpelling")
    @WebResult(name = "CheckSpellingResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCheckSpellingResponse checkSpellingRequest(
        @WebParam(name = "CheckSpellingRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCheckSpellingRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCompleteTaskInstanceResponse
     */
    @WebMethod(action = "urn:zimbraMail/CompleteTaskInstance")
    @WebResult(name = "CompleteTaskInstanceResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCompleteTaskInstanceResponse completeTaskInstanceRequest(
        @WebParam(name = "CompleteTaskInstanceRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCompleteTaskInstanceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testContactActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/ContactAction")
    @WebResult(name = "ContactActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testContactActionResponse contactActionRequest(
        @WebParam(name = "ContactActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testContactActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testConvActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/ConvAction")
    @WebResult(name = "ConvActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testConvActionResponse convActionRequest(
        @WebParam(name = "ConvActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testConvActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCounterAppointmentResponse
     */
    @WebMethod(action = "urn:zimbraMail/CounterAppointment")
    @WebResult(name = "CounterAppointmentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCounterAppointmentResponse counterAppointmentRequest(
        @WebParam(name = "CounterAppointmentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCounterAppointmentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateAppointmentExceptionResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateAppointmentException")
    @WebResult(name = "CreateAppointmentExceptionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateAppointmentExceptionResponse createAppointmentExceptionRequest(
        @WebParam(name = "CreateAppointmentExceptionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateAppointmentExceptionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateAppointmentResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateAppointment")
    @WebResult(name = "CreateAppointmentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateAppointmentResponse createAppointmentRequest(
        @WebParam(name = "CreateAppointmentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateAppointmentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateContactResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateContact")
    @WebResult(name = "CreateContactResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateContactResponse createContactRequest(
        @WebParam(name = "CreateContactRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateContactRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateDataSourceResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateDataSource")
    @WebResult(name = "CreateDataSourceResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateDataSourceResponse createDataSourceRequest(
        @WebParam(name = "CreateDataSourceRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateDataSourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateFolderResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateFolder")
    @WebResult(name = "CreateFolderResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateFolderResponse createFolderRequest(
        @WebParam(name = "CreateFolderRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateFolderRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateMountpointResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateMountpoint")
    @WebResult(name = "CreateMountpointResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateMountpointResponse createMountpointRequest(
        @WebParam(name = "CreateMountpointRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateMountpointRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateNoteResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateNote")
    @WebResult(name = "CreateNoteResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateNoteResponse createNoteRequest(
        @WebParam(name = "CreateNoteRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateNoteRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateSearchFolderResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateSearchFolder")
    @WebResult(name = "CreateSearchFolderResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateSearchFolderResponse createSearchFolderRequest(
        @WebParam(name = "CreateSearchFolderRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateSearchFolderRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateTagResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateTag")
    @WebResult(name = "CreateTagResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateTagResponse createTagRequest(
        @WebParam(name = "CreateTagRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateTagRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateTaskExceptionResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateTaskException")
    @WebResult(name = "CreateTaskExceptionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateTaskExceptionResponse createTaskExceptionRequest(
        @WebParam(name = "CreateTaskExceptionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateTaskExceptionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateTaskResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateTask")
    @WebResult(name = "CreateTaskResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateTaskResponse createTaskRequest(
        @WebParam(name = "CreateTaskRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateTaskRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testCreateWaitSetResponse
     */
    @WebMethod(action = "urn:zimbraMail/CreateWaitSet")
    @WebResult(name = "CreateWaitSetResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testCreateWaitSetResponse createWaitSetRequest(
        @WebParam(name = "CreateWaitSetRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testCreateWaitSetRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testDeclineCounterAppointmentResponse
     */
    @WebMethod(action = "urn:zimbraMail/DeclineCounterAppointment")
    @WebResult(name = "DeclineCounterAppointmentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testDeclineCounterAppointmentResponse declineCounterAppointmentRequest(
        @WebParam(name = "DeclineCounterAppointmentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testDeclineCounterAppointmentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testDeleteDataSourceResponse
     */
    @WebMethod(action = "urn:zimbraMail/DeleteDataSource")
    @WebResult(name = "DeleteDataSourceResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testDeleteDataSourceResponse deleteDataSourceRequest(
        @WebParam(name = "DeleteDataSourceRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testDeleteDataSourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testDeleteDeviceResponse
     */
    @WebMethod(action = "urn:zimbraMail/DeleteDevice")
    @WebResult(name = "DeleteDeviceResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testDeleteDeviceResponse deleteDeviceRequest(
        @WebParam(name = "DeleteDeviceRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testDeleteDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testDestroyWaitSetResponse
     */
    @WebMethod(action = "urn:zimbraMail/DestroyWaitSet")
    @WebResult(name = "DestroyWaitSetResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testDestroyWaitSetResponse destroyWaitSetRequest(
        @WebParam(name = "DestroyWaitSetRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testDestroyWaitSetRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testDiffDocumentResponse
     */
    @WebMethod(action = "urn:zimbraMail/DiffDocument")
    @WebResult(name = "DiffDocumentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testDiffDocumentResponse diffDocumentRequest(
        @WebParam(name = "DiffDocumentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testDiffDocumentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testDismissCalendarItemAlarmResponse
     */
    @WebMethod(action = "urn:zimbraMail/DismissCalendarItemAlarm")
    @WebResult(name = "DismissCalendarItemAlarmResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testDismissCalendarItemAlarmResponse dismissCalendarItemAlarmRequest(
        @WebParam(name = "DismissCalendarItemAlarmRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testDismissCalendarItemAlarmRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testDocumentActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/DocumentAction")
    @WebResult(name = "DocumentActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testDocumentActionResponse documentActionRequest(
        @WebParam(name = "DocumentActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testDocumentActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testEmptyDumpsterResponse
     */
    @WebMethod(action = "urn:zimbraMail/EmptyDumpster")
    @WebResult(name = "EmptyDumpsterResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testEmptyDumpsterResponse emptyDumpsterRequest(
        @WebParam(name = "EmptyDumpsterRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testEmptyDumpsterRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testEnableSharedReminderResponse
     */
    @WebMethod(action = "urn:zimbraMail/EnableSharedReminder")
    @WebResult(name = "EnableSharedReminderResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testEnableSharedReminderResponse enableSharedReminderRequest(
        @WebParam(name = "EnableSharedReminderRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testEnableSharedReminderRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testExpandRecurResponse
     */
    @WebMethod(action = "urn:zimbraMail/ExpandRecur")
    @WebResult(name = "ExpandRecurResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testExpandRecurResponse expandRecurRequest(
        @WebParam(name = "ExpandRecurRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testExpandRecurRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testExportContactsResponse
     */
    @WebMethod(action = "urn:zimbraMail/ExportContacts")
    @WebResult(name = "ExportContactsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testExportContactsResponse exportContactsRequest(
        @WebParam(name = "ExportContactsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testExportContactsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testFolderActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/FolderAction")
    @WebResult(name = "FolderActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testFolderActionResponse folderActionRequest(
        @WebParam(name = "FolderActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testFolderActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testForwardAppointmentInviteResponse
     */
    @WebMethod(action = "urn:zimbraMail/ForwardAppointmentInvite")
    @WebResult(name = "ForwardAppointmentInviteResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testForwardAppointmentInviteResponse forwardAppointmentInviteRequest(
        @WebParam(name = "ForwardAppointmentInviteRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testForwardAppointmentInviteRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testForwardAppointmentResponse
     */
    @WebMethod(action = "urn:zimbraMail/ForwardAppointment")
    @WebResult(name = "ForwardAppointmentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testForwardAppointmentResponse forwardAppointmentRequest(
        @WebParam(name = "ForwardAppointmentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testForwardAppointmentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "urn:zimbraMail/GenerateUUID")
    @WebResult(name = "GenerateUUIDResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public String generateUUIDRequest(
        @WebParam(name = "GenerateUUIDRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGenerateUUIDRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetActivityStreamResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetActivityStream")
    @WebResult(name = "GetActivityStreamResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetActivityStreamResponse getActivityStreamRequest(
        @WebParam(name = "GetActivityStreamRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetActivityStreamRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetAllDevicesResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetAllDevices")
    @WebResult(name = "GetAllDevicesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetAllDevicesResponse getAllDevicesRequest(
        @WebParam(name = "GetAllDevicesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetAllDevicesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetAppointmentResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetAppointment")
    @WebResult(name = "GetAppointmentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetAppointmentResponse getAppointmentRequest(
        @WebParam(name = "GetAppointmentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetAppointmentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetApptSummariesResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetApptSummaries")
    @WebResult(name = "GetApptSummariesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetApptSummariesResponse getApptSummariesRequest(
        @WebParam(name = "GetApptSummariesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetApptSummariesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetCalendarItemSummariesResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetCalendarItemSummaries")
    @WebResult(name = "GetCalendarItemSummariesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetCalendarItemSummariesResponse getCalendarItemSummariesRequest(
        @WebParam(name = "GetCalendarItemSummariesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetCalendarItemSummariesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetCommentsResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetComments")
    @WebResult(name = "GetCommentsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetCommentsResponse getCommentsRequest(
        @WebParam(name = "GetCommentsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetCommentsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetContactsResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetContacts")
    @WebResult(name = "GetContactsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetContactsResponse getContactsRequest(
        @WebParam(name = "GetContactsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetContactsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetConvResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetConv")
    @WebResult(name = "GetConvResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetConvResponse getConvRequest(
        @WebParam(name = "GetConvRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetConvRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetCustomMetadataResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetCustomMetadata")
    @WebResult(name = "GetCustomMetadataResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetCustomMetadataResponse getCustomMetadataRequest(
        @WebParam(name = "GetCustomMetadataRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetCustomMetadataRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetDataSourcesResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetDataSources")
    @WebResult(name = "GetDataSourcesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetDataSourcesResponse getDataSourcesRequest(
        @WebParam(name = "GetDataSourcesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetDataSourcesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "urn:zimbraMail/GetDocumentShareURL")
    @WebResult(name = "GetDocumentShareURLResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public String getDocumentShareURLRequest(
        @WebParam(name = "GetDocumentShareURLRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetDocumentShareURLRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetEffectiveFolderPermsResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetEffectiveFolderPerms")
    @WebResult(name = "GetEffectiveFolderPermsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetEffectiveFolderPermsResponse getEffectiveFolderPermsRequest(
        @WebParam(name = "GetEffectiveFolderPermsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetEffectiveFolderPermsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetFilterRulesResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetFilterRules")
    @WebResult(name = "GetFilterRulesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetFilterRulesResponse getFilterRulesRequest(
        @WebParam(name = "GetFilterRulesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetFilterRulesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetFolderResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetFolder")
    @WebResult(name = "GetFolderResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetFolderResponse getFolderRequest(
        @WebParam(name = "GetFolderRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetFolderRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetFreeBusyResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetFreeBusy")
    @WebResult(name = "GetFreeBusyResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetFreeBusyResponse getFreeBusyRequest(
        @WebParam(name = "GetFreeBusyRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetFreeBusyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetICalResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetICal")
    @WebResult(name = "GetICalResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetICalResponse getICalRequest(
        @WebParam(name = "GetICalRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetICalRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetImportStatusResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetImportStatus")
    @WebResult(name = "GetImportStatusResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetImportStatusResponse getImportStatusRequest(
        @WebParam(name = "GetImportStatusRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetImportStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetItemResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetItem")
    @WebResult(name = "GetItemResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetItemResponse getItemRequest(
        @WebParam(name = "GetItemRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetItemRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetMailboxMetadataResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetMailboxMetadata")
    @WebResult(name = "GetMailboxMetadataResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetMailboxMetadataResponse getMailboxMetadataRequest(
        @WebParam(name = "GetMailboxMetadataRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetMailboxMetadataRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetMiniCalResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetMiniCal")
    @WebResult(name = "GetMiniCalResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetMiniCalResponse getMiniCalRequest(
        @WebParam(name = "GetMiniCalRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetMiniCalRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetMsgMetadataResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetMsgMetadata")
    @WebResult(name = "GetMsgMetadataResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetMsgMetadataResponse getMsgMetadataRequest(
        @WebParam(name = "GetMsgMetadataRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetMsgMetadataRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetMsgResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetMsg")
    @WebResult(name = "GetMsgResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetMsgResponse getMsgRequest(
        @WebParam(name = "GetMsgRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetMsgRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetNoteResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetNote")
    @WebResult(name = "GetNoteResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetNoteResponse getNoteRequest(
        @WebParam(name = "GetNoteRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetNoteRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetNotificationsResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetNotifications")
    @WebResult(name = "GetNotificationsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetNotificationsResponse getNotificationsRequest(
        @WebParam(name = "GetNotificationsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetNotificationsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetOutgoingFilterRulesResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetOutgoingFilterRules")
    @WebResult(name = "GetOutgoingFilterRulesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetOutgoingFilterRulesResponse getOutgoingFilterRulesRequest(
        @WebParam(name = "GetOutgoingFilterRulesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetOutgoingFilterRulesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetPermissionResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetPermission")
    @WebResult(name = "GetPermissionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetPermissionResponse getPermissionRequest(
        @WebParam(name = "GetPermissionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetPermissionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetRecurResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetRecur")
    @WebResult(name = "GetRecurResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetRecurResponse getRecurRequest(
        @WebParam(name = "GetRecurRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetRecurRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetSearchFolderResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetSearchFolder")
    @WebResult(name = "GetSearchFolderResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetSearchFolderResponse getSearchFolderRequest(
        @WebParam(name = "GetSearchFolderRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetSearchFolderRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetShareDetailsResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetShareDetails")
    @WebResult(name = "GetShareDetailsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetShareDetailsResponse getShareDetailsRequest(
        @WebParam(name = "GetShareDetailsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetShareDetailsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetShareNotificationsResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetShareNotifications")
    @WebResult(name = "GetShareNotificationsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetShareNotificationsResponse getShareNotificationsRequest(
        @WebParam(name = "GetShareNotificationsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetShareNotificationsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetSpellDictionariesResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetSpellDictionaries")
    @WebResult(name = "GetSpellDictionariesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetSpellDictionariesResponse getSpellDictionariesRequest(
        @WebParam(name = "GetSpellDictionariesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetSpellDictionariesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetSystemRetentionPolicyResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetSystemRetentionPolicy")
    @WebResult(name = "GetSystemRetentionPolicyResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetSystemRetentionPolicyResponse getSystemRetentionPolicyRequest(
        @WebParam(name = "GetSystemRetentionPolicyRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetSystemRetentionPolicyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetTagResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetTag")
    @WebResult(name = "GetTagResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetTagResponse getTagRequest(
        @WebParam(name = "GetTagRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetTagRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetTaskResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetTask")
    @WebResult(name = "GetTaskResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetTaskResponse getTaskRequest(
        @WebParam(name = "GetTaskRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetTaskRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetTaskSummariesResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetTaskSummaries")
    @WebResult(name = "GetTaskSummariesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetTaskSummariesResponse getTaskSummariesRequest(
        @WebParam(name = "GetTaskSummariesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetTaskSummariesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetWatchersResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetWatchers")
    @WebResult(name = "GetWatchersResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetWatchersResponse getWatchersRequest(
        @WebParam(name = "GetWatchersRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetWatchersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetWatchingItemsResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetWatchingItems")
    @WebResult(name = "GetWatchingItemsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetWatchingItemsResponse getWatchingItemsRequest(
        @WebParam(name = "GetWatchingItemsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetWatchingItemsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetWorkingHoursResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetWorkingHours")
    @WebResult(name = "GetWorkingHoursResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetWorkingHoursResponse getWorkingHoursRequest(
        @WebParam(name = "GetWorkingHoursRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetWorkingHoursRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetYahooAuthTokenResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetYahooAuthToken")
    @WebResult(name = "GetYahooAuthTokenResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetYahooAuthTokenResponse getYahooAuthTokenRequest(
        @WebParam(name = "GetYahooAuthTokenRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetYahooAuthTokenRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGetYahooCookieResponse
     */
    @WebMethod(action = "urn:zimbraMail/GetYahooCookie")
    @WebResult(name = "GetYahooCookieResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGetYahooCookieResponse getYahooCookieRequest(
        @WebParam(name = "GetYahooCookieRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGetYahooCookieRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testGrantPermissionResponse
     */
    @WebMethod(action = "urn:zimbraMail/GrantPermission")
    @WebResult(name = "GrantPermissionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testGrantPermissionResponse grantPermissionRequest(
        @WebParam(name = "GrantPermissionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testGrantPermissionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testICalReplyResponse
     */
    @WebMethod(action = "urn:zimbraMail/ICalReply")
    @WebResult(name = "ICalReplyResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testICalReplyResponse iCalReplyRequest(
        @WebParam(name = "ICalReplyRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testICalReplyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testImportAppointmentsResponse
     */
    @WebMethod(action = "urn:zimbraMail/ImportAppointments")
    @WebResult(name = "ImportAppointmentsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testImportAppointmentsResponse importAppointmentsRequest(
        @WebParam(name = "ImportAppointmentsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testImportAppointmentsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testImportContactsResponse
     */
    @WebMethod(action = "urn:zimbraMail/ImportContacts")
    @WebResult(name = "ImportContactsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testImportContactsResponse importContactsRequest(
        @WebParam(name = "ImportContactsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testImportContactsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testImportDataResponse
     */
    @WebMethod(action = "urn:zimbraMail/ImportData")
    @WebResult(name = "ImportDataResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testImportDataResponse importDataRequest(
        @WebParam(name = "ImportDataRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testImportDataRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testInvalidateReminderDeviceResponse
     */
    @WebMethod(action = "urn:zimbraMail/InvalidateReminderDevice")
    @WebResult(name = "InvalidateReminderDeviceResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testInvalidateReminderDeviceResponse invalidateReminderDeviceRequest(
        @WebParam(name = "InvalidateReminderDeviceRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testInvalidateReminderDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testItemActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/ItemAction")
    @WebResult(name = "ItemActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testItemActionResponse itemActionRequest(
        @WebParam(name = "ItemActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testItemActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testListDocumentRevisionsResponse
     */
    @WebMethod(action = "urn:zimbraMail/ListDocumentRevisions")
    @WebResult(name = "ListDocumentRevisionsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testListDocumentRevisionsResponse listDocumentRevisionsRequest(
        @WebParam(name = "ListDocumentRevisionsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testListDocumentRevisionsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testModifyAppointmentResponse
     */
    @WebMethod(action = "urn:zimbraMail/ModifyAppointment")
    @WebResult(name = "ModifyAppointmentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testModifyAppointmentResponse modifyAppointmentRequest(
        @WebParam(name = "ModifyAppointmentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testModifyAppointmentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testModifyContactResponse
     */
    @WebMethod(action = "urn:zimbraMail/ModifyContact")
    @WebResult(name = "ModifyContactResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testModifyContactResponse modifyContactRequest(
        @WebParam(name = "ModifyContactRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testModifyContactRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testModifyDataSourceResponse
     */
    @WebMethod(action = "urn:zimbraMail/ModifyDataSource")
    @WebResult(name = "ModifyDataSourceResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testModifyDataSourceResponse modifyDataSourceRequest(
        @WebParam(name = "ModifyDataSourceRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testModifyDataSourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testModifyFilterRulesResponse
     */
    @WebMethod(action = "urn:zimbraMail/ModifyFilterRules")
    @WebResult(name = "ModifyFilterRulesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testModifyFilterRulesResponse modifyFilterRulesRequest(
        @WebParam(name = "ModifyFilterRulesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testModifyFilterRulesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testModifyMailboxMetadataResponse
     */
    @WebMethod(action = "urn:zimbraMail/ModifyMailboxMetadata")
    @WebResult(name = "ModifyMailboxMetadataResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testModifyMailboxMetadataResponse modifyMailboxMetadataRequest(
        @WebParam(name = "ModifyMailboxMetadataRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testModifyMailboxMetadataRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testModifyOutgoingFilterRulesResponse
     */
    @WebMethod(action = "urn:zimbraMail/ModifyOutgoingFilterRules")
    @WebResult(name = "ModifyOutgoingFilterRulesResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testModifyOutgoingFilterRulesResponse modifyOutgoingFilterRulesRequest(
        @WebParam(name = "ModifyOutgoingFilterRulesRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testModifyOutgoingFilterRulesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testModifySearchFolderResponse
     */
    @WebMethod(action = "urn:zimbraMail/ModifySearchFolder")
    @WebResult(name = "ModifySearchFolderResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testModifySearchFolderResponse modifySearchFolderRequest(
        @WebParam(name = "ModifySearchFolderRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testModifySearchFolderRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testModifyTaskResponse
     */
    @WebMethod(action = "urn:zimbraMail/ModifyTask")
    @WebResult(name = "ModifyTaskResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testModifyTaskResponse modifyTaskRequest(
        @WebParam(name = "ModifyTaskRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testModifyTaskRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testMsgActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/MsgAction")
    @WebResult(name = "MsgActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testMsgActionResponse msgActionRequest(
        @WebParam(name = "MsgActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testMsgActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testNoOpResponse
     */
    @WebMethod(action = "urn:zimbraMail/NoOp")
    @WebResult(name = "NoOpResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testNoOpResponse noOpRequest(
        @WebParam(name = "NoOpRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testNoOpRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testNoteActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/NoteAction")
    @WebResult(name = "NoteActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testNoteActionResponse noteActionRequest(
        @WebParam(name = "NoteActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testNoteActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testPurgeRevisionResponse
     */
    @WebMethod(action = "urn:zimbraMail/PurgeRevision")
    @WebResult(name = "PurgeRevisionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testPurgeRevisionResponse purgeRevisionRequest(
        @WebParam(name = "PurgeRevisionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testPurgeRevisionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testRankingActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/RankingAction")
    @WebResult(name = "RankingActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testRankingActionResponse rankingActionRequest(
        @WebParam(name = "RankingActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testRankingActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testRegisterDeviceResponse
     */
    @WebMethod(action = "urn:zimbraMail/RegisterDevice")
    @WebResult(name = "RegisterDeviceResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testRegisterDeviceResponse registerDeviceRequest(
        @WebParam(name = "RegisterDeviceRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testRegisterDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testRemoveAttachmentsResponse
     */
    @WebMethod(action = "urn:zimbraMail/RemoveAttachments")
    @WebResult(name = "RemoveAttachmentsResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testRemoveAttachmentsResponse removeAttachmentsRequest(
        @WebParam(name = "RemoveAttachmentsRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testRemoveAttachmentsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testRevokePermissionResponse
     */
    @WebMethod(action = "urn:zimbraMail/RevokePermission")
    @WebResult(name = "RevokePermissionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testRevokePermissionResponse revokePermissionRequest(
        @WebParam(name = "RevokePermissionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testRevokePermissionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSaveDocumentResponse
     */
    @WebMethod(action = "urn:zimbraMail/SaveDocument")
    @WebResult(name = "SaveDocumentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSaveDocumentResponse saveDocumentRequest(
        @WebParam(name = "SaveDocumentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSaveDocumentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSaveDraftResponse
     */
    @WebMethod(action = "urn:zimbraMail/SaveDraft")
    @WebResult(name = "SaveDraftResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSaveDraftResponse saveDraftRequest(
        @WebParam(name = "SaveDraftRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSaveDraftRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSearchConvResponse
     */
    @WebMethod(action = "urn:zimbraMail/SearchConv")
    @WebResult(name = "SearchConvResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSearchConvResponse searchConvRequest(
        @WebParam(name = "SearchConvRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSearchConvRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSearchResponse
     */
    @WebMethod(action = "urn:zimbraMail/Search")
    @WebResult(name = "SearchResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSearchResponse searchRequest(
        @WebParam(name = "SearchRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSearchRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSendDeliveryReportResponse
     */
    @WebMethod(action = "urn:zimbraMail/SendDeliveryReport")
    @WebResult(name = "SendDeliveryReportResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSendDeliveryReportResponse sendDeliveryReportRequest(
        @WebParam(name = "SendDeliveryReportRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSendDeliveryReportRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSendInviteReplyResponse
     */
    @WebMethod(action = "urn:zimbraMail/SendInviteReply")
    @WebResult(name = "SendInviteReplyResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSendInviteReplyResponse sendInviteReplyRequest(
        @WebParam(name = "SendInviteReplyRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSendInviteReplyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSendMsgResponse
     */
    @WebMethod(action = "urn:zimbraMail/SendMsg")
    @WebResult(name = "SendMsgResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSendMsgResponse sendMsgRequest(
        @WebParam(name = "SendMsgRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSendMsgRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSendShareNotificationResponse
     */
    @WebMethod(action = "urn:zimbraMail/SendShareNotification")
    @WebResult(name = "SendShareNotificationResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSendShareNotificationResponse sendShareNotificationRequest(
        @WebParam(name = "SendShareNotificationRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSendShareNotificationRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSendVerificationCodeResponse
     */
    @WebMethod(action = "urn:zimbraMail/SendVerificationCode")
    @WebResult(name = "SendVerificationCodeResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSendVerificationCodeResponse sendVerificationCodeRequest(
        @WebParam(name = "SendVerificationCodeRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSendVerificationCodeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSetAppointmentResponse
     */
    @WebMethod(action = "urn:zimbraMail/SetAppointment")
    @WebResult(name = "SetAppointmentResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSetAppointmentResponse setAppointmentRequest(
        @WebParam(name = "SetAppointmentRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSetAppointmentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSetCustomMetadataResponse
     */
    @WebMethod(action = "urn:zimbraMail/SetCustomMetadata")
    @WebResult(name = "SetCustomMetadataResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSetCustomMetadataResponse setCustomMetadataRequest(
        @WebParam(name = "SetCustomMetadataRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSetCustomMetadataRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSetMailboxMetadataResponse
     */
    @WebMethod(action = "urn:zimbraMail/SetMailboxMetadata")
    @WebResult(name = "SetMailboxMetadataResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSetMailboxMetadataResponse setMailboxMetadataRequest(
        @WebParam(name = "SetMailboxMetadataRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSetMailboxMetadataRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSetTaskResponse
     */
    @WebMethod(action = "urn:zimbraMail/SetTask")
    @WebResult(name = "SetTaskResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSetTaskResponse setTaskRequest(
        @WebParam(name = "SetTaskRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSetTaskRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSnoozeCalendarItemAlarmResponse
     */
    @WebMethod(action = "urn:zimbraMail/SnoozeCalendarItemAlarm")
    @WebResult(name = "SnoozeCalendarItemAlarmResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSnoozeCalendarItemAlarmResponse snoozeCalendarItemAlarmRequest(
        @WebParam(name = "SnoozeCalendarItemAlarmRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSnoozeCalendarItemAlarmRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testSyncResponse
     */
    @WebMethod(action = "urn:zimbraMail/Sync")
    @WebResult(name = "SyncResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testSyncResponse syncRequest(
        @WebParam(name = "SyncRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testSyncRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testTagActionResponse
     */
    @WebMethod(action = "urn:zimbraMail/TagAction")
    @WebResult(name = "TagActionResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testTagActionResponse tagActionRequest(
        @WebParam(name = "TagActionRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testTagActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testTestDataSourceResponse
     */
    @WebMethod(action = "urn:zimbraMail/TestDataSource")
    @WebResult(name = "TestDataSourceResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testTestDataSourceResponse testDataSourceRequest(
        @WebParam(name = "TestDataSourceRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testTestDataSourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testUpdateDeviceStatusResponse
     */
    @WebMethod(action = "urn:zimbraMail/UpdateDeviceStatus")
    @WebResult(name = "UpdateDeviceStatusResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testUpdateDeviceStatusResponse updateDeviceStatusRequest(
        @WebParam(name = "UpdateDeviceStatusRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testUpdateDeviceStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testVerifyCodeResponse
     */
    @WebMethod(action = "urn:zimbraMail/VerifyCode")
    @WebResult(name = "VerifyCodeResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testVerifyCodeResponse verifyCodeRequest(
        @WebParam(name = "VerifyCodeRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testVerifyCodeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.mail.testWaitSetResponse
     */
    @WebMethod(action = "urn:zimbraMail/WaitSet")
    @WebResult(name = "WaitSetResponse", targetNamespace = "urn:zimbraMail", partName = "params")
    public testWaitSetResponse waitSetRequest(
        @WebParam(name = "WaitSetRequest", targetNamespace = "urn:zimbraMail", partName = "params")
        testWaitSetRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testBecomeMasterResponse
     */
    @WebMethod(action = "urn:zimbraRepl/BecomeMaster")
    @WebResult(name = "BecomeMasterResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testBecomeMasterResponse becomeMasterRequest(
        @WebParam(name = "BecomeMasterRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testBecomeMasterRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testBringDownServiceIPResponse
     */
    @WebMethod(action = "urn:zimbraRepl/BringDownServiceIP")
    @WebResult(name = "BringDownServiceIPResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testBringDownServiceIPResponse bringDownServiceIPRequest(
        @WebParam(name = "BringDownServiceIPRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testBringDownServiceIPRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testBringUpServiceIPResponse
     */
    @WebMethod(action = "urn:zimbraRepl/BringUpServiceIP")
    @WebResult(name = "BringUpServiceIPResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testBringUpServiceIPResponse bringUpServiceIPRequest(
        @WebParam(name = "BringUpServiceIPRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testBringUpServiceIPRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testReplicationStatusResponse
     */
    @WebMethod(action = "urn:zimbraRepl/ReplicationStatus")
    @WebResult(name = "ReplicationStatusResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testReplicationStatusResponse replicationStatusRequest(
        @WebParam(name = "ReplicationStatusRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testReplicationStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testStartCatchupResponse
     */
    @WebMethod(action = "urn:zimbraRepl/StartCatchup")
    @WebResult(name = "StartCatchupResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testStartCatchupResponse startCatchupRequest(
        @WebParam(name = "StartCatchupRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testStartCatchupRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testStartFailoverClientResponse
     */
    @WebMethod(action = "urn:zimbraRepl/StartFailoverClient")
    @WebResult(name = "StartFailoverClientResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testStartFailoverClientResponse startFailoverClientRequest(
        @WebParam(name = "StartFailoverClientRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testStartFailoverClientRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testStartFailoverDaemonResponse
     */
    @WebMethod(action = "urn:zimbraRepl/StartFailoverDaemon")
    @WebResult(name = "StartFailoverDaemonResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testStartFailoverDaemonResponse startFailoverDaemonRequest(
        @WebParam(name = "StartFailoverDaemonRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testStartFailoverDaemonRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testStopFailoverClientResponse
     */
    @WebMethod(action = "urn:zimbraRepl/StopFailoverClient")
    @WebResult(name = "StopFailoverClientResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testStopFailoverClientResponse stopFailoverClientRequest(
        @WebParam(name = "StopFailoverClientRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testStopFailoverClientRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.replication.testStopFailoverDaemonResponse
     */
    @WebMethod(action = "urn:zimbraRepl/StopFailoverDaemon")
    @WebResult(name = "StopFailoverDaemonResponse", targetNamespace = "urn:zimbraRepl", partName = "params")
    public testStopFailoverDaemonResponse stopFailoverDaemonRequest(
        @WebParam(name = "StopFailoverDaemonRequest", targetNamespace = "urn:zimbraRepl", partName = "params")
        testStopFailoverDaemonRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.sync.testCancelPendingRemoteWipeResponse
     */
    @WebMethod(action = "urn:zimbraSync/CancelPendingRemoteWipe")
    @WebResult(name = "CancelPendingRemoteWipeResponse", targetNamespace = "urn:zimbraSync", partName = "params")
    public testCancelPendingRemoteWipeResponse cancelPendingRemoteWipeRequest(
        @WebParam(name = "CancelPendingRemoteWipeRequest", targetNamespace = "urn:zimbraSync", partName = "params")
        testCancelPendingRemoteWipeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.sync.testGetDeviceStatusResponse
     */
    @WebMethod(action = "urn:zimbraSync/GetDeviceStatus")
    @WebResult(name = "GetDeviceStatusResponse", targetNamespace = "urn:zimbraSync", partName = "params")
    public testGetDeviceStatusResponse getDeviceStatusRequest(
        @WebParam(name = "GetDeviceStatusRequest", targetNamespace = "urn:zimbraSync", partName = "params")
        testGetDeviceStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.sync.testRemoteWipeResponse
     */
    @WebMethod(action = "urn:zimbraSync/RemoteWipe")
    @WebResult(name = "RemoteWipeResponse", targetNamespace = "urn:zimbraSync", partName = "params")
    public testRemoteWipeResponse remoteWipeRequest(
        @WebParam(name = "RemoteWipeRequest", targetNamespace = "urn:zimbraSync", partName = "params")
        testRemoteWipeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.sync.testRemoveDeviceResponse
     */
    @WebMethod(action = "urn:zimbraSync/RemoveDevice")
    @WebResult(name = "RemoveDeviceResponse", targetNamespace = "urn:zimbraSync", partName = "params")
    public testRemoveDeviceResponse removeDeviceRequest(
        @WebParam(name = "RemoveDeviceRequest", targetNamespace = "urn:zimbraSync", partName = "params")
        testRemoveDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.sync.testResumeDeviceResponse
     */
    @WebMethod(action = "urn:zimbraSync/ResumeDevice")
    @WebResult(name = "ResumeDeviceResponse", targetNamespace = "urn:zimbraSync", partName = "params")
    public testResumeDeviceResponse resumeDeviceRequest(
        @WebParam(name = "ResumeDeviceRequest", targetNamespace = "urn:zimbraSync", partName = "params")
        testResumeDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.sync.testSuspendDeviceResponse
     */
    @WebMethod(action = "urn:zimbraSync/SuspendDevice")
    @WebResult(name = "SuspendDeviceResponse", targetNamespace = "urn:zimbraSync", partName = "params")
    public testSuspendDeviceResponse suspendDeviceRequest(
        @WebParam(name = "SuspendDeviceRequest", targetNamespace = "urn:zimbraSync", partName = "params")
        testSuspendDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testChangeUCPasswordResponse
     */
    @WebMethod(action = "urn:zimbraVoice/ChangeUCPassword")
    @WebResult(name = "ChangeUCPasswordResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testChangeUCPasswordResponse changeUCPasswordRequest(
        @WebParam(name = "ChangeUCPasswordRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testChangeUCPasswordRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testGetUCInfoResponse
     */
    @WebMethod(action = "urn:zimbraVoice/GetUCInfo")
    @WebResult(name = "GetUCInfoResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testGetUCInfoResponse getUCInfoRequest(
        @WebParam(name = "GetUCInfoRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testGetUCInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testGetVoiceFeaturesResponse
     */
    @WebMethod(action = "urn:zimbraVoice/GetVoiceFeatures")
    @WebResult(name = "GetVoiceFeaturesResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testGetVoiceFeaturesResponse getVoiceFeaturesRequest(
        @WebParam(name = "GetVoiceFeaturesRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testGetVoiceFeaturesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testGetVoiceFolderResponse
     */
    @WebMethod(action = "urn:zimbraVoice/GetVoiceFolder")
    @WebResult(name = "GetVoiceFolderResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testGetVoiceFolderResponse getVoiceFolderRequest(
        @WebParam(name = "GetVoiceFolderRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testGetVoiceFolderRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testGetVoiceInfoResponse
     */
    @WebMethod(action = "urn:zimbraVoice/GetVoiceInfo")
    @WebResult(name = "GetVoiceInfoResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testGetVoiceInfoResponse getVoiceInfoRequest(
        @WebParam(name = "GetVoiceInfoRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testGetVoiceInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testGetVoiceMailPrefsResponse
     */
    @WebMethod(action = "urn:zimbraVoice/GetVoiceMailPrefs")
    @WebResult(name = "GetVoiceMailPrefsResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testGetVoiceMailPrefsResponse getVoiceMailPrefsRequest(
        @WebParam(name = "GetVoiceMailPrefsRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testGetVoiceMailPrefsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testModifyFromNumResponse
     */
    @WebMethod(action = "urn:zimbraVoice/ModifyFromNum")
    @WebResult(name = "ModifyFromNumResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testModifyFromNumResponse modifyFromNumRequest(
        @WebParam(name = "ModifyFromNumRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testModifyFromNumRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testModifyVoiceFeaturesResponse
     */
    @WebMethod(action = "urn:zimbraVoice/ModifyVoiceFeatures")
    @WebResult(name = "ModifyVoiceFeaturesResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testModifyVoiceFeaturesResponse modifyVoiceFeaturesRequest(
        @WebParam(name = "ModifyVoiceFeaturesRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testModifyVoiceFeaturesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testModifyVoiceMailPinResponse
     */
    @WebMethod(action = "urn:zimbraVoice/ModifyVoiceMailPin")
    @WebResult(name = "ModifyVoiceMailPinResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testModifyVoiceMailPinResponse modifyVoiceMailPinRequest(
        @WebParam(name = "ModifyVoiceMailPinRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testModifyVoiceMailPinRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testModifyVoiceMailPrefsResponse
     */
    @WebMethod(action = "urn:zimbraVoice/ModifyVoiceMailPrefs")
    @WebResult(name = "ModifyVoiceMailPrefsResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testModifyVoiceMailPrefsResponse modifyVoiceMailPrefsRequest(
        @WebParam(name = "ModifyVoiceMailPrefsRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testModifyVoiceMailPrefsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testResetVoiceFeaturesResponse
     */
    @WebMethod(action = "urn:zimbraVoice/ResetVoiceFeatures")
    @WebResult(name = "ResetVoiceFeaturesResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testResetVoiceFeaturesResponse resetVoiceFeaturesRequest(
        @WebParam(name = "ResetVoiceFeaturesRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testResetVoiceFeaturesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testSearchVoiceResponse
     */
    @WebMethod(action = "urn:zimbraVoice/SearchVoice")
    @WebResult(name = "SearchVoiceResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testSearchVoiceResponse searchVoiceRequest(
        @WebParam(name = "SearchVoiceRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testSearchVoiceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testUploadVoiceMailResponse
     */
    @WebMethod(action = "urn:zimbraVoice/UploadVoiceMail")
    @WebResult(name = "UploadVoiceMailResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testUploadVoiceMailResponse uploadVoiceMailRequest(
        @WebParam(name = "UploadVoiceMailRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testUploadVoiceMailRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.voice.testVoiceMsgActionResponse
     */
    @WebMethod(action = "urn:zimbraVoice/VoiceMsgAction")
    @WebResult(name = "VoiceMsgActionResponse", targetNamespace = "urn:zimbraVoice", partName = "params")
    public testVoiceMsgActionResponse voiceMsgActionRequest(
        @WebParam(name = "VoiceMsgActionRequest", targetNamespace = "urn:zimbraVoice", partName = "params")
        testVoiceMsgActionRequest params);

}
