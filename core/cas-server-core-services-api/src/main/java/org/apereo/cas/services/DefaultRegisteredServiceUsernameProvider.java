package org.apereo.cas.services;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import lombok.NoArgsConstructor;

/**
 * Resolves the username for the service to be the default principal id.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DefaultRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = 5823989148794052951L;

    public DefaultRegisteredServiceUsernameProvider(final String canonicalizationMode) {
        super(canonicalizationMode, false);
    }

    @Override
    public String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        LOGGER.debug("Returning the default principal id [{}] for username.", principal.getId());
        return principal.getId();
    }
}
