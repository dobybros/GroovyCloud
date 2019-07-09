package chat.utils;

import chat.logs.LoggerEx;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ClassFieldsHolder {
	public static abstract class FieldIdentifier {
		public static final String MAPKEY = "mapkey";
		
		public abstract String getFieldKey(Field field);
		
		public FieldEx field(Field field) {
			return new FieldEx(field);
		}
	}

	public static class FieldEx extends HashMap<String, Object>{
		private Field field;
		public FieldEx(Field field) {
			this.field = field;
		}
		public Field getField() {
			return field;
		}
	}
	
	private static final String TAG = ClassFieldsHolder.class.getSimpleName();

	private HashMap<String, FieldEx> fieldMap = new HashMap<>();
	
	public ClassFieldsHolder(Class<?> documentClass, FieldIdentifier fieldIdentifier) {
		Class<?> i = documentClass;
	    while (i != null && !i.equals(Object.class)) {
	    	Field[] fields = i.getDeclaredFields();
	    	for(Field field : fields) {
	    		if(fieldIdentifier != null) {
	    			String key = fieldIdentifier.getFieldKey(field);
	    			if(StringUtils.isNotBlank(key)) {
						fieldMap.put(key, fieldIdentifier.field(field));
					}
	    		}
			}
	        i = i.getSuperclass();
	    }
	}
	
	public void assignField(Object obj, String fieldKey, Object value) {
		FieldEx field = fieldMap.get(fieldKey);
		assignField(obj, field.getField(), value);
	}

	public Object convert(Object value, Field field) {
		return ConvertUtils.convert(value, field.getType());
	}

	public void assignField(Object obj, Field field, Object value) {
		if(field == null || value == null || obj == null)
			return;
		try {
			if(!field.isAccessible())
				field.setAccessible(true);

			field.set(obj, convert(value, field));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "Assign value " + value + " to field " + field + " for object " + obj);
		}
	}

	public HashMap<String, FieldEx> getFieldMap() {
		return fieldMap;
	}
}