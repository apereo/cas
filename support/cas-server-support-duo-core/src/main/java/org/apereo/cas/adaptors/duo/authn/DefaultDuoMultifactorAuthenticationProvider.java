package org.apereo.cas.adaptors.duo.authn;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;

/**
 * This is {@link DefaultDuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultDuoMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider
        implements DuoMultifactorAuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDuoMultifactorAuthenticationProvider.class);
    private static final long serialVersionUID = 4789727148634156909L;

    private final DuoAuthenticationService duoAuthenticationService;

    public DefaultDuoMultifactorAuthenticationProvider(final DuoAuthenticationService duoAuthenticationService) {
        this.duoAuthenticationService = duoAuthenticationService;
    }

    @Override
    public DuoAuthenticationService getDuoAuthenticationService() {
        return this.duoAuthenticationService;
    }

    @Override
    protected boolean isAvailable() {
        return this.duoAuthenticationService.ping();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final DefaultDuoMultifactorAuthenticationProvider rhs = (DefaultDuoMultifactorAuthenticationProvider) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(duoAuthenticationService, rhs.duoAuthenticationService)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(duoAuthenticationService)
                .toHashCode();
    }

    @Override
    protected boolean supportsInternal(final Event e, final Authentication authentication, final RegisteredService registeredService) {
        if (!super.supportsInternal(e, authentication, registeredService)) {
            return false;
        }

        final Principal principal = authentication.getPrincipal();
        final DuoUserAccountAuthStatus acct = this.duoAuthenticationService.getDuoUserAccountAuthStatus(principal.getId());
        LOGGER.debug("Found duo user account status [{}] for [{}]", acct, principal);

        if (acct == DuoUserAccountAuthStatus.ALLOW) {
            LOGGER.debug("Account status is set for allow/bypass for [{}]", principal);
            return false;
        }
        if (acct == DuoUserAccountAuthStatus.DENY) {
            LOGGER.warn("Account status is set to deny access to [{}]", principal);
        }

        return true;
    }
}


