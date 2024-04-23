package org.apereo.cas.services;


import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link DefaultRegisteredServiceSingleSignOnParticipationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
public class DefaultRegisteredServiceSingleSignOnParticipationPolicy implements RegisteredServiceSingleSignOnParticipationPolicy {
    @Serial
    private static final long serialVersionUID = -1223944598337761319L;

    private TriStateBoolean createCookieOnRenewedAuthentication;

    @Override
    public boolean shouldParticipateInSso(final RegisteredService registeredService, final AuthenticationAwareTicket ticketState) {
        return true;
    }
}
