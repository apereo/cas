package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.apache.commons.lang3.Strings;
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
        val prefix = TransientSessionTicket.PREFIX + UniqueTicketIdGenerator.SEPARATOR;
        return Strings.CI.prependIfMissing(id, prefix);
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
            expirationPolicy = (ExpirationPolicy) properties.remove(ExpirationPolicy.class.getName());
        }
        return expirationPolicy;
    }

    /**
     * Create delegated authentication request ticket.
     *
     * @param service    the service
     * @param properties the properties
     * @return the delegated authentication request ticket
     * @throws Throwable the throwable
     */
    T create(Service service, Map<String, Serializable> properties) throws Throwable;

    /**
     * Create transient session ticket.
     *
     * @param id         the id
     * @param properties the properties
     * @return the transient session ticket
     */
    default T create(final String id, final Map<String, Serializable> properties) {
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
     * @throws Throwable the throwable
     */
    default T create(final Service service) throws Throwable {
        return create(service, new LinkedHashMap<>());
    }

    /**
     * Create ticket.
     *
     * @param properties the properties
     * @return the t
     * @throws Throwable the throwable
     */
    default T create(final Map<String, Serializable> properties) throws Throwable {
        return create((Service) null, properties);
    }
}
