package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * A request that is sent when a client {@link Store} wish to buy a shoe.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class PurchaseOrderRequest implements Request<Receipt> {
	/**
	 * String - the shoe which the client wish to buy from the {@link Store}.
	 */
	private String shoeType;
	/**
	 * boolean - indicates if the client will buy the shoe if it's only on
	 * discount or not respectively.
	 */
	private boolean onlyDiscount;
	/**
	 * String - the {@link SellingService} name.
	 */
	private String seller;
	/**
	 * String - the {@link WebsiteClientService} name.
	 */
	private String customer;
	/**
	 * int - the tick when the {@link PurchaseOrderRequest} occurred.
	 */
	private int requestTick;
	/**
	 * int - the amount of shoes of type {@link #shoeType} which the client
	 * willing to buy.
	 */
	private int amount;

	/**
	 * Creates a new {@link PurchaseOrderRequest}.
	 * 
	 * @param seller
	 *            String - the {@link SellingService} name.
	 * @param customer
	 *            String - the {@link WebsiteClientService} name.
	 * @param shoeType
	 *            String - the shoe which the client wish to buy from the
	 *            {@link Store}.
	 * @param onlyDiscount
	 *            boolean - indicates if the client will buy the shoe if it's
	 *            only on discount or not respectively.
	 * @param requestTick
	 *            int - the tick when the {@link PurchaseOrderRequest} occurred.
	 * @param amount
	 *            int - the amount of shoes of type {@link #shoeType} which the
	 *            client willing to buy.
	 */
	public PurchaseOrderRequest(String seller, String customer, String shoeType, boolean onlyDiscount, int requestTick,
			int amount) {
		this.onlyDiscount = onlyDiscount;
		this.shoeType = shoeType;
		this.seller = seller;
		this.customer = customer;
		this.requestTick = requestTick;
		this.amount = amount;
	}

	/**
	 * @return the {@link #shoeType}.
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return the {@link #onlyDiscount}.
	 */
	public boolean isOnlyDiscount() {
		return onlyDiscount;
	}

	/**
	 * @return the {@link #seller}.
	 */
	public String getSeller() {
		return seller;
	}

	/**
	 * @return return the {@link #customer}.
	 */
	public String getCustomer() {
		return customer;
	}

	/**
	 * @return the {@link #requestTick}.
	 */
	public int getRequestTick() {
		return requestTick;
	}

	/**
	 * @return the {@link #amount}.
	 */
	public int getAmount() {
		return amount;
	}
}
