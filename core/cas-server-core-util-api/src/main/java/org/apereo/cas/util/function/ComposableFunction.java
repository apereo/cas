package org.apereo.cas.util.function;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result and
 * could be with a Consumer of the resulting type.
 *
 * @author David Rodriguez
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 *
 * @since 5.2.0
 */
@FunctionalInterface
public interface ComposableFunction<T, R> extends Function<T, R> {

    /**
     * Chain this function with a Consumer that expects the same type.
     *
     * @param after Consumer that will receive the output of the function
     *
     * @return after consumer with the output of the function passed in
     */
    default Consumer<T> andThen(final Consumer<R> after) {
        Objects.requireNonNull(after);
        return t -> after.accept(apply(t));
    }
}
