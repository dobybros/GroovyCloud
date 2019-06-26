package script.javascript.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import chat.utils.FileExtensionFilter;
import chat.utils.IteratorEx;

public class Java8Tester {
	static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	public static ScriptEngine getEngine() throws ScriptException, FileNotFoundException {
		final ScriptEngine nashorn = scriptEngineManager.getEngineByName("nashorn");
		String root = "/Users/aplombchen/Dev/taineng/Server/workspace-ggts/ChatCore/src/script/javascript/runtime";
		final File file = new File(root);
		FileExtensionFilter filter = new FileExtensionFilter(new IteratorEx<File>() {
			@Override
			public boolean iterate(File theFile) {
				try {
					FileReader reader = new FileReader(theFile.getAbsolutePath().replaceAll("\\\\", "/"));
					if (nashorn instanceof Compilable) {
						System.out.println("Compiling.... " + theFile);
						Compilable compEngine = (Compilable) nashorn;
						CompiledScript cs = compEngine.compile(reader);
						cs.eval();
					} else {
						nashorn.eval(reader);
					}
				} catch (FileNotFoundException | ScriptException e) {
					e.printStackTrace();
				}
				return true;
			}
		}).filter(file);
		return nashorn;
	}

	public static void main(String args[]) throws IOException, NoSuchMethodException, ScriptException {

		for (int i = 0; i < 4; i++) {
			ScriptEngine nashorn = Java8Tester.getEngine();
			Bindings bindings = nashorn.createBindings();
			bindings.put("h", "haha");
			if (nashorn instanceof Invocable) {
				int times = 1000000;
				Invocable invoke = (Invocable) nashorn;
				System.out.println("hi " + invoke.invokeFunction("hi"));

				System.out.println("hello " + invoke.invokeFunction("hello"));
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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