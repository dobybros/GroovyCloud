package chat.utils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class HashTree<K, V> {
	private HashMap<K, HashTree<K, V>> parentMap;
	private HashMap<K, V> kvMap;
	public HashMap<String, Object> parameters;
	
	public HashTree() {
		parentMap = new HashMap<K, HashTree<K,V>>();
		kvMap = new HashMap<K, V>();
	}
	
	public boolean containsKey(K key) {
		return kvMap.containsKey(key);
	}
	
	public void setParameter(String key, Object value) {
		if(parameters == null)
			parameters = new HashMap<String, Object>();
		parameters.put(key, value);
	}
	
	public Object getParameter(String key) {
		if(parameters == null)
			return null;
		return parameters.get(key);
	}
	
	public V get(K key) {
		return kvMap.get(key);
	}
	
	public V put(K key, V value) {
		return kvMap.put(key, value);
	}
	
	public V delete(K key) {
		return kvMap.remove(key);
	}
	
	public Collection<V> values() {
		return kvMap.values();
	}
	
	public HashTree<K, V> getChildren(K key) {
		return getChildren(key, false);
	}
	public HashTree<K, V> getChildren(K key, boolean create) {
		HashTree<K, V> treeNode = parentMap.get(key);
		if(create && treeNode == null) {
			synchronized (this) {
				treeNode = parentMap.get(key);
				if(treeNode == null) {
					treeNode = new HashTree<K, V>();
					parentMap.put(key, treeNode);
				}
			}
		}
		return treeNode;
	}
	
	public Set<K> getChildrens() {
		return parentMap.keySet();
	}
}