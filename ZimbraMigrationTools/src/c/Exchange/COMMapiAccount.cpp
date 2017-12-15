/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
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
// COMMapiAccount.cpp : Implementation of COMMapiAccount

#include "common.h"
#include "COMMapiAccount.h"

Zimbra::Util::CriticalSection  cs;

// ===================================================================================
// class COMMapiAccount
// ===================================================================================
STDMETHODIMP COMMapiAccount::InterfaceSupportsErrorInfo(REFIID riid)
{
    static const IID* const arr[] = 
    {
        &IID_IMapiAccount
    };

    for (int i=0; i < sizeof(arr) / sizeof(arr[0]); i++)
    {
        if (InlineIsEqualGUID(*arr[i],riid))
            return S_OK;
    }
    return S_FALSE;
}

wstring BSTR2Str(const BSTR& b)
{
    UINT l = SysStringLen(b);
    if (!l)
        return L"";
    return wstring(b, l);
}

STDMETHODIMP COMMapiAccount::InitMapiAccount(BOOL bIsServerMigration, BSTR bstrSrcAccount, BSTR bstrZCSAccount, BOOL bIsPublicFoldersMigration, BSTR *pErrorText)
//
// Called several times during a multi-account migration - once for each account being migrated
//
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_FALSE;

    LOG_TRACE(_T("IsServerMigration: %s"), bIsServerMigration?_T("Yes"):_T("No"));
    LOG_TRACE(_T("SrcAccount:        '%s'"), BSTR2Str(bstrSrcAccount).c_str());
    LOG_TRACE(_T("ZCSAccount:        '%s'"), BSTR2Str(bstrZCSAccount).c_str());

    if (!bIsPublicFoldersMigration)
    {
        // Not public folders
        if (bIsServerMigration)
        {
            // ----------------------
            // Server migration
            // ----------------------
            // Done as part of tools init in this case - just as well because called multiple times on multiple threads in case of multi-user server mig!
            hr = InitCMapiAccount(bstrSrcAccount, bstrZCSAccount, pErrorText);
        }
        else
        {
            // ----------------------
            // PST or User migration
            // ----------------------

            // Init MAPI, wrap in temp profile if necessary, open default store
            LPCWSTR err = CSessionGlobal::InitGlobalSessionAndStore(bstrSrcAccount);
            if (!err)
                hr = InitCMapiAccount(L"", bstrZCSAccount, pErrorText);
            else
                *pErrorText = CComBSTR(err);
        }
    }
    else
    {
        // -------------------------
        // Public folders migration
        // -------------------------
        // Following causes MAPILogonEx for Admin account and opens default store (but doesn't walk its hierarchy)
        // "EXCH_ADMIN" simply forces _InitGlobalSessionAndStore() to think this is a SERVER migration
        LPCWSTR err = CSessionGlobal::InitGlobalSessionAndStore(bstrSrcAccount, L"EXCH_ADMIN", bIsPublicFoldersMigration); 
        if (!err)
        {
            // DCB_BUG_Don't call InitCMapiAccount this because it calls OpenUserStore() - which is a nonsense because that 
            // just opens the default Admin store which we've already done above). Also causes errors in log
            // hr = InitCMapiAccount(L"", L"", pErrorText);

            // Instead, the following is sufficient
            m_pMapiAccount = new Zimbra::MAPI::CMapiAccount(bstrSrcAccount, bstrZCSAccount);
            *pErrorText = SysAllocString(L"");
        }
        else
            *pErrorText = CComBSTR(err);

        hr = InitAndEnumeratePublicFolders(pErrorText);
    }

    if (FAILED(hr))
    {
        CComBSTR str = "Init error ";
        str.AppendBSTR(*pErrorText);
        LOG_ERROR(_T("%s"), str.m_str);
    }
    return hr;
}

HRESULT COMMapiAccount::InitCMapiAccount(BSTR bstrSrcAccount, BSTR bstrZCSAccount, BSTR *StatusMsg)
{
    LOGFN_TRACE_NO;

    Zimbra::Util::AutoCriticalSection autocriticalsection(cs);

    // Init session and stores
    m_pMapiAccount = new Zimbra::MAPI::CMapiAccount(bstrSrcAccount, bstrZCSAccount);
    LPCWSTR lpStatus = m_pMapiAccount->LogonAndGetRootFolder();

    *StatusMsg = (lpStatus) ? CComBSTR(lpStatus) : SysAllocString(L"");

    return S_OK;
}

STDMETHODIMP COMMapiAccount::UninitMapiAccount()
{
    LOGFN_TRACE_NO;

    if (m_pMapiAccount)
    {
        delete m_pMapiAccount;
        m_pMapiAccount = NULL;
    }

    return S_OK;
}

/*
// =========================================================================================
// DCB_BUG_103092  Special Folder Identification Algorithm
// =========================================================================================


Background
----------

The C# layer will eventually receive a list of folders from the C++ layer.
It needs to know which of these are SPECIAL FOLDERS (Inbox, Sent, Calendar etc) so that it can place
the contents of these into the corresponding ZCS special folders.

For EXCHANGE MIGRATIONS, we will have identified special folders earlier by looking at the
Exchange Special Folder EntryIDs on the root an Inbox per https://msdn.microsoft.com/en-us/library/cc463910(v=exchg.80).aspx

The same is true of PST migrations for PSTS WHICH HAVE BEEN USED AS THE DEFAULT STORE.

However there is another type of PST - which I have chosen to call an ARCHIVE PST.

In many organizations, each user is given a relatively small mailbox quota. When they exceed
this, they export some of their data to a PST using Outlook Import/Export wizard. Such a PST
is an ARCHIVE PST.

Unfortunately, the Import/Export wizard does not set the Special Folder EntryIDs on root and Inbox
in an ARCHIVE PST.

In Archive PSTs, the only way for us to determine the special folders is by looking at display names.

This however is non-trivial because the special folder display names may well be localized and there
is no indication in the PST what the language is.

To make matters worse, some languages use the same display name for some folders. E.g. Danish, German, Swedish
and Norwegian all use "Kalendar" for the calendar special folder


Examples
========

An archive PST containing the following folders is, to a good degree of certainty, English
    Inbox
    Delete Items
    Junk E-mail
    Sent Items
    Drafts
    Contacts
    Emailed Contacts
    Tasks

Likewise an archive PST containing the following folders is, to a good degree of certainty, German
    Posteingang,
    Gel\u00f6schte Objekte OR Gel\u00f6schte Elemente
    Junk-E-Mail
    Gesendete Objekte      OR  Gesendete Elemente
    Entw\u00fcrfe
    Kontakte
    Kalender
    Vorgeschlagene Kontakte
    Aufgaben

But an archive PST containing just "Kalendar" might be Danish, German, Swedish or Norwegian


What about a PST containing the following:
    Inbox
    Delete Items
    Junk E-mail
    Sent Items
    Drafts
    Contacts
    Emailed Contacts
    Tasks
    Kalendar

i.e. all english names + one called "Kalendar"

** In this case, we probably want to avoid treating Kalendar as a special folder **

This means that we can't simply look at each folder in isolation. Rather we have to try to guess the language
by considering all folders together, and then use this overall lang to identify which are the special folders.


ALGORITHM
==========

STEP 1: Get complete folder list. 

STEP 2: Once we've got the folder list, take the Depth 2 subset (those at same depth as Inbox etc)

STEP 3: For each supported language, see how many folder names match

  struct LangScore
Lang          nScore
--------      ------
ENG 1033        8
SPA 3082        0 
DAN 1030        0
GER 1031        1
FRE 1036        0
ITA 1040        0
MAL 1086        0
DUT 1043        3                  
POL 1045        0
POR 2070        0
PTB 1046        0
ROM 1048        0
SWE 1053        3
SLV 1060        0
SLK 1051        0
RUS 1049        0
CHI 2052        0
NOR 1044        3

STEP 4: Pick the language with the highest score

STEP 5: Rerun the match FOR THE FOUND LANGUAGE and mark folders with SFID

*/

// How to add a language
// 1. Add entry to gaLangScores
// 2. Add the localised folder names to gaLocalizedSFNames

// Supported LangIDs
#define LANGID_ENG 1033 // English
#define LANGID_SPA 3082 // Spanish
#define LANGID_DAN 1030 // Danish
#define LANGID_GER 1031 // German
#define LANGID_FRE 1036 // French
#define LANGID_ITA 1040 // Italian
#define LANGID_MAL 1086 // Malay
#define LANGID_DUT 1043 // Dutch (Metherlands)
#define LANGID_POL 1045 // Polish
#define LANGID_POR 2070 // Portugese
#define LANGID_PTB 1046 // Portugese (Brazil)
#define LANGID_ROM 1048 // Romanian
#define LANGID_SWE 1053 // Swedish
#define LANGID_SLV 1060 // Slovenian
#define LANGID_SLK 1051 // Slovak
#define LANGID_RUS 1049 // Russian
#define LANGID_CHI 2052 // Simplified Chinese (CN)
#define LANGID_NOR 1044 // Norwegian
const int g_NUM_LANGS = 18; // Number of #defines above

const int FIND_IN_FILES_COUNT = 269; // Easy way to get this number: do find-in-files for "{LANGID" and look at resulting count
const int g_NUM_LOCALIZED_SPECIAL_FOLDER_NAMES = FIND_IN_FILES_COUNT-1; 

#pragma warning(disable:4428)

struct LocalizedSFNames
{
    int                   nLangID;
    ZimbraSpecialFolderId nZSFID;
    wstring               sName;
};

// The following table contains a list of all special folder names in all supported languages
// and is based on code removed in changelist 553326 ZimbraAPI.cs (which was originally in the C++ layer)

// This was then supplemented with some strings from OL2016.
// - Create foreign lang PST (or using a previously created POP profile with PST, change the OL language and start OL with /resetfoldernames switch)
// - Open in OL
// - Right click folder name->properties + copy the name string
// - Convert to \u notation - see //depot/zimbra/main/MAPIPlayground/Uescape

