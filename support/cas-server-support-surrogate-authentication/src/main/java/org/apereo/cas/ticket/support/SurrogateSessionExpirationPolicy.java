package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketState;

import java.util.Map;

/**
 * Delegates to different expiration policies depending on whether surrogate
 * is true or not.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
public class SurrogateSessionExpirationPolicy extends BaseDelegatingExpirationPolicy {
    private static final long serialVersionUID = -2735975347698196127L;
    
    /**
     * Policy types.
     */
    public enum PolicyTypes {
        /**
         * Surrogate policy type.
         */
        SURROGATE,
        /**
         * Default policy type.
         */
        DEFAULT
    }
    
    /**
     * Instantiates a new surrogate session expiration policy.
     *
     * @param policy the policy
     */
    @JsonCreator
    public SurrogateSessionExpirationPolicy(@JsonProperty("policy") final ExpirationPolicy policy) {
        super(policy);
    }

    @Override
    protected String getExpirationPolicyNameFor(final TicketState ticketState) {
        final Map<String, Object> attributes = ticketState.getAuthentication().getAttributes();
        if (attributes.containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL)
                && attributes.containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER)) {
            LOGGER.debug("Ticket is associated with a surrogate authentication.");
            return PolicyTypes.SURROGATE.name();
        }

        LOGGER.debug("Ticket is not associated with a surrogate authentication.");
        return PolicyTypes.DEFAULT.name();
    }
}
