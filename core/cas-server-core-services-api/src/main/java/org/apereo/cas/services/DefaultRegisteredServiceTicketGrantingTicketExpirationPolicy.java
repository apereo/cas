package org.apereo.cas.services;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Optional;

/**
 * This is {@link DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy implements RegisteredServiceTicketGrantingTicketExpirationPolicy {
    private static final long serialVersionUID = 1122553887352573119L;

    private long maxTimeToLiveInSeconds;

    @Override
    public Optional<ExpirationPolicy> toExpirationPolicy() {
        if (getMaxTimeToLiveInSeconds() > 0) {
            return Optional.of(new HardTimeoutExpirationPolicy(getMaxTimeToLiveInSeconds()));
        }
        return Optional.empty();
    }
}