// The number of languages in here must match g_NUM_LANGS
// The languages should be in descending order of preference
const LocalizedSFNames gaLocalizedSFNames[g_NUM_LOCALIZED_SPECIAL_FOLDER_NAMES] = 
{
    // --------------------------------------------------------------------------------------
    // English
    // --------------------------------------------------------------------------------------
    {LANGID_ENG, ZM_INBOX,            L"Inbox"},
    {LANGID_ENG, ZM_TRASH,            L"Deleted Items"},
    {LANGID_ENG, ZM_SPAM,             L"Junk E-mail"},
    {LANGID_ENG, ZM_SENT_MAIL,        L"Sent Items"},
    {LANGID_ENG, ZM_DRAFTS,           L"Drafts"},
    {LANGID_ENG, ZM_CONTACTS,         L"Contacts"},
    {LANGID_ENG, ZM_CALENDAR,         L"Calendar"},
    {LANGID_ENG, ZM_EMAILEDCONTACTS,  L"Suggested Contacts"},
    {LANGID_ENG, ZM_TASKS,            L"Tasks"},

    {LANGID_ENG, ZM_UNSUPPORTED_OUTBOX,         L"Outbox"},
    {LANGID_ENG, ZM_UNSUPPORTED_SYNC_ISSUES,    L"Sync Issues"},  
    {LANGID_ENG, ZM_UNSUPPORTED_NOTES,          L"Notes"},  

    // --------------------------------------------------------------------------------------
    // Spanish
    // --------------------------------------------------------------------------------------
    {LANGID_SPA, ZM_INBOX,            L"Bandeja de entrada"},
    
    {LANGID_SPA, ZM_TRASH,            L"Papelera"},                 
    {LANGID_SPA, ZM_TRASH,            L"Elementos eliminados"},
    
    {LANGID_SPA, ZM_SPAM,             L"Correo no deseado"},
    {LANGID_SPA, ZM_SPAM,             L"Correo electr\u00f3nico no deseado"}, // From OL2016
    
    {LANGID_SPA, ZM_SENT_MAIL,        L"enviados"},                 
    {LANGID_SPA, ZM_SENT_MAIL,        L"Elementos enviados"},
    
    {LANGID_SPA, ZM_DRAFTS,           L"Borrador"}, // From OL2016
    {LANGID_SPA, ZM_DRAFTS,           L"Borradores"},

    {LANGID_SPA, ZM_CONTACTS,         L"Contactos"},
    
    {LANGID_SPA, ZM_CALENDAR,         L"Agenda"},                   
    {LANGID_SPA, ZM_CALENDAR,         L"Calendario"},
    
    {LANGID_SPA, ZM_EMAILEDCONTACTS,  L"Contactos respondidos"},
    {LANGID_SPA, ZM_TASKS,            L"Tareas"},

    {LANGID_SPA, ZM_UNSUPPORTED_OUTBOX,         L"Bandeja de salida"},
    {LANGID_SPA, ZM_UNSUPPORTED_SYNC_ISSUES,    L"Problemas de sincronizaci\u00f3n"},
    {LANGID_SPA, ZM_UNSUPPORTED_NOTES,          L"Notas"},  


    // --------------------------------------------------------------------------------------
    // Danish
    // --------------------------------------------------------------------------------------
    {LANGID_DAN, ZM_INBOX,            L"Indbakke"},
    
    {LANGID_DAN, ZM_TRASH,            L"Papirkurv"},                            
    {LANGID_DAN, ZM_TRASH,            L"Slettet post"},

    {LANGID_DAN, ZM_SPAM,             L"U\u00f8nsket mail"}, // from OL2016
    {LANGID_DAN, ZM_SPAM,             L"U\u00f8nsket e-mail"},                  
    {LANGID_DAN, ZM_SPAM,             L"U\u00f8nsket"},

    {LANGID_DAN, ZM_SENT_MAIL,        L"Sendt post"},                           
    {LANGID_DAN, ZM_SENT_MAIL,        L"Sendt"},

    {LANGID_DAN, ZM_DRAFTS,           L"Kladder"},
    
    {LANGID_DAN, ZM_CONTACTS,         L"Kontakter"},                            
    {LANGID_DAN, ZM_CONTACTS,         L"Kontaktpersoner"},

    {LANGID_DAN, ZM_CALENDAR,         L"Kalender"},
    {LANGID_DAN, ZM_EMAILEDCONTACTS,  L"Kontakter, der er sendt mail til"},
    {LANGID_DAN, ZM_TASKS,            L"Opgaver"},

    {LANGID_DAN, ZM_UNSUPPORTED_OUTBOX,      L"Udbakke"},
    {LANGID_DAN, ZM_UNSUPPORTED_SYNC_ISSUES, L"Synkroniser udgaver"},
    {LANGID_DAN, ZM_UNSUPPORTED_SYNC_ISSUES, L"Synkroniseringsfejl"},
    {LANGID_DAN, ZM_UNSUPPORTED_NOTES,       L"Notater"},  
    {LANGID_DAN, ZM_UNSUPPORTED_NOTES,       L"Noter"},  

    // --------------------------------------------------------------------------------------
    // German
    // --------------------------------------------------------------------------------------
    {LANGID_GER, ZM_INBOX,            L"Posteingang"},
    
    {LANGID_GER, ZM_TRASH,            L"Gel\u00f6schte Objekte"},         
    {LANGID_GER, ZM_TRASH,            L"Gel\u00f6schte Elemente"},
    
    {LANGID_GER, ZM_SPAM,             L"Junk-E-Mail"},     
    
    {LANGID_GER, ZM_SENT_MAIL,        L"Gesendete Objekte"},              
    {LANGID_GER, ZM_SENT_MAIL,        L"Gesendete Elemente"},
    
    {LANGID_GER, ZM_DRAFTS,           L"Entw\u00fcrfe"},
    {LANGID_GER, ZM_CONTACTS,         L"Kontakte"},  
    {LANGID_GER, ZM_CALENDAR,         L"Kalender"},
    {LANGID_GER, ZM_EMAILEDCONTACTS,  L"Vorgeschlagene Kontakte"},
    {LANGID_GER, ZM_TASKS,            L"Aufgaben"},

    {LANGID_GER, ZM_UNSUPPORTED_OUTBOX,      L"Postausgang"},
    {LANGID_GER, ZM_UNSUPPORTED_SYNC_ISSUES, L"Synchronisationsprobleme"},
    {LANGID_GER, ZM_UNSUPPORTED_NOTES,       L"Notizen"},  

    // --------------------------------------------------------------------------------------
    // French
    // --------------------------------------------------------------------------------------
    {LANGID_FRE, ZM_INBOX,            L"Bo\u00eete de R\u00e9ception"},
    {LANGID_FRE, ZM_INBOX,            L"Bo\u00eete de r\u00e9ception"},

    {LANGID_FRE, ZM_TRASH,            L"\u00c9l\u00e9ments supprim\u00e9s"}, // From OL2016
    {LANGID_FRE, ZM_TRASH,            L"Corbeille"},                      
    {LANGID_FRE, ZM_TRASH,            L"\u00e9ments supprim\u00e9s"},  

    {LANGID_FRE, ZM_SPAM,             L"Spam"},      
    {LANGID_FRE, ZM_SPAM,             L"Courrier ind\u00e9sirable"},          
    {LANGID_FRE, ZM_SPAM,             L"Courrier ind\u00e9sirables"},  

    {LANGID_FRE, ZM_SENT_MAIL,        L"\u00c9l\u00e9ments envoy\u00e9s"}, // From OL2016                    
    {LANGID_FRE, ZM_SENT_MAIL,        L"Envoy\u00e9"},                    
    {LANGID_FRE, ZM_SENT_MAIL,        L"\u00e9l\u00e9ments envoy\u00e9s"},    

    {LANGID_FRE, ZM_DRAFTS,           L"Brouillons"},
    {LANGID_FRE, ZM_CONTACTS,         L"Contacts"},  // From OL2016
    {LANGID_FRE, ZM_CALENDAR,         L"Calendrier"},
    {LANGID_FRE, ZM_EMAILEDCONTACTS,  L"Personnes contact\u00e9es par mail"},
    {LANGID_FRE, ZM_TASKS,            L"T\u00e2ches"},

    {LANGID_FRE, ZM_UNSUPPORTED_OUTBOX,      L"Bo\u00eete d'envoi"},
    {LANGID_FRE, ZM_UNSUPPORTED_SYNC_ISSUES, L"Probl\u00e8mes de synchronisation"},
    {LANGID_FRE, ZM_UNSUPPORTED_NOTES,       L"Remarques"},  

    // --------------------------------------------------------------------------------------
    // Italian
    // --------------------------------------------------------------------------------------
    {LANGID_ITA, ZM_INBOX,            L"In arrivo"},                              
    {LANGID_ITA, ZM_INBOX,            L"Posta in arrivo"},

    {LANGID_ITA, ZM_TRASH,            L"Cestino"},                                
    {LANGID_ITA, ZM_TRASH,            L"Posta eliminata"},  

    {LANGID_ITA, ZM_SPAM,             L"Posta indesiderata"},  

    {LANGID_ITA, ZM_SENT_MAIL,        L"Inviati"},                                
    {LANGID_ITA, ZM_SENT_MAIL,        L"Posta inviata"},  

    {LANGID_ITA, ZM_DRAFTS,           L"Bozze"},
    {LANGID_ITA, ZM_CONTACTS,         L"Contatti"},  

    {LANGID_ITA, ZM_CALENDAR,         L"Agenda"},                                 
    {LANGID_ITA, ZM_CALENDAR,         L"Calendario"},

    {LANGID_ITA, ZM_EMAILEDCONTACTS,  L"Contatti usati per email"},               
    {LANGID_ITA, ZM_EMAILEDCONTACTS,  L"Contatti Email"},

    {LANGID_ITA, ZM_TASKS,            L"Attivit\u00e0"}, // From OL2016
    {LANGID_ITA, ZM_TASKS,            L"Impegni"},                                
    {LANGID_ITA, ZM_TASKS,            L"Attivit\u00e1"},

    {LANGID_ITA, ZM_UNSUPPORTED_OUTBOX,      L"Posta in uscita"},
    {LANGID_ITA, ZM_UNSUPPORTED_SYNC_ISSUES, L"Problemi di sincronizzazione"},
    {LANGID_ITA, ZM_UNSUPPORTED_NOTES,       L"Note"},  

    // --------------------------------------------------------------------------------------
    // Malay
    // --------------------------------------------------------------------------------------
    {LANGID_MAL, ZM_INBOX,            L"Peti Masuk"},

    {LANGID_MAL, ZM_TRASH,            L"Item Terpadam"},  
    {LANGID_MAL, ZM_TRASH,            L"Item Dihapuskan"},  


    {LANGID_MAL, ZM_SPAM,             L"E-mel Remeh"}, // From OL2016
    {LANGID_MAL, ZM_SPAM,             L"E-Mel Sarap"},  

    {LANGID_MAL, ZM_SENT_MAIL,        L"Item Hantar"},  
    {LANGID_MAL, ZM_SENT_MAIL,        L"Skickat"},  

    {LANGID_MAL, ZM_DRAFTS,           L"Draf"},

    {LANGID_MAL, ZM_CONTACTS,         L"Kenalan"}, // From OL2016
    {LANGID_MAL, ZM_CONTACTS,         L"Orang Hubungan"},  

    {LANGID_MAL, ZM_CALENDAR,         L"Kalendar"},
    {LANGID_MAL, ZM_CALENDAR,         L"Kalender"},

    {LANGID_MAL, ZM_EMAILEDCONTACTS,  L"Kenalan Dihantar E-mel"}, // From OL2016
    {LANGID_MAL, ZM_TASKS,            L"Tugas"},

    {LANGID_MAL, ZM_UNSUPPORTED_OUTBOX,      L"Peti Keluar"},
    {LANGID_MAL, ZM_UNSUPPORTED_SYNC_ISSUES, L"Isu Penyegerakan"},
    {LANGID_MAL, ZM_UNSUPPORTED_NOTES,       L"Nota"},  

    // --------------------------------------------------------------------------------------
    // Dutch (Metherlands)
    // --------------------------------------------------------------------------------------
    {LANGID_DUT, ZM_INBOX,            L"Postvak IN"},
    {LANGID_DUT, ZM_TRASH,            L"Verwijderde items"},  
    {LANGID_DUT, ZM_SPAM,             L"Ongewenste e-mail"},  
    {LANGID_DUT, ZM_SENT_MAIL,        L"Verzonden items"},  
    {LANGID_DUT, ZM_DRAFTS,           L"Concepten"},
    {LANGID_DUT, ZM_CONTACTS,         L"Contactpersonen"},  
    {LANGID_DUT, ZM_CALENDAR,         L"Agenda"},
    {LANGID_DUT, ZM_EMAILEDCONTACTS,  L"Voorgestelde contactpersonen"},
    {LANGID_DUT, ZM_TASKS,            L"Taken"},

    {LANGID_DUT, ZM_UNSUPPORTED_OUTBOX,      L"Postvak UIT"},
    {LANGID_DUT, ZM_UNSUPPORTED_SYNC_ISSUES, L"Synchronisatieproblemen"},
    {LANGID_DUT, ZM_UNSUPPORTED_NOTES,       L"Aantekeningen"},  

    // --------------------------------------------------------------------------------------
    // Polish
    // --------------------------------------------------------------------------------------
    {LANGID_POL, ZM_INBOX,            L"Skrzynka odbiorcza"},
    {LANGID_POL, ZM_TRASH,            L"Elementy usuni\u0119te"},  
    {LANGID_POL, ZM_SPAM,             L"Wiadomo\u015bci-\u015bmieci"},  
    {LANGID_POL, ZM_SENT_MAIL,        L"Elementy wys\u0142ane"},  

    {LANGID_POL, ZM_DRAFTS,           L"Wersje robocze"}, // From OL2016
    {LANGID_POL, ZM_DRAFTS,           L"Kopie robocze"},

    {LANGID_POL, ZM_CONTACTS,         L"Kontakty"},  
    {LANGID_POL, ZM_CALENDAR,         L"Kalendarz"},
    {LANGID_POL, ZM_EMAILEDCONTACTS,  L"Sugerowane kontakty"},
    {LANGID_POL, ZM_TASKS,            L"Zadania"},

    {LANGID_POL, ZM_UNSUPPORTED_OUTBOX,      L"Skrzynka nadawcza"},
    {LANGID_POL, ZM_UNSUPPORTED_SYNC_ISSUES, L"Problemy z synchronizacj\u0105"},
    {LANGID_POL, ZM_UNSUPPORTED_NOTES,       L"Notatki"},  

    // --------------------------------------------------------------------------------------
    // Portugese
    // --------------------------------------------------------------------------------------
    {LANGID_POR, ZM_INBOX,            L"A receber"}, // OL2016
    {LANGID_POR, ZM_INBOX,            L"Entrada"},                      
    {LANGID_POR, ZM_INBOX,            L"A Receber"},

    {LANGID_POR, ZM_TRASH,            L"Itens eliminados"}, // OL2016
    {LANGID_POR, ZM_TRASH,            L"Lixeira"},                      
    {LANGID_POR, ZM_TRASH,            L"Itens Eliminados"},

    {LANGID_POR, ZM_SPAM,             L"Lixo"}, // OL2016                 
    {LANGID_POR, ZM_SPAM,             L"Spam"},                         
    {LANGID_POR, ZM_SPAM,             L"Correio Electr\u00f3nico N\u00e3o Solicitado"}, 

    {LANGID_POR, ZM_SENT_MAIL,        L"Itens enviados"}, // OL2016
    {LANGID_POR, ZM_SENT_MAIL,        L"Enviadas"},                     
    {LANGID_POR, ZM_SENT_MAIL,        L"Itens Enviados"},

    {LANGID_POR, ZM_DRAFTS,           L"Rascunhos"},
    {LANGID_POR, ZM_CONTACTS,         L"Contactos"},

    {LANGID_POR, ZM_CALENDAR,         L"Agenda"},                       
    {LANGID_POR, ZM_CALENDAR,         L"Calend\u00e1rio"},

    {LANGID_POR, ZM_EMAILEDCONTACTS,  L"Contatos que receberam e-mail"},

    {LANGID_POR, ZM_TASKS,            L"Tarefas"},

    {LANGID_POR, ZM_UNSUPPORTED_OUTBOX,      L"A enviar"},
    {LANGID_POR, ZM_UNSUPPORTED_SYNC_ISSUES, L"Problemas de sincroniza\u00e7\u00e3o"},
    {LANGID_POR, ZM_UNSUPPORTED_NOTES,       L"Observa\u00e7\u00f5es"},  

    // --------------------------------------------------------------------------------------
    // Portugese (Brazil)
    // --------------------------------------------------------------------------------------
    {LANGID_PTB, ZM_INBOX,            L"Caixa de entrada"},
    {LANGID_PTB, ZM_TRASH,            L"Itens Exclu\u00eddos"},


    {LANGID_PTB, ZM_SPAM,             L"Lixo eletr\u00f4nico"}, // From OL2016
    {LANGID_PTB, ZM_SPAM,             L"Lixo Eletr\u00f4nico"},  

    {LANGID_PTB, ZM_SENT_MAIL,        L"Itens enviados"}, // From OL2016
    {LANGID_PTB, ZM_SENT_MAIL,        L"Mensagens enviadas"},

    {LANGID_PTB, ZM_DRAFTS,           L"Rascunhos"},

    {LANGID_PTB, ZM_CONTACTS,         L"Contatos"}, // From OL2016
    {LANGID_PTB, ZM_CONTACTS,         L"Contactos"},

    {LANGID_PTB, ZM_CALENDAR,         L"Calend\u00e1rio"},
    {LANGID_PTB, ZM_EMAILEDCONTACTS,  L"Contatos que receberam e-mail"},
    {LANGID_PTB, ZM_TASKS,            L"Tarefas"},

    {LANGID_PTB, ZM_UNSUPPORTED_OUTBOX,      L"Caixa de sa\u00edda"},
    {LANGID_PTB, ZM_UNSUPPORTED_SYNC_ISSUES, L"Problemas de sincroniza\u00e7\u00e3o"},
    {LANGID_PTB, ZM_UNSUPPORTED_NOTES,       L"Observa\u00e7\u00f5es"},  

    // --------------------------------------------------------------------------------------
    // Romanian
    // --------------------------------------------------------------------------------------
    {LANGID_ROM, ZM_INBOX,            L"Inbox"}, // From OL2016
    {LANGID_ROM, ZM_TRASH,            L"Elemente \u0219terse"},

    {LANGID_ROM, ZM_SPAM,             L"E-mail nedorit"}, // From OL2016
    {LANGID_ROM, ZM_SPAM,             L"Po\u0219t\u0103 electronic\u0103 nedorit\u0103"},  

    {LANGID_ROM, ZM_SENT_MAIL,        L"Elemente trimise"},
    {LANGID_ROM, ZM_DRAFTS,           L"Schi\u021be"},
    {LANGID_ROM, ZM_CONTACTS,         L"Persoane de contact"},
    {LANGID_ROM, ZM_CALENDAR,         L"Calendar"}, // From OL2016
    {LANGID_ROM, ZM_EMAILEDCONTACTS,  L"Contacte destinatare"}, // From OL2016
    {LANGID_ROM, ZM_TASKS,            L"Activit\u0103\u021bi"},

    {LANGID_ROM, ZM_UNSUPPORTED_OUTBOX,      L"Outbox"},
    {LANGID_ROM, ZM_UNSUPPORTED_SYNC_ISSUES, L"Probleme de sincronizare"},
    {LANGID_ROM, ZM_UNSUPPORTED_NOTES,       L"Note"},  

    // --------------------------------------------------------------------------------------
    // Swedish
    // --------------------------------------------------------------------------------------
    {LANGID_SWE, ZM_INBOX,            L"Inkorgen"},
    {LANGID_SWE, ZM_TRASH,            L"Borttaget"},
    {LANGID_SWE, ZM_SPAM,             L"Skr\u00e4ppost"},  
    {LANGID_SWE, ZM_SENT_MAIL,        L"Skickat"},
    {LANGID_SWE, ZM_DRAFTS,           L"Utkast"},
    {LANGID_SWE, ZM_CONTACTS,         L"Kontakter"},
    {LANGID_SWE, ZM_CALENDAR,         L"Kalender"},
    {LANGID_SWE, ZM_EMAILEDCONTACTS,  L"E-postkontakter"}, // From OL2016
    {LANGID_SWE, ZM_TASKS,            L"Uppgifter"},

    {LANGID_SWE, ZM_UNSUPPORTED_OUTBOX,      L"Utkorgen"},
    {LANGID_SWE, ZM_UNSUPPORTED_SYNC_ISSUES, L"Synkroniseringsproblem"},
    {LANGID_SWE, ZM_UNSUPPORTED_NOTES,       L"Anteckningar"},  

    // --------------------------------------------------------------------------------------
    // Slovenian
    // --------------------------------------------------------------------------------------
    {LANGID_SLV, ZM_INBOX,            L"Prejeto"},
    {LANGID_SLV, ZM_TRASH,            L"Izbrisano"},
    {LANGID_SLV, ZM_SPAM,             L"Ne\u017eelena e-po\u0161ta"},  // From DJP OL2016
    {LANGID_SLV, ZM_SENT_MAIL,        L"Poslano"},

    {LANGID_SLV, ZM_DRAFTS,           L"Odpo\u015dlji"},                    
    {LANGID_SLV, ZM_DRAFTS,           L"Osnutki"},

    {LANGID_SLV, ZM_CONTACTS,         L"Stiki"},
    {LANGID_SLV, ZM_CALENDAR,         L"Koledar"},
    {LANGID_SLV, ZM_EMAILEDCONTACTS,  L"po e-po\u015dti stiki"},
    {LANGID_SLV, ZM_TASKS,            L"Opravila"},

    {LANGID_SLV, ZM_UNSUPPORTED_OUTBOX,      L"Odpo\u0161lji"},
    {LANGID_SLV, ZM_UNSUPPORTED_SYNC_ISSUES, L"Te\u017eave s sinhronizacijo"},
    {LANGID_SLV, ZM_UNSUPPORTED_NOTES,       L"Bele\u017eke"},  

    // --------------------------------------------------------------------------------------
    // Slovak
    // --------------------------------------------------------------------------------------
    {LANGID_SLK, ZM_INBOX,            L"Doru\u010den\u00e1 po\u0161ta"},
    {LANGID_SLK, ZM_TRASH,            L"Odstr\u00e1nen\u00e9 polo\u017eky"},
    {LANGID_SLK, ZM_SPAM,             L"Nevy\u017eiadan\u00e1 po\u0161ta"},  
    {LANGID_SLK, ZM_SENT_MAIL,        L"Odoslan\u00e1 po\u0161ta"},
    {LANGID_SLK, ZM_DRAFTS,           L"Koncepty"},
    {LANGID_SLK, ZM_CONTACTS,         L"Kontakty"},
    {LANGID_SLK, ZM_CALENDAR,         L"Kalend\u00e1r"},
    {LANGID_SLK, ZM_EMAILEDCONTACTS,  L"Kontaktovan\u00e9 osoby"}, // Taken from ZmMsg_sk.properties(20)
    {LANGID_SLK, ZM_TASKS,            L"\u00dalohy"},

    {LANGID_SLK, ZM_UNSUPPORTED_OUTBOX,      L"Po\u0161ta na odoslanie"},
    {LANGID_SLK, ZM_UNSUPPORTED_SYNC_ISSUES, L"Probl\u00e9my so synchroniz\u00e1ciou"},
    {LANGID_SLK, ZM_UNSUPPORTED_NOTES,       L"Pozn\u00e1mky"},  

    // --------------------------------------------------------------------------------------
    // Russian
    // --------------------------------------------------------------------------------------
    {LANGID_RUS, ZM_INBOX,            L"\u0412\u0445\u043e\u0434\u044f\u0449\u0438\u0435"},
    {LANGID_RUS, ZM_TRASH,            L"\u0423\u0434\u0430\u043b\u0435\u043d\u043d\u044b\u0435"},
    {LANGID_RUS, ZM_SPAM,             L"\u041d\u0435\u0436\u0435\u043b\u0430\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u043f\u043e\u0447\u0442\u0430"},  
    {LANGID_RUS, ZM_SENT_MAIL,        L"\u041e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043d\u044b\u0435"},
    {LANGID_RUS, ZM_DRAFTS,           L"\u0427\u0435\u0440\u043d\u043e\u0432\u0438\u043a\u0438"},
    {LANGID_RUS, ZM_CONTACTS,         L"\u041a\u043e\u043d\u0442\u0430\u043a\u0442\u044b"},

    {LANGID_RUS, ZM_CALENDAR,         L"\u041a\u0430\u043b\u0435\u043d\u0434\u0430\u0440\u044c"},               
    {LANGID_RUS, ZM_CALENDAR,         L"\u0415\u0436\u0435\u0434\u043d\u0435\u0432\u043d\u0438\u043a"},

    {LANGID_RUS, ZM_EMAILEDCONTACTS,  L"\u041e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043d\u044b\u0435 \u043f\u043e \u044d\u043b. \u043f\u043e\u0447\u0442\u0435 \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u044b"}, // Taken from ZmMsg_ru.properties(20)
    {LANGID_RUS, ZM_TASKS,            L"\u0417\u0430\u0434\u0430\u0447\u0438"},

    {LANGID_RUS, ZM_UNSUPPORTED_OUTBOX,      L"\u0418\u0441\u0445\u043e\u0434\u044f\u0449\u0438\u0435"},
    {LANGID_RUS, ZM_UNSUPPORTED_SYNC_ISSUES, L"\u041f\u0440\u043e\u0431\u043b\u0435\u043c\u044b \u0441\u0438\u043d\u0445\u0440\u043e\u043d\u0438\u0437\u0430\u0446\u0438\u0438"},
    {LANGID_RUS, ZM_UNSUPPORTED_NOTES,       L"\u0417\u0430\u043c\u0435\u0442\u043a\u0438"},  

    // --------------------------------------------------------------------------------------
    // Simplified Chinese (CN)
    // --------------------------------------------------------------------------------------
    {LANGID_CHI, ZM_INBOX,            L"\u6536\u4ef6\u7bb1"},
    {LANGID_CHI, ZM_TRASH,            L"\u5df2\u5220\u9664\u90ae\u4ef6"},
    {LANGID_CHI, ZM_SPAM,             L"\u5783\u573e\u90ae\u4ef6"},  
    {LANGID_CHI, ZM_SENT_MAIL,        L"\u5df2\u53d1\u9001\u90ae\u4ef6"},

    {LANGID_CHI, ZM_DRAFTS,           L"\u8349\u7a3f"}, // From DJP from OL2016
    {LANGID_CHI, ZM_DRAFTS,           L"\u8349\u7a3f\u7bb1"},

    {LANGID_CHI, ZM_CONTACTS,         L"\u8054\u7cfb\u4eba"},

    {LANGID_CHI, ZM_CALENDAR,         L"\u65e5\u5386"}, // From DJP (taken from OL2016)
    {LANGID_CHI, ZM_CALENDAR,         L"\u884c\u4e8b\u5386"},

    {LANGID_CHI, ZM_EMAILEDCONTACTS,  L"\u7535\u5b50\u90ae\u4ef6\u8054\u7cfb\u4eba"},
    {LANGID_CHI, ZM_TASKS,            L"\u4efb\u52a1"},

    {LANGID_CHI, ZM_UNSUPPORTED_OUTBOX,      L"\u53d1\u4ef6\u7bb1"},
    {LANGID_CHI, ZM_UNSUPPORTED_SYNC_ISSUES, L"\u540c\u6b65\u95ee\u9898"},
    {LANGID_CHI, ZM_UNSUPPORTED_NOTES,       L"\u5907\u6ce8"},  

    // --------------------------------------------------------------------------------------
    // Norwegian
    // --------------------------------------------------------------------------------------
    {LANGID_NOR, ZM_INBOX,            L"Innboks"},
    {LANGID_NOR, ZM_TRASH,            L"Slettede elementer"},

    {LANGID_NOR, ZM_SPAM,             L"S\u00f8ppelpost"},
    {LANGID_NOR, ZM_SPAM,             L"Posta S\u00f8ppelpost"},

    {LANGID_NOR, ZM_SENT_MAIL,        L"Sendte elementer"},
    {LANGID_NOR, ZM_DRAFTS,           L"Kladd"},
    {LANGID_NOR, ZM_CONTACTS,         L"Kontakter"},
    {LANGID_NOR, ZM_CALENDAR,         L"Kalender"},
    {LANGID_NOR, ZM_EMAILEDCONTACTS,  L"Foresl\u00e5tte kontakter"},
    {LANGID_NOR, ZM_TASKS,            L"Oppgaver"},

    {LANGID_NOR, ZM_UNSUPPORTED_OUTBOX,      L"Utboks"},
    {LANGID_NOR, ZM_UNSUPPORTED_SYNC_ISSUES, L"Synkroniseringsfeil"},
    {LANGID_NOR, ZM_UNSUPPORTED_NOTES,       L"Notater"},  
};
#pragma warning(default:4428)


