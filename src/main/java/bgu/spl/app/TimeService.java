package bgu.spl.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;

/**
 * This micro-service is the global system timer (handles the clock ticks in the
 * system). it is responsible for counting how much clock ticks passed since the
 * beginning of its execution and notifying every other micro-services (thats
 * interested) about it using the {@link TickBroadcast}.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class TimeService extends MicroService {
	/**
	 * int - describes the current tick of the whole system.
	 * 
	 */
	private int tick = 0;
	/**
	 * int - number of milliseconds each clock tick takes
	 */
	private int speed;
	/**
	 * int - the number of ticks before termination.
	 */
	private int duration;
	/**
	 * singleton {@link Store}.
	 */
	private Store store;
	/**
	 * CountDownLatch - let the current thread wait for the rest of the threads
	 * to be ready.
	 */
	private CountDownLatch countDown;
	/**
	 * Phaser - A reusable synchronization barrier, similar in functionality to
	 * CyclicBarrier and CountDownLatch but supporting more flexible usage.
	 */
	private Phaser phaser;
	private final Logger log;

	/**
	 * Creates a new {@link TimeService} micro-service.
	 * 
	 * @param speed
	 *            int - number of milliseconds each clock tick takes
	 * @param duration
	 *            int - the number of ticks before termination.
	 * @param store
	 *            singleton {@link Store}.
	 * @param countDown
	 *            CountDownLatch - let the current thread wait for the rest of
	 *            the threads to be ready.
	 * @param phaser
	 *            A reusable synchronization barrier, similar in functionality
	 *            to CyclicBarrier and CountDownLatch but supporting more
	 *            flexible usage.
	 */
	public TimeService(int speed, int duration, Store store, CountDownLatch countDown, Phaser phaser) {

		super("TimeService");
		this.countDown = countDown;
		this.speed = speed;
		this.duration = duration;
		this.store = store;
		this.phaser = phaser;
		this.log = Logger.getLogger("timeService");
	}

	/**
	 * Let the micro-service wait the other micro-services (Threads) to be
	 * ready.
	 */
	private void waitOtherThreads() {
		try {
			countDown.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see bgu.spl.mics.MicroService#initialize()
	 */
	protected void initialize() {
		int delay = speed; // milliseconds
		TimeService thisService = this;
		Timer timer = new Timer();
		log.info("Ready and waiting for other services");
		waitOtherThreads();
		log.info("All services are ready to start");
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				tick++;
				if (tick > duration) {
					thisService.sendBroadcast(new TerminationBroadcast(phaser));
					phaser.arriveAndAwaitAdvance();
					log.info("	MicroService " + thisService.getName() + " Terminated!");
					thisService.terminate();
					timer.cancel();
					store.print();
				} else {
					log.info("Tick " + tick + ":");
					thisService.sendBroadcast(new TickBroadcast(tick, duration));
				}
			}
		}, 0, delay);
		Thread.currentThread().stop();
	}
}
