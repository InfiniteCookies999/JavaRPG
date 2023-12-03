package client.game;

import client.Client;
import client.game.entity.Entity;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiTextAlignment;

public class PlayerHealthBar extends GuiComponent {

	private GuiLabel slider;
	
	public PlayerHealthBar() {
		super(GuiLocation.TOP_LEFT, 100, 24);
		setColor(Color.rgb(0x121212));
		slider = new GuiLabel(0, this.height - 4);
		slider.setFont(Client.instance.getFont());
		slider.setTextAlignment(GuiTextAlignment.LEFT);
		slider.setColor(Color.RED);
		slider.setPosition(2, 2);
		addChild(slider);
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		super.render(batch, parentRX, parentRY);
		
		Entity player = Client.instance.player;
		
		int fullWidth = this.width - 4;
		slider.width = fullWidth * player.getHealth() / player.getMaxHealth();
		
		String text = String.format("%s / %s", player.getHealth(), player.getMaxHealth());
		slider.setText(text);
		slider.setTextOffsetX(
				this.width / 2 - slider.getFont().getLengthOfText(text) / 2);
		
	}
}
