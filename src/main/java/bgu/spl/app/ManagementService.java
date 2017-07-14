package bgu.spl.app;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.mics.MicroService;

/**
 * This micro-service can add discount to shoe in the store and send
 * {@link NewDiscountBroadcast} to notify clients about them.
 * <p>
 * In addition, this micro-service handles the {@link RestockRequest} that is
 * being sent by the {@link SellingService}.
 * <p>
 * {@link ManagementService} can send {@link ManufacturingOrderRequest} to to
 * the {@link ShoeFactoryService} in order to load new storage to the
 * {@link Store}.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class ManagementService extends MicroService {

	/**
	 * ArrayList - which contains all the {@link DiscountSchedule}.
	 */
	private ArrayList<DiscountSchedule> discountSchedules;
	/**
	 * Map - shoe orders that have not been completed.
	 */
	private Map<String, ArrayList<ManufacturingOrderInfo>> orders;
	/**
	 * singleton {@link Store}.
	 */
	private Store store;
	/**
	 * int - current tick of the global clock.
	 */
	private int currentTick;
	/**
	 * CountDownLatch - let the current thread wait for the rest of the threads
	 * to be ready.
	 */
	private CountDownLatch countDown;
	private final Logger log;

	/**
	 * Creates a micro-service {@link ManagementService} for the store.
	 * 
	 * @param store
	 *            singleton {@Store}.
	 * @param countDown
	 *            let the current thread wait for the rest of the threads to be
	 *            ready.
	 */
	public ManagementService(Store store, CountDownLatch countDown) {
		super("manager");
		this.countDown = countDown;
		this.discountSchedules = new ArrayList<DiscountSchedule>();
		this.store = store;
		this.currentTick = 0;
		this.orders = new ConcurrentHashMap<String, ArrayList<ManufacturingOrderInfo>>();
		this.log = Logger.getLogger("manager");
	}

	/**
	 * Add a {@link DiscountSchedule} for a specific shoe with specific amount
	 * at a specific tick.
	 * 
	 * @param shoeType
	 *            the type of shoe to add discount to.
	 * @param tick
	 *            the tick number to send the add the discount at.
	 * @param amount
	 *            the amount of items to put on discount
	 */
	public void addDiscountSchedules(String shoeType, int tick, int amount) {
		this.discountSchedules.add(new DiscountSchedule(shoeType, tick, amount));
	}

	/**
	 * let the {@link ManagementService} subscribe for the needed broadcasts:
	 * <li>{@link TerminationBroadcast}</li>
	 * <li>{@link TickBroadcast}</li>
	 */
	private void subscribeBroadcasts() {
		this.subscribeBroadcast(TerminationBroadcast.class, broadcast -> {
			broadcast.getPhaser().arriveAndAwaitAdvance();
			log.info("	MicroService manager Terminated!");
			this.terminate();
		});

		this.subscribeBroadcast(TickBroadcast.class, broadcast -> {
			this.currentTick = broadcast.getTick();
			for (int i = 0; i < discountSchedules.size(); i++) {
				DiscountSchedule ds = discountSchedules.get(i);
				if (ds.getTick() == broadcast.getTick()) {
					if (store.isInStock(ds.getShoeType())) {
						log.info("	New discount broadcast of shoe: "+ds.getShoeType());
						this.sendBroadcast(new NewDiscountBroadcast(ds.getShoeType(), ds.getAmount()));
						store.addDiscount(ds.getShoeType(), ds.getAmount());
						discountSchedules.remove(ds);
					}
				}
			}
		});
	}

	/**
	 * let the {@link ManagementService} subscribe for the needed requests:
	 * <li>{@link RestockRequest}</li>.
	 */
	private void subscribeRequests() {
		this.subscribeRequest(RestockRequest.class, request -> {
			String str ="	" + this.getName() + " recieved restock request of shoe: " + request.getShoeType();
			boolean found = false;
			int amount = this.currentTick % 5 + 1;
			String shoeType = request.getShoeType();
			boolean isOrdered = isAlreadyOrdered(shoeType);
			if (!isOrdered) {
				log.info(str + "\n		Ordering " + amount + " of: '" + shoeType + "'");
				orders.get(shoeType).add(new ManufacturingOrderInfo(shoeType, amount, 1));
				found = this.sendRequest(new ManufacturingOrderRequest(request.getShoeType(), amount, this.currentTick),
						result -> onComplete(request, result));
			} else {
				log.info(str + "\n		Shoe was already ordered, waiting for it's completion.");
				ArrayList<ManufacturingOrderInfo> list = orders.get(shoeType);
				list.get(list.size() - 1).addRequest(request);
				found = true;
			}

			if (!found)
				this.complete(request, false);

		});
	}

	/**
	 * Checks if the shoe of type {@code shoeType} is already ordered.
	 * 
	 * @param shoeType
	 *            the shoe which needed to be checked if it is already ordered
	 *            or not.
	 * @return true if the shoe of type {@code shoeType} is already ordered and
	 *         false otherwise.
	 */
	private boolean isAlreadyOrdered(String shoeType) {
		boolean isOrdered = true;
		if (!orders.containsKey(shoeType)) {
			isOrdered = false;
			orders.put(shoeType, new ArrayList<ManufacturingOrderInfo>());
		} else {
			ArrayList<ManufacturingOrderInfo> list = orders.get(shoeType);
			if (list.isEmpty() || list.get(list.size() - 1).isAllReserved())
				isOrdered = false;
		}
		return isOrdered;
	}

	/**
	 * Adding a {@code result} {@link Receipt} to the {@link Store} receipts
	 * file and removing the first {@link ManufacturingOrderInfo} order from the
	 * {@link #orders} and completing all the requests of that order.
	 * 
	 * @param request
	 *            a {@link RestckRequest} which the manager received from the
	 *            {@link SellingService}.
	 * @param result
	 *            a {@link Receipt}.
	 */
	private void onComplete(RestockRequest request, Receipt result) {
		log.info("	" + this.getName() + " recieved " + result.getAmountSold() + " x "
				+ result.getShoeType() + " from " + result.getSeller());
		store.file(new Receipt(result.getSeller(), "store", result.getShoeType(), false, this.currentTick,
				result.getRequestTick(), result.getAmountSold()));
		ManufacturingOrderInfo order = orders.get(result.getShoeType()).remove(0);
		ArrayList<RestockRequest> requests = order.getRequests();
		for (RestockRequest r : requests) {
			this.complete(r, true);
		}

		store.addShoeIfNotInStorage(result.getShoeType(), 0, 0);
		store.add(result.getShoeType(), result.getAmountSold() - order.getAmountReserved());
		this.complete(request, true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bgu.spl.mics.MicroService#initialize()
	 */
	@Override
	protected void initialize() {
		this.subscribeBroadcasts();
		this.subscribeRequests();
		log.info("Ready and waiting for other services");
		countDown.countDown();

	}
}
