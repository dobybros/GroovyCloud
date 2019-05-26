package script;

import chat.errors.CoreException;

public abstract class ScriptRuntime {
	private Long version;
	
	protected String path;

	public abstract void init() throws CoreException;
	
	public abstract void start() throws CoreException;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public abstract void close();
}
