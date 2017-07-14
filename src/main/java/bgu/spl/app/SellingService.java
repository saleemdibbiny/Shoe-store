package bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;

/**
 * This micro-service handles {@link PurchaseOrderRequest}. When the Selling
 * Service receives a {@link PurchaseOrderRequest}, it handles it by trying to
 * take the required shoe from the storage. If it succedded it creates a
 * {@link Recipt}, file it in the {@link Store} and pass it to the client.
 * <p>
 * If there were no shoes on the requested type on stock, the selling service
 * will send {@link RestockRequest} to the {@link ManagementService}.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class SellingService extends MicroService {
	/**
	 * singleton {@link Store}.
	 */
	private Store store;
	/**
	 * int - describes the current global system clock.
	 */
	private int currentTick;
	/**
	 * String - the Selling service name.
	 */
	private String name;
	/**
	 * CountDownLatch - let the current thread wait for the rest of the threads
	 * to be ready.
	 */
	private CountDownLatch countDown;
	private final Logger log;

	/**
	 * Creates a new {@link SellingService} micro-service.
	 * 
	 * @param store
	 *            singleton {@link Store}.
	 * @param name
	 *            String - the Selling service name.
	 * @param countDown
	 *            CountDownLatch - let the current thread wait for the rest of
	 *            the threads to be ready.
	 */
	public SellingService(Store store, String name, CountDownLatch countDown) {
		super(name);
		this.store = store;
		this.currentTick = 0;
		this.name = name;
		this.countDown = countDown;
		this.log = Logger.getLogger(name);
	}

	/**
	 * let the {@link SellingService} subscribe for the needed broadcasts:
	 * <li>{@link TerminationBroadcast}</li>
	 * <li>{@link TickBroadcast}</li>
	 */
	private void subscribeTerminationAndTickBroadcasts() {
		this.subscribeBroadcast(TerminationBroadcast.class, broadcast -> {
			broadcast.getPhaser().arriveAndAwaitAdvance();
			log.info("	MicroService " + this.name + " Terminated!");
			this.terminate();
		});
		this.subscribeBroadcast(TickBroadcast.class, broadcast -> {
			this.currentTick = broadcast.getTick();
		});
	}

	/**
	 * let the {@link SellingService} subscribe for the needed requests:
	 * <li>{@link PurchaseOrderRequest}</li>. When this micro-service receives a
	 * {@link PurchaseOrderRequest}, it handles it by trying to take the
	 * required shoe from the storage, If it succeeded it creates a
	 * {@link Receipt}, file it in the {@link Store} and pass it to the client,
	 * If there were no shoes on the requested type on stock, the selling
	 * service will send {@link RestockRequest}, if the request completed with
	 * the value “false” the SellingService will complete the
	 * {@link PurchaseOrderRequest} with the value of “null”, If the client
	 * indicates in the order that he wish to get this shoe only on discount and
	 * no more discounted shoes are left then it will complete the client
	 * request with "null" result.
	 */
	private void subscribePurchaseOrderRequests() {
		this.subscribeRequest(PurchaseOrderRequest.class, req -> {
			boolean shoeFound = false;
			boolean isDiscounted = true;
			BuyResult result = store.take(req.getShoeType(), req.isOnlyDiscount());
			log.info(
					"	" + name + " recieved purchase request of " + req.getShoeType() + "\n		Result: " + result);
			switch (result) {
			case NOT_IN_STOCK:
				this.sendRequest(new RestockRequest(req.getShoeType()), restockReq -> {
					if (restockReq.booleanValue()) {
						store.file(new Receipt(req.getSeller(), req.getCustomer(), req.getShoeType(), false,
								currentTick, req.getRequestTick(), 1));
						this.complete(req, new Receipt(req.getSeller(), req.getCustomer(), req.getShoeType(), false,
								currentTick, req.getRequestTick(), 1));
					}
				});
				break;
			case NOT_ON_DISCOUNT:
				this.complete(req, null);
				break;
			case REGULAR_PRICE:
				isDiscounted = false;
			case DISCOUNTED_PRICE:
				shoeFound = true;
				break;
			}
			if (shoeFound) {
				store.file(new Receipt(req.getSeller(), req.getCustomer(), req.getShoeType(), false, currentTick,
						req.getRequestTick(), 1));
				this.complete(req, new Receipt(req.getSeller(), req.getCustomer(), req.getShoeType(), isDiscounted,
						currentTick, req.getRequestTick(), 1));
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
		subscribePurchaseOrderRequests();
		log.info("Ready and waiting for other services");
		countDown.countDown();

	}

}
