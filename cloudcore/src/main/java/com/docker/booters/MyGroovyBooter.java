package com.docker.booters;

import com.docker.script.MyBaseRuntime;
import com.docker.utils.ScriptUtils;
import script.groovy.runtime.GroovyBooter;

/**
 * Created by lick on 2019/5/15.
 * Description：
 */
public class MyGroovyBooter extends GroovyBooter {
    private static final String TAG = MyGroovyBooter.class.getSimpleName();
    public MyGroovyBooter() {
    }
    public void beforeDeploy() {
        MyBaseRuntime myBaseRuntime = (MyBaseRuntime) getGroovyRuntime();
        if(myBaseRuntime != null){
            ScriptUtils.serviceStubProxy(myBaseRuntime, TAG);
        }
    }
}
