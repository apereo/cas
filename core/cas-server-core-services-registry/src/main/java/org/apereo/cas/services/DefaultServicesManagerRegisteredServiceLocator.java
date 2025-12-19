package org.apereo.cas.services;

import module java.base;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This is {@link DefaultServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@NoArgsConstructor
public class DefaultServicesManagerRegisteredServiceLocator extends BaseServicesManagerRegisteredServiceLocator {

    @Override
    protected Pair<String, Class<? extends RegisteredService>> getRegisteredServiceIndexedType() {
        return Pair.of(CasRegisteredService.FRIENDLY_NAME, CasRegisteredService.class);
    }
}
