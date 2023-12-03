package client.game;

import org.lwjgl.opengl.GL20;

import client.Client;
import client.Constants;
import client.Screen;
import client.net.ConnectionStatus;
import client.net.NetworkManager;
import client.net.out.LoginPacketOut;
import lombok.Setter;
import vork.App;
import vork.gfx.Color;
import vork.gfx.RenderContext;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiManager;
import vork.gfx.gui.GuiRenderMode;
import vork.gfx.gui.GuiTextAlignment;
import vork.gfx.gui.GuiTextField;
import vork.gfx.gui.RenderModeRepeats;
import vork.input.Keys;

public class LoginScreen implements Screen {

	private boolean attemptingToConnect = false;
	private boolean attemptingLogin = false;
	
	private static final Color WHITE_TEXT_COLOR = Color.rgb(0xcfcfcf);
	
	private GuiTextField usernameTextField;
	private GuiLabel connectionStatusLabel;
	private GuiLabel errorLabel;
	
	private NetworkManager networkManager;
	
	@Setter
	private LoginResponse loginResponse = LoginResponse.WAITING;
	
	private int connectionStatusTick;
	
	@Override
	public void init() {
		
		networkManager = Client.instance.getNetworkManager();
		
		GuiComponent loginPanel = new GuiComponent(GuiLocation.CENTER, 16*15, 16*8);
		loginPanel.setTexture(Resources.getUITexture("login_panel.png"));
		loginPanel.setRenderMode(GuiRenderMode.NINE_PATCH);
		loginPanel.setRenderModeRepeats(RenderModeRepeats.TOP);
		
		usernameTextField = new GuiTextField(Resources.getUITexture("login_panel_field.png"));
		usernameTextField.setLocation(GuiLocation.TOP_LEFT);
		usernameTextField.setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		usernameTextField.setRequiredPattern(Constants.USERNAME_REGEX_PATTERN);
		usernameTextField.setPosition(20, -20);
		usernameTextField.setFont(Client.instance.getFont());
		usernameTextField.width = 150;
		usernameTextField.setPlaceholderText("username");
		usernameTextField.setTextOffset(4, 2);
		usernameTextField.setTextColor(WHITE_TEXT_COLOR);
		usernameTextField.setPlaceholderColor(new Color(WHITE_TEXT_COLOR, 0.75f));
		usernameTextField.setCaretHeight(14);
		usernameTextField.setCaretOffsetY(2);
		usernameTextField.setMaxDisplayWindowTextLength(150 - 4*2);
		usernameTextField.setCharacterLimit(Constants.MAX_USERNAME_LENGTH);
		usernameTextField.giveFocus();
		usernameTextField.addCallback(GuiCallbackType.TEXT_CHANGED, () -> {
			errorLabel.setText("");
		});
		
		GuiLabel loginButton = new GuiLabel("login", GuiLocation.TOP_LEFT, 70, 20);
		loginButton.setTexture(Resources.getUITexture("panel_button.png"));
		loginButton.setPosition(20, -60);
		loginButton.setTextColor(WHITE_TEXT_COLOR);
		loginButton.setTextOffsetY(2);
		loginButton.setFont(Client.instance.getFont());
		loginButton.setHoverColor(Color.GREEN);
		loginButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
			attemptConnection();
		});
		
		connectionStatusLabel = new GuiLabel(0, 0);
		connectionStatusLabel.setFont(Client.instance.getFont());
		connectionStatusLabel.setTextAlignment(GuiTextAlignment.NONE);
		connectionStatusLabel.setLocation(GuiLocation.TOP_LEFT);
		connectionStatusLabel.setPosition(loginButton.width + 30, -77);
		connectionStatusLabel.setTextColor(WHITE_TEXT_COLOR.adjustBrightness(-20));
		
		errorLabel = new GuiLabel(0, 0);
		errorLabel.setFont(Client.instance.getFont());
		errorLabel.setTextAlignment(GuiTextAlignment.NONE);
		errorLabel.setLocation(GuiLocation.TOP_LEFT);
		errorLabel.setPosition(22, -55);
		errorLabel.setTextColor(Color.rgb(0xd92525));
		
		loginPanel.addChild(usernameTextField);
		loginPanel.addChild(loginButton);
		loginPanel.addChild(connectionStatusLabel);
		loginPanel.addChild(errorLabel);
		
		GuiManager.addRootComponent(loginPanel);
		
	}
	
	private void attemptConnection() {
		if (attemptingToConnect || attemptingLogin) return;
		connectionStatusTick = 0;
		
		if (usernameTextField.getText().isEmpty()) {
			errorLabel.setText("> Empty username");
			return;
		}
		
		errorLabel.setText("");
		attemptingToConnect = true;
		usernameTextField.setDisabled(true);
		
		loginResponse = LoginResponse.WAITING;
		networkManager.connectToServer(Constants.SERVER_ADDRESS, (short) 6234);
		
	}

	@Override
	public void tick(RenderContext context) {
		
		if (attemptingToConnect) {
			ConnectionStatus status = networkManager.checkConnectionStatus();
			if (status == ConnectionStatus.FAILED) {
				attemptingToConnect = false;
				setFailedToConnectState();
			} else if (status == ConnectionStatus.STILL_ATTEMPTING) {
				connectionStatusLabel.setText("attempting connection " +
						".".repeat(++connectionStatusTick/20 % 4));
			} else if (status == ConnectionStatus.SUCCESS) {
				attemptingToConnect = false;
				attemptingLogin = true;
				new LoginPacketOut(usernameTextField.getText()).send();
			}
		}
		
		if (attemptingLogin) {
			if (!networkManager.isConnected()) {
				// Lost connection while waiting on response.
				attemptingLogin = false;
				setFailedToConnectState();
			} else {
				connectionStatusLabel.setText("Logging in " + ".".repeat(++connectionStatusTick/20 % 4));
				
				switch (loginResponse) {
				case BANNED:
					break;
				case INVALID_CREDENTIALS:
					attemptingLogin = false;
					usernameTextField.setDisabled(false);
					errorLabel.setText("> Invalid credentials");
					connectionStatusLabel.clearText();
					break;
				case SEND_TO_CHARACTER_CREATION:
					Client.instance.setScreen(new CharacterCreationScreen());
					break;
				case SEND_TO_WORLD:
					break;
				case WAITING:
					// Still waiting on a response from the server
					break;
				}
			}
		}
		
		if (App.input.isKeyJustPressed(Keys.ENTER)) {
			attemptConnection();
		}
		
		App.gfx.clear(GL20.GL_COLOR_BUFFER_BIT);
		App.gfx.enableBlend(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		Client.instance.getFont().render(0, App.gfx.getWndHeight() - 16, "Fps: " + Client.instance.getFps());
		
		GuiManager.update();
		GuiManager.render(Client.instance.getBatch());
		
		context.render();
		
	}
	
	private void setFailedToConnectState() {
		usernameTextField.setDisabled(false);
		errorLabel.setText("> Failed to connect to server");
		connectionStatusLabel.clearText();
	}

	@Override
	public void dispose() {
		
	}
}
