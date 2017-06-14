package org.apereo.cas.util.function;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result and
 * could be with a Consumer of the resulting type.
 *
 * PLEASE REMOVE THIS IF IT'S ADDED TO THE JDK
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 *
 * @since 5.2.0
 */public interface ComposableFunction<T, R> extends Function<T, R> {

    default Consumer<T> andThen(Consumer<R> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.accept(apply(t));
    }
}
