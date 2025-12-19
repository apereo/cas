package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.services.RegisteredServiceDefinition;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Strategy that contains configuration and metadata to construct ticket expiration policies.
 *
 * @author Misagh Moayyed
 * @see Ticket
 * @since 6.0.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ExpirationPolicyBuilder<T extends Ticket> extends Serializable {

    /**
     * Bean name to indicate the policy bean for ticket-granting tickets.
     */
    String BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY = "grantingTicketExpirationPolicy";
    /**
     * Bean name to indicate the policy bean for proxy-granting tickets.
     */
    String BEAN_NAME_PROXY_GRANTING_TICKET_EXPIRATION_POLICY = "proxyGrantingTicketExpirationPolicy";
    /**
     * Bean name to indicate the policy bean for service ticket.
     */
    String BEAN_NAME_SERVICE_TICKET_EXPIRATION_POLICY = "serviceTicketExpirationPolicy";
    /**
     * Bean name to indicate the policy bean for proxy tickets.
     */
    String BEAN_NAME_PROXY_TICKET_EXPIRATION_POLICY = "proxyTicketExpirationPolicy";
    /**
     * Bean name to indicate the policy bean for transient session.
     */
    String BEAN_NAME_TRANSIENT_SESSION_TICKET_EXPIRATION_POLICY = "transientSessionTicketExpirationPolicy";

    /**
     * Method build ticket expiration policy.
     *
     * @return - the policy
     */
    ExpirationPolicy buildTicketExpirationPolicy();

    /**
     * Build ticket expiration policy for service.
     *
     * @param registeredService the registered service
     * @return the optional
     */
    default ExpirationPolicy buildTicketExpirationPolicyFor(final RegisteredServiceDefinition registeredService) {
        return buildTicketExpirationPolicy();
    }

}
