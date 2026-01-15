package org.apereo.cas.mfa.simple.ticket;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketFactory;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface CasSimpleMultifactorAuthenticationTicketFactory extends TicketFactory {
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
     * Create ticket.
     *
     * @param service    the service
     * @param properties the properties
     * @return the delegated authentication request ticket
     * @throws Throwable the throwable
     */
    CasSimpleMultifactorAuthenticationTicket create(Service service, Map<String, Serializable> properties) throws Throwable;

    /**
     * Create cas simple multifactor authentication ticket.
     *
     * @param id         the id
     * @param service    the service
     * @param properties the properties
     * @return the cas simple multifactor authentication ticket
     */
    CasSimpleMultifactorAuthenticationTicket create(String id, Service service, Map<String, Serializable> properties);

}
