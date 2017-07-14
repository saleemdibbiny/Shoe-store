package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * A {@link Request} that is sent by the {@link SellingService} to the
 * {@link ManagementService} so that he will know that he need to order new
 * shoes from the {@link ShoeFactoryService}.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class RestockRequest implements Request<Boolean> {
	/**
	 * String - the shoe which the {@link ManagementService} need to order from
	 * the {@link ShoeFactoryService}.
	 */
	private String shoeType;

	/**
	 * Creates new {@link RestockRequest}.
	 * 
	 * @param shoeType
	 *            String - the shoe which the {@link ManagementService} need to
	 *            order from the {@link ShoeFactoryService}.
	 */
	public RestockRequest(String shoeType) {
		this.shoeType = shoeType;
	}

	/**
	 * @return the {@link #shoeType}.
	 */
	public String getShoeType() {
		return shoeType;
	}
}
