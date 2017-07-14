package bgu.spl.app;

import bgu.spl.mics.Broadcast;

/**
 * A {@link Broadcast} message that is sent at every passed clock tick. This
 * message must contain the current tick (int).
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class TickBroadcast implements Broadcast {
	/**
	 * 
	 */
	private int tick;
	/**
	 * 
	 */
	private int lifeSpan;

	/**
	 * Creates a new {@link TickBroadcast} Broadcast message.
	 * 
	 * @param tick
	 *            the current global tick.
	 * @param lifeSpan
	 *            the duration ticks before all Threads (micro-services)
	 *            terminate.
	 */
	public TickBroadcast(int tick, int lifeSpan) {
		this.tick = tick;
		this.lifeSpan = lifeSpan;
	}

	/**
	 * @return the {@link #tick}.
	 */
	public int getTick() {
		return tick;
	}

	/**
	 * @return true if the current tick equals the {@link #lifeSpan}.
	 */
	public boolean isDead() {
		return lifeSpan == tick;
	}
}
