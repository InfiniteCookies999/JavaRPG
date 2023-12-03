package client.game.menus.container;

import java.util.ArrayList;
import java.util.List;

import client.Client;
import client.game.PlayScreen;
import client.game.items.ItemDefinition;
import client.game.items.ItemStack;
import client.game.menus.SelectOptionPopupMenu;
import client.net.out.ContainerMovePacketOut;
import client.net.out.DropItemPacketOut;
import lombok.Getter;
import vork.App;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.drag.GuiDragSource;
import vork.gfx.gui.drag.GuiDragTarget;
import vork.input.Keys;

public class ContainerSlot {
	
	private class DraggedItem extends GuiComponent {

		private ItemStack itemStack;
		private ContainerSlot fromSlot;
		
		public DraggedItem(ItemStack itemStack) {
			super(ITEM_SIZE * 2, ITEM_SIZE * 2);
			this.itemStack = itemStack;
		}
		
	}
	
	private class Source extends GuiDragSource {

		private final ContainerSlot fromSlot;
		
		public Source(GuiComponent container, ContainerSlot fromSlot) {
			super(container);
			this.fromSlot = fromSlot;
		}

		@Override
		public GuiComponent beginDrag(int cursorX, int cursorY) {
			if (App.input.isAnyKeyPressed(Keys.LEFT_SHIFT, Keys.RIGHT_SHIFT) && !fromSlot.locked) {
				// Dropping the item!
				dropItem();
				return null;
			}
			
			if (itemStack != null) {
				DraggedItem draggedItem = new DraggedItem(itemStack);
				setTextureInfoForDisplayItem(itemStack, draggedItem);
				draggedItem.x = cursorX - ITEM_SIZE;
				draggedItem.y = cursorY - ITEM_SIZE;
				draggedItem.fromSlot = fromSlot;
				getContainer().clearChildren();
				return draggedItem;
			}
			return null;
		}

		@Override
		public void dragCanceled(GuiComponent draggedComponent) {
			DraggedItem draggedItem = (DraggedItem) draggedComponent;
			addDisplayChild(draggedItem.itemStack);
		}	
	}
	
	private class Target extends GuiDragTarget {

		private final ContainerSlot toSlot;
		
		public Target(GuiComponent container, ContainerSlot toSlot) {
			super(container);
			this.toSlot = toSlot;
		}

		@Override
		public boolean onDrop(GuiComponent draggedComponent) {
			if (!(draggedComponent instanceof DraggedItem)) {
				return false;
			}
			DraggedItem draggedItem = (DraggedItem) draggedComponent;
			ContainerSlot fromSlot = draggedItem.fromSlot;
			if (toSlot.locked || fromSlot.locked) {
				return false;
			}
			
			/*if (fromSlot.itemStack != null) {
				// Swapping items.
				fromSlot.setItemStack(toSlot.itemStack);
				toSlot.setItemStack(draggedItem.itemStack);
			} else {
				fromSlot.setItemStack(null);
				toSlot.setItemStack(draggedItem.itemStack);	
			}*/
			
			fromSlot.locked = true;
			toSlot.locked = true;
			new ContainerMovePacketOut(0, fromSlot.slotIndex, toSlot.slotIndex).send();
			return true;
		}
	}
	
	@Getter
	private final GuiComponent container;
	
	@Getter
	private final Source source;
	
	@Getter
	private final Target target;
	
	@Getter
	private int slotIndex;
	
	@Getter
	private ItemStack itemStack;
	
	private static final int ITEM_SIZE = 16;
	
	/**
	 * Can only move items to a new slot a single time
	 * before receiving a network response. */
	private boolean locked = false;
	
	public ContainerSlot(GuiComponent container, int slotIndex) {
		this.container = container;
		source = new Source(container, this);
		target = new Target(container, this);
		this.slotIndex = slotIndex;
		container.addCallback(GuiCallbackType.RIGHT_CLICK, () -> {
			if (itemStack == null) return;
			PlayScreen playScreen = Client.instance.getScreen();
			SelectOptionPopupMenu popupMenu = playScreen.getSelectOptionPopupMenu();
			List<Object> selectedItem = new ArrayList<>();
			selectedItem.add(this);
			popupMenu.open(selectedItem);
		});
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		container.clearChildren();
		if (itemStack != null) {
			addDisplayChild(itemStack);
		}
	}

	private void addDisplayChild(ItemStack itemStack) {
		GuiComponent displayItem = new GuiComponent(ITEM_SIZE * 2, ITEM_SIZE * 2);
		displayItem.setLocation(GuiLocation.CENTER);
		setTextureInfoForDisplayItem(itemStack, displayItem);
		container.addChild(displayItem);
	}
	
	private static void setTextureInfoForDisplayItem(ItemStack itemStack, GuiComponent displayItem) {
		int textureId = itemStack.getTextureId();
		displayItem.setTexture(ItemDefinition.getTexture(textureId));
		displayItem.setUv(ItemDefinition.getUvCoords(textureId));
	}
	
	public void dropItem() {
		if (itemStack == null) return;
		new DropItemPacketOut(slotIndex).send();
		locked = true;
	}
	
	public void lock() {
		locked = true;
	}

	public void unlock() {
		locked = false;
	}
}
