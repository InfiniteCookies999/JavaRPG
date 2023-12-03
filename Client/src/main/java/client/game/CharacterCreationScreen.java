package client.game;

import org.lwjgl.opengl.GL20;

import client.Client;
import client.Constants;
import client.Screen;
import client.game.entity.Direction;
import client.game.entity.Entity;
import client.game.entity.HumanRenderer;
import client.game.world.Location;
import client.net.out.CharacterCreatePacketOut;
import vork.App;
import vork.gfx.Camera;
import vork.gfx.Color;
import vork.gfx.RenderContext;
import vork.gfx.SpriteBatch;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiChildDisplayMode;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiManager;
import vork.gfx.gui.GuiRenderMode;
import vork.gfx.gui.GuiTextAlignment;

public class CharacterCreationScreen implements Screen {

	private GuiComponent panel;
	
	private Entity displayCharacter;
	private HumanRenderer characterRenderer;
	
	private static final Direction[] facingDirections = new Direction[4];
	static {
		facingDirections[0] = Direction.SOUTH;
		facingDirections[1] = Direction.EAST;
		facingDirections[2] = Direction.NORTH;
		facingDirections[3] = Direction.WEST;
	}
	
	private GuiComponent[] genderButtons = new GuiComponent[3];
	
	private int facingDirectionIdx;
	private int genderSelectIdx;
	
	private int shirtStyle;
	private int shirtColor;
	private int pantsStyle;
	private int pantsColor;
	
	private boolean sentRequest = false;
	
	private class CharacterdisplayPanel extends GuiComponent {

		private Camera camera = new Camera();
		private Entity displayCharacter;
		
		private CharacterdisplayPanel(GuiLocation location, int width, int height) {
			super(location, width, height);
			camera.zoom = Constants.WORLD_PIXEL_SCALE;
		}
		
		@Override
		public void render(SpriteBatch batch, int parentRX, int parentRY) {
			super.render(batch, parentRX, parentRY);
			
			int eW = displayCharacter.renderer.getEntityWidth();
			int eH = displayCharacter.renderer.getEntityHeight();
			
			int xx = outX + outWidth/2 - (eW*Constants.WORLD_PIXEL_SCALE/2);
			int yy = outY + outHeight/2 - (eH*Constants.WORLD_PIXEL_SCALE/2) + 10;
			
			displayCharacter.x = xx / Constants.WORLD_PIXEL_SCALE;
			displayCharacter.y = yy / Constants.WORLD_PIXEL_SCALE;
			
			// Flush the UI that is not part of the display character
			// overlay that has been generated so far.
			GuiManager.flushRendering(batch);
			
			displayCharacter.render(batch);
			
			Client.instance.getRenderContext().render(ShaderProgram -> {
				camera.update();
				ShaderProgram.uniformMatrix4fv("u_proj", camera.getProjMat());
			});
		}
	}
	
	@Override
	public void init() {
		
		panel = new GuiComponent(GuiLocation.CENTER, 300, 260);
		panel.setTexture(Resources.getUITexture("window_panel_1.png"));
		panel.setRenderMode(GuiRenderMode.NINE_PATCH);
		
		displayCharacter = new Entity();
		displayCharacter.setFacingDirection(Direction.SOUTH);
		displayCharacter.currentLocation = new Location(0, 0);
		displayCharacter.futureLocation = new Location(0, 0);
		displayCharacter.setHealth(1);
		displayCharacter.setMaxHealth(1);
		
		characterRenderer = new HumanRenderer();
		displayCharacter.renderer = characterRenderer;
		characterRenderer.bodyType = HumanRenderer.getHumanShirtType(0, 0);
		characterRenderer.legsType = HumanRenderer.getHumanPantsType(0, 0);
		characterRenderer.setup();
		
		GuiComponent topDiv = new GuiComponent(panel.width, 240/2);
		topDiv.setLocation(GuiLocation.TOP);
		topDiv.setColor(Color.FULL_TRANSPARENT);
		
		GuiComponent bottomDiv = new GuiComponent(panel.width, panel.height - topDiv.height);
		bottomDiv.setLocation(GuiLocation.BOTTOM);
		bottomDiv.setColor(Color.FULL_TRANSPARENT);
		
		CharacterdisplayPanel characterDisplayPanel = new CharacterdisplayPanel(GuiLocation.TOP_LEFT, 100, 120);
		characterDisplayPanel.setTexture(Resources.getUITexture("window_panel_3.png"));
		characterDisplayPanel.setRenderMode(GuiRenderMode.NINE_PATCH);
		characterDisplayPanel.displayCharacter = displayCharacter;
		
		GuiComponent characterDisplayPanelLB = new GuiComponent(Resources.getUITexture("panel_arrow_button.png"));
		characterDisplayPanelLB.setHoverColor(Color.GREEN);
		characterDisplayPanelLB.flipX = true;
		characterDisplayPanelLB.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
			--facingDirectionIdx;
			if (facingDirectionIdx < 0) {
				facingDirectionIdx = facingDirections.length - 1;
			}
		});
		
		GuiComponent characterDisplayPanelRB = new GuiComponent(Resources.getUITexture("panel_arrow_button.png"));
		characterDisplayPanelRB.setHoverColor(Color.GREEN);
		characterDisplayPanelRB.setLocation(GuiLocation.RIGHT);
		characterDisplayPanelRB.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
			++facingDirectionIdx;
			if (facingDirectionIdx >= facingDirections.length) {
				facingDirectionIdx = 0;
			}
		});
		
		characterDisplayPanel.addChild(characterDisplayPanelLB);
		characterDisplayPanel.addChild(characterDisplayPanelRB);
		
		GuiComponent genderSelection = new GuiComponent(180, 40);
		genderSelection.setLocation(GuiLocation.TOP);
		genderSelection.x = characterDisplayPanel.width + 4;
		genderSelection.setColor(Color.FULL_TRANSPARENT);
		
		{ // gender
		
			GuiLabel genderSelectionTitle = new GuiLabel("Gender", 0, Client.instance.getBoldFont().getGlyphHeight());
			genderSelectionTitle.setFont(Client.instance.getBoldFont());
			genderSelectionTitle.setLocation(GuiLocation.TOP);
			genderSelectionTitle.setTextAlignment(GuiTextAlignment.NONE);
			
			GuiLabel manGenderButton       = createGenderButton("Man", 0);
			GuiLabel womanGenderButton     = createGenderButton("Woman", 1);
			GuiLabel nonBinaryGenderButton = createGenderButton("Non-Bin.", 2);
			
			genderSelection.addChild(genderSelectionTitle);
			genderSelection.addChild(manGenderButton);
			genderSelection.addChild(womanGenderButton);
			genderSelection.addChild(nonBinaryGenderButton);
			
		}
		
		GuiComponent skinToneSelection = new GuiComponent(180, 40);
		skinToneSelection.setLocation(GuiLocation.TOP);
		skinToneSelection.x = characterDisplayPanel.width + 4;
		skinToneSelection.y = -genderSelection.height;
		skinToneSelection.setColor(Color.FULL_TRANSPARENT);
		
		{ // Skin Tone
			GuiLabel skinSelectionTitle = new GuiLabel("Skin Tone", 0, Client.instance.getBoldFont().getGlyphHeight());
			skinSelectionTitle.setFont(Client.instance.getBoldFont());
			skinSelectionTitle.setLocation(GuiLocation.TOP);
			skinSelectionTitle.setTextAlignment(GuiTextAlignment.NONE);
			
			GuiLabel button = new GuiLabel("tone", 100, 16);
			button.setFont(Client.instance.getFont());
			button.setLocation(GuiLocation.TOP);
			button.setColor(Color.BLACK);
			button.y = -Client.instance.getBoldFont().getGlyphHeight() - 4;
			button.setColor(Color.FULL_TRANSPARENT);
			
			GuiComponent leftButton = new GuiComponent(Resources.getUITexture("panel_arrow_button.png"));
			leftButton.setLocation(GuiLocation.LEFT);
			leftButton.flipX = true;
			leftButton.setHoverColor(Color.GREEN);
			leftButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
				--characterRenderer.skinColor;
				if (characterRenderer.skinColor < 0) {
					characterRenderer.skinColor = Resources.NUM_HUMAN_SKIN_COLORS - 1;
				}
				characterRenderer.reset();
			});
			
			GuiComponent rightButton = new GuiComponent(Resources.getUITexture("panel_arrow_button.png"));
			rightButton.setLocation(GuiLocation.LEFT);
			rightButton.setLocation(GuiLocation.RIGHT);
			rightButton.setHoverColor(Color.GREEN);
			rightButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
				++characterRenderer.skinColor;
				if (characterRenderer.skinColor >= Resources.NUM_HUMAN_SKIN_COLORS) {
					characterRenderer.skinColor = 0;
				}
				characterRenderer.reset();
			});
			
			button.addChild(leftButton);
			button.addChild(rightButton);
			skinToneSelection.addChild(skinSelectionTitle);
			skinToneSelection.addChild(button);
		}
		
		GuiComponent horizontalLineSeperator = new GuiComponent(Resources.getUITexture("horizontal_line_seperator.png"));
		horizontalLineSeperator.setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		horizontalLineSeperator.x = characterDisplayPanel.width;
		horizontalLineSeperator.width = topDiv.width - characterDisplayPanel.width - 2;
		
		GuiLabel confirmButton = new GuiLabel("confirm", 80, 24);
		confirmButton.setTexture(Resources.getUITexture("panel_button.png"));
		confirmButton.setFont(Client.instance.getFont());
		confirmButton.setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		confirmButton.x = characterDisplayPanel.width + 4;
		confirmButton.setTextOffsetY(1);
		confirmButton.setHoverColor(Color.GREEN);
		confirmButton.y = 10;
		confirmButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
			if (sentRequest) return;
			new CharacterCreatePacketOut(
					genderSelectIdx,
					characterRenderer.skinColor,
					characterRenderer.hairStyle,
					characterRenderer.hairColor,
					characterRenderer.eyeColor,
					pantsStyle,
					pantsColor,
					shirtStyle,
					shirtColor)
			.send();
			sentRequest = true;
		});
		
		topDiv.addChild(confirmButton);
		topDiv.addChild(characterDisplayPanel);
		topDiv.addChild(genderSelection);
		topDiv.addChild(horizontalLineSeperator);
		topDiv.addChild(skinToneSelection);
		
		{ // Bottom div
			
			GuiComponent styleDiv = new GuiComponent(bottomDiv.width/2, bottomDiv.height);
			styleDiv.setLocation(GuiLocation.LEFT);
			styleDiv.setColor(Color.FULL_TRANSPARENT);
			styleDiv.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN, 0, 8);
			
			{ // Style div
				
				GuiLabel styleTitle = new GuiLabel("Style", 0, Client.instance.getBoldFont().getGlyphHeight());
				styleTitle.setFont(Client.instance.getBoldFont());
				styleTitle.setLocation(GuiLocation.CENTER);
				GuiLabel hairStyleButton  = createChoiceButton("hair",
					() -> {
						++characterRenderer.hairStyle;
						if (characterRenderer.hairStyle >= Resources.NUM_HUMAN_HAIR_STYLES) {
							characterRenderer.hairStyle = 0;
						}
						characterRenderer.reset();
					},
					() -> {
						--characterRenderer.hairStyle;
						if (characterRenderer.hairStyle < 0) {
							characterRenderer.hairStyle = Resources.NUM_HUMAN_HAIR_STYLES - 1;
						}
						characterRenderer.reset();
					});
				GuiLabel shirtStyleButton = createChoiceButton("shirt",
					() -> {
						++shirtStyle;
						if (shirtStyle >= Resources.NUM_HUMAN_SHIRT_STYLES) {
							shirtStyle = 0;
						}
						characterRenderer.bodyType = HumanRenderer.getHumanShirtType(shirtStyle, shirtColor);
						characterRenderer.reset();
					},
					() -> {
						--shirtStyle;
						if (shirtStyle < 0) {
							shirtStyle = Resources.NUM_HUMAN_SHIRT_STYLES - 1;
						}
						characterRenderer.bodyType = HumanRenderer.getHumanShirtType(shirtStyle, shirtColor);
						characterRenderer.reset();
					});
				GuiLabel pantsStyleButton = createChoiceButton("pants",
					() -> {
						++pantsStyle;
						if (pantsStyle >= Resources.NUM_HUMAN_PANTS_STYLES) {
							pantsStyle = 0;
						}
						characterRenderer.legsType = HumanRenderer.getHumanPantsType(pantsStyle, pantsColor);
						characterRenderer.reset();
					},
					() -> {
						--pantsStyle;
						if (pantsStyle < 0) {
							pantsStyle = Resources.NUM_HUMAN_PANTS_STYLES - 1;
						}
						characterRenderer.legsType = HumanRenderer.getHumanPantsType(pantsStyle, pantsColor);
						characterRenderer.reset();
					});
				
				styleDiv.addChild(styleTitle);
				styleDiv.addChild(hairStyleButton);
				styleDiv.addChild(shirtStyleButton);
				styleDiv.addChild(pantsStyleButton);
				
			}
			
			GuiComponent colorDiv = new GuiComponent(bottomDiv.width/2, bottomDiv.height);
			colorDiv.setLocation(GuiLocation.RIGHT);
			colorDiv.setColor(Color.FULL_TRANSPARENT);
			colorDiv.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN, 0, 8);
			
			{ // Color div
				GuiLabel colorTitle = new GuiLabel("color", 0, Client.instance.getBoldFont().getGlyphHeight());
				colorTitle.setFont(Client.instance.getBoldFont());
				colorTitle.setLocation(GuiLocation.CENTER);
				
				GuiLabel hairColorButton  = createChoiceButton("hair",
					() -> {
						++characterRenderer.hairColor;
						if (characterRenderer.hairColor >= Resources.NUM_HUMAN_HAIR_COLORS) {
							characterRenderer.hairColor = 0;
						}
						characterRenderer.reset();
					},
					() -> {
						--characterRenderer.hairColor;
						if (characterRenderer.hairColor < 0) {
							characterRenderer.hairColor = Resources.NUM_HUMAN_HAIR_COLORS - 1;
						}
						characterRenderer.reset();
					});
				GuiLabel shirtColorButton = createChoiceButton("shirt",
					() -> {
						++shirtColor;
						if (shirtColor >= Resources.NUM_HUMAN_CLOTHES_COLORS) {
							shirtColor = 0;
						}
						characterRenderer.bodyType = HumanRenderer.getHumanShirtType(shirtStyle, shirtColor);
						characterRenderer.reset();
					},
					() -> {
						--shirtColor;
						if (shirtColor < 0) {
							shirtColor = Resources.NUM_HUMAN_CLOTHES_COLORS - 1;
						}
						characterRenderer.bodyType = HumanRenderer.getHumanShirtType(shirtStyle, shirtColor);
						characterRenderer.reset();
					});
				GuiLabel pantsColorButton = createChoiceButton("pants",
					() -> {
						++pantsColor;
						if (pantsColor >= Resources.NUM_HUMAN_CLOTHES_COLORS) {
							pantsColor = 0;
						}
						characterRenderer.legsType = HumanRenderer.getHumanPantsType(pantsStyle, pantsColor);
						characterRenderer.reset();
					},
					() -> {
						--pantsColor;
						if (pantsColor < 0) {
							pantsColor = Resources.NUM_HUMAN_CLOTHES_COLORS - 1;
						}
						characterRenderer.legsType = HumanRenderer.getHumanPantsType(pantsStyle, pantsColor);
						characterRenderer.reset();
					});
				GuiLabel eyeColorButton = createChoiceButton("eyes",
					() -> {
						++characterRenderer.eyeColor;
						if (characterRenderer.eyeColor >= Constants.HUMAN_EYE_COLORS.length) {
							characterRenderer.eyeColor = 0;
						}
						characterRenderer.reset();
					},
					() -> {
						--characterRenderer.eyeColor;
						if (characterRenderer.eyeColor < 0) {
							characterRenderer.eyeColor = Constants.HUMAN_EYE_COLORS.length - 1;
						}
						characterRenderer.reset();
					});
				
				
				colorDiv.addChild(colorTitle);
				colorDiv.addChild(hairColorButton);
				colorDiv.addChild(shirtColorButton);
				colorDiv.addChild(pantsColorButton);
				colorDiv.addChild(eyeColorButton);
			}
			
			bottomDiv.addChild(styleDiv);
			bottomDiv.addChild(colorDiv);
		}
		
		
		panel.addChild(topDiv);
		panel.addChild(bottomDiv);
		
		GuiManager.addRootComponent(panel);
		
	}

	private GuiLabel createGenderButton(String title, int idx) {
		final int buttonWidth = 60;
		GuiLabel genderButton = new GuiLabel(title, buttonWidth, 20);
		genderButton.setLocation(GuiLocation.TOP);
		genderButton.setFont(Client.instance.getFont());
		genderButton.setTexture(Resources.getUITexture("panel_button.png"));
		genderButton.setRenderMode(GuiRenderMode.NINE_PATCH);
		genderButton.x = buttonWidth * idx;
		genderButton.y = -Client.instance.getBoldFont().getGlyphHeight() - 4;
		genderButton.setTextOffsetY(1);
		genderButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
			genderSelectIdx = idx;
		});
		genderButtons[idx] = genderButton;
		return genderButton;
	}
	
	private GuiLabel createChoiceButton(String title, Runnable rightCallback, Runnable leftCallback) {
		GuiLabel styleButton = new GuiLabel(title, 100, 16);
		styleButton.setFont(Client.instance.getFont());
		styleButton.setLocation(GuiLocation.CENTER);
		styleButton.setColor(Color.FULL_TRANSPARENT);
		
		GuiComponent leftButton = new GuiComponent(Resources.getUITexture("panel_arrow_button.png"));
		leftButton.setLocation(GuiLocation.LEFT);
		leftButton.setHoverColor(Color.GREEN);
		leftButton.flipX = true;
		leftButton.addCallback(GuiCallbackType.LEFT_CLICK, leftCallback);
		
		GuiComponent rightButton = new GuiComponent(Resources.getUITexture("panel_arrow_button.png"));
		rightButton.setLocation(GuiLocation.RIGHT);
		rightButton.setHoverColor(Color.GREEN);
		rightButton.addCallback(GuiCallbackType.LEFT_CLICK, rightCallback);
		
		styleButton.addChild(leftButton);
		styleButton.addChild(rightButton);
		
		return styleButton;
	}
	
	@Override
	public void tick(RenderContext context) {
		
		App.gfx.clear(GL20.GL_COLOR_BUFFER_BIT);
		App.gfx.enableBlend(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		if (!sentRequest) {
			GuiManager.update();	
		}
		
		displayCharacter.setFacingDirection(facingDirections[facingDirectionIdx]);
		
		for (int i = 0; i < genderButtons.length; i++) {
			if (i == genderSelectIdx) {
				genderButtons[i].setTexture(Resources.getUITexture("select_option_button.png"));
			} else {
				genderButtons[i].setTexture(Resources.getUITexture("panel_button.png"));
			}
		}
		
		GuiManager.render(Client.instance.getBatch());
		
		context.render();
		
	}

	@Override
	public void dispose() {
		displayCharacter.renderer.dispose();
	}
}
