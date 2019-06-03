package script.filter;


/**
 * 
 * @author aplomb
 *
 * @param <T>
 */
public interface JsonFilter<T> {
	public Object filter(T target, Object... arguments);

	public T from(Object doc, Object... arguments);
}
