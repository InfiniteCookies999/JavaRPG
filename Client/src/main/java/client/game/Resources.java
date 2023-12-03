package client.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.game.items.ItemDefinition;
import lombok.Getter;
import vork.gfx.PaddedTexture;
import vork.gfx.ShaderProgram;
import vork.gfx.Texture;
import vork.util.FilePath;
import vork.util.PaddedTextureResource;
import vork.util.TextureResource;

public class Resources {

	private static final Map<String, TextureResource> uiTextures = new HashMap<>();
	private static final List<PaddedTextureResource> tileSheets = new ArrayList<>();
	private static final List<TextureResource> mobTextures = new ArrayList<>();
	private static final Map<String, String> shaderSources = new HashMap<>();
	private static final List<Texture> itemTextures = new ArrayList<>();
	
	@Getter
	private static final Map<ShaderProgramType, ShaderProgram> shaderPrograms = new HashMap<>();
	
	public static final int NUM_HUMAN_SKIN_COLORS    = 4;
	public static final int NUM_HUMAN_HAIR_COLORS    = 7;
	public static final int NUM_HUMAN_CLOTHES_COLORS = 9;
	
	public static final int NUM_HUMAN_HAIR_STYLES  = 8;
	public static final int NUM_HUMAN_SHIRT_STYLES = 4;
	public static final int NUM_HUMAN_PANTS_STYLES = 5;
	
	public static final int[]   TXR_HUMAN_SKIN_IDXS  = new int[NUM_HUMAN_SKIN_COLORS];
	public static final int[][] TXR_HUMAN_HAIR_IDXS  = new int[NUM_HUMAN_HAIR_STYLES][NUM_HUMAN_HAIR_COLORS];
	
	public static final List<Integer> HUMAN_EYE_BUFFER_INDEXES = new ArrayList<>();
	
	private static final List<int[]> HUMAN_TEXTURE_PIXELS = new ArrayList<>();
	
	private static final List<ItemDefinition> itemDefinitions = new ArrayList<>();
	
	public static void hotReload() throws IOException {
		for (TextureResource resource : uiTextures.values()) {
			resource.hotReload();
		}
		for (TextureResource resource : mobTextures) {
			resource.hotReload();
		}
		for (PaddedTextureResource resource : tileSheets) {
			resource.hotReload();
		}
	}
	
	public static void addItemDefinition(ItemDefinition definition) {
		itemDefinitions.add(definition);
	}
	
	public static ItemDefinition getItemDefinition(int id) {
		return itemDefinitions.get(id);
	}
	
	public static void putShaderSource(FilePath file, String source) {
		shaderSources.put(file.getName(), source);
	}
	
	public static String getShaderSource(String fileName) {
		return shaderSources.get(fileName);
	}
	
	public static int getNumberOfTileSheets() {
		return tileSheets.size();
	}
	
	public static void putShaderProgram(ShaderProgramType type, ShaderProgram program) {
		shaderPrograms.put(type, program);
	}
	
	public static ShaderProgram getShaderProgram(ShaderProgramType type) {
		return shaderPrograms.get(type);
	}
	
	public static void putUITexture(FilePath file, Texture texture) {
		uiTextures.put(file.getName(), new TextureResource(file, texture));
	}
	
	public static int addTileSheet(FilePath file, PaddedTexture tileSheet) {
		tileSheets.add(new PaddedTextureResource(file, tileSheet));
		return tileSheets.size() - 1;
	}
	
	public static PaddedTexture getTileSheet(int tileSheetIdx) {
		return tileSheets.get(tileSheetIdx).getData();
	}
	
	public static void addItemTexture(Texture texture) {
		itemTextures.add(texture);
	}
	
	public static Texture getItemTexture(int textureId) {
		return itemTextures.get(textureId);
	}
	
	public static Texture getUITexture(String name) {
		TextureResource resource = uiTextures.get(name);
		if (resource == null) {
			throw new IllegalArgumentException("Missing UI texture: " + name);
		}
		return resource.getData();
	}
	
	public static void addMobTexture(FilePath file, Texture texture) {
		mobTextures.add(new TextureResource(file, texture));
	}
	
	public static Texture getMobTexture(int index) {
		return mobTextures.get(index).getData();
	}
	
	public static int addHumanTexturePixels(int[] buffer) {
		HUMAN_TEXTURE_PIXELS.add(buffer);
		return HUMAN_TEXTURE_PIXELS.size() - 1;
	}
	
	public static int[] getHumanTexturePixels(int index) {
		return HUMAN_TEXTURE_PIXELS.get(index);
	}
	
	public static void dispose() {
		for (TextureResource resource : uiTextures.values()) {
			resource.getData().dispose();
		}
		for (TextureResource resource : mobTextures) {
			resource.getData().dispose();
		}
		for (PaddedTextureResource resource : tileSheets) {
			resource.getData().dispose();
		}
		for (Texture texture : itemTextures) {
			texture.dispose();
		}
		for (ShaderProgram program : shaderPrograms.values()) {
			program.dispose();
		}
	}
}
