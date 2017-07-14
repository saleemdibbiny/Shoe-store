package bgu.spl.app;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents information about a single type of shoe in the
 * {@link Store}.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class ShoeStorageInfo {
	/**
	 * String - the type of the shoe.
	 */
	private String shoeType;
	/**
	 * AtomicInteger - the number of shoes of {@link #shoeType} currently on the
	 * storage.
	 * 
	 */
	private AtomicInteger amount;
	/**
	 * AtomicInteger - amount of shoes in this storage that can be sale in a
	 * discounted price.
	 * 
	 */
	private AtomicInteger discountedAmount = new AtomicInteger();

	public ShoeStorageInfo(String shoeType, int amount) {
		this.shoeType = shoeType;
		this.amount = new AtomicInteger(amount);
		this.discountedAmount = new AtomicInteger();
	}

	/**
	 * Creates a new {@link ShoeStorageInfo} instance.
	 * 
	 * @param shoeType
	 *            String - the type of the shoe.
	 * @param amount
	 *            int - the number of shoes of {@link #shoeType} currently on
	 *            the storage.
	 * @param discountedAmount
	 *            int - amount of shoes in this storage that can be sale in a
	 *            discounted price.
	 */
	public ShoeStorageInfo(String shoeType, int amount, int discountedAmount) {
		this.shoeType = shoeType;
		this.amount = new AtomicInteger(amount);
		this.discountedAmount = new AtomicInteger(discountedAmount);
	}

	/**
	 * @return the {@link #shoeType}.
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return {@link #amount}.
	 */
	public int getAmountOnStorage() {
		return amount.get();
	}

	/**
	 * @return the {@link #discountedAmount}.
	 */
	public int getDiscountedAmount() {
		return discountedAmount.get();
	}

	/**
	 * add the {@code moreAmount} to the current {@link #amount} On storage.
	 * 
	 * @param moreAmount
	 *            the amount to add to the current {@link #amount} on storage.
	 */
	public void addAmountOnStorage(int moreAmount) {
		this.amount.addAndGet(moreAmount);
	}

	/**
	 * add the {@code moreDiscountedAmount} to the current
	 * {@link #discountedAmount} on storage.
	 * 
	 * @param moreDiscountedAmount
	 *            the amount to add to the current {@link #discountedAmount} on
	 *            storage.
	 */
	public void addDiscountedAmount(int moreDiscountedAmount) {
		this.discountedAmount.addAndGet(moreDiscountedAmount);
	}

}