package org.apereo.cas.adaptors.duo.authn;

import module java.base;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationRegistrationProperties;
import org.apereo.cas.services.RegisteredService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DefaultDuoSecurityMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
public class DefaultDuoSecurityMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider
    implements DuoSecurityMultifactorAuthenticationProvider {

    @Serial
    private static final long serialVersionUID = 4789727148634156909L;

    private DuoSecurityMultifactorAuthenticationRegistrationProperties registration;

    private @NonNull DuoSecurityAuthenticationService duoAuthenticationService;

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return this.duoAuthenticationService.ping();
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public String getFriendlyName() {
        return StringUtils.defaultIfBlank(duoAuthenticationService.getProperties().getName(), "Duo Security");
    }
}
