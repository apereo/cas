package org.apereo.cas.adaptors.radius.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.adaptors.radius.AccessChallengedException;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.RadiusUtils;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.FailedLoginException;

import java.io.Serializable;
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
        final RadiusTokenCredential radiusCredential = (RadiusTokenCredential) credential;
        final String password = radiusCredential.getToken();

        final RequestContext context = RequestContextHolder.getRequestContext();
        final String username = WebUtils.getAuthentication(context).getPrincipal().getId();

        final Pair<Boolean, Optional<Map<String, Object>>> result;
        try {
            final Serializable state = radiusCredential.getState();
            radiusCredential.setState(null);
            result = RadiusUtils.authenticate(username, password, state, this.servers,
                    this.failoverOnAuthenticationFailure, this.failoverOnException);
        } catch (final Exception e) {
            throw new FailedLoginException("Radius authentication failed " + e.getMessage());
        }
        if (result.getLeft()) {
            return createHandlerResult(credential, this.principalFactory.createPrincipal(username, result.getRight().get()),
                    new ArrayList<>());
        } else if (result.getRight().isPresent()) {
            final Serializable state = (Serializable) result.getRight().get().getOrDefault("State", null);
            radiusCredential.setState(state);
            final String message = result.getRight().get().getOrDefault("Reply-Message", "?").toString();
            radiusCredential.setMessage(message);
            throw new AccessChallengedException(message);
        }
        throw new FailedLoginException("Radius authentication failed for user " + username);
    }
}
