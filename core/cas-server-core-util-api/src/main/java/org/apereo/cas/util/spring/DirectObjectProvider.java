package org.apereo.cas.util.spring;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import java.util.stream.Stream;


/**
 * This is {@link DirectObjectProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public record DirectObjectProvider<T>(T object) implements ObjectProvider<@NonNull T> {

    @Override
    public T getObject() throws BeansException {
        return object;
    }

    @Override
    public T getObject(final Object... objects) throws BeansException {
        return object;
    }

    @Override
    public T getIfAvailable() throws BeansException {
        return object;
    }

    @Override
    public T getIfUnique() throws BeansException {
        return object;
    }

    @Override
    public @NonNull Stream<T> stream() {
        return object == null
            ? Stream.empty()
            : Stream.of(object);
    }

    /**
     * Empty direct object provider.
     *
     * @return the direct object provider
     */
    public static DirectObjectProvider empty() {
        return new DirectObjectProvider<>(null);
    }
}
