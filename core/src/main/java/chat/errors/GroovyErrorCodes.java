package chat.errors;



/**
 */
public interface GroovyErrorCodes {
	public static final int CODE_CORE = 18000;

	//Groovy related codes. 
	public static final int ERROR_GROOVY_CLASSNOTFOUND = CODE_CORE + 1;
	public static final int ERROR_GROOY_NEWINSTANCE_FAILED = CODE_CORE + 2;
	public static final int ERROR_GROOY_CLASSCAST = CODE_CORE + 3;
	public static final int ERROR_GROOVY_INVOKE_FAILED = CODE_CORE + 4;
	public static final int ERROR_GROOVYSERVLET_SERVLET_NOT_INITIALIZED = CODE_CORE + 5;
	public static final int ERROR_URL_PARAMETER_NULL = CODE_CORE + 6;
	public static final int ERROR_URL_VARIABLE_NULL = CODE_CORE + 7;
	public static final int ERROR_GROOVY_PARSECLASS_FAILED = CODE_CORE + 8;
	public static final int ERROR_GROOVY_UNKNOWN = CODE_CORE + 9;
	public static final int ERROR_GROOVY_CLASSLOADERNOTFOUND = CODE_CORE + 10;
	public static final int ERROR_JAVASCRIPT_LOADFILE_FAILED = CODE_CORE + 11;
	public static final int ERROR_URL_HEADER_NULL = CODE_CORE + 12;

}
