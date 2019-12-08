package org.apereo.cas.adaptors.radius.authentication.handler.support;

import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.RadiusUtils;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Authentication Handler to authenticate a user against a RADIUS server.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public class RadiusAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    
    /**
     * Array of RADIUS servers to authenticate against.
     */
    private final List<RadiusServer> servers;

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an exception.
     */
    private final boolean failoverOnException;

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an authentication failure.
     */
    private final boolean failoverOnAuthenticationFailure;

    public RadiusAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                       final List<RadiusServer> servers, final boolean failoverOnException, final boolean failoverOnAuthenticationFailure) {
        super(name, servicesManager, principalFactory, null);
        this.servers = servers;
        this.failoverOnException = failoverOnException;
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) throws GeneralSecurityException {

        try {
            val username = credential.getUsername();
            val result = RadiusUtils.authenticate(username, credential.getPassword(), this.servers,
                this.failoverOnAuthenticationFailure, this.failoverOnException, Optional.empty());
            if (result.getKey() && result.getValue().isPresent()) {
                val attributes = CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(result.getValue().get());
                return createHandlerResult(credential,
                    principalFactory.createPrincipal(username, attributes),
                    new ArrayList<>(0));
            }
            throw new FailedLoginException("Radius authentication failed for user " + username);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FailedLoginException("Radius authentication failed " + e.getMessage());
        }
    }
}
