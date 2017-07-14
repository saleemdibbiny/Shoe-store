package bgu.spl.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class holds the {@link ShoeStorageInfo} collection, One for each shoe
 * type the store offers.
 * <p>
 * In addition, it contains a list of receipts issued to and by the store.
 * <p>
 * <u>This object implemented as a thread safe singleton.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 */
public class Store {

	/**
	 * List - contains all the {@link ShoeStorageInfo} collection, one for each
	 * shoe type the store offers.
	 */
	private static List<ShoeStorageInfo> shoeStorageData;

	/**
	 * A flag which needed to implements the store as singleton.
	 */
	private static boolean set = false;
	/**
	 * ArrayList - contains all the receipts issued to and by the store.
	 */
	private ArrayList<Receipt> receipts;

	/**
	 * private Store constructor.
	 */
	private Store() {
		this.receipts = new ArrayList<Receipt>();
	}

	/**
	 * A private static class which creates a new private {@link Store}.
	 * 
	 * @author Anan Kays, Saleem Dibbiny
	 *
	 */
	private static class SingletonHolder {
		private static Store instance = new Store();
	}

	/**
	 * initialize the store storage, the method will add the items in the given
	 * array to the store.
	 * 
	 * @param shoeStorageInfo
	 *            represent information about a single type of shoe in the
	 *            store.
	 * @return a Store instance (singleton).
	 */
	public static Store load(ShoeStorageInfo[] shoeStorageInfo) {
		synchronized (Store.class) {
			Store s = SingletonHolder.instance;
			if (!set) {
				Store.shoeStorageData = Collections
						.synchronizedList(new ArrayList<ShoeStorageInfo>(Arrays.asList(shoeStorageInfo)));
				set = true;
			}
			return s;
		}
	}

	/**
	 * This method will attempt to take a single shoeType from the store.
	 * 
	 * @param shoeType
	 *            shoe type to take.
	 * @param onlyDiscount
	 *            indicates that the client wish to take the item only if it is
	 *            on discount or not.
	 * @return enum {@link BuyResult}.
	 */
	public synchronized BuyResult take(String shoeType, boolean onlyDiscount) {
		int discountedAmount = 0;
		ShoeStorageInfo shoeStorageInfo = getByShoeType(shoeType);
		boolean found = isInStock(shoeStorageInfo);

		if (!found)
			return BuyResult.NOT_IN_STOCK;
		else {
			discountedAmount = shoeStorageInfo.getDiscountedAmount();
			if (onlyDiscount)
				if (discountedAmount == 0)
					return BuyResult.NOT_ON_DISCOUNT;
				else {
					shoeStorageInfo.addAmountOnStorage(-1);
					shoeStorageInfo.addDiscountedAmount(-1);
					return BuyResult.DISCOUNTED_PRICE;
				}
			else if (discountedAmount > 0) {
				shoeStorageInfo.addAmountOnStorage(-1);
				shoeStorageInfo.addDiscountedAmount(-1);
				return BuyResult.DISCOUNTED_PRICE;
			} else {
				shoeStorageInfo.addAmountOnStorage(-1);
				return BuyResult.REGULAR_PRICE;
			}

		}

	}

	/**
	 * Check whether the shoe of type {@code shoeType} is in stock.
	 * <p>
	 * <u> (the method search the storage for the shoe by his {@code shoeType}
	 * name first).<u>
	 * 
	 * @param shoeType
	 *            the checked shoe.
	 * @return true if the shoe of type {@code shoeType} is in stock and false
	 *         otherwise.
	 */
	public boolean isInStock(String shoeType) {
		ShoeStorageInfo s = getByShoeType(shoeType);
		return !(s == null || s.getAmountOnStorage() == 0);
	}

	/**
	 * Check whether the shoe is in stock.
	 * <p>
	 * <u>(the store is already have the shoe in the {@link ShoeStorageInfo}
	 * collection but do not know if still remain amount of that shoe).</u>
	 * 
	 * @param shoe
	 *            the checked shoe.
	 * @return true if the shoe is in stock and false otherwise.
	 */
	public boolean isInStock(ShoeStorageInfo shoe) {
		return !(shoe == null || shoe.getAmountOnStorage() == 0);
	}

