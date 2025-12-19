package org.apereo.cas.jpa;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link AbstractJpaEntityFactory}.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 6.2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractJpaEntityFactory<T> implements JpaEntityFactory<T> {
    private final String dialect;
    
    /**
     * New document.
     *
     * @return the document
     */
    public T newInstance() {
        return FunctionUtils.doUnchecked(() -> getType().getDeclaredConstructor().newInstance());
    }
}