void COMMapiAccount::RemoveUnsupportedSpecialFolders(vector<Folder_Data>& vFolders)
{
    LOGFN_TRACE_NO;

    size_t NumFoldersBefore = vFolders.size(); 

    // This is used to ensure that we remove children of a skipped folder
    // See algorithm below. The algorithm assumes vFolders is in depth-first order 
    ULONG ulSkipBelowDepth = ULONG_MAX;

    std::vector<Folder_Data>::iterator it; 
    for (it = vFolders.begin(); it!=vFolders.end();)
    {
        if (it->ulDepth > ulSkipBelowDepth)
        {
            LOG_TRACE(_T("Skipping '%s' at depth %d because it's a child of an unsupported folder"), it->name.c_str(), it->ulDepth);
            it = vFolders.erase(it);
            continue;
        }
        else
        if (it->ulDepth == ulSkipBelowDepth)
        {
            // We've passed all children of the skipped folder - turn off the depth filter
            ulSkipBelowDepth = ULONG_MAX; 
        }

        // ----------------------------------------------
        // Skip unsupported folder
        // ----------------------------------------------
        if (it->zimbraspecialfolderid >= ZM_UNSUPPORTED_OUTBOX)
        {
            LOG_TRACE(_T("Skipping unsupported folder '%s' at depth %d"), it->name.c_str(), it->ulDepth);

            // Set ulSkipBelowDepth to the depth of the unsupported folder
            // This will cause all subsequent folders in the list to be skipped if
            // their depth exceeds this i.e. if they are child folders
            ulSkipBelowDepth = it->ulDepth;

            it = vFolders.erase(it);
            continue;
        }
        it++;
    }

    if (NumFoldersBefore != vFolders.size())
    {
        // ==========================================================
        // Log folders after skipping
        // ==========================================================
        LOG_GEN_SUMMARY(_T(" "));
        LOG_GEN_SUMMARY(_T("====== Folders after removing unsupported (%d removed) ==="), NumFoldersBefore-vFolders.size());
        LogFolders(vFolders);
    }
}


