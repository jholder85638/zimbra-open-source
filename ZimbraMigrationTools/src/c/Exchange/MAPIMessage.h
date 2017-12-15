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
#pragma once

namespace Zimbra
{
namespace MAPI
{


#define PR_URL_NAME                     PROP_TAG(PT_TSTRING, 0x6707)

#define EXCHIVERB_OPEN                  0
#define EXCHIVERB_RESERVED_COMPOSE      100
#define EXCHIVERB_RESERVED_OPEN         101
#define EXCHIVERB_REPLYTOSENDER         102
#define EXCHIVERB_REPLYTOALL            103
#define EXCHIVERB_FORWARD               104
#define EXCHIVERB_PRINT                 105
#define EXCHIVERB_SAVEAS                106
#define EXCHIVERB_RESERVED_DELIVERY     107
#define EXCHIVERB_REPLYTOFOLDER         108

typedef enum _ZM_ITEM_TYPE
{
    ZT_NONE = 0, 
    ZT_MAIL, 
    ZT_CONTACTS, 
    ZT_APPOINTMENTS, 
    ZT_TASKS, 
    ZT_MEETREQ, 
    ZTMAX
} ZM_ITEM_TYPE;


// =============================
// MAPIMessageException class
// =============================
class MAPIMessageException: public GenericException
{
public:
    MAPIMessageException(HRESULT hrErrCode, LPCWSTR lpszDescription);
    MAPIMessageException(HRESULT hrErrCode, LPCWSTR lpszDescription, LPCWSTR lpszShortDescription, int nLine, LPCSTR strFile);
    virtual ~MAPIMessageException() {}
};

// =============================
// MAPIMessage Class
// =============================
class MAPIMessage:public Zimbra::Util::ZimObj
{
public:
    MAPIMessage();
    ~MAPIMessage();

    void InitMAPIMessage(LPMESSAGE pMessage, MAPISession &session, bool bPartial=false);
    void _InitMAPIMessage(LPMESSAGE pMessage, bool bPartial=false, LPMAPITABLE* ppRecipTable = NULL);
    void InternalFree();

    LPMESSAGE InternalMessageObject() { return m_pMessage; }

    bool Subject(LPTSTR *ppSubject);
    bool FilterDate(LPTSTR *ppFilterDate);
    ZM_ITEM_TYPE ItemType();
    bool IsFlagged();
    bool GetURLName(LPTSTR *pstrUrlName);
    bool IsDraft();
    BOOL IsFromMe();
    BOOL IsUnread();
    BOOL Forwarded();
    BOOL RepliedTo();
    bool HasAttach();
    BOOL IsUnsent();
    bool HasHtmlPart();
    bool HasTextPart();
    SBinary &UniqueId();

    __int64 DeliveryDate();
    LPSTR DeliveryDateString();
    LPSTR DeliveryUnixString();

    LPSTR SubmitDateString();
    LPSTR SubmitDateUnixString();
    __int64 SubmitDate();

    DWORD MessageSize();

    std::vector<LPWSTR>* SetKeywords();

    SBinary EntryID() { return m_EntryID; }
    bool TextBody(LPTSTR *ppBody, unsigned int &nTextChars);

    // reads the utf8 body and retruns it with accented chararcters
    bool UTF8EncBody(LPTSTR *ppBody, unsigned int &nTextChars);

    // return the html body of the message
    bool HtmlBody(LPVOID *ppBody, unsigned int &nHtmlBodyLen);
    bool DecodeRTF2HTML(char *buf, unsigned int *len);
    bool IsRTFHTML(const char *buf);

    void AddBody(mimepp::Message &msg, LPSTR pCharset);
    void ToMimePPMessage(mimepp::Message &msg);

private:
    MAPISession *m_session;
    LPMESSAGE m_pMessage;
    LPSPropValue m_pMessagePropVals;
    LPSRowSet m_pRecipientRows;
    SBinary m_EntryID;

