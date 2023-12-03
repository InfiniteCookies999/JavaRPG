package client.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;

import client.Client;
import client.Constants;
import client.Screen;
import client.game.entity.HumanRenderer;
import client.game.items.ItemCatagory;
import client.game.items.ItemDefinition;
import vork.gfx.PaddedTexture;
import vork.gfx.RenderContext;
import vork.gfx.ShaderProgram;
import vork.gfx.ShaderType;
import vork.gfx.Texture;
import vork.gfx.TextureFilter;
import vork.util.AsyncResourceLoader;
import vork.util.AsyncResourceLoader.LoadedAsyncResource;
import vork.util.FilePath;

public class LoadingScreen implements Screen {
	
	private AsyncResourceLoader loader = new AsyncResourceLoader();

	private static final Pattern HUMAN_HAIR_PATH_RGX = Pattern.compile("human_hair_[0-9]+.png");

	private int humanSkinColorCounter  = 0;
	private int humanHairStyleCounter  = 0;

	private static final FilePath cacheDirectory;
	static {
		cacheDirectory = FilePath.external(System.getenv("LOCALAPPDATA")).append("java_rpg_cache99");
	}
	
	private static final Map<Integer, Integer> GRAYSCALE_MAP = new HashMap<>();
	static {
		GRAYSCALE_MAP.put(0xFFFFFFFF, 0);
		GRAYSCALE_MAP.put(0xFFAAAAAA, 1);
		GRAYSCALE_MAP.put(0xFF444444, 2);
	}
	