void COMMapiAccount::AddMissingSFIDs(vector<Folder_Data>& vFolders)
//
// vFolders is a list of discovered folders.
// Some of them will have zimbraspecialfolderid set because earlier we found exch special folder entryIDs on root/inbox
// But for an Archive PST migration, these will typically be missing.
// This method adds in the missing ones by matching on folder display names
// This gets tricky for localized archive psts because there is nothing in the PST to say what the language is
// Therefore we have to heuristically determine the language.
// We do this by walking the depth 2 folders and totalling up how many display name matches we get in each language
// The one with the highest score is the one we use
{
    LOGFN_TRACE_NO;
   
    // ===========================================================================
    // See if we've already found special folders via EntryID method
    // ===========================================================================
    LOG_GEN_SUMMARY(_T(" "));
    LOG_GEN_SUMMARY(_T("Checking which special folders were found via EID method..."));

    ULONG ulNumSpecialFoldersViaEID = 0;
    ULONG ulNumDepth2Folders = 0;
    CountSFs(vFolders, ulNumSpecialFoldersViaEID, ulNumDepth2Folders);

    if (ulNumSpecialFoldersViaEID >= 8)
    {
        LOG_GEN_SUMMARY(_T("Already found all special via EID method"));
        return;
    }
    else
        LOG_GEN_SUMMARY(_T("EID method found only %d special folder(s) among %d top-level IPM folder(s) -> search for more via display name match"), ulNumSpecialFoldersViaEID, ulNumDepth2Folders);

    // ===========================================================================
    // Scan the special (depth 2) folders and calc match scores
    // ===========================================================================

    // Initialize the per-langauge scores to zero
    struct LangScore
    {
        wstring sLang;
        int     LangID;
        int     nScore;
        wstring sMatches;
    };
    LangScore gaLangScores[g_NUM_LANGS] = 
    {
        {L"English      ",  LANGID_ENG, 0, L""},
        {L"Spanish      ",  LANGID_SPA, 0, L""},
        {L"Danish       ",  LANGID_DAN, 0, L""},
        {L"German       ",  LANGID_GER, 0, L""},
        {L"French       ",  LANGID_FRE, 0, L""},
        {L"Italian      ",  LANGID_ITA, 0, L""},
        {L"Malay        ",  LANGID_MAL, 0, L""},
        {L"Dutch        ",  LANGID_DUT, 0, L""},
        {L"Polish       ",  LANGID_POL, 0, L""},
        {L"Portugese    ",  LANGID_POR, 0, L""},
        {L"Portugese Br ",  LANGID_PTB, 0, L""},
        {L"Romainian    ",  LANGID_ROM, 0, L""},
        {L"Swedish      ",  LANGID_SWE, 0, L""},
        {L"Slovenian    ",  LANGID_SLV, 0, L""},
        {L"Slovak       ",  LANGID_SLK, 0, L""},
        {L"Russian      ",  LANGID_RUS, 0, L""},
        {L"Chinese      ",  LANGID_CHI, 0, L""},
        {L"Norwegian    ",  LANGID_NOR, 0, L""},
    };

    size_t nNumFolders = vFolders.size();
    std::vector<Folder_Data>::iterator it = vFolders.begin();
    for (size_t i = 0; i < nNumFolders; i++, it++)
    {
        wstring sFoldName = it->name.c_str();
        if (it->ulDepth == 2)
        {
            // Walk aLocalizedSFNames looking for display name match
            for (int i=0; i<g_NUM_LOCALIZED_SPECIAL_FOLDER_NAMES; i++)
            {
                if (sFoldName.compare(gaLocalizedSFNames[i].sName.c_str()) == 0)
                {
                    // Matches - increment the count for that lang in the lang-hit-totalizer
                    int nLangId = gaLocalizedSFNames[i].nLangID;
                    for (int j=0; j<g_NUM_LANGS; j++)
                    {
                        if (gaLangScores[j].LangID == nLangId)
                        {
                            gaLangScores[j].nScore++;

                            // Add to sMatches
                            if (gaLangScores[j].sMatches.size())
                                gaLangScores[j].sMatches += L", ";
                            wstring s = GetZimSFStr(gaLocalizedSFNames[i].nZSFID);
                            gaLangScores[j].sMatches += (s + L":" + sFoldName);

                            break;
                        }
                    }

                    // continue - might match multiple langs
                }
            } // for
        } //if (it->ulDepth == 2)
    }

    // ================================================================
    // Find the language with the highest score
    // ================================================================
    LOG_GEN_SUMMARY(_T(" "));
    LOG_GEN_SUMMARY(_T("======================= Language Scores ========================="));
    int idx = -1;
    int nMaxScore = -1;
    for (int j=0; j<g_NUM_LANGS; j++)
    {
        // Log results
        wstring sMatches;
        if (gaLangScores[j].nScore == 1)
            sMatches = L"match  ";
        else
            sMatches = L"matches";
        if (gaLangScores[j].nScore)
            sMatches = sMatches + L" ("+gaLangScores[j].sMatches+L")";

        LOG_GEN_SUMMARY(_T(" %s %d   %2d %s"), gaLangScores[j].sLang.c_str(), gaLangScores[j].LangID, gaLangScores[j].nScore, sMatches.c_str());

        if (gaLangScores[j].nScore > nMaxScore)
        {
            nMaxScore = gaLangScores[j].nScore;
            idx = j;
        }
    }
    int nBestMatchLangID = gaLangScores[idx].LangID;
    LOG_GEN_SUMMARY(_T(" "));
    LOG_GEN_SUMMARY(_T(" -> Best language match is %d %s"), nBestMatchLangID, gaLangScores[idx].sLang.c_str());



    // ================================================================
    // Look for special folder name matches for the selected language
    // ================================================================
    // Walk it again matching on display names from that language and set SFID
    it = vFolders.begin();
    for (size_t i = 0; i < nNumFolders; i++, it++)
    {
        wstring sFoldName = it->name.c_str();
        if (it->ulDepth == 2)
        {
            if (it->zimbraspecialfolderid == 0) // not already got a sfid
            {
                // Find start of entries for language
                bool bFoundMatchingLangID = false;
                for (int i=0; i<g_NUM_LOCALIZED_SPECIAL_FOLDER_NAMES; i++)
                {
                    if (gaLocalizedSFNames[i].nLangID != nBestMatchLangID)
                    {
                        if (!bFoundMatchingLangID)
                            continue; 
                        else
                            break;
                    }
                                                                                        // TODO jump out when exhausted them
                    bFoundMatchingLangID = true;

                    if (sFoldName.compare(gaLocalizedSFNames[i].sName.c_str())==0)
                    {
                        it->zimbraspecialfolderid = gaLocalizedSFNames[i].nZSFID;
                        break;
                    }
                }
            }
        }
    }

    // ================================================================
    // Log a warning if didn't find all 8 special folders
    // ================================================================
    ULONG ulNumSpecialFoldersAfterDisplayNames = 0;
    ulNumDepth2Folders = 0;
    CountSFs(vFolders, ulNumSpecialFoldersAfterDisplayNames, ulNumDepth2Folders);
    if (ulNumSpecialFoldersAfterDisplayNames < 8)
    {
        LOG_WARNING(_T("Some special folders are missing (only %d found)"), ulNumSpecialFoldersAfterDisplayNames);
    }

    // ==========================================================
    // Log folders with language-driven SFIDs
    // ==========================================================
    LOG_GEN_SUMMARY(_T(" "));
    LOG_GEN_SUMMARY(_T("====== Folders with displayname-driven SFIDs ==="));
    LogFolders(vFolders);
}

wstring COMMapiAccount::GetZimSFStr(long lZimSFID)
{
    wstring s;
    switch(lZimSFID)
    {
        case ZM_ROOT:               s = _T("ROOT");                break;
        case ZM_MAILBOX_ROOT:       s = _T("MAILBOX_ROOT");        break;

        case ZM_INBOX:              s = _T("INBOX");               break;
        case ZM_TRASH:              s = _T("TRASH");               break; 
        case ZM_SPAM:               s = _T("JUNK");                break;
        case ZM_SENT_MAIL:          s = _T("SENT_MAIL");           break;
        case ZM_DRAFTS:             s = _T("DRAFTS");              break;
        case ZM_CONTACTS:           s = _T("CONTACTS");            break;
        case ZM_CALENDAR:           s = _T("CALENDAR");            break;
        case ZM_EMAILEDCONTACTS:    s = _T("EMAILEDCONTACTS");     break;
        case ZM_TASKS:              s = _T("TASKS");               break;

        case ZM_UNSUPPORTED_OUTBOX:       s = _T("OUTBOX");        break;
        case ZM_UNSUPPORTED_SYNC_ISSUES:  s = _T("SYNC_ISSUES");   break;
        case ZM_UNSUPPORTED_NOTES:        s = _T("NOTES");         break;

        default: _ASSERT(FALSE);
    }
    return s;
}

void COMMapiAccount::CountSFs(vector<Folder_Data>& vFolders, ULONG& ulNumSpecialFolders, ULONG& ulNumDepth2Folders)
{
    // Count SFs among Depth 2 folders
    ulNumSpecialFolders = 0;
    ulNumDepth2Folders = 0;

    LOG_GEN_SUMMARY(_T(" "));
    LOG_GEN_SUMMARY(_T("Special folders"));
    LOG_GEN_SUMMARY(_T("==============="));

    size_t count = 0;
    size_t size = vFolders.size();
    std::vector<Folder_Data>::iterator it = vFolders.begin();
    for (size_t i = 0; i < size; i++, it++)
    {
        if (it->ulDepth == 2)
        {
            ulNumDepth2Folders++;
            if (it->zimbraspecialfolderid != 0)
            {
                wstring sSFStr = GetZimSFStr(it->zimbraspecialfolderid);
                Zimbra::MAPI::Util::PadWithSpaces(sSFStr, 30);

                wstring sFoldName = it->name.c_str();
                LOG_GEN_SUMMARY(_T("%2d %s %s"), ++count, sSFStr.c_str(), sFoldName.c_str());

                ulNumSpecialFolders++;
            }
        }
    }
    LOG_GEN_SUMMARY(_T(" -> %d special folder(s) found in %d top-level folder(s)"), ulNumSpecialFolders, ulNumDepth2Folders);
    LOG_GEN_SUMMARY(_T(" "));
}

void COMMapiAccount::LogFolders(vector<Folder_Data>& vFolders)
{
    size_t size = vFolders.size();
    std::vector<Folder_Data>::iterator it = vFolders.begin();
    for (size_t i = 0; i < size; i++, it++)
    {
        // --------------------------------------
        // Log the folder details
        // --------------------------------------
        ULONG ulDepth = it->ulDepth;
        wstring sDepthPadding(ulDepth, L'-');
        wstring sFoldName = sDepthPadding+(it->name.c_str());

        // DCB_BUG 103285 Testing - 70 chars (Max folder length on Exchange seems to be 255 chars)
        //sFoldName = _T("0123456789012345678901234567890123456789012345678901234567890123456789"); 

        Zimbra::MAPI::Util::PadWithSpaces(sFoldName, 60);

        wstring sItems = it->itemcount==1?L"item ":L"items";

        long sfid = it->zimbraspecialfolderid;
        if (!sfid)
        {
            // Not a special folder
            LOG_GEN_SUMMARY(_T(" %s [%5d %s]"), sFoldName.c_str(), it->itemcount, sItems.c_str());
        }
        else
        {
            // It's a special folder
            wstring s = GetZimSFStr(sfid);

            LOG_GEN_SUMMARY(_T(" %s [%5d %s] (Maps to ZCS special folder '%s' (ID:%d))"), sFoldName.c_str(), it->itemcount, sItems.c_str(), s.c_str(), sfid);
        }
    }
}

STDMETHODIMP COMMapiAccount::GetFolders(VARIANT *folders)
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;

    LOG_TRACE(_T("======================================================================================="));
    LOG_INFO( _T("Getting folder list from source store"));
    LOG_TRACE(_T("======================================================================================="));

    VariantInit(folders);
    folders->vt = VT_ARRAY | VT_DISPATCH;

    USES_CONVERSION;

    /*
    // DCB Test migration SUMMARY log
    LOG_SUMMARY(_T("Getting folder list"));
    int i = 0;
    int j = 1/i;
    (void)j;*/

    // ======================================================================================================
    // Get folders into "vFolders" by walking folder hierarchy
    // ======================================================================================================
    vector<Folder_Data> vFolders;
    LPCWSTR lpStatus= m_pMapiAccount->GetRootFolderHierarchy(vFolders);
    if (lpStatus != NULL )
    {
        LOG_ERROR(_T("GetRootFolderHierarchy failed '%s'"), lpStatus);
        folders->parray = NULL;
        hr = S_FALSE;
        return hr;
    }

    size_t size = vFolders.size();
    LOG_GEN_SUMMARY(_T(" "));
    if (size == 0)
    {
        LOG_ERROR(_T("No folders found"));
        hr = S_OK;
        return hr;
    }
    else
    {
        LOG_GEN_SUMMARY(_T("============================================================================="));
        LOG_GEN_SUMMARY(_T("Found %d folders"), size);
        LOG_GEN_SUMMARY(_T("============================================================================="));
    }

    // ----------------------------------------------------------
    // Log folders with EID-driven SFIDs
    // ----------------------------------------------------------
    LOG_GEN_SUMMARY(_T(" "));
    LOG_GEN_SUMMARY(_T("====== Folders with EID-driven SFIDs ========"));
    LogFolders(vFolders);

    // ======================================================================================================
    // Supplement any missing special folder ids
    // ======================================================================================================
    // Only do this for PSTs for 2 reasons:
    // - we're only trying to address the issue of missing SF EIDs - and these should only happen in archive PSTs
    // - We don't want to do this for public folders, because we expect no Special Folders in public folders hierarchy
    Zimbra::MAPI::MIG_TYPE mt = CSessionGlobal::GetMigrationType();
    if ((mt == MIGTYPE_USER_PST) || (mt == MIGTYPE_USER_PROFILE))
        AddMissingSFIDs(vFolders);

    // ======================================================================================================
    // Drop unsupported folders e.g. Outbox
    // ======================================================================================================
    RemoveUnsupportedSpecialFolders(vFolders); // NOOP for Public Folders since these will all have zimbraspecialfolderid == 0




    // ======================================================================================================
    // Transfer vFolders to "folders" out param
    // ======================================================================================================

    // Create a safe array of folders
    size = vFolders.size();
    SAFEARRAYBOUND bounds = { (ULONG)size, 0 };
    SAFEARRAY *psa = SafeArrayCreate(VT_DISPATCH, 1, &bounds);
    IFolderObject **pfolders;
    SafeArrayAccessData(psa, (void **)&pfolders);

    std::vector<Folder_Data>::iterator it = vFolders.begin();
    for (size_t i = 0; i < size; i++, it++)
    {
        // --------------------------------------
        // Create a IFolderObject for this folder
        // --------------------------------------
        CComPtr<IFolderObject> pIFolderObject;
        hr = CoCreateInstance(CLSID_FolderObject, NULL, CLSCTX_ALL, IID_IFolderObject, reinterpret_cast<void **>(&pIFolderObject));
        if (SUCCEEDED(hr))
        {
            // --------------------------------------
            // Set its attributes
            // --------------------------------------

            // Name
            CComBSTR bstrName(it->name.c_str());
            pIFolderObject->put_Name(SysAllocString(bstrName));

            // ZimbraSpecialFolderId
            pIFolderObject->put_ZimbraSpecialFolderId(it->zimbraspecialfolderid);

            // Folder Path
            CComBSTR bstrPath(it->folderpath.c_str());
            pIFolderObject->put_FolderPath(SysAllocString(bstrPath));

            // Container class
            CComBSTR bstrClass(it->containerclass.c_str());
            pIFolderObject->put_ContainerClass(SysAllocString(bstrClass));

            // Item count
            pIFolderObject->put_ItemCount(it->itemcount);

            // Folder EID
            VARIANT var;
            SBinary Folderid = it->sbin;
            VariantInit(&var);
            var.vt = VT_ARRAY | VT_UI1;
            SAFEARRAYBOUND rgsabound[1];
            rgsabound[0].cElements = Folderid.cb;
            rgsabound[0].lLbound = 0;
            var.parray = SafeArrayCreate(VT_UI1, 1, rgsabound);
            if (var.parray != NULL)
            {
                void *pArrayData = NULL;
                SafeArrayAccessData(var.parray, &pArrayData);
                memcpy(pArrayData, Folderid.lpb, Folderid.cb); // Copy data to it
                SafeArrayUnaccessData(var.parray);
            }
            else
                LOG_ERROR(_T("SafeArrayCreate failed"));

            pIFolderObject->put_EID(var);
        }

        if (FAILED(hr))
        {
            LOG_ERROR(_T("Failed %08X"), hr);
            return S_FALSE;
        }

        // --------------------------------------
        // Copy the folder obj into the array
        // --------------------------------------
        pIFolderObject.CopyTo(&pfolders[i]);

    } // for (walk folders)
    LOG_INFO(_T(" "));

    SafeArrayUnaccessData(psa);
    folders->parray = psa;

    return S_OK;
}

