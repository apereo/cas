package org.apereo.cas.services.query;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceIndexService;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * This is {@link BaseRegisteredServiceIndexService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseRegisteredServiceIndexService implements RegisteredServiceIndexService {
    protected final List<ServicesManagerRegisteredServiceLocator> registeredServiceLocators;
    protected final CasConfigurationProperties casProperties;

    @Override
    public boolean isEnabled() {
        return casProperties.getServiceRegistry().getCore().isIndexServices();
    }
}

