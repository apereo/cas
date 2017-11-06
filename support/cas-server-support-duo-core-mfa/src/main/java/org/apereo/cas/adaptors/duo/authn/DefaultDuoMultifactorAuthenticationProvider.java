package org.apereo.cas.adaptors.duo.authn;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.Event;

/**
 * This is {@link DefaultDuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultDuoMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider implements DuoMultifactorAuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDuoMultifactorAuthenticationProvider.class);
    private static final long serialVersionUID = 4789727148634156909L;

    private String registrationUrl;
    
    private DuoSecurityAuthenticationService duoAuthenticationService;

    /**
     * Required for serialization purposes and reflection.
     */
    public DefaultDuoMultifactorAuthenticationProvider() {
    }

    public DefaultDuoMultifactorAuthenticationProvider(final DuoSecurityAuthenticationService duoAuthenticationService) {
        this.duoAuthenticationService = duoAuthenticationService;
    }

    @Override
    public DuoSecurityAuthenticationService getDuoAuthenticationService() {
        return this.duoAuthenticationService;
    }

    @Override
    protected boolean isAvailable() {
        Assert.notNull(this.duoAuthenticationService, "duoAuthenticationService cannot be null");
        return this.duoAuthenticationService.ping();
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), DuoSecurityMultifactorProperties.DEFAULT_IDENTIFIER);
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
        Assert.notNull(this.duoAuthenticationService, "duoAuthenticationService cannot be null");
        
        if (!super.supportsInternal(e, authentication, registeredService)) {
            return false;
        }

        final Principal principal = authentication.getPrincipal();
        final DuoUserAccount acct = this.duoAuthenticationService.getDuoUserAccount(principal.getId());
        LOGGER.debug("Found duo user account status [{}] for [{}]", acct, principal);

        if (acct.getStatus() == DuoUserAccountAuthStatus.ALLOW) {
            LOGGER.debug("Account status is set for allow/bypass for [{}]", principal);
            return false;
        }
        if (acct.getStatus() == DuoUserAccountAuthStatus.DENY) {
            LOGGER.warn("Account status is set to deny access to [{}]", principal);
        }

        return true;
    }

    @Override
    public String getFriendlyName() {
        return "Duo Security";
    }

    @Override
    public String getRegistrationUrl() {
        return registrationUrl;
    }

    public void setRegistrationUrl(final String registrationUrl) {
        this.registrationUrl = registrationUrl;
    }
}