    // --------------------------------------------------
    // Msg props
    // --------------------------------------------------
    // Must match MAPIMessage::m_messagePropTags, top of MAPIMessage.cpp
    typedef enum _MessagePropIndex 
    {
        MESSAGE_CLASS, 
        MESSAGE_FLAGS, 
        CLIENT_SUBMIT_TIME, 
        SENDER_ADDRTYPE, 
        SENDER_EMAIL_ADDR,
        SENDER_NAME, 
        SENDER_ENTRYID, 
        SUBJECT, 
        TEXT_BODY, 
        HTML_BODY, 
        INTERNET_CPID,
        MESSAGE_CODEPAGE, 
        LAST_VERB_EXECUTED, 
        FLAG_STATUS, 
        ENTRYID, 
        SENT_ADDRTYPE,
        SENT_ENTRYID, 
        SENT_EMAIL_ADDR, 
        SENT_NAME, 
        REPLY_NAMES, 
        REPLY_ENTRIES,
        MIME_HEADERS, 
        IMPORTANCE, 
        INTERNET_MESSAGE_ID, 
        DELIVERY_DATE, 
        URL_NAME,
        MESSAGE_SIZE, 
        STORE_SUPPORT_MASK, 
        RTF_IN_SYNC, 
        NMSGPROPS
    } MessagePropIndex;

    typedef struct _MessagePropTags
    {
        ULONG cValues;
        ULONG aulPropTags[NMSGPROPS];
    } MessagePropTags;
    static MessagePropTags m_messagePropTags;

    // --------------------------------------------------
    // Recip props
    // --------------------------------------------------
    typedef enum _RecipientPropIndex
    {
        RDISPLAY_NAME, RENTRYID, RADDRTYPE, REMAIL_ADDRESS, RRECIPIENT_TYPE, RNPROPS
    } RecipientPropIndex;

    typedef struct _RecipientPropTags
    {
        ULONG cValues;
        ULONG aulPropTags[RNPROPS];
    } RecipientPropTags;
    static RecipientPropTags m_recipientPropTags;

    // --------------------------------------------------
    // ReplyTo props
    // --------------------------------------------------
    typedef enum _ReplyToPropIndex
    {
        REPLYTO_DISPLAY_NAME, REPLYTO_ENTRYID, REPLYTO_ADDRTYPE, REPLYTO_EMAIL_ADDRESS,
        NREPLYTOPROPS
    } ReplyToPropIndex;

    typedef struct _ReplyToPropTags
    {
        ULONG cValues;
        ULONG aulPropTags[NREPLYTOPROPS];
    } ReplyToPropTags;
    static ReplyToPropTags m_replyToPropTags;


    // --------------------------------------------------
    // Misc
    // --------------------------------------------------
    CHAR m_pDateTimeStr[32];
    CHAR m_pDeliveryDateTimeStr[32];
    CHAR m_pDeliveryUnixDateTimeStr[32];
    CHAR m_pDateUnixDateTimeStr[32];
    std::vector<std::string> RTFElement;
    enum EnumRTFElement
    {
        NOTFOUND = -1, OPENBRACE = 0, CLOSEBRACE, HTMLTAG, MHTMLTAG, PAR, TAB, LI, FI, HEXCHAR,
        PNTEXT, HTMLRTF, OPENBRACEESC, CLOSEBRACEESC, END, HTMLRTF0
    };

    unsigned int CodePageId();

    EnumRTFElement MatchRTFElement(const char *psz);

    const char *Advance(const char *psz, const char *pszCharSet);
};

class MessageIteratorRestriction;

// =============================
// MessageIterator class
// =============================
class MessageIterator: public MAPITableIterator
{
public:
    MessageIterator();
    virtual ~MessageIterator();

    virtual LPSPropTagArray GetTableProps();
    virtual LPSSortOrderSet GetSortOrder();
    virtual LPSRestriction GetRestriction(ULONG TypeMask, FILETIME startDate);

    BOOL GetNext(MAPIMessage &msg);
    BOOL GetNext(__int64 &date, SBinary &bin);

private:
    typedef enum _MessageIterPropTagIdx
    {
        MI_ENTRYID, MI_LONGTERM_ENTRYID_FROM_TABLE, MI_DATE, MI_MESSAGE_CLASS, NMSGPROPS
    } MessageIterPropTagIdx;

    typedef struct _MessageIterPropTags
    {
        ULONG cValues;
        ULONG aulPropTags[NMSGPROPS];
    } MessageIterPropTags;

