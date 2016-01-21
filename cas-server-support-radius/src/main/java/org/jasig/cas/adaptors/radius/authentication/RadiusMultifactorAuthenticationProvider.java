package org.jasig.cas.adaptors.radius.authentication;

import org.jasig.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.jasig.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAuthenticationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The authentication provider for yubikey.
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("radiusAuthenticationProvider")
public class RadiusMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${cas.radius.rank:0}")
    private int rank;

    @Autowired
    @Qualifier("radiusAuthenticationHandler")
    private RadiusAuthenticationHandler radiusAuthenticationHandler;

    @Override
    public boolean verify(final RegisteredService service) throws AuthenticationException {
        if (radiusAuthenticationHandler.canPing()) {
            return true;
        }

        final RegisteredServiceAuthenticationPolicy policy = service.getAuthenticationPolicy();
        if (policy != null && policy.isFailOpen()) {
            logger.warn("RADIUS servers could not be reached. Since the authentication provider is configured to fail-open, "
                    + "authentication will proceed without RADIUS for service {}", service.getServiceId());
            return false;
        }

        throw new AuthenticationException();
    }

    @Override
    public String getId() {
        return RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return this.rank;
    }
}
