package vork.server.game.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import vork.server.FilePath;
import vork.server.game.scripts.ast.S2Node;

public class S2Script {
	
	@Getter
	@Setter
	private List<S2Node> statements = new ArrayList<>();

	@Getter
	private Map<String, S2Label> labels = new HashMap<>();
	
	@Getter
	private S2Logger logger;
	
	public S2Script(FilePath filePath) {
		logger = new S2Logger(filePath.getName());
	}
}
