package org.apereo.cas.configuration.model.support.cookie;

import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * Base property class for cookies that can be pinned to the HTTP session.
 * Pinned cookies are ignored if they arrive on a request with different attributes, such as IP address or user-agent,
 * than what was present when the cookie was created.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-cookie", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class PinnableCookieProperties extends CookieProperties {

    @Serial
    private static final long serialVersionUID = -7643955577897341936L;

    /**
     * When generating cookie values, determine whether the value
     * should be compounded and signed with the properties of
     * the current session, such as IP address, user-agent, etc.
     */
    private boolean pinToSession = true;

    /**
     * A regular expression pattern that indicates the set of allowed IP addresses,
     * when {@link #isPinToSession()} is configured. In the event that there is a mismatch
     * between the cookie IP address and the current request-provided IP address (i.e. network switches, VPN, etc),
     * the cookie can still be considered valid if the new IP address matches the pattern
     * specified here.
     */
    @RegularExpressionCapable
    private String allowedIpAddressesPattern;

    /**
     * When set to {@code true} and assuming {@link #isPinToSession()} is also {@code true},
     * client sessions (using the client IP address) are geo-located using a geolocation service when/if configured.
     * The resulting session is either pinned to the client geolocation, or the default client address.
     */
    private boolean geoLocateClientSession;
}
