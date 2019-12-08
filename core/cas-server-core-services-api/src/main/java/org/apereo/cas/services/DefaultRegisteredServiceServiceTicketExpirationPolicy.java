package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DefaultRegisteredServiceServiceTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultRegisteredServiceServiceTicketExpirationPolicy implements RegisteredServiceServiceTicketExpirationPolicy {
    private static final long serialVersionUID = -6745109870746310448L;

    private long numberOfUses;

    private String timeToLive;
}
