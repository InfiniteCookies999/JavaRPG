package client.editor;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.lwjgl.opengl.GL20;

import client.Constants;
import client.editor.tools.CreateChunkTool;
import client.editor.tools.EditTool;
import client.editor.tools.EditToolType;
import client.editor.tools.EraseChunkTool;
import client.editor.tools.EraseCollisionTool;
import client.editor.tools.EraseTilesTool;
import client.editor.tools.PlaceCollisionTool;
import client.editor.tools.PlaceTilesTool;
import client.game.Resources;
import client.game.world.Chunk;
import client.game.world.ChunkLoader;
import client.game.world.Tile;
import client.game.world.World;
import lombok.AllArgsConstructor;
import lombok.Getter;
import vork.App;
import vork.LaunchSettings;
import vork.gfx.Camera;
import vork.gfx.Color;
import vork.gfx.PaddedTexture;
import vork.gfx.RenderContext;
import vork.gfx.ShaderProgram;
import vork.gfx.ShaderType;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.TextureFilter;
import vork.gfx.UvCoords;
import vork.gfx.VkFont;
import vork.gfx.gui.GuiManager;
import vork.input.Keys;
import vork.math.Matrix4;
import vork.math.Vector2;
import vork.util.FilePath;

public class Editor extends App {
	
	public static final Editor instance = new Editor();
	
	public static final String vertex =
			"#version 430\n" +
			"\n" +
			"layout (location = 0) in vec2 v_pos;\n" +
			"layout (location = 1) in vec4 v_color;\n" +
			"layout (location = 2) in vec2 v_uv;\n" +
			"\n" +
			"out vec4 f_color;\n" +
			"out vec2 f_uv;\n" +
			"\n" +
			"uniform mat4 u_proj;\n" +
			"\n" +
			"void main() {\n" +
			"	gl_Position = u_proj * vec4(v_pos, 0.0, 1.0);\n" +
			"	f_color = v_color;\n" +
			"	f_uv = vec2(v_uv.x, 1.0 - v_uv.y);" +
			"}\n"
			;
	
	public static final String fragment =
			"#version 430\n" +
			"\n" +
			"in vec4 f_color;\n" +
			"in vec2 f_uv;\n" +
			"\n" +
			"out vec4 o_color;\n" +
			"\n" +
			"uniform sampler2D u_sampler;\n" +
			"\n" +
			"void main() {\n" +
			"	o_color = f_color * texture(u_sampler, f_uv);\n" +
			"}\n"
			;
	
	@Getter
	private VkFont font;
	
	@Getter
	private ShaderProgram defaultShaderProgram = new ShaderProgram();
	
	@Getter
	private SpriteBatch batch = new SpriteBatch();
	
	private Camera camera = new Camera();
	private float camX, camY;
	// 1 to 1 ratio of coords to screen pixels
	private Camera overlayCamera = new Camera();
	
	public static final int TOP_PANEL_HEIGHT  = 30;
	public static final int RIGHT_PANEL_WIDTH = 250;
	
	private static final int MAX_NUMBER_OF_UNDOS = 20;
	
	public RightPanel rightPanel;
	private TopPanel topPanel;
	public EraseChunkWarningMenu eraseChunkWarningMenu;
	
	private static final FilePath CACHE_DIR = FilePath.home(".vork_cache214235");
	private static final FilePath WORLD_DIR = CACHE_DIR.append("world");
	private static final FilePath TILE_SHEETS_DIR = CACHE_DIR.append("textures");
	
	@Getter
	private World world;
		
	@Getter
	private RenderContext renderContext = new RenderContext(defaultShaderProgram, batch) {
		
		@Override
		protected void uploadUniforms(ShaderProgram program) {
			camera.update();
			program.uniformMatrix4fv("u_proj", camera.getProjMat());
		}
	};
	
	private EditTool<?>[] editTools = new EditTool[EditToolType.values().length];
	
	@AllArgsConstructor
	private class UndoState {
		private EditTool<?> tool;
		private Object undoObject;
	}
	
