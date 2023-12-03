package vork.server.game.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import vork.server.FilePath;
import vork.server.game.scripts.S2Parser;
import vork.server.game.scripts.S2Script;
import vork.server.game.scripts.S2SemanticAnalysis;

public class NpcScriptManager {
	
	private static final Map<String, S2Script> npcScripts = new HashMap<>();
	
	public static void loadScripts() throws IOException {
		FilePath scriptsDirectory = FilePath.internal("scripts");
		for (FilePath scriptFile : scriptsDirectory.getFilesInDirectory()) {
			if (scriptFile.getExtension().equals("s2")) {
				loadScript(scriptFile);
			}
		}
	}
	
	private static void loadScript(FilePath scriptFile) throws IOException {
		String scriptName = scriptFile.getName().substring(0, scriptFile.getName().length() - 3);
		//System.out.println("trying to parse script : " + scriptName);
		S2Script script = new S2Parser().parse(scriptFile);
		if (script.getLogger().hasErrors()) return;
		new S2SemanticAnalysis().analyze(script);
		if (script.getLogger().hasErrors()) return;
		//System.out.println("loaded script: " + scriptName);
		npcScripts.put(scriptName, script);
	}
	
	public static S2Script getScript(String scriptName) {
		if (!npcScripts.containsKey(scriptName)) {
			throw new IllegalArgumentException("Unknown script for script name: " + scriptName);
		}
		return npcScripts.get(scriptName);
	}
}
