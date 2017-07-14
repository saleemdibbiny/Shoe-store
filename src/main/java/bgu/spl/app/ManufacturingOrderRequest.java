package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * A request that is sent when the {@link ManagementService} want the
 * {@link ShoeFactoryService} to manufacture a shoe for the {@link Store}.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class ManufacturingOrderRequest implements Request<Receipt> {
	/**
	 * String - the type of a shoe which the {@link ManagementService} requests
	 * from the {@link ShoeFactoryService}.
	 */
	private String shoeType;
	/**
	 * int - the amount of a shoe of type {@link #shoeType} which the
	 * {@link ManagementService} requests from the {@link ShoeFactoryService}.
	 */
	private int amount;
	/**
	 * int - used to save the initial {@link #amount}, Because {@link #amount}
	 * will be decreased in the future.
	 */
	private int requestedAmount;
	/**
	 * int - the tick when the {@link Request} occurred.
	 */
	private int requestTick;

	/**
	 * Creates a new {@link ManufacturingOrderRequest}.
	 * 
	 * @param shoeType
	 *            String - the type of a shoe which the
	 *            {@link ManagementService} requests from the
	 *            {@link ShoeFactoryService}.
	 * @param amount
	 *            int - the {@link #amount} of a shoe of type {@link #shoeType}
	 *            which the {@link ManagementService} requests from the factory.
	 * @param requestTick
	 *            int - the tick when the request occurred.
	 */
	public ManufacturingOrderRequest(String shoeType, int amount, int requestTick) {
		this.shoeType = shoeType;
		this.amount = amount;
		this.requestedAmount = amount;
		this.requestTick = requestTick;
	}

	/**
	 * @return the {@link #shoeType}
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return the {@link #amount}.
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * decrease the {@link #amount} by 1.
	 */
	public void decreaseAmount() {
		this.amount--;
	}

	/**
	 * @return {@link #requestedAmount}
	 */
	public int getRequestedAmount() {
		return this.requestedAmount;
	}

	/**
	 * @return the {@link #requestTick}.
	 */
	public int getRequestTick() {
		return this.requestTick;
	}
}
