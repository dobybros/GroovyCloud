package com.dobybros.chat.storage.mongodb;

import org.apache.commons.lang.StringUtils;
import org.bson.BSON;
import org.bson.Document;


/**
 * A BasicDBObject that abandon empty key and null value.
 * provide some useful method
 */
public class CleanDocument extends Document {

	private static final long serialVersionUID = 8027097799463248640L;
	public CleanDocument() {}
	public CleanDocument(Document dbo) {
	    if (dbo != null) {
	        putAll(dbo);
	    }
	}

	@Override
	public Object put(String key, Object val) {
		if(val == null || (StringUtils.isBlank(key))) 
			return null;
//		if (val instanceof Documentable) {
//		    return super.put(key, ((Documentable)val).toDocument());
//		} else if (val instanceof Iterable){
//            @SuppressWarnings("rawtypes")
//            Iterator it = ((Iterable)val).iterator();
//            if (it.hasNext()) {
//                Object obj = it.next();
//                if (obj instanceof Documentable) { 
//                	ArrayList<Document> list = new ArrayList<Document>();
//                    list.add(((Documentable)obj).toDocument());
//                    while (it.hasNext()) {
//                        list.add(((Documentable)it.next()).toDocument());
//                    }
//                    return super.put(key, list);
//                } 
//            } 
//            return super.put(key, val);
//        } else {
//		    return super.put(key, val);
//		}
		return super.put(key, val);
	}
	
	@Override
	public CleanDocument append(String key, Object val) {
	    this.put(key, val);
	    return this;
	}
	
    /**
     * @param key
     * @param clazz Only support DBObjectable
     * @return clazz instance
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String key, Class<? super V> clazz) {
        Object value = super.get(key);
        if (value == null) {
            return null;
        } else {
        	return (V) value;
        }
//        else if (Documentable.class.isAssignableFrom(clazz) && (value instanceof Document)) {
//            boolean haveNoPaCon = false;
//            for (Constructor<?> ctt : clazz.getConstructors()) {
//                if (ctt.getParameterTypes().length == 0) {
//                    haveNoPaCon = true;
//                    break;
//                }
//            }
//            if (!haveNoPaCon) {
//                throw new IllegalArgumentException("The " + clazz
//                        + " does not have default public constructor.");
//            }
//            try {
//                Documentable dbt = (Documentable) clazz.newInstance();
//                dbt.fromDocument((Document) value);
//                return (V) dbt;
//            } catch (InstantiationException | IllegalAccessException e) {
//                e.printStackTrace();
//                return (V) value;
//            }
//        } else if (List.class.isAssignableFrom(clazz)) {
//            return (V)value;
//        } else {
//           throw new IllegalStateException();
//        }
    }
    
    /** Returns the value of a field as an <code>integer</code>.
     * @param key the field to look for
     * @return the field value or null
     */
    public Integer getInteger( String key ){
        Object o = get(key);
        if ( o == null )
            return null;

        return Integer.valueOf(BSON.toInt( o ));
    }

    /** Returns the value of a field as an <code>Integer</code>.
     * @param key the field to look for
     * @param def the default to return
     * @return the field value (or default)
     */
    public Integer getInteger( String key , Integer def ){
        Object foo = get( key );
        if ( foo == null )
            return def;

        return Integer.valueOf(BSON.toInt( foo ));
    }

    /**
     * Returns the value of a field as a <code>Long</code>.
     *
     * @param key the field to return
     * @return the field value or null
     */
    public Long getLongObject( String key){
        Object foo = get( key );
        if (foo == null) 
            return null;
        return ((Number)foo).longValue();
    }

    /**
     * Returns the value of a field as an <code>Long</code>.
     * @param key the field to look for
     * @param def the default to return
     * @return the field value (or default)
     */
    public Long getLongObject( String key , Long def ) {
        Object foo = get( key );
        if ( foo == null )
            return def;

        return ((Number)foo).longValue();
    }

    /**
     * Returns the value of a field as a <code>Double</code>.
     *
     * @param key the field to return
     * @return the field value or null
     */
    public Double getDoubleObject( String key){
        Object foo = get( key );
        if (foo == null) 
            return null;
        return ((Number)foo).doubleValue();
    }

    /**
     * Returns the value of a field as an <code>Double</code>.
     * @param key the field to look for
     * @param def the default to return
     * @return the field value (or default)
     */
    public Double getDoubleObject( String key , Double def ) {
        Object foo = get( key );
        if ( foo == null )
            return def;

        return ((Number)foo).doubleValue();
    }
    
}
