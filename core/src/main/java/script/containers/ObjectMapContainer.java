package script.containers;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 不能用脚本语言的对象存入到Java的容器中， 在Redeploy之后脚本语言的同一对象的Class是不相同的。
 * 
 * @author aplomb
 *
 */
public class ObjectMapContainer extends ConcurrentHashMap<String, Object>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ObjectMapContainer instance = new ObjectMapContainer();
	private ObjectMapContainer() {
	}
	public static ObjectMapContainer getInstance() {
		return instance;
	}
}