STDMETHODIMP COMMapiAccount::GetFolderItems(IFolderObject *FolderObj, VARIANT vCreationDate, VARIANT *vItems)
{
    LOGFN_TRACE_NO;

    BSTR b;
    HRESULT hr = FolderObj->get_FolderPath(&b);

    LOG_TRACE(_T("---------------------------------------------------------------------------------------"));
    LOG_TRACE(_T("Getting all items from folder '%s'..."), b);
    LOG_TRACE(_T("---------------------------------------------------------------------------------------"));

    VariantInit(vItems);
    vItems->vt = VT_ARRAY | VT_DISPATCH;
    SAFEARRAY *psa;

    // ==================================================================================
    // Get folder's items into vItemDataList
    // ==================================================================================
    vector<Item_Data> vItemDataList;

    SBinary folderEntryid;
    folderEntryid.cb = 0;
    folderEntryid.lpb = NULL;

    USES_CONVERSION;

    VARIANT vararg;
    VariantInit(&vararg);
    vararg.vt = (VT_ARRAY | VT_UI1);

    FolderObj->get_EID(&vararg);
    if (vararg.vt == (VT_ARRAY | VT_UI1))       // (OLE SAFEARRAY)
    {
        // Retrieve size of array
        folderEntryid.cb = vararg.parray->rgsabound[0].cElements;
        ULONG size = folderEntryid.cb;

        folderEntryid.lpb = new BYTE[size];     // Allocate a buffer to store the data
        if (folderEntryid.lpb != NULL)
        {
            void *pArrayData;
            SafeArrayAccessData(vararg.parray, &pArrayData);
            memcpy(folderEntryid.lpb, pArrayData, size);        
            SafeArrayUnaccessData(vararg.parray);

            LPCWSTR lpStatus = m_pMapiAccount->GetFolderItems(folderEntryid, vItemDataList);
            if (lpStatus != NULL)
            {
                LOG_ERROR(_T("GetFolderItems failed '%s'"), lpStatus);
                hr = S_FALSE;
                return hr;
           }
        }
        else
            LOG_ERROR(_T("GetFolderItems folderEntryid is null"));
    }

    size_t size = vItemDataList.size();
    if (size == 0)
    {
        LOG_ERROR(_T("GetFolderItems returned no folders"));
        hr = S_OK;
        return hr;
    }
    else
        LOG_INFO(_T("Returned %d item(s)"), size);


    // ==================================================================================
    // Transfer the folder items from vItemDataList to OUT param "vItems"
    // ==================================================================================
    SAFEARRAYBOUND bounds = { (ULONG)size, 0 };
    psa = SafeArrayCreate(VT_DISPATCH, 1, &bounds);
    IItemObject **pItems;
    SafeArrayAccessData(psa, (void **)&pItems);

    vector<Item_Data>::iterator it = vItemDataList.begin();
    for (size_t i = 0; i < size; i++, it++)
    {
        // -------------------------
        // Create pIItemObject
        // -------------------------
        CComPtr<IItemObject> pIItemObject;
        hr = CoCreateInstance(CLSID_ItemObject, NULL, CLSCTX_ALL, IID_IItemObject, reinterpret_cast<void **>(&pIItemObject));
        if (SUCCEEDED(hr))
        {
            // -----------------
            // Type
            // -----------------
            pIItemObject->put_Type((FolderType)(it->lItemType));

            // -----------------
            // Message Size
            // -----------------
            pIItemObject->put_MessageSize(it->ulMessageSize);

            // -----------------
            // ID
            // -----------------
            // pIItemObject->put_ID(it->sbMessageID))

            // -----------------
            // Parent folder
            // -----------------
            pIItemObject->put_Parentfolder(FolderObj);

            // -----------------
            // Creation date
            // -----------------
            vCreationDate.vt = VT_DATE;
            vCreationDate.date = (long)(it->i64SubmitDate);

            // -----------------
            // ItemID
            // -----------------
            SBinary Itemid = it->sbMessageID;

            VARIANT var;
            VariantInit(&var);
            var.vt = VT_ARRAY | VT_UI1;

            SAFEARRAYBOUND rgsabound[1];
            rgsabound[0].cElements = Itemid.cb;
            rgsabound[0].lLbound = 0;
            var.parray = SafeArrayCreate(VT_UI1, 1, rgsabound);
            if (var.parray != NULL)
            {
                void *pArrayData = NULL;
                SafeArrayAccessData(var.parray, &pArrayData);
                memcpy(pArrayData, Itemid.lpb, Itemid.cb);
                SafeArrayUnaccessData(var.parray);
            }
            else
                LOG_ERROR(_T("GetFolderItems SafeArrayCreate is null"));

            pIItemObject->put_ItemID(var);

            // -----------------
            // Subject
            // -----------------
            pIItemObject->put_Subject(SysAllocString(it->strSubject.c_str()));

            // -----------------
            // FilterDate
            // -----------------
            pIItemObject->put_FilterDate(SysAllocString(it->strFilterDate.c_str()));

            // -----------------
            // ID as string
            // -----------------
            /*
            Zimbra::Util::ScopedArray<CHAR> spUid(new CHAR[(Itemid.cb * 2) + 1]);
            if (spUid.get() != NULL)
            {
                Zimbra::Util::HexFromBin(Itemid.lpb, Itemid.cb, spUid.get());
                CComBSTR str = spUid.getref();
                pIItemObject->put_IDasString(str);
                SysFreeString(str);
            }
            */
        }

        if (FAILED(hr))
            return S_FALSE;

        // -------------------------------------------
        // Copy the the pIItemObject into the variant
        // -------------------------------------------
        pIItemObject.CopyTo(&pItems[i]);

    } // for

    SafeArrayUnaccessData(psa);
    vItems->parray = psa;

    if (folderEntryid.lpb != NULL)
        delete folderEntryid.lpb;

    return S_OK;
}

