package script.javascript.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import chat.errors.CoreException;
import chat.logs.LoggerEx;

public class Java8Tester2 {

	public static void main(String args[]) throws IOException, NoSuchMethodException, ScriptException, CoreException {
		JavascriptRuntime runtime = new JavascriptRuntime();
		runtime.setPath("C:\\Dev\\GameServer\\workspace\\server\\ChatCore\\src\\script\\javascript\\runtime");
		runtime.init();
		for(int i = 0; i < 10000; i++) {
			ScriptEngine nashorn = runtime.getCurrentScriptEngine();
			Bindings bindings = nashorn.createBindings();
			bindings.put("h", "haha");
			if (nashorn instanceof Invocable) {
				Invocable invoke = (Invocable) nashorn;
				Map<String, Object> map = new HashMap<>();
				Map<String, Object> map1 = new HashMap<>();
				map.put("map", map1);
				map.put("key", "hi");
				map1.put("key", "haha");
				long time = System.currentTimeMillis();
				Object obj = invoke.invokeFunction("hello", map);
//				System.out.println("hello " + obj + " takes " + (System.currentTimeMillis() - time));
				
				time = System.currentTimeMillis();
				obj = invoke.invokeFunction("hello", map);
//				System.out.println(" takes " + (System.currentTimeMillis() - time));
			}
			runtime.start();
			
			System.gc();
			System.out.println("mem " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		}
		// ScriptEngine nashorn = Java8Tester.getEngine();
		// if(nashorn instanceof Invocable){
		// Invocable invoke = (Invocable) nashorn;
		//
		// int times = 1000000;
		// Java8Tester tester = new Java8Tester();
		// long time = System.currentTimeMillis();
		// for(int i = 0; i < times; i++) {
		// Double sum = tester.addFun(2, 3);
		// }
		// System.out.println("java takes " + (System.currentTimeMillis() -
		// time));
		//
		// time = System.currentTimeMillis();
		// for(int i = 0; i < times; i++) {
		// Double sum = (Double) invoke.invokeFunction("addFun", 2, 3);
		// }
		// System.out.println("addFun takes " + (System.currentTimeMillis() -
		// time));
		//
		// time = System.currentTimeMillis();
		// invoke.invokeFunction("add");
		// System.out.println("add takes " + (System.currentTimeMillis() -
		// time));
		// }

		// time = System.currentTimeMillis();
		// for(int i = 0; i < times; i++) {
		// Double sum = (Double) invoke.invokeFunction("addFun", 2, 3);
		// }
		// System.out.println("addFun takes " + (System.currentTimeMillis() -
		// time));

		//
		// time = System.currentTimeMillis();
		// invoke.invokeFunction("minus");
		// System.out.println("minus takes " + (System.currentTimeMillis() -
		// time));
		//
		// time = System.currentTimeMillis();
		// invoke.invokeFunction("add");
		// System.out.println("add takes " + (System.currentTimeMillis() -
		// time));
		//
		// time = System.currentTimeMillis();
		// invoke.invokeFunction("add");
		// System.out.println("add takes " + (System.currentTimeMillis() -
		// time));

	}

	public double addFun(double a, double b) {
		return a + b;
	}
}