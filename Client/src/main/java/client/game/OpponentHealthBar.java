package client.game;

import client.Client;
import client.game.entity.Entity;
import lombok.Setter;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.VkFont;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiTextAlignment;

public class OpponentHealthBar extends GuiLabel {

	private GuiLabel slider;
	
	@Setter
	private int entityId = -1;
	
	public OpponentHealthBar(PlayerHealthBar playerHealthBar) {
		super(GuiLocation.TOP_LEFT, playerHealthBar.width, 40);
		VkFont font = Client.instance.getFont();
		setFont(font);
		setTextAlignment(GuiTextAlignment.NONE);
		setPosition(0, -playerHealthBar.height);
		setHidden(true);
		setColor(Color.rgb(0x121212));
		setTextOffset(4, this.height - font.getGlyphHeight() + 2);
		
		slider = new GuiLabel(0, playerHealthBar.height);
		slider.setFont(font);
		slider.setTextAlignment(GuiTextAlignment.LEFT);
		slider.setColor(new Color(Color.RED, 0.75f));
		slider.setPosition(2, 2);
		
		addChild(slider);
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		super.render(batch, parentRX, parentRY);
		
		Entity entity = Client.instance.getEntity(entityId);
		if (entity != null && !entity.isDead()) {
			setHidden(false);
			setText(entity.getName());
			
			int fullWidth = this.width - 4;
			slider.width = fullWidth * entity.getHealth() / entity.getMaxHealth();
			
			String text = String.format("%s / %s", entity.getHealth(), entity.getMaxHealth());
			slider.setText(text);
			slider.setTextOffsetX(
					this.width / 2 - slider.getFont().getLengthOfText(text) / 2);	
		} else {
			setHidden(true);
			entityId = -1;
		}
	}
}
