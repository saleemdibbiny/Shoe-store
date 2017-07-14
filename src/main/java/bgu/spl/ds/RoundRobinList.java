package bgu.spl.ds;

import java.util.ArrayList;

/**
 * This class supports the round-robin fashion (Cyclic execution). and
 * implements a blocking queue which supports the round-robin fashion.
 * 
 * @author Anan Kays, Saleem Dibbiny
 *
 * @param <E>
 */
public class RoundRobinList<E> {
	/**
	 * int - decides which index will be taken in the next execution.
	 */
	private int currIndex;
	/**
	 * ArrayList - implementing a queue using an arrayList.
	 */
	private ArrayList<E> list;

	/**
	 * Creates a new {@link RoundRobinList} and initialize the fields.
	 */
	public RoundRobinList() {
		this.list = new ArrayList<E>();
		this.currIndex = -1;
	}

	/**
	 * @return an element in the queue in a cyclic method (round-robin).
	 */
	public E select() {
		synchronized (this.getClass()) {
			currIndex = (currIndex + 1) % this.size();
			E e = list.get(currIndex);
			return e;
		}
	}

	/**
	 * A synchronized method which Adds the {@code t} to the end of the list.
	 * 
	 * @param t
	 *            the item to add to the list.
	 * @throws InterruptedException
	 */
	public void put(E t) throws InterruptedException {
		synchronized (this.getClass()) {
			list.add(t);
		}
	}

	/**
	 * @return true if the list is empty.
	 */
	public boolean isEmpty() {
		return list.size() == 0;
	}

	/**
	 * @return the size of the list.
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Removing an item from the list in a safe way which will not break the
	 * round-robin fashion.
	 * 
	 * @param e
	 *            the item to remove.
	 */
	public void remove(E e) {
		synchronized (this.getClass()) {
			if (currIndex >= list.indexOf(e))
				currIndex--;
			list.remove(e);
		}
	}

}