	/**
	 * Search the store storage for the shoe of type {@code shoeType}.
	 * 
	 * @param shoeType
	 *            the searched shoe of type {@code shoeType}.
	 * @return the shoe if it is in the store storage and null otherwise.
	 */
	public ShoeStorageInfo getByShoeType(String shoeType) {
		for (ShoeStorageInfo s : shoeStorageData) {
			if (s.getShoeType().compareTo(shoeType) == 0) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Adds the given {@code amount} to the corresponding
	 * {@link ShoeStorageInfo}'s discountedAmount field.
	 * 
	 * @param shoeType
	 *            the shoe of that type to add the discounted amount to.
	 * @param amount
	 *            the amount to add to the corresponding {@code shoeType}
	 *            discount amount.
	 */
	public void addDiscount(String shoeType, int amount) {
		if (amount > 0) {
			ShoeStorageInfo s = getByShoeType(shoeType);
			if (s != null) {
				s.addDiscountedAmount(Math.min(s.getAmountOnStorage() - s.getDiscountedAmount(), amount));
				if (s.getAmountOnStorage() < s.getDiscountedAmount())
					System.err.println("err");
			}
		}
	}

	/**
	 * Adding a receipt to the receipts list of the store.
	 * 
	 * @param receipt
	 *            the {@link Receipt} which the store want to add to the
	 *            receipts list.
	 */
	public synchronized void file(Receipt receipt) {
		receipts.add(receipt);
	}

	/**
	 * Printing all the {@link ShoeStorageInfo} collection and all the receipts
	 * file of the store.
	 */
	public void print() {
		System.out.println("Printing all the Shoe Storage Info...:\n");
		int i = 1;
		for (ShoeStorageInfo s : shoeStorageData) {
			ShoeStorageInfo shoeStorageInfo = s;
			System.out.println("	Shoe #" + i);
			System.out.println("		" + "Name: " + shoeStorageInfo.getShoeType() + ".");
			System.out.println("		" + "Amount: " + shoeStorageInfo.getAmountOnStorage() + ".");
			System.out.println("		" + "Discounted Amount: " + shoeStorageInfo.getDiscountedAmount() + ".\n");
			i++;
		}
		i = 1;
		System.out.println("\n Printing all the receipts info...:\n");
		for (Receipt receipt : receipts) {
			System.out.println("	Receipt #" + i);
			System.out.println("		Seller Name: " + receipt.getSeller() + ".");
			System.out.println("		Customer Name: " + receipt.getCustomer() + ".");
			System.out.println("		Shoe Type: " + receipt.getShoeType() + ".");
			System.out.println("		Discount: " + receipt.isDiscount() + ".");
			System.out.println("		Issued Tick: " + receipt.getIssuedTick() + ".");
			System.out.println("		Request Tick: " + receipt.getRequestTick() + ".");
			System.out.println("		Amount Sold: " + receipt.getAmountSold() + ".\n");
			i++;
		}

	}

	/**
	 * Add a new specific shoe of type {@code shoeType} to the store storage if
	 * it is not available in the storage.
	 * 
	 * @param shoeType
	 *            the shoe of type {@code shoeType} which will be added if it is
	 *            not available in the store storage.
	 * @param amount
	 *            the amount of shoes of type {@code shoeType} which will be
	 *            added if the shoe is not in the store storage.
	 * @param discountedAmount
	 *            the discounted amount of shoes of type {@code shoeType} which
	 *            will be added if the shoe is not in the store storage.
	 */
	public void addShoeIfNotInStorage(String shoeType, int amount, int discountedAmount) {
		if (getByShoeType(shoeType) == null)
			Store.shoeStorageData.add(new ShoeStorageInfo(shoeType, amount, discountedAmount));
	}

	/**
	 * Adds the given {@code amount} to the {@link ShoeStorageInfo} of the given
	 * shoe of type {@code shoeType}.
	 * 
	 * @param shoeType
	 *            the shoe of that type to add the {@code amount} to.
	 * @param amount
	 *            the amount to add to the corresponding {@code shoeType}
	 *            amount.
	 */
	public void add(String shoeType, int amount) {
		ShoeStorageInfo s = getByShoeType(shoeType);
		if (s != null)
			s.addAmountOnStorage(amount);
	}

	public static List<ShoeStorageInfo> getShoeStorageData() {
		return shoeStorageData;
	}

	public ArrayList<Receipt> getReceipts() {
		return receipts;
	}

}