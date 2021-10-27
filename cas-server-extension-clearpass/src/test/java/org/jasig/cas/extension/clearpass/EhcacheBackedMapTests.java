package org.jasig.cas.extension.clearpass;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is tests for
 * {@link org.jasig.cas.extension.clearpass.EhcacheBackedMap}.
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 3.5.0
 */
public class EhcacheBackedMapTests {

    private CacheManager manager;
    private Cache cache;
    private EhcacheBackedMap map;

    @Before
    public void prep() {
        this.manager = CacheManager.create();
        this.manager.addCache("cascache");
        this.cache = this.manager.getCache("cascache");
        this.map = new EhcacheBackedMap(this.cache);
    }

    @After
    public void shutdown() {
        this.manager.shutdown();
    }

    @Test
    public void verifyEmptyMapSize() {
        assertEquals(map.size(), 0);
        assertTrue(map.isEmpty());
    }

    @Test
    public void verifyGetPutOps() {
        this.map.put("key", "value");
        assertNotNull(this.map.get("key"));

        this.map.remove("key");
        assertTrue(map.isEmpty());
    }

    @Test
    public void verifyPutClear() {
        final String[][] arrayItems = {{"key0", "Item0"}, {"key1", "Item1"}, {"key2", "Item2"}};
        final Map mapItems = ArrayUtils.toMap(arrayItems);

        this.map.putAll(mapItems);
        assertEquals(map.size(), 3);

        this.map.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void verifyKeysValues() {
        final String[][] arrayItems = {{"key0", "Item0"}, {"key1", "Item1"}, {"key2", "Item2"}};
        final Map mapItems = ArrayUtils.toMap(arrayItems);

        this.map.putAll(mapItems);
        assertEquals(map.keySet().size(), 3);
        assertEquals(map.values().size(), 3);
    }

    @Test
    public void verifyContains() {
        final String[][] arrayItems = {{"key0", "Item0"}, {"key1", "Item1"}, {"key2", "Item2"}};
        final Map mapItems = ArrayUtils.toMap(arrayItems);

        this.map.putAll(mapItems);
        assertTrue(map.containsKey("key2"));
        assertTrue(map.containsValue("Item1"));
    }
}
