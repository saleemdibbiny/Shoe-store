package bgu.spl.app;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;

/**
 * This micro-service describes a shoe factory that manufacture shoes for the
 * store. This micro-service handles the {@link ManufacturingOrderRequest} it
 * takes it exactly 1 tick to manufacture a single shoe,
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class ShoeFactoryService extends MicroService {
	/**
	 * int - describes the current tick of the global clock system.
	 */
	private int currentTick;
	/**
	 * ArrayList - contains all the {@link ManufacturingOrderRequest} schedules.
	 */
	ArrayList<ManufacturingOrderRequest> requestsSchedule = new ArrayList<ManufacturingOrderRequest>();
	/**
	 * String - the name of the factory.
	 */
	private String name;
	/**
	 * CountDownLatch - let the current thread wait for the rest of the threads
	 * to be ready.
	 */
	private CountDownLatch countDown;
	private final Logger log;

	/**
	 * Creates a new {@link ShoeFactoryService} micro-service.
	 * 
	 * @param name
	 *            String - the name of the factory.
	 * @param countDown
	 *            CountDownLatch - let the current thread wait for the rest of
	 *            the threads to be ready.
	 */
	public ShoeFactoryService(String name, CountDownLatch countDown) {
		super(name);
		currentTick = 0;
		this.countDown = countDown;
		this.name = name;
		this.log = Logger.getLogger(name);
	}

	/**
	 * let the {@link ShoeFactoryService} subscribe for the needed broadcasts:
	 * <li>{@link TerminationBroadcast}</li>
	 * <li>{@link TickBroadcast}</li>
	 */
	private void subscribeBroadcasts() {
		this.subscribeBroadcast(TerminationBroadcast.class, broadcast -> {
			broadcast.getPhaser().arriveAndAwaitAdvance();
			log.info("	MicroService " + this.name + " Terminated!");
			this.terminate();
		});
		this.subscribeBroadcast(TickBroadcast.class, broadcast -> {
			this.currentTick = broadcast.getTick();
			if (requestsSchedule.size() > 0) {
				ManufacturingOrderRequest r = requestsSchedule.get(0);
				r.decreaseAmount();
				if (r.getAmount() == 0) {
					this.complete(r, new Receipt(this.name, "store", r.getShoeType(), false, this.currentTick,
							r.getRequestTick(), r.getRequestedAmount()));
					requestsSchedule.remove(0);
				}
			}
		});
	}

	/**
	 * let the {@link ShoeFactoryService} subscribe for the needed requests:
	 * <li>{@link ManufacturingOrderRequest}</li>
	 */
	private void subscribeRequests() {
		this.subscribeRequest(ManufacturingOrderRequest.class, request -> {
			
			log.info("	"+this.name+" recieved a request of manufacturing "+request.getAmount() + " x "+request.getShoeType());
			requestsSchedule.add(request);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bgu.spl.mics.MicroService#initialize()
	 */
	protected void initialize() {
		subscribeBroadcasts();
		subscribeRequests();
		log.info("Ready and waiting for other services");
		countDown.countDown();

	}

}