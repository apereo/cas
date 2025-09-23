package org.apereo.cas.services.query;

import org.apereo.cas.services.RegisteredService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

/**
 * This is {@link RegisteredServiceQuery}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuppressWarnings("ClassWithOnlyPrivateConstructors")
@Getter
@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
@With
public class RegisteredServiceQuery<T> {
    private final Class<? extends RegisteredService> type;

    private final String name;

    private final T value;

    private boolean includeAssignableTypes;
}
