package com.dobybros.chat.utils;

import java.util.*;


/**
 * @author Bin
 *
 * @param <E>
 */
public class ArraySet<E> extends AbstractSet<E> implements Set<E>, Cloneable,
		java.io.Serializable {
	static final long serialVersionUID = -5024744406713321676L;

	private transient Vector<E> map;

	public ArraySet() {
		map = new Vector<E>();
	}

	public ArraySet(Collection<? extends E> c) {
		map = new Vector<E>(Math.max((int) (c.size() / .75f) + 1, 16));
		addAll(c);
	}

	public ArraySet(int initialCapacity) {
		map = new Vector<E>(initialCapacity);
	}
	

	public Iterator<E> iterator() {
		return map.iterator();
	}

	/**
	 * Returns the number of elements in this set (its cardinality).
	 * 
	 * @return the number of elements in this set (its cardinality).
	 */
	public int size() {
		return map.size();
	}

	/**
	 * Returns <tt>true</tt> if this set contains no elements.
	 * 
	 * @return <tt>true</tt> if this set contains no elements.
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.
	 * 
	 * @param o
	 *            element whose presence in this set is to be tested.
	 * @return <tt>true</tt> if this set contains the specified element.
	 */
	public boolean contains(Object o) {
		return map.contains(o);
	}

	/**
	 * Adds the specified element to this set if it is not already present.
	 * 
	 * @param o
	 *            element to be added to this set.
	 * @return <tt>true</tt> if the set did not already contain the specified
	 *         element.
	 */
	public boolean add(E o) {
		return map.add(o);
	}

	/**
	 * Removes the specified element from this set if it is present.
	 * 
	 * @param o
	 *            object to be removed from this set, if present.
	 * @return <tt>true</tt> if the set contained the specified element.
	 */
	public boolean remove(Object o) {
		return map.remove(o);
	}

	/**
	 * Removes all of the elements from this set.
	 */
	public void clear() {
		map.clear();
	}

//	/**
//	 * Returns a shallow copy of this <tt>ArraySet</tt> instance: the elements
//	 * themselves are not cloned.
//	 * 
//	 * @return a shallow copy of this set.
//	 */
//	@SuppressWarnings("unchecked")
//	public Object clone() {
//		Vector<E> newSet = (Vector<E>) map.clone();
//		return newSet;
//	}

}