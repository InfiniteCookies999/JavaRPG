package client.editor;

import java.util.ArrayList;
import java.util.List;

import client.Constants;
import client.game.Resources;
import client.game.world.RenderLayer;
import client.game.world.Tile;
import client.game.world.World;
import lombok.Getter;
import vork.App;
import vork.gfx.Color;
import vork.gfx.PaddedTexture;
import vork.gfx.SpriteBatch;
import vork.gfx.UvCoords;
import vork.gfx.VkFont;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiCheckBox;
import vork.gfx.gui.GuiChildDisplayMode;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiHorizontalScrollBar;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiTextAlignment;
import vork.gfx.gui.GuiVerticalScrollBar;
import vork.input.Keys;

public class RightPanel extends GuiComponent {
	
	@Getter
	private int selectedLayerIdx = 0;
	
	@Getter
	private int selectedTileSheetIdx = 0;

	private GuiLabel tileSheetDropdownButton;
	private GuiComponent layerSelector;
	private GuiComponent tileSelectorArea;
	private GuiVerticalScrollBar tileSelVerticalScrollBar;
	private GuiHorizontalScrollBar tileSelHorizontalScrollBar;
	
	private List<String> tileSheetNames;
	
	@Getter
	private int tileSelStartX, tileSelStartY;
	@Getter
	private int tileSelEndX, tileSelEndY;
	private int tileSelPivotX, tileSelPivotY;
	
	private class TileSheetDisplayInfoSave {
		private int tileSelStartX, tileSelStartY;
		private int tileSelEndX, tileSelEndY;
		private int tileSelPivotX, tileSelPivotY;
		float verticalScrollPercent = 0.0f;
		float horizontalScrollPercent = 0.0f;
	}
	
	private TileSheetDisplayInfoSave[] tileSheetDisplaySaveInfoList;
	
