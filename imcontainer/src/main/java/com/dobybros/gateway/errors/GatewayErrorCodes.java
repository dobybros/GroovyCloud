package com.dobybros.gateway.errors;

public class GatewayErrorCodes {
	//Error code between 8000 and 9000 will force close tcp channel, but will wait for push every message in tcp channel
	public static final int TCPCHANNEL_CLOSE_START = 8000;
	public static final int ERROR_TCPCHANNEL_MISSING_ONLINEUSER = TCPCHANNEL_CLOSE_START + 1;
	public static final int TCPCHANNEL_CLOSE_END = 8499;

	//Error code between 8500 and 9000 will force close tcp channel and ignore the messages in tcp channel
	public static final int TCPCHANNEL_CLOSE_IMMEDIATELY_START = 8500;
	public static final int ERROR_TCPCHANNEL_IDENTIFY_FAILED = TCPCHANNEL_CLOSE_IMMEDIATELY_START + 1;
	public static final int ERROR_TCPCHANNEL_ALREADY_CREATED = TCPCHANNEL_CLOSE_IMMEDIATELY_START + 2;
	public static final int ERROR_HAILPACK_UNKNOWNERROR = TCPCHANNEL_CLOSE_IMMEDIATELY_START + 3;
	public static final int ERROR_HAILPACK_IO_ERROR = TCPCHANNEL_CLOSE_IMMEDIATELY_START + 4;
	public static final int ERROR_TCPCHANNEL_ENCODE_ILLEGAL = TCPCHANNEL_CLOSE_IMMEDIATELY_START + 5;
	public static final int ERROR_TCPCHANNEL_UNKNOWN = TCPCHANNEL_CLOSE_IMMEDIATELY_START + 6;
	public static final int ERROR_TCPCHANNEL_NOLONGER_ONLINE = TCPCHANNEL_CLOSE_IMMEDIATELY_START + 7;
	public static final int TCPCHANNEL_CLOSE_IMMEDIATELY_END = 9000;
	
