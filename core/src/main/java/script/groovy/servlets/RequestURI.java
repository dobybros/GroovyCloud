package script.groovy.servlets;

public class RequestURI {
	public static final String VARIABLE = "VARIABLE";
	
	private String[] uri;
	private String method;
	
	private String groovyPath;
	private String groovyMethod;
	
	public RequestURI(String uri, String method, String groovyPath, String groovyMethod) {
		this.uri = uri.split("/");
		this.method = method;
		this.groovyPath = groovyPath;
		this.groovyMethod = groovyMethod;
	}

	public String[] getUri() {
		return uri;
	}

	public String getMethod() {
		return method;
	}

	public String getGroovyPath() {
		return groovyPath;
	}

	public String getGroovyMethod() {
		return groovyMethod;
	}

}
