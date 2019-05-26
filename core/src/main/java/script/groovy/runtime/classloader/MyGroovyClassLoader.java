package script.groovy.runtime.classloader;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import script.groovy.runtime.GroovyRuntime;

import java.util.HashMap;

public class MyGroovyClassLoader extends GroovyClassLoader {
    private long version;
    private GroovyRuntime groovyRuntime;
    public MyGroovyClassLoader(ClassLoader parentClassLoader,
                               CompilerConfiguration cc, GroovyRuntime groovyRuntime) {
        super(parentClassLoader, cc);
        this.groovyRuntime = groovyRuntime;
    }

    public GroovyRuntime getGroovyRuntime() {
        return groovyRuntime;
    }

    public long getVersion() {
        return version;
    }

    public String toString() {
        return MyGroovyClassLoader.class.getSimpleName() + "#" + version;
    }

    public ClassHolder getClass(String classStr) {
        if(classStr.endsWith(".groovy")) {
            classStr = classStr.substring(0, classStr.length() - 7).replace("/", ".");
        }
        return groovyRuntime.getCachedClasses().get(classStr);
    }

    public static MyGroovyClassLoader newClassLoader(ClassLoader parentClassLoader, GroovyRuntime groovyRuntime) {
        CompilerConfiguration cc = new CompilerConfiguration();
        // cc.setMinimumRecompilationInterval(0);
        // cc.setRecompileGroovySource(true);
        cc.setSourceEncoding("utf8");
        // cc.setTargetDirectory("/home/momo/Aplomb/workspaces/workspace (server)/Group/");
        cc.setClasspath(groovyRuntime.getPath());

        // cc.setDebug(true);
        cc.setRecompileGroovySource(false);
        cc.setMinimumRecompilationInterval(Integer.MAX_VALUE);
        cc.setVerbose(false);
        cc.setDebug(false);
//		try {
//			cc.setOutput(new PrintWriter(new File("/Users/aplomb/Dev/taineng/test/log.txt")));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}

        return new MyGroovyClassLoader(parentClassLoader, cc, groovyRuntime);
    }

    public void setVersion(long version) {
        this.version = version;
    }
}