package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BlockingLIFOQueueTest {

    @Test
    public void testLifoOrdering() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        q.add("c");
        assertEquals("c", q.poll());
        assertEquals("b", q.poll());
        assertEquals("a", q.poll());
        assertNull(q.poll());
    }

    @Test
    public void testOffer() {
        var q = new BlockingLIFOQueue<Integer>();
        assertTrue(q.offer(1));
        assertTrue(q.offer(2));
        assertEquals(2, q.poll());
        assertEquals(1, q.poll());
    }

    @Test
    public void testPutAndTake() throws InterruptedException {
        var q = new BlockingLIFOQueue<String>();
        q.put("x");
        q.put("y");
        assertEquals("y", q.take());
        assertEquals("x", q.take());
    }

    @Test
    public void testSizeAndIsEmpty() {
        var q = new BlockingLIFOQueue<String>();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        q.add("a");
        assertFalse(q.isEmpty());
        assertEquals(1, q.size());
    }

    @Test
    public void testAddAllLifoOrdering() {
        var q = new BlockingLIFOQueue<String>();
        q.addAll(List.of("a", "b", "c"));
        assertEquals("c", q.poll());
        assertEquals("b", q.poll());
        assertEquals("a", q.poll());
    }

    @Test
    public void testContains() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        assertTrue(q.contains("a"));
        assertTrue(q.contains("b"));
        assertFalse(q.contains("c"));
    }

    @Test
    public void testRemoveObject() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        assertTrue(q.remove("a"));
        assertFalse(q.contains("a"));
        assertTrue(q.contains("b"));
    }

    @Test
    public void testRemove() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        assertEquals("b", q.remove());
        assertEquals(1, q.size());
    }

    @Test
    public void testPeekAndElement() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        assertEquals("b", q.peek());
        assertEquals("b", q.element());
        assertEquals(2, q.size());
    }

    @Test
    public void testClear() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        q.clear();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
    }

    @Test
    public void testDrainTo() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        q.add("c");
        var out = new ArrayList<String>();
        int n = q.drainTo(out);
        assertEquals(3, n);
        assertTrue(q.isEmpty());
        assertEquals(3, out.size());
    }

    @Test
    public void testDrainToWithMax() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        q.add("c");
        var out = new ArrayList<String>();
        int n = q.drainTo(out, 2);
        assertEquals(2, n);
        assertEquals(1, q.size());
    }

    @Test
    public void testRemainingCapacity() {
        var q = new BlockingLIFOQueue<String>();
        assertEquals(Integer.MAX_VALUE, q.remainingCapacity());
    }

    @Test
    public void testPollWithTimeout() throws InterruptedException {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        assertEquals("a", q.poll(1, TimeUnit.SECONDS));
        assertNull(q.poll(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testToArray() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        assertEquals(2, q.toArray().length);
        assertEquals(2, q.toArray(new String[0]).length);
    }

    @Test
    public void testIterator() {
        var q = new BlockingLIFOQueue<String>();
        q.add("a");
        q.add("b");
        q.add("c");
        var items = new ArrayList<String>();
        for (var s : q) items.add(s);
        assertEquals(List.of("c", "b", "a"), items);
    }
}
