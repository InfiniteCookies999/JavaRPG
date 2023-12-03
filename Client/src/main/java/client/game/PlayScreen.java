package client.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL20;

import client.Client;
import client.Screen;
import client.game.entity.Direction;
import client.game.entity.Entity;
import client.game.entity.EntityHealthNumberRenderer;
import client.game.entity.EntityRenderer;
import client.game.entity.LevelUpRenderer;
import client.game.entity.PublicChatMessageRenderer;
import client.game.items.GroundItem;
import client.game.menus.EscMenu;
import client.game.menus.NpcTalkMenu;
import client.game.menus.SelectOptionPopupMenu;
import client.game.menus.SettingsMenu;
import client.game.menus.ShopMenu;
import client.game.menus.TextChatMenu;
import client.game.pathing.EntityFollower;
import client.game.pathing.GroundItemFollower;
import client.game.skills.SkillType;
import client.game.world.Chunk;
import client.game.world.ChunkLoader;
import client.game.world.World;
import client.net.NetworkManager;
import client.net.out.DeathConfirmationPacketOut;
import client.net.out.EntitySelectOptionPacketOut;
import lombok.Getter;
import vork.App;
import vork.gfx.Camera;
import vork.gfx.Color;
import vork.gfx.RenderContext;
import vork.gfx.ShaderProgram;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiManager;
import vork.input.Buttons;
import vork.input.Keys;
import vork.math.Vector2;
import vork.util.Collision;
import vork.util.FilePath;

public class PlayScreen implements Screen {

	@Getter
	private World world = new World();
	private Camera camera = new Camera();
	private Camera worldTextCamera = new Camera();
	
	private MovementController movementController = new MovementController();

	private OpponentHealthBar opponentHealthBar;
	
	@Getter
	private TextChatMenu textChatMenu;
	private EscMenu escMenu;
	private SettingsMenu settingsMenu;
	@Getter
	private SelectOptionPopupMenu selectOptionPopupMenu;
	@Getter
	private NpcTalkMenu npcTalkMenu;
	@Getter
	private ShopMenu shopMenu;
	
	private GuiComponent menuWithFullControl;
	private List<GuiComponent> menus = new ArrayList<>();
	
	@Getter
	private EntityFollower entityFollower;
	
	@Getter
	private GroundItemFollower groundItemFollower;
	
	private int entityInteractOption = 0;
	
	private List<EntityHealthNumberRenderer> entityHealthNumberRenderers = new ArrayList<>();
	private List<PublicChatMessageRenderer> publicChatMessageRenderers = new ArrayList<>();
	private List<LevelUpRenderer> levelUpRenderers = new ArrayList<>();
	private Map<Integer, GroundItem> groundItems = new HashMap<>();
	
	private int timePlayerHasBeenDead = 0;
	private boolean loggingOut = false;
	
	/**
	 * The menu takes control and ends up being the only
	 * menu interface allowed to be interacted with. All
	 * other interfaces will be disabled while it is open.
	 * 
	 * @param menu The menu to take full control.
	 */
	public void menuTakeFullControl(GuiComponent menu) {
		if (menuWithFullControl != null) { // Hide the current menu if there is one.
			menuWithFullControl.setHidden(true);
		}
		
		Client.instance.containersDragAndDrop.cancelDrag();
		
		menuWithFullControl = menu;
		menuWithFullControl.setDisabled(false);
		menuWithFullControl.setHidden(false);
		
		for (GuiComponent otherMenu : menus) {
			if (!otherMenu.equals(menu)) {
				otherMenu.setDisabled(true);
			}
		}
	}
	
	public void menuResignFullControl() {
		menuWithFullControl.setHidden(true);
		menuWithFullControl = null;
		
		// Re-enable the menus.
		for (GuiComponent otherMenu : menus) {
			otherMenu.setDisabled(false);
		}
	}
	
