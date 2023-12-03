package vork.server.game.container;

import lombok.Data;

@Data
public class ItemDefinition {
	private int id;
	private ItemCatagory catagory;
	private String name;
}

