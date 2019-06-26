package com.docker.script.callers;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.script.BaseRuntime;
import script.groovy.runtime.GroovyRuntime;

import java.lang.reflect.InvocationTargetException;

public class CallerUtils {
    private static final String TAG = CallerUtils.class.getSimpleName();

    public static void callMethod(Object caller, String methodName) {
        BaseRuntime baseRuntime = (BaseRuntime) GroovyRuntime.getCurrentGroovyRuntime(caller.getClass().getClassLoader());
        if(baseRuntime != null) {
            try {
                baseRuntime.executeBeanMethod(caller, methodName);
            } catch (CoreException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "executeBeanMethod(CoreException) " + caller + " failed, " + e.getMessage() + " e " + e.getClass());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "executeBeanMethod(InvocationTargetException) " + caller + " failed, " + e.getMessage() + " e " + e.getClass());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "executeBeanMethod(IllegalAccessException) " + caller + " failed, " + e.getMessage() + " e " + e.getClass());
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.error(TAG, "executeBeanMethod(Throwable) " + caller + " failed, " + t.getMessage() + " t " + t.getClass());
            }
        }
    }
}