STDMETHODIMP COMMapiAccount::GetItemData(BSTR UserId, VARIANT ItemId, FolderType type, VARIANT *pRet)
//
// ItemId is the item to fetch
// type is the folder it resides in (this is used to decide what kind of item it is)
//
// Calls m_pMapiAccount->GetItem() to get the data as a C++ struct ContactItemData/MessageItemData etc
// and then builds a dictionary (name-value pairs) for each attibute and outputs in pVal
//
{
    LOGFN_TRACE_NO;

    HRESULT hr = S_OK;

    std::map<BSTR, BSTR> mapDict;

    SBinary ItemID;
    CComBSTR name = UserId;

    FolderType ft = type;
    if (ItemId.vt == (VT_ARRAY | VT_UI1))       // (OLE SAFEARRAY)
    {
        // Get the ItemID from the passed-in variant
        ItemID.cb = ItemId.parray->rgsabound[0].cElements;
        ItemID.lpb = new BYTE[ItemID.cb];
        if (ItemID.lpb != NULL)
        {
            void *pArrayData;
            SafeArrayAccessData(ItemId.parray, &pArrayData);
            memcpy(ItemID.lpb, pArrayData, ItemID.cb);
            SafeArrayUnaccessData(ItemId.parray);

            LPCWSTR ret = NULL;

            if (ft == 2)
            {
                // =======================================================================
                // Contact
                // =======================================================================
                LOG_TRACE(_T("Contact"));

                ContactItemData cd;
                ret = m_pMapiAccount->GetItem(ItemID, cd);
                if(ret != NULL)
                {
                    delete ItemID.lpb;
                    hr = S_FALSE;
                    return hr;
                }

                if (ret == NULL)	// FBS bug 71630 -- 3/22/12
                {
                    mapDict[L"assistantPhone"]      = SysAllocString((cd.AssistantPhone).c_str());
                    mapDict[L"birthday"]            = SysAllocString((cd.Birthday).c_str());
                    mapDict[L"anniversary"]         = SysAllocString((cd.Anniversary).c_str());
                    mapDict[L"callbackPhone"]       = SysAllocString((cd.CallbackPhone).c_str());
                    mapDict[L"carPhone"]            = SysAllocString((cd.CarPhone).c_str());
                    mapDict[L"company"]             = SysAllocString((cd.Company).c_str());
                    mapDict[L"companyPhone"]        = SysAllocString((cd.CompanyPhone).c_str());
                    mapDict[L"department"]          = SysAllocString((cd.Department).c_str());
                    mapDict[L"email"]               = SysAllocString((cd.Email1).c_str());
                    mapDict[L"email2"]              = SysAllocString((cd.Email2).c_str());
                    mapDict[L"email3"]              = SysAllocString((cd.Email3).c_str());
                    mapDict[L"fileAs"]              = SysAllocString((cd.FileAs).c_str());
                    mapDict[L"firstName"]           = SysAllocString((cd.FirstName).c_str());
                    mapDict[L"homeCity"]            = SysAllocString((cd.HomeCity).c_str());
                    mapDict[L"homeCountry"]         = SysAllocString((cd.HomeCountry).c_str());
                    mapDict[L"homeFax"]             = SysAllocString((cd.HomeFax).c_str());
                    mapDict[L"homePhone"]           = SysAllocString((cd.HomePhone).c_str());
                    mapDict[L"homePhone2"]          = SysAllocString((cd.HomePhone2).c_str());
                    mapDict[L"homePostalCode"]      = SysAllocString((cd.HomePostalCode).c_str());
                    mapDict[L"homeState"]           = SysAllocString((cd.HomeState).c_str());
                    mapDict[L"homeStreet"]          = SysAllocString((cd.HomeStreet).c_str());
                    mapDict[L"homeURL"]             = SysAllocString((cd.HomeURL).c_str());
                    mapDict[L"imAddress1"]          = SysAllocString((cd.IMAddress1).c_str());
                    mapDict[L"jobTitle"]            = SysAllocString((cd.JobTitle).c_str());
                    mapDict[L"lastName"]            = SysAllocString((cd.LastName).c_str());
                    mapDict[L"middleName"]          = SysAllocString((cd.MiddleName).c_str());
                    mapDict[L"mobilePhone"]         = SysAllocString((cd.MobilePhone).c_str());
                    mapDict[L"namePrefix"]          = SysAllocString((cd.NamePrefix).c_str());
                    mapDict[L"nameSuffix"]          = SysAllocString((cd.NameSuffix).c_str());
                    mapDict[L"nickname"]            = SysAllocString((cd.NickName).c_str());
                    mapDict[L"notes"]               = SysAllocString((cd.Notes).c_str());
                    mapDict[L"otherCity"]           = SysAllocString((cd.OtherCity).c_str());
                    mapDict[L"otherCountry"]        = SysAllocString((cd.OtherCountry).c_str());
                    mapDict[L"otherFax"]            = SysAllocString((cd.OtherFax).c_str());
                    mapDict[L"otherPhone"]          = SysAllocString((cd.OtherPhone).c_str());
                    mapDict[L"otherPostalCode"]     = SysAllocString((cd.OtherPostalCode).c_str());
                    mapDict[L"otherState"]          = SysAllocString((cd.OtherState).c_str());
                    mapDict[L"otherStreet"]         = SysAllocString((cd.OtherStreet).c_str());
                    mapDict[L"otherURL"]            = SysAllocString((cd.OtherURL).c_str());
                    mapDict[L"pager"]               = SysAllocString((cd.Pager).c_str());
                    mapDict[L"workCity"]            = SysAllocString((cd.WorkCity).c_str());
                    mapDict[L"workCountry"]         = SysAllocString((cd.WorkCountry).c_str());
                    mapDict[L"workFax"]             = SysAllocString((cd.WorkFax).c_str());
                    mapDict[L"workPhone"]           = SysAllocString((cd.WorkPhone).c_str());
                    mapDict[L"workPhone2"]          = SysAllocString((cd.WorkPhone2).c_str());
                    mapDict[L"workPhone3"]          = SysAllocString((cd.PrimaryPhone).c_str());
                    mapDict[L"workPostalCode"]      = SysAllocString((cd.WorkPostalCode).c_str());
                    mapDict[L"workState"]           = SysAllocString((cd.WorkState).c_str());
                    mapDict[L"workStreet"]          = SysAllocString((cd.WorkStreet).c_str());
                    mapDict[L"workURL"]             = SysAllocString((cd.WorkURL).c_str());
                    mapDict[L"outlookUserField1"]   = SysAllocString((cd.UserField1).c_str());
                    mapDict[L"outlookUserField2"]   = SysAllocString((cd.UserField2).c_str());
                    mapDict[L"outlookUserField3"]   = SysAllocString((cd.UserField3).c_str());
                    mapDict[L"outlookUserField4"]   = SysAllocString((cd.UserField4).c_str());
                    mapDict[L"image"]               = SysAllocString((cd.ContactImagePath).c_str());
                    mapDict[L"imageContentType"]    = SysAllocString((cd.ImageContenttype).c_str());
                    mapDict[L"imageContentDisp"]    = SysAllocString((cd.ImageContentdisp).c_str());

                    if (cd.Type.length() > 0)
                    {
                        if (wcsicmp(cd.Type.c_str(), L"group") == 0)
                        {
                            mapDict[L"type"] = SysAllocString(cd.Type.c_str());
                            mapDict[L"dlist"] = SysAllocString((cd.pDList).c_str());
                        }
                    }

                    if (cd.UserDefinedFields.size() > 0)
                    {
                        vector<ContactUDFields>::iterator it;
                        for (it = cd.UserDefinedFields.begin(); it != cd.UserDefinedFields.end(); it++)
                        {
                            BSTR bstrNam = SysAllocString(it->Name.c_str());
                            mapDict[bstrNam] = SysAllocString(it->value.c_str());
                        }
                    }

                    bool bHasTags = false;
                    if (cd.vTags)
                    {
                        wstring tagData;
                        int numTags = (int)cd.vTags->size();
                        if (numTags > 0)
                        {
                            for (int i = 0; i < numTags; i++)
                            {
                                tagData += (*cd.vTags)[i];
                                if (i < (numTags - 1))
                                    tagData += L",";
                            }
                            mapDict[L"tags"] = SysAllocString(tagData.c_str());
                            delete cd.vTags;
                            bHasTags = true;
                        }
                    }
                    if (!bHasTags)
                        mapDict[L"tags"] = SysAllocString(L"");
                }
                

            }
            else 
            if ((ft == 1) || (ft == 5))    
            {
                // =======================================================================
                // message or meeting request
                // =======================================================================
                LOG_TRACE(_T("Message/MeetingRqst"));

                // Get the raw MAPI data to msgdata
                MessageItemData msgdata;
                ret = m_pMapiAccount->GetItem(ItemID, msgdata);
                if (ret != NULL)
                {
                    delete ItemID.lpb;
                    hr = S_FALSE;
                    return hr;
                }

                // msgdata -> dictionary
                if (ret == NULL)	// 71630
                {
                    // Subject
                    mapDict[L"Subject"] = SysAllocString((msgdata.Subject).c_str());
                    LOG_INFO(_T("Subject: '%s'"), msgdata.Subject.c_str());

                    // Date
                    mapDict[L"Date"] = SysAllocString((msgdata.DateString).c_str());

                    // dwMimeSize
                    WCHAR pwszMimeSize[10];
                    _ltow(msgdata.dwMimeSize, pwszMimeSize, 10);
                    mapDict[L"MimeSize"] = SysAllocString(pwszMimeSize);

                    // MimeFile
                    mapDict[L"MimeFile"] = SysAllocString((msgdata.MimeFile).c_str());

                    // UrlName
                    mapDict[L"UrlName"] = SysAllocString((msgdata.Urlname).c_str());
                    LOG_TRACE(_T("urlname: '%s'"), msgdata.Urlname.c_str());

                    // rcvdDate
                    mapDict[L"rcvdDate"] = SysAllocString((msgdata.DeliveryUnixString.c_str()));

                    
                    
                    // wstrmimeBuffer
                    if (msgdata.pwsMimeBuffer)
                    {
                        BSTR bstrMimeBuffer = SysAllocString(msgdata.pwsMimeBuffer); // Allocates a new string + copies the passed-in string into it
                        Zimbra::MAPI::Util::PauseAfterLargeMemDelta();

                        mapDict[L"wstrmimeBuffer"] = bstrMimeBuffer; // Doesnt alloc further mem
                        Zimbra::MAPI::Util::PauseAfterLargeMemDelta();

                        delete[] msgdata.pwsMimeBuffer;
                        msgdata.pwsMimeBuffer = NULL;
                        Zimbra::MAPI::Util::PauseAfterLargeMemDelta();
                    }



                    // DateUnixString
                    mapDict[L"DateUnixString"] = SysAllocString(msgdata.DateUnixString.c_str());

                    // Tags
                    bool bHasTags = false;
                    if (msgdata.vTags)
                    {
                        wstring tagData;
                        int numTags = (int)msgdata.vTags->size();
                        if (numTags > 0)
                        {
                            for (int i = 0; i < numTags; i++)
                            {
                                tagData += (*msgdata.vTags)[i];
                                if (i < (numTags - 1))
                                    tagData += L",";
                            }
                            mapDict[L"tags"] = SysAllocString(tagData.c_str());
                            LOG_INFO(_T("tagData: '%s'"), tagData.c_str());

                            delete msgdata.vTags;
                            bHasTags = true;
                        }
                    }
                    if (!bHasTags)
                        mapDict[L"tags"] = SysAllocString(L"");

                    // flags
                    CComBSTR flags = L"";
                    if (msgdata.HasAttachments)
                        flags.Append(L"a");
                    if (msgdata.IsUnread)
                        flags.Append(L"u");
                    if (msgdata.IsFlagged)
                        flags.Append(L"f");
                    //if(msgdata.HasText)
                    //     flags.AppendBSTR(L"T");
                    //if(msgdata.HasHtml)
                    //     flags.AppendBSTR(L"H");
                    if (msgdata.IsDraft)
                        flags.Append(L"d");
                    if (msgdata.IsForwarded)
                        flags.Append(L"w");
                    if ((msgdata.IsUnsent) || (msgdata.Urlname.substr(0, 11) == L"/Sent Items"))
                        flags.Append(L"s");
                    if (msgdata.RepliedTo)
                        flags.Append(L"r");

                    /*
                    mapDict[L"Has Attachments"] = (msgdata.HasAttachments)? L"True":L"False";
                    mapDict[L"HasHTML"]         = (msgdata.HasHtml)?        L"True":L"False";
                    mapDict[L"HasText"]         = (msgdata.HasText)?        L"True":L"False";
                    mapDict[L"IsDraft"]         = (msgdata.IsDraft)?        L"True":L"False";
                    mapDict[L"IsFlagged"]       = (msgdata.IsFlagged)?      L"True":L"False";
                    mapDict[L"IsForwarded"]     = (msgdata.IsForwarded)?    L"True":L"False";
                    mapDict[L"IsFromMe"]        = (msgdata.IsFromMe)?       L"True":L"False";
                    mapDict[L"IsUnread"]        = (msgdata.IsUnread)?       L"True":L"False";
                    mapDict[L"IsUnsent"]        = (msgdata.IsUnsent)?       L"True":L"False";
                    mapDict[L"IsUnread"]        = (msgdata.IsUnread)?       L"True":L"False";
                    mapDict[L"RepliedTo"]       = (msgdata.IsUnread)?       L"True":L"False";
                    */

                    mapDict[L"flags"] = SysAllocString(flags);

                    /*printf("Subject: %S Date: %I64X DateString:%S		\
                     *      DeliveryDate: %I64X deliveryDateString: %S		\
                     *      Has Attachments: %d Has HTML:%d Has Text:%d	\
                     *      Is Draft:%d Is Flagged: %d Is Forwarded: %d	\
                     *      IsFromMe:%d IsUnread:%d IsUnsent:%d IsRepliedTo:%d	\
                     *      URLName: %S\n",
                     *      msgdata.Subject.c_str(), msgdata.Date, msgdata.DateString.c_str(),
                     *      msgdata.deliveryDate, msgdata.DeliveryDateString.c_str(),msgdata.HasAttachments,
                     *      msgdata.HasHtml, msgdata.HasText,msgdata.IsDraft,msgdata.IsFlagged,msgdata.IsForwarded,
                     *      msgdata.IsFromMe, msgdata.IsUnread, msgdata.IsUnsent,msgdata.RepliedTo,msgdata.Urlname.c_str()
                     *      );
                     *
                     * printf("MIME FILE PATH: %S\n\n\n\n", msgdata.MimeFile.c_str());*/
                }
            }
            else 
            if (ft == 3)
            {
                // =======================================================================
                // Appt
                // =======================================================================
                LOG_TRACE(_T("Appt"));

                // Get the raw MAPI data to apptData
                ApptItemData apptData;
                ret = m_pMapiAccount->GetItem(ItemID, apptData);
                if(ret != NULL)
                {
                    delete ItemID.lpb;
                    hr= S_FALSE;
                    return hr;
                }

                // apptData -> dictionary

                // ------------------------------------------------------------------
                // Series attributes
                // ------------------------------------------------------------------
                if (ret == NULL)	// 71630
                {
                    if (apptData.Uid.length() == 0)     // FBS bug 72893 -- 4/12/12
                        apptData.Uid = Zimbra::MAPI::Util::GetGUID();

                    mapDict[L"name"]            = SysAllocString(apptData.Name                          .c_str());

                    mapDict[L"s"]               = SysAllocString(apptData.StartDate                     .c_str());
                    mapDict[L"e"]               = SysAllocString(apptData.EndDate                       .c_str());
                    mapDict[L"sFilterDate"]     = SysAllocString(apptData.CalFilterDate                 .c_str());   // FBS bug 73982 -- 5/14/12

                    mapDict[L"rsvp"]            = SysAllocString(apptData.RSVP                          .c_str());
                    mapDict[L"ptst"]            = SysAllocString(apptData.PartStat                      .c_str());
                    mapDict[L"currst"]          = SysAllocString(apptData.CurrStat                      .c_str());
                    mapDict[L"fb"]              = SysAllocString(apptData.FreeBusy                      .c_str());
                    mapDict[L"allDay"]          = SysAllocString(apptData.AllDay                        .c_str());
                    mapDict[L"transp"]          = SysAllocString(apptData.Transparency                  .c_str());
                    mapDict[L"su"]              = SysAllocString(apptData.Subject                       .c_str());
                    mapDict[L"loc"]             = SysAllocString(apptData.Location                      .c_str());
                    mapDict[L"uid"]             = SysAllocString(apptData.Uid                           .c_str());
                    mapDict[L"m"]               = SysAllocString(apptData.AlarmTrigger                  .c_str());
                    mapDict[L"class"]           = SysAllocString(apptData.ApptClass                     .c_str());
                    mapDict[L"orAddr"]          = SysAllocString(apptData.organizer.addr                .c_str());
                    mapDict[L"orName"]          = SysAllocString(apptData.organizer.nam                 .c_str());
                    mapDict[L"contentType0"]    = SysAllocString(apptData.vMessageParts[0].contentType  .c_str());
                    mapDict[L"content0"]        = SysAllocString(apptData.vMessageParts[0].content      .c_str());
                    mapDict[L"contentType1"]    = SysAllocString(apptData.vMessageParts[1].contentType  .c_str());
                    mapDict[L"content1"]        = SysAllocString(apptData.vMessageParts[1].content      .c_str());

                    // attendees
                    wstring attendeeData = L"";
                    int numAttendees = (int)apptData.vAttendees.size();
                    if (numAttendees > 0)
                    {
                        for (int i = 0; i < numAttendees; i++)
                        {
                            Attendee* pAttendee = apptData.vAttendees[i];
                            attendeeData += pAttendee->nam;
                            attendeeData += L"~";
                            attendeeData += pAttendee->addr;
                            attendeeData += L"~";
                            attendeeData += pAttendee->role;
                            attendeeData += L"~";
                            attendeeData += pAttendee->partstat;
                            if (i < (numAttendees - 1))     // don't write comma after last attendee
                                attendeeData += L"~";
                        }
                        mapDict[L"attendees"] = SysAllocString(attendeeData.c_str());
                        for (int i = (numAttendees - 1); i >= 0; i--)
                            delete (apptData.vAttendees[i]);
                    }

                    // attachments
                    int numAttachments = (int)apptData.vAttachments.size();
                    if (numAttachments > 0)
                    {
                        BSTR attrs[NUM_ATTACHMENT_ATTRS];

                        WCHAR pwszNumAttachments[10];
                        _ltow(numAttachments, pwszNumAttachments, 10);
                        mapDict[L"numAttachments"] = SysAllocString(pwszNumAttachments);

                        for (int i = 0; i < numAttachments; i++)
                        {
                            AttachmentInfo* pAtt = apptData.vAttachments[i];
                            CreateAttachmentAttrs(attrs, i, -1);

                            LPTSTR pwszTmp = NULL;

                            LPSTR pszContentType = pAtt->pszContentType;
                            AtoW((LPSTR)pszContentType, pwszTmp);
                            mapDict[attrs[0]] =  SysAllocString(pwszTmp);
                            delete[] pwszTmp;

                            LPSTR pszTempFile = pAtt->pszTempFile;
                            AtoW((LPSTR)pszTempFile, pwszTmp);
                            mapDict[attrs[1]] =  SysAllocString(pwszTmp);
                            delete[] pwszTmp;

                            LPSTR pszRealName = pAtt->pszRealName;
                            AtoW((LPSTR)pszRealName, pwszTmp);
                            mapDict[attrs[2]] =  SysAllocString(pwszTmp);
                            delete[] pwszTmp;

                            LPSTR pszContentDisposition = pAtt->pszContentDisposition;
                            AtoW((LPSTR)pszContentDisposition, pwszTmp);
                            mapDict[attrs[3]] =  SysAllocString(pwszTmp);
                            delete[] pwszTmp;
                        }

                        // clean up any attachment
                        for (int i = (numAttachments - 1); i >= 0; i--)
                        {
                            AttachmentInfo* pAtt = apptData.vAttachments[i];
                            delete pAtt->pszContentType;
                            delete pAtt->pszTempFile;
                            delete pAtt->pszRealName;
                            delete pAtt->pszContentDisposition;
                            delete pAtt;
                        }                       
                    }

                    // Tags
                    bool bHasTags = false;
                    if (apptData.vTags)
                    {
                        wstring tagData;
                        int numTags = (int)apptData.vTags->size();
                        if (numTags > 0)
                        {
                            for (int i = 0; i < numTags; i++)
                            {
                                tagData += (*apptData.vTags)[i];
                                if (i < (numTags - 1))
                                    tagData += L",";
                            }
                            mapDict[L"tags"] = SysAllocString(tagData.c_str());
                            delete apptData.vTags;
                            bHasTags = true;
                        }
                    }
                    if (!bHasTags)
                        mapDict[L"tags"] = SysAllocString(L"");

                    // -------------
                    // Timezone data
                    // -------------
                    // tz_start
                    if (apptData.tzStart.id != _T(""))
                    {
                        mapDict[L"tz_start_tid"]     = SysAllocString((apptData.tzStart.id).c_str());
                        mapDict[L"tz_start_stdoff"]  = SysAllocString((apptData.tzStart.standardOffset).c_str());
                        mapDict[L"tz_start_dayoff"]  = SysAllocString((apptData.tzStart.daylightOffset).c_str());
                        mapDict[L"tz_start_sweek"]   = SysAllocString((apptData.tzStart.standardStartWeek).c_str());
                        mapDict[L"tz_start_swkday"]  = SysAllocString((apptData.tzStart.standardStartWeekday).c_str());
                        mapDict[L"tz_start_smon"]    = SysAllocString((apptData.tzStart.standardStartMonth).c_str());
                        mapDict[L"tz_start_shour"]   = SysAllocString((apptData.tzStart.standardStartHour).c_str());
                        mapDict[L"tz_start_smin"]    = SysAllocString((apptData.tzStart.standardStartMinute).c_str());
                        mapDict[L"tz_start_ssec"]    = SysAllocString((apptData.tzStart.standardStartSecond).c_str());
                        mapDict[L"tz_start_dweek"]   = SysAllocString((apptData.tzStart.daylightStartWeek).c_str());
                        mapDict[L"tz_start_dwkday"]  = SysAllocString((apptData.tzStart.daylightStartWeekday).c_str());
                        mapDict[L"tz_start_dmon"]    = SysAllocString((apptData.tzStart.daylightStartMonth).c_str());
                        mapDict[L"tz_start_dhour"]   = SysAllocString((apptData.tzStart.daylightStartHour).c_str());
                        mapDict[L"tz_start_dmin"]    = SysAllocString((apptData.tzStart.daylightStartMinute).c_str());
                        mapDict[L"tz_start_dsec"]    = SysAllocString((apptData.tzStart.daylightStartSecond).c_str());
                    }

                    // tz_start_end
                    if (apptData.tzEnd.id != _T(""))
                    {
                        mapDict[L"tz_end_tid"]     = SysAllocString((apptData.tzEnd.id).c_str());
                        mapDict[L"tz_end_stdoff"]  = SysAllocString((apptData.tzEnd.standardOffset).c_str());
                        mapDict[L"tz_end_dayoff"]  = SysAllocString((apptData.tzEnd.daylightOffset).c_str());
                        mapDict[L"tz_end_sweek"]   = SysAllocString((apptData.tzEnd.standardStartWeek).c_str());
                        mapDict[L"tz_end_swkday"]  = SysAllocString((apptData.tzEnd.standardStartWeekday).c_str());
                        mapDict[L"tz_end_smon"]    = SysAllocString((apptData.tzEnd.standardStartMonth).c_str());
                        mapDict[L"tz_end_shour"]   = SysAllocString((apptData.tzEnd.standardStartHour).c_str());
                        mapDict[L"tz_end_smin"]    = SysAllocString((apptData.tzEnd.standardStartMinute).c_str());
                        mapDict[L"tz_end_ssec"]    = SysAllocString((apptData.tzEnd.standardStartSecond).c_str());
                        mapDict[L"tz_end_dweek"]   = SysAllocString((apptData.tzEnd.daylightStartWeek).c_str());
                        mapDict[L"tz_end_dwkday"]  = SysAllocString((apptData.tzEnd.daylightStartWeekday).c_str());
                        mapDict[L"tz_end_dmon"]    = SysAllocString((apptData.tzEnd.daylightStartMonth).c_str());
                        mapDict[L"tz_end_dhour"]   = SysAllocString((apptData.tzEnd.daylightStartHour).c_str());
                        mapDict[L"tz_end_dmin"]    = SysAllocString((apptData.tzEnd.daylightStartMinute).c_str());
                        mapDict[L"tz_end_dsec"]    = SysAllocString((apptData.tzEnd.daylightStartSecond).c_str());
                    }

                    // Legacy timezone data only available on recurrent, and wont be available if the above newstyle available
                    if (apptData.recurPattern.length() > 0)
                    {
                        if (apptData.tzLegacy.id != _T(""))
                        {
                            mapDict[L"tz_legacy_tid"]     = SysAllocString((apptData.tzLegacy.id).c_str());
                            mapDict[L"tz_legacy_stdoff"]  = SysAllocString((apptData.tzLegacy.standardOffset).c_str());
                            mapDict[L"tz_legacy_dayoff"]  = SysAllocString((apptData.tzLegacy.daylightOffset).c_str());
                            mapDict[L"tz_legacy_sweek"]   = SysAllocString((apptData.tzLegacy.standardStartWeek).c_str());
                            mapDict[L"tz_legacy_swkday"]  = SysAllocString((apptData.tzLegacy.standardStartWeekday).c_str());
                            mapDict[L"tz_legacy_smon"]    = SysAllocString((apptData.tzLegacy.standardStartMonth).c_str());
                            mapDict[L"tz_legacy_shour"]   = SysAllocString((apptData.tzLegacy.standardStartHour).c_str());
                            mapDict[L"tz_legacy_smin"]    = SysAllocString((apptData.tzLegacy.standardStartMinute).c_str());
                            mapDict[L"tz_legacy_ssec"]    = SysAllocString((apptData.tzLegacy.standardStartSecond).c_str());
                            mapDict[L"tz_legacy_dweek"]   = SysAllocString((apptData.tzLegacy.daylightStartWeek).c_str());
                            mapDict[L"tz_legacy_dwkday"]  = SysAllocString((apptData.tzLegacy.daylightStartWeekday).c_str());
                            mapDict[L"tz_legacy_dmon"]    = SysAllocString((apptData.tzLegacy.daylightStartMonth).c_str());
                            mapDict[L"tz_legacy_dhour"]   = SysAllocString((apptData.tzLegacy.daylightStartHour).c_str());
                            mapDict[L"tz_legacy_dmin"]    = SysAllocString((apptData.tzLegacy.daylightStartMinute).c_str());
                            mapDict[L"tz_legacy_dsec"]    = SysAllocString((apptData.tzLegacy.daylightStartSecond).c_str());
                        }
                    }

                    // ----------
                    // recurrence
                    // ----------
                    if (apptData.recurPattern.length() > 0)
                    {
                        mapDict[L"freq"]  = SysAllocString((apptData.recurPattern).c_str());
                        mapDict[L"ival"]  = SysAllocString((apptData.recurInterval).c_str());
                        mapDict[L"count"] = SysAllocString((apptData.recurCount).c_str());      // can set this either way

                        if (apptData.recurEndDate.length() > 0)
                            mapDict[L"until"] = SysAllocString((apptData.recurEndDate).c_str());

                        if (apptData.recurWkday.length() > 0)
                            mapDict[L"wkday"] = SysAllocString((apptData.recurWkday).c_str());

                        if (apptData.recurDayOfMonth.length() > 0)
                            mapDict[L"modaylist"] = SysAllocString((apptData.recurDayOfMonth).c_str());

                        if (apptData.recurMonthOfYear.length() > 0)
                            mapDict[L"molist"] = SysAllocString((apptData.recurMonthOfYear).c_str());

                        if (apptData.recurMonthOccurrence.length() > 0)
                            mapDict[L"poslist"] = SysAllocString((apptData.recurMonthOccurrence).c_str());

                        // --------------------------------------------------------
                        // Exception attributes
                        // --------------------------------------------------------
                        int numExceptions = (int)apptData.vExceptions.size();   
                        if (numExceptions > 0)
                        {
                            WCHAR pwszNumExceptions[10];
                            _ltow(numExceptions, pwszNumExceptions, 10);
                            mapDict[L"numExceptions"] = SysAllocString(pwszNumExceptions);

                            for (int i = 0; i < numExceptions; i++)
                            {
                                MAPIAppointment* pExceptAppt = apptData.vExceptions[i];

                                BSTR attrs[NUM_EXCEPTION_ATTRS];
                                CreateExceptionAttrs(attrs, i);

                                // Main
                                mapDict[attrs[0]] =  SysAllocString(pExceptAppt->GetExceptionType()             .c_str());
                                mapDict[attrs[1]]  = SysAllocString(pExceptAppt->GetResponseStatus()            .c_str());
                                mapDict[attrs[2]]  = SysAllocString(pExceptAppt->GetBusyStatus()                .c_str());
                                mapDict[attrs[3]]  = SysAllocString(pExceptAppt->GetAllday()                    .c_str());
                                mapDict[attrs[4]]  = SysAllocString(pExceptAppt->GetSubject()                   .c_str());
                                mapDict[attrs[5]]  = SysAllocString(pExceptAppt->GetSubject()                   .c_str());
                                mapDict[attrs[6]]  = SysAllocString(pExceptAppt->GetLocation()                  .c_str());
                                mapDict[attrs[7]]  = SysAllocString(pExceptAppt->GetReminderMinutes()           .c_str());
                                mapDict[attrs[8]]  = SysAllocString(pExceptAppt->GetStartDate()                 .c_str());
                                mapDict[attrs[9]]  = SysAllocString(pExceptAppt->GetStartDateForRecID()         .c_str());
                                mapDict[attrs[10]] = SysAllocString(pExceptAppt->GetEndDate()                   .c_str());
                                mapDict[attrs[11]] = SysAllocString(pExceptAppt->GetOrganizerAddr()             .c_str());
                                mapDict[attrs[12]] = SysAllocString(pExceptAppt->GetOrganizerName()             .c_str());
                                mapDict[attrs[13]] = SysAllocString(L"text/plain");
                                mapDict[attrs[14]] = SysAllocString(pExceptAppt->GetPlainTextFileAndContent()   .c_str());
                                mapDict[attrs[15]] = SysAllocString(L"text/html");
                                mapDict[attrs[16]] = SysAllocString(pExceptAppt->GetHtmlFileAndContent()        .c_str());

                                // Attendees
                                attendeeData = L"";
                                int numAttendeesInException = (int)pExceptAppt->GetAttendees().size();
                                if (numAttendeesInException > 0)
                                {
                                    for (int j = 0; j < numAttendeesInException; j++)
                                    {         
                                        Attendee* pAttendee = pExceptAppt->GetAttendees()[j];
                                        attendeeData += pAttendee->nam;
                                        attendeeData += L"~";
                                        attendeeData += pAttendee->addr;
                                        attendeeData += L"~";
                                        attendeeData += pAttendee->role;
                                        attendeeData += L"~";
                                        attendeeData += pAttendee->partstat;
                                        if (j < (numAttendeesInException - 1))     // don't write comma after last attendee
                                            attendeeData += L"~";
                                    }
                                    for (int j = (numAttendeesInException - 1); j >= 0; j--)
                                        delete pExceptAppt->GetAttendees()[j];

                                }
                                mapDict[attrs[17]] = SysAllocString(attendeeData.c_str());

                                // Attachments
                                int numAttachmentsInException = (int)pExceptAppt->GetAttachmentInfo().size();
                                WCHAR pwszNumAttachments[10];
                                _ltow(numAttachmentsInException, pwszNumAttachments, 10);
                                mapDict[attrs[18]] = SysAllocString(pwszNumAttachments);
                                if (numAttachmentsInException > 0)
                                {
                                    BSTR attrs[NUM_ATTACHMENT_ATTRS];
                                    for (int j = 0; j < (int)numAttachmentsInException; j++)
                                    {
                                        AttachmentInfo* pAtt = pExceptAppt->GetAttachmentInfo()[j];

                                        CreateAttachmentAttrs(attrs, j, i);

                                        LPTSTR pwszTmp = NULL;

                                        LPSTR pszContentType = pAtt->pszContentType;
                                        AtoW((LPSTR)pszContentType, pwszTmp);
                                        mapDict[attrs[0]] =  SysAllocString(pwszTmp);
                                        delete[] pwszTmp;

                                        LPSTR pszTempFile = pAtt->pszTempFile;
                                        AtoW((LPSTR)pszTempFile, pwszTmp);
                                        mapDict[attrs[1]] =  SysAllocString(pwszTmp);
                                        delete[] pwszTmp;

                                        LPSTR pszRealName = pAtt->pszRealName;
                                        AtoW((LPSTR)pszRealName, pwszTmp);
                                        mapDict[attrs[2]] =  SysAllocString(pwszTmp);
                                        delete[] pwszTmp;

                                        LPSTR pszContentDisposition = pAtt->pszContentDisposition;
                                        AtoW((LPSTR)pszContentDisposition, pwszTmp);
                                        mapDict[attrs[3]] =  SysAllocString(pwszTmp);
                                        delete[] pwszTmp;
                                    }

                                    for (int j = (numAttachmentsInException - 1); j >= 0; j--)
                                    {
                                        AttachmentInfo* pAtt = pExceptAppt->GetAttachmentInfo()[j];
                                        delete pAtt->pszContentType;
                                        delete pAtt->pszTempFile;
                                        delete pAtt->pszRealName;
                                        delete pAtt->pszContentDisposition;
                                        delete pAtt;
                                    }

                                } // if (numAttachmentsInException > 0)

                            } // for (int i = 0; i < numExceptions; i++)

                            // clean up any exceptions
                            for (int i = (numExceptions - 1); i >= 0; i--)
                                delete (apptData.vExceptions[i]);

                        } // if (numExceptions > 0)

                    } // if (apptData.recurPattern.length() > 0)


                } // if (ret == NULL)
            }
            else 
            if (ft == 4)
            {
                // =======================================================================
                // Task
                // =======================================================================
                LOG_TRACE(_T("Task"));

                TaskItemData taskData;
                ret = m_pMapiAccount->GetItem(ItemID, taskData);
                if(ret != NULL)
                {
                    delete ItemID.lpb;
                    hr= S_FALSE;
                    return hr;
                }
                if (ret == NULL)	// 71630
                {
                    mapDict[L"name"]            = SysAllocString((taskData.Subject).c_str());
                    mapDict[L"su"]              = SysAllocString((taskData.Subject).c_str());
                    mapDict[L"priority"]        = SysAllocString((taskData.Importance).c_str());
                    mapDict[L"s"]               = SysAllocString((taskData.TaskStart).c_str());
                    mapDict[L"sFilterDate"]     = SysAllocString((taskData.TaskFilterDate).c_str());   // FBS bug 73982 -- 5/14/12
                    mapDict[L"e"]               = SysAllocString((taskData.TaskDue).c_str());
                    mapDict[L"status"]          = SysAllocString((taskData.Status).c_str());
                    mapDict[L"percentComplete"] = SysAllocString((taskData.PercentComplete).c_str());
                    mapDict[L"xp-TOTAL_WORK"]   = SysAllocString((taskData.TotalWork).c_str());
                    mapDict[L"xp-ACTUAL_WORK"]  = SysAllocString((taskData.ActualWork).c_str());
                    mapDict[L"xp-COMPANIES"]    = SysAllocString((taskData.Companies).c_str());
                    mapDict[L"xp-MILEAGE"]      = SysAllocString((taskData.Mileage).c_str());
                    mapDict[L"xp-BILLING"]      = SysAllocString((taskData.BillingInfo).c_str());

                    if (taskData.TaskFlagDueBy.length() > 0)
                        mapDict[L"taskflagdueby"] = SysAllocString((taskData.TaskFlagDueBy).c_str());

                    mapDict[L"class"]           = SysAllocString((taskData.ApptClass).c_str());
                    mapDict[L"contentType0"]    = SysAllocString((taskData.vMessageParts[0].contentType).c_str());
                    mapDict[L"content0"]        = SysAllocString((taskData.vMessageParts[0].content).c_str());
                    mapDict[L"contentType1"]    = SysAllocString((taskData.vMessageParts[1].contentType).c_str());
                    mapDict[L"content1"]        = SysAllocString((taskData.vMessageParts[1].content).c_str());

                    // attachments
                    int numAttachments = (int)taskData.vAttachments.size();
                    if (numAttachments > 0)
                    {
                        WCHAR pwszNumAttachments[10];
                        BSTR attrs[NUM_ATTACHMENT_ATTRS];

                        _ltow(numAttachments, pwszNumAttachments, 10);
                        mapDict[L"numAttachments"] = SysAllocString(pwszNumAttachments);
                        for (int i = 0; i < numAttachments; i++)
                        {
                            CreateAttachmentAttrs(attrs, i, -1);

                            LPSTR pszContentType = taskData.vAttachments[i]->pszContentType;
                            LPSTR pszTempFile = taskData.vAttachments[i]->pszTempFile;
                            LPSTR pszRealName = taskData.vAttachments[i]->pszRealName;
                            LPSTR pszContentDisposition = taskData.vAttachments[i]->pszContentDisposition;

                            LPTSTR pwDes = NULL;
                            AtoW((LPSTR)pszContentType,pwDes);
                            mapDict[attrs[0]] =  SysAllocString(pwDes);
                            delete[] pwDes;

                            AtoW((LPSTR)pszTempFile,pwDes);
                            mapDict[attrs[1]] =  SysAllocString(pwDes);
                            delete[] pwDes;

                            AtoW((LPSTR)pszRealName,pwDes);
                            mapDict[attrs[2]] =  SysAllocString(pwDes);
                            delete[] pwDes;

                            AtoW((LPSTR)pszContentDisposition,pwDes);
                            mapDict[attrs[3]] =  SysAllocString(pwDes);
                            delete[] pwDes;
                        }

                        // clean up any attachment
                        for (int i = (numAttachments - 1); i >= 0; i--)
                        {
                            delete taskData.vAttachments[i]->pszContentType;
                            delete taskData.vAttachments[i]->pszTempFile;
                            delete taskData.vAttachments[i]->pszRealName;
                            delete taskData.vAttachments[i]->pszContentDisposition;
                            delete taskData.vAttachments[i];
                        }                       
                    }

                    // Tags
                    bool bHasTags = false;
                    if (taskData.vTags)
                    {
                        wstring tagData;
                        int numTags = (int)taskData.vTags->size();
                        if (numTags > 0)
                        {
                            for (int i = 0; i < numTags; i++)
                            {
                                tagData += (*taskData.vTags)[i];
                                if (i < (numTags - 1))
                                    tagData += L",";
                            }
                            mapDict[L"tags"] = SysAllocString(tagData.c_str());
                            delete taskData.vTags;
                            bHasTags = true;
                        }
                    }
                    if (!bHasTags)
                        mapDict[L"tags"] = SysAllocString(L"");

                    // recurrence
                    if (taskData.recurPattern.length() > 0)
                    {
                        mapDict[L"freq"] = SysAllocString((taskData.recurPattern).c_str());
                        mapDict[L"ival"] = SysAllocString((taskData.recurInterval).c_str());
                        mapDict[L"count"] = SysAllocString((taskData.recurCount).c_str());      // can set this either way

                        if (taskData.recurEndDate.length() > 0)
                            mapDict[L"until"] = SysAllocString((taskData.recurEndDate).c_str());

                        if (taskData.recurWkday.length() > 0)
                            mapDict[L"wkday"] = SysAllocString((taskData.recurWkday).c_str());

                        if (taskData.recurDayOfMonth.length() > 0)
                            mapDict[L"modaylist"] = SysAllocString((taskData.recurDayOfMonth).c_str());

                        if (taskData.recurMonthOfYear.length() > 0)
                            mapDict[L"molist"] = SysAllocString((taskData.recurMonthOfYear).c_str());

                        if (taskData.recurMonthOccurrence.length() > 0)
                            mapDict[L"poslist"] = SysAllocString((taskData.recurMonthOccurrence).c_str());

                        /*
                        // timezone
                        mapDict[L"tz_legacy_tid"]      = SysAllocString((apptData.tz.id).c_str());
                        mapDict[L"tz_legacy_stdoff"]   = SysAllocString((apptData.tz.standardOffset).c_str());
                        mapDict[L"tz_legacy_dayoff"]   = SysAllocString((apptData.tz.daylightOffset).c_str());
                        mapDict[L"tz_legacy_sweek"]    = SysAllocString((apptData.tz.standardStartWeek).c_str());
                        mapDict[L"tz_legacy_swkday"]   = SysAllocString((apptData.tz.standardStartWeekday).c_str());
                        mapDict[L"tz_legacy_smon"]     = SysAllocString((apptData.tz.standardStartMonth).c_str());
                        mapDict[L"tz_legacy_shour"]    = SysAllocString((apptData.tz.standardStartHour).c_str());
                        mapDict[L"tz_legacy_smin"]     = SysAllocString((apptData.tz.standardStartMinute).c_str());
                        mapDict[L"tz_legacy_ssec"]     = SysAllocString((apptData.tz.standardStartSecond).c_str());
                        mapDict[L"tz_legacy_dweek"]    = SysAllocString((apptData.tz.daylightStartWeek).c_str());
                        mapDict[L"tz_legacy_dwkday"]   = SysAllocString((apptData.tz.daylightStartWeekday).c_str());
                        mapDict[L"tz_legacy_dmon"]     = SysAllocString((apptData.tz.daylightStartMonth).c_str());
                        mapDict[L"tz_legacy_dhour"]    = SysAllocString((apptData.tz.daylightStartHour).c_str());
                        mapDict[L"tz_legacy_dmin"]     = SysAllocString((apptData.tz.daylightStartMinute).c_str());
                        mapDict[L"tz_legacy_dsec"]     = SysAllocString((apptData.tz.daylightStartSecond).c_str());
                        //
                        */
                    }
                }
            }
        }
        delete ItemID.lpb;
    }

    // ----------------------------------------------------
    // All data now in mapDict -> transfer to pRet VARIANT
    // ----------------------------------------------------
    hr = CppDictToVarArray(mapDict, pRet);

    return hr;
}

