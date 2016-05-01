package org.jasig.cas.adaptors.radius.authentication;

import net.jradius.exception.TimeoutException;
import org.jasig.cas.adaptors.radius.RadiusServer;
import org.jasig.cas.adaptors.radius.RadiusUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.util.Pair;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.annotation.Resource;
import javax.security.auth.login.FailedLoginException;
import java.net.SocketTimeoutException;
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
@RefreshScope
@Component("radiusTokenAuthenticationHandler")
public class RadiusTokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    
    @Resource(name = "radiusTokenServers")
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
    public RadiusTokenAuthenticationHandler() {
        super();
        logger.debug("Using {}", getClass().getSimpleName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return RadiusTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        try {
            final RadiusTokenCredential radiusCredential = (RadiusTokenCredential) credential;
            final String password = radiusCredential.getToken();

            final RequestContext context = RequestContextHolder.getRequestContext();
            final String username = WebUtils.getAuthentication(context).getPrincipal().getId();

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
     * Can ping boolean.
     *
     * @return the boolean
     */
    public boolean canPing() {
        final String uidPsw = getClass().getSimpleName();
        for (final RadiusServer server : this.servers) {
            logger.debug("Attempting to ping RADIUS server {} via simulating an authentication request. If the server responds "
                    + "successfully, mock authentication will fail correctly.", server);
            try {
                server.authenticate(uidPsw, uidPsw);
            } catch (final TimeoutException | SocketTimeoutException e) {

                logger.debug("Server {} is not available", server);
                continue;
                
            } catch (final Exception e) {
                logger.debug("Pinging RADIUS server was successful. Response {}", e.getMessage());
            }
            return true;
        }
        return false;


    }
}
