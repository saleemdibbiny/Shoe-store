package bgu.spl.app;

import java.util.ArrayList;

/**
 * This class representing a single manufacturing order info of a shoe.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class ManufacturingOrderInfo {
	/**
	 * String - the shoe type.
	 */
	private String shoeType;
	/**
	 * int - the ordered amount by the {@link ManagementService}.
	 */
	private int amountOnOrder;
	/**
	 * int - the amount which already ordered to the client.
	 */
	private int amountReserved;
	/**
	 * ArrayList - contains all the {@link RestockReuest}s of that shoe.
	 */
	private ArrayList<RestockRequest> requests;

	/**
	 * Creates a new {@link ManufacturingOrderInfo}.
	 * 
	 * @param shoeType
	 *            String - the shoe type.
	 * @param amountOnOrder
	 *            int - the ordered amount by the {@link ManagementService}.
	 * @param amountReserved
	 *            int - the amount which already ordered to the client.
	 */
	public ManufacturingOrderInfo(String shoeType, int amountOnOrder, int amountReserved) {
		this.shoeType = shoeType;
		this.amountOnOrder = amountOnOrder;
		this.amountReserved = amountReserved;
		requests = new ArrayList<RestockRequest>();
	}

	/**
	 * @return the {@link #shoeType}.
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return the {@link #amountOnOrder}.
	 */
	public int getAmountOnOrder() {
		return amountOnOrder;
	}

	/**
	 * @return the {@link #amountReserved}.
	 */
	public int getAmountReserved() {
		return amountReserved;
	}

	/**
	 * @return true if the {@link #amountReserved} equals to the
	 *         {@link #amountOnOrder}, false otherwise.
	 */
	public boolean isAllReserved() {
		return amountOnOrder == amountReserved;
	}

	/**
	 * Adds a {@link RestockRequest} to the {@link #requests} list and increase
	 * {@link #amountReserved} by 1.
	 * 
	 * @param r
	 *            the request which will be added to the {@link #requests} list.
	 */
	public void addRequest(RestockRequest r) {
		requests.add(r);
		amountReserved++;
	}

	/**
	 * @return the {@link #requests} list.
	 */
	public ArrayList<RestockRequest> getRequests() {
		return requests;
	}
}
