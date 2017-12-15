/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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

// System files - These should really be in precompiled header
#include <Iads.h>
#include <adshlp.h>
#include <AdsErr.h>
#include <objsel.h>
#include <cmnquery.h>
#include <dsquery.h>
#include <Shlobj.h>
#include <shlguid.h>
#include <dsclient.h>
#include <adshlp.h>
#include <IADS.h>
#include <sddl.h>

#pragma warning(disable : 4995)
#include <strsafe.h>

#include "commonMAPI.h"
#include "edkmdb.h"
#include "edk/edkmapi.h"

#include "optimize.h"
#include "logger.h"

#include "GenericException.h"

#include "resource.h"

//#define ENABLE_MIMEPP_MEM_MAPPED_STRINGS
#include "Zimbra/Zimbra.h" // #includes "mimepp.h"

#include "Exchange_i.h"

#include "MAPISession.h"
#include "MAPIStore.h"
#include "MAPITableIterator.h"
#include "MAPIMessage.h"
#include "MAPIFolder.h"
#include "MapiUtils.h"
#include "MAPIContacts.h"
#include "MAPIRfc2445.h"
#include "MAPIAppointment.h"
#include "MAPITask.h"
#include "MAPIAccount.h"
#include "ExchangeAdmin.h"
#include "SessionGlobal.h"

#include "util.h"

using namespace Zimbra::Util;
using namespace Zimbra::MAPI;


class CExchangeModule: public CAtlDllModuleT<CExchangeModule>
{
public:
    DECLARE_LIBID(LIBID_Exchange) DECLARE_REGISTRY_APPID_RESOURCEID(IDR_EXCHANGE, "{0A8DF15B-275B-408A-84EA-8AEF784C81C3}")
};

extern class CExchangeModule _AtlModule;

/* Flags for MAPIINIT_0 structure ulFlags value passed to MAPIInitialize()  */
#define MAPI_NO_COINIT			0x00000008

/*------------------------------------------------------------------------
 *
 *	"IExchangeManageStore2" Interface Declaration
 *
 *	Used for store management functions.
 *
 *-----------------------------------------------------------------------*/

#define EXCHANGE_IEXCHANGEMANAGESTORE2_METHODS(IPURE)					\
    MAPIMETHOD(CreateNewsgroupNameEntryID)								\
        (THIS_	LPSTR						lpszNewsgroupName,			\
                ULONG FAR *					lpcbEntryID,				\
                LPENTRYID FAR *				lppEntryID) IPURE;

#undef		 INTERFACE
#define		 INTERFACE	IExchangeManageStore2
DECLARE_MAPI_INTERFACE_(IExchangeManageStore2, IUnknown)
{
    MAPI_IUNKNOWN_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE2_METHODS(PURE)
};
#undef	IMPL
#define IMPL

DECLARE_MAPI_INTERFACE_PTR(IExchangeManageStore2, LPEXCHANGEMANAGESTORE2);


/*------------------------------------------------------------------------
 *
 *	"IExchangeManageStore3" Interface Declaration
 *
 *	Used for store management functions.
 *
 *-----------------------------------------------------------------------*/

#define EXCHANGE_IEXCHANGEMANAGESTORE3_METHODS(IPURE)				\
    MAPIMETHOD(GetMailboxTableOffset)							    \
        (THIS_	LPSTR						lpszServerName,			\
                LPMAPITABLE FAR *			lppTable,				\
                ULONG						ulFlags,				\
                UINT						uOffset) IPURE;

#undef		 INTERFACE
#define		 INTERFACE  IExchangeManageStore3
DECLARE_MAPI_INTERFACE_(IExchangeManageStore3, IUnknown)
{
    MAPI_IUNKNOWN_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE2_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE3_METHODS(PURE)
};
#undef	IMPL
#define IMPL

DECLARE_MAPI_INTERFACE_PTR(IExchangeManageStore3, LPEXCHANGEMANAGESTORE3);
/*------------------------------------------------------------------------
 *
 *	"IExchangeManageStore4" Interface Declaration
 *
 *	Used for store management functions.
 *
 *-----------------------------------------------------------------------*/

