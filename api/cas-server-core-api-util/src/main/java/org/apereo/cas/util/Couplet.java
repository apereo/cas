package org.apereo.cas.util;

import module java.base;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link Couplet}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record Couplet<L, R>(@Nullable L key, @Nullable R value) implements Serializable {

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
    public @Nullable L getLeft() {
        return key;
    }

    /**
     * Gets right.
     *
     * @return the right
     */
    public @Nullable R getRight() {
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

    /**
     * Right couplet.
     *
     * @param <L>   the type parameter
     * @param <R>   the type parameter
     * @param value the value
     * @return the couplet
     */
    public static <L, R> Couplet<L, R> right(final R value) {
        return new Couplet<>(null, value);
    }

    /**
     * Left couplet.
     *
     * @param <L> the type parameter
     * @param <R> the type parameter
     * @param key the key
     * @return the couplet
     */
    public static <L, R> Couplet<L, R> left(final L key) {
        return new Couplet<>(key, null);
    }

    /**
     * Contains left value?.
     *
     * @return the boolean
     */
    public boolean hasLeft() {
        return key != null;
    }

    /**
     * Has key been defined?.
     *
     * @return true/false
     */
    public boolean hasKey() {
        return hasLeft();
    }
    
    /**
     * Contains right value?.
     *
     * @return true/false
     */
    public boolean hasRight() {
        return value != null;
    }

    /**
     * Contains value?.
     *
     * @return true/false
     */
    public boolean hasValue() {
        return hasRight();
    }
}