	private Stack<UndoState> undoStates = new Stack<>();
	
	public static void main(String[] args) throws IOException {
		
		LaunchSettings settings = new LaunchSettings();
		settings.enableVsync  = true;
		settings.windowTitle  = "Editor";
		settings.windowWidth  = Constants.WORLD_PIXEL_SCALE * Constants.NUM_VISIBLE_PIXELS + RIGHT_PANEL_WIDTH;
		settings.windowHeight = Constants.WORLD_PIXEL_SCALE * Constants.NUM_VISIBLE_PIXELS + TOP_PANEL_HEIGHT;
		settings.shouldWindowResize = false;
		
		instance.run(settings);
		
	}
	
	public void addUndoState(EditTool<?> tool, Object undoObject) {
		if (undoStates.size() == MAX_NUMBER_OF_UNDOS) {
			// Remove the oldest entry so that the newer entries can be undone.
			undoStates.remove(0);	
		}
		undoStates.add(new UndoState(tool, undoObject));
	}

	@Override
	protected void init() throws IOException {
		
		glDisable(GL_MULTISAMPLE);
		
		FilePath[] editorTexturesFiles = FilePath.internal("textures/editor").getFilesInDirectory();
		for (FilePath file : editorTexturesFiles) {
			Texture texture = Texture.createFromPNGFile(file, TextureFilter.NEAREST_NEAREST);
			Resources.putUITexture(file, texture);
		}
		List<String> tileSheetNames = new ArrayList<>();
		loadTileSheet(tileSheetNames, TILE_SHEETS_DIR.append("tile_sheet_floors0.png"));
		loadTileSheet(tileSheetNames, TILE_SHEETS_DIR.append("tile_sheet_outside0.png"));
		loadTileSheet(tileSheetNames, TILE_SHEETS_DIR.append("tile_sheet_outside0.png"));
		loadTileSheet(tileSheetNames, TILE_SHEETS_DIR.append("tile_sheet_walls.png"));
		loadTileSheet(tileSheetNames, TILE_SHEETS_DIR.append("tile_sheet_decoration.png"));
		
		if (!WORLD_DIR.exist()) {
			WORLD_DIR.mkDir();	
		}
		
		world = new World();
		
		ChunkLoader chunkLoader = new ChunkLoader(WORLD_DIR);
		for (int y = -10; y < 10; y++) {
			for (int x = -10; x < 10; x++) {
				Chunk chunk = chunkLoader.loadChunk(x, y);
				if (chunk != null) {
					world.addChunk(chunk);
				}
			}
		}
		
		defaultShaderProgram.attachShader(vertex, ShaderType.VERTEX);
		defaultShaderProgram.attachShader(fragment, ShaderType.FRAGMENT);
		defaultShaderProgram.compile();
		
		defaultShaderProgram.bind();
		batch.create(defaultShaderProgram);
		defaultShaderProgram.unbind();
		
		font = VkFont.create(FilePath.internal("fonts/font.png"), FilePath.internal("fonts/font.fmt.txt"), batch);
		
		rightPanel = new RightPanel(tileSheetNames);
		topPanel = new TopPanel();
		eraseChunkWarningMenu = new EraseChunkWarningMenu();
		GuiManager.addRootComponent(rightPanel);
		GuiManager.addRootComponent(topPanel);
		GuiManager.addRootComponent(eraseChunkWarningMenu);
		
		camera.zoom = Constants.WORLD_PIXEL_SCALE;
		
		editTools[EditToolType.PLACE_TILES.ordinal()] = new PlaceTilesTool(this);
		editTools[EditToolType.ERASE_TILES.ordinal()] = new EraseTilesTool(this);
		editTools[EditToolType.CREATE_CHUNK.ordinal()] = new CreateChunkTool(this);
		editTools[EditToolType.ERASE_CHUNK.ordinal()] = new EraseChunkTool(this);
		editTools[EditToolType.PLACE_COLLISION.ordinal()] = new PlaceCollisionTool(this);
		editTools[EditToolType.ERASE_COLLISION.ordinal()] = new EraseCollisionTool(this);
		
	}

