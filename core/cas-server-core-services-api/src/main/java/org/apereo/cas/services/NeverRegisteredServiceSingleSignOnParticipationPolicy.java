package org.apereo.cas.services;

import org.apereo.cas.ticket.AuthenticationAwareTicket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link NeverRegisteredServiceSingleSignOnParticipationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
@RequiredArgsConstructor
public class NeverRegisteredServiceSingleSignOnParticipationPolicy implements RegisteredServiceSingleSignOnParticipationPolicy {
    @Serial
    private static final long serialVersionUID = -1123946898337761319L;

    @Override
    public boolean shouldParticipateInSso(final RegisteredService registeredService, final AuthenticationAwareTicket ticketState) {
        return false;
    }
}
