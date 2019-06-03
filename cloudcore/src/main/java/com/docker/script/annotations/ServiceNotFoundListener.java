package com.docker.script.annotations;

import com.docker.script.BaseRuntime;

public interface ServiceNotFoundListener {
	public BaseRuntime getRuntimeWhenNotFound(String service);
}
