package chat.utils;


/**
 * @author Aplomb
 *
 * @param <T>
 */
public abstract class IteratorWithEndpoint<T> implements IteratorEx<T> {
	protected int count = 0;
	
	
	@Override
	public final boolean iterate(T t) {
		count++;
		return iterateIt(t);
	}

	protected abstract boolean iterateIt(T t); 
	
	/**
	 * Mark traversal end
	 */
	public abstract void end();

	/**
	 * Amount of traversal
	 * @return
	 */
	public int count() {
		return count;
	}
	
}
