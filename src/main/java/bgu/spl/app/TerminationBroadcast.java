package bgu.spl.app;

import java.util.concurrent.Phaser;

import bgu.spl.mics.Broadcast;

/**
 * A {@link Broadcast} message that is sent to every Thread (micro-service) that
 * should terminate at the end of the program execution.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class TerminationBroadcast implements Broadcast {
	/**
	 * Phaser - A reusable synchronization barrier, similar in functionality to
	 * CyclicBarrier and CountDownLatch but supporting more flexible usage.
	 */
	private Phaser phaser;

	/**
	 * Creates a new {@link TerminationBroadcast} broadcast message.
	 * <p>
	 * The phaser makes a "meeting-point" for all the services and let them wait
	 * for each other, when all Threads (micro-services) arrive to that point,
	 * they GRACEFULLY terminate!.
	 * 
	 * @param phaser
	 *            Phaser - A reusable synchronization barrier, similar in
	 *            functionality to CyclicBarrier and CountDownLatch but
	 *            supporting more flexible usage.
	 */
	public TerminationBroadcast(Phaser phaser) {
		this.phaser = phaser;
	}

	/**
	 * @return the {@link #phaser}.
	 */
	public Phaser getPhaser() {
		return phaser;
	}
}
