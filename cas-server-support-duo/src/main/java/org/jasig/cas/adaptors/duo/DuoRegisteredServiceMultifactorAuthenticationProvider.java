package org.jasig.cas.adaptors.duo;

import org.jasig.cas.services.RegisteredServiceAuthenticationPolicy;
import org.jasig.cas.services.RegisteredServiceMultifactorAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is {@link DuoRegisteredServiceMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoAuthenticationProvider")
public class DuoRegisteredServiceMultifactorAuthenticationProvider implements RegisteredServiceMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Autowired
    @Qualifier("duoAuthenticationService")
    private DuoAuthenticationService duoAuthenticationService;

    private String id;

    @Override
    public String provide(final RegisteredServiceAuthenticationPolicy policy) {
        return null;
    }
}
