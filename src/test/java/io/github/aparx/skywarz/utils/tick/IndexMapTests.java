package io.github.aparx.skywarz.utils.tick;

import io.github.aparx.skywarz.utils.array.IndexMap;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-18 10:55
 * @since 1.0
 */
public class IndexMapTests {

  @Test
  @Order(1 + Order.DEFAULT)
  public void size() {
    IndexMap<String> map = new IndexMap<>();
    map.putAll(new String[]{"a", "b", "c", "d", "e"});
    Assertions.assertEquals(5, map.size());
    map.remove(1);
    Assertions.assertEquals(4, map.size());
    map.remove(3);
    Assertions.assertEquals(3, map.size());
    map.clear();
    Assertions.assertEquals(0, map.size());
    Assertions.assertEquals(IndexMap.DEFAULT_INITIAL_CAPACITY, map.capacity());
  }

  @Test
  public void put() {
    IndexMap<String> map = new IndexMap<>();
    map.put(IndexMap.Entry.of(0, "hello"));
    map.put(IndexMap.Entry.of(1, "world"));
    Assertions.assertEquals("hello", map.get(0));
    Assertions.assertEquals("world", map.get(1));
    Assertions.assertEquals("hello", map.put(0, "hello"));
    Assertions.assertEquals("world", map.put(1, "world"));
    Assertions.assertEquals("hello", map.get(0));
    Assertions.assertEquals("world", map.get(1));
    Assertions.assertEquals("hello", map.put(0, "hello"));
    Assertions.assertEquals("world", map.remove(1));
    Assertions.assertNull(map.put(2, "world"));
    Assertions.assertEquals("hello", map.get(0));
    Assertions.assertEquals("world", map.get(2));
    Assertions.assertNull(map.get(1));
  }

  @Test
  public void putAll() {
    IndexMap<String> map = new IndexMap<>();
    map.put(0, "hello");
    map.put(3, "world");
    map.putAll(new String[]{"a", "b", "c"});
    Assertions.assertEquals("a", map.get(0));
    Assertions.assertEquals("b", map.get(1));
    Assertions.assertEquals("c", map.get(2));
    Assertions.assertEquals("world", map.get(3));

    map.putAll(Map.of(0, "hello", 3, "there"));
    Assertions.assertEquals("hello", map.get(0));
    Assertions.assertEquals("b", map.get(1));
    Assertions.assertEquals("c", map.get(2));
    Assertions.assertEquals("there", map.get(3));
  }

  @Test
  public void contains() {
    IndexMap<String> map = new IndexMap<>();
    map.put(0, "a");
    map.put(1, "b");
    map.put(2, "c");
    map.put(3, null);
    map.put(5, null);
    Assertions.assertTrue(map.contains(0, "a"));
    Assertions.assertFalse(map.contains(0, "b"));
    Assertions.assertTrue(map.contains(1, "b"));
    Assertions.assertFalse(map.contains(1, "c"));
    Assertions.assertTrue(map.contains(2, "c"));
    Assertions.assertFalse(map.contains(3, "d"));
    Assertions.assertTrue(map.contains(3, null));
    Assertions.assertFalse(map.contains(4, null));
    Assertions.assertTrue(map.contains(5, null));
    Assertions.assertFalse(map.contains(6, "a"));
  }

  @Test
  public void containsKey() {
    IndexMap<String> map = new IndexMap<>();
    map.put(0, "a");
    map.put(1, "b");
    map.put(2, "c");
    map.put(3, null);
    map.put(5, null);
    Assertions.assertTrue(map.containsKey(0));
    Assertions.assertTrue(map.containsKey(1));
    Assertions.assertTrue(map.containsKey(2));
    Assertions.assertTrue(map.containsKey(3));
    Assertions.assertFalse(map.containsKey(4));
    Assertions.assertTrue(map.containsKey(5));
    Assertions.assertFalse(map.containsKey(6));
    Assertions.assertFalse(map.containsKey(7));
  }

  @Test
  public void containsValue() {
    IndexMap<String> map = new IndexMap<>();
    map.put(0, "a");
    map.put(1, "b");
    map.put(2, "c");
    map.put(3, null);
    map.put(5, null);
    Assertions.assertTrue(map.containsValue("a"));
    Assertions.assertTrue(map.containsValue("b"));
    Assertions.assertTrue(map.containsValue("c"));
    Assertions.assertTrue(map.containsValue(null));
    Assertions.assertFalse(map.containsValue("d"));
  }

  @Test
  public void indexOf() {
    IndexMap<String> map = new IndexMap<>();
    map.put(0, "a");
    map.put(1, "b");
    map.put(2, "c");
    map.put(3, null);
    map.put(5, null);
    Assertions.assertEquals(0, map.indexOf("a"));
    Assertions.assertEquals(1, map.indexOf("b"));
    Assertions.assertEquals(2, map.indexOf("c"));
    Assertions.assertEquals(3, map.indexOf(null));
    Assertions.assertEquals(-1, map.indexOf("d"));
  }

  @Test
  public void lastIndexOf() {
    IndexMap<String> map = new IndexMap<>();
    map.put(0, "a");
    map.put(1, "a");
    map.put(2, "b");
    map.put(3, "b");
    map.put(4, "c");
    map.put(5, "c");
    map.put(6, null);
    map.put(7, null);
    Assertions.assertEquals(1, map.lastIndexOf("a"));
    Assertions.assertEquals(3, map.lastIndexOf("b"));
    Assertions.assertEquals(5, map.lastIndexOf("c"));
    Assertions.assertEquals(7, map.lastIndexOf(null));
    Assertions.assertEquals(-1, map.lastIndexOf("d"));
  }


}
