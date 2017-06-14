package org.apereo.cas.util.function;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a supplier of results that could be chained with a Consumer.
 *
 * PLEASE REMOVE THIS IF IT'S ADDED TO THE JDK
 *
 * <p>There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #get()}.
 *
 * @param <T> the type of results supplied by this supplier
 *
 * @since 5.2.0
 */
public interface ComposableSupplier<T> extends Supplier<T> {

    default Consumer<T> andThen(Consumer<T> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.accept(get());
    }
}
