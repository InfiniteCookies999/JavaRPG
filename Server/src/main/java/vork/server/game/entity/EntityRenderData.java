package vork.server.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class EntityRenderData {
	
	@Getter
	private EntityRenderType type;
	
}
