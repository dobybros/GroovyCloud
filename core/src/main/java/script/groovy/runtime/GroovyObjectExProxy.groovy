package script.groovy.runtime;
import script.groovy.object.GroovyObjectEx
class GroovyObjectExProxy implements GroovyInterceptable{
	private GroovyObjectEx<?> groovyObject;
	public GroovyObjectExProxy(GroovyObjectEx<?> groovyObject) {
		this.groovyObject = groovyObject;
	}
	def invokeMethod(String name, args) {
		Class<?> groovyClass = this.groovyObject.getGroovyClass();
		def calledMethod = groovyClass.metaClass.getMetaMethod(name, args);
		def returnObj = calledMethod?.invoke(this.groovyObject.getObject(), args);
		return returnObj;
    }
}


