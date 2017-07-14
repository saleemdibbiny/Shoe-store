package bgu.spl.app;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StoreTest {
	Store store;
	ShoeStorageInfo[] shoeStorageInfo;
	
	@Before
	public void setUp() throws Exception {
		shoeStorageInfo = new ShoeStorageInfo[5];
		for(int i = 0; i<shoeStorageInfo.length; i++){
			shoeStorageInfo[i] = new ShoeStorageInfo("type"+i, i);
		}
		store = Store.load(shoeStorageInfo);
	}

	@After
	public void tearDown() throws Exception {
		store.getReceipts().clear();
		Store.getShoeStorageData().clear();
		for(int i = 0; i<shoeStorageInfo.length; i++)
			Store.getShoeStorageData().add(new ShoeStorageInfo("type"+i, i));
	}

	@Test
	public void testLoad() {
		assertEquals(store, Store.load(new ShoeStorageInfo[5]));
	}

	@Test
	public void testTake() {
		assertEquals(BuyResult.NOT_IN_STOCK, store.take("type0", false));
		assertEquals(BuyResult.NOT_IN_STOCK, store.take("type0", true));
		assertEquals(BuyResult.REGULAR_PRICE, store.take("type1", false));
		assertEquals(BuyResult.NOT_IN_STOCK, store.take("type1", false));
		store.addDiscount("type2", 2);
		assertEquals(BuyResult.DISCOUNTED_PRICE, store.take("type2", true));
		assertEquals(BuyResult.DISCOUNTED_PRICE, store.take("type2", false));
		assertEquals(BuyResult.NOT_ON_DISCOUNT, store.take("type3", true));
		
	}

	@Test
	public void testIsInStockString() {
		assertEquals(false, store.isInStock("type0"));
		assertEquals(true, store.isInStock("type1"));
	}

	@Test
	public void testAddDiscount() {
		store.addDiscount("type0", 4);
		assertEquals(0, store.getByShoeType("type0").getDiscountedAmount());
		store.addDiscount("type0", -1);
		assertEquals(0, store.getByShoeType("type0").getDiscountedAmount());
		store.addDiscount("type1", 1);
		assertEquals(1, store.getByShoeType("type1").getDiscountedAmount());
		store.addDiscount("type1", 1);
		assertEquals(1, store.getByShoeType("type1").getDiscountedAmount());
		store.addDiscount("type2", 5);
		assertEquals(2, store.getByShoeType("type2").getDiscountedAmount());
		
	}

	@Test
	public void testFile() {
		assertEquals(true, store.getReceipts().isEmpty());
		store.file(new Receipt("seller", "customer", "type1", true, 2, 1, 1));
		assertEquals(1, store.getReceipts().size());
		assertEquals("type1", store.getReceipts().get(0).getShoeType());
		assertEquals(1, store.getReceipts().get(0).getAmountSold());
	}

	@Test
	public void testPrint() {
		
	}

	@Test
	public void testAddShoeIfNotInStorage() {
		store.addShoeIfNotInStorage("type5", 2, 1);
		assertEquals(ShoeStorageInfo.class, store.getByShoeType("type5").getClass());
		assertEquals(2, store.getByShoeType("type5").getAmountOnStorage());
		assertEquals(1, store.getByShoeType("type5").getDiscountedAmount());
	}

	@Test
	public void testAdd() {
		store.add("type1", 4);
		assertEquals(5, store.getByShoeType("type1").getAmountOnStorage());
		store.add("type5", 5);
		assertEquals(null, store.getByShoeType("type5"));
	}

	@Test
	public void testGetReceipts(){
		assertEquals(new ArrayList<Receipt>(), store.getReceipts());
	}
	
	@Test
	public void getShoeStorageData(){
		List<ShoeStorageInfo> s = new ArrayList<ShoeStorageInfo>();
		for(int i = 0; i<shoeStorageInfo.length; i++)
			s.add(new ShoeStorageInfo("type"+i, i));
		List<ShoeStorageInfo> s2 = Store.getShoeStorageData();
		for(int i = 0; i<shoeStorageInfo.length; i++){
			ShoeStorageInfo shoe1 = s.get(i);
			ShoeStorageInfo shoe2 = s2.get(i);
			assertEquals(shoe1.getShoeType(), shoe2.getShoeType());
			assertEquals(shoe1.getAmountOnStorage(), shoe2.getAmountOnStorage());
			assertEquals(shoe1.getDiscountedAmount(), shoe2.getDiscountedAmount());
		}
	}

}
