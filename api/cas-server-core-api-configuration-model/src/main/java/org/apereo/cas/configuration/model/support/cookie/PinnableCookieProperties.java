package org.apereo.cas.configuration.model.support.cookie;

import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
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
@AllArgsConstructor
@NoArgsConstructor
@With
public class PinnableCookieProperties extends CookieProperties {

    @Serial
    private static final long serialVersionUID = -7643955577897341936L;

    /**
     * When generating cookie values, determine whether the value
     * should be compounded and signed with the properties of
     * the current session, such as IP address, user-agent, etc.
     * <p>
     * Turning off session pinning is not exactly ideal. This is a barrier to prevent cookie
     * replay attacks. A cookie that was created with an IP given a certain location cannot
     * be replayed back in a different browser with a different IP from a different location.
     * <p>
     * There may however be legitimate reasons to turn it off and relax this after careful reviews,
     * if you intend to support a type of user who intends to switch their IP quite frequently.
     * A classic example is the type of user that is on VPN at the time of login,
     * but decides to resume their work a few hours later off VPN or at a different location.
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
