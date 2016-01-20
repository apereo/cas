package org.jasig.cas.adaptors.duo;

import org.jasig.cas.adaptors.duo.web.flow.DuoMultifactorWebflowConfigurer;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is {@link DuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoAuthenticationProvider")
public class DuoMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("duoAuthenticationService")
    private DuoAuthenticationService duoAuthenticationService;

    @Override
    public String buildIdentifier(final RegisteredService service) throws AuthenticationException {
        if (duoAuthenticationService.canPing()) {
            return DuoMultifactorWebflowConfigurer.MFA_DUO_EVENT_ID;
        } else if (service.getAuthenticationPolicy().isFailOpen()) {
            logger.warn("Duo could not be reached. Since the authentication provider is configured to fail-open, authentication will "
                    + "proceed without Duo for service {}", service.getServiceId());
            return null;
        }
        throw new AuthenticationException();
    }
}