STDMETHODIMP COMMapiAccount::GetOOO(BSTR *OOOInfo)
{
    LOGFN_TRACE_NO;

    LPCWSTR lpInfo = m_pMapiAccount->GetOOOStateAndMsg();
    *OOOInfo = CComBSTR(lpInfo);
    delete[] lpInfo;

    return S_OK;
}

STDMETHODIMP COMMapiAccount::GetRules(VARIANT *rules)
{
    // This is the big method for Exchange rules processing.  It reads the PR_RULES_TABLE,
    // gets the info back, and creates a map (pMap) that will be read by the Zimbra API
    // layer (SetModifyFilterRulesRequest, which calls AddFilterRuleToRequest for each rule.)

    // Assuming there are two rules, Foo and Bar, here is an example of pMap.  We'll only show
    // the format of the first rule.  There are 3 entries for each rule: rule, tests and actions.

    // So the first rule will have 0filterRule, 0filterTests, 0filterActions, the second will have
    // 1filterRule, 1filterTests, 1filterActions, etc.  There is a numRules name/value pair that
    // gives the number of rules.  In a given entry, there are certain token delimiters.  The rule
    // entry uses ",", the test entry uses an initial ":" followed by "`~".  The action entries
    // just delimit tokens by "`~".  The string "^^^" is the delimiter for multiple filterTests
    // and multiple filterActions.

    // Here is an example of a map for the rule:
    // "with foo in the subject, forward it to user2 and move it to the Test folder"

    // name            value
    // ----            -----
    // numRules        1
    // 0filterRule     name,TestRule,active,1
    // 0filterTests"   allof:headerTest`~index`~0`~stringComparison`~contains`~header`~Subject`~value`~foo
    // 0filterActions  actionRedirect`~a`~user2^^^actionFileInto`~folderPath`~Test
    
    LOGFN_TRACE_NO;

    USES_CONVERSION;
    vector<CRule> vRuleList;
    LPCWSTR status = m_pMapiAccount->GetExchangeRules(vRuleList);
    if(status != NULL)
    {
        //LOG_ERROR(_T("GetRules failed '%s'"), status); // Expected for PST migration
        LOG_GEN(_T("GetRules failed '%s'"), status);
        return S_FALSE;
    }

    size_t numRules = vRuleList.size();
    if (numRules == 0)
    {
        LOG_INFO(_T("No rules to migrate"));
        return S_OK;
    }

    WCHAR pwszTemp[5];
    _itow((int)numRules, pwszTemp, 10);
    std::map<BSTR, BSTR> pMap;
    pMap[L"numRules"] = SysAllocString(pwszTemp);

    // Create the array of attrs (0filterRule, 0filterTests, 0filterActions, 1filterRule, etc.)
    // std:map needs all names allocated separately
    int numAttrs = ((int)numRules) * 3;
    WCHAR tmp[10];
    LPWSTR* ruleMapNames = new LPWSTR[numAttrs];
    for (int i = 0; i < numAttrs; i += 3)
    {
        _ltow(i/3, tmp, 10);
        ruleMapNames[i] = new WCHAR[20];
        lstrcpy(ruleMapNames[i], tmp);
        lstrcat(ruleMapNames[i], L"filterRule");

        ruleMapNames[i + 1] = new WCHAR[20];
        lstrcpy(ruleMapNames[i + 1], tmp);
        lstrcat(ruleMapNames[i + 1], L"filterTests");

        ruleMapNames[i + 2] = new WCHAR[20];
        lstrcpy(ruleMapNames[i + 2], tmp);
        lstrcat(ruleMapNames[i + 2], L"filterActions");
    }
    /////////////////

    CRuleMap* pRuleMap = new CRuleMap();
    LPWSTR pwszLine = new WCHAR[20000];
    int iIndex = 0;
    for (std::vector<CRule>::iterator ruleIndex = vRuleList.begin(); ruleIndex != vRuleList.end(); ruleIndex++)
    {
        CRule &rule = *ruleIndex;
        int iMapIndex = iIndex * 3;

        // Rule
        pRuleMap->WriteFilterRule(rule, pwszLine);
        pMap[ruleMapNames[iMapIndex]] = SysAllocString(pwszLine);

        // Tests
        pRuleMap->WriteFilterTests(rule, pwszLine);
        pMap[ruleMapNames[iMapIndex + 1]] = SysAllocString(pwszLine);

        // Action
        pRuleMap->WriteFilterActions(rule, pwszLine);
        pMap[ruleMapNames[iMapIndex + 2]] = SysAllocString(pwszLine);
        iIndex++;
    }

    delete pRuleMap;
    delete pwszLine;


    // Create SafeArray of VARIANT BSTRs
    VariantInit(rules);
    SAFEARRAY *pSA = NULL;
    SAFEARRAYBOUND aDim[2];                     // two dimensional array
    aDim[0].lLbound = 0;
    aDim[0].cElements = (ULONG)pMap.size();
    aDim[1].lLbound = 0;
    aDim[1].cElements = (ULONG)pMap.size();      // rectangular array
    pSA = SafeArrayCreate(VT_BSTR, 2, aDim);    // again, 2 dimensions

    long aLong[2];
    HRESULT hr = S_OK;
    if (pSA != NULL)
    {
        BSTR temp;
        std::map<BSTR, BSTR>::iterator it;
        for (long x = aDim[0].lLbound; x < 2 /*(aDim[0].cElements + aDim[0].lLbound)*/; x++)
        {
            aLong[0] = x;                       // set x index
            it = pMap.begin();
            for (long y = aDim[1].lLbound; y < (long)(aDim[1].cElements + aDim[1].lLbound); y++)
            {
                aLong[1] = y;                   // set y index
                if (aLong[0] > 0)
                    temp = SysAllocString((*it).second);
                else
                    temp = SysAllocString((*it).first);
                hr = SafeArrayPutElement(pSA, aLong, temp);

                it++;
            }
        }
    }
    rules->vt = VT_ARRAY | VT_BSTR;
    rules->parray = pSA;

    // Now that the map is set, delete the rule map names.
    // Don't need to go backwards, but it's a good convention
    for (int i = (numAttrs - 1); i >= 0; i--)
        delete ruleMapNames[i];
    delete ruleMapNames;

    return hr;
}


