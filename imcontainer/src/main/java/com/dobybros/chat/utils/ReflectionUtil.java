package com.dobybros.chat.utils;

import chat.logs.LoggerEx;
import org.springframework.util.ClassUtils;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * @author
 * 
 */
public class ReflectionUtil {
	/**
	 * public abstract void test.Test.init(test.Test)
	 * 
	 * will generate "init"
	 * @param
	 * @return
	 */
	public static String generateMethodKey(Method method){
		String str = method.toString();
		int endPos = str.indexOf('(');
		int startPos = str.lastIndexOf('.', endPos) + 1;
		return str.substring(startPos, endPos);
//		return method.toGenericString();
	}
	public static void main(String[] args) {
	}

	public static Class<?> getInitiatableClass(Class<?> clazz) {
		Class<?> theClass = null;
		if(Collection.class.isAssignableFrom(clazz)) {
			theClass = ArrayList.class;
		} else if(Map.class.isAssignableFrom(clazz)) {
			theClass = HashMap.class;
		}
		if(theClass != null) {
			LoggerEx.debug("getInitiatableClass", "Convert class from " + clazz + " to " + theClass);
			return theClass;
		}
		return clazz;
	}

	public static boolean canBeInitiated(Class<?> clazz) {
		if(ClassUtils.isPrimitiveOrWrapper(clazz))
			return true;
		if(clazz.isInterface()) {
			return false;
		}
//		ConstructorUtils.getMatchingAccessibleConstructor(clazz, null);
		try {
			Constructor<?> contructor = clazz.getConstructor();
			if(contructor == null)
				return false;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static Method[] getMethods(Class<?> clazz){
		HashMap<String, Method> allFields = new HashMap<String, Method>();
		Method[] sFields = null;
		Method[] fields = clazz.getDeclaredMethods();
		for(Method m : fields){
			allFields.put(generateMethodKey(m), m);
		}
		Class<?>[] cInterfaces = clazz.getInterfaces();
		for(Class<?> c : cInterfaces){
			if(c != null){
				sFields = getInterfaceMethods(c);
			}
			if(sFields != null){
				for(Method f : sFields){
					if(!allFields.containsKey(generateMethodKey(f))){
						allFields.put(generateMethodKey(f), f);
					}
				}
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if(superClass != null && !superClass.equals(Object.class)) {
			sFields = getMethods(superClass);
			if(sFields != null){
				for(Method f : sFields){
					if(!allFields.containsKey(generateMethodKey(f))){
						allFields.put(generateMethodKey(f), f);
					}
				}
			}
		}

		Method[] fs = new Method[allFields.size()];
		allFields.values().toArray(fs);
		return fs;
	}

	public static Method[] getInterfaceMethods(Class<?> clazz){
		if(!Modifier.isInterface(clazz.getModifiers())){
			return null;
		}
		HashMap<String, Method> allFields = new HashMap<String, Method>();
		Method[] sFields = null;
		Method[] fields = clazz.getDeclaredMethods();
		for(Method m : fields){
			allFields.put(generateMethodKey(m), m);
		}
		Class<?>[] cInterfaces = clazz.getInterfaces();
		for(Class<?> c : cInterfaces){
			if(c != null){
				sFields = getInterfaceMethods(c);
			}
			if(sFields != null){
				for(Method f : sFields){
					if(!allFields.containsKey(generateMethodKey(f))){
						allFields.put(generateMethodKey(f), f);
					}
				}
			}
		}
		Method[] fs = new Method[allFields.size()];
		allFields.values().toArray(fs);
		return fs;
	}
	
	public static Field getField(Class clazz, String fieldName)
			throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}
	
	public static Field[] getFields(Class<?> clazz){
		Field[] sFields = null;
		Field[] fields = clazz.getDeclaredFields();
		Class<?> c = clazz.getSuperclass();
		if(c != null && !c.equals(Object.class)){
			sFields = getFields(c);
		}
		if(sFields == null){
			return fields;
		}else{
			HashMap<String, Field> allFields = new HashMap<String, Field>();
			
			//Exclude the same fields existing between subclass and superclass.
			for(Field f : sFields){
				allFields.put(f.getName(), f);
			}
			for(Field f : fields){
				allFields.put(f.getName(), f);
			}
			
			Field[] fs = new Field[allFields.size()];
			allFields.values().toArray(fs);
			return fs;
		}
		
	}

	public static void makeAccessible(Field field) {
		if (!Modifier.isPublic(field.getModifiers())
				|| !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

	/**
	 * 1. 得到某个对象的属性
	 * 
	 * @param owner
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	public static Object getFieldValue(Object owner, String fieldName)
			throws Exception {
		Class<? extends Object> ownerClass = owner.getClass(); // 得到该对象的Class
		Field field = getField(ownerClass, fieldName); // 通过Class得到类声明的属性
		field.setAccessible(true);
		Object property = field.get(owner); // 通过对象得到该属性的实例，如果这个属性是非公有的，这里会报IllegalAccessException
		return property;
	}
	
	public static Object getFieldValue(Field field, Object owner) throws Exception{
		field.setAccessible(true);
		return field.get(owner);
	}

	/**
	 * 2. 得到某个类的静态属性
	 * 
	 * @param className
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	public static Object getStaticProperty(String className, String fieldName)
			throws Exception {
		Class<?> ownerClass = Class.forName(className); // 首先得到这个类的Class
		Field field = ownerClass.getField(fieldName); // 和上面一样，通过Class得到类声明的属性
		field.setAccessible(true);
		Object property = field.get(ownerClass); // 这里和上面有些不同，因为该属性是静态的，所以直接从类的Class里取
		return property;
	}

	/**
	 * 3. 执行某对象的方法
	 * 
	 * @param owner
	 * @param methodName
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeMethod(Object owner, String methodName,
			Object[] args) throws Exception {
		Class<? extends Object> ownerClass = owner.getClass(); // 首先还是必须得到这个对象的Class
		Class[] argsClass = null;
		if(args != null) {
			argsClass = new Class[args.length];
			// 以下几行，配置参数的Class数组，作为寻找Method的条件
			for (int i = 0, j = args.length; i < j; i++) {
				argsClass[i] = args[i].getClass();
			}
		}
		Method method = null;
		if(argsClass != null)
			method = ownerClass.getMethod(methodName, argsClass); // 通过Method名和参数的Class数组得到要执行的Method
		else
			method = ownerClass.getMethod(methodName); // 通过Method名和参数的Class数组得到要执行的Method

		if(args != null)
			return method.invoke(owner, args); // 执行该Method，invoke方法的参数是执行这个方法的对象，和参数数组。返回值是Object，也既是该方法的返回值
		else
			return method.invoke(owner);
	}

	/**
	 * 4. 执行某个类的静态方法 基本的原理和实例3相同，不同点是最后一行，invoke的一个参数是null，因为这是静态方法，不需要借助实例运行
	 * 
	 * @param className
	 * @param methodName
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeStaticMethod(String className,
			String methodName, Object[] args) throws Exception {
		Class<?> ownerClass = Class.forName(className);
		Class[] argsClass = new Class[args.length];
		for (int i = 0, j = args.length; i < j; i++) {
			argsClass[i] = args[i].getClass();
		}
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(null, args);
	}

	/**
	 * 5. 新建实例
	 * 这里说的方法是执行带参数的构造函数来新建实例的方法。如果不需要参数，可以直接使用newoneClass.newInstance()来实现。
	 * 
	 * @param className
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Object newInstance(String className, Object[] args)
			throws Exception {
		Class<?> newoneClass = Class.forName(className); // 第一步，得到要构造的实例的Class
		// 以下几行，得到参数的Class数组
		Class[] argsClass = null;
		if(args != null) {
			argsClass = new Class[args.length];
			for (int i = 0, j = args.length; i < j; i++) {
				argsClass[i] = args[i].getClass();
			}
		}
		Constructor cons = null;
		if(argsClass == null)
		 	cons = newoneClass.getConstructor(); // 得到构造方法
		else
			cons = newoneClass.getConstructor(argsClass); // 得到构造方法
		return cons.newInstance(args); // 新建实例
	}

	/**
	 * 6. 判断是否为某个类的实例
	 * 
	 * @param obj
	 * @param cls
	 * @return
	 */
	public static boolean isInstance(Object obj, Class<?> cls) {
		return cls.isInstance(obj);
	}

	/**
	 * 7. 得到数组中的某个元素
	 * 
	 * @param array
	 * @param index
	 * @return
	 */
	public static Object getByArray(Object array, int index) {
		return Array.get(array, index);
	}

	public static void log(String name, Object obj){  
        Class ownerClass = obj.getClass();  
          
        //获得对象的属性，包括private级的，用getField()可以获得public级的所有属性。括号内可指定属性名称获得特定的属性  
        Field fields[] = getFields(ownerClass);  

        String objName = ownerClass.getName();//获得对象的全路径名称  
          
        System.out.println(name + objName + ": ");  
          
        for(int i = 0;i < fields.length;i ++){  
            Field field = fields[i];  
            
            String fieldName = field.getName();//获得属性名称  
            Object value = null;
            try {
				value = getFieldValue(obj, fieldName);
			} catch (Exception e) {
			}
			if(value != null){
				System.out.println("    [" + fieldName + " = " + value.toString() + "]");
			}
        }  
         
        if(fields.length == 0){
        	System.out.println("    [" + obj.toString() + "]");  
        }
    }

	public static long getCrc(Method method) {
		if(method == null)
			return -1;
		String name = method.getName();
		String className = method.getDeclaringClass().getName();
		CRC32 crc = new CRC32();
		String str = name + "#" + className;
		crc.update(str.getBytes());
		long value = crc.getValue();
		if(value == 2380642687L)
			System.out.print("");
		return value;
	}
}