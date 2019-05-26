package com.dobybros.chat.script.annotations.presence;

import com.docker.script.MyBaseRuntime;

import java.util.Properties;

public class PresenceGroovyRuntime extends MyBaseRuntime {
	private static final String TAG = PresenceGroovyRuntime.class.getSimpleName();

	public void prepare(String service, Properties properties, String localScriptPath) {
		super.prepare(service, properties, localScriptPath);
	}
}
