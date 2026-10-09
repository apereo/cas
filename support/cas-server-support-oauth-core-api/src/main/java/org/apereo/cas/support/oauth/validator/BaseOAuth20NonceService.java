package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Keeps server-provided nonces, such as DPoP nonces and client attestation challenges, as transient session tickets marked
 * with a property of their own and expiring after a time to live, so they are unpredictable, shared by all nodes that share
 * the ticket registry, and never mistaken for one another. With the stateless ticket registry a nonce carries its own
 * expiration. A nonce may be used more than once until it expires.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public abstract class BaseOAuth20NonceService {
    protected final TicketRegistry ticketRegistry;

    protected final TicketFactory ticketFactory;

    protected final CasConfigurationProperties casProperties;

    /**
     * Create a nonce.
     *
     * @return the nonce
     * @throws Throwable the throwable
     */
    public String create() throws Throwable {
        val properties = new HashMap<String, Serializable>();
        properties.put(getMarkerProperty(), Boolean.TRUE.toString());
        properties.put(ExpirationPolicy.class.getName(), new HardTimeoutExpirationPolicy(Math.max(1, getTimeToLive().toSeconds())));
        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        return Objects.requireNonNull(ticketRegistry.addTicket(factory.create(properties))).getId();
    }

    /**
     * Whether a nonce was handed out by this service and has not expired.
     *
     * @param nonce the nonce
     * @return true when valid
     */
    public boolean isValid(final @Nullable String nonce) {
        if (StringUtils.isBlank(nonce)) {
            return false;
        }
        val ticket = FunctionUtils.doAndHandle(() -> ticketRegistry.getTicket(nonce));
        return ticket instanceof final TransientSessionTicket transientTicket && !transientTicket.isExpired()
            && transientTicket.containsProperty(getMarkerProperty());
    }

    /**
     * The property that marks the transient session tickets of this service.
     *
     * @return the property name
     */
    protected abstract String getMarkerProperty();

    /**
     * How long a nonce may be used.
     *
     * @return the time to live
     */
    protected abstract Duration getTimeToLive();
}