	public static final int GATEWAY_ERROR_START = 1000;
	public static final int ERROR_ILLEGAL_PARAMETER = GATEWAY_ERROR_START + 1;
	public static final int ERROR_TERMINAL_ILLEGAL = GATEWAY_ERROR_START + 2;
	public static final int ERROR_DEVICETOKEN_ILLEGAL = GATEWAY_ERROR_START + 3;
	public static final int ERROR_PASSWORD_ILLEGAL = GATEWAY_ERROR_START + 4;
	public static final int ERROR_ACCOUNTNAME_ILLEGAL = GATEWAY_ERROR_START + 5;
	public static final int ERROR_ID_ILLEGAL = GATEWAY_ERROR_START + 6;
	public static final int ERROR_USERSTATUS_NOT_FOUND = GATEWAY_ERROR_START + 7;
	public static final int ERROR_USERSTATUS_ADD_FAILED = GATEWAY_ERROR_START + 8;
	public static final int ERROR_LOGINACCOUNT_NOT_FOUND = GATEWAY_ERROR_START + 9;
	public static final int ERROR_USERSTATUS_QUERY_DB_FAILED = GATEWAY_ERROR_START + 10;
	public static final int ERROR_OAUTH_LOGINURL_NULL = GATEWAY_ERROR_START + 11;
	public static final int ERROR_URLENCODE_FAILED = GATEWAY_ERROR_START + 12;
	public static final int ERROR_TERMINAL_NOT_EXIST = GATEWAY_ERROR_START + 13;
	public static final int ERROR_USERID_NOT_EXIST = GATEWAY_ERROR_START + 14;
	public static final int ERROR_USERLOGIN_LOCK_FAILED = GATEWAY_ERROR_START + 15;
	public static final int ERROR_HTTPCHANNEL_KICKED = GATEWAY_ERROR_START + 16;
	public static final int ERROR_LOGINUSER_NOT_FOUND = GATEWAY_ERROR_START + 17;
	public static final int ERROR_LOGINUSER_USERPRESENT_NOTFOUND = GATEWAY_ERROR_START + 18;
	public static final int ERROR_LOGINUSER_USERPRESENT_ILLEGAL = GATEWAY_ERROR_START + 19;
	public static final int ERROR_MESSAGE_PARENTTOPICID_ILLEGAL = GATEWAY_ERROR_START + 20;
	public static final int ERROR_MESSAGE_CONTENT_ILLEGAL = GATEWAY_ERROR_START + 21;
	public static final int ERROR_MESSAGE_CONTENTTYPE_ILLEGAL = GATEWAY_ERROR_START + 22;
	public static final int ERROR_OFFLINETOPIC_SAVE_FAILED = GATEWAY_ERROR_START + 23;
	public static final int ERROR_OFFLINETOPIC_QUERY_FAILED = GATEWAY_ERROR_START + 24;
	public static final int ERROR_OFFLINETOPIC_REMOVE_FAILED = GATEWAY_ERROR_START + 25;
	public static final int ERROR_OFFLINETOPIC_MISSING_PENDINGUSERIDS = GATEWAY_ERROR_START + 26;
	public static final int ERROR_MESSAGE_MISSING_CLIENTID = GATEWAY_ERROR_START + 27;
	public static final int ERROR_MESSAGE_MISSING_PARTICIPANTIDS = GATEWAY_ERROR_START + 28;
	public static final int ERROR_OFFLINETOPIC_MISSING_MESSAGE = GATEWAY_ERROR_START + 29;
	public static final int ERROR_ADDTOPICEVENT_MISSING_TOPIC = GATEWAY_ERROR_START + 30;
	public static final int ERROR_SERVERNAME_NULL = GATEWAY_ERROR_START + 31;
	public static final int ERROR_LOGIN_SWITCHUSER = GATEWAY_ERROR_START + 32;
	public static final int ERROR_DOWNLOAD_FAILED = GATEWAY_ERROR_START + 33;
	public static final int ERROR_FILE_NOTFOUND = GATEWAY_ERROR_START + 34;
	public static final int ERROR_USERFOLLOW_QUERY_FAILED = GATEWAY_ERROR_START + 35;
	public static final int ERROR_ONLINEUSER_NOT_INITIALIZED = GATEWAY_ERROR_START + 36;
	public static final int ERROR_FOLLOWUSERIDS_QUERY_FAILED = GATEWAY_ERROR_START + 37;
	public static final int ERROR_FOLLOWUSER_ALREADYFOLLOWED = GATEWAY_ERROR_START + 38;
	public static final int ERROR_FOLLOWUSER_NOTFOLLOWEDYET = GATEWAY_ERROR_START + 39;
	public static final int ERROR_CONTACTGROUP_SAVE_FAILED = GATEWAY_ERROR_START + 40;
	public static final int ERROR_CONTACTGROUP_QUERY_FAILED = GATEWAY_ERROR_START + 41;
	public static final int ERROR_USERUPDATE_FAILED = GATEWAY_ERROR_START + 42;
	public static final int ERROR_CONTACTGROUP_DELETE_FAILED = GATEWAY_ERROR_START + 43;
	public static final int ERROR_CONTACTGROUP_UPDATE_FAILED = GATEWAY_ERROR_START + 44;
	public static final int ERROR_CONTACTGROUP_NAME_NULL = GATEWAY_ERROR_START + 45;
	public static final int ERROR_CONTACTGROUP_NOUSER = GATEWAY_ERROR_START + 46;
	public static final int ERROR_CONTACTGROUP_TYPE_ILLEGAL = GATEWAY_ERROR_START + 47;
	public static final int ERROR_USERLOCATION_ADD_FAILED = GATEWAY_ERROR_START + 48;	
	public static final int ERROR_LOCATION_QUERY_FAILED = GATEWAY_ERROR_START + 49;
	public static final int ERROR_OFFSET_VALUE_WRONG = GATEWAY_ERROR_START + 50;
	public static final int ERROR_LONGITUDE_VALUE_WRONG = GATEWAY_ERROR_START + 51;
	public static final int ERROR_LATITUDE_VALUE_WRONG = GATEWAY_ERROR_START + 52;
	public static final int ERROR_LIMIT_VALUE_WRONG = GATEWAY_ERROR_START + 53;
	public static final int ERROR_EXCEEDED_MAX_MESSAGE = GATEWAY_ERROR_START + 54;
	public static final int ERROR_NO_VERIFICATION_CODE = GATEWAY_ERROR_START + 55;
	public static final int ERROR_WRONG_VERIFICATION_CODE = GATEWAY_ERROR_START + 56;
	public static final int ERROR_EXCEEDED_MAX_CHECK = GATEWAY_ERROR_START + 57;
	public static final int ERROR_VERIFICATION_CODE_TIMEOUT = GATEWAY_ERROR_START + 58;
	public static final int ERROR_EXCEEDED_MAX_STRANGER_TALK = GATEWAY_ERROR_START + 59;
	public static final int ERROR_USERBLOCK_QUERY_FAILED = GATEWAY_ERROR_START + 60;
	public static final int ERROR_BLOCKUSER_ALREADYFOLLOWED = GATEWAY_ERROR_START + 61;
	public static final int ERROR_BLOCKUSER_NOTFOLLOWEDYET = GATEWAY_ERROR_START + 62;
	public static final int ERROR_BLOCKUSER_ALREADYBLOCKED = GATEWAY_ERROR_START + 63;
	public static final int ERROR_BLOCKUSER_NOTBLOCKEDYET = GATEWAY_ERROR_START + 64;
	public static final int ERROR_EXCEEDED_MAX_FOLLOWUSERS = GATEWAY_ERROR_START + 65;
	public static final int ERROR_DEVICE_TOKEN_UPDATE_LOCATION = GATEWAY_ERROR_START + 66;
	public static final int ERROR_GET_OLDMISSID_WRONG = GATEWAY_ERROR_START + 67;
	public static final int ERROR_ALREADY_CHANGED_MISSID = GATEWAY_ERROR_START + 68;
	public static final int ERROR_DISTANCE_VALUE_WRONG = GATEWAY_ERROR_START + 69;
	public static final int ERROR_MISSID_FIRST_CHARACTER_WRONG = GATEWAY_ERROR_START + 70;
	public static final int ERROR_LOGOUT_FAILED = GATEWAY_ERROR_START + 71;
	public static final int ERROR_ONLINEUSER_NULL = GATEWAY_ERROR_START + 72;
	public static final int ERROR_EXCEEDED_MAX_NEARBY_USERS = GATEWAY_ERROR_START + 73;
	public static final int ERROR_UNREADCOUNT_VALUE_WRONG = GATEWAY_ERROR_START + 74;
	public static final int ERROR_USER_KICKED_BY_LOGIN_OTHER_DEVICE = GATEWAY_ERROR_START + 75;
	public static final int ERROR_FOLLOWEVENT_MISSING_FOLLOWTIME = GATEWAY_ERROR_START + 76;
	public static final int ERROR_FOLLOWEVENT_MISSING_SENDERUSERID = GATEWAY_ERROR_START + 77;
	public static final int ERROR_FOLLOWEVENT_MISSING_FOLLOW_TYPE = GATEWAY_ERROR_START + 78;
	public static final int ERROR_BLOCKEVENT_MISSING_FOLLOWTIME = GATEWAY_ERROR_START + 79;
	public static final int ERROR_BLOCKEVENT_MISSING_FOLLOW_TYPE = GATEWAY_ERROR_START + 80;
	public static final int ERROR_UNDEVELOPED_YET = GATEWAY_ERROR_START + 81;
	public static final int ERROR_DEVICEUSER_CANNOT_UPDATE_PROFILE = GATEWAY_ERROR_START + 82;
	public static final int ERROR_ONLY_DEVICEUSER_CAN_REGIST = GATEWAY_ERROR_START + 83;
	public static final int ERROR_MOMENT_CITY_NULL = GATEWAY_ERROR_START + 84;
	public static final int ERROR_LOCATE_ERROR = GATEWAY_ERROR_START + 85;
	public static final int ERROR_MOMENT_ID_NULL = GATEWAY_ERROR_START + 86;
	public static final int ERROR_MOMENT_DB_QUERY_FAILED = GATEWAY_ERROR_START + 87;
	public static final int ERROR_ADD_COMMENT_FLOOR_INC_FAILED = GATEWAY_ERROR_START + 88;
	public static final int ERROR_ADD_COMMENT_FAILED = GATEWAY_ERROR_START + 89;
	public static final int ERROR_GENDER_NULL = GATEWAY_ERROR_START + 90;
	public static final int ERROR_NORMAL_USER_DELETE_COMMENT = GATEWAY_ERROR_START + 91;
	public static final int ERROR_BLOCKUSER_BLOCK_ADMIN = GATEWAY_ERROR_START + 92;
	public static final int ERROR_LOCATION_INFO_INCOMPLETE = GATEWAY_ERROR_START + 93;
	public static final int ERROR_USER_KICKED_BY_FORBIDDEN = GATEWAY_ERROR_START + 94;
	public static final int ERROR_MOMENT_NULL = GATEWAY_ERROR_START + 95;
	public static final int ERROR_PASSWORD_NULL = GATEWAY_ERROR_START + 96;
	public static final int ERROR_ONLY_DEVICEUSER_CAN_FORGET_PASSWORD = GATEWAY_ERROR_START + 97;
	public static final int ERROR_USER_NOT_EXIST = GATEWAY_ERROR_START + 98;
	public static final int ERROR_EMAIL_NULL = GATEWAY_ERROR_START + 99;
	public static final int ERROR_SEND_EMAIL_FAILED = GATEWAY_ERROR_START + 100;
	public static final int ERROR_GENDER_WRONG = GATEWAY_ERROR_START + 101;
	public static final int ERROR_ADDCOMMENTEVENT_MISSING_COMMENT = GATEWAY_ERROR_START + 102;
	public static final int ERROR_DELETECOMMENTEVENT_MISSING_COMMENT = GATEWAY_ERROR_START + 103;
	public static final int ERROR_COMMENT_NULL = GATEWAY_ERROR_START + 104;
	public static final int ERROR_GEODBPATH_NULL = GATEWAY_ERROR_START + 105;
	public static final int ERROR_USERLOGIN_ANOTHERSERVER = GATEWAY_ERROR_START + 106;
	public static final int ERROR_ADDMOMENTLIKEEVENT_MISSING_MOMENTID = GATEWAY_ERROR_START + 107;
	public static final int ERROR_LOCATE_FAILED = GATEWAY_ERROR_START + 108;
	public static final int ERROR_MESSAGESENDING_NOTPREPARED = GATEWAY_ERROR_START + 109;
	public static final int ERROR_RPCMESSAGE_JSONNULL = GATEWAY_ERROR_START + 110;
	public static final int ERROR_RPCMESSAGE_RESURRECT_FAILED = GATEWAY_ERROR_START + 111;
	public static final int ERROR_RPCMESSAGE_TYPE_NULL = GATEWAY_ERROR_START + 112;
	public static final int ERROR_MESSAGE_TARGETIDSSIZE_CANNOTEXCEED_ONE = GATEWAY_ERROR_START + 113;
	public static final int ERROR_USERIDS_NULL = GATEWAY_ERROR_START + 114;
	public static final int ERROR_LOGINACCOUNTS_NULL = GATEWAY_ERROR_START + 115;
	public static final int ERROR_VISIT_PULL_FAILED = GATEWAY_ERROR_START + 116;
	public static final int ERROR_VISIT_PUSH_FAILED = GATEWAY_ERROR_START + 117;
	public static final int ERROR_VISIT_QUERY_FAILED = GATEWAY_ERROR_START + 118;
	public static final int ERROR_EXCEEDED_MAX_VISITORS = GATEWAY_ERROR_START + 119;
	public static final int ERROR_FACEBOOK_ACCOUNT_NULL = GATEWAY_ERROR_START + 120;
	public static final int ERROR_ALREADY_BINDED_WITH_FACEBOOK = GATEWAY_ERROR_START + 121;
	public static final int ERROR_NO_EMAILACCOUNT = GATEWAY_ERROR_START + 122;
	public static final int ERROR_NO_FACEBOOKACCOUNT = GATEWAY_ERROR_START + 123;
	public static final int ERROR_UNBIND_ERROR = GATEWAY_ERROR_START + 124;
	public static final int ERROR_PERMUTE_ERROR = GATEWAY_ERROR_START + 125;
	public static final int ERROR_SITCKERSUIT_NOT_FOUND = GATEWAY_ERROR_START + 126;
	public static final int ERROR_REPORT_NULL = GATEWAY_ERROR_START + 127;
	public static final int ERROR_CHATSESSIONID_INVALID = GATEWAY_ERROR_START + 128;
	public static final int ERROR_CONTACTGROUP_CONNECTED_FAILED = GATEWAY_ERROR_START + 129;
	public static final int ERROR_CONTACTGROUP_CREAOR_NULL = GATEWAY_ERROR_START + 130;
	public static final int ERROR_CONTACTGROUP_NUMBER_NULL = GATEWAY_ERROR_START + 131;
	public static final int ERROR_CONTACTGROUP_LOCATION_NULL = GATEWAY_ERROR_START + 132;
	public static final int ERROR_CONTACTGROUP_PLACEID_NULL = GATEWAY_ERROR_START + 133;
	public static final int ERROR_CONTACTGROUP_LOCATIONNAME_NULL = GATEWAY_ERROR_START + 134;
	public static final int ERROR_CONTACTGROUP_DESCRIPTION_NULL = GATEWAY_ERROR_START + 135;
	public static final int ERROR_CONTACTGROUP_ICONS_NULL = GATEWAY_ERROR_START + 136;
	public static final int ERROR_CONTACTGROUP_WRONG_PERMISSION = GATEWAY_ERROR_START + 137;
	public static final int ERROR_CONTACTGROUP_ADD_ADMIN_FAILED = GATEWAY_ERROR_START + 138;
	public static final int ERROR_CONTACTGROUP_REMOVE_MEMBER_FAILED = GATEWAY_ERROR_START + 139;
	public static final int ERROR_ADD_APPLY_INTERVAL_WRONG = GATEWAY_ERROR_START + 140;
	public static final int ERROR_APPLY_USERID_NULL = GATEWAY_ERROR_START + 141;
	public static final int ERROR_APPLY_GROUPID_NULL = GATEWAY_ERROR_START + 142;
	public static final int ERROR_APPLY_TIME_NULL = GATEWAY_ERROR_START + 143;
	public static final int ERROR_APPLICATION_OVERDUE = GATEWAY_ERROR_START + 144;
	public static final int ERROR_APPLICATION_STATUS_NULL = GATEWAY_ERROR_START + 145;
	public static final int ERROR_APPLICATION_HAS_BEEN_SOLVED = GATEWAY_ERROR_START + 146;
	public static final int ERROR_APPLICATION_APPROVE_FAILED = GATEWAY_ERROR_START + 147;
	public static final int ERROR_CONTACTGROUP_ADD_MEMBER_FAILED = GATEWAY_ERROR_START + 148;
	public static final int ERROR_APPLICATION_REJECT_FAILED = GATEWAY_ERROR_START + 149;
	public static final int ERROR_CONTACTGROUP_UPDATE_NICKNAME_FAILED = GATEWAY_ERROR_START + 150;
	public static final int ERROR_CONTACTGROUP_SETTING_TYPE_ILLEGAL = GATEWAY_ERROR_START + 151;
	public static final int ERROR_CONTACTGROUP_UPDATE_SETTINGS_FAILED = GATEWAY_ERROR_START + 152;
	public static final int ERROR_CONTACTGROUP_LANGUAGE_NULL = GATEWAY_ERROR_START + 153;
	public static final int ERROR_APPLICATION_NULL = GATEWAY_ERROR_START + 154;
	public static final int ERROR_CONTACTGROUPJOINEDEVENT_MISSING_USERID = GATEWAY_ERROR_START + 155;
	public static final int ERROR_CONTACTGROUPDISMISSEDEVENT_MISSING_CONTACTGROUPID = GATEWAY_ERROR_START + 156;
	public static final int ERROR_CONTACTGROUPUPDATEDEVENT_MISSING_USERID = GATEWAY_ERROR_START + 157;
	public static final int ERROR_CONTACTGROUPREMOVEDMEMBERSEVENT_MISSING_REMOVEDUSERIDS = GATEWAY_ERROR_START + 158;
	public static final int ERROR_CONTACTGROUPREMOVEDMEMBERSEVENT_MISSING_CONTACTGROUPID = GATEWAY_ERROR_START + 159;
	public static final int ERROR_CONTACTGROUPNICKNAMECHANGEDEVENT_MISSING_GROUPID = GATEWAY_ERROR_START + 160;
	public static final int ERROR_CONTACTGROUPLEAVEEVENT_MISSING_GROUPID = GATEWAY_ERROR_START + 161;
	public static final int ERROR_CONTACTGROUPAPPLICATIONEVENT_MISSING_GROUPID = GATEWAY_ERROR_START + 162;
	public static final int ERROR_CONTACTGROUPREJECTAPPLICATIONEVENT_MISSING_REJECTEDUSERID = GATEWAY_ERROR_START + 163;
	public static final int ERROR_CONTACTGROUP_MEMBERS_NULL = GATEWAY_ERROR_START + 164;
	public static final int ERROR_CONTACTGROUPADMINCHANGEDEVENT_MISSING_GROUPID = GATEWAY_ERROR_START + 165;
	public static final int ERROR_CONTACTGROUPSTATUSCHANGEDEVENT_MISSING_USERID = GATEWAY_ERROR_START + 166;
	public static final int ERROR_USER_IS_NOT_IN_GROUP = GATEWAY_ERROR_START + 167;
	public static final int ERROR_MOMENT_ICONS_NULL = GATEWAY_ERROR_START + 168;
	public static final int ERROR_GROUP_TICKET_NULL = GATEWAY_ERROR_START + 169;
	public static final int ERROR_GROUP_TICKET_CANNOT_USE_FOR_THIS_BEHAVIOR = GATEWAY_ERROR_START + 170;
	public static final int ERROR_GROUP_TICKET_GROUPID_NULL = GATEWAY_ERROR_START + 171;
	public static final int ERROR_MEMBER_TYPE_NULL = GATEWAY_ERROR_START + 172;
	public static final int ERROR_ALREADY_BINDED_WITH_EMAIL = GATEWAY_ERROR_START + 173;
	public static final int ERROR_EMAIL_ALREDY_EXIST = GATEWAY_ERROR_START + 174;
	public static final int ERROR_BINDING_RELATIONSHIPS_TOO_LESS = GATEWAY_ERROR_START + 175;
	public static final int ERROR_APPVERSION_DOWNLOAD_NULL = GATEWAY_ERROR_START + 176;
	public static final int ERROR_APPVERSION_LATESTVERSION_NULL = GATEWAY_ERROR_START + 177;
	public static final int ERROR_APPVERSION_MINVERSION_NULL = GATEWAY_ERROR_START + 178;
	public static final int ERROR_APPVERSION_RELEASENOTES_NULL = GATEWAY_ERROR_START + 179;
	public static final int ERROR_APPVERSION_UPDATETIME_NULL = GATEWAY_ERROR_START + 180;
	public static final int ERROR_SEARCHRESPONSE_NULL = GATEWAY_ERROR_START + 181;
	public static final int ERROR_NOTIFICATIONEVENT_MISSING_TEXT = GATEWAY_ERROR_START + 182;
	public static final int ERROR_CONTACTGROUP_NOT_FORBIDDEN = GATEWAY_ERROR_START + 183;
	public static final int ERROR_MOMENT_TYPE_WRONG = GATEWAY_ERROR_START + 184;
	public static final int ERROR_CHANNEL_TCP2UDP = GATEWAY_ERROR_START + 185;
	public static final int ERROR_MEMBERSHIPENROLLED_MISSING_VIPORDERID = GATEWAY_ERROR_START + 186;
	public static final int ERROR_MEMBERSHIPEXPIRED_MISSING_VIPORDERID = GATEWAY_ERROR_START + 187;
	public static final int ERROR_USER_NULL = GATEWAY_ERROR_START + 188;
	public static final int ERROR_ONLINESERVICEUSER_NULL = GATEWAY_ERROR_START + 189;
	public static final int ERROR_SERVERINFO_NOTEXPECTED = GATEWAY_ERROR_START + 190;
	public static final int ERROR_SERVER_NULL = GATEWAY_ERROR_START + 191;
	public static final int ERROR_SESSIONCONTEXTATTR_NULL = GATEWAY_ERROR_START + 192;
	public static final int ERROR_SERVER_ISFULL = GATEWAY_ERROR_START + 193;

	public static final int TALENTCHAT_ERROR_END = 2000;
	
}
