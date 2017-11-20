package org.apereo.cas.util.function;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a supplier of results that could be chained with a Consumer.
 *
 * <p>There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 *
 * @author David Rodriguez
 *
 * @param <T> the type of results supplied by this supplier
 *
 * @since 5.2.0
 */
@FunctionalInterface
public interface ComposableSupplier<T> extends Supplier<T> {

    /**
     * Pass the supplied result to the Consumer.
     *
     * @param after Consumer that will receive the supplied result
     *
     * @return after consumer with the supplied result passed in
     */
    default Consumer<T> andThen(final Consumer<T> after) {
        Objects.requireNonNull(after);
        return t -> after.accept(get());
    }
}
