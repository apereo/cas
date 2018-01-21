package org.apereo.cas.adaptors.duo.authn;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.RegisteredService;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.Event;

/**
 * This is {@link DefaultDuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class DefaultDuoMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider implements DuoMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    private String registrationUrl;

    @NonNull
    private DuoSecurityAuthenticationService duoAuthenticationService;

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
    protected boolean supportsInternal(final Event e, final Authentication authentication, final RegisteredService registeredService) {
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
}
