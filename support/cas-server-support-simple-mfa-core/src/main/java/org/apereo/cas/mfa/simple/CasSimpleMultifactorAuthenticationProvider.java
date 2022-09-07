package org.apereo.cas.mfa.simple;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;


/**
 * This is {@link CasSimpleMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@NoArgsConstructor
public class CasSimpleMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    @Serial
    private static final long serialVersionUID = 4789727148634156909L;

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), CasSimpleMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return true;
    }

    @Override
    public String getFriendlyName() {
        return "CAS Simple Multifactor Authentication";
    }
}