	private void loadTileSheet(List<String> tileSheetNames, FilePath file) throws IOException {
		tileSheetNames.add(file.getName().substring(0, file.getName().length() - 4));
		PaddedTexture tileSheet = Texture.createFromPNGFile(file, TextureFilter.NEAREST_NEAREST, Constants.TILE_SIZE, Constants.TILE_SIZE);
		Resources.addTileSheet(file, tileSheet);
	}
	
	@Override
	protected void tick() {
		
		try {
			Resources.hotReload();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		App.gfx.clear(GL20.GL_COLOR_BUFFER_BIT);
		App.gfx.enableBlend(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		rightPanel.height = App.gfx.getWndHeight() - TOP_PANEL_HEIGHT;
		
		final float cameraSpeed = 2.0f;
		float mx = 0, my = 0;
		if (App.input.isAnyKeyPressed(Keys.W, Keys.UP)) {
			my += cameraSpeed;
		}
		if (App.input.isAnyKeyPressed(Keys.A, Keys.LEFT)) {
			mx -= cameraSpeed;
		}
		if (App.input.isAnyKeyPressed(Keys.S, Keys.DOWN)) {
			my -= cameraSpeed;
		}
		if (App.input.isAnyKeyPressed(Keys.D, Keys.RIGHT)) {
			mx += cameraSpeed;
		}
		camX += mx;
		camY += my;
		
		camera.centerAround(camX, camY);
		camera.update();
		
		world.renderNonOverhead(camera, batch);
		world.renderOverhead(camera, batch);
		
		renderSelectedTiles();
		renderCollisionBoxes();
		
		renderContext.render();
		
		overlayCamera.update();
		renderGrid();
		renderTileSelectBorder();
		renderChunkCreateOrEraseVisual();	
		
		GuiManager.update();
		GuiManager.render(batch);
		
		updateToolActions();
		
	}

	private void renderSelectedTiles() {
		EditToolType toolType = topPanel.getSelectedEditToolType();
		if (toolType != EditToolType.PLACE_TILES) {
			return;
		}
		if (GuiManager.isCursorHovering()) {
			return;
		}
		
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		int curWorldTileX = (int) Math.floor(cursorWorldCoords.x / Constants.TILE_SIZE);
		int curWorldTileY = (int) Math.floor(cursorWorldCoords.y / Constants.TILE_SIZE);
		
		int tileSheetIdx = rightPanel.getSelectedTileSheetIdx();
		
		int sx = rightPanel.getTileSelStartX();
		int sy = rightPanel.getTileSelStartY();
		int ex = rightPanel.getTileSelEndX();
		int ey = rightPanel.getTileSelEndY();
		
		int worldStartX = curWorldTileX - (ex - sx)/2;
		int worldStartY = curWorldTileY - (ey - sy)/2;
		
		for (int y = sy; y <= ey; y++) {
			for (int x = sx; x <= ex; x++) {
				Tile tile = world.getTile(worldStartX + x - sx, worldStartY + y - sy);
				if (tile == null) {
					return;
				}
			}
		}
		
		for (int y = sy; y <= ey; y++) {
			for (int x = sx; x <= ex; x++) {
				Tile.LayerRenderData data = new Tile.LayerRenderData();
				data.tileSheetIdx = tileSheetIdx;
				data.tileSheetIdxX = x;
				data.tileSheetIdxY = y;
				data.render(batch, worldStartX + x - sx, worldStartY + y - sy, Color.WHITE);
			}	
		}
	}
	
	private void renderCollisionBoxes() {
		EditToolType toolType = topPanel.getSelectedEditToolType();
		if (!(toolType == EditToolType.PLACE_COLLISION ||
			  toolType == EditToolType.ERASE_COLLISION
				)) {
			return;
		}
		
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		int curWorldTileX = (int) Math.floor(cursorWorldCoords.x / Constants.TILE_SIZE);
		int curWorldTileY = (int) Math.floor(cursorWorldCoords.y / Constants.TILE_SIZE);
		
		{ // Render the tile currently attempting to be placed.
			int chunkX = curWorldTileX >> Constants.CHUNK_SIZE_LOG2;
			int chunkY = curWorldTileY >> Constants.CHUNK_SIZE_LOG2;	
			
			Chunk chunk = world.getChunk(chunkX, chunkY);
			
			if (chunk != null) {
				batch.addQuad(
						Constants.TILE_SIZE * curWorldTileX,
						Constants.TILE_SIZE * curWorldTileY,
						Constants.TILE_SIZE, Constants.TILE_SIZE,
						toolType == EditToolType.PLACE_COLLISION ? new Color(Color.RED, 0.5f) : new Color(Color.GREEN, 0.5f),
						UvCoords.FULL,
						App.gfx.emptyTexture);	
			}
		}
			
		// Displaying the currently existing collisions
		
		int camChunkX = getCenterCamChunkX();
		int camChunkY = getCenterCamChunkY();
		int by0 = camChunkY - 1;
		int by1 = camChunkY + 1;
		int bx0 = camChunkX - 1;
		int bx1 = camChunkX + 1;
		
		for (int chunkY = by0; chunkY <= by1; ++chunkY) {
			for (int chunkX = bx0; chunkX <= bx1; ++chunkX) {
				Chunk chunk = world.getChunk(chunkX, chunkY);
				if (chunk == null) {
					continue;
				}
				
				for (int ty = 0; ty < Constants.CHUNK_SIZE; ty++) {
					for (int tx = 0; tx < Constants.CHUNK_SIZE; tx++) {
						Tile tile = chunk.getTile(ty, tx);
						if (tile.isTraversible) {
							continue;
						}
						
						batch.addQuad(
								Constants.TILE_SIZE * tile.worldX,
								Constants.TILE_SIZE * tile.worldY,
								Constants.TILE_SIZE,
								Constants.TILE_SIZE,
								new Color(Color.RED, 0.5f),
								UvCoords.FULL,
								App.gfx.emptyTexture);
					}	
				}
			}
		}
		
	}

	private void updateToolActions() {
		
		if (App.input.isKeyPressed(Keys.LEFT_CTRL) && App.input.isKeyJustPressed(Keys.Z)) {
			if (!undoStates.empty()) {
				UndoState state = undoStates.pop();
				state.tool.unUndo(world, state.undoObject);
			}
		}
		
		if (GuiManager.isCursorHovering() &&
			eraseChunkWarningMenu.isHidden() // If it is not hidden we want to allow updating the selection
				) {
			return;
		}
		
		boolean canStartAction = true;
		for (EditTool<?> tool : editTools) {
			if (tool == null) {
				continue;
			}
			
			if (tool.isActing()) {
				canStartAction = false;
				tool.act(world, camera);
				if (tool.canEnd()) {
					tool.end();
					canStartAction = true;
					return; // Don't want to start again immediately
				}
				break;
			}
		}
		
		if (canStartAction) {
			for (EditTool<?> tool : editTools) {
				if (tool == null) {
					continue;
				}
				
				if (tool.getType() == topPanel.getSelectedEditToolType() && tool.canStart()) {
					tool.start();
					break;
				}
			}
		}
	}
	
	private void renderGrid() {
		int camChunkX = getCenterCamChunkX();
		int camChunkY = getCenterCamChunkY();
		int by0 = camChunkY - 1;
		int by1 = camChunkY + 1;
		int bx0 = camChunkX - 1;
		int bx1 = camChunkX + 1;
		
		final int off = Constants.TILE_SIZE * Constants.WORLD_PIXEL_SCALE;
		
		Matrix4 invOverlayCamera = overlayCamera.getProjMat().invertOrtho2D();
		
		for (int chunkY = by0; chunkY <= by1; ++chunkY) {
			for (int chunkX = bx0; chunkX <= bx1; ++chunkX) {
				Chunk chunk = world.getChunk(chunkX, chunkY);
				if (chunk != null) continue;
				
				int xx = chunkX * Constants.CHUNK_SIZE * Constants.TILE_SIZE;
				int yy = chunkY * Constants.CHUNK_SIZE * Constants.TILE_SIZE;
				
				Vector2 screenCoords = camera.getProjMat().mul(new Vector2(xx, yy));
				Vector2 gridXY = invOverlayCamera.mul(screenCoords);
				
				for (int x = 0; x <= Constants.CHUNK_SIZE; x++) {
					batch.addQuad(
							gridXY.x + off*x,
							gridXY.y,
							1,
							Constants.CHUNK_SIZE*Constants.TILE_SIZE*Constants.WORLD_PIXEL_SCALE,
							Color.GRAY,
							UvCoords.FULL,
							App.gfx.emptyTexture);
				}
				for (int y = 0; y <= Constants.CHUNK_SIZE; y++) {
					batch.addQuad(
							gridXY.x,
							gridXY.y + off*y,
							Constants.CHUNK_SIZE*Constants.TILE_SIZE*Constants.WORLD_PIXEL_SCALE,
							1,
							Color.GRAY,
							UvCoords.FULL,
							App.gfx.emptyTexture);
				}
			}	
		}
	}
	
	private void renderTileSelectBorder() {
		EditToolType toolType = topPanel.getSelectedEditToolType();
		if (!(toolType == EditToolType.PLACE_TILES ||
			  toolType == EditToolType.ERASE_TILES ||
			  toolType == EditToolType.PLACE_COLLISION ||
			  toolType == EditToolType.ERASE_COLLISION
				)) {
			return;
		}
		if (GuiManager.isCursorHovering()) {
			return;
		}
		
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		int curWorldTileX = (int) Math.floor(cursorWorldCoords.x / Constants.TILE_SIZE);
		int curWorldTileY = (int) Math.floor(cursorWorldCoords.y / Constants.TILE_SIZE);
		
		Matrix4 invOverlayCamera = overlayCamera.getProjMat().invertOrtho2D();
		
		final int tileWidth = Constants.TILE_SIZE * Constants.WORLD_PIXEL_SCALE;
		
		if (toolType != EditToolType.PLACE_TILES) {
			Tile cursorTile = world.getTile(curWorldTileX, curWorldTileY);
			if (cursorTile == null) {
				return;
			}
			
			float xx = curWorldTileX * Constants.TILE_SIZE, yy = curWorldTileY * Constants.TILE_SIZE;
			Vector2 screenCoords = camera.getProjMat().mul(new Vector2(xx, yy));
			Vector2 coords = invOverlayCamera.mul(screenCoords);
			
			batch.addQuad(
					coords.x, coords.y,
					tileWidth, 1,
					Color.WHITE, UvCoords.FULL, App.gfx.emptyTexture);
			batch.addQuad(
					coords.x, coords.y,
					1, tileWidth,
					Color.WHITE, UvCoords.FULL, App.gfx.emptyTexture);
			batch.addQuad(
					coords.x, coords.y + tileWidth - 1,
					tileWidth, 1,
					Color.WHITE, UvCoords.FULL, App.gfx.emptyTexture);
			batch.addQuad(
					coords.x + tileWidth - 1, coords.y,
					1, tileWidth,
					Color.WHITE, UvCoords.FULL, App.gfx.emptyTexture);	
		} else {
			
			int sx = rightPanel.getTileSelStartX();
			int sy = rightPanel.getTileSelStartY();
			int ex = rightPanel.getTileSelEndX();
			int ey = rightPanel.getTileSelEndY();
			
			int worldStartX = curWorldTileX - (ex - sx)/2;
			int worldStartY = curWorldTileY - (ey - sy)/2;
			
			for (int y = sy; y <= ey; y++) {
				for (int x = sx; x <= ex; x++) {
					Tile tile = world.getTile(worldStartX + x - sx, worldStartY + y - sy);
					if (tile == null) {
						return;
					}
				}
			}

			float xx = worldStartX * Constants.TILE_SIZE, yy = worldStartY * Constants.TILE_SIZE;
			Vector2 screenCoords = camera.getProjMat().mul(new Vector2(xx, yy));
			Vector2 coords = invOverlayCamera.mul(screenCoords);
			
			batch.addQuad(
					coords.x, coords.y,
					tileWidth * (ex - sx + 1), 1,
					Color.WHITE, UvCoords.FULL, App.gfx.emptyTexture);
			batch.addQuad(
					coords.x, coords.y + tileWidth * (ey - sy + 1) - 1,
					tileWidth * (ex - sx + 1), 1,
					Color.WHITE, UvCoords.FULL, App.gfx.emptyTexture);
			batch.addQuad(
					coords.x, coords.y,
					1, tileWidth * (ey - sy + 1),
					Color.WHITE, UvCoords.FULL, App.gfx.emptyTexture);
			batch.addQuad(
					coords.x + + tileWidth * (ex - sx + 1) - 1, coords.y,
					1, tileWidth * (ey - sy + 1),
					Color.WHITE, UvCoords.FULL, App.gfx.emptyTexture);
		}
	}
	
	private void renderChunkCreateOrEraseVisual() {
		EditToolType toolType = topPanel.getSelectedEditToolType();
		if (!(toolType == EditToolType.CREATE_CHUNK ||
			  toolType == EditToolType.ERASE_CHUNK
				)) {
			return;
		}
		if (GuiManager.isCursorHovering()) {
			return;
		}
		
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		int chunkX = ((int) Math.floor(cursorWorldCoords.x / Constants.TILE_SIZE)) >> Constants.CHUNK_SIZE_LOG2;
		int chunkY = ((int) Math.floor(cursorWorldCoords.y / Constants.TILE_SIZE)) >> Constants.CHUNK_SIZE_LOG2;
		
		Chunk chunk = world.getChunk(chunkX, chunkY);
		if (toolType == EditToolType.CREATE_CHUNK) {
			if (chunk != null) {
				return; // Chunk already exist.
			}
		} else {
			if (chunk == null) {
				return; // Chunk doesn't exist.
			}
		}
		
		Matrix4 invOverlayCamera = overlayCamera.getProjMat().invertOrtho2D();
		
		float xx = chunkX * Constants.CHUNK_SIZE * Constants.TILE_SIZE;
		float yy = chunkY * Constants.CHUNK_SIZE * Constants.TILE_SIZE;
		
		Vector2 screenCoords = camera.getProjMat().mul(new Vector2(xx, yy));
		Vector2 coords = invOverlayCamera.mul(screenCoords);
		
		Color color = toolType == EditToolType.CREATE_CHUNK ? new Color(Color.GREEN, 0.5f)
				                                            : new Color(Color.RED, 0.5f);
		
		batch.addQuad(
				coords.x,
				coords.y,
				Constants.CHUNK_SIZE * Constants.TILE_SIZE * Constants.WORLD_PIXEL_SCALE,
				Constants.CHUNK_SIZE * Constants.TILE_SIZE * Constants.WORLD_PIXEL_SCALE,
				color,
				UvCoords.FULL,
				App.gfx.emptyTexture);
		
	}
	
	private int getCenterCamChunkX() {
		int worldTileX = (int) (Math.floor(camera.getCenterX()) / Constants.TILE_SIZE);
		return worldTileX >> Constants.CHUNK_SIZE_LOG2;
	}
	
	private int getCenterCamChunkY() {
		int worldTileY = (int) (Math.floor(camera.getCenterY()) / Constants.TILE_SIZE);
		return worldTileY >> Constants.CHUNK_SIZE_LOG2;
	}
	
	@Override
	protected void dispose() {
		try {
			for (Chunk chunk : world.chunks) {
				ChunkSaver.save(WORLD_DIR, chunk);	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Resources.dispose();
		font.dispose();
		batch.dispose();
		defaultShaderProgram.dispose();
	}
}
