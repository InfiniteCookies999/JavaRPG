package vork.server.game.event;

import lombok.Setter;

public abstract class Event {
	/**
	 * Ticks until this event is run again.
	 */
	@Setter
	private int delayTicks;
	
	public void process() {
		if (delayTicks > 0) {
			--delayTicks;
			return;
		}
		run();
	}
	
	protected abstract void run();
	
	public abstract boolean isOver();
	
}
