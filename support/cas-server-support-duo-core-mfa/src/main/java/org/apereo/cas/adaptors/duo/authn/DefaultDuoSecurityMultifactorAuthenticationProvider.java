package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;

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
@NoArgsConstructor
@RefreshScope
public class DefaultDuoSecurityMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider implements DuoSecurityMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    private String registrationUrl;

    private @NonNull DuoSecurityAuthenticationService duoAuthenticationService;

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return this.duoAuthenticationService.ping();
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), DuoSecurityMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public String getFriendlyName() {
        return "Duo Security";
    }
}
