package bgu.spl.app;

/**
 * Describes a schedule of a single client-purchase at a specific tick.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class PurchaseSchedule {
	/**
	 * String - the type of shoe to purchase.
	 */
	private String shoeType;
	/**
	 * int - the tick number to send the {@link PurchaseOrderRequest} at.
	 */
	private int tick;

	/**
	 * Creates a new {@link PurchaseSchedule}.
	 * 
	 * @param shoeType
	 *            String - the type of shoe to purchase.
	 * @param tick
	 *            int - the tick number to send the {@link PurchaseOrderRequest}
	 *            at.
	 */
	public PurchaseSchedule(String shoeType, int tick) {
		this.shoeType = shoeType;
		this.tick = tick;
	}

	/**
	 * @return the {@link #shoeType}.
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return the {@link #tick}.
	 */
	public int getTick() {
		return tick;
	}
}