	@Override
	public void init() {
		
		ChunkLoader loader = new ChunkLoader(FilePath.internal("world"));
		for (int y = -3; y <= 3; y++) {
			for (int x = -3; x <= 3; x++) {
				Chunk chunk = loader.loadChunk(x, y);
				if (chunk != null) {
					world.addChunk(chunk);
				}
			}
		}
		
		System.out.println("number of chunks loaded: " + world.chunks.size());
		
		textChatMenu = addMenu(new TextChatMenu());
		settingsMenu = addMenu(new SettingsMenu());
		escMenu = addMenu(new EscMenu(settingsMenu));
		npcTalkMenu = addMenu(new NpcTalkMenu());
		shopMenu = addMenu(new ShopMenu());
		//shopMenu.open();
		
		addMenu(Client.instance.inventory);
		
		PlayerHealthBar playerHealthBar = new PlayerHealthBar();
		opponentHealthBar = new OpponentHealthBar(playerHealthBar);
		GuiManager.addRootComponent(playerHealthBar);
		GuiManager.addRootComponent(opponentHealthBar);

		// Keep this last so it appears on top of the other UI components
		selectOptionPopupMenu = addMenu(new SelectOptionPopupMenu(camera));
		
		entityFollower = new EntityFollower(Client.instance.player);
		groundItemFollower = new GroundItemFollower(Client.instance.player, this);
		
		camera.zoom = 3.0f;
	}
	
	private <T extends GuiComponent> T addMenu(T menu) {
		GuiManager.addRootComponent(menu);
		menus.add(menu);
		return menu;
	}
	
