package org.apereo.cas.adaptors.radius;

import module java.base;
import org.apereo.cas.util.LoggingUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jradius.packet.attribute.RadiusAttribute;
import org.apache.commons.lang3.tuple.Pair;

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
                    val attributes = response.attributes()
                        .stream()
                        .collect(Collectors.toMap(RadiusAttribute::getAttributeName, RadiusAttribute::getValue, (__, b) -> b, () -> new HashMap<String, Object>()));
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
                LoggingUtils.warn(LOGGER, "failoverOnException enabled -- trying next server.", e);
            }
        }
        return Pair.of(Boolean.FALSE, Optional.empty());
    }
}
