package com.docker.utils;

import java.util.*;


/**
 * @author Bin
 *
 * @param <K>
 * @param <V>
 */
public class SortedMap<K, V> extends HashMap<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -861205405511243758L;
	
	private Vector<K> queue;
	
	public SortedMap() {
		this(0);
	}

	public SortedMap(int capacity) {
		queue = new Vector<K>();
	}

//	@SuppressWarnings("unused")
//	private SortedMap(Map<K, V> map) {
//		throw new RuntimeException("Stub!");
//	}

	@SuppressWarnings("unchecked")
	public SortedMap<K, V> clone() {
		final SortedMap<K, V> newOne = (SortedMap<K, V>) super.clone();
//		newOne.queue.addAll(queue);
		
		return newOne;
	}
	
	public void cloneMap(SortedMap<K, V> map) {
		Object[] array = queue.toArray();
		for(Object obj : array) {
			map.put((K) obj, get(obj));
		}
	}
	
//	@Override
//	public Set<Entry<K, V>> entrySet() {
//		return super.entrySet();
//	}

	public void reverseOrder() {
		Collections.reverse(queue);
	}

	public boolean isEmpty() {
		return super.isEmpty();
	}
	
	public synchronized void move(int pos,K key){
		queue.remove(key);
		queue.insertElementAt(key, pos);
	}

	public int size() {
		return super.size();
	}

	public V get(Object key) {
		return super.get(key);
	}

	public int indexOf(K key) {
		return queue.indexOf(key);
	}
	
	public V get(int index) {
		if (index >= 0 && index < queue.size()) {
			K k = queue.get(index);
			return super.get(k);
		}
		return null;
	}
	
	public boolean containsKey(Object key) {
		return super.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return super.containsValue(value);
	}

	public synchronized V put(K key, V value) {
		if (queue.contains(key)) {
			queue.remove(key);
		}
		queue.add(key);
		return super.put(key, value);
	}
	
	public synchronized V add(Integer index, K key, V value) {
//		queue.insertElementAt(key, index);
		if(queue.contains(key)) {
			queue.remove(key);
		}
		queue.add(index, key);
		return super.put(key, value);
	}
	
	@Override
	public synchronized void putAll(Map<? extends K,? extends V> map) {
		Iterator<? extends K> iterator = map.keySet().iterator();
		while(iterator.hasNext()) {
			K k = iterator.next();
			put(k,map.get(k));
		}
	}
	
	public synchronized void putAll(SortedMap<? extends K,? extends V> sortedMap) {
		Iterator<? extends K> iterator = sortedMap.keySet().iterator();
		while(iterator.hasNext()) {
			K k = iterator.next();
			put(k,sortedMap.get(k));
		}
	}
	
	public synchronized V replaceKey(K oldKey, K key, V value) {
		int pos = queue.indexOf(oldKey);
		if(pos == -1) 
			return null;
		super.remove(oldKey);
		queue.removeElement(oldKey);
		queue.add(pos, key);
		return super.put(key, value);
	}

//	private void putAll(Map map) {
//		throw new RuntimeException("Stub!");
//	}

	public synchronized V remove(Object key) {
		if (queue.contains(key)) {
			queue.remove(key);
		}
		return super.remove(key);
	}

	public synchronized void clear() {
		queue.clear();
		super.clear();
	}

	public List<K> keyList() {
		List<K> list = new ArrayList<K>();
		list.addAll(queue);
		return list;
	}
	
	public Set<K> keySet() {
		Set<K> set = new ArraySet<K>();
		set.addAll(queue);
		return set;
	}
	
	public synchronized void insert(int pos,K key,V value) {
		if (queue.contains(key)) {
			queue.remove(key);
		}
		queue.insertElementAt(key, pos);
		super.put(key, value);
	}

	public static class KeyValue<K, V> {
		public KeyValue(K k, V v) {
			this.key = k;
			this.value = v;
		}
		private K key;
		private V value;

		public K getKey() {
			return key;
		}

		public void setKey(K key) {
			this.key = key;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}
	}

	public SortedMap<K, V> sortValues(Comparator<KeyValue<K, V>> comparator) {
		TreeSet<KeyValue<K, V>> collection = new TreeSet<KeyValue<K, V>>(comparator);
		List<K> keys = new ArrayList<>();
		for(K k : queue) {
			V v = get(k);
			collection.add(new KeyValue(k, v));
		}
		SortedMap<K, V> map = new SortedMap<>();
		for(KeyValue<K, V> keyValue : collection) {
			map.put(keyValue.key, keyValue.value);
		}
		return map;
	}

	public Collection<V> values() {
		Collection<V> collection = new ArrayList<V>();
		for(K k : queue) {
			collection.add(get(k));
		}
		return collection;
	}

	public List<V> valueList() {
		ArrayList<V> collection = new ArrayList<V>();
		for(K k : queue) {
			collection.add(get(k));
		}
		return collection;
	}

	public Set<V> valueSet() {
		Set<V> set = new ArraySet<V>();
		for (K k : queue) {
			set.add(super.get(k));
		}
		return set;
	}
	
	public K firstKey() {
		try {
			return queue.firstElement();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	public K lastKey() {
		try {
			return queue.lastElement();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
}
