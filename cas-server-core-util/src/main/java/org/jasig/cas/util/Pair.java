package org.jasig.cas.util;

/**
 * This class contains a pair of objects.
 *
 * @author Jerome Leleu
 * @param <A> the generic type, first item
 * @param <B> the generic type, second item
 * @since 4.0.0
 */
public class Pair<A, B> {

    /** The first object of the pair. */
    private final A first;

    /** The second object of the pair. */
    private final B second;

    /**
     * Build a pair.
     *
     * @param first the first object of the pair.
     * @param second the second object of the pair.
     */
    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Return the first object of the pair.
     * @return the first object of the pair.
     */
    public final A getFirst() {
        return this.first;
    }

    /**
     * Return the second object of the pair.
     * @return the second object of the pair.
     */
    public final B getSecond() {
        return this.second;
    }
}
