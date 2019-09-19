package test;

import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.storage.cache.CacheAnnotationHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class test {

    public static void main(String[] args) throws NoSuchMethodException{
        Long beforeTime = System.currentTimeMillis();
        Method method = CacheAnnotationHandler.class.getDeclaredMethod("getDataCache", CacheObj.class);
        String params = getParamStr(method);
        for(int i=0;i<100000;i++){
            //生成crc
            ReflectionUtil.getCrc(CacheAnnotationHandler.class,method.getName(),params);

//            generationMethodStr(CacheAnnotationHandler.class,method);

        }
        Long lastTime = System.currentTimeMillis();
        System.out.println(lastTime - beforeTime);

        Long beforeTime1 = System.currentTimeMillis();
//        Method method = CacheAnnotationHandler.class.getDeclaredMethod("getDataCache", CacheAnnotationHandler.CacheObj.class);
        for(int i=0;i<100000;i++){
            //生成crc
//            ReflectionUtil.getCrc(CacheAnnotationHandler.class,method.getName(),getParamStr(method));

            generationMethodStr(CacheAnnotationHandler.class,method,params);

        }
        Long lastTime1 = System.currentTimeMillis();
        System.out.println(lastTime1 - beforeTime1);



    }

    public static String generationMethodStr(Class clazz, Method method, String params){
//        String parameters = getParamStr(method);
        return clazz.getSimpleName() + method.getName() + params;
    }

    public static String getParamStr(Method method){
        String parameters = "";
        for(Parameter parameter : method.getParameters()){
            parameters += parameter.getName();
        }
        return parameters;
    };


}
