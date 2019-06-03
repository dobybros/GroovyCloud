package chat.errors;


import java.util.HashMap;
import java.util.List;


public class CoreException extends Exception {
	private static final long serialVersionUID = -3101325177490138661L;
	private List<CoreException> moreExceptions;
	private HashMap<String, Object> infoMap;
	private Object data;

	public CoreException() {}

	public CoreException(int code) {
	    this.code = code;
	}
	
	public CoreException(int code, String[] parameters, String message) {
		this(code, message);
		this.parameters = parameters;
	}

	public CoreException(int code, Object data, String message) {
		this(code, message);
		this.data = data;
	}

	public CoreException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public CoreException(int code, String message, Throwable throwable) {
		super(message, throwable);
		this.code = code;
	}
	
	public CoreException(String message) {
		super(message);
	}
	
	public CoreException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	public int getCode() {
		return code;
	}
	public String[] getParameters() {
		return parameters;
	}
	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}
	private int code;
	private String[] parameters;
	/**
	 * 
	 */
	public String toString() {
		return code + "|" + this.getMessage();
	}

	public List<CoreException> getMoreExceptions() {
		return moreExceptions;
	}

	public void setMoreExceptions(List<CoreException> moreExceptions) {
		this.moreExceptions = moreExceptions;
	}
	
	public CoreException setInfo(String key, Object value) {
		if(infoMap == null) 
			infoMap = new HashMap<String, Object>();
		infoMap.put(key, value);
		return this;
	}
	
	public Object getInfo(String key) {
		if(infoMap == null)
			return null;
		return infoMap.get(key);
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}