HRESULT COMMapiAccount::InitAndEnumeratePublicFolders(BSTR * statusMsg)
{
    HRESULT hr = S_OK;

    m_pMapiAccount->InitializePublicFolders();
    
    std::vector<std::string> pubFldrList;
    m_pMapiAccount->EnumeratePublicFolders(pubFldrList); 

    // Log discovered PFs
    vector<std::string>::iterator pfenumItr;
    LOG_TRACE(L"Enumerated Public folders:");
    for (pfenumItr = pubFldrList.begin(); pfenumItr != pubFldrList.end(); pfenumItr++)
        LOG_TRACE(L"- %s", pfenumItr->c_str());
    
    *statusMsg =  SysAllocString(L"");

    return hr;
}

void COMMapiAccount::CreateAttachmentAttrs(BSTR attrs[], int numAtt, int numExcep)
//
// Generates attachment attribute names of the form
//
//   ex<n>_att<n>_attrname e.g. e.g. ex0_att0_attContentType
//
// This method also gets called for non-exceptions in which case numExcep==-1 and "ex<n> is omitted
//
{
    LOGFN_TRACE_NO;

    LPWSTR names[] = {L"attContentType", L"attTempFile", L"attRealName", L"attContentDisposition"};
                     
    WCHAR pwszAttr[40] = {0};

    WCHAR pwszExNum[10];
    _ltow(numExcep, pwszExNum, 10);

    WCHAR pwszAttNum[10];
    _ltow(numAtt, pwszAttNum, 10);

    for (int i = 0; i < NUM_ATTACHMENT_ATTRS; i++)
    {
        if (numExcep>=0)
        {
            // Need to include "ex<n>" prefix
            lstrcpy(pwszAttr, L"ex");
            lstrcat(pwszAttr, pwszExNum);
            lstrcat(pwszAttr, L"_");
            lstrcat(pwszAttr, L"att");
        }
        else
        {
            // Exclude the prefix
            lstrcpy(pwszAttr, L"att");
        }

        lstrcat(pwszAttr, pwszAttNum);
        lstrcat(pwszAttr, L"_");
        lstrcat(pwszAttr, names[i]);
        attrs[i] = SysAllocString(pwszAttr);
    }
}

void COMMapiAccount::CreateExceptionAttrs(BSTR attrs[], int num)
{
    LOGFN_TRACE_NO;

    LPWSTR names[] = 
    {
        L"exceptionType", 
        L"ptst", 
        L"fb", 
        L"allDay",
        L"name", 
        L"su", 
        L"loc", 
        L"m", 
        L"s", 
        L"rid", 
        L"e",                      
        L"orAddr", 
        L"orName", 
        L"contentType0", 
        L"content0",
        L"contentType1", 
        L"content1", 
        L"attendees",
        L"numAttachments"
    };
                     
    WCHAR pwszNum[10];
    _ltow(num, pwszNum, 10);

    for (int i = 0; i < NUM_EXCEPTION_ATTRS; i++)
    {
        WCHAR pwszAttr[35];           // ex0_exceptionType etc
        lstrcpy(pwszAttr, L"ex");
        lstrcat(pwszAttr, pwszNum);
        lstrcat(pwszAttr, L"_");
        lstrcat(pwszAttr, names[i]);
        attrs[i] = SysAllocString(pwszAttr);
    }
}

HRESULT COMMapiAccount::CppDictToVarArray(std::map<BSTR, BSTR>& mapDict, VARIANT *pVar)
//
// Converts mapDict to a 2xN array of variants for passing back to c# layer
// Assumes each mapDict value is a SysAllocString. Frees this.
//
{
    LOGFN_VERBOSE_NO;
    HRESULT hr = S_OK;

    ULONG ulMapSize = (ULONG)mapDict.size();

    // Array neeeds to be 2 cols (key, value) BY ulMapSize rows

    // Create 2 dimensional rectangular SafeArray of VARIANT BSTRs
    const int nDIMS = 2;
    SAFEARRAYBOUND aDim[nDIMS];
    aDim[0].lLbound = 0;
    aDim[0].cElements = 2;
    aDim[1].lLbound = 0;
    aDim[1].cElements = ulMapSize;

    SAFEARRAY *pSA = SafeArrayCreate(VT_BSTR, nDIMS, aDim); // Effectively BSTR[2, ulMapSize]
    if (pSA)
    {
        // 2 passes x = 0, and x = 1
        // First pass does the key names, second does the values
        for (long x = 0; x < 2; x++)
        {
            long aDest[2];
            aDest[0] = x; 

            std::map<BSTR, BSTR>::iterator itDict = mapDict.begin();
            for (long y = 0; y < (long)ulMapSize; y++)
            {
                BSTR temp;
                if (x == 0)
                    temp = SysAllocString(itDict->first);
                else
                    temp = SysAllocString(itDict->second);

                // Copy the data across
                aDest[1] = y;
                hr = SafeArrayPutElement(pSA, aDest, temp);
                _ASSERT(hr == S_OK);

                SysFreeString(temp);
                itDict++;
            }
        }
    }

    VariantInit(pVar);
    pVar->vt = VT_ARRAY | VT_BSTR;
    pVar->parray = pSA;

    // Free all BSTRs
    std::map<BSTR,BSTR>::iterator itDict = mapDict.begin();
    while(itDict != mapDict.end())
    {
        SysFreeString((*itDict).second);
        itDict++;
    }
    mapDict.clear();

    return hr;
}
