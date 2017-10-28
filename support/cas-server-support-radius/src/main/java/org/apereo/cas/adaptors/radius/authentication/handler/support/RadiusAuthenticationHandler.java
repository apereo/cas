package org.apereo.cas.adaptors.radius.authentication.handler.support;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.RadiusUtils;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Handler to authenticate a user against a RADIUS server.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class RadiusAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RadiusAuthenticationHandler.class);

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

    /**
     * Instantiates a new Radius authentication handler.
     *
     * @param name                            the name
     * @param servicesManager                 the services manager
     * @param principalFactory                the principal factory
     * @param servers                         RADIUS servers to authenticate against.
     * @param failoverOnException             boolean on whether to failover or not.
     * @param failoverOnAuthenticationFailure boolean on whether to failover or not.
     */
    public RadiusAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                       final List<RadiusServer> servers, final boolean failoverOnException, final boolean failoverOnAuthenticationFailure) {
        super(name, servicesManager, principalFactory, null);
        LOGGER.debug("Using [{}]", getClass().getSimpleName());

        this.servers = servers;
        this.failoverOnException = failoverOnException;
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential, final String originalPassword)
            throws GeneralSecurityException {

        try {
            final String username = credential.getUsername();
            final Pair<Boolean, Optional<Map<String, Object>>> result =
                    RadiusUtils.authenticate(username, credential.getPassword(), this.servers,
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
