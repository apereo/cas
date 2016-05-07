package org.apereo.cas.adaptors.radius;

import net.jradius.packet.attribute.RadiusAttribute;
import org.apereo.cas.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link RadiusUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class RadiusUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(RadiusUtils.class);

    private RadiusUtils() {}
    
    /**
     * Authenticate pair.
     *
     * @param username                        the username
     * @param password                        the password
     * @param servers                         the servers
     * @param failoverOnAuthenticationFailure the failover on authentication failure
     * @param failoverOnException             the failover on exception
     * @return the pair
     * @throws Exception the exception
     */
    public static Pair<Boolean, Optional<Map<String, Object>>> authenticate(final String username, final String password,
                                                                            final List<RadiusServer> servers,
                                                                            final boolean failoverOnAuthenticationFailure,
                                                                            final boolean failoverOnException) 
                            throws Exception {
        for (final RadiusServer radiusServer : servers) {
            LOGGER.debug("Attempting to authenticate {} at {}", username, radiusServer);
            try {
                final RadiusResponse response = radiusServer.authenticate(username, password);
                if (response != null) {
                    final Map<String, Object> attributes = new HashMap<>();
                    for (final RadiusAttribute attribute : response.getAttributes()) {
                        attributes.put(attribute.getAttributeName(), attribute.getValue().toString());
                    }
                    return new Pair<>(Boolean.TRUE, Optional.of(attributes));
                }

                if (!failoverOnAuthenticationFailure) {
                    throw new FailedLoginException("Radius authentication failed for user " + username);
                }
                LOGGER.debug("failoverOnAuthenticationFailure enabled -- trying next server");
            } catch (final Exception e) {
                if (!failoverOnException) {
                    throw e;
                }
                LOGGER.warn("failoverOnException enabled -- trying next server.", e);
            }
        }
        return new Pair<>(Boolean.TRUE, Optional.empty());
    }
}