	public static String loadGLSLFile(FilePath file) throws IOException {
		// TODO: Here we could allow glsl files to have #include and
		// whatever else we might want
		String source = "";
		try (BufferedReader reader = file.getBufferedReader()) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				source += line + "\n";
			}
		}
		
		return source;
	}
	
	@Override
	public void init() {
		try {
			
			// LOADING GLSL FILES
			
			loader.loadDirectory(
					FilePath.internal("shaders"),
					LoadingScreen::loadGLSLFile,
					this::processGLSLFile);
			
			// LOADING TEXTURES
			
			loader.loadDirectory(
					FilePath.internal("textures/ui"),
					Texture::loadBufferOfPNG,
					this::processUITexture);

			loader.load(
					FilePath.internal("textures/tilesheets/tile_sheet_floors0.png"),
					Texture::loadBufferOfPNG,
					this::processTileSheet);
			loader.load(
					FilePath.internal("textures/tilesheets/tile_sheet_outside0.png"),
					Texture::loadBufferOfPNG,
					this::processTileSheet);
			loader.load(
					FilePath.internal("textures/tilesheets/tile_sheet_walls.png"),
					Texture::loadBufferOfPNG,
					this::processTileSheet);
			loader.load(
					FilePath.internal("textures/tilesheets/tile_sheet_decoration.png"),
					Texture::loadBufferOfPNG,
					this::processTileSheet);
			
			
			loader.loadDirectory(
					cacheDirectory.append("textures/human/pants"),
					Texture::loadBufferOfPNG,
					this::processHumanClothesTexture);
			loader.loadDirectory(
					cacheDirectory.append("textures/human/shirt"),
					Texture::loadBufferOfPNG,
					this::processHumanClothesTexture);
			loader.loadDirectory(
					cacheDirectory.append("textures/human/skin"),
					Texture::loadBufferOfPNG,
					this::processHumanSkinTexture);
			loader.loadDirectory(
					cacheDirectory.append("textures/human/head"),
					Texture::loadBufferOfPNG,
					this::processHumanHeadTexture);
			
			FilePath armorFile = cacheDirectory.append("textures/human/armor/load_order.txt");
			try (BufferedReader reader = armorFile.getBufferedReader()) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) continue;
					loader.load(
							cacheDirectory.append("textures/human/armor/" + line),
							Texture::loadBufferOfPNG,
							this::processHumanArmorTexture);	
				}
			}
			
			FilePath mobFile = cacheDirectory.append("textures/mob/mobs.txt");
			
			try (BufferedReader reader = mobFile.getBufferedReader()) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) continue;
					loader.load(
							cacheDirectory.append("textures/mob/" + line),
							Texture::loadBufferOfPNG,
							this::processMobTexture);	
				}
			}
			
			// TODO: Load async
			FilePath itemsFile = FilePath.internal("items.yaml");	
			try (InputStream itemsInStream = itemsFile.getInputStream()) {
				List<Map<String, Object>> contents = new Yaml().load(itemsInStream);
				for (Map<String, Object> itemDef : contents) {
					int textureId = (int) itemDef.get("texture_id");
					String name = (String) itemDef.get("name");
					ItemCatagory catagory = ItemCatagory.valueOf((String) itemDef.get("catagory"));
					
					ItemDefinition definition = new ItemDefinition();
					definition.setTextureId(textureId);
					definition.setName(name);
					definition.setCatagory(catagory);
					Resources.addItemDefinition(definition);
				}
			}
			
			
			loader.load(
					FilePath.internal("textures/items/item_sheet2.png"),
					Texture::loadBufferOfPNG,
					this::processItemTexture);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		loader.start();
		
	}

	@Override
	public void tick(RenderContext context) {
		Exception exception = loader.getPossibleEncounteredException();
		if (exception != null) {
			// TODO: May want to display the exception to the user.
			exception.printStackTrace();
			loader.stop();
		}
		
		loader.process();
		
		if (loader.isFinished()) {
			
			// Creating the shader programs.
			Resources.putShaderProgram(ShaderProgramType.DEFAULT, Client.instance.getDefaultShaderProgram());
			
			{
				ShaderProgram program = new ShaderProgram();
				program.attachShader(Resources.getShaderSource("vert.glsl"), ShaderType.VERTEX);
				program.attachShader(Resources.getShaderSource("entity_border_frag.glsl"), ShaderType.FRAGMENT);			
				program.compile();
				
				Resources.putShaderProgram(ShaderProgramType.ENTITY_BORDER, program);
			}
			
			{
				ShaderProgram program = new ShaderProgram();
				program.attachShader(Resources.getShaderSource("pos_provider_vert.glsl"), ShaderType.VERTEX);
				program.attachShader(Resources.getShaderSource("entity_default_attacked_frag.glsl"), ShaderType.FRAGMENT);			
				program.compile();
				
				Resources.putShaderProgram(ShaderProgramType.DEFAULT_ATTACKED, program);
			}
			
			{
				ShaderProgram program = new ShaderProgram();
				program.attachShader(Resources.getShaderSource("pos_provider_vert.glsl"), ShaderType.VERTEX);
				program.attachShader(Resources.getShaderSource("entity_dying_frag.glsl"), ShaderType.FRAGMENT);			
				program.compile();
				
				Resources.putShaderProgram(ShaderProgramType.ENTITY_DIED, program);
			}
			
			
			Client.instance.setScreen(new LoginScreen());
		}
	}

	private void processItemTexture(LoadedAsyncResource resource) {
		Texture.Buffer buffer = (Texture.Buffer) resource.getLoadedObject();
		Resources.addItemTexture(Texture.createFromBuffer(buffer, TextureFilter.NEAREST_NEAREST));
	}

	private void processGLSLFile(LoadedAsyncResource resource) {
		String source = (String) resource.getLoadedObject();
		Resources.putShaderSource(resource.getFile(), source);
	}

	private void processUITexture(AsyncResourceLoader.LoadedAsyncResource resource) {
		Texture.Buffer buffer = (Texture.Buffer) resource.getLoadedObject();
		Resources.putUITexture(
				resource.getFile(),
				Texture.createFromBuffer(buffer, TextureFilter.NEAREST_NEAREST));
	}
	
	private void processHumanClothesTexture(AsyncResourceLoader.LoadedAsyncResource resource) {
		Texture.Buffer buffer = (Texture.Buffer) resource.getLoadedObject();
		
		for (int i = 0; i < Resources.NUM_HUMAN_CLOTHES_COLORS; i++) {
			Resources.addHumanTexturePixels(applyColorsToHumanTexturePixels(buffer, Constants.HUMAN_CLOTHES_COLORS[i]));
		}
	}
	
	private void processHumanArmorTexture(LoadedAsyncResource resource) {
		
		Texture.Buffer buffer = (Texture.Buffer) resource.getLoadedObject();
		
		int index = Resources.addHumanTexturePixels(buffer.getPixels());
		if (HumanRenderer.START_INDEXES_FOR_ARMOR == 0) {
			HumanRenderer.START_INDEXES_FOR_ARMOR = index;
		}
	}
	
	private void processHumanSkinTexture(AsyncResourceLoader.LoadedAsyncResource resource) {
		Texture.Buffer buffer = (Texture.Buffer) resource.getLoadedObject();
		int index = Resources.addHumanTexturePixels(buffer.getPixels());
		Resources.TXR_HUMAN_SKIN_IDXS[humanSkinColorCounter++] = index;
	}
	
	private void processHumanHeadTexture(AsyncResourceLoader.LoadedAsyncResource resource) {
		Texture.Buffer buffer = (Texture.Buffer) resource.getLoadedObject();
		String fileName = resource.getFile().getName();
		
		if (fileName.equals("human_eyes.png")) {
			int[] pixels = buffer.getPixels();
			for (int i = 0; i < pixels.length; i++) {
				if (pixels[i] != 0) {
					Resources.HUMAN_EYE_BUFFER_INDEXES.add(i);
				}
			}
		} else if (HUMAN_HAIR_PATH_RGX.matcher(fileName).matches()) {
			for (int i = 0; i < Resources.NUM_HUMAN_HAIR_COLORS; i++) {
				int index = Resources.addHumanTexturePixels(applyColorsToHumanTexturePixels(buffer, Constants.HUMAN_HAIR_COLORS[i]));
				Resources.TXR_HUMAN_HAIR_IDXS[humanHairStyleCounter][i] = index;
			}
			++humanHairStyleCounter;
		}
	}
	
	private int[] applyColorsToHumanTexturePixels(Texture.Buffer buffer, int[] colorMap) {
		int w = 48, h = 128;
		int[] pixels = buffer.getPixels();
		int[] result = new int[w*h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int pixel = pixels[y*w + x];
				Integer grayscale = GRAYSCALE_MAP.get(pixel);
				if (grayscale != null) {
					result[y*w + x] = colorMap[grayscale];
				} else {
					result[y*w + x] = pixel;
				}
				
			}
		}
		return result;
	}
	
	private void processTileSheet(AsyncResourceLoader.LoadedAsyncResource resource) {
		Texture.Buffer buffer = (Texture.Buffer) resource.getLoadedObject();
		
		PaddedTexture tileSheet = Texture.createFromBuffer(buffer, TextureFilter.NEAREST_NEAREST, Constants.TILE_SIZE, Constants.TILE_SIZE);
		Resources.addTileSheet(resource.getFile(), tileSheet);
		
	}
	
	private void processMobTexture(LoadedAsyncResource resource) {
		Texture.Buffer buffer = (Texture.Buffer) resource.getLoadedObject();
		Texture mobTexture = Texture.createFromBuffer(buffer, TextureFilter.NEAREST_NEAREST);
		Resources.addMobTexture(resource.getFile(), mobTexture);
	}
	
	@Override
	public void dispose() {
		// Want to stop the loader thread in case the user tried
		// closing the window while in the loading screen.
		loader.stop();
	}

}
