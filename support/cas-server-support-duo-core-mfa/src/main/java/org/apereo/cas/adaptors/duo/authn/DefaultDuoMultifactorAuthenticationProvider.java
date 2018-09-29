package org.apereo.cas.adaptors.duo.authn;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.RegisteredService;
import org.springframework.util.Assert;

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
    public boolean isAvailable(final RegisteredService service) {
        Assert.notNull(this.duoAuthenticationService, "duoAuthenticationService cannot be null");
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
