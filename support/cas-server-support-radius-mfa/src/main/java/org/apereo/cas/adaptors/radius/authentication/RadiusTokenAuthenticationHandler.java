package org.apereo.cas.adaptors.radius.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.RadiusUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link RadiusTokenAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RadiusTokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    
    private final List<RadiusServer> servers;
    private final boolean failoverOnException;
    private final boolean failoverOnAuthenticationFailure;

    public RadiusTokenAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                            final PrincipalFactory principalFactory,
                                            final List<RadiusServer> servers,
                                            final boolean failoverOnException,
                                            final boolean failoverOnAuthenticationFailure) {
        super(name, servicesManager, principalFactory, null);
        this.servers = servers;
        this.failoverOnException = failoverOnException;
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;

        LOGGER.debug("Using [{}]", getClass().getSimpleName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return RadiusTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        try {
            final RadiusTokenCredential radiusCredential = (RadiusTokenCredential) credential;
            final String password = radiusCredential.getToken();

            final Authentication authentication = WebUtils.getInProgressAuthentication();
            if (authentication == null) {
                throw new IllegalArgumentException("CAS has no reference to an authentication event to locate a principal");
            }
            final Principal principal = authentication.getPrincipal();
            final String username = principal.getId();

            final Pair<Boolean, Optional<Map<String, Object>>> result =
                    RadiusUtils.authenticate(username, password, this.servers,
                            this.failoverOnAuthenticationFailure, this.failoverOnException);
            if (result.getKey()) {
                return createHandlerResult(credential,
                        this.principalFactory.createPrincipal(username, result.getValue().get()),
                        new ArrayList<>());
            }
            throw new FailedLoginException("Radius authentication failed for user " + username);
        } catch (final Exception e) {
            throw new FailedLoginException("Radius authentication failed " + e.getMessage());
        }
    }
}
