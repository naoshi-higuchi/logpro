package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;

public final class BlockingLIFOQueue<E> implements BlockingQueue<E> {
	private final BlockingDeque<E> fDeque = new LinkedBlockingDeque<>();

	@Override public boolean add(E e)                                          { fDeque.addFirst(e); return true; }
	@Override public boolean offer(E e)                                        { return fDeque.offerFirst(e); }
	@Override public void put(E e) throws InterruptedException                 { fDeque.putFirst(e); }
	@Override public boolean offer(E e, long t, TimeUnit u) throws InterruptedException { return fDeque.offerFirst(e, t, u); }
	@Override public E take() throws InterruptedException                      { return fDeque.takeFirst(); }
	@Override public E poll(long t, TimeUnit u) throws InterruptedException    { return fDeque.pollFirst(t, u); }
	@Override public int remainingCapacity()                                   { return fDeque.remainingCapacity(); }
	@Override public boolean remove(Object o)                                  { return fDeque.removeFirstOccurrence(o); }
	@Override public boolean contains(Object o)                                { return fDeque.contains(o); }
	@Override public int drainTo(Collection<? super E> c)                     { return fDeque.drainTo(c); }
	@Override public int drainTo(Collection<? super E> c, int max)            { return fDeque.drainTo(c, max); }
	@Override public E remove()                                                { return fDeque.removeFirst(); }
	@Override public E poll()                                                  { return fDeque.pollFirst(); }
	@Override public E element()                                               { return fDeque.element(); }
	@Override public E peek()                                                  { return fDeque.peekFirst(); }
	@Override public int size()                                                { return fDeque.size(); }
	@Override public boolean isEmpty()                                         { return fDeque.isEmpty(); }
	@Override public Iterator<E> iterator()                                    { return fDeque.iterator(); }
	@Override public Object[] toArray()                                        { return fDeque.toArray(); }
	@Override public <T> T[] toArray(T[] a)                                   { return fDeque.toArray(a); }
	@Override public boolean containsAll(Collection<?> c)                      { return fDeque.containsAll(c); }
	@Override public boolean removeAll(Collection<?> c)                        { return fDeque.removeAll(c); }
	@Override public boolean retainAll(Collection<?> c)                        { return fDeque.retainAll(c); }
	@Override public void clear()                                              { fDeque.clear(); }

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (var e : c) fDeque.addFirst(e);
		return true;
	}
}
