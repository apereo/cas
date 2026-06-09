package org.apereo.cas.util;

import module java.base;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This is {@link Couplet}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record Couplet<L, R>(L key, R value) implements Serializable {

    /**
     * To pair object.
     *
     * @return the pair
     */
    public Pair toPair() {
        return Pair.of(key, value);
    }

    /**
     * Gets left.
     *
     * @return the left
     */
    public L getLeft() {
        return key;
    }

    /**
     * Gets right.
     *
     * @return the right
     */
    public R getRight() {
        return value;
    }
    
    /**
     * Create a couplet.
     *
     * @param key   the key
     * @param value the value
     * @return the object
     */
    public static <L, R> Couplet of(final L key, final R value) {
        return new Couplet(key, value);
    }
}
