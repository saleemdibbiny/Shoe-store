
package bgu.spl.app;

/**
 * Describe the customer buying result, the results may be:
 * <p>
 * <li>{@link #NOT_IN_STOCK}</li>
 * <li>{@link #NOT_ON_DISCOUNT}</li>
 * <li>{@link #REGULAR_PRICE}</li>
 * <li>{@link #DISCOUNTED_PRICE}</li>
 * 
 * @author Anan Kays, Saleem Dibbiny
 */
public enum BuyResult {
	/**
	 * The item is not in stock
	 */
	NOT_IN_STOCK, /**
					 * The item in stock but not on discount
					 */
	NOT_ON_DISCOUNT, /**
						 * The customer bought the item with regular price
						 */
	REGULAR_PRICE, /**
					 * The customer bought the item with discounted price
					 */
	DISCOUNTED_PRICE
}
