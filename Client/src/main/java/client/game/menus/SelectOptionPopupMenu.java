package client.game.menus;

import java.util.List;

import client.Client;
import client.game.PlayScreen;
import client.game.Resources;
import client.game.SelectOptions;
import client.game.entity.Entity;
import client.game.items.GroundItem;
import client.game.menus.container.ContainerSlot;
import vork.App;
import vork.gfx.Camera;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiChildDisplayMode;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiRenderMode;
import vork.gfx.gui.TextFormatBuilder;
import vork.math.Vector2;

public class SelectOptionPopupMenu extends GuiComponent {

	private class SelectOptionButton extends GuiLabel {
		private int optionId;
		private int selectedEntityId;
		private int selectedGroundItemId;
		private ContainerSlot selectedContainerSlot;
		
		private SelectOptionButton(String text, int width, int height) {
			super(text, width, height);
		}
	}
	
	private static final int BUTTON_HEIGHT = 22;
	private static final int DEFAULT_BUTTON_WIDTH = 100 - 8;
	private int maxButtonWidth;
	private Camera worldCamera;
	private Vector2 entityLocation;
	private Vector2 uiCoordsLocation;
	private int selectedIndex = 0;
	
	private final Texture selectedTexture;
	private final Texture notSelectedTexture;
	
	public SelectOptionPopupMenu(Camera worldCamera) {
		super(100, 100);
		this.worldCamera = worldCamera;
		setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN);
		setHidden(true);
		selectedTexture = Resources.getUITexture("select_option_button.png");
		notSelectedTexture = Resources.getUITexture("panel_button.png");
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		
		if (!isHidden()) {
			if (entityLocation != null) {
				Vector2 position = worldCamera.getProjMat().mul(entityLocation);
				position = position.add(1, 1);
				position.x *= App.gfx.getWndWidth()*0.5f;
				position.y *= App.gfx.getWndHeight()*0.5f;
				
				// Make sure not to render outside the window.
				if (position.x + this.width > App.gfx.getWndWidth()) {
					position.x = App.gfx.getWndWidth() - this.width;
				}
				if (position.y + this.height > App.gfx.getWndHeight()) {
					position.y = App.gfx.getWndHeight() - this.height;
				}
				
				setPosition((int) position.x, (int) position.y);		
			} else {
				setPosition((int) uiCoordsLocation.x, (int) uiCoordsLocation.y);
			}
		}
		
		super.render(batch, parentRX, parentRY);
	}
	
	public void open(List<Object> selectedObjects) {
		
		setHidden(false);
		clearChildren();
		PlayScreen playScreen = Client.instance.getScreen();
		playScreen.getTextChatMenu().getTextField().removeFocus();

		selectedIndex = 0;
		
		maxButtonWidth = DEFAULT_BUTTON_WIDTH;
		selectedObjects.forEach(this::createButtons);
		maxButtonWidth += 8; // Giving it some padding.
		
		addSelectButton(createOptionButton("close", 0, null));
		
		for (int i = 0; i < getNumberOfChildren(); i++) {
			GuiComponent child = getChild(i);
			child.width = maxButtonWidth;
		}
		
		getChild(0).setTexture(selectedTexture);
		
		this.height = BUTTON_HEIGHT * getNumberOfChildren();
		
		if (selectedObjects.size() == 1 && selectedObjects.get(0) instanceof ContainerSlot) {
			entityLocation = null;
			uiCoordsLocation = new Vector2(App.input.getCursorWndX(), App.input.getCursorWndY());
		} else {
			float minObjectX = Float.MAX_VALUE, minObjectY = Float.MAX_VALUE;
			float maxObjectWidth = 0;
			for (Object selectedObject : selectedObjects) {
				if (selectedObject instanceof Entity) {
					Entity selectedEntity = (Entity) selectedObject;
					if (selectedEntity.x < minObjectX) minObjectX = selectedEntity.x;
					if (selectedEntity.y < minObjectY) minObjectY = selectedEntity.y;
					if (selectedEntity.renderer.getEntityWidth() > maxObjectWidth)
						maxObjectWidth = selectedEntity.renderer.getEntityWidth();	
				} else if (selectedObject instanceof GroundItem) {
					GroundItem selectedGroundItem = (GroundItem) selectedObject;
					if (selectedGroundItem.getX() < minObjectX) minObjectX = selectedGroundItem.getX();
					if (selectedGroundItem.getY() < minObjectY) minObjectY = selectedGroundItem.getY();
					if (selectedGroundItem.getWidth() > maxObjectWidth)
						maxObjectWidth = selectedGroundItem.getWidth();
				}
			}
			
			uiCoordsLocation = null;
			entityLocation = new Vector2(minObjectX + maxObjectWidth * 0.5f, minObjectY + 8);	
		}
	}
	
	private void createButtons(Object selectedObject) {
		if (selectedObject instanceof Entity) {
			createButtonsForEntity((Entity) selectedObject);
		} else if (selectedObject instanceof GroundItem) {
			createButtonsForGroundItem((GroundItem) selectedObject);
		} else if (selectedObject instanceof ContainerSlot) {
			createButtonsForContainerSlot((ContainerSlot) selectedObject);
		}
	}

	private void createButtonsForEntity(Entity selectedEntity) {
		if ((selectedEntity.selectOptions & SelectOptions.ATTACK) != 0) {
			SelectOptionButton button = createOptionButton("", SelectOptions.ATTACK, selectedEntity);
			int level = selectedEntity.getCombatLevel();
			button.setFormattedText(
					new TextFormatBuilder("attack ", Color.WHITE)
						.append(selectedEntity.getName(), Color.YELLOW)
						.append(String.format(" (lvl %s)", level), Color.CYAN)
						.build()
			);
			addSelectButton(button);
		}
		if ((selectedEntity.selectOptions & SelectOptions.TALK_TO) != 0) {
			SelectOptionButton button = createOptionButton("", SelectOptions.TALK_TO, selectedEntity);
			button.setFormattedText(
					new TextFormatBuilder("talk to ", Color.WHITE)
					    .append(selectedEntity.getName(), Color.YELLOW)
					    .build()
			);
			addSelectButton(button);
		}
		if ((selectedEntity.selectOptions & SelectOptions.FOLLOW) != 0) {
			SelectOptionButton button = createOptionButton("", SelectOptions.FOLLOW, selectedEntity);
			button.setFormattedText(
					new TextFormatBuilder("follow ", Color.WHITE)
					    .append(selectedEntity.getName(), Color.YELLOW)
					    .build()
			);
			addSelectButton(button);
		}
		if ((selectedEntity.selectOptions & SelectOptions.OPEN_SHOP) != 0) {
			addSelectButton(createOptionButton("open shop", SelectOptions.OPEN_SHOP, selectedEntity));
		}
	}
	
	private void createButtonsForGroundItem(GroundItem groundItem) {
		SelectOptionButton button = createOptionButton("", SelectOptions.PICKUP_ITEM, groundItem);
		String itemName =  groundItem.getItemDefinition().getName();
		button.setFormattedText(
				new TextFormatBuilder("pickup ", Color.WHITE)
					.append(itemName, Color.rgb(0xcc29a3))
					.build()
		);
		addSelectButton(button);
	}

	private void createButtonsForContainerSlot(ContainerSlot containerSlot) {
		addSelectButton(createOptionButton("drop", SelectOptions.DROP_ITEM, containerSlot));
	}
	
	private SelectOptionButton createOptionButton(String name, int optionId, Object selectedObject) {
		SelectOptionButton button = new SelectOptionButton(name, 0, BUTTON_HEIGHT);
		button.setFont(Client.instance.getBoldFont());
		button.optionId = optionId;
		if (selectedObject != null) {
			if (selectedObject instanceof Entity) {
				button.selectedEntityId = ((Entity) selectedObject).id;	
			} else if (selectedObject instanceof GroundItem) {
				button.selectedGroundItemId = ((GroundItem) selectedObject).getId();
			} else if (selectedObject instanceof ContainerSlot) {
				button.selectedContainerSlot = ((ContainerSlot) selectedObject);
			}
		}
		button.setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		button.setTexture(notSelectedTexture);
		button.setTextOffsetY(1);
		int buttonIndex = getNumberOfChildren();
		button.addCallback(GuiCallbackType.HOVER_ENTER, () -> {
			int prevSelectedIndex = selectedIndex;
			selectedIndex = buttonIndex;
			getChild(prevSelectedIndex).setTexture(notSelectedTexture);
			getChild(selectedIndex).setTexture(selectedTexture);
		});
		button.addCallback(GuiCallbackType.LEFT_CLICK, () -> selectOption(button));
		return button;
	}
	
	private void addSelectButton(SelectOptionButton button) {
		int textLength = button.getFont().getLengthOfText(button.getText());
		if (textLength > maxButtonWidth) {
			maxButtonWidth = textLength;
		}
		addChild(button);
	}
	
	private void selectOption(SelectOptionButton button) {
		setHidden(true);
		
		if (button.optionId == -1) {
			// close button.
			return;
		}
		
		PlayScreen playScreen = Client.instance.getScreen();
		playScreen.cancelCurrentAction(false);
		
		if (button.optionId == SelectOptions.PICKUP_ITEM) {
			playScreen.getGroundItemFollower().setGroundItemId(button.selectedGroundItemId);
			return;
		} else if (button.optionId == SelectOptions.DROP_ITEM) {
			button.selectedContainerSlot.dropItem();
			return;
		}
		
		Entity entity = Client.instance.getEntity(button.selectedEntityId);
		if (entity == null || entity.isDead()) {
			return;
		}
		
		playScreen.performEntityInteraction(entity, button.optionId);
	}
}
