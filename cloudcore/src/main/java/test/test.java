package test;

import chat.utils.ReflectionUtil;
import com.docker.data.CacheObj;
import com.docker.storage.cache.CacheAnnotationHandler;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class test {
    private static ExpressionParser parser = new SpelExpressionParser();

    public static void main(String[] args) throws NoSuchMethodException {
//        Long beforeTime = System.currentTimeMillis();
//        Method method = CacheAnnotationHandler.class.getDeclaredMethod("getDataCache", CacheObj.class);
//        String params = getParamStr(method);
//        for(int i=0;i<100000;i++){
//            //生成crc
//            ReflectionUtil.getCrc(CacheAnnotationHandler.class,method.getName(),params);
//
////            generationMethodStr(CacheAnnotationHandler.class,method);
//
//        }
//        Long lastTime = System.currentTimeMillis();
//        System.out.println(lastTime - beforeTime);
//
//        Long beforeTime1 = System.currentTimeMillis();
////        Method method = CacheAnnotationHandler.class.getDeclaredMethod("getDataCache", CacheAnnotationHandler.CacheObj.class);
//        for(int i=0;i<100000;i++){
//            //生成crc
////            ReflectionUtil.getCrc(CacheAnnotationHandler.class,method.getName(),getParamStr(method));
//
//            generationMethodStr(CacheAnnotationHandler.class,method,params);
//
//        }
//        Long lastTime1 = System.currentTimeMillis();
//        System.out.println(lastTime1 - beforeTime1);
        Long beforeTime1 = System.currentTimeMillis();
        for(int i=0;i<10000;i++){
            System.out.println(parseSpel(new String[]{"id"+1}, new String[]{"213412423"+i}, "#id"+1));
        }
        Long lastTime1 = System.currentTimeMillis();
        System.out.println(lastTime1 - beforeTime1);

    }


    private static Object parseSpel(String[] paramNames, Object[] arguments, String spel) {
        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int len = 0; len < paramNames.length; len++) {
                context.setVariable(paramNames[len], arguments[len]);
            }
        }
        try {
            Expression expression = parser.parseExpression(spel);
            return expression.getValue(context);
        } catch (Exception e) {
//            return defaultResult;
        }
        return null;
    }

    public static String generationMethodStr(Class clazz, Method method, String params) {
//        String parameters = getParamStr(method);
        return clazz.getSimpleName() + method.getName() + params;
    }

    public static String getParamStr(Method method) {
        String parameters = "";
        for (Parameter parameter : method.getParameters()) {
            parameters += parameter.getName();
        }
        return parameters;
    }

    ;


}