	public RightPanel(List<String> tileSheetNames) {
		super(GuiLocation.RIGHT, Editor.RIGHT_PANEL_WIDTH, 0);
		this.tileSheetNames = tileSheetNames;
		setColor(EditorColors.UI_PRIMARY_COLOR);
		
		tileSheetDisplaySaveInfoList = new TileSheetDisplayInfoSave[Resources.getNumberOfTileSheets()];
		for (int i = 0; i < Resources.getNumberOfTileSheets(); i++) {
			tileSheetDisplaySaveInfoList[i] = new TileSheetDisplayInfoSave();
		}
		
		// The line that seperates the top panel from this right panel
		//
		GuiComponent topLineSeperator = new GuiComponent(width, 1);
		topLineSeperator.setColor(EditorColors.UI_WHITE_COLOR);
		topLineSeperator.setLocation(GuiLocation.TOP);
		addChild(topLineSeperator);
		
		VkFont font = Editor.instance.getFont();
		
		// The layer panel for choosing which layer to work with
		//
		GuiLabel layersTitle = new GuiLabel("Layers", 0, font.getGlyphHeight());
		layersTitle.setFont(font);
		layersTitle.setLocation(GuiLocation.TOP);
		layersTitle.setTextAlignment(GuiTextAlignment.NONE);
		layersTitle.setTextColor(EditorColors.UI_WHITE_COLOR);
		layersTitle.setPosition(16, -6);
		addChild(layersTitle);
		
		GuiComponent layersSelectorDiv = new GuiComponent(width - 10, 180);
		layersSelectorDiv.setLocation(GuiLocation.TOP_RIGHT);
		layersSelectorDiv.setPosition(0, -30);
		layersSelectorDiv.setColor(Color.BLACK);
		
		layerSelector = new GuiComponent(layersSelectorDiv.width - 1, layersSelectorDiv.height - 1);
		layerSelector.setPosition(1, 1);
		layerSelector.setColor(EditorColors.UI_WHITE_COLOR);
		layerSelector.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN);
		RenderLayer[] renderLayers = RenderLayer.values();
		for (int i = 0; i < renderLayers.length; i++) {
			GuiLabel layersButton = new GuiLabel(renderLayers[i].toString().toLowerCase() + " - layer", layerSelector.width - 20, 20);
			layersButton.setFont(font);
			layersButton.setColor(i % 2 == 0 ? EditorColors.UI_WHITE_COLOR : Color.rgb(0x8db0c2));
			layersButton.setTextColor(EditorColors.UI_OFFSET_TEXT_COLOR);
			layersButton.setTextAlignment(GuiTextAlignment.LEFT);
			layersButton.setTextOffsetX(30);
			layersButton.setHoverColor(EditorColors.UI_HOVER_COLOR);
			final int layerIndex = i;
			layersButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
				selectedLayerIdx = layerIndex;
			});
			
			GuiComponent bucketIcon = new GuiComponent(Resources.getUITexture("editor_layer_paint_bucket.png"));
			bucketIcon.setLocation(GuiLocation.CENTER_VERTICAL);
			bucketIcon.x = 4;
			layersButton.addChild(bucketIcon);
			
			GuiCheckBox eyeIcon = new GuiCheckBox(20, 20);
			eyeIcon.setTexture(Resources.getUITexture("edit_layer_eye_closed.png"));
			eyeIcon.setCheckedTexture(Resources.getUITexture("edit_layer_eye_open.png"));
			eyeIcon.setChecked(true);
			eyeIcon.setLocation(GuiLocation.RIGHT);
			eyeIcon.x = 20;
			eyeIcon.addCallback(GuiCallbackType.CHECKBOX_CHANGE, () -> {
				World world = Editor.instance.getWorld();
				world.visibleLayers[layerIndex] = eyeIcon.isChecked();
			});
			layersButton.addChild(eyeIcon);
			
			layerSelector.addChild(layersButton);
		}
		layersSelectorDiv.addChild(layerSelector);
		addChild(layersSelectorDiv);
		
		// The tile selector
		//
		
		final int SCROLL_BAR_THIN = 10;
		final int SELECTOR_SIZE = layersSelectorDiv.width;
		final int INDENT = width - SELECTOR_SIZE + 10;
		final int TILE_SHEET_DROPDOWN_BTN_HEIGHT = 20;
		
		GuiLabel tileSelectorTitle = new GuiLabel("Tile Sheet Selection", 100, 20);
		tileSelectorTitle.setFont(font);
		tileSelectorTitle.setColor(Color.FULL_TRANSPARENT);
		tileSelectorTitle.setTextColor(EditorColors.UI_WHITE_COLOR);
		tileSelectorTitle.setTextAlignment(GuiTextAlignment.NONE);
		tileSelectorTitle.setPosition(INDENT, SELECTOR_SIZE + TILE_SHEET_DROPDOWN_BTN_HEIGHT + 8);
		addChild(tileSelectorTitle);
		
		tileSheetDropdownButton = new GuiLabel(width - INDENT*2, TILE_SHEET_DROPDOWN_BTN_HEIGHT);
		tileSheetDropdownButton.setFont(font);
		tileSheetDropdownButton.setPosition(INDENT, SELECTOR_SIZE + 4);
		tileSheetDropdownButton.setTextColor(EditorColors.UI_OFFSET_TEXT_COLOR);
		tileSheetDropdownButton.setTextAlignment(GuiTextAlignment.LEFT);
		tileSheetDropdownButton.setTextOffsetX(SCROLL_BAR_THIN);
		tileSheetDropdownButton.setColor(EditorColors.UI_WHITE_COLOR);
		tileSheetDropdownButton.setAllowClickedThroughChild(true);

		GuiComponent tileSheetDropdownSelGroupingDiv = new GuiComponent(tileSheetDropdownButton.width, TILE_SHEET_DROPDOWN_BTN_HEIGHT*3 + 2);
		tileSheetDropdownSelGroupingDiv.setColor(Color.GRAY);
		tileSheetDropdownSelGroupingDiv.y = -tileSheetDropdownSelGroupingDiv.height;
		tileSheetDropdownSelGroupingDiv.setHidden(true);
		
		tileSheetDropdownButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
			tileSheetDropdownSelGroupingDiv.setHidden(!tileSheetDropdownSelGroupingDiv.isHidden());
		});
		
		GuiComponent tileSheetDropdownSelGrouping = new GuiComponent(tileSheetDropdownButton.width - 2, tileSheetDropdownSelGroupingDiv.height-2);
		tileSheetDropdownSelGrouping.setColor(Color.RED);
		tileSheetDropdownSelGrouping.setPosition(1, 1);
		tileSheetDropdownSelGroupingDiv.addChild(tileSheetDropdownSelGrouping);
		
		GuiComponent tileSheetDropdownSelArea = new GuiComponent(tileSheetDropdownSelGrouping.width, TILE_SHEET_DROPDOWN_BTN_HEIGHT*Resources.getNumberOfTileSheets());
		tileSheetDropdownSelArea.setColor(Color.GREEN);
		tileSheetDropdownSelArea.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN);
		
		GuiVerticalScrollBar.DragBar dropdownDragBar = new GuiVerticalScrollBar.DragBar(0, 0);
		dropdownDragBar.setColor(Color.GRAY);
		dropdownDragBar.setHoverColor(Color.GRAY.adjustBrightness(-20));
		dropdownDragBar.setOffset(1, 1);
		
		GuiVerticalScrollBar dropdownScrollBar = new GuiVerticalScrollBar(SCROLL_BAR_THIN, tileSheetDropdownSelGrouping.height, dropdownDragBar);
		dropdownScrollBar.setColor(EditorColors.UI_WHITE_COLOR);
		dropdownScrollBar.setViewArea(tileSheetDropdownSelArea);
		
		for (int idx = 0; idx < Resources.getNumberOfTileSheets(); idx++) {
			GuiLabel dropdownButton = new GuiLabel(tileSheetNames.get(idx), tileSheetDropdownSelArea.width - dropdownScrollBar.width, TILE_SHEET_DROPDOWN_BTN_HEIGHT);
			dropdownButton.setFont(font);
			dropdownButton.setColor(EditorColors.UI_WHITE_COLOR);
			dropdownButton.setTextColor(EditorColors.UI_OFFSET_TEXT_COLOR);
			GuiComponent seperator = new GuiComponent(dropdownButton.width, 1);
			seperator.setColor(Color.GRAY);
			seperator.setLocation(GuiLocation.TOP);
			dropdownButton.addChild(seperator);
			dropdownButton.x = dropdownScrollBar.width;
			dropdownButton.setHoverColor(EditorColors.UI_HOVER_COLOR);
			final int tileSheetIdx = idx;
			dropdownButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
				setTileSelectorTileSheet(tileSheetIdx);
				tileSheetDropdownSelGroupingDiv.setHidden(true);
			});
			tileSheetDropdownSelArea.addChild(dropdownButton);
		}
		
		tileSheetDropdownSelGrouping.addChild(tileSheetDropdownSelArea);
		tileSheetDropdownSelGrouping.addChild(dropdownScrollBar);
		
		GuiComponent dropdownArrow = new GuiComponent(Resources.getUITexture("editor_dropdown_arrow.png"));
		dropdownArrow.setLocation(GuiLocation.RIGHT);
		tileSheetDropdownButton.addChild(dropdownArrow);
		tileSheetDropdownButton.addChild(tileSheetDropdownSelGroupingDiv);
		
		
		// Creating the tile selector
		//
		
		GuiComponent tileSelectorGroupingDiv = new GuiComponent(SELECTOR_SIZE, SELECTOR_SIZE);
		tileSelectorGroupingDiv.setColor(Color.BLACK);
		tileSelectorGroupingDiv.setLocation(GuiLocation.RIGHT);
		
		GuiComponent tileSelectorGrouping = new GuiComponent(tileSelectorGroupingDiv.width - SCROLL_BAR_THIN - 1, tileSelectorGroupingDiv.width - SCROLL_BAR_THIN - 1);
		tileSelectorGrouping.setColor(Color.GRAY);
		tileSelectorGrouping.setLocation(GuiLocation.TOP_RIGHT);
		tileSelectorGrouping.y = -1;
		
		GuiVerticalScrollBar.DragBar tileSelVerticalDragBar = new GuiVerticalScrollBar.DragBar(0, 0);
		tileSelVerticalDragBar.setColor(Color.GRAY);
		tileSelVerticalDragBar.setHoverColor(Color.GRAY.adjustBrightness(-20));
		tileSelVerticalDragBar.setOffset(1, 1);
		tileSelVerticalScrollBar = new GuiVerticalScrollBar(SCROLL_BAR_THIN, tileSelectorGrouping.height, tileSelVerticalDragBar);
		tileSelVerticalScrollBar.setColor(EditorColors.UI_WHITE_COLOR);
		tileSelVerticalScrollBar.x = -tileSelVerticalScrollBar.width;
		
		GuiHorizontalScrollBar.DragBar tileSelHorizontalDragBar = new GuiHorizontalScrollBar.DragBar(0, 0);
		tileSelHorizontalDragBar.setColor(Color.GRAY);
		tileSelHorizontalDragBar.setHoverColor(Color.GRAY.adjustBrightness(-20));
		tileSelHorizontalDragBar.setOffset(1, 1);
		tileSelHorizontalScrollBar = new GuiHorizontalScrollBar(tileSelectorGrouping.width, SCROLL_BAR_THIN, tileSelHorizontalDragBar);
		tileSelHorizontalScrollBar.setColor(EditorColors.UI_WHITE_COLOR);
		tileSelHorizontalScrollBar.y = -tileSelHorizontalScrollBar.height;
		
		tileSelectorArea = new GuiComponent(0, 0);
		tileSelectorArea.setColor(Color.GRAY);
		
		tileSelVerticalScrollBar.setViewArea(tileSelectorArea);
		tileSelHorizontalScrollBar.setViewArea(tileSelectorArea);
		
		tileSelectorGrouping.addChild(tileSelectorArea);
		tileSelectorGrouping.addChild(tileSelVerticalScrollBar);
		tileSelectorGrouping.addChild(tileSelHorizontalScrollBar);
		
		tileSelectorGroupingDiv.addChild(tileSelectorGrouping);
		addChild(tileSelectorGroupingDiv);
		addChild(tileSheetDropdownButton);
		
		setTileSelectorTileSheet(0);
		
	}
	
	private void setTileSelectorTileSheet(int tileSheetIdx) {
		// Saving current information.
		
		TileSheetDisplayInfoSave currentSaveInfo = tileSheetDisplaySaveInfoList[selectedTileSheetIdx];
		currentSaveInfo.tileSelStartX = tileSelStartX;
		currentSaveInfo.tileSelStartY = tileSelStartY;
		currentSaveInfo.tileSelEndX = tileSelEndX;
		currentSaveInfo.tileSelEndY = tileSelEndY;
		currentSaveInfo.tileSelPivotX = tileSelPivotX;
		currentSaveInfo.tileSelPivotY = tileSelPivotY;
		currentSaveInfo.verticalScrollPercent = tileSelVerticalScrollBar.getScrollPercent();
		currentSaveInfo.horizontalScrollPercent = tileSelHorizontalScrollBar.getScrollPercent();
		
		// Applying the save information.
		
		TileSheetDisplayInfoSave existingSaveInfo = tileSheetDisplaySaveInfoList[tileSheetIdx];
		tileSelStartX = existingSaveInfo.tileSelStartX;
		tileSelStartY = existingSaveInfo.tileSelStartY;
		tileSelEndX = existingSaveInfo.tileSelEndX;
		tileSelEndY = existingSaveInfo.tileSelEndY;
		tileSelPivotX = existingSaveInfo.tileSelPivotX;
		tileSelPivotY = existingSaveInfo.tileSelPivotY;
		tileSelVerticalScrollBar.setScrollPercent(existingSaveInfo.verticalScrollPercent);
		tileSelHorizontalScrollBar.setScrollPercent(existingSaveInfo.horizontalScrollPercent);
		
		selectedTileSheetIdx = tileSheetIdx;
		
		tileSheetDropdownButton.setText(tileSheetNames.get(tileSheetIdx));
		
		PaddedTexture tileSheet = Resources.getTileSheet(tileSheetIdx);
		
		int numXTiles = tileSheet.originalWidth / Constants.TILE_SIZE;
		int numYTiles = tileSheet.originalHeight / Constants.TILE_SIZE;
		
		tileSelectorArea.clearChildren();
		final int tileScale = 2;
		final int tileSize = Constants.TILE_SIZE * tileScale;
		tileSelectorArea.setSize(numXTiles * tileSize, numYTiles * tileSize);
		
		for (int y = 0; y < numYTiles; y++) {
			for (int x = 0; x < numXTiles; x++) {
				GuiComponent tileButton = new GuiComponent(tileSize, tileSize);
				tileButton.setPosition(x*tileSize, y*tileSize);
				tileButton.setTexture(tileSheet);
				
				UvCoords uv = UvCoords.createFromPixelSize(
						(Constants.TILE_SIZE + 2) * x + 1,
						(Constants.TILE_SIZE + 2) * y + 1,
						Constants.TILE_SIZE,
						Constants.TILE_SIZE,
						tileSheet);
				tileButton.setUv(uv);
				
				GuiComponent overlay = new GuiComponent(tileSize, tileSize);
				overlay.setColor(new Color(Color.WHITE, 0.5f));
				overlay.setHidden(true);
				tileButton.addChild(overlay);
				tileButton.setAllowClickedThroughChild(true);
				final int selX = x;
				final int selY = y;
				tileButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
					if (App.input.isAnyKeyPressed(Keys.LEFT_SHIFT, Keys.RIGHT_SHIFT)) {
						tileSelStartX = tileSelPivotX < selX ? tileSelPivotX : selX;
						tileSelStartY = tileSelPivotY < selY ? tileSelPivotY : selY;
						tileSelEndX = tileSelPivotX > selX ? tileSelPivotX : selX;
						tileSelEndY = tileSelPivotY > selY ? tileSelPivotY : selY;
					} else {
						tileSelPivotX = selX;
						tileSelPivotY = selY;
						tileSelStartX = selX;
						tileSelStartY = selY;
						tileSelEndX   = selX;
						tileSelEndY   = selY;	
					}
				});
				
				tileSelectorArea.addChild(tileButton);
			}
		}
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		// Updating the bucket icons
		for (int i = 0; i < RenderLayer.values().length; i++) {
			GuiComponent bucketIcon = layerSelector.getChild(i).getChild(0);
			bucketIcon.setHidden(i != selectedLayerIdx);
		}
		
		// Showing overlays for which tiles are selected
		PaddedTexture tileSheet = Resources.getTileSheet(selectedTileSheetIdx);
		int numXTiles = tileSheet.originalWidth / Constants.TILE_SIZE;
		int numYTiles = tileSheet.originalHeight / Constants.TILE_SIZE;
		for (int y = 0; y < numYTiles; y++) {
			for (int x = 0; x < numXTiles; x++) {
				boolean showOverlay = x >= tileSelStartX && x <= tileSelEndX &&
						              y >= tileSelStartY && y <= tileSelEndY;
				GuiComponent tileButton = tileSelectorArea.getChild(y*numXTiles + x);
				tileButton.getChild(0).setHidden(!showOverlay);
			}
		}
		
		super.render(batch, parentRX, parentRY);
	}
	
	public List<Tile.LayerRenderData> getSelectedRenderData(){
		List<Tile.LayerRenderData> selected = new ArrayList<>();
		
		return selected;
	}
}
