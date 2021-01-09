package org.apereo.cas.util.scripting;

import org.apereo.cas.util.DigestUtils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.DisposableBean;

import java.util.Set;

/**
 * This is {@link ScriptResourceCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface ScriptResourceCacheManager<K extends String, V extends ExecutableCompiledGroovyScript>
    extends AutoCloseable, DisposableBean {

    /**
     * Bean name for script resource cache manager.
     */
    String BEAN_NAME = "scriptResourceCacheManager";

    /**
     * Compute key.
     *
     * @param bits the bits
     * @return the key
     */
    static String computeKey(Pair<String, String> bits) {
        return DigestUtils.sha256(bits.getKey() + ':' + bits.getValue());
    }

    /**
     * Compute key string.
     *
     * @param key the key
     * @return the string
     */
    static String computeKey(final String key) {
        return DigestUtils.sha256(key);
    }

    /**
     * Get item.
     *
     * @param key the key
     * @return the item
     */
    V get(K key);

    /**
     * Contains key ?
     *
     * @param key the key
     * @return the boolean
     */
    boolean containsKey(K key);

    /**
     * Put script resource cache manager.
     *
     * @param key   the key
     * @param value the value
     * @return the script resource cache manager
     */
    ScriptResourceCacheManager<K, V> put(K key, V value);

    /**
     * Remove script resource cache manager.
     *
     * @param key the key
     * @return the script resource cache manager
     */
    ScriptResourceCacheManager<K, V> remove(K key);

    /**
     * Gets keys.
     *
     * @return the keys
     */
    Set<String> getKeys();

    /**
     * Clear items.
     *
     * @return the groovy script resource cache manager
     */
    @SneakyThrows
    default ScriptResourceCacheManager<K, V> clear() {
        close();
        return this;
    }

    @Override
    void close();

    @Override
    default void destroy() {
        close();
    }

    /**
     * Is cache empty?
     *
     * @return true/false
     */
    boolean isEmpty();

}