	@Override
	public void tick(RenderContext context) {
		
		NetworkManager networkManager = Client.instance.getNetworkManager();
		if (!networkManager.isConnected() || loggingOut) {
			Client.instance.setScreen(new LoginScreen());
			return;
		}
		
		procesInputs();
		update();
		
		App.gfx.clear(GL20.GL_COLOR_BUFFER_BIT);
		App.gfx.enableBlend(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		// Rendering the world
		
		world.renderNonOverhead(camera, Client.instance.getBatch());
	
		for (GroundItem groundItem : groundItems.values()) {
			groundItem.render(Client.instance.getBatch());
		}
		
		Collection<Entity> entities = Client.instance.entities.values();
		List<Entity> sortedEntities = new ArrayList<>();
		sortedEntities.addAll(entities);
		
		Collections.sort(sortedEntities, (e1, e2) -> {
			float y1 = e1.y + e1.renderer.getRenderYOffset();
			float y2 = e2.y + e2.renderer.getRenderYOffset();
			if (e1.isPrimaryPlayer() && y1 == y2) {
				return 1;
			} else if (e2.isPrimaryPlayer() && y1 == y2) {
				return -1;
			}
			
			return (int) (y2 - y1);
		});
		
		for (Entity entity : sortedEntities) {
			entity.render(Client.instance.getBatch());	
		}
		
		Entity player = Client.instance.player;
		
		world.renderOverhead(camera, Client.instance.getBatch());
		
		EntityRenderer playerRenderer = player.renderer;
		camera.centerAround(
				player.x + playerRenderer.getEntityWidth()/2,
				player.y + playerRenderer.getEntityHeight()/2);
		camera.update();
		
		for (LevelUpRenderer levelUpRenderer : levelUpRenderers) {
			levelUpRenderer.render(Client.instance.getBatch());
		}
		levelUpRenderers.removeIf(LevelUpRenderer::isFinished);
		
		for (ShaderProgram program : Resources.getShaderPrograms().values()) {
			program.bind();
			program.uniformMatrix4fv("u_proj", camera.getProjMat());
			program.unbind();
		}
		
		context.render(program -> { });
		
		// Rendering world overlay text
		
		worldTextCamera.zoom = 2.0f;
		worldTextCamera.update();
		for (EntityHealthNumberRenderer numberRenderer : entityHealthNumberRenderers) {
			numberRenderer.render(camera, worldTextCamera, Client.instance.getBatch(), Client.instance.getFont());
		}
		entityHealthNumberRenderers.removeIf(EntityHealthNumberRenderer::isFinished);
		
		context.render(program -> {
			program.uniformMatrix4fv("u_proj", worldTextCamera.getProjMat());
		});
		
		worldTextCamera.zoom = 1.0f;
		worldTextCamera.update();
		for (PublicChatMessageRenderer publicChatRenderer : publicChatMessageRenderers) {
			publicChatRenderer.render(camera, worldTextCamera, Client.instance.getBatch(), Client.instance.getBoldFont());
		}
		publicChatMessageRenderers.removeIf(PublicChatMessageRenderer::isFinished);
		
		context.render(program -> {
			program.uniformMatrix4fv("u_proj", worldTextCamera.getProjMat());
		});
		
		// Rendering UI
		
		GuiManager.update();
		Client.instance.containersDragAndDrop.update();
		GuiManager.render(Client.instance.getBatch());
		
		// Rendering debug text
		
		if (settingsMenu.shouldShowFps()) {
			Client.instance.getFont().render(0, App.gfx.getWndHeight() - 16, "fps: " + Client.instance.getFps(), Color.YELLOW);
		}
		if (settingsMenu.shouldShowCharacterLocation()) {
			Client.instance.getFont().render(0, App.gfx.getWndHeight() - 16*2, "character tile: " + Client.instance.player.currentLocation, Color.YELLOW);
		}
		
		context.render();
		
	}
	
	private void procesInputs() {
		
		// Opening/closing esc window.
		if (App.input.isKeyJustPressed(Keys.ESCAPE)) {
			if (escMenu == menuWithFullControl) {
				menuResignFullControl();
			} else if (menuWithFullControl == null)  { // As long as no other menu has control open the escape menu.
				menuTakeFullControl(escMenu);
			}
		}
		
		// Change from settings back to esc menu.
		if (App.input.isKeyJustPressed(Keys.ESCAPE)) {
			if (settingsMenu == menuWithFullControl) { // go back to the esc window.
				menuTakeFullControl(escMenu);	
			}
		}
		
		boolean canPerformGameInput = !textChatMenu.getTextField().hasFocus() && menuWithFullControl == null;
		
		Entity player = Client.instance.player;
		
		canPerformGameInput &= !player.isDead();
		
		Direction moveDirection = Direction.NONE;
		if (App.input.isKeyPressed(Keys.W) || App.input.isKeyPressed(Keys.UP)) {
			moveDirection = Direction.NORTH;
		}
		if (App.input.isKeyPressed(Keys.A) || App.input.isKeyPressed(Keys.LEFT)) {
			moveDirection = Direction.WEST;
		}
		if (App.input.isKeyPressed(Keys.S) || App.input.isKeyPressed(Keys.DOWN)) {
			moveDirection = Direction.SOUTH;	
		}
		if (App.input.isKeyPressed(Keys.D) || App.input.isKeyPressed(Keys.RIGHT)) {
			moveDirection = Direction.EAST;
		}
		
		if (canPerformGameInput) {
			player.predictiveDirection = moveDirection;
			if (moveDirection != Direction.NONE) {
				cancelCurrentAction(true);
			}
			
			if (App.input.isButtonJustPressed(Buttons.RIGHT) && !GuiManager.isCursorHovering()) {
			
				List<Object> objectsUnderCursor = getObjectsUnderCursor();
				
				if (!objectsUnderCursor.isEmpty() ) {
					selectOptionPopupMenu.open(objectsUnderCursor);	
				}	
			}
		} else {
			player.predictiveDirection = Direction.NONE;
		}
		
		groundItemFollower.update(world);
		entityFollower.update(world);
	}
	
	private List<Object> getObjectsUnderCursor() {
		List<Object> objectsUnderCursor = new ArrayList<>();
		objectsUnderCursor.addAll(getEntitiesUnderCursor());
		objectsUnderCursor.addAll(getGroundItemsUnderCursor());
		return objectsUnderCursor;
	}

	private List<Entity> getEntitiesUnderCursor() {
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		
		List<Entity> entitiesUnderCursor = new ArrayList<>();
		for (Entity entity : Client.instance.entities.values()) {
			int entityWidth = entity.renderer.getEntityWidth();
			int entityHeight = entity.renderer.getEntityHeight();
			if (Collision.pointToRect(
					cursorWorldCoords.x,
					cursorWorldCoords.y,
					entity.x,
					entity.y,
					entityWidth,
					entityHeight) && !entity.isPrimaryPlayer() && !entity.isDead()) {
				entitiesUnderCursor.add(entity);
			}
		}
		
		return entitiesUnderCursor;
	}
	
	private List<GroundItem> getGroundItemsUnderCursor() {
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		
		List<GroundItem> groundItemsUnderCursor = new ArrayList<>();
		for (GroundItem groundItem : groundItems.values()) {
			if (Collision.pointToRect(
					cursorWorldCoords.x,
					cursorWorldCoords.y,
					groundItem.getX(),
					groundItem.getY(),
					groundItem.getWidth(),
					groundItem.getHeight())) {
				groundItemsUnderCursor.add(groundItem);
			}
		}
		
		return groundItemsUnderCursor;
	}
	
	public void performEntityInteraction(Entity entity, int optionId) {
		
		if (Client.instance.player.isDead()) {
			// Cannot perform an interaction while dead
			return;
		}
		
		entityInteractOption = optionId;
		
		switch (optionId) {
		case SelectOptions.ATTACK:
			new EntitySelectOptionPacketOut(entity, optionId).send();
			entityFollower.setFollowingEntityId(entity.id);
			opponentHealthBar.setEntityId(entity.id);
			break;
		case SelectOptions.TALK_TO:
			new EntitySelectOptionPacketOut(entity, optionId).send();
			entityFollower.setFollowingEntityId(entity.id);
			break;
		case SelectOptions.FOLLOW:
			entityFollower.setFollowingEntityId(entity.id);
			break;
		case SelectOptions.OPEN_SHOP:
			break;
		}
	}
	
	public void cancelCurrentAction(boolean sendCancelPacket) {
		if (sendCancelPacket &&	
			(entityInteractOption == SelectOptions.ATTACK ||
			 entityInteractOption == SelectOptions.OPEN_SHOP ||
			 entityInteractOption == SelectOptions.TALK_TO)
				) {
			// Tell the server we no longer want to interact with
			// the given entity.
			new EntitySelectOptionPacketOut(null, 0).send();	
		}
		npcTalkMenu.setHidden(true);
		groundItemFollower.cancel();
		entityFollower.setFollowingEntityId(-1);
		entityInteractOption = 0;
		opponentHealthBar.setEntityId(-1);
	}
	
	public void handleEntityTeleported(Entity entity) {
		// If the entity was teleported stop following it.
		if (entity.id == entityFollower.getFollowingEntityId()) {
			entityFollower.setFollowingEntityId(-1);
		}
	}
	
	public void logout() {
		loggingOut = true;
	}
	
	private void update() {
		for (Entity entity : Client.instance.entities.values()) {
			movementController.processMovement(world, entity);	
		}
		
		if (Client.instance.player.isDead()) {
			// Need to respawn the player once its death animation is over
			if (timePlayerHasBeenDead > 60*2) {
				new DeathConfirmationPacketOut().send();
			}
			++timePlayerHasBeenDead;
		}
	}

	public void handlePlayerDeath() {
		cancelCurrentAction(false);
		Client.instance.player.predictiveDirection = Direction.NONE;
		Client.instance.player.moveQueue.clear();
		timePlayerHasBeenDead = 0;
		Client.instance.inventory.unlockAllSlots();
	}
	
	@Override
	public void dispose() {
		Client.instance.player = null;
		for (Entity entity : Client.instance.entities.values()) {
			entity.renderer.dispose();
		}
		Client.instance.entities.clear();
		for (int i = 0; i < SkillType.NUM_SKILLS; i++) {
			Client.instance.skills[i] = null;
		}
		Client.instance.inventory = null;
		Client.instance.containersDragAndDrop = null;
		Client.instance.getNetworkManager().close();
	}

	public void addEntityHealthNumberRenderer(EntityHealthNumberRenderer healthNumberRenderer) {
		entityHealthNumberRenderers.add(healthNumberRenderer);
	}
	
	public void addPublicChatMessageRenderer(PublicChatMessageRenderer publicChatMessageRenderer) {
		publicChatMessageRenderers.add(publicChatMessageRenderer);
	}
	
	public void addLevelUpRenderer(LevelUpRenderer levelUpRenderer) {
		levelUpRenderers.add(levelUpRenderer);
	}
	
	public void addGroundItem(int id, GroundItem groundItem) {
		groundItems.put(id, groundItem);
	}
	
	public void removeGroundItem(int id) {
		groundItems.remove(id);
	}
	
	public GroundItem getGroundItem(int id) {
		return groundItems.get(id);
	}
}
