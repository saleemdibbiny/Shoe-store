package bgu.spl.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class ShoeStoreRunner {
	private static final Logger log = Logger.getLogger(ShoeStoreRunner.class.getName());

	public static void main(String args[]) {
		log.info("Reading json file");
		AllData data = readJsonFile("src/resources/files/" + args[0]);
		int numberOfServices = getNumberOfServices(data);

		CountDownLatch countDown = new CountDownLatch(numberOfServices - 1);
		Phaser phaser = new Phaser(numberOfServices);

		log.info("Initializing " + numberOfServices + " services");

		// Store
		Store store = initializeStore(data);

		// Management Service
		initializeManagementService(store, data, countDown);

		// Web Site Client Services
		initializeWebsiteClientService(data, countDown, phaser);

		// Selling Services
		initializeSellingService(store, data, countDown);

		// Shoe Factory Services
		initializeShoeFactoryService(data, countDown);

		// Timer Service
		initializeTimeService(store, data, countDown, phaser);

	}

	private static void initializeTimeService(Store store, AllData data, CountDownLatch countDown, Phaser phaser) {
		TimeService timer = new TimeService(data.services.time.speed, data.services.time.duration, store, countDown,
				phaser);
		new Thread(timer).start();

	}

	private static void initializeShoeFactoryService(AllData data, CountDownLatch countDown) {
		for (int i = 0; i < data.services.factories; i++) {
			ShoeFactoryService factory = new ShoeFactoryService("Factory" + i, countDown);
			new Thread(factory).start();
		}

	}

	private static void initializeSellingService(Store store, AllData data, CountDownLatch countDown) {
		for (int i = 0; i < data.services.sellers; i++) {
			SellingService seller = new SellingService(store, "SellingService" + i, countDown);
			new Thread(seller).start();
		}

	}

	private static void initializeWebsiteClientService(AllData data, CountDownLatch countDown, Phaser phaser) {
		CustomerData[] customers = data.services.customers;
		WebsiteClientService[] websiteClientServices = new WebsiteClientService[customers.length];
		for (int i = 0; i < data.services.customers.length; i++) {
			CustomerData customer = customers[i];
			websiteClientServices[i] = new WebsiteClientService(customer.name, countDown, phaser);
			for (int j = 0; j < customer.wishList.length; j++) {
				websiteClientServices[i].addWishItem(customer.wishList[j]);
			}
			for (int j = 0; j < customer.purchaseSchedule.length; j++) {
				PurchaseSchedule purchaseInfo = customer.purchaseSchedule[j];
				websiteClientServices[i].addPurchaseSchedule(purchaseInfo.getShoeType(), purchaseInfo.getTick());
			}
			new Thread(websiteClientServices[i]).start();

		}

	}

	private static void initializeManagementService(Store store, AllData data, CountDownLatch countDown) {
		ManagementService managerService = new ManagementService(store, countDown);
		for (int i = 0; i < data.services.manager.discountSchedule.length; i++) {
			managerService.addDiscountSchedules(data.services.manager.discountSchedule[i].getShoeType(),
					data.services.manager.discountSchedule[i].getTick(),
					data.services.manager.discountSchedule[i].getAmount());
		}
		new Thread(managerService).start();

	}

	private static Store initializeStore(AllData data) {
		return Store.load(Arrays.stream(data.initialStorage).map(s -> new ShoeStorageInfo(s.shoeType, s.amount))
				.toArray(ShoeStorageInfo[]::new));
	}

	private static int getNumberOfServices(AllData data) {
		int numberOfServices = 1;
		numberOfServices += data.services.factories + data.services.sellers + data.services.customers.length;
		if (data.services.manager != null)
			numberOfServices += 1;
		return numberOfServices;
	}

	private static AllData readJsonFile(String path) {
		Gson gson = new Gson();
		JsonReader reader = null;
		try {
			reader = new JsonReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		AllData data = gson.fromJson(reader, AllData.class);
		return data;
	}

	private class AllData {
		private InitialShoeData[] initialStorage;
		private ServicesData services;
	}

	private class ServicesData {
		private TimeData time;
		private ManagerData manager;
		private int factories;
		private int sellers;
		private CustomerData[] customers;
	}

	private class TimeData {
		private int speed;
		private int duration;
	}

	private class ManagerData {
		private DiscountSchedule[] discountSchedule;

	}

	private class CustomerData {
		private String name;
		private String[] wishList;
		private PurchaseSchedule[] purchaseSchedule;
	}

	private class InitialShoeData {
		private String shoeType;
		private int amount;
	}
}