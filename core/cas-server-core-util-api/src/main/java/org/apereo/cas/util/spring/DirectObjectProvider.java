package org.apereo.cas.util.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

import jakarta.annotation.Nonnull;


/**
 * This is {@link DirectObjectProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public record DirectObjectProvider<T>(T object) implements ObjectProvider<T> {

    @Override
    public T getObject() throws BeansException {
        return object;
    }

    @Override
    public T getObject(
        @Nonnull final Object... objects) throws BeansException {
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
}
