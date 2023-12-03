package client.editor.tools;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EditToolType {
	PLACE_TILES("place_tiles_button.png"),
	ERASE_TILES("erase_tiles_button.png"),
	CREATE_CHUNK("create_chunk_button.png"),
	ERASE_CHUNK("erase_chunk_button.png"),
	PLACE_COLLISION("place_collision_button.png"),
	ERASE_COLLISION("erase_collision_button.png");
	
	@Getter
	private String topPanelTexturePath;

}
