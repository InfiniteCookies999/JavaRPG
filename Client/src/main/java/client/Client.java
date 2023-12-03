package client;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import client.game.LoadingScreen;
import client.game.Resources;
import client.game.entity.Entity;
import client.game.menus.container.InventoryMenu;
import client.game.skills.Skill;
import client.game.skills.SkillType;
import client.net.NetworkHandle;
import client.net.NetworkManager;
import client.net.in.AnimationListener;
import client.net.in.ChangeFacingDirectionListener;
import client.net.in.ChatTextListener;
import client.net.in.ContainerMoveListener;
import client.net.in.DespawnGroundItemListener;
import client.net.in.EntityDespawnListener;
import client.net.in.EntityMoveListener;
import client.net.in.EntitySpawnListener;
import client.net.in.EntityTeleportListener;
import client.net.in.HealthChangeListener;
import client.net.in.LoginListener;
import client.net.in.NetworkEventBus;
import client.net.in.NpcChatListener;
import client.net.in.OpenShopListener;
import client.net.in.PickupItemListener;
import client.net.in.SetItemListener;
import client.net.in.SetXpListener;
import client.net.in.SpawnGroundItemListener;
import lombok.Getter;
import vork.App;
import vork.LaunchSettings;
import vork.gfx.Camera;
import vork.gfx.RenderContext;
import vork.gfx.ShaderProgram;
import vork.gfx.ShaderType;
import vork.gfx.SpriteBatch;
import vork.gfx.VkFont;
import vork.gfx.gui.GuiManager;
import vork.gfx.gui.drag.GuiDragAndDrop;
import vork.util.FilePath;


public class Client extends App {
	
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
	
	public static final Client instance = new Client();
	
	@Getter
	private ShaderProgram defaultShaderProgram = new ShaderProgram();
	
	@Getter
	private SpriteBatch batch = new SpriteBatch();
	
	private Camera camera = new Camera();
	
	@Getter
	private VkFont font;
	
	@Getter
	private VkFont boldFont;
	
	@Getter
	private VkFont bigFont;
	
	@Getter
	private NetworkManager networkManager = new NetworkManager();
	
	private Screen screen;
	
	//
	// Game related variables loaded in when joining the
	// game world.
	//
	
	public Entity player;
	public Map<Integer, Entity> entities = new HashMap<>();
	
	public Skill[] skills = new Skill[SkillType.NUM_SKILLS];
	
	public GuiDragAndDrop containersDragAndDrop;
	public InventoryMenu inventory;
	
	@Getter
	private RenderContext renderContext = new RenderContext(defaultShaderProgram, batch) {
		
		@Override
		protected void uploadUniforms(ShaderProgram program) {
			camera.update();
			program.uniformMatrix4fv("u_proj", camera.getProjMat());
		}
	};
	
	private static boolean hotReloadResources = false;
	
	static class A {
		
	}
	
	public static void main(String[] args) throws IOException {
	
		LaunchSettings settings = new LaunchSettings();
		settings.enableVsync  = true;
		settings.windowTitle  = "client 1.0";
		settings.windowWidth  = Constants.WORLD_PIXEL_SCALE * Constants.NUM_VISIBLE_PIXELS;
		settings.windowHeight = Constants.WORLD_PIXEL_SCALE * Constants.NUM_VISIBLE_PIXELS;
		settings.windowIcon   = Optional.of(FilePath.internal("tree_wnd_icon.png"));
		settings.shouldWindowResize = false;
		
		for (String arg : args) {
			if (arg.startsWith("-")) {
				if (arg.equals("-hotreload")) {
					hotReloadResources = true;
				}
			}
		}
		
		instance.run(settings);
	}
	
	public void setScreen(Screen screen) {
		GuiManager.clear();
		if (this.screen != null) {
			this.screen.dispose();
		}
		this.screen = screen;
		screen.init();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Screen> T getScreen() {
		return (T) this.screen;
	}
	
	public NetworkHandle getNetworkHandle() {
		return networkManager.getHandle();
	}
	
	public Entity getEntity(int entityId) {
		return entities.get(entityId);
	}

	@Override
	protected void init() throws IOException {
	
		glDisable(GL_MULTISAMPLE);
		
		NetworkEventBus eventBus = networkManager.getEventBus();
		eventBus.registerListener(new LoginListener());
		eventBus.registerListener(new EntitySpawnListener());
		eventBus.registerListener(new EntityDespawnListener());
		eventBus.registerListener(new EntityMoveListener());
		eventBus.registerListener(new ChatTextListener());
		eventBus.registerListener(new HealthChangeListener());
		eventBus.registerListener(new ChangeFacingDirectionListener());
		eventBus.registerListener(new EntityTeleportListener());
		eventBus.registerListener(new SetXpListener());
		eventBus.registerListener(new AnimationListener());
		eventBus.registerListener(new SetItemListener());
		eventBus.registerListener(new SpawnGroundItemListener());
		eventBus.registerListener(new DespawnGroundItemListener());
		eventBus.registerListener(new ContainerMoveListener());
		eventBus.registerListener(new PickupItemListener());
		eventBus.registerListener(new NpcChatListener());
		eventBus.registerListener(new OpenShopListener());
		
		defaultShaderProgram.attachShader(vertex, ShaderType.VERTEX);
		defaultShaderProgram.attachShader(fragment, ShaderType.FRAGMENT);
		defaultShaderProgram.compile();
		
		defaultShaderProgram.bind();
		batch.create(defaultShaderProgram);
		defaultShaderProgram.unbind();
		
		font = VkFont.create(FilePath.internal("fonts/font.png"), FilePath.internal("fonts/font.fmt.txt"), batch);
		boldFont = VkFont.create(FilePath.internal("fonts/font_bold.png"), FilePath.internal("fonts/font_bold.fmt.txt"), batch);
		bigFont = VkFont.create(FilePath.internal("fonts/big_font.png"), FilePath.internal("fonts/big_font.fmt.txt"), batch);
		
		setScreen(new LoadingScreen());
		//setScreen(new TestScreen());
		
	}

	@Override
	protected void tick() {
		
		if (hotReloadResources) {
			try {
				Resources.hotReload();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		networkManager.getEventBus().gameThreadPublish();
		
		screen.tick(renderContext);

		// Want to make sure all our data is sent by
		// at least the end of the tick.
		NetworkHandle handle = networkManager.getHandle();
		if (handle != null) {
			handle.flushBuffer();
		}
	}
	
	@Override
	protected void dispose() {
		networkManager.close();
		renderContext.dispose();
		font.dispose();
		boldFont.dispose();
		Resources.dispose();
	}
}
