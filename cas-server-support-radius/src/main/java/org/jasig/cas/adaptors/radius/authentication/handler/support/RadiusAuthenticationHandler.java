package org.jasig.cas.adaptors.radius.authentication.handler.support;

import org.jasig.cas.adaptors.radius.RadiusServer;
import org.jasig.cas.adaptors.radius.RadiusUtils;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
@Component("radiusAuthenticationHandler")
public class RadiusAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /** Array of RADIUS servers to authenticate against. */
    @NotNull
    @Size(min=1)
    @Resource(name="radiusServers")
    private List<RadiusServer> servers;

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an exception.
     */
    @Value("${cas.radius.failover.authn:false}")
    private boolean failoverOnException;

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an authentication failure.
     */
    @Value("${cas.radius.failover.exception:false}")
    private boolean failoverOnAuthenticationFailure;

    /**
     * Instantiates a new Radius authentication handler.
     */
    public RadiusAuthenticationHandler() {
        super();
        logger.debug("Using {}", getClass().getSimpleName());
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        try {
            final String password = getPasswordEncoder().encode(credential.getPassword());
            final String username = credential.getUsername();

            final Pair<Boolean, Optional<Map<String, Object>>> result =
                    RadiusUtils.authenticate(username, password, this.servers, 
                            this.failoverOnAuthenticationFailure, this.failoverOnException);
            if (result.getFirst()) {
                return createHandlerResult(credential, this.principalFactory.createPrincipal(username, result.getSecond().get()),
                        new ArrayList<>());
            }
            throw new FailedLoginException("Radius authentication failed for user " + username);
        } catch (final Exception e) {
            throw new FailedLoginException("Radius authentication failed " + e.getMessage());
        }
    }

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an authentication failure.
     *
     * @param failoverOnAuthenticationFailure boolean on whether to failover or
     * not.
     */
    public void setFailoverOnAuthenticationFailure(
            final boolean failoverOnAuthenticationFailure) {
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
    }

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an exception.
     *
     * @param failoverOnException boolean on whether to failover or not.
     */
    public void setFailoverOnException(final boolean failoverOnException) {
        this.failoverOnException = failoverOnException;
    }

    public void setServers(final List<RadiusServer> servers) {
        this.servers = servers;
    }
    
}
