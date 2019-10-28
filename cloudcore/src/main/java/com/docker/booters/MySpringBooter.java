package com.docker.booters;

import com.docker.script.MyBaseRuntime;
import com.docker.utils.ScriptUtils;
import script.groovy.runtime.SpringBooter;

/**
 * Created by lick on 2019/5/15.
 * Description：
 */
public class MySpringBooter extends SpringBooter {
    private static final String TAG = MySpringBooter.class.getSimpleName();
    public MySpringBooter() {
    }
    public void beforeDeploy() {
        MyBaseRuntime myBaseRuntime = (MyBaseRuntime) getGroovyRuntime();
        ScriptUtils.serviceStubProxy(myBaseRuntime, TAG);
    }
}
