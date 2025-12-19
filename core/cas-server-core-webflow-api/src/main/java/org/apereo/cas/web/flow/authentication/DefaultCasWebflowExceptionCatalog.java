package org.apereo.cas.web.flow.authentication;

import module java.base;
import lombok.Getter;

/**
 * This is {@link DefaultCasWebflowExceptionCatalog}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
public class DefaultCasWebflowExceptionCatalog implements CasWebflowExceptionCatalog {
    private final Set<Class<? extends Throwable>> registeredExceptions = new LinkedHashSet<>();

    @Override
    public void registerException(final Class<? extends Throwable> throwable) {
        registeredExceptions.add(throwable);
    }

    @Override
    public void registerExceptions(final Collection<Class<? extends Throwable>> throwable) {
        registeredExceptions.addAll(throwable);
    }
}
