package org.apereo.cas.mfa.accepto;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.AccepttoMultifactorProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;


/**
 * This is {@link AccepttoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@NoArgsConstructor
public class AccepttoMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 1234727148634156909L;

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), AccepttoMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return true;
    }

    @Override
    public String getFriendlyName() {
        return "CAS Acceptto Multifactor Authentication";
    }
}

