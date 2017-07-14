package bgu.spl.app;

import bgu.spl.mics.Broadcast;

/**
 * a Broadcast message that is sent when the {@link ManagementService} of the
 * {@link Store} decides to have a sale on a specific shoe.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class NewDiscountBroadcast implements Broadcast {
	/**
	 * string - the shoe which the {@link ManaementService} decided to have sale
	 * on.
	 */
	private String shoeType;
	/**
	 * int - the amount of the shoe of type {@link #shoeType} which the
	 * {@link ManaementService} decided to have sale on.
	 */
	private int amount;

	/**
	 * Creates a new {@link NewDiscountBroadcast}.
	 * 
	 * @param shoeType
	 *            string - the shoe which the {@link ManaementService} decided
	 *            to have sale on.
	 * @param amount
	 *            int - the amount of the shoe of type {@link #shoeType} which
	 *            the {@link ManaementService} decided to have sale on.
	 */
	public NewDiscountBroadcast(String shoeType, int amount) {
		this.shoeType = shoeType;
		this.amount = amount;
	}

	/**
	 * @return the {@link #shoeType}.
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

}
