package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;

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
    @Serial
    private static final long serialVersionUID = 1122553887352573119L;

    private long maxTimeToLiveInSeconds;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Long> userAgents = new HashMap<>();
    
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Long> ipAddresses = new HashMap<>();

    @Override
    public Optional<ExpirationPolicy> toExpirationPolicy() {
        val clientInfo = ClientInfoHolder.getClientInfo();
        
        if (clientInfo != null && userAgents != null
            && !userAgents.isEmpty() && StringUtils.isNotBlank(clientInfo.getUserAgent())) {
            val timeToLive = userAgents
                .keySet()
                .stream()
                .filter(u -> RegexUtils.find(u, clientInfo.getUserAgent()))
                .findFirst()
                .map(u -> userAgents.get(u))
                .orElse(-1L);
            if (timeToLive >= 0) {
                return Optional.of(new HardTimeoutExpirationPolicy(timeToLive));
            }
        }

        if (clientInfo != null && ipAddresses != null
            && !ipAddresses.isEmpty() && StringUtils.isNotBlank(clientInfo.getClientIpAddress())) {
            val timeToLive = ipAddresses
                .keySet()
                .stream()
                .filter(u -> RegexUtils.find(u, clientInfo.getClientIpAddress()))
                .findFirst()
                .map(u -> ipAddresses.get(u))
                .orElse(-1L);
            if (timeToLive >= 0) {
                return Optional.of(new HardTimeoutExpirationPolicy(timeToLive));
            }
        }

        return getMaxTimeToLiveInSeconds() > 0
            ? Optional.of(new HardTimeoutExpirationPolicy(getMaxTimeToLiveInSeconds()))
            : Optional.empty();
    }
}
