package org.apereo.cas.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link DirectObjectProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class DirectObjectProvider<T> implements ObjectProvider<T> {
    private final T object;

    @Override
    public T getIfAvailable() throws BeansException {
        return object;
    }

    @Override
    public T getIfUnique() throws BeansException {
        return object;
    }

    @Override
    public T getObject() throws BeansException {
        return object;
    }
    
    @Override
    public T getObject(final Object... objects) throws BeansException {
        return object;
    }
}
