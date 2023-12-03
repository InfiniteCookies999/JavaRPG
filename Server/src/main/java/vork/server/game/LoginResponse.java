package vork.server.game;

public enum LoginResponse {
	WAITING,
	SEND_TO_CHARACTER_CREATION,
	SEND_TO_WORLD,
	INVALID_CREDENTIALS,
	BANNED,
}
