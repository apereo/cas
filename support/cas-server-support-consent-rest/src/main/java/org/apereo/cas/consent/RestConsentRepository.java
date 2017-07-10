package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link RestConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RestConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 6583408862493270206L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RestConsentRepository.class);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        try {
            return null;
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        try {
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
