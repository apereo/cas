package org.apereo.cas.web.flow.authentication;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
