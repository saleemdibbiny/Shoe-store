package bgu.spl.app;

/**
 * Describes a schedule of a single discount that the {@link ManagementService}
 * will add to a specific shoe at a specific tick.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class DiscountSchedule {
	/**
	 * string - the type of shoe to add discount to.
	 */
	private String shoeType;
	/**
	 * int - the tick number to send the add the discount at.
	 */
	private int tick;
	/**
	 * int - the amount of items to put on discount
	 */
	private int amount;

	/**
	 * Creates a new {@link DiscountSchedule}.
	 * 
	 * @param shoeType
	 *            string - the type of shoe to add discount to.
	 * 
	 * @param tick
	 *            int - the tick number to send the add the discount at.
	 * 
	 * @param amount
	 *            int - the amount of items to put on discount
	 * 
	 */
	public DiscountSchedule(String shoeType, int tick, int amount) {
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}

	/**
	 * @return string - the shoe type name.
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return int - the tick number to send the add the discount at.
	 */
	public int getTick() {
		return tick;
	}

	/**
	 * @return int - amount of items to put on discount.
	 */
	public int getAmount() {
		return amount;
	}

}
