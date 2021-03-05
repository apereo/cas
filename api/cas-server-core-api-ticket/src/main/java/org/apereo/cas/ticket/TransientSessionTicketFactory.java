package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link TransientSessionTicketFactory}.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 5.3.0
 */
public interface TransientSessionTicketFactory<T extends TransientSessionTicket> extends TicketFactory {
    /**
     * Normalize ticket id string.
     *
     * @param id the id
     * @return the string
     */
    static String normalizeTicketId(final String id) {
        return TransientSessionTicket.PREFIX + '-' + id;
    }

    /**
     * Build expiration policy expiration policy.
     *
     * @param expirationPolicyBuilder the expiration policy builder
     * @param properties              the properties
     * @return the expiration policy
     */
    static ExpirationPolicy buildExpirationPolicy(final ExpirationPolicyBuilder expirationPolicyBuilder,
        final Map<String, Serializable> properties) {
        var expirationPolicy = expirationPolicyBuilder.buildTicketExpirationPolicy();
        if (properties.containsKey(ExpirationPolicy.class.getName())) {
            expirationPolicy = ExpirationPolicy.class.cast(properties.remove(ExpirationPolicy.class.getName()));
        }
        return expirationPolicy;
    }

    /**
     * Create delegated authentication request ticket.
     *
     * @param service    the service
     * @param properties the properties
     * @return the delegated authentication request ticket
     */
    T create(Service service, Map<String, Serializable> properties);

    /**
     * Create transient session ticket.
     *
     * @param id         the id
     * @param properties the properties
     * @return the transient session ticket
     */
    default T create(String id, Map<String, Serializable> properties) {
        return create(id, null, properties);
    }

    /**
     * Create transient ticket.
     *
     * @param id         the id
     * @param service    the service
     * @param properties the properties
     * @return the t
     */
    T create(String id, Service service, Map<String, Serializable> properties);

    /**
     * Create delegated authentication request ticket.
     *
     * @param service the service
     * @return the delegated authentication request ticket
     */
    default T create(final Service service) {
        return create(service, new LinkedHashMap<>(0));
    }
}
