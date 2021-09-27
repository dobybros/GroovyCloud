package script.javascript.runtime;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.FileExtensionFilter;
import script.ScriptRuntime;

import javax.script.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class JavascriptRuntime extends ScriptRuntime {
	private ScriptEngineManager scriptEngineManager;
	private ScriptEngine currentScriptEngine;
	@Override
	public void init() throws CoreException {
		scriptEngineManager = new ScriptEngineManager();
		start();
	}

	@Override
	public void start() throws CoreException {
		final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("nashorn");
		final File file = new File(path);
		FileExtensionFilter filter = new FileExtensionFilter().filter(file);
		List<File> files = filter.getFiles();
		for(File theFile : files) {
			try (FileReader reader = new FileReader(theFile.getAbsolutePath().replaceAll("\\\\", "/"))) {
				if (scriptEngine instanceof Compilable) {
					System.out.println("Compiling.... " + theFile);
					Compilable compEngine = (Compilable) scriptEngine;
					CompiledScript cs = compEngine.compile(reader);
					cs.eval();
				} else {
					scriptEngine.eval(reader);
				}
			} catch (IOException | ScriptException e) {
				e.printStackTrace();
				throw new CoreException(ChatErrorCodes.ERROR_JAVASCRIPT_LOADFILE_FAILED, "Load js file " + theFile + " failed, " + e.getMessage());
			}
		}
		
		currentScriptEngine = scriptEngine;
	}

	@Override
	public void close() {

	}

	public ScriptEngine getCurrentScriptEngine() {
		return currentScriptEngine;
	}

	public void setCurrentScriptEngine(ScriptEngine currentScriptEngine) {
		this.currentScriptEngine = currentScriptEngine;
	}

}
