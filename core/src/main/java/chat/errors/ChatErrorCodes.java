package chat.errors;



/**
 * code -19999~-10000
 */
public interface ChatErrorCodes {
	public static final int CODE_CORE = -10000;

	public static final int ERROR_ONLINESERVER_NOT_FOUND = CODE_CORE - 121;
	public static final int ERROR_ONLINESERVER_UPDATE_FAILED = CODE_CORE - 122;
	public static final int ERROR_ONLINESERVER_DELETE_FAILED = CODE_CORE - 123;
	public static final int ERROR_ONLINESERVER_ADD_FAILED = CODE_CORE - 124;
	public static final int ERROR_ONLINESERVER_QUERY_FAILED = CODE_CORE - 125;

	public static final int ERROR_USERPRESENT_ADD_FAILED = CODE_CORE - 126;
	public static final int ERROR_USERPRESENT_UPDATE_FAILED = CODE_CORE - 127;
	public static final int ERROR_USERPRESENT_QUERY_FAILED = CODE_CORE - 128;
	public static final int ERROR_USERPRESENT_NOTFOUND = CODE_CORE - 129;
	public static final int ERROR_USERPRESENT_DELETE_FAILED = CODE_CORE - 130;
	public static final int ERROR_ILLEGAL_PARAMETER = CODE_CORE - 131;
	public static final int ERROR_SSH_CONNECT_FAILED = CODE_CORE - 132;
	public static final int ERROR_SSH_EXEC_FAILED = CODE_CORE - 133;
	public static final int ERROR_MESSAGEADD_FAILED = CODE_CORE - 134;
	public static final int ERROR_ITERATOR_NULL = CODE_CORE - 135;
	public static final int ERROR_UNKNOWN = CODE_CORE - 136;
	public static final int ERROR_CORE_LOADRESOURCE_FAILED = CODE_CORE - 137;
	public static final int ERROR_CORE_LOADRESOURCE_NOT_EXIST = CODE_CORE - 138;
	public static final int ERROR_CORE_UPLOAD_DB_FAILED = CODE_CORE - 139;
	public static final int ERROR_CORE_ZKDATA_PERSISTENT_FAILED = CODE_CORE - 140;
	public static final int ERROR_CORE_ZKENSUREPATH_FAILED = CODE_CORE - 141;
	public static final int ERROR_CORE_ZKGETDATA_FAILED = CODE_CORE - 142;
	public static final int ERROR_CORE_ZKDATA_RESURRECT_FAILED = CODE_CORE - 143;
	public static final int ERROR_CORE_ZKDATA_NEWINSTANCE_FAILED = CODE_CORE - 144;
	public static final int ERROR_CORE_ZKADDWATCHEREX_FAILED = CODE_CORE - 145;
	public static final int ERROR_ZK_DISCONNECTED = CODE_CORE - 146;
	public static final int ERROR_CORE_SERVERPORT_ILLEGAL = CODE_CORE - 147;
	public static final int ERROR_DAOINIT_FAILED = CODE_CORE - 148;
	public static final int ERROR_ILLEGAL_ENCODE = CODE_CORE - 149;
	public static final int ERROR_READCONTENT_FAILED = CODE_CORE - 150;
	public static final int ERROR_UPLOAD_FAILED = CODE_CORE - 151;
	public static final int ERROR_PARSE_REQUEST_FAILED = CODE_CORE - 152;
	public static final int ERROR_ACCOUNTNAME_ILLEGAL = CODE_CORE - 153;
	public static final int ERROR_ACCOUNTDOMAIN_ILLEGAL = CODE_CORE - 154;
	public static final int ERROR_IO_FAILED = CODE_CORE - 155;
	public static final int ERROR_LOGINUSER_NOT_FOUND = CODE_CORE - 156;
	public static final int ERROR_BALANCER_NOT_READY = CODE_CORE - 157;
	public static final int ERROR_CHARACTER_OVER_MAXIMUM_LIMITS = CODE_CORE - 158;
	public static final int ERROR_TASKADD_FAILED = CODE_CORE - 159;
	public static final int ERROR_FILE_EMPTY = CODE_CORE - 160;
	public static final int ERROR_RPC_TYPE_NOMAPPING = CODE_CORE - 161;
	public static final int ERROR_RPC_TYPE_NOSERVERADAPTER = CODE_CORE - 162;
	public static final int ERROR_RPC_DISCONNECTED = CODE_CORE - 163;
	public static final int ERROR_RPC_REQUESTTYPE_ILLEGAL = CODE_CORE - 164;
	public static final int ERROR_RPC_REQUESTDATA_NULL = CODE_CORE - 165;
	public static final int ERROR_RPC_PERSISTENT_FAILED = CODE_CORE - 166;
	public static final int ERROR_RPC_RESURRECT_FAILED = CODE_CORE - 167;
	public static final int ERROR_RMICALL_CONNECT_FAILED = CODE_CORE - 168;
	public static final int ERROR_RMICALL_FAILED = CODE_CORE - 169;
	public static final int ERROR_RPC_ILLEGAL = CODE_CORE - 170;
	public static final int ERROR_RPC_TYPE_REQUEST_NOMAPPING = CODE_CORE - 171;
	public static final int ERROR_GROOVY_PARSECLASS_FAILED = CODE_CORE - 172;
	public static final int ERROR_GROOVY_UNKNOWN = CODE_CORE - 173;
	public static final int ERROR_GROOVYSERVLET_SERVLET_NOT_INITIALIZED = CODE_CORE - 174;
	public static final int ERROR_URL_VARIABLE_NULL = CODE_CORE - 175;
	public static final int ERROR_URL_PARAMETER_NULL = CODE_CORE - 176;
	public static final int ERROR_URL_HEADER_NULL = CODE_CORE - 177;
	public static final int ERROR_JAVASCRIPT_LOADFILE_FAILED = CODE_CORE - 178;
	public static final int ERROR_GROOY_CLASSCAST = CODE_CORE - 179;
	public static final int ERROR_METHODMAPPING_METHOD_NULL = CODE_CORE - 180;
	public static final int ERROR_METHODMAPPING_INSTANCE_NULL = CODE_CORE - 181;
	public static final int ERROR_METHODMAPPING_ACCESS_FAILED = CODE_CORE - 182;
	public static final int ERROR_METHODMAPPING_INVOKE_FAILED = CODE_CORE - 183;
	public static final int ERROR_METHODMAPPING_INVOKE_UNKNOWNERROR = CODE_CORE - 184;
	public static final int ERROR_POST_FAILED = CODE_CORE - 185;
	public static final int ERROR_LANSERVERS_NOSERVERS = CODE_CORE - 186;
	public static final int ERROR_RPC_CALLREMOTE_FAILED = CODE_CORE - 187;
	public static final int ERROR_METHODRESPONSE_NULL = CODE_CORE - 188;
	public static final int ERROR_METHODREQUEST_CRC_ILLEGAL = CODE_CORE - 189;
	public static final int ERROR_METHODREQUEST_METHODNOTFOUND = CODE_CORE - 190;
	public static final int ERROR_RPC_DECODE_FAILED = CODE_CORE - 191;
	public static final int ERROR_RPC_ENCODER_NOTFOUND = CODE_CORE - 192;
	public static final int ERROR_RPC_ENCODER_NULL = CODE_CORE - 193;
	public static final int ERROR_RPC_ENCODE_FAILED = CODE_CORE - 194;
	public static final int ERROR_METHODREQUEST_SERVICE_NOTFOUND = CODE_CORE - 195;
	public static final int ERROR_METHODREQUEST_SKELETON_NULL = CODE_CORE - 196;
	public static final int ERROR_METHODREQUEST_SERVICE_NULL = CODE_CORE - 197;

	public static final int ERROR_SDOCKER_QUERY_FAILED = CODE_CORE - 201;
	public static final int ERROR_GET_FAILED = CODE_CORE - 202;
	public static final int ERROR_NO_REMOTESERVERS= CODE_CORE - 203;
	public static final int ERROR_NO_BASERUNTIME= CODE_CORE - 204;
	public static final int ERROR_NO_GROOVYFILE= CODE_CORE - 205;
	public static final int ERROR_DISCOVERY_NOTFOUND= CODE_CORE - 206;
	public static final int ERROR_ASYNC_ERROR= CODE_CORE - 207;
	public static final int ERROR_ASYNC_TIMEOUT= CODE_CORE - 208;
	public static final int ERROR_QUARTZ_ADDFAILED =  CODE_CORE - 209;
	public static final int ERROR_QUARTZ_CANCELEDFAILED= CODE_CORE - 210;
	public static final int ERROR_SERVER_CONNECT_FAILED= CODE_CORE - 211;
	/*******************ehcache******************/
	public static final int ERROR_CREATE_CACHE= CODE_CORE - 212;
	public static final int ERROR_CACHE_ILLEGAL_PARAMETER= CODE_CORE - 213;
	public static final int ERROR_CACHE_PUT= CODE_CORE - 214;
	public static final int ERROR_CACHE_GET= CODE_CORE - 215;

	public static final int ERROR_REMOTEERVICE_CONCURRENTLIMIT= CODE_CORE - 216;
	public static final int ERROR_ASYNC_NEEDRETRY= CODE_CORE - 217;

	public static final int ERROR_REMOTE_RPC_FAILED= CODE_CORE - 218;
}