#define EXCHANGE_IEXCHANGEMANAGESTORE4_METHODS(IPURE)				\
    MAPIMETHOD(GetPublicFolderTableOffset)							\
        (THIS_	LPSTR						lpszServerName,			\
                LPMAPITABLE FAR *			lppTable,				\
                ULONG						ulFlags,			    \
                UINT						uOffset) IPURE;

#undef		 INTERFACE
#define		 INTERFACE  IExchangeManageStore4
DECLARE_MAPI_INTERFACE_(IExchangeManageStore4, IUnknown)
{
    MAPI_IUNKNOWN_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE2_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE3_METHODS(PURE)
    EXCHANGE_IEXCHANGEMANAGESTORE4_METHODS(PURE)
};
#undef	IMPL
#define IMPL

DECLARE_MAPI_INTERFACE_PTR(IExchangeManageStore4, LPEXCHANGEMANAGESTORE4);

// Sometimes IExchangeManageStore5 is in edkmdb.h, sometimes it isn't
#ifndef EXCHANGE_IEXCHANGEMANAGESTORE5_METHODS
#define USES_IID_IExchangeManageStore5

/*------------------------------------------------------------------------
*
*	'IExchangeManageStore5' Interface Declaration
*
*	Used for store management functions.
*
*-----------------------------------------------------------------------*/

#define EXCHANGE_IEXCHANGEMANAGESTORE5_METHODS(IPURE)	            \
    MAPIMETHOD(GetMailboxTableEx)						            \
                (THIS_	LPSTR			lpszServerName,				\
                LPGUID					lpguidMdb,					\
                LPMAPITABLE*			lppTable,					\
                ULONG					ulFlags,					\
                UINT					uOffset) IPURE;				\
                                                                    \
    MAPIMETHOD(GetPublicFolderTableEx)							    \
                (THIS_	LPSTR			lpszServerName,				\
                LPGUID					lpguidMdb,					\
                LPMAPITABLE*			lppTable,					\
                ULONG					ulFlags,					\
                UINT					uOffset) IPURE;				\

#undef  INTERFACE
#define INTERFACE  IExchangeManageStore5
DECLARE_MAPI_INTERFACE_(IExchangeManageStore5, IUnknown)
{
    MAPI_IUNKNOWN_METHODS(PURE)
        EXCHANGE_IEXCHANGEMANAGESTORE_METHODS(PURE)
        EXCHANGE_IEXCHANGEMANAGESTORE2_METHODS(PURE)
        EXCHANGE_IEXCHANGEMANAGESTORE3_METHODS(PURE)
        EXCHANGE_IEXCHANGEMANAGESTORE4_METHODS(PURE)
        EXCHANGE_IEXCHANGEMANAGESTORE5_METHODS(PURE)
};
#undef	IMPL
#define IMPL
DECLARE_MAPI_INTERFACE_PTR(IExchangeManageStore5, LPEXCHANGEMANAGESTORE5);
#endif // #ifndef EXCHANGE_IEXCHANGEMANAGESTORE5_METHODS


DEFINE_GUID(IID_IExchangeManageStore2,0xb6dca470, 0xff3, 0x11d0, 0xa4, 0x9, 0x0, 0xc0, 0x4f, 0xd7, 0xbd, 0x87);
DEFINE_GUID(IID_IExchangeManageStore3,0x166d9bc2, 0xdb75, 0x44a9, 0x8a, 0x93, 0x9f, 0x3f, 0xfc, 0x99, 0x4d, 0x76) ;
DEFINE_GUID(IID_IExchangeManageStore4,0x2590ff87, 0xc431, 0x4f9c, 0xb1, 0xa8, 0xcd, 0x69, 0xd7, 0x60, 0xcd, 0x10);	// {2590FF87-C431-4f9c-B1A8-CD69D760CD10}

// Sometimes IExchangeManageStore5 is in edkmdb.h, sometimes it isn't
#ifdef USES_IID_IExchangeManageStore5
DEFINE_GUID(IID_IExchangeManageStore5,0x7907dd18, 0xf141, 0x4676, 0xb1, 0x02, 0x37, 0xc9, 0xd9, 0x36, 0x34, 0x30);
#endif
