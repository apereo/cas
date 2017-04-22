package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link JdbcConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JdbcConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 6599908862493270206L;

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        return null;
    }
}
