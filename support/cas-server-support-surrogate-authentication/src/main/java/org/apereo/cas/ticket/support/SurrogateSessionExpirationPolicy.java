package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Delegates to different expiration policies depending on whether surrogate
 * is true or not.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class SurrogateSessionExpirationPolicy extends AbstractCasExpirationPolicy {

    private static final long serialVersionUID = -2735975347698196127L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateSessionExpirationPolicy.class);

    @JsonProperty
    private ExpirationPolicy surrogateExpirationPolicy;

    @JsonProperty
    private ExpirationPolicy sessionExpirationPolicy;

    /**
     * Instantiates a new surrogate delegating expiration policy.
     */
    public SurrogateSessionExpirationPolicy() {
    }

    /**
     * Instantiates a new surrogate delegating expiration policy.
     *
     * @param surrogateExpirationPolicy the surrogate expiration policy
     * @param sessionExpirationPolicy   the session expiration policy
     */
    public SurrogateSessionExpirationPolicy(final ExpirationPolicy surrogateExpirationPolicy,
                                            final ExpirationPolicy sessionExpirationPolicy) {
        this.surrogateExpirationPolicy = surrogateExpirationPolicy;
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }

    @PostConstruct
    private void postConstruct() {
        if (this.surrogateExpirationPolicy != null) {
            LOGGER.debug("Using surrogate expiration policy of [{}]", this.surrogateExpirationPolicy);
        }
        if (this.sessionExpirationPolicy != null) {
            LOGGER.debug("Using session expiration policy of [{}]", this.sessionExpirationPolicy);
        }
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (this.surrogateExpirationPolicy != null && this.sessionExpirationPolicy != null) {
            final Map<String, Object> attributes = ticketState.getAuthentication().getAttributes();
            if (attributes.containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_CREDENTIAL)
                    && attributes.containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER)) {
                LOGGER.debug("Ticket is not associated with a surrogate authentication. Invoking [{}]", this.sessionExpirationPolicy);
                return this.sessionExpirationPolicy.isExpired(ticketState);
            }

            LOGGER.debug("Ticket is associated with a surrogate authentication. Invoking [{}]", this.surrogateExpirationPolicy);
            return this.surrogateExpirationPolicy.isExpired(ticketState);
        }
        LOGGER.warn("No expiration policy settings are defined for surrogate authentication");
        return false;
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        if (this.surrogateExpirationPolicy != null) {
            return this.surrogateExpirationPolicy.getTimeToLive();
        }
        return 0L;
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        if (this.surrogateExpirationPolicy != null) {
            return this.surrogateExpirationPolicy.getTimeToIdle();
        }
        return 0L;
    }

    public void setSurrogateExpirationPolicy(final ExpirationPolicy surrogateExpirationPolicy) {
        this.surrogateExpirationPolicy = surrogateExpirationPolicy;
    }

    public void setSessionExpirationPolicy(final ExpirationPolicy sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final SurrogateSessionExpirationPolicy rhs = (SurrogateSessionExpirationPolicy) obj;
        return new EqualsBuilder()
                .append(this.surrogateExpirationPolicy, rhs.surrogateExpirationPolicy)
                .append(this.sessionExpirationPolicy, rhs.sessionExpirationPolicy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(surrogateExpirationPolicy)
                .append(sessionExpirationPolicy)
                .toHashCode();
    }
}
