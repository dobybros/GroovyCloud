package script.groovy.runtime;

import chat.errors.CoreException;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ant.Groovy;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

/**
 * Created by lick on 2019/5/15.
 * Description：
 */
public interface RuntimeBootListener {

    GroovyRuntime getGroovyRuntime();
    void setGroovyRuntime(GroovyRuntime groovyRuntime);
    MyGroovyClassLoader getClassLoader();

    void start(ClassLoader parentClassLoader) throws CoreException;

    Class<?>[] getLoadedClasses();

    void close();
}
