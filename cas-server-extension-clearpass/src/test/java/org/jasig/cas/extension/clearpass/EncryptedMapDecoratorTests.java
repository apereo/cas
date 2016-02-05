package org.jasig.cas.extension.clearpass;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 1.0.6
 */
public class EncryptedMapDecoratorTests {

    private Map<String, String> map;

    private EncryptedMapDecorator decorator;

    private CacheManager cacheManager;

    @Before
    public void setUp() throws Exception {
        try {
            this.cacheManager = new CacheManager(this.getClass().getClassLoader()
                    .getResourceAsStream("ehcacheClearPass.xml"));
            final Cache cache = this.cacheManager.getCache("clearPassCache");
            this.map = new EhcacheBackedMap(cache);
            this.decorator = new EncryptedMapDecorator(map);
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        this.cacheManager.removalAll();
        this.cacheManager.shutdown();
    }

    @Test
    public void addItem() {
        final String key = "MY_KEY";
        final String value = "MY_VALUE";
        this.decorator.put(key, value);
        assertEquals(value, this.decorator.get(key));
        assertNull(this.map.get(key));
    }

    @Test
    public void addManyItems() {
        final int totalItems = 100;

        IntStream.range(0, totalItems).forEach(i -> this.decorator.put("key" + i, "value" + i));

        assertEquals(this.decorator.size(), totalItems);

        IntStream.range(0, totalItems).forEach(i -> {
            assertNull(this.map.get("key" + i));
            assertEquals("value" + i, this.decorator.get("key" + i));
        });
    }

    @Test
    public void addAndRemoveItem() {
        final String key1 = "MY_REALLY_KEY";
        final String value1 = "MY_VALUE";
        final String key2 = "MY_KEY2";
        final String value2 = "MY_VALUE2";

        this.decorator.put(key1, value1);
        this.decorator.put(key2, value2);
        assertEquals(value1, this.decorator.get(key1));
        assertEquals(value2, this.decorator.get(key2));
        assertNull(this.map.get(key1));
        assertNull(this.map.get(key2));

        assertEquals(value1, this.decorator.remove(key1));
        assertEquals(value2, this.decorator.remove(key2));

        assertNull(this.decorator.get(key1));
        assertNull(this.decorator.get(key2));
    }

    @Test
    public void addNullKeyAndValue() {
        this.decorator.put(null, null);
        assertNull(this.decorator.get(null));
    }

    @Test
    public void addNullValue() {
        this.decorator.put("hello", null);
        assertNull(this.decorator.get("hello"));
    }
}
