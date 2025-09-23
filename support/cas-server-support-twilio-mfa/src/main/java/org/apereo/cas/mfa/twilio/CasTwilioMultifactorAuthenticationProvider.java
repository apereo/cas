package org.apereo.cas.mfa.twilio;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.twilio.CasTwilioMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredService;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import java.io.Serial;

/**
 * This is {@link CasTwilioMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@NoArgsConstructor
public class CasTwilioMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    @Serial
    private static final long serialVersionUID = 4189727148634156909L;

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), CasTwilioMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return true;
    }

    @Override
    public String getFriendlyName() {
        return "CAS Twilio Multifactor Authentication";
    }
}

