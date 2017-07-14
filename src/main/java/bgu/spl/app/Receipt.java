package bgu.spl.app;

/**
 * Representing a receipt that should be sent to a client after buying a shoe.
 * also it is representing a receipt that should be sent to the
 * {@link ManagementService} after the {@link ShoeFactoryService} manufacture
 * for the {@link Store} a shoe.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class Receipt {

	/**
	 * String - the seller name. it may be {@link SellingService} or
	 * {@link ShoeFactoryService}.
	 */
	private String seller;
	/**
	 * String - the client name. it may be {@link WebsiteClientService} or
	 * {@link Store}.
	 */
	private String customer;
	/**
	 * String - the shoe which have been sold to the client.
	 */
	private String shoeType;
	/**
	 * boolean - indicates if the shoe was sold with discount or not
	 * respectively.
	 */
	private boolean discount;
	/**
	 * int - the tick number when the client received the shoe.
	 */
	private int issuedTick;
	/**
	 * int - the tick number when the client sent a {@link PurchaseOrderRequest}
	 * .
	 */
	private int requestTick;
	/**
	 * int - amount of shoes which sold to the client.
	 */
	private int amountSold;

	/**
	 * Creates new {@link Receipt}.
	 * 
	 * @param seller
	 *            String - the seller name. it may be {@link SellingService} or
	 *            {@link ShoeFactoryService}.
	 * @param customer
	 *            String - the client name. it may be
	 *            {@link WebsiteClientService} or {@link Store}.
	 * @param shoeType
	 *            String - the shoe which have been sold to the client.
	 * @param discount
	 *            boolean - indicates if the shoe was sold with discount or not
	 *            respectively.
	 * @param issuedTick
	 *            int - the tick number when the client received the shoe.
	 * @param requestTick
	 *            int - the tick number when the client sent a
	 *            {@link PurchaseOrderRequest}
	 * @param amountSold
	 *            int - amount of shoes which sold to the client
	 */
	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick,
			int amountSold) {
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}

	/**
	 * @return the {@link #seller}.
	 */
	public String getSeller() {
		return seller;
	}

	/**
	 * @return the {@link #customer}.
	 */
	public String getCustomer() {
		return customer;
	}

	/**
	 * @return the {@link #shoeType}.
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return the {@link #isDiscount}.
	 */
	public boolean isDiscount() {
		return discount;
	}

	/**
	 * @return the {@link #issuedTick}.
	 */
	public int getIssuedTick() {
		return issuedTick;
	}

	/**
	 * @return the {@link #requestTick}.
	 */
	public int getRequestTick() {
		return requestTick;
	}

	/**
	 * @return the {@link #amountSold}.
	 */
	public int getAmountSold() {
		return amountSold;
	}
}