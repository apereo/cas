package org.apereo.cas.adaptors.duo.authn;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.adaptors.duo.DuoIntegration;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;

import java.util.Optional;

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

    private DuoAuthenticationService duoAuthenticationService;

    public void setDuoAuthenticationService(final DuoAuthenticationService duoAuthenticationService) {
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
    protected boolean supportsInternal(final Event e, final Authentication authentication,
                                       final RegisteredService registeredService) {
        if (super.supportsInternal(e, authentication, registeredService)) {
            final Principal principal = authentication.getPrincipal();
            final Optional<DuoUserAccount> acct = this.duoAuthenticationService.getDuoUserAccount(principal.getId());
            if (acct.isPresent()) {

                if (acct.get().isAccountStatusBypass()) {
                    LOGGER.debug("Found duo user account for {}; Account status is set for bypass", principal);
                    return false;
                }

                LOGGER.debug("Found duo user account for {}; Account status is eligible for multifactor authentication", principal);
                return true;
            }

            LOGGER.debug("Could not locate duo user account for {}. Checking enrollment policy...", principal);
            final Optional<DuoIntegration> policy = this.duoAuthenticationService.getDuoIntegrationPolicy();
            if (policy.isPresent()) {
                if (policy.get().isEnrollmentStatusBypass()) {
                    LOGGER.debug("Duo integration is set to bypass new-user enrollment for {}", principal);
                    return false;
                } else {
                    LOGGER.debug("Duo integration requires user account registration for {}", principal);
                }
            } else {
                LOGGER.debug("Duo integration policy could not be retrieved", principal);
            }
        }

        return true;
    }
}


