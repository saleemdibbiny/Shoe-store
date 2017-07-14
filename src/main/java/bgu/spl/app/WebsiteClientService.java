package bgu.spl.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;

/**
 * This micro-service describes one client connected to the {@link Store}
 * web-site.
 * <p>
 * The client has:
 * <li>A list of {@link PurchaseSchedule}</li>.
 * <li>A wish list contains name of shoe types that the client will buy only
 * when there is a discount on them. discount.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class WebsiteClientService extends MicroService {
	/**
	 * List - contains purchases that the client needs to make, every purchase
	 * has a corresponding time tick to send the {@link PurchaseRequest} at.
	 */
	private List<PurchaseSchedule> purchaseSchedules = new ArrayList<PurchaseSchedule>();
	/**
	 * Set - The client wish list contains name of shoe types that the client
	 * will buy only when there is a discount on them
	 */
	private Set<String> wishList = new HashSet<String>();
	/**
	 * String - The client name.
	 */
	private String name;
	/**
	 * int - describes the current global clock system.
	 */
	private int currentTick = 0;
	/**
	 * AtomicInteger - the number of requests which sent by the client.
	 */
	private AtomicInteger requestsSent = new AtomicInteger();
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
	/**
	 * AtomicBoolean - check whether the micro-service should terminate.
	 */
	private AtomicBoolean isTerminated;
	private final Logger log;

	/**
	 * Creates a new {@link WebsiteClientService} micro-service.
	 * 
	 * @param name
	 *            String - The client name.
	 * @param countDown
	 *            CountDownLatch - let the current thread wait for the rest of
	 *            the threads to be ready.
	 * @param phaser
	 *            Phaser - A reusable synchronization barrier, similar in
	 *            functionality to CyclicBarrier and CountDownLatch but
	 *            supporting more flexible usage.
	 */	
	public WebsiteClientService(String name, CountDownLatch countDown, Phaser phaser) {
		super(name);
		this.wishList = new HashSet<String>();
		this.purchaseSchedules = new ArrayList<PurchaseSchedule>();
		this.name = name;
		this.countDown = countDown;
		this.phaser = phaser;
		this.isTerminated = new AtomicBoolean();
		this.log = Logger.getLogger(name);
	}

	/**
	 * @param shoeType
	 * @param tick
	 */
	public void addPurchaseSchedule(String shoeType, int tick) {
		this.purchaseSchedules.add(new PurchaseSchedule(shoeType, tick));
	}

	/**
	 * @param itemType
	 */
	public void addWishItem(String itemType) {
		this.wishList.add(itemType);
	}

	/**
	 * let the {@link WebsiteClientService} subscribe for the needed broadcasts:
	 * <li>{@link TerminationBroadcast}</li>
	 * <li>{@link TickBroadcast}</li> When the micro-service subsribe for the
	 * {@link TickBroadcast}, He iterate over all the {@link PurchaseShcedule}
	 * list and checks if the {@link #currentTick} equals to the shoe scheduled
	 * tick, if Yes, {@link PurchaseOrderRequest} will be sent by the client,
	 * does nothing otherwise.
	 * <p>
	 * In addition, the client will terminate in case that the
	 * {@link #purchaseSchedules} list and the {@link #wishList} are empty.
	 */
	private void subscribeTerminationAndTickBroadcasts() {
		this.subscribeBroadcast(TerminationBroadcast.class, broadcast -> {
			if (!this.isTerminated.getAndSet(true)) {
				broadcast.getPhaser().arriveAndAwaitAdvance();
				log.info("	MicroService " + this.name + " Terminated!");
				this.terminate();
			}
		});
		this.subscribeBroadcast(TickBroadcast.class, tick -> {
			currentTick = tick.getTick();
			int i = 0;
			ArrayList<PurchaseSchedule> list = new ArrayList<PurchaseSchedule>();
			for (PurchaseSchedule purchaseSchedule : purchaseSchedules) {
				if (purchaseSchedule.getTick() == currentTick) {
					String shoeType = purchaseSchedules.get(i).getShoeType();
					list.add(purchaseSchedule);
					requestsSent.incrementAndGet();
					log.info("	Client " + name + " requested to buy: " + shoeType + ".");
					this.sendRequest(new PurchaseOrderRequest("Store", name, shoeType, false, currentTick, 1), req -> {
						if (req != null)
							log.info("	Client " + name + " has Successfully bought: " + shoeType + ".");
						requestsSent.decrementAndGet();
						if (wishList.size() == 0 && purchaseSchedules.size() == 0 && requestsSent.intValue() == 0
								&& !this.isTerminated.getAndSet(true)) {
							this.phaser.arriveAndDeregister();
							this.terminate();
						}
					});
				}
				i++;
			}
			purchaseSchedules.removeAll(list);
		});
	}

	/**
	 * let the {@link WebsiteClientService} subscribe for the needed broadcasts:
	 * <li>{@link NewDiscountBroadcast}</li> The client subscribed for this
	 * {@link Broadcast} In order to get notified when a new discount is
	 * available.
	 */
	private void subscribeNewDiscountBroadcast() {
		this.subscribeBroadcast(NewDiscountBroadcast.class, cal -> {
			if (wishList != null)
				if (wishList.contains(cal.getShoeType())) {
					requestsSent.incrementAndGet();
					wishList.remove(cal.getShoeType());
					this.sendRequest(new PurchaseOrderRequest("Store", name, cal.getShoeType(), true, currentTick, 1),
							req -> {
						if (req != null) {
							log.info("	Client " + name + " has Successfully bought: " + cal.getShoeType()
									+ ", YESSS!!.");
						} else {
							wishList.add(cal.getShoeType());
						}
						requestsSent.decrementAndGet();
						if (wishList.size() == 0 && purchaseSchedules.size() == 0 && requestsSent.intValue() == 0
								&& !this.isTerminated.getAndSet(true)) {
							this.phaser.arriveAndDeregister();
							this.terminate();
						}
					});

				}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bgu.spl.mics.MicroService#initialize()
	 */
	@Override
	protected void initialize() {
		subscribeTerminationAndTickBroadcasts();
		subscribeNewDiscountBroadcast();
		log.info("Ready and waiting for other services");
		countDown.countDown();
	}

}