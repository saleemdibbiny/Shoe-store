package bgu.spl.mics.impl;

import bgu.spl.ds.RoundRobinList;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import java.util.concurrent.*;

public class MessageBusImpl implements MessageBus {
	private ConcurrentHashMap<String, LinkedBlockingQueue<Message>> PersonalMessageQueues;
	private ConcurrentHashMap<String, LinkedBlockingQueue<Class<? extends Message>>> PersonalSubscribes;
	private ConcurrentHashMap<Class<? extends Request>, RoundRobinList<MicroService>> RequestSubscribers;
	private ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> BroadcastSubscribers;
	private ConcurrentHashMap<Request, MicroService> RequesterMessage;
	private ConcurrentHashMap<String, LinkedBlockingQueue<Request>> RequestsSent;

	private MessageBusImpl() {
		PersonalMessageQueues = new ConcurrentHashMap<String, LinkedBlockingQueue<Message>>();
		RequestSubscribers = new ConcurrentHashMap<Class<? extends Request>, RoundRobinList<MicroService>>();
		BroadcastSubscribers = new ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>>();
		RequesterMessage = new ConcurrentHashMap<Request, MicroService>();
		RequestsSent = new ConcurrentHashMap<String, LinkedBlockingQueue<Request>>();
		PersonalSubscribes = new ConcurrentHashMap<String, LinkedBlockingQueue<Class<? extends Message>>>();
	}

	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	public synchronized void subscribeRequest(Class<? extends Request> type, MicroService m) {
		try {
			if (!RequestSubscribers.containsKey(type)) {
				RequestSubscribers.put(type, new RoundRobinList<MicroService>());
			}
			RequestSubscribers.get(type).put(m);
			if (!PersonalSubscribes.containsKey(m.getName()))
				PersonalSubscribes.put(m.getName(), new LinkedBlockingQueue<Class<? extends Message>>());
			PersonalSubscribes.get(m.getName()).put(type);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		try {
			if (!BroadcastSubscribers.containsKey(type))
				BroadcastSubscribers.put(type, new LinkedBlockingQueue<MicroService>());
			BroadcastSubscribers.get(type).put(m);
			if (!PersonalSubscribes.containsKey(m.getName()))
				PersonalSubscribes.put(m.getName(), new LinkedBlockingQueue<Class<? extends Message>>());
			PersonalSubscribes.get(m.getName()).put(type);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Notifying the MessageBus that the request {@code r} is completed and its
	 * result was {@code result}. When this method is called, the message-bus
	 * will implicitly add the special {@link RequestCompleted} message to the
	 * queue of the requesting micro-service, the RequestCompleted message will
	 * also contain the result of the request ({@code result}).
	 * <p>
	 * 
	 * @param <T>
	 *            the type of the result expected by the completed request
	 * @param r
	 *            the completed request
	 * @param result
	 *            the result of the completed request
	 */
	public <T> void complete(Request<T> r, T result) {
		MicroService requester = RequesterMessage.remove(r);
		try {
			PersonalMessageQueues.get(requester.getName()).put(new RequestCompleted<T>(r, result));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sendBroadcast(Broadcast b) {
		LinkedBlockingQueue<MicroService> queue = BroadcastSubscribers.get(b.getClass());
		if (queue != null) {
			for (MicroService service : queue) {
				try {
					PersonalMessageQueues.get(service.getName()).put(b);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * add the {@link Request} {@code r} to the message queue of one of the
	 * micro-services subscribed to {@code r.getClass()} in a round-robin
	 * fashion.
	 * <p>
	 * 
	 * @param r
	 *            the request to add to the queue.
	 * @param requester
	 *            the {@link MicroService} sending {@code r}.
	 * @return true if there was at least one micro-service subscribed to
	 *         {@code r.getClass()} and false otherwise.
	 */
	public synchronized boolean sendRequest(Request<?> r, MicroService requester) {
		RoundRobinList<MicroService> ls = RequestSubscribers.get(r.getClass());
		if (ls != null && ls.size() > 0) {
			try {
				MicroService m = ls.select();
				RequesterMessage.put(r, requester);
				if (!RequestsSent.containsKey(requester.getName())) {
					RequestsSent.put(requester.getName(), new LinkedBlockingQueue<Request>());
				}
				RequestsSent.get(requester.getName()).put(r);
				PersonalMessageQueues.get(m.getName()).put(r);
				return true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public void register(MicroService m) {
		PersonalMessageQueues.put(m.getName(), new LinkedBlockingQueue<Message>());

	}

	public synchronized void unregister(MicroService m) {
		LinkedBlockingQueue<Class<? extends Message>> types = PersonalSubscribes.get(m.getName());
		if (types != null) {
			for (Class<? extends Message> type : types) {
				if (Broadcast.class.isAssignableFrom(type)) {
					LinkedBlockingQueue<MicroService> queue = BroadcastSubscribers.get(type);
					queue.remove(m);
				} else {
					if (Request.class.isAssignableFrom(type)) {
						RoundRobinList<MicroService> queue = RequestSubscribers.get(type);
						queue.remove(m);
					}
				}
			}
			PersonalSubscribes.remove(m.getName());
		}
		PersonalMessageQueues.remove(m.getName());
		if (RequestsSent.containsKey(m.getName())) {
			for (Request RequestSent : RequestsSent.get(m.getName())) {
				RequesterMessage.remove(RequestSent);
			}
			RequestsSent.remove(m.getName());
		}
	}

	/**
	 * using this method, a <b>registered</b> micro-service can take message
	 * from its allocated queue. This method is blocking -meaning that if no
	 * messages are available in the micro-service queue it should wait until a
	 * message became available. The method should throw the
	 * {@link IllegalStateException} in the case where {@code m} was never
	 * registered.
	 * <p>
	 * 
	 * @param m
	 *            the micro-service requesting to take a message from its
	 *            message queue
	 * @return the next message in the {@code m}'s queue (blocking)
	 * @throws InterruptedException
	 *             if interrupted while waiting for a message to became
	 *             available.
	 */
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!PersonalMessageQueues.containsKey(m.getName()))
			throw new IllegalStateException("MicroService " + m.getName() + " NOT Registered");
		Message message = (PersonalMessageQueues.get(m.getName())).take();
		return message;
	}

	public ConcurrentHashMap<String, LinkedBlockingQueue<Message>> getPersonalMessageQueues() {
		return PersonalMessageQueues;
	}

	public ConcurrentHashMap<String, LinkedBlockingQueue<Class<? extends Message>>> getPersonalSubscribes() {
		return PersonalSubscribes;
	}

	public ConcurrentHashMap<Class<? extends Request>, RoundRobinList<MicroService>> getRequestSubscribers() {
		return RequestSubscribers;
	}

	public ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> getBroadcastSubscribers() {
		return BroadcastSubscribers;
	}

	public ConcurrentHashMap<Request, MicroService> getRequesterMessage() {
		return RequesterMessage;
	}

	public ConcurrentHashMap<String, LinkedBlockingQueue<Request>> getRequestsSent() {
		return RequestsSent;
	}

}