package vork.server.game.entity;

import vork.server.game.DropTable;

public class NpcTemplate {
	public String name;
	public int maxHealth;
	public int maxHit;
	public int combatLevel;
	public int selectOptions;
	public float chanceToMove;
	public float chanceToStop;
	public float moveSpeed;
	public EntityRenderData renderData;
	public DropTable dropTable;
}
