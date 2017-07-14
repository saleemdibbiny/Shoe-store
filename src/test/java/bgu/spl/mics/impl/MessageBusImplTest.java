package bgu.spl.mics.impl;

import static org.junit.Assert.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.ds.RoundRobinList;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImplTest {
	MessageBusImpl messageBus;
	Broadcast b, b2;
	Request<Boolean> r, r2;
	MicroService m, m2, m3, m4;

	@Before
	public void setUp() {

		messageBus = MessageBusImpl.getInstance();
		b = new Broadcast() {
		};
		b2 = new Broadcast() {
		};
		r = new Request<Boolean>() {
		};
		r2 = new Request<Boolean>() {
		};

		m = new MicroService("MicroService1") {
			@Override
			protected void initialize() {
			}
		};
		m2 = new MicroService("MicroService2") {
			@Override
			protected void initialize() {
			}
		};
		m3 = new MicroService("MicroService3") {
			@Override
			protected void initialize() {
			}
		};
		m4 = new MicroService("MicroService3") {
			@Override
			protected void initialize() {
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		messageBus.getRequestSubscribers().clear();
		messageBus.getBroadcastSubscribers().clear();
		messageBus.getPersonalMessageQueues().clear();
		messageBus.getRequesterMessage().clear();
		messageBus.getRequestsSent().clear();
		messageBus.getPersonalSubscribes().clear();
	}

	@Test
	public void testGetInstance() {
		assertEquals(messageBus, MessageBusImpl.getInstance());
	}

	@Test
	public void testSubscribeRequest() {

		messageBus.subscribeRequest(r.getClass(), m);

		RoundRobinList<MicroService> list = messageBus.getRequestSubscribers().get(r.getClass());

		// Test if microservice successfuly subscribed
		assertNotEquals(null, list);
		assertEquals(1, list.size());
		assertEquals("MicroService1", list.select().getName());

		messageBus.subscribeRequest(r.getClass(), m2);
		messageBus.subscribeRequest(r.getClass(), m3);

		// Test if the second and third microservices successfuly subscribed
		assertNotEquals(null, list);
		assertEquals(3, list.size());
		assertEquals("MicroService2", list.select().getName());
		assertEquals("MicroService3", list.select().getName());
		assertEquals("MicroService1", list.select().getName());
		assertEquals("MicroService2", list.select().getName());
	}

	@Test
	public void testSubscribeBroadcast() {
		messageBus.subscribeBroadcast(b.getClass(), m);
		LinkedBlockingQueue<MicroService> queue = messageBus.getBroadcastSubscribers().get(b.getClass());
		// Test if microservice successfuly subscribed
		assertNotEquals(null, queue);
		assertEquals(1, queue.size());
		assertEquals(m, queue.peek());

		messageBus.subscribeBroadcast(b.getClass(), m2);

		// Test if the second microservice successfuly subscribed
		assertNotEquals(null, queue);
		assertEquals(2, queue.size());
		assertEquals(true, queue.contains(m2));
		assertEquals("MicroService1", queue.remove().getName());
		assertEquals("MicroService2", queue.remove().getName());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComplete() {
		messageBus.register(m);
		messageBus.register(m2);
		messageBus.subscribeRequest(r.getClass(), m);
		messageBus.sendRequest(r, m2);
		Message msg = null;
		try {
			msg = messageBus.awaitMessage(m);
		} catch (InterruptedException e) {
			fail("An unhandled exception");
			e.printStackTrace();
		}
		messageBus.complete((Request<Boolean>)msg, true);
		try {
			assertEquals(Boolean.TRUE,((RequestCompleted<Boolean>)(messageBus.awaitMessage(m2))).getResult());
		} catch (InterruptedException e) {
			fail("An unhandled exception");
			e.printStackTrace();
		}
		messageBus.sendRequest(r, m2);
		try {
			msg = messageBus.awaitMessage(m);
		} catch (InterruptedException e) {
			fail("An unhandled exception");
			e.printStackTrace();
		}
		messageBus.complete((Request<Boolean>)msg, false);
		try {
			assertEquals(Boolean.FALSE,((RequestCompleted<Boolean>)(messageBus.awaitMessage(m2))).getResult());
		} catch (InterruptedException e) {
			fail("An unhandled exception");
			e.printStackTrace();
		}
	}

	@Test
	public void testSendBroadcast() {
		messageBus.register(m);
		messageBus.register(m2);
		messageBus.register(m3);

		messageBus.subscribeBroadcast(b.getClass(), m);
		messageBus.sendBroadcast(b);
		ConcurrentHashMap<String, LinkedBlockingQueue<Message>> messages = messageBus.getPersonalMessageQueues();
		LinkedBlockingQueue<Message> queue1 = messages.get(m.getName());
		LinkedBlockingQueue<Message> queue2 = messages.get(m2.getName());
		LinkedBlockingQueue<Message> queue3 = messages.get(m3.getName());
		assertNotEquals(null, queue1);
		assertEquals(1, queue1.size());
		assertEquals(b.getClass(), queue1.peek().getClass());
		assertNotEquals(null, queue2);
		assertEquals(0, queue2.size());
		assertNotEquals(null, queue3);
		assertEquals(0, queue3.size());
		messageBus.subscribeBroadcast(b2.getClass(), m);
		messageBus.subscribeBroadcast(b2.getClass(), m2);
		messageBus.subscribeBroadcast(b.getClass(), m3);
		messageBus.sendBroadcast(b2);
		assertNotEquals(null, queue1);
		assertEquals(2, queue1.size());
		assertEquals(b.getClass(), queue1.remove().getClass());
		assertEquals(b2.getClass(), queue1.remove().getClass());
		assertNotEquals(null, queue2);
		assertEquals(1, queue2.size());
		assertEquals(b2.getClass(), queue2.remove().getClass());
		assertNotEquals(null, queue3);
		assertEquals(0, queue3.size());

	}

	@Test
	public void testSendRequest() {
		messageBus.register(m);
		messageBus.register(m2);
		messageBus.register(m3);
		messageBus.register(m4);

		messageBus.subscribeRequest(r.getClass(), m);
		messageBus.sendRequest(r, m3);
		ConcurrentHashMap<String, LinkedBlockingQueue<Message>> messages = messageBus.getPersonalMessageQueues();
		LinkedBlockingQueue<Message> queue1 = messages.get(m.getName());
		LinkedBlockingQueue<Message> queue2 = messages.get(m2.getName());
		LinkedBlockingQueue<Message> queue3 = messages.get(m3.getName());
		assertNotEquals(null, queue1);
		assertNotEquals(null, queue2);
		assertNotEquals(null, queue3);
		assertEquals(1, queue1.size());
		assertEquals(r.getClass(), queue1.peek().getClass());
		assertEquals(0, queue2.size());
		assertEquals(0, queue3.size());
		messageBus.subscribeRequest(r.getClass(), m2);
		messageBus.subscribeRequest(r.getClass(), m3);
		messageBus.sendRequest(r, m4);
		assertEquals(1, queue1.size());
		assertEquals(r.getClass(), queue1.peek().getClass());
		assertEquals(1, queue2.size());
		assertEquals(r.getClass(), queue2.peek().getClass());
		assertEquals(0, queue3.size());
		messageBus.sendRequest(r, m4);
		assertEquals(1, queue1.size());
		assertEquals(r.getClass(), queue1.peek().getClass());
		assertEquals(1, queue2.size());
		assertEquals(r.getClass(), queue2.peek().getClass());
		assertEquals(1, queue3.size());
		assertEquals(r.getClass(), queue3.peek().getClass());
		messageBus.sendRequest(r, m4);
		assertEquals(2, queue1.size());
		assertEquals(r.getClass(), queue1.peek().getClass());
		assertEquals(1, queue2.size());
		assertEquals(r.getClass(), queue2.peek().getClass());
		assertEquals(1, queue3.size());
		assertEquals(r.getClass(), queue3.peek().getClass());
		messageBus.subscribeRequest(r2.getClass(), m);
		messageBus.subscribeRequest(r2.getClass(), m2);
		messageBus.subscribeRequest(r2.getClass(), m3);
		messageBus.sendRequest(r2, m4);
		assertEquals(3, queue1.size());
		assertEquals(r.getClass(), queue1.peek().getClass());
		assertEquals(1, queue2.size());
		assertEquals(r.getClass(), queue2.peek().getClass());
		assertEquals(1, queue3.size());
		assertEquals(r.getClass(), queue3.peek().getClass());
		messageBus.sendRequest(r2, m4);
		assertEquals(3, queue1.size());
		assertEquals(r.getClass(), queue1.peek().getClass());
		assertEquals(2, queue2.size());
		assertEquals(r.getClass(), queue2.peek().getClass());
		assertEquals(1, queue3.size());
		assertEquals(r.getClass(), queue3.peek().getClass());
		assertEquals(r.getClass(), queue1.remove().getClass());
		assertEquals(r.getClass(), queue1.remove().getClass());
		assertEquals(r2.getClass(), queue1.remove().getClass());
		assertEquals(r.getClass(), queue2.remove().getClass());
		assertEquals(r2.getClass(), queue2.remove().getClass());
		assertEquals(r.getClass(), queue3.remove().getClass());

		// Test if one can send for himself
		messageBus.sendRequest(r, m2);
		assertEquals(r.getClass(), queue2.remove().getClass());
		assertEquals(0, queue1.size());
		assertEquals(0, queue2.size());
		assertEquals(0, queue3.size());

	}

	@Test
	public void testRegister() {
		assertEquals(null, messageBus.getPersonalMessageQueues().get(m.getName()));
		messageBus.register(m);
		assertNotEquals(null, messageBus.getPersonalMessageQueues().get(m.getName()));
	}

	@Test
	public void testUnregister() {
		messageBus.register(m);
		messageBus.unregister(m);
		assertEquals(null, messageBus.getPersonalMessageQueues().get(m.getName()));

	}

	@Test
	public void testAwaitMessage() {
		messageBus.register(m);
		messageBus.subscribeBroadcast(b.getClass(), m);
		messageBus.sendBroadcast(b);
		try {
			assertEquals(messageBus.getPersonalMessageQueues().get(m.getName()).peek(), messageBus.awaitMessage(m));
		} catch (InterruptedException e) {
			fail("An unhandled exception");
			e.printStackTrace();
		}
		assertEquals(0, messageBus.getPersonalMessageQueues().get(m.getName()).size());

	}

	@Test
	public void getPersonalMessageQueues() {
		assertEquals(new ConcurrentHashMap<String, LinkedBlockingQueue<Message>>(), messageBus.getPersonalMessageQueues());
	}

	@Test
	public void getPersonalSubscribes() {
		assertEquals(new ConcurrentHashMap<String, LinkedBlockingQueue<Class<? extends Message>>>(), messageBus.getPersonalSubscribes());
	}

	@Test
	public void getRequestSubscribers() {
		assertEquals(new ConcurrentHashMap<Class<? extends Request<Boolean>>, RoundRobinList<MicroService>>(), messageBus.getRequestSubscribers());
	}

	@Test
	public void getBroadcastSubscribers() {
		assertEquals(new ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>>(), messageBus.getBroadcastSubscribers());
	}

	@Test
	public void getRequesterMessage() {
		assertEquals(new ConcurrentHashMap<Request<Boolean>, MicroService>(), messageBus.getRequesterMessage());
	}

	@Test
	public void getRequestsSent() {
		assertEquals(new ConcurrentHashMap<String, LinkedBlockingQueue<Request<Boolean>>>(), messageBus.getRequestsSent());
	}

}

