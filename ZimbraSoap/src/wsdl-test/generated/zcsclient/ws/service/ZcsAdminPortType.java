
package generated.zcsclient.ws.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import generated.zcsclient.admin.testAbortHsmRequest;
import generated.zcsclient.admin.testAbortHsmResponse;
import generated.zcsclient.admin.testAbortXMbxSearchRequest;
import generated.zcsclient.admin.testAbortXMbxSearchResponse;
import generated.zcsclient.admin.testActivateLicenseRequest;
import generated.zcsclient.admin.testActivateLicenseResponse;
import generated.zcsclient.admin.testAddAccountAliasRequest;
import generated.zcsclient.admin.testAddAccountAliasResponse;
import generated.zcsclient.admin.testAddAccountLoggerRequest;
import generated.zcsclient.admin.testAddAccountLoggerResponse;
import generated.zcsclient.admin.testAddDistributionListAliasRequest;
import generated.zcsclient.admin.testAddDistributionListAliasResponse;
import generated.zcsclient.admin.testAddDistributionListMemberRequest;
import generated.zcsclient.admin.testAddDistributionListMemberResponse;
import generated.zcsclient.admin.testAddGalSyncDataSourceRequest;
import generated.zcsclient.admin.testAddGalSyncDataSourceResponse;
import generated.zcsclient.admin.testAdminCreateWaitSetRequest;
import generated.zcsclient.admin.testAdminCreateWaitSetResponse;
import generated.zcsclient.admin.testAdminDestroyWaitSetRequest;
import generated.zcsclient.admin.testAdminDestroyWaitSetResponse;
import generated.zcsclient.admin.testAdminWaitSetRequest;
import generated.zcsclient.admin.testAdminWaitSetResponse;
import generated.zcsclient.admin.testAuthRequest;
import generated.zcsclient.admin.testAuthResponse;
import generated.zcsclient.admin.testAutoCompleteGalRequest;
import generated.zcsclient.admin.testAutoCompleteGalResponse;
import generated.zcsclient.admin.testAutoProvAccountRequest;
import generated.zcsclient.admin.testAutoProvAccountResponse;
import generated.zcsclient.admin.testAutoProvTaskControlRequest;
import generated.zcsclient.admin.testAutoProvTaskControlResponse;
import generated.zcsclient.admin.testBackupAccountQueryRequest;
import generated.zcsclient.admin.testBackupAccountQueryResponse;
import generated.zcsclient.admin.testBackupQueryRequest;
import generated.zcsclient.admin.testBackupQueryResponse;
import generated.zcsclient.admin.testBackupRequest;
import generated.zcsclient.admin.testBackupResponse;
import generated.zcsclient.admin.testCancelPendingRemoteWipeRequest;
import generated.zcsclient.admin.testCancelPendingRemoteWipeResponse;
import generated.zcsclient.admin.testCheckAuthConfigRequest;
import generated.zcsclient.admin.testCheckAuthConfigResponse;
import generated.zcsclient.admin.testCheckBlobConsistencyRequest;
import generated.zcsclient.admin.testCheckBlobConsistencyResponse;
import generated.zcsclient.admin.testCheckDirectoryRequest;
import generated.zcsclient.admin.testCheckDirectoryResponse;
import generated.zcsclient.admin.testCheckDomainMXRecordRequest;
import generated.zcsclient.admin.testCheckDomainMXRecordResponse;
import generated.zcsclient.admin.testCheckExchangeAuthRequest;
import generated.zcsclient.admin.testCheckExchangeAuthResponse;
import generated.zcsclient.admin.testCheckGalConfigRequest;
import generated.zcsclient.admin.testCheckGalConfigResponse;
import generated.zcsclient.admin.testCheckHealthRequest;
import generated.zcsclient.admin.testCheckHealthResponse;
import generated.zcsclient.admin.testCheckHostnameResolveRequest;
import generated.zcsclient.admin.testCheckHostnameResolveResponse;
import generated.zcsclient.admin.testCheckPasswordStrengthRequest;
import generated.zcsclient.admin.testCheckPasswordStrengthResponse;
import generated.zcsclient.admin.testCheckRightRequest;
import generated.zcsclient.admin.testCheckRightResponse;
import generated.zcsclient.admin.testClearCookieRequest;
import generated.zcsclient.admin.testClearCookieResponse;
import generated.zcsclient.admin.testCompactIndexRequest;
import generated.zcsclient.admin.testCompactIndexResponse;
import generated.zcsclient.admin.testComputeAggregateQuotaUsageRequest;
import generated.zcsclient.admin.testComputeAggregateQuotaUsageResponse;
import generated.zcsclient.admin.testConfigureZimletRequest;
import generated.zcsclient.admin.testConfigureZimletResponse;
import generated.zcsclient.admin.testCopyCosRequest;
import generated.zcsclient.admin.testCopyCosResponse;
import generated.zcsclient.admin.testCountAccountRequest;
import generated.zcsclient.admin.testCountAccountResponse;
import generated.zcsclient.admin.testCountObjectsRequest;
import generated.zcsclient.admin.testCountObjectsResponse;
import generated.zcsclient.admin.testCreateAccountRequest;
import generated.zcsclient.admin.testCreateAccountResponse;
import generated.zcsclient.admin.testCreateAlwaysOnClusterRequest;
import generated.zcsclient.admin.testCreateAlwaysOnClusterResponse;
import generated.zcsclient.admin.testCreateArchiveRequest;
import generated.zcsclient.admin.testCreateArchiveResponse;
import generated.zcsclient.admin.testCreateCalendarResourceRequest;
import generated.zcsclient.admin.testCreateCalendarResourceResponse;
import generated.zcsclient.admin.testCreateCosRequest;
import generated.zcsclient.admin.testCreateCosResponse;
import generated.zcsclient.admin.testCreateDataSourceRequest;
import generated.zcsclient.admin.testCreateDataSourceResponse;
import generated.zcsclient.admin.testCreateDistributionListRequest;
import generated.zcsclient.admin.testCreateDistributionListResponse;
import generated.zcsclient.admin.testCreateDomainRequest;
import generated.zcsclient.admin.testCreateDomainResponse;
import generated.zcsclient.admin.testCreateGalSyncAccountRequest;
import generated.zcsclient.admin.testCreateGalSyncAccountResponse;
import generated.zcsclient.admin.testCreateLDAPEntryRequest;
import generated.zcsclient.admin.testCreateLDAPEntryResponse;
import generated.zcsclient.admin.testCreateServerRequest;
import generated.zcsclient.admin.testCreateServerResponse;
import generated.zcsclient.admin.testCreateSystemRetentionPolicyRequest;
import generated.zcsclient.admin.testCreateSystemRetentionPolicyResponse;
import generated.zcsclient.admin.testCreateUCServiceRequest;
import generated.zcsclient.admin.testCreateUCServiceResponse;
import generated.zcsclient.admin.testCreateVolumeRequest;
import generated.zcsclient.admin.testCreateVolumeResponse;
import generated.zcsclient.admin.testCreateXMPPComponentRequest;
import generated.zcsclient.admin.testCreateXMPPComponentResponse;
import generated.zcsclient.admin.testCreateXMbxSearchRequest;
import generated.zcsclient.admin.testCreateXMbxSearchResponse;
import generated.zcsclient.admin.testCreateZimletRequest;
import generated.zcsclient.admin.testCreateZimletResponse;
import generated.zcsclient.admin.testDedupeBlobsRequest;
import generated.zcsclient.admin.testDedupeBlobsResponse;
import generated.zcsclient.admin.testDelegateAuthRequest;
import generated.zcsclient.admin.testDelegateAuthResponse;
import generated.zcsclient.admin.testDeleteAccountRequest;
import generated.zcsclient.admin.testDeleteAccountResponse;
import generated.zcsclient.admin.testDeleteAlwaysOnClusterRequest;
import generated.zcsclient.admin.testDeleteAlwaysOnClusterResponse;
import generated.zcsclient.admin.testDeleteCalendarResourceRequest;
import generated.zcsclient.admin.testDeleteCalendarResourceResponse;
import generated.zcsclient.admin.testDeleteCosRequest;
import generated.zcsclient.admin.testDeleteCosResponse;
import generated.zcsclient.admin.testDeleteDataSourceRequest;
import generated.zcsclient.admin.testDeleteDataSourceResponse;
import generated.zcsclient.admin.testDeleteDistributionListRequest;
import generated.zcsclient.admin.testDeleteDistributionListResponse;
import generated.zcsclient.admin.testDeleteDomainRequest;
import generated.zcsclient.admin.testDeleteDomainResponse;
import generated.zcsclient.admin.testDeleteGalSyncAccountRequest;
import generated.zcsclient.admin.testDeleteGalSyncAccountResponse;
import generated.zcsclient.admin.testDeleteLDAPEntryRequest;
import generated.zcsclient.admin.testDeleteLDAPEntryResponse;
import generated.zcsclient.admin.testDeleteMailboxRequest;
import generated.zcsclient.admin.testDeleteMailboxResponse;
import generated.zcsclient.admin.testDeleteServerRequest;
import generated.zcsclient.admin.testDeleteServerResponse;
import generated.zcsclient.admin.testDeleteSystemRetentionPolicyRequest;
import generated.zcsclient.admin.testDeleteSystemRetentionPolicyResponse;
import generated.zcsclient.admin.testDeleteUCServiceRequest;
import generated.zcsclient.admin.testDeleteUCServiceResponse;
import generated.zcsclient.admin.testDeleteVolumeRequest;
import generated.zcsclient.admin.testDeleteVolumeResponse;
import generated.zcsclient.admin.testDeleteXMPPComponentRequest;
import generated.zcsclient.admin.testDeleteXMPPComponentResponse;
import generated.zcsclient.admin.testDeleteXMbxSearchRequest;
import generated.zcsclient.admin.testDeleteXMbxSearchResponse;
import generated.zcsclient.admin.testDeleteZimletRequest;
import generated.zcsclient.admin.testDeleteZimletResponse;
import generated.zcsclient.admin.testDeployZimletRequest;
import generated.zcsclient.admin.testDeployZimletResponse;
import generated.zcsclient.admin.testDisableArchiveRequest;
import generated.zcsclient.admin.testDisableArchiveResponse;
import generated.zcsclient.admin.testDumpSessionsRequest;
import generated.zcsclient.admin.testDumpSessionsResponse;
import generated.zcsclient.admin.testEnableArchiveRequest;
import generated.zcsclient.admin.testEnableArchiveResponse;
import generated.zcsclient.admin.testExportAndDeleteItemsRequest;
import generated.zcsclient.admin.testExportAndDeleteItemsResponse;
import generated.zcsclient.admin.testExportMailboxRequest;
import generated.zcsclient.admin.testExportMailboxResponse;
import generated.zcsclient.admin.testFailoverClusterServiceRequest;
import generated.zcsclient.admin.testFailoverClusterServiceResponse;
import generated.zcsclient.admin.testFixCalendarEndTimeRequest;
import generated.zcsclient.admin.testFixCalendarEndTimeResponse;
import generated.zcsclient.admin.testFixCalendarPriorityRequest;
import generated.zcsclient.admin.testFixCalendarPriorityResponse;
import generated.zcsclient.admin.testFixCalendarTZRequest;
import generated.zcsclient.admin.testFixCalendarTZResponse;
import generated.zcsclient.admin.testFlushCacheRequest;
import generated.zcsclient.admin.testFlushCacheResponse;
import generated.zcsclient.admin.testGenCSRRequest;
import generated.zcsclient.admin.testGenCSRResponse;
import generated.zcsclient.admin.testGetAccountInfoRequest;
import generated.zcsclient.admin.testGetAccountInfoResponse;
import generated.zcsclient.admin.testGetAccountLoggersRequest;
import generated.zcsclient.admin.testGetAccountLoggersResponse;
import generated.zcsclient.admin.testGetAccountMembershipRequest;
import generated.zcsclient.admin.testGetAccountMembershipResponse;
import generated.zcsclient.admin.testGetAccountRequest;
import generated.zcsclient.admin.testGetAccountResponse;
import generated.zcsclient.admin.testGetAdminConsoleUICompRequest;
import generated.zcsclient.admin.testGetAdminConsoleUICompResponse;
import generated.zcsclient.admin.testGetAdminExtensionZimletsRequest;
import generated.zcsclient.admin.testGetAdminExtensionZimletsResponse;
import generated.zcsclient.admin.testGetAdminSavedSearchesRequest;
import generated.zcsclient.admin.testGetAdminSavedSearchesResponse;
import generated.zcsclient.admin.testGetAggregateQuotaUsageOnServerRequest;
import generated.zcsclient.admin.testGetAggregateQuotaUsageOnServerResponse;
import generated.zcsclient.admin.testGetAllAccountLoggersRequest;
import generated.zcsclient.admin.testGetAllAccountLoggersResponse;
import generated.zcsclient.admin.testGetAllAccountsRequest;
import generated.zcsclient.admin.testGetAllAccountsResponse;
import generated.zcsclient.admin.testGetAllActiveServersRequest;
import generated.zcsclient.admin.testGetAllActiveServersResponse;
import generated.zcsclient.admin.testGetAllAdminAccountsRequest;
import generated.zcsclient.admin.testGetAllAdminAccountsResponse;
import generated.zcsclient.admin.testGetAllAlwaysOnClustersRequest;
import generated.zcsclient.admin.testGetAllAlwaysOnClustersResponse;
import generated.zcsclient.admin.testGetAllCalendarResourcesRequest;
import generated.zcsclient.admin.testGetAllCalendarResourcesResponse;
import generated.zcsclient.admin.testGetAllConfigRequest;
import generated.zcsclient.admin.testGetAllConfigResponse;
import generated.zcsclient.admin.testGetAllCosRequest;
import generated.zcsclient.admin.testGetAllCosResponse;
import generated.zcsclient.admin.testGetAllDistributionListsRequest;
import generated.zcsclient.admin.testGetAllDistributionListsResponse;
import generated.zcsclient.admin.testGetAllDomainsRequest;
import generated.zcsclient.admin.testGetAllDomainsResponse;
import generated.zcsclient.admin.testGetAllEffectiveRightsRequest;
import generated.zcsclient.admin.testGetAllEffectiveRightsResponse;
import generated.zcsclient.admin.testGetAllFreeBusyProvidersRequest;
import generated.zcsclient.admin.testGetAllFreeBusyProvidersResponse;
import generated.zcsclient.admin.testGetAllLocalesRequest;
import generated.zcsclient.admin.testGetAllLocalesResponse;
import generated.zcsclient.admin.testGetAllMailboxesRequest;
import generated.zcsclient.admin.testGetAllMailboxesResponse;
import generated.zcsclient.admin.testGetAllRightsRequest;
import generated.zcsclient.admin.testGetAllRightsResponse;
import generated.zcsclient.admin.testGetAllServersRequest;
import generated.zcsclient.admin.testGetAllServersResponse;
import generated.zcsclient.admin.testGetAllSkinsRequest;
import generated.zcsclient.admin.testGetAllSkinsResponse;
import generated.zcsclient.admin.testGetAllUCProvidersRequest;
import generated.zcsclient.admin.testGetAllUCProvidersResponse;
import generated.zcsclient.admin.testGetAllUCServicesRequest;
import generated.zcsclient.admin.testGetAllUCServicesResponse;
import generated.zcsclient.admin.testGetAllVolumesRequest;
import generated.zcsclient.admin.testGetAllVolumesResponse;
import generated.zcsclient.admin.testGetAllXMPPComponentsRequest;
import generated.zcsclient.admin.testGetAllXMPPComponentsResponse;
import generated.zcsclient.admin.testGetAllZimletsRequest;
import generated.zcsclient.admin.testGetAllZimletsResponse;
import generated.zcsclient.admin.testGetAlwaysOnClusterRequest;
import generated.zcsclient.admin.testGetAlwaysOnClusterResponse;
import generated.zcsclient.admin.testGetApplianceHSMFSRequest;
import generated.zcsclient.admin.testGetApplianceHSMFSResponse;
import generated.zcsclient.admin.testGetAttributeInfoRequest;
import generated.zcsclient.admin.testGetAttributeInfoResponse;
import generated.zcsclient.admin.testGetCSRRequest;
import generated.zcsclient.admin.testGetCSRResponse;
import generated.zcsclient.admin.testGetCalendarResourceRequest;
import generated.zcsclient.admin.testGetCalendarResourceResponse;
import generated.zcsclient.admin.testGetCertRequest;
import generated.zcsclient.admin.testGetCertResponse;
import generated.zcsclient.admin.testGetClusterStatusRequest;
import generated.zcsclient.admin.testGetClusterStatusResponse;
import generated.zcsclient.admin.testGetConfigRequest;
import generated.zcsclient.admin.testGetConfigResponse;
import generated.zcsclient.admin.testGetCosRequest;
import generated.zcsclient.admin.testGetCosResponse;
import generated.zcsclient.admin.testGetCreateObjectAttrsRequest;
import generated.zcsclient.admin.testGetCreateObjectAttrsResponse;
import generated.zcsclient.admin.testGetCurrentVolumesRequest;
import generated.zcsclient.admin.testGetCurrentVolumesResponse;
import generated.zcsclient.admin.testGetDataSourcesRequest;
import generated.zcsclient.admin.testGetDataSourcesResponse;
import generated.zcsclient.admin.testGetDelegatedAdminConstraintsRequest;
import generated.zcsclient.admin.testGetDelegatedAdminConstraintsResponse;
import generated.zcsclient.admin.testGetDeviceStatusRequest;
import generated.zcsclient.admin.testGetDeviceStatusResponse;
import generated.zcsclient.admin.testGetDevicesCountRequest;
import generated.zcsclient.admin.testGetDevicesCountResponse;
import generated.zcsclient.admin.testGetDevicesCountSinceLastUsedRequest;
import generated.zcsclient.admin.testGetDevicesCountSinceLastUsedResponse;
import generated.zcsclient.admin.testGetDevicesCountUsedTodayRequest;
import generated.zcsclient.admin.testGetDevicesCountUsedTodayResponse;
import generated.zcsclient.admin.testGetDevicesRequest;
import generated.zcsclient.admin.testGetDevicesResponse;
import generated.zcsclient.admin.testGetDistributionListMembershipRequest;
import generated.zcsclient.admin.testGetDistributionListMembershipResponse;
import generated.zcsclient.admin.testGetDistributionListRequest;
import generated.zcsclient.admin.testGetDistributionListResponse;
import generated.zcsclient.admin.testGetDomainInfoRequest;
import generated.zcsclient.admin.testGetDomainInfoResponse;
import generated.zcsclient.admin.testGetDomainRequest;
import generated.zcsclient.admin.testGetDomainResponse;
import generated.zcsclient.admin.testGetEffectiveRightsRequest;
import generated.zcsclient.admin.testGetEffectiveRightsResponse;
import generated.zcsclient.admin.testGetFreeBusyQueueInfoRequest;
import generated.zcsclient.admin.testGetFreeBusyQueueInfoResponse;
import generated.zcsclient.admin.testGetGrantsRequest;
import generated.zcsclient.admin.testGetGrantsResponse;
import generated.zcsclient.admin.testGetHsmStatusRequest;
import generated.zcsclient.admin.testGetHsmStatusResponse;
import generated.zcsclient.admin.testGetIndexStatsRequest;
import generated.zcsclient.admin.testGetIndexStatsResponse;
import generated.zcsclient.admin.testGetLDAPEntriesRequest;
import generated.zcsclient.admin.testGetLDAPEntriesResponse;
import generated.zcsclient.admin.testGetLicenseInfoRequest;
import generated.zcsclient.admin.testGetLicenseInfoResponse;
import generated.zcsclient.admin.testGetLicenseRequest;
import generated.zcsclient.admin.testGetLicenseResponse;
import generated.zcsclient.admin.testGetLoggerStatsRequest;
import generated.zcsclient.admin.testGetLoggerStatsResponse;
import generated.zcsclient.admin.testGetMailQueueInfoRequest;
import generated.zcsclient.admin.testGetMailQueueInfoResponse;
import generated.zcsclient.admin.testGetMailQueueRequest;
import generated.zcsclient.admin.testGetMailQueueResponse;
import generated.zcsclient.admin.testGetMailboxRequest;
import generated.zcsclient.admin.testGetMailboxResponse;
import generated.zcsclient.admin.testGetMailboxStatsRequest;
import generated.zcsclient.admin.testGetMailboxStatsResponse;
import generated.zcsclient.admin.testGetMailboxVersionRequest;
import generated.zcsclient.admin.testGetMailboxVersionResponse;
import generated.zcsclient.admin.testGetMailboxVolumesRequest;
import generated.zcsclient.admin.testGetMailboxVolumesResponse;
import generated.zcsclient.admin.testGetMemcachedClientConfigRequest;
import generated.zcsclient.admin.testGetMemcachedClientConfigResponse;
import generated.zcsclient.admin.testGetQuotaUsageRequest;
import generated.zcsclient.admin.testGetQuotaUsageResponse;
import generated.zcsclient.admin.testGetRightRequest;
import generated.zcsclient.admin.testGetRightResponse;
import generated.zcsclient.admin.testGetRightsDocRequest;
import generated.zcsclient.admin.testGetRightsDocResponse;
import generated.zcsclient.admin.testGetSMIMEConfigRequest;
import generated.zcsclient.admin.testGetSMIMEConfigResponse;
import generated.zcsclient.admin.testGetServerNIfsRequest;
import generated.zcsclient.admin.testGetServerNIfsResponse;
import generated.zcsclient.admin.testGetServerRequest;
import generated.zcsclient.admin.testGetServerResponse;
import generated.zcsclient.admin.testGetServerStatsRequest;
import generated.zcsclient.admin.testGetServerStatsResponse;
import generated.zcsclient.admin.testGetServiceStatusRequest;
import generated.zcsclient.admin.testGetServiceStatusResponse;
import generated.zcsclient.admin.testGetSessionsRequest;
import generated.zcsclient.admin.testGetSessionsResponse;
import generated.zcsclient.admin.testGetShareInfoRequest;
import generated.zcsclient.admin.testGetShareInfoResponse;
import generated.zcsclient.admin.testGetSystemRetentionPolicyRequest;
import generated.zcsclient.admin.testGetSystemRetentionPolicyResponse;
import generated.zcsclient.admin.testGetUCServiceRequest;
import generated.zcsclient.admin.testGetUCServiceResponse;
import generated.zcsclient.admin.testGetVersionInfoRequest;
import generated.zcsclient.admin.testGetVersionInfoResponse;
import generated.zcsclient.admin.testGetVolumeRequest;
import generated.zcsclient.admin.testGetVolumeResponse;
import generated.zcsclient.admin.testGetXMPPComponentRequest;
import generated.zcsclient.admin.testGetXMPPComponentResponse;
import generated.zcsclient.admin.testGetXMbxSearchesListRequest;
import generated.zcsclient.admin.testGetXMbxSearchesListResponse;
import generated.zcsclient.admin.testGetZimletRequest;
import generated.zcsclient.admin.testGetZimletResponse;
import generated.zcsclient.admin.testGetZimletStatusRequest;
import generated.zcsclient.admin.testGetZimletStatusResponse;
import generated.zcsclient.admin.testGrantRightRequest;
import generated.zcsclient.admin.testGrantRightResponse;
import generated.zcsclient.admin.testHsmRequest;
import generated.zcsclient.admin.testHsmResponse;
import generated.zcsclient.admin.testInstallCertRequest;
import generated.zcsclient.admin.testInstallCertResponse;
import generated.zcsclient.admin.testInstallLicenseRequest;
import generated.zcsclient.admin.testInstallLicenseResponse;
import generated.zcsclient.admin.testLockoutMailboxRequest;
import generated.zcsclient.admin.testLockoutMailboxResponse;
import generated.zcsclient.admin.testMailQueueActionRequest;
import generated.zcsclient.admin.testMailQueueActionResponse;
import generated.zcsclient.admin.testMailQueueFlushRequest;
import generated.zcsclient.admin.testMailQueueFlushResponse;
import generated.zcsclient.admin.testMigrateAccountRequest;
import generated.zcsclient.admin.testMigrateAccountResponse;
import generated.zcsclient.admin.testModifyAccountRequest;
import generated.zcsclient.admin.testModifyAccountResponse;
import generated.zcsclient.admin.testModifyAdminSavedSearchesRequest;
import generated.zcsclient.admin.testModifyAdminSavedSearchesResponse;
import generated.zcsclient.admin.testModifyAlwaysOnClusterRequest;
import generated.zcsclient.admin.testModifyAlwaysOnClusterResponse;
import generated.zcsclient.admin.testModifyCalendarResourceRequest;
import generated.zcsclient.admin.testModifyCalendarResourceResponse;
import generated.zcsclient.admin.testModifyConfigRequest;
import generated.zcsclient.admin.testModifyConfigResponse;
import generated.zcsclient.admin.testModifyCosRequest;
import generated.zcsclient.admin.testModifyCosResponse;
import generated.zcsclient.admin.testModifyDataSourceRequest;
import generated.zcsclient.admin.testModifyDataSourceResponse;
import generated.zcsclient.admin.testModifyDelegatedAdminConstraintsRequest;
import generated.zcsclient.admin.testModifyDelegatedAdminConstraintsResponse;
import generated.zcsclient.admin.testModifyDistributionListRequest;
import generated.zcsclient.admin.testModifyDistributionListResponse;
import generated.zcsclient.admin.testModifyDomainRequest;
import generated.zcsclient.admin.testModifyDomainResponse;
import generated.zcsclient.admin.testModifyLDAPEntryRequest;
import generated.zcsclient.admin.testModifyLDAPEntryResponse;
import generated.zcsclient.admin.testModifySMIMEConfigRequest;
import generated.zcsclient.admin.testModifySMIMEConfigResponse;
import generated.zcsclient.admin.testModifyServerRequest;
import generated.zcsclient.admin.testModifyServerResponse;
import generated.zcsclient.admin.testModifySystemRetentionPolicyRequest;
import generated.zcsclient.admin.testModifySystemRetentionPolicyResponse;
import generated.zcsclient.admin.testModifyUCServiceRequest;
import generated.zcsclient.admin.testModifyUCServiceResponse;
import generated.zcsclient.admin.testModifyVolumeRequest;
import generated.zcsclient.admin.testModifyVolumeResponse;
import generated.zcsclient.admin.testModifyZimletRequest;
import generated.zcsclient.admin.testModifyZimletResponse;
import generated.zcsclient.admin.testMoveBlobsRequest;
import generated.zcsclient.admin.testMoveBlobsResponse;
import generated.zcsclient.admin.testMoveMailboxRequest;
import generated.zcsclient.admin.testMoveMailboxResponse;
import generated.zcsclient.admin.testNoOpRequest;
import generated.zcsclient.admin.testNoOpResponse;
import generated.zcsclient.admin.testPingRequest;
import generated.zcsclient.admin.testPingResponse;
import generated.zcsclient.admin.testPurgeAccountCalendarCacheRequest;
import generated.zcsclient.admin.testPurgeAccountCalendarCacheResponse;
import generated.zcsclient.admin.testPurgeFreeBusyQueueRequest;
import generated.zcsclient.admin.testPurgeFreeBusyQueueResponse;
import generated.zcsclient.admin.testPurgeMessagesRequest;
import generated.zcsclient.admin.testPurgeMessagesResponse;
import generated.zcsclient.admin.testPurgeMovedMailboxRequest;
import generated.zcsclient.admin.testPurgeMovedMailboxResponse;
import generated.zcsclient.admin.testPushFreeBusyRequest;
import generated.zcsclient.admin.testPushFreeBusyResponse;
import generated.zcsclient.admin.testQueryMailboxMoveRequest;
import generated.zcsclient.admin.testQueryMailboxMoveResponse;
import generated.zcsclient.admin.testQueryWaitSetRequest;
import generated.zcsclient.admin.testQueryWaitSetResponse;
import generated.zcsclient.admin.testReIndexRequest;
import generated.zcsclient.admin.testReIndexResponse;
import generated.zcsclient.admin.testRecalculateMailboxCountsRequest;
import generated.zcsclient.admin.testRecalculateMailboxCountsResponse;
import generated.zcsclient.admin.testRefreshRegisteredAuthTokensRequest;
import generated.zcsclient.admin.testRefreshRegisteredAuthTokensResponse;
import generated.zcsclient.admin.testRegisterMailboxMoveOutRequest;
import generated.zcsclient.admin.testRegisterMailboxMoveOutResponse;
import generated.zcsclient.admin.testReloadAccountRequest;
import generated.zcsclient.admin.testReloadAccountResponse;
import generated.zcsclient.admin.testReloadLocalConfigRequest;
import generated.zcsclient.admin.testReloadLocalConfigResponse;
import generated.zcsclient.admin.testReloadMemcachedClientConfigRequest;
import generated.zcsclient.admin.testReloadMemcachedClientConfigResponse;
import generated.zcsclient.admin.testRemoteWipeRequest;
import generated.zcsclient.admin.testRemoteWipeResponse;
import generated.zcsclient.admin.testRemoveAccountAliasRequest;
import generated.zcsclient.admin.testRemoveAccountAliasResponse;
import generated.zcsclient.admin.testRemoveAccountLoggerRequest;
import generated.zcsclient.admin.testRemoveAccountLoggerResponse;
import generated.zcsclient.admin.testRemoveDeviceRequest;
import generated.zcsclient.admin.testRemoveDeviceResponse;
import generated.zcsclient.admin.testRemoveDistributionListAliasRequest;
import generated.zcsclient.admin.testRemoveDistributionListAliasResponse;
import generated.zcsclient.admin.testRemoveDistributionListMemberRequest;
import generated.zcsclient.admin.testRemoveDistributionListMemberResponse;
import generated.zcsclient.admin.testRenameAccountRequest;
import generated.zcsclient.admin.testRenameAccountResponse;
import generated.zcsclient.admin.testRenameCalendarResourceRequest;
import generated.zcsclient.admin.testRenameCalendarResourceResponse;
import generated.zcsclient.admin.testRenameCosRequest;
import generated.zcsclient.admin.testRenameCosResponse;
import generated.zcsclient.admin.testRenameDistributionListRequest;
import generated.zcsclient.admin.testRenameDistributionListResponse;
import generated.zcsclient.admin.testRenameLDAPEntryRequest;
import generated.zcsclient.admin.testRenameLDAPEntryResponse;
import generated.zcsclient.admin.testRenameUCServiceRequest;
import generated.zcsclient.admin.testRenameUCServiceResponse;
import generated.zcsclient.admin.testResetAllLoggersRequest;
import generated.zcsclient.admin.testResetAllLoggersResponse;
import generated.zcsclient.admin.testRestoreRequest;
import generated.zcsclient.admin.testRestoreResponse;
import generated.zcsclient.admin.testResumeDeviceRequest;
import generated.zcsclient.admin.testResumeDeviceResponse;
import generated.zcsclient.admin.testRevokeRightRequest;
import generated.zcsclient.admin.testRevokeRightResponse;
import generated.zcsclient.admin.testRolloverRedoLogRequest;
import generated.zcsclient.admin.testRolloverRedoLogResponse;
import generated.zcsclient.admin.testRunUnitTestsRequest;
import generated.zcsclient.admin.testRunUnitTestsResponse;
import generated.zcsclient.admin.testScheduleBackupsRequest;
import generated.zcsclient.admin.testScheduleBackupsResponse;
import generated.zcsclient.admin.testSearchAccountsRequest;
import generated.zcsclient.admin.testSearchAccountsResponse;
import generated.zcsclient.admin.testSearchAutoProvDirectoryRequest;
import generated.zcsclient.admin.testSearchAutoProvDirectoryResponse;
import generated.zcsclient.admin.testSearchCalendarResourcesRequest;
import generated.zcsclient.admin.testSearchCalendarResourcesResponse;
import generated.zcsclient.admin.testSearchDirectoryRequest;
import generated.zcsclient.admin.testSearchDirectoryResponse;
import generated.zcsclient.admin.testSearchGalRequest;
import generated.zcsclient.admin.testSearchGalResponse;
import generated.zcsclient.admin.testSearchMultiMailboxRequest;
import generated.zcsclient.admin.testSearchMultiMailboxResponse;
import generated.zcsclient.admin.testSetCurrentVolumeRequest;
import generated.zcsclient.admin.testSetCurrentVolumeResponse;
import generated.zcsclient.admin.testSetLocalServerOnlineRequest;
import generated.zcsclient.admin.testSetLocalServerOnlineResponse;
import generated.zcsclient.admin.testSetPasswordRequest;
import generated.zcsclient.admin.testSetPasswordResponse;
import generated.zcsclient.admin.testSetServerOfflineRequest;
import generated.zcsclient.admin.testSetServerOfflineResponse;
import generated.zcsclient.admin.testSuspendDeviceRequest;
import generated.zcsclient.admin.testSuspendDeviceResponse;
import generated.zcsclient.admin.testSyncGalAccountRequest;
import generated.zcsclient.admin.testSyncGalAccountResponse;
import generated.zcsclient.admin.testUndeployZimletRequest;
import generated.zcsclient.admin.testUndeployZimletResponse;
import generated.zcsclient.admin.testUnloadMailboxRequest;
import generated.zcsclient.admin.testUnloadMailboxResponse;
import generated.zcsclient.admin.testUnregisterMailboxMoveOutRequest;
import generated.zcsclient.admin.testUnregisterMailboxMoveOutResponse;
import generated.zcsclient.admin.testUpdateDeviceStatusRequest;
import generated.zcsclient.admin.testUpdateDeviceStatusResponse;
import generated.zcsclient.admin.testUpdatePresenceSessionIdRequest;
import generated.zcsclient.admin.testUpdatePresenceSessionIdResponse;
import generated.zcsclient.admin.testUploadDomCertRequest;
import generated.zcsclient.admin.testUploadDomCertResponse;
import generated.zcsclient.admin.testUploadProxyCARequest;
import generated.zcsclient.admin.testUploadProxyCAResponse;
import generated.zcsclient.admin.testVerifyCertKeyRequest;
import generated.zcsclient.admin.testVerifyCertKeyResponse;
import generated.zcsclient.admin.testVerifyIndexRequest;
import generated.zcsclient.admin.testVerifyIndexResponse;
import generated.zcsclient.admin.testVerifyStoreManagerRequest;
import generated.zcsclient.admin.testVerifyStoreManagerResponse;
import generated.zcsclient.admin.testVersionCheckRequest;
import generated.zcsclient.admin.testVersionCheckResponse;
import generated.zcsclient.adminext.testBulkIMAPDataImportRequest;
import generated.zcsclient.adminext.testBulkIMAPDataImportResponse;
import generated.zcsclient.adminext.testBulkImportAccountsRequest;
import generated.zcsclient.adminext.testBulkImportAccountsResponse;
import generated.zcsclient.adminext.testGenerateBulkProvisionFileFromLDAPRequest;
import generated.zcsclient.adminext.testGenerateBulkProvisionFileFromLDAPResponse;
import generated.zcsclient.adminext.testGetBulkIMAPImportTaskListRequest;
import generated.zcsclient.adminext.testGetBulkIMAPImportTaskListResponse;
import generated.zcsclient.adminext.testPurgeBulkIMAPImportTasksRequest;
import generated.zcsclient.adminext.testPurgeBulkIMAPImportTasksResponse;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.7-hudson-48-
 * Generated source version: 2.1
 * 
 */
@WebService(name = "zcsAdminPortType", targetNamespace = "http://www.zimbra.com/wsdl/ZimbraService.wsdl")
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
public interface ZcsAdminPortType {


    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAbortHsmResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AbortHsm")
    @WebResult(name = "AbortHsmResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAbortHsmResponse abortHsmRequest(
        @WebParam(name = "AbortHsmRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAbortHsmRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAbortXMbxSearchResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AbortXMbxSearch")
    @WebResult(name = "AbortXMbxSearchResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAbortXMbxSearchResponse abortXMbxSearchRequest(
        @WebParam(name = "AbortXMbxSearchRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAbortXMbxSearchRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testActivateLicenseResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ActivateLicense")
    @WebResult(name = "ActivateLicenseResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testActivateLicenseResponse activateLicenseRequest(
        @WebParam(name = "ActivateLicenseRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testActivateLicenseRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAddAccountAliasResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AddAccountAlias")
    @WebResult(name = "AddAccountAliasResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAddAccountAliasResponse addAccountAliasRequest(
        @WebParam(name = "AddAccountAliasRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAddAccountAliasRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAddAccountLoggerResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AddAccountLogger")
    @WebResult(name = "AddAccountLoggerResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAddAccountLoggerResponse addAccountLoggerRequest(
        @WebParam(name = "AddAccountLoggerRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAddAccountLoggerRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAddDistributionListAliasResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AddDistributionListAlias")
    @WebResult(name = "AddDistributionListAliasResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAddDistributionListAliasResponse addDistributionListAliasRequest(
        @WebParam(name = "AddDistributionListAliasRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAddDistributionListAliasRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAddDistributionListMemberResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AddDistributionListMember")
    @WebResult(name = "AddDistributionListMemberResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAddDistributionListMemberResponse addDistributionListMemberRequest(
        @WebParam(name = "AddDistributionListMemberRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAddDistributionListMemberRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAddGalSyncDataSourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AddGalSyncDataSource")
    @WebResult(name = "AddGalSyncDataSourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAddGalSyncDataSourceResponse addGalSyncDataSourceRequest(
        @WebParam(name = "AddGalSyncDataSourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAddGalSyncDataSourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAdminCreateWaitSetResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AdminCreateWaitSet")
    @WebResult(name = "AdminCreateWaitSetResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAdminCreateWaitSetResponse adminCreateWaitSetRequest(
        @WebParam(name = "AdminCreateWaitSetRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAdminCreateWaitSetRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAdminDestroyWaitSetResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AdminDestroyWaitSet")
    @WebResult(name = "AdminDestroyWaitSetResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAdminDestroyWaitSetResponse adminDestroyWaitSetRequest(
        @WebParam(name = "AdminDestroyWaitSetRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAdminDestroyWaitSetRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAdminWaitSetResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AdminWaitSet")
    @WebResult(name = "AdminWaitSetResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAdminWaitSetResponse adminWaitSetRequest(
        @WebParam(name = "AdminWaitSetRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAdminWaitSetRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAuthResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/Auth")
    @WebResult(name = "AuthResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAuthResponse authRequest(
        @WebParam(name = "AuthRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAuthRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAutoCompleteGalResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AutoCompleteGal")
    @WebResult(name = "AutoCompleteGalResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAutoCompleteGalResponse autoCompleteGalRequest(
        @WebParam(name = "AutoCompleteGalRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAutoCompleteGalRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAutoProvAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AutoProvAccount")
    @WebResult(name = "AutoProvAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAutoProvAccountResponse autoProvAccountRequest(
        @WebParam(name = "AutoProvAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAutoProvAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testAutoProvTaskControlResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/AutoProvTaskControl")
    @WebResult(name = "AutoProvTaskControlResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testAutoProvTaskControlResponse autoProvTaskControlRequest(
        @WebParam(name = "AutoProvTaskControlRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testAutoProvTaskControlRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testBackupAccountQueryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/BackupAccountQuery")
    @WebResult(name = "BackupAccountQueryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testBackupAccountQueryResponse backupAccountQueryRequest(
        @WebParam(name = "BackupAccountQueryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testBackupAccountQueryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testBackupQueryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/BackupQuery")
    @WebResult(name = "BackupQueryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testBackupQueryResponse backupQueryRequest(
        @WebParam(name = "BackupQueryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testBackupQueryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testBackupResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/Backup")
    @WebResult(name = "BackupResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testBackupResponse backupRequest(
        @WebParam(name = "BackupRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testBackupRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCancelPendingRemoteWipeResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CancelPendingRemoteWipe")
    @WebResult(name = "CancelPendingRemoteWipeResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCancelPendingRemoteWipeResponse cancelPendingRemoteWipeRequest(
        @WebParam(name = "CancelPendingRemoteWipeRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCancelPendingRemoteWipeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckAuthConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckAuthConfig")
    @WebResult(name = "CheckAuthConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckAuthConfigResponse checkAuthConfigRequest(
        @WebParam(name = "CheckAuthConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckAuthConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckBlobConsistencyResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckBlobConsistency")
    @WebResult(name = "CheckBlobConsistencyResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckBlobConsistencyResponse checkBlobConsistencyRequest(
        @WebParam(name = "CheckBlobConsistencyRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckBlobConsistencyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckDirectoryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckDirectory")
    @WebResult(name = "CheckDirectoryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckDirectoryResponse checkDirectoryRequest(
        @WebParam(name = "CheckDirectoryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckDirectoryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckDomainMXRecordResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckDomainMXRecord")
    @WebResult(name = "CheckDomainMXRecordResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckDomainMXRecordResponse checkDomainMXRecordRequest(
        @WebParam(name = "CheckDomainMXRecordRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckDomainMXRecordRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckExchangeAuthResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckExchangeAuth")
    @WebResult(name = "CheckExchangeAuthResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckExchangeAuthResponse checkExchangeAuthRequest(
        @WebParam(name = "CheckExchangeAuthRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckExchangeAuthRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckGalConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckGalConfig")
    @WebResult(name = "CheckGalConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckGalConfigResponse checkGalConfigRequest(
        @WebParam(name = "CheckGalConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckGalConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckHealthResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckHealth")
    @WebResult(name = "CheckHealthResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckHealthResponse checkHealthRequest(
        @WebParam(name = "CheckHealthRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckHealthRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckHostnameResolveResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckHostnameResolve")
    @WebResult(name = "CheckHostnameResolveResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckHostnameResolveResponse checkHostnameResolveRequest(
        @WebParam(name = "CheckHostnameResolveRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckHostnameResolveRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckPasswordStrengthResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckPasswordStrength")
    @WebResult(name = "CheckPasswordStrengthResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckPasswordStrengthResponse checkPasswordStrengthRequest(
        @WebParam(name = "CheckPasswordStrengthRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckPasswordStrengthRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCheckRightResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CheckRight")
    @WebResult(name = "CheckRightResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCheckRightResponse checkRightRequest(
        @WebParam(name = "CheckRightRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCheckRightRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testClearCookieResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ClearCookie")
    @WebResult(name = "ClearCookieResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testClearCookieResponse clearCookieRequest(
        @WebParam(name = "ClearCookieRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testClearCookieRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCompactIndexResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CompactIndex")
    @WebResult(name = "CompactIndexResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCompactIndexResponse compactIndexRequest(
        @WebParam(name = "CompactIndexRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCompactIndexRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testComputeAggregateQuotaUsageResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ComputeAggregateQuotaUsage")
    @WebResult(name = "ComputeAggregateQuotaUsageResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testComputeAggregateQuotaUsageResponse computeAggregateQuotaUsageRequest(
        @WebParam(name = "ComputeAggregateQuotaUsageRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testComputeAggregateQuotaUsageRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testConfigureZimletResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ConfigureZimlet")
    @WebResult(name = "ConfigureZimletResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testConfigureZimletResponse configureZimletRequest(
        @WebParam(name = "ConfigureZimletRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testConfigureZimletRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCopyCosResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CopyCos")
    @WebResult(name = "CopyCosResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCopyCosResponse copyCosRequest(
        @WebParam(name = "CopyCosRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCopyCosRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCountAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CountAccount")
    @WebResult(name = "CountAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCountAccountResponse countAccountRequest(
        @WebParam(name = "CountAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCountAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCountObjectsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CountObjects")
    @WebResult(name = "CountObjectsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCountObjectsResponse countObjectsRequest(
        @WebParam(name = "CountObjectsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCountObjectsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateAccount")
    @WebResult(name = "CreateAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateAccountResponse createAccountRequest(
        @WebParam(name = "CreateAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateAlwaysOnClusterResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateAlwaysOnCluster")
    @WebResult(name = "CreateAlwaysOnClusterResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateAlwaysOnClusterResponse createAlwaysOnClusterRequest(
        @WebParam(name = "CreateAlwaysOnClusterRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateAlwaysOnClusterRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateArchiveResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateArchive")
    @WebResult(name = "CreateArchiveResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateArchiveResponse createArchiveRequest(
        @WebParam(name = "CreateArchiveRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateArchiveRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateCalendarResourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateCalendarResource")
    @WebResult(name = "CreateCalendarResourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateCalendarResourceResponse createCalendarResourceRequest(
        @WebParam(name = "CreateCalendarResourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateCalendarResourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateCosResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateCos")
    @WebResult(name = "CreateCosResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateCosResponse createCosRequest(
        @WebParam(name = "CreateCosRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateCosRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateDataSourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateDataSource")
    @WebResult(name = "CreateDataSourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateDataSourceResponse createDataSourceRequest(
        @WebParam(name = "CreateDataSourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateDataSourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateDistributionListResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateDistributionList")
    @WebResult(name = "CreateDistributionListResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateDistributionListResponse createDistributionListRequest(
        @WebParam(name = "CreateDistributionListRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateDistributionListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateDomainResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateDomain")
    @WebResult(name = "CreateDomainResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateDomainResponse createDomainRequest(
        @WebParam(name = "CreateDomainRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateDomainRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateGalSyncAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateGalSyncAccount")
    @WebResult(name = "CreateGalSyncAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateGalSyncAccountResponse createGalSyncAccountRequest(
        @WebParam(name = "CreateGalSyncAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateGalSyncAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateLDAPEntryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateLDAPEntry")
    @WebResult(name = "CreateLDAPEntryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateLDAPEntryResponse createLDAPEntryRequest(
        @WebParam(name = "CreateLDAPEntryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateLDAPEntryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateServerResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateServer")
    @WebResult(name = "CreateServerResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateServerResponse createServerRequest(
        @WebParam(name = "CreateServerRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateServerRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateSystemRetentionPolicyResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateSystemRetentionPolicy")
    @WebResult(name = "CreateSystemRetentionPolicyResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateSystemRetentionPolicyResponse createSystemRetentionPolicyRequest(
        @WebParam(name = "CreateSystemRetentionPolicyRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateSystemRetentionPolicyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateUCServiceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateUCService")
    @WebResult(name = "CreateUCServiceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateUCServiceResponse createUCServiceRequest(
        @WebParam(name = "CreateUCServiceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateUCServiceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateVolumeResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateVolume")
    @WebResult(name = "CreateVolumeResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateVolumeResponse createVolumeRequest(
        @WebParam(name = "CreateVolumeRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateVolumeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateXMPPComponentResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateXMPPComponent")
    @WebResult(name = "CreateXMPPComponentResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateXMPPComponentResponse createXMPPComponentRequest(
        @WebParam(name = "CreateXMPPComponentRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateXMPPComponentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateXMbxSearchResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateXMbxSearch")
    @WebResult(name = "CreateXMbxSearchResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateXMbxSearchResponse createXMbxSearchRequest(
        @WebParam(name = "CreateXMbxSearchRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateXMbxSearchRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testCreateZimletResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/CreateZimlet")
    @WebResult(name = "CreateZimletResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testCreateZimletResponse createZimletRequest(
        @WebParam(name = "CreateZimletRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testCreateZimletRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDedupeBlobsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DedupeBlobs")
    @WebResult(name = "DedupeBlobsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDedupeBlobsResponse dedupeBlobsRequest(
        @WebParam(name = "DedupeBlobsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDedupeBlobsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDelegateAuthResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DelegateAuth")
    @WebResult(name = "DelegateAuthResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDelegateAuthResponse delegateAuthRequest(
        @WebParam(name = "DelegateAuthRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDelegateAuthRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteAccount")
    @WebResult(name = "DeleteAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteAccountResponse deleteAccountRequest(
        @WebParam(name = "DeleteAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteAlwaysOnClusterResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteAlwaysOnCluster")
    @WebResult(name = "DeleteAlwaysOnClusterResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteAlwaysOnClusterResponse deleteAlwaysOnClusterRequest(
        @WebParam(name = "DeleteAlwaysOnClusterRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteAlwaysOnClusterRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteCalendarResourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteCalendarResource")
    @WebResult(name = "DeleteCalendarResourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteCalendarResourceResponse deleteCalendarResourceRequest(
        @WebParam(name = "DeleteCalendarResourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteCalendarResourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteCosResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteCos")
    @WebResult(name = "DeleteCosResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteCosResponse deleteCosRequest(
        @WebParam(name = "DeleteCosRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteCosRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteDataSourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteDataSource")
    @WebResult(name = "DeleteDataSourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteDataSourceResponse deleteDataSourceRequest(
        @WebParam(name = "DeleteDataSourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteDataSourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteDistributionListResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteDistributionList")
    @WebResult(name = "DeleteDistributionListResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteDistributionListResponse deleteDistributionListRequest(
        @WebParam(name = "DeleteDistributionListRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteDistributionListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteDomainResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteDomain")
    @WebResult(name = "DeleteDomainResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteDomainResponse deleteDomainRequest(
        @WebParam(name = "DeleteDomainRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteDomainRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteGalSyncAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteGalSyncAccount")
    @WebResult(name = "DeleteGalSyncAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteGalSyncAccountResponse deleteGalSyncAccountRequest(
        @WebParam(name = "DeleteGalSyncAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteGalSyncAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteLDAPEntryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteLDAPEntry")
    @WebResult(name = "DeleteLDAPEntryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteLDAPEntryResponse deleteLDAPEntryRequest(
        @WebParam(name = "DeleteLDAPEntryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteLDAPEntryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteMailboxResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteMailbox")
    @WebResult(name = "DeleteMailboxResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteMailboxResponse deleteMailboxRequest(
        @WebParam(name = "DeleteMailboxRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteMailboxRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteServerResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteServer")
    @WebResult(name = "DeleteServerResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteServerResponse deleteServerRequest(
        @WebParam(name = "DeleteServerRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteServerRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteSystemRetentionPolicyResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteSystemRetentionPolicy")
    @WebResult(name = "DeleteSystemRetentionPolicyResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteSystemRetentionPolicyResponse deleteSystemRetentionPolicyRequest(
        @WebParam(name = "DeleteSystemRetentionPolicyRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteSystemRetentionPolicyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteUCServiceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteUCService")
    @WebResult(name = "DeleteUCServiceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteUCServiceResponse deleteUCServiceRequest(
        @WebParam(name = "DeleteUCServiceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteUCServiceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteVolumeResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteVolume")
    @WebResult(name = "DeleteVolumeResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteVolumeResponse deleteVolumeRequest(
        @WebParam(name = "DeleteVolumeRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteVolumeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteXMPPComponentResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteXMPPComponent")
    @WebResult(name = "DeleteXMPPComponentResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteXMPPComponentResponse deleteXMPPComponentRequest(
        @WebParam(name = "DeleteXMPPComponentRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteXMPPComponentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteXMbxSearchResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteXMbxSearch")
    @WebResult(name = "DeleteXMbxSearchResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteXMbxSearchResponse deleteXMbxSearchRequest(
        @WebParam(name = "DeleteXMbxSearchRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteXMbxSearchRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeleteZimletResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeleteZimlet")
    @WebResult(name = "DeleteZimletResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeleteZimletResponse deleteZimletRequest(
        @WebParam(name = "DeleteZimletRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeleteZimletRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDeployZimletResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DeployZimlet")
    @WebResult(name = "DeployZimletResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDeployZimletResponse deployZimletRequest(
        @WebParam(name = "DeployZimletRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDeployZimletRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDisableArchiveResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DisableArchive")
    @WebResult(name = "DisableArchiveResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDisableArchiveResponse disableArchiveRequest(
        @WebParam(name = "DisableArchiveRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDisableArchiveRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testDumpSessionsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/DumpSessions")
    @WebResult(name = "DumpSessionsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testDumpSessionsResponse dumpSessionsRequest(
        @WebParam(name = "DumpSessionsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testDumpSessionsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testEnableArchiveResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/EnableArchive")
    @WebResult(name = "EnableArchiveResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testEnableArchiveResponse enableArchiveRequest(
        @WebParam(name = "EnableArchiveRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testEnableArchiveRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testExportAndDeleteItemsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ExportAndDeleteItems")
    @WebResult(name = "ExportAndDeleteItemsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testExportAndDeleteItemsResponse exportAndDeleteItemsRequest(
        @WebParam(name = "ExportAndDeleteItemsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testExportAndDeleteItemsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testExportMailboxResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ExportMailbox")
    @WebResult(name = "ExportMailboxResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testExportMailboxResponse exportMailboxRequest(
        @WebParam(name = "ExportMailboxRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testExportMailboxRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testFailoverClusterServiceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/FailoverClusterService")
    @WebResult(name = "FailoverClusterServiceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testFailoverClusterServiceResponse failoverClusterServiceRequest(
        @WebParam(name = "FailoverClusterServiceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testFailoverClusterServiceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testFixCalendarEndTimeResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/FixCalendarEndTime")
    @WebResult(name = "FixCalendarEndTimeResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testFixCalendarEndTimeResponse fixCalendarEndTimeRequest(
        @WebParam(name = "FixCalendarEndTimeRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testFixCalendarEndTimeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testFixCalendarPriorityResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/FixCalendarPriority")
    @WebResult(name = "FixCalendarPriorityResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testFixCalendarPriorityResponse fixCalendarPriorityRequest(
        @WebParam(name = "FixCalendarPriorityRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testFixCalendarPriorityRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testFixCalendarTZResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/FixCalendarTZ")
    @WebResult(name = "FixCalendarTZResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testFixCalendarTZResponse fixCalendarTZRequest(
        @WebParam(name = "FixCalendarTZRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testFixCalendarTZRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testFlushCacheResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/FlushCache")
    @WebResult(name = "FlushCacheResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testFlushCacheResponse flushCacheRequest(
        @WebParam(name = "FlushCacheRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testFlushCacheRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGenCSRResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GenCSR")
    @WebResult(name = "GenCSRResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGenCSRResponse genCSRRequest(
        @WebParam(name = "GenCSRRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGenCSRRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAccountInfoResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAccountInfo")
    @WebResult(name = "GetAccountInfoResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAccountInfoResponse getAccountInfoRequest(
        @WebParam(name = "GetAccountInfoRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAccountInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAccountLoggersResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAccountLoggers")
    @WebResult(name = "GetAccountLoggersResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAccountLoggersResponse getAccountLoggersRequest(
        @WebParam(name = "GetAccountLoggersRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAccountLoggersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAccountMembershipResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAccountMembership")
    @WebResult(name = "GetAccountMembershipResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAccountMembershipResponse getAccountMembershipRequest(
        @WebParam(name = "GetAccountMembershipRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAccountMembershipRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAccount")
    @WebResult(name = "GetAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAccountResponse getAccountRequest(
        @WebParam(name = "GetAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAdminConsoleUICompResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAdminConsoleUIComp")
    @WebResult(name = "GetAdminConsoleUICompResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAdminConsoleUICompResponse getAdminConsoleUICompRequest(
        @WebParam(name = "GetAdminConsoleUICompRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAdminConsoleUICompRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAdminExtensionZimletsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAdminExtensionZimlets")
    @WebResult(name = "GetAdminExtensionZimletsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAdminExtensionZimletsResponse getAdminExtensionZimletsRequest(
        @WebParam(name = "GetAdminExtensionZimletsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAdminExtensionZimletsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAdminSavedSearchesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAdminSavedSearches")
    @WebResult(name = "GetAdminSavedSearchesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAdminSavedSearchesResponse getAdminSavedSearchesRequest(
        @WebParam(name = "GetAdminSavedSearchesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAdminSavedSearchesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAggregateQuotaUsageOnServerResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAggregateQuotaUsageOnServer")
    @WebResult(name = "GetAggregateQuotaUsageOnServerResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAggregateQuotaUsageOnServerResponse getAggregateQuotaUsageOnServerRequest(
        @WebParam(name = "GetAggregateQuotaUsageOnServerRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAggregateQuotaUsageOnServerRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllAccountLoggersResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllAccountLoggers")
    @WebResult(name = "GetAllAccountLoggersResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllAccountLoggersResponse getAllAccountLoggersRequest(
        @WebParam(name = "GetAllAccountLoggersRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllAccountLoggersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllAccountsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllAccounts")
    @WebResult(name = "GetAllAccountsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllAccountsResponse getAllAccountsRequest(
        @WebParam(name = "GetAllAccountsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllAccountsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllActiveServersResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllActiveServers")
    @WebResult(name = "GetAllActiveServersResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllActiveServersResponse getAllActiveServersRequest(
        @WebParam(name = "GetAllActiveServersRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllActiveServersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllAdminAccountsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllAdminAccounts")
    @WebResult(name = "GetAllAdminAccountsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllAdminAccountsResponse getAllAdminAccountsRequest(
        @WebParam(name = "GetAllAdminAccountsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllAdminAccountsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllAlwaysOnClustersResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllAlwaysOnClusters")
    @WebResult(name = "GetAllAlwaysOnClustersResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllAlwaysOnClustersResponse getAllAlwaysOnClustersRequest(
        @WebParam(name = "GetAllAlwaysOnClustersRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllAlwaysOnClustersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllCalendarResourcesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllCalendarResources")
    @WebResult(name = "GetAllCalendarResourcesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllCalendarResourcesResponse getAllCalendarResourcesRequest(
        @WebParam(name = "GetAllCalendarResourcesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllCalendarResourcesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllConfig")
    @WebResult(name = "GetAllConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllConfigResponse getAllConfigRequest(
        @WebParam(name = "GetAllConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllCosResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllCos")
    @WebResult(name = "GetAllCosResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllCosResponse getAllCosRequest(
        @WebParam(name = "GetAllCosRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllCosRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllDistributionListsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllDistributionLists")
    @WebResult(name = "GetAllDistributionListsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllDistributionListsResponse getAllDistributionListsRequest(
        @WebParam(name = "GetAllDistributionListsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllDistributionListsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllDomainsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllDomains")
    @WebResult(name = "GetAllDomainsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllDomainsResponse getAllDomainsRequest(
        @WebParam(name = "GetAllDomainsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllDomainsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllEffectiveRightsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllEffectiveRights")
    @WebResult(name = "GetAllEffectiveRightsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllEffectiveRightsResponse getAllEffectiveRightsRequest(
        @WebParam(name = "GetAllEffectiveRightsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllEffectiveRightsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllFreeBusyProvidersResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllFreeBusyProviders")
    @WebResult(name = "GetAllFreeBusyProvidersResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllFreeBusyProvidersResponse getAllFreeBusyProvidersRequest(
        @WebParam(name = "GetAllFreeBusyProvidersRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllFreeBusyProvidersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllLocalesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllLocales")
    @WebResult(name = "GetAllLocalesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllLocalesResponse getAllLocalesRequest(
        @WebParam(name = "GetAllLocalesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllLocalesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllMailboxesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllMailboxes")
    @WebResult(name = "GetAllMailboxesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllMailboxesResponse getAllMailboxesRequest(
        @WebParam(name = "GetAllMailboxesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllMailboxesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllRightsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllRights")
    @WebResult(name = "GetAllRightsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllRightsResponse getAllRightsRequest(
        @WebParam(name = "GetAllRightsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllRightsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllServersResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllServers")
    @WebResult(name = "GetAllServersResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllServersResponse getAllServersRequest(
        @WebParam(name = "GetAllServersRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllServersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllSkinsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllSkins")
    @WebResult(name = "GetAllSkinsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllSkinsResponse getAllSkinsRequest(
        @WebParam(name = "GetAllSkinsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllSkinsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllUCProvidersResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllUCProviders")
    @WebResult(name = "GetAllUCProvidersResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllUCProvidersResponse getAllUCProvidersRequest(
        @WebParam(name = "GetAllUCProvidersRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllUCProvidersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllUCServicesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllUCServices")
    @WebResult(name = "GetAllUCServicesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllUCServicesResponse getAllUCServicesRequest(
        @WebParam(name = "GetAllUCServicesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllUCServicesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllVolumesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllVolumes")
    @WebResult(name = "GetAllVolumesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllVolumesResponse getAllVolumesRequest(
        @WebParam(name = "GetAllVolumesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllVolumesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllXMPPComponentsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllXMPPComponents")
    @WebResult(name = "GetAllXMPPComponentsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllXMPPComponentsResponse getAllXMPPComponentsRequest(
        @WebParam(name = "GetAllXMPPComponentsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllXMPPComponentsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAllZimletsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAllZimlets")
    @WebResult(name = "GetAllZimletsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAllZimletsResponse getAllZimletsRequest(
        @WebParam(name = "GetAllZimletsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAllZimletsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAlwaysOnClusterResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAlwaysOnCluster")
    @WebResult(name = "GetAlwaysOnClusterResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAlwaysOnClusterResponse getAlwaysOnClusterRequest(
        @WebParam(name = "GetAlwaysOnClusterRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAlwaysOnClusterRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetApplianceHSMFSResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetApplianceHSMFS")
    @WebResult(name = "GetApplianceHSMFSResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetApplianceHSMFSResponse getApplianceHSMFSRequest(
        @WebParam(name = "GetApplianceHSMFSRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetApplianceHSMFSRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetAttributeInfoResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetAttributeInfo")
    @WebResult(name = "GetAttributeInfoResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetAttributeInfoResponse getAttributeInfoRequest(
        @WebParam(name = "GetAttributeInfoRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetAttributeInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetCSRResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetCSR")
    @WebResult(name = "GetCSRResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetCSRResponse getCSRRequest(
        @WebParam(name = "GetCSRRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetCSRRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetCalendarResourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetCalendarResource")
    @WebResult(name = "GetCalendarResourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetCalendarResourceResponse getCalendarResourceRequest(
        @WebParam(name = "GetCalendarResourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetCalendarResourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetCertResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetCert")
    @WebResult(name = "GetCertResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetCertResponse getCertRequest(
        @WebParam(name = "GetCertRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetCertRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetClusterStatusResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetClusterStatus")
    @WebResult(name = "GetClusterStatusResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetClusterStatusResponse getClusterStatusRequest(
        @WebParam(name = "GetClusterStatusRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetClusterStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetConfig")
    @WebResult(name = "GetConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetConfigResponse getConfigRequest(
        @WebParam(name = "GetConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetCosResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetCos")
    @WebResult(name = "GetCosResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetCosResponse getCosRequest(
        @WebParam(name = "GetCosRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetCosRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetCreateObjectAttrsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetCreateObjectAttrs")
    @WebResult(name = "GetCreateObjectAttrsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetCreateObjectAttrsResponse getCreateObjectAttrsRequest(
        @WebParam(name = "GetCreateObjectAttrsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetCreateObjectAttrsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetCurrentVolumesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetCurrentVolumes")
    @WebResult(name = "GetCurrentVolumesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetCurrentVolumesResponse getCurrentVolumesRequest(
        @WebParam(name = "GetCurrentVolumesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetCurrentVolumesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDataSourcesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDataSources")
    @WebResult(name = "GetDataSourcesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDataSourcesResponse getDataSourcesRequest(
        @WebParam(name = "GetDataSourcesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDataSourcesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDelegatedAdminConstraintsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDelegatedAdminConstraints")
    @WebResult(name = "GetDelegatedAdminConstraintsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDelegatedAdminConstraintsResponse getDelegatedAdminConstraintsRequest(
        @WebParam(name = "GetDelegatedAdminConstraintsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDelegatedAdminConstraintsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDeviceStatusResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDeviceStatus")
    @WebResult(name = "GetDeviceStatusResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDeviceStatusResponse getDeviceStatusRequest(
        @WebParam(name = "GetDeviceStatusRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDeviceStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDevicesCountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDevicesCount")
    @WebResult(name = "GetDevicesCountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDevicesCountResponse getDevicesCountRequest(
        @WebParam(name = "GetDevicesCountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDevicesCountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDevicesCountSinceLastUsedResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDevicesCountSinceLastUsed")
    @WebResult(name = "GetDevicesCountSinceLastUsedResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDevicesCountSinceLastUsedResponse getDevicesCountSinceLastUsedRequest(
        @WebParam(name = "GetDevicesCountSinceLastUsedRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDevicesCountSinceLastUsedRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDevicesCountUsedTodayResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDevicesCountUsedToday")
    @WebResult(name = "GetDevicesCountUsedTodayResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDevicesCountUsedTodayResponse getDevicesCountUsedTodayRequest(
        @WebParam(name = "GetDevicesCountUsedTodayRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDevicesCountUsedTodayRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDevicesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDevices")
    @WebResult(name = "GetDevicesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDevicesResponse getDevicesRequest(
        @WebParam(name = "GetDevicesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDevicesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDistributionListMembershipResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDistributionListMembership")
    @WebResult(name = "GetDistributionListMembershipResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDistributionListMembershipResponse getDistributionListMembershipRequest(
        @WebParam(name = "GetDistributionListMembershipRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDistributionListMembershipRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDistributionListResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDistributionList")
    @WebResult(name = "GetDistributionListResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDistributionListResponse getDistributionListRequest(
        @WebParam(name = "GetDistributionListRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDistributionListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDomainInfoResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDomainInfo")
    @WebResult(name = "GetDomainInfoResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDomainInfoResponse getDomainInfoRequest(
        @WebParam(name = "GetDomainInfoRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDomainInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetDomainResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetDomain")
    @WebResult(name = "GetDomainResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetDomainResponse getDomainRequest(
        @WebParam(name = "GetDomainRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetDomainRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetEffectiveRightsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetEffectiveRights")
    @WebResult(name = "GetEffectiveRightsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetEffectiveRightsResponse getEffectiveRightsRequest(
        @WebParam(name = "GetEffectiveRightsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetEffectiveRightsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetFreeBusyQueueInfoResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetFreeBusyQueueInfo")
    @WebResult(name = "GetFreeBusyQueueInfoResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetFreeBusyQueueInfoResponse getFreeBusyQueueInfoRequest(
        @WebParam(name = "GetFreeBusyQueueInfoRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetFreeBusyQueueInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetGrantsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetGrants")
    @WebResult(name = "GetGrantsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetGrantsResponse getGrantsRequest(
        @WebParam(name = "GetGrantsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetGrantsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetHsmStatusResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetHsmStatus")
    @WebResult(name = "GetHsmStatusResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetHsmStatusResponse getHsmStatusRequest(
        @WebParam(name = "GetHsmStatusRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetHsmStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetIndexStatsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetIndexStats")
    @WebResult(name = "GetIndexStatsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetIndexStatsResponse getIndexStatsRequest(
        @WebParam(name = "GetIndexStatsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetIndexStatsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetLDAPEntriesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetLDAPEntries")
    @WebResult(name = "GetLDAPEntriesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetLDAPEntriesResponse getLDAPEntriesRequest(
        @WebParam(name = "GetLDAPEntriesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetLDAPEntriesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetLicenseInfoResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetLicenseInfo")
    @WebResult(name = "GetLicenseInfoResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetLicenseInfoResponse getLicenseInfoRequest(
        @WebParam(name = "GetLicenseInfoRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetLicenseInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetLicenseResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetLicense")
    @WebResult(name = "GetLicenseResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetLicenseResponse getLicenseRequest(
        @WebParam(name = "GetLicenseRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetLicenseRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetLoggerStatsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetLoggerStats")
    @WebResult(name = "GetLoggerStatsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetLoggerStatsResponse getLoggerStatsRequest(
        @WebParam(name = "GetLoggerStatsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetLoggerStatsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetMailQueueInfoResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetMailQueueInfo")
    @WebResult(name = "GetMailQueueInfoResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetMailQueueInfoResponse getMailQueueInfoRequest(
        @WebParam(name = "GetMailQueueInfoRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetMailQueueInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetMailQueueResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetMailQueue")
    @WebResult(name = "GetMailQueueResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetMailQueueResponse getMailQueueRequest(
        @WebParam(name = "GetMailQueueRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetMailQueueRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetMailboxResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetMailbox")
    @WebResult(name = "GetMailboxResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetMailboxResponse getMailboxRequest(
        @WebParam(name = "GetMailboxRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetMailboxRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetMailboxStatsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetMailboxStats")
    @WebResult(name = "GetMailboxStatsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetMailboxStatsResponse getMailboxStatsRequest(
        @WebParam(name = "GetMailboxStatsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetMailboxStatsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetMailboxVersionResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetMailboxVersion")
    @WebResult(name = "GetMailboxVersionResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetMailboxVersionResponse getMailboxVersionRequest(
        @WebParam(name = "GetMailboxVersionRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetMailboxVersionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetMailboxVolumesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetMailboxVolumes")
    @WebResult(name = "GetMailboxVolumesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetMailboxVolumesResponse getMailboxVolumesRequest(
        @WebParam(name = "GetMailboxVolumesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetMailboxVolumesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetMemcachedClientConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetMemcachedClientConfig")
    @WebResult(name = "GetMemcachedClientConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetMemcachedClientConfigResponse getMemcachedClientConfigRequest(
        @WebParam(name = "GetMemcachedClientConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetMemcachedClientConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetQuotaUsageResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetQuotaUsage")
    @WebResult(name = "GetQuotaUsageResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetQuotaUsageResponse getQuotaUsageRequest(
        @WebParam(name = "GetQuotaUsageRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetQuotaUsageRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetRightResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetRight")
    @WebResult(name = "GetRightResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetRightResponse getRightRequest(
        @WebParam(name = "GetRightRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetRightRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetRightsDocResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetRightsDoc")
    @WebResult(name = "GetRightsDocResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetRightsDocResponse getRightsDocRequest(
        @WebParam(name = "GetRightsDocRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetRightsDocRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetSMIMEConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetSMIMEConfig")
    @WebResult(name = "GetSMIMEConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetSMIMEConfigResponse getSMIMEConfigRequest(
        @WebParam(name = "GetSMIMEConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetSMIMEConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetServerNIfsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetServerNIfs")
    @WebResult(name = "GetServerNIfsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetServerNIfsResponse getServerNIfsRequest(
        @WebParam(name = "GetServerNIfsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetServerNIfsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetServerResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetServer")
    @WebResult(name = "GetServerResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetServerResponse getServerRequest(
        @WebParam(name = "GetServerRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetServerRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetServerStatsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetServerStats")
    @WebResult(name = "GetServerStatsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetServerStatsResponse getServerStatsRequest(
        @WebParam(name = "GetServerStatsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetServerStatsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetServiceStatusResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetServiceStatus")
    @WebResult(name = "GetServiceStatusResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetServiceStatusResponse getServiceStatusRequest(
        @WebParam(name = "GetServiceStatusRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetServiceStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetSessionsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetSessions")
    @WebResult(name = "GetSessionsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetSessionsResponse getSessionsRequest(
        @WebParam(name = "GetSessionsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetSessionsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetShareInfoResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetShareInfo")
    @WebResult(name = "GetShareInfoResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetShareInfoResponse getShareInfoRequest(
        @WebParam(name = "GetShareInfoRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetShareInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetSystemRetentionPolicyResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetSystemRetentionPolicy")
    @WebResult(name = "GetSystemRetentionPolicyResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetSystemRetentionPolicyResponse getSystemRetentionPolicyRequest(
        @WebParam(name = "GetSystemRetentionPolicyRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetSystemRetentionPolicyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetUCServiceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetUCService")
    @WebResult(name = "GetUCServiceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetUCServiceResponse getUCServiceRequest(
        @WebParam(name = "GetUCServiceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetUCServiceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetVersionInfoResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetVersionInfo")
    @WebResult(name = "GetVersionInfoResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetVersionInfoResponse getVersionInfoRequest(
        @WebParam(name = "GetVersionInfoRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetVersionInfoRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetVolumeResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetVolume")
    @WebResult(name = "GetVolumeResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetVolumeResponse getVolumeRequest(
        @WebParam(name = "GetVolumeRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetVolumeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetXMPPComponentResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetXMPPComponent")
    @WebResult(name = "GetXMPPComponentResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetXMPPComponentResponse getXMPPComponentRequest(
        @WebParam(name = "GetXMPPComponentRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetXMPPComponentRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetXMbxSearchesListResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetXMbxSearchesList")
    @WebResult(name = "GetXMbxSearchesListResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetXMbxSearchesListResponse getXMbxSearchesListRequest(
        @WebParam(name = "GetXMbxSearchesListRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetXMbxSearchesListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetZimletResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetZimlet")
    @WebResult(name = "GetZimletResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetZimletResponse getZimletRequest(
        @WebParam(name = "GetZimletRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetZimletRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGetZimletStatusResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GetZimletStatus")
    @WebResult(name = "GetZimletStatusResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGetZimletStatusResponse getZimletStatusRequest(
        @WebParam(name = "GetZimletStatusRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGetZimletStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testGrantRightResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/GrantRight")
    @WebResult(name = "GrantRightResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testGrantRightResponse grantRightRequest(
        @WebParam(name = "GrantRightRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testGrantRightRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testHsmResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/Hsm")
    @WebResult(name = "HsmResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testHsmResponse hsmRequest(
        @WebParam(name = "HsmRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testHsmRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testInstallCertResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/InstallCert")
    @WebResult(name = "InstallCertResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testInstallCertResponse installCertRequest(
        @WebParam(name = "InstallCertRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testInstallCertRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testInstallLicenseResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/InstallLicense")
    @WebResult(name = "InstallLicenseResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testInstallLicenseResponse installLicenseRequest(
        @WebParam(name = "InstallLicenseRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testInstallLicenseRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testLockoutMailboxResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/LockoutMailbox")
    @WebResult(name = "LockoutMailboxResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testLockoutMailboxResponse lockoutMailboxRequest(
        @WebParam(name = "LockoutMailboxRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testLockoutMailboxRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testMailQueueActionResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/MailQueueAction")
    @WebResult(name = "MailQueueActionResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testMailQueueActionResponse mailQueueActionRequest(
        @WebParam(name = "MailQueueActionRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testMailQueueActionRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testMailQueueFlushResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/MailQueueFlush")
    @WebResult(name = "MailQueueFlushResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testMailQueueFlushResponse mailQueueFlushRequest(
        @WebParam(name = "MailQueueFlushRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testMailQueueFlushRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testMigrateAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/MigrateAccount")
    @WebResult(name = "MigrateAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testMigrateAccountResponse migrateAccountRequest(
        @WebParam(name = "MigrateAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testMigrateAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyAccount")
    @WebResult(name = "ModifyAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyAccountResponse modifyAccountRequest(
        @WebParam(name = "ModifyAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyAdminSavedSearchesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyAdminSavedSearches")
    @WebResult(name = "ModifyAdminSavedSearchesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyAdminSavedSearchesResponse modifyAdminSavedSearchesRequest(
        @WebParam(name = "ModifyAdminSavedSearchesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyAdminSavedSearchesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyAlwaysOnClusterResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyAlwaysOnCluster")
    @WebResult(name = "ModifyAlwaysOnClusterResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyAlwaysOnClusterResponse modifyAlwaysOnClusterRequest(
        @WebParam(name = "ModifyAlwaysOnClusterRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyAlwaysOnClusterRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyCalendarResourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyCalendarResource")
    @WebResult(name = "ModifyCalendarResourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyCalendarResourceResponse modifyCalendarResourceRequest(
        @WebParam(name = "ModifyCalendarResourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyCalendarResourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyConfig")
    @WebResult(name = "ModifyConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyConfigResponse modifyConfigRequest(
        @WebParam(name = "ModifyConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyCosResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyCos")
    @WebResult(name = "ModifyCosResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyCosResponse modifyCosRequest(
        @WebParam(name = "ModifyCosRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyCosRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyDataSourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyDataSource")
    @WebResult(name = "ModifyDataSourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyDataSourceResponse modifyDataSourceRequest(
        @WebParam(name = "ModifyDataSourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyDataSourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyDelegatedAdminConstraintsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyDelegatedAdminConstraints")
    @WebResult(name = "ModifyDelegatedAdminConstraintsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyDelegatedAdminConstraintsResponse modifyDelegatedAdminConstraintsRequest(
        @WebParam(name = "ModifyDelegatedAdminConstraintsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyDelegatedAdminConstraintsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyDistributionListResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyDistributionList")
    @WebResult(name = "ModifyDistributionListResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyDistributionListResponse modifyDistributionListRequest(
        @WebParam(name = "ModifyDistributionListRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyDistributionListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyDomainResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyDomain")
    @WebResult(name = "ModifyDomainResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyDomainResponse modifyDomainRequest(
        @WebParam(name = "ModifyDomainRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyDomainRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyLDAPEntryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyLDAPEntry")
    @WebResult(name = "ModifyLDAPEntryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyLDAPEntryResponse modifyLDAPEntryRequest(
        @WebParam(name = "ModifyLDAPEntryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyLDAPEntryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifySMIMEConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifySMIMEConfig")
    @WebResult(name = "ModifySMIMEConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifySMIMEConfigResponse modifySMIMEConfigRequest(
        @WebParam(name = "ModifySMIMEConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifySMIMEConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyServerResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyServer")
    @WebResult(name = "ModifyServerResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyServerResponse modifyServerRequest(
        @WebParam(name = "ModifyServerRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyServerRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifySystemRetentionPolicyResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifySystemRetentionPolicy")
    @WebResult(name = "ModifySystemRetentionPolicyResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifySystemRetentionPolicyResponse modifySystemRetentionPolicyRequest(
        @WebParam(name = "ModifySystemRetentionPolicyRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifySystemRetentionPolicyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyUCServiceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyUCService")
    @WebResult(name = "ModifyUCServiceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyUCServiceResponse modifyUCServiceRequest(
        @WebParam(name = "ModifyUCServiceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyUCServiceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyVolumeResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyVolume")
    @WebResult(name = "ModifyVolumeResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyVolumeResponse modifyVolumeRequest(
        @WebParam(name = "ModifyVolumeRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyVolumeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testModifyZimletResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ModifyZimlet")
    @WebResult(name = "ModifyZimletResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testModifyZimletResponse modifyZimletRequest(
        @WebParam(name = "ModifyZimletRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testModifyZimletRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testMoveBlobsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/MoveBlobs")
    @WebResult(name = "MoveBlobsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testMoveBlobsResponse moveBlobsRequest(
        @WebParam(name = "MoveBlobsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testMoveBlobsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testMoveMailboxResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/MoveMailbox")
    @WebResult(name = "MoveMailboxResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testMoveMailboxResponse moveMailboxRequest(
        @WebParam(name = "MoveMailboxRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testMoveMailboxRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testNoOpResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/NoOp")
    @WebResult(name = "NoOpResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testNoOpResponse noOpRequest(
        @WebParam(name = "NoOpRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testNoOpRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testPingResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/Ping")
    @WebResult(name = "PingResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testPingResponse pingRequest(
        @WebParam(name = "PingRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testPingRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testPurgeAccountCalendarCacheResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/PurgeAccountCalendarCache")
    @WebResult(name = "PurgeAccountCalendarCacheResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testPurgeAccountCalendarCacheResponse purgeAccountCalendarCacheRequest(
        @WebParam(name = "PurgeAccountCalendarCacheRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testPurgeAccountCalendarCacheRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testPurgeFreeBusyQueueResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/PurgeFreeBusyQueue")
    @WebResult(name = "PurgeFreeBusyQueueResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testPurgeFreeBusyQueueResponse purgeFreeBusyQueueRequest(
        @WebParam(name = "PurgeFreeBusyQueueRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testPurgeFreeBusyQueueRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testPurgeMessagesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/PurgeMessages")
    @WebResult(name = "PurgeMessagesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testPurgeMessagesResponse purgeMessagesRequest(
        @WebParam(name = "PurgeMessagesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testPurgeMessagesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testPurgeMovedMailboxResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/PurgeMovedMailbox")
    @WebResult(name = "PurgeMovedMailboxResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testPurgeMovedMailboxResponse purgeMovedMailboxRequest(
        @WebParam(name = "PurgeMovedMailboxRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testPurgeMovedMailboxRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testPushFreeBusyResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/PushFreeBusy")
    @WebResult(name = "PushFreeBusyResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testPushFreeBusyResponse pushFreeBusyRequest(
        @WebParam(name = "PushFreeBusyRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testPushFreeBusyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testQueryMailboxMoveResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/QueryMailboxMove")
    @WebResult(name = "QueryMailboxMoveResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testQueryMailboxMoveResponse queryMailboxMoveRequest(
        @WebParam(name = "QueryMailboxMoveRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testQueryMailboxMoveRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testQueryWaitSetResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/QueryWaitSet")
    @WebResult(name = "QueryWaitSetResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testQueryWaitSetResponse queryWaitSetRequest(
        @WebParam(name = "QueryWaitSetRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testQueryWaitSetRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testReIndexResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ReIndex")
    @WebResult(name = "ReIndexResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testReIndexResponse reIndexRequest(
        @WebParam(name = "ReIndexRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testReIndexRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRecalculateMailboxCountsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RecalculateMailboxCounts")
    @WebResult(name = "RecalculateMailboxCountsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRecalculateMailboxCountsResponse recalculateMailboxCountsRequest(
        @WebParam(name = "RecalculateMailboxCountsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRecalculateMailboxCountsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRefreshRegisteredAuthTokensResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RefreshRegisteredAuthTokens")
    @WebResult(name = "RefreshRegisteredAuthTokensResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRefreshRegisteredAuthTokensResponse refreshRegisteredAuthTokensRequest(
        @WebParam(name = "RefreshRegisteredAuthTokensRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRefreshRegisteredAuthTokensRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRegisterMailboxMoveOutResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RegisterMailboxMoveOut")
    @WebResult(name = "RegisterMailboxMoveOutResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRegisterMailboxMoveOutResponse registerMailboxMoveOutRequest(
        @WebParam(name = "RegisterMailboxMoveOutRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRegisterMailboxMoveOutRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testReloadAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ReloadAccount")
    @WebResult(name = "ReloadAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testReloadAccountResponse reloadAccountRequest(
        @WebParam(name = "ReloadAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testReloadAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testReloadLocalConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ReloadLocalConfig")
    @WebResult(name = "ReloadLocalConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testReloadLocalConfigResponse reloadLocalConfigRequest(
        @WebParam(name = "ReloadLocalConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testReloadLocalConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testReloadMemcachedClientConfigResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ReloadMemcachedClientConfig")
    @WebResult(name = "ReloadMemcachedClientConfigResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testReloadMemcachedClientConfigResponse reloadMemcachedClientConfigRequest(
        @WebParam(name = "ReloadMemcachedClientConfigRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testReloadMemcachedClientConfigRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRemoteWipeResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RemoteWipe")
    @WebResult(name = "RemoteWipeResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRemoteWipeResponse remoteWipeRequest(
        @WebParam(name = "RemoteWipeRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRemoteWipeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRemoveAccountAliasResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RemoveAccountAlias")
    @WebResult(name = "RemoveAccountAliasResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRemoveAccountAliasResponse removeAccountAliasRequest(
        @WebParam(name = "RemoveAccountAliasRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRemoveAccountAliasRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRemoveAccountLoggerResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RemoveAccountLogger")
    @WebResult(name = "RemoveAccountLoggerResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRemoveAccountLoggerResponse removeAccountLoggerRequest(
        @WebParam(name = "RemoveAccountLoggerRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRemoveAccountLoggerRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRemoveDeviceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RemoveDevice")
    @WebResult(name = "RemoveDeviceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRemoveDeviceResponse removeDeviceRequest(
        @WebParam(name = "RemoveDeviceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRemoveDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRemoveDistributionListAliasResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RemoveDistributionListAlias")
    @WebResult(name = "RemoveDistributionListAliasResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRemoveDistributionListAliasResponse removeDistributionListAliasRequest(
        @WebParam(name = "RemoveDistributionListAliasRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRemoveDistributionListAliasRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRemoveDistributionListMemberResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RemoveDistributionListMember")
    @WebResult(name = "RemoveDistributionListMemberResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRemoveDistributionListMemberResponse removeDistributionListMemberRequest(
        @WebParam(name = "RemoveDistributionListMemberRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRemoveDistributionListMemberRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRenameAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RenameAccount")
    @WebResult(name = "RenameAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRenameAccountResponse renameAccountRequest(
        @WebParam(name = "RenameAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRenameAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRenameCalendarResourceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RenameCalendarResource")
    @WebResult(name = "RenameCalendarResourceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRenameCalendarResourceResponse renameCalendarResourceRequest(
        @WebParam(name = "RenameCalendarResourceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRenameCalendarResourceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRenameCosResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RenameCos")
    @WebResult(name = "RenameCosResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRenameCosResponse renameCosRequest(
        @WebParam(name = "RenameCosRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRenameCosRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRenameDistributionListResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RenameDistributionList")
    @WebResult(name = "RenameDistributionListResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRenameDistributionListResponse renameDistributionListRequest(
        @WebParam(name = "RenameDistributionListRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRenameDistributionListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRenameLDAPEntryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RenameLDAPEntry")
    @WebResult(name = "RenameLDAPEntryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRenameLDAPEntryResponse renameLDAPEntryRequest(
        @WebParam(name = "RenameLDAPEntryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRenameLDAPEntryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRenameUCServiceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RenameUCService")
    @WebResult(name = "RenameUCServiceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRenameUCServiceResponse renameUCServiceRequest(
        @WebParam(name = "RenameUCServiceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRenameUCServiceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testResetAllLoggersResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ResetAllLoggers")
    @WebResult(name = "ResetAllLoggersResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testResetAllLoggersResponse resetAllLoggersRequest(
        @WebParam(name = "ResetAllLoggersRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testResetAllLoggersRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRestoreResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/Restore")
    @WebResult(name = "RestoreResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRestoreResponse restoreRequest(
        @WebParam(name = "RestoreRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRestoreRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testResumeDeviceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ResumeDevice")
    @WebResult(name = "ResumeDeviceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testResumeDeviceResponse resumeDeviceRequest(
        @WebParam(name = "ResumeDeviceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testResumeDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRevokeRightResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RevokeRight")
    @WebResult(name = "RevokeRightResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRevokeRightResponse revokeRightRequest(
        @WebParam(name = "RevokeRightRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRevokeRightRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRolloverRedoLogResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RolloverRedoLog")
    @WebResult(name = "RolloverRedoLogResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRolloverRedoLogResponse rolloverRedoLogRequest(
        @WebParam(name = "RolloverRedoLogRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRolloverRedoLogRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testRunUnitTestsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/RunUnitTests")
    @WebResult(name = "RunUnitTestsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testRunUnitTestsResponse runUnitTestsRequest(
        @WebParam(name = "RunUnitTestsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testRunUnitTestsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testScheduleBackupsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/ScheduleBackups")
    @WebResult(name = "ScheduleBackupsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testScheduleBackupsResponse scheduleBackupsRequest(
        @WebParam(name = "ScheduleBackupsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testScheduleBackupsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSearchAccountsResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SearchAccounts")
    @WebResult(name = "SearchAccountsResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSearchAccountsResponse searchAccountsRequest(
        @WebParam(name = "SearchAccountsRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSearchAccountsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSearchAutoProvDirectoryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SearchAutoProvDirectory")
    @WebResult(name = "SearchAutoProvDirectoryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSearchAutoProvDirectoryResponse searchAutoProvDirectoryRequest(
        @WebParam(name = "SearchAutoProvDirectoryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSearchAutoProvDirectoryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSearchCalendarResourcesResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SearchCalendarResources")
    @WebResult(name = "SearchCalendarResourcesResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSearchCalendarResourcesResponse searchCalendarResourcesRequest(
        @WebParam(name = "SearchCalendarResourcesRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSearchCalendarResourcesRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSearchDirectoryResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SearchDirectory")
    @WebResult(name = "SearchDirectoryResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSearchDirectoryResponse searchDirectoryRequest(
        @WebParam(name = "SearchDirectoryRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSearchDirectoryRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSearchGalResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SearchGal")
    @WebResult(name = "SearchGalResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSearchGalResponse searchGalRequest(
        @WebParam(name = "SearchGalRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSearchGalRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSearchMultiMailboxResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SearchMultiMailbox")
    @WebResult(name = "SearchMultiMailboxResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSearchMultiMailboxResponse searchMultiMailboxRequest(
        @WebParam(name = "SearchMultiMailboxRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSearchMultiMailboxRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSetCurrentVolumeResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SetCurrentVolume")
    @WebResult(name = "SetCurrentVolumeResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSetCurrentVolumeResponse setCurrentVolumeRequest(
        @WebParam(name = "SetCurrentVolumeRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSetCurrentVolumeRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSetLocalServerOnlineResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SetLocalServerOnline")
    @WebResult(name = "SetLocalServerOnlineResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSetLocalServerOnlineResponse setLocalServerOnlineRequest(
        @WebParam(name = "SetLocalServerOnlineRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSetLocalServerOnlineRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSetPasswordResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SetPassword")
    @WebResult(name = "SetPasswordResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSetPasswordResponse setPasswordRequest(
        @WebParam(name = "SetPasswordRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSetPasswordRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSetServerOfflineResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SetServerOffline")
    @WebResult(name = "SetServerOfflineResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSetServerOfflineResponse setServerOfflineRequest(
        @WebParam(name = "SetServerOfflineRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSetServerOfflineRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSuspendDeviceResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SuspendDevice")
    @WebResult(name = "SuspendDeviceResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSuspendDeviceResponse suspendDeviceRequest(
        @WebParam(name = "SuspendDeviceRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSuspendDeviceRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testSyncGalAccountResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/SyncGalAccount")
    @WebResult(name = "SyncGalAccountResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testSyncGalAccountResponse syncGalAccountRequest(
        @WebParam(name = "SyncGalAccountRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testSyncGalAccountRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testUndeployZimletResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/UndeployZimlet")
    @WebResult(name = "UndeployZimletResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testUndeployZimletResponse undeployZimletRequest(
        @WebParam(name = "UndeployZimletRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testUndeployZimletRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testUnloadMailboxResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/UnloadMailbox")
    @WebResult(name = "UnloadMailboxResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testUnloadMailboxResponse unloadMailboxRequest(
        @WebParam(name = "UnloadMailboxRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testUnloadMailboxRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testUnregisterMailboxMoveOutResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/UnregisterMailboxMoveOut")
    @WebResult(name = "UnregisterMailboxMoveOutResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testUnregisterMailboxMoveOutResponse unregisterMailboxMoveOutRequest(
        @WebParam(name = "UnregisterMailboxMoveOutRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testUnregisterMailboxMoveOutRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testUpdateDeviceStatusResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/UpdateDeviceStatus")
    @WebResult(name = "UpdateDeviceStatusResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testUpdateDeviceStatusResponse updateDeviceStatusRequest(
        @WebParam(name = "UpdateDeviceStatusRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testUpdateDeviceStatusRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testUpdatePresenceSessionIdResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/UpdatePresenceSessionId")
    @WebResult(name = "UpdatePresenceSessionIdResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testUpdatePresenceSessionIdResponse updatePresenceSessionIdRequest(
        @WebParam(name = "UpdatePresenceSessionIdRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testUpdatePresenceSessionIdRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testUploadDomCertResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/UploadDomCert")
    @WebResult(name = "UploadDomCertResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testUploadDomCertResponse uploadDomCertRequest(
        @WebParam(name = "UploadDomCertRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testUploadDomCertRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testUploadProxyCAResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/UploadProxyCA")
    @WebResult(name = "UploadProxyCAResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testUploadProxyCAResponse uploadProxyCARequest(
        @WebParam(name = "UploadProxyCARequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testUploadProxyCARequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testVerifyCertKeyResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/VerifyCertKey")
    @WebResult(name = "VerifyCertKeyResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testVerifyCertKeyResponse verifyCertKeyRequest(
        @WebParam(name = "VerifyCertKeyRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testVerifyCertKeyRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testVerifyIndexResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/VerifyIndex")
    @WebResult(name = "VerifyIndexResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testVerifyIndexResponse verifyIndexRequest(
        @WebParam(name = "VerifyIndexRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testVerifyIndexRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testVerifyStoreManagerResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/VerifyStoreManager")
    @WebResult(name = "VerifyStoreManagerResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testVerifyStoreManagerResponse verifyStoreManagerRequest(
        @WebParam(name = "VerifyStoreManagerRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testVerifyStoreManagerRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.admin.testVersionCheckResponse
     */
    @WebMethod(action = "urn:zimbraAdmin/VersionCheck")
    @WebResult(name = "VersionCheckResponse", targetNamespace = "urn:zimbraAdmin", partName = "params")
    public testVersionCheckResponse versionCheckRequest(
        @WebParam(name = "VersionCheckRequest", targetNamespace = "urn:zimbraAdmin", partName = "params")
        testVersionCheckRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.adminext.testBulkIMAPDataImportResponse
     */
    @WebMethod(action = "urn:zimbraAdminExt/BulkIMAPDataImport")
    @WebResult(name = "BulkIMAPDataImportResponse", targetNamespace = "urn:zimbraAdminExt", partName = "params")
    public testBulkIMAPDataImportResponse bulkIMAPDataImportRequest(
        @WebParam(name = "BulkIMAPDataImportRequest", targetNamespace = "urn:zimbraAdminExt", partName = "params")
        testBulkIMAPDataImportRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.adminext.testBulkImportAccountsResponse
     */
    @WebMethod(action = "urn:zimbraAdminExt/BulkImportAccounts")
    @WebResult(name = "BulkImportAccountsResponse", targetNamespace = "urn:zimbraAdminExt", partName = "params")
    public testBulkImportAccountsResponse bulkImportAccountsRequest(
        @WebParam(name = "BulkImportAccountsRequest", targetNamespace = "urn:zimbraAdminExt", partName = "params")
        testBulkImportAccountsRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.adminext.testGenerateBulkProvisionFileFromLDAPResponse
     */
    @WebMethod(action = "urn:zimbraAdminExt/GenerateBulkProvisionFileFromLDAP")
    @WebResult(name = "GenerateBulkProvisionFileFromLDAPResponse", targetNamespace = "urn:zimbraAdminExt", partName = "params")
    public testGenerateBulkProvisionFileFromLDAPResponse generateBulkProvisionFileFromLDAPRequest(
        @WebParam(name = "GenerateBulkProvisionFileFromLDAPRequest", targetNamespace = "urn:zimbraAdminExt", partName = "params")
        testGenerateBulkProvisionFileFromLDAPRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.adminext.testGetBulkIMAPImportTaskListResponse
     */
    @WebMethod(action = "urn:zimbraAdminExt/GetBulkIMAPImportTaskList")
    @WebResult(name = "GetBulkIMAPImportTaskListResponse", targetNamespace = "urn:zimbraAdminExt", partName = "params")
    public testGetBulkIMAPImportTaskListResponse getBulkIMAPImportTaskListRequest(
        @WebParam(name = "GetBulkIMAPImportTaskListRequest", targetNamespace = "urn:zimbraAdminExt", partName = "params")
        testGetBulkIMAPImportTaskListRequest params);

    /**
     * 
     * @param params
     * @return
     *     returns generated.zcsclient.adminext.testPurgeBulkIMAPImportTasksResponse
     */
    @WebMethod(action = "urn:zimbraAdminExt/PurgeBulkIMAPImportTasks")
    @WebResult(name = "PurgeBulkIMAPImportTasksResponse", targetNamespace = "urn:zimbraAdminExt", partName = "params")
    public testPurgeBulkIMAPImportTasksResponse purgeBulkIMAPImportTasksRequest(
        @WebParam(name = "PurgeBulkIMAPImportTasksRequest", targetNamespace = "urn:zimbraAdminExt", partName = "params")
        testPurgeBulkIMAPImportTasksRequest params);

}
