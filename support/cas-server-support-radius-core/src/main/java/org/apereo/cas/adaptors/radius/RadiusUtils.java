package org.apereo.cas.adaptors.radius;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

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
@Slf4j
@UtilityClass
public class RadiusUtils {

    /**
     * Authenticate pair.
     *
     * @param username                        the username
     * @param password                        the password
     * @param servers                         the servers
     * @param failoverOnAuthenticationFailure the failover on authentication failure
     * @param failoverOnException             the failover on exception
     * @param state                           the state
     * @return the pair
     * @throws Exception the exception
     */
    public static Pair<Boolean, Optional<Map<String, Object>>> authenticate(final String username,
                                                                            final String password,
                                                                            final List<RadiusServer> servers,
                                                                            final boolean failoverOnAuthenticationFailure,
                                                                            final boolean failoverOnException,
                                                                            final Optional state) throws Exception {
        for (val radiusServer : servers) {
            LOGGER.debug("Attempting to authenticate [{}] at [{}]", username, radiusServer);
            try {
                val response = radiusServer.authenticate(username, password, state);
                if (response != null) {
                    val attributes = new HashMap<String, Object>();
                    response.getAttributes().forEach(attribute -> attributes.put(attribute.getAttributeName(), attribute.getValue()));
                    return Pair.of(Boolean.TRUE, Optional.of(attributes));
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
        return Pair.of(Boolean.FALSE, Optional.empty());
    }
}
