package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 *
 * @author naoshi
 */
public final class BlockingLIFOQueue<E> implements BlockingQueue<E> {
	private final BlockingDeque<E> fDeque = new LinkedBlockingDeque();

	public boolean add(E e) {
		fDeque.addFirst(e);
		return true;
	}

	public boolean offer(E e) {
		return fDeque.offerFirst(e);
	}

	public void put(E e) throws InterruptedException {
		fDeque.putFirst(e);
	}

	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return fDeque.offerFirst(e, timeout, unit);
	}

	public E take() throws InterruptedException {
		return fDeque.takeFirst();
	}

	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		return fDeque.pollFirst(timeout, unit);
	}

	public int remainingCapacity() {
		return fDeque.remainingCapacity();
	}

	public boolean remove(Object o) {
		return fDeque.removeFirstOccurrence(o);
	}

	public boolean contains(Object o) {
		return fDeque.contains(o);
	}

	public int drainTo(Collection<? super E> c) {
		return fDeque.drainTo(c);
	}

	public int drainTo(Collection<? super E> c, int maxElements) {
		return fDeque.drainTo(c, maxElements);
	}

	public E remove() {
		return fDeque.removeFirst();
	}

	public E poll() {
		return fDeque.pollFirst();
	}

	public E element() {
		return fDeque.element();
	}

	public E peek() {
		return fDeque.peekFirst();
	}

	public int size() {
		return fDeque.size();
	}

	public boolean isEmpty() {
		return fDeque.isEmpty();
	}

	public Iterator<E> iterator() {
		return fDeque.iterator();
	}

	public Object[] toArray() {
		return fDeque.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return fDeque.toArray(a);
	}

	public boolean containsAll(Collection<?> c) {
		return fDeque.containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		for (E e : c) {
			fDeque.addFirst(e);
		}
		return true;
	}

	public boolean removeAll(Collection<?> c) {
		return fDeque.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return fDeque.retainAll(c);
	}

	public void clear() {
		fDeque.clear();
	}
}
