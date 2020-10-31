package org.apereo.cas.mfa.simple.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketFactory;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface CasSimpleMultifactorAuthenticationTicketFactory extends TicketFactory {
    /**
     * Normalize ticket id string.
     *
     * @param id the id
     * @return the string
     */
    static String normalizeTicketId(final String id) {
        return CasSimpleMultifactorAuthenticationTicket.PREFIX + '-' + id;
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
     * Create ticket.
     *
     * @param service    the service
     * @param properties the properties
     * @return the delegated authentication request ticket
     */
    CasSimpleMultifactorAuthenticationTicket create(Service service, Map<String, Serializable> properties);

}