    typedef struct _MessageIterSort
    {
        ULONG cSorts;
        ULONG cCategories;
        ULONG cExpanded;
        SSortOrder aSort[1];
    } MessageIterSortOrder;

protected:
    static MessageIterPropTags m_TableProps;
    static MessageIterSortOrder m_sortOrder;
    static MessageIteratorRestriction m_restriction;
};

// =================================
// MessageIteratorRestriction class
// =================================
//
// The contents table is restricted so that during the GetFolderItems phase, only a subset
// of messages is returned to c# layer.
//
// The ctor initializes some props that will be used to build the restriction, but it is not
// until GetRestriction() is called that the restriction is actually built.
//
// GetRestriction takes 2 params: 
// - TypeMask: causes the restriction to omit certain message type
// - startDate: ignores messages before a certain date
//
// Having said the above, the two params are currently always set to ZCM_ALL and NULL start date
// which means the restriction is currently hard-coded to the following which for all intents and 
// purposes means  "pass EVERYTHING to c# layer" (the c# layer then skips messages it doesn't want 
// to migrate. This is of course inefficient and it would be better to weed out msgs we dont want
// to migrate at the C++ layer - but for now it seems we're not doing this):

/*
 Restriction Type = RES_OR
 cRes = 8

    Restriction Type = RES_CONTENT
    PropTag   : Id(PR_MESSAGE_CLASS) Type(PT_UNICODE)
    FuzzyLevel: FL_IGNORECASE FL_SUBSTRING
    Value     : ipm.note

    Restriction Type = RES_CONTENT
    PropTag   : Id(PR_MESSAGE_CLASS) Type(PT_UNICODE)
    FuzzyLevel: FL_IGNORECASE FL_SUBSTRING
    Value     : ipm

    Restriction Type = RES_CONTENT
    PropTag   : Id(PR_MESSAGE_CLASS) Type(PT_UNICODE)
    FuzzyLevel: FL_IGNORECASE FL_SUBSTRING
    Value     : ipm.post

    Restriction Type = RES_CONTENT
    PropTag   : Id(PR_MESSAGE_CLASS) Type(PT_UNICODE)
    FuzzyLevel: FL_IGNORECASE FL_SUBSTRING
    Value     : ipm.contact

    Restriction Type = RES_CONTENT
    PropTag   : Id(PR_MESSAGE_CLASS) Type(PT_UNICODE)
    FuzzyLevel: FL_IGNORECASE FL_SUBSTRING
    Value     : ipm.distlist

    Restriction Type = RES_CONTENT
    PropTag   : Id(PR_MESSAGE_CLASS) Type(PT_UNICODE)
    FuzzyLevel: FL_IGNORECASE FL_SUBSTRING
    Value     : ipm.task

    Restriction Type = RES_CONTENT
    PropTag   : Id(PR_MESSAGE_CLASS) Type(PT_UNICODE)
    FuzzyLevel: FL_IGNORECASE FL_SUBSTRING
    Value     : ipm.appointment

    Restriction Type = RES_CONTENT
    PropTag   : Id(PR_MESSAGE_CLASS) Type(PT_UNICODE)
    FuzzyLevel: FL_IGNORECASE FL_SUBSTRING
    Value     : ipm.schedule

*/

// DCB_BUG_103092 Note that Notes class IPM.StickyNote is absent from above, which means we never return Notes to c# layer


//
//
//
class MessageIteratorRestriction
{
public:
    MessageIteratorRestriction();
    ~MessageIteratorRestriction();

    LPSRestriction GetRestriction(ULONG TypeMask, FILETIME startDate);

private:
    SRestriction pR[25];

    SPropValue _propValCont;
    SPropValue _propValMail;
    SPropValue _propValCTime;
    SPropValue _propValSTime;
    SPropValue _propValCanbeMail;
    SPropValue _propValCanbeMailPost;

    SPropValue _propValAppt;
    LPWSTR _pApptClass;

    SPropValue _propValTask;
    LPWSTR _pTaskClass;

    SPropValue _propValReqAndRes;
    LPWSTR _pReqAndResClass;

    SPropValue _propValDistList;
    LPWSTR _pDistListClass;

    LPWSTR _pContactClass;
    LPWSTR _pMailClass;
    SPropValue _propValIMAPHeaderOnly;
};

mimepp::Mailbox *MakeMimePPMailbox(LPTSTR pDisplayName, LPTSTR pSmtpAddress);
}
}
