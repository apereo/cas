package org.apereo.cas.token;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jasig.cas.client.validation.TicketValidator;

import java.time.ZonedDateTime;
import java.util.HashMap;

/**
 * This is {@link JWTTokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class JWTTokenTicketBuilder implements TokenTicketBuilder {
    private final TicketValidator ticketValidator;
    private final ExpirationPolicy expirationPolicy;
    private final JWTBuilder jwtBuilder;

    @Override
    @SneakyThrows
    public String build(final String serviceTicketId, final Service service) {
        val assertion = this.ticketValidator.validate(serviceTicketId, service.getId());
        val attributes = new HashMap<String, Object>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());

        val validUntilDate = FunctionUtils.doIf(
            assertion.getValidUntilDate() != null,
            assertion::getValidUntilDate,
            () -> {
                val dt = ZonedDateTime.now().plusSeconds(expirationPolicy.getTimeToLive());
                return DateTimeUtils.dateOf(dt);
            })
            .get();

        val builder = JWTBuilder.JwtRequest.builder();
        val request = builder.serviceAudience(service.getId())
            .issueDate(assertion.getAuthenticationDate())
            .jwtId(serviceTicketId)
            .subject(assertion.getPrincipal().getName())
            .validUntilDate(validUntilDate)
            .attributes(attributes)
            .build();
        return jwtBuilder.build(request);
    }

    @Override
    @SneakyThrows
    public String build(final TicketGrantingTicket ticketGrantingTicket) {
        val authentication = ticketGrantingTicket.getAuthentication();
        val attributes = new HashMap<String, Object>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        val dt = ZonedDateTime.now().plusSeconds(expirationPolicy.getTimeToLive());
        val validUntilDate = DateTimeUtils.dateOf(dt);

        val builder = JWTBuilder.JwtRequest.builder();
        val request = builder.serviceAudience(jwtBuilder.getCasSeverPrefix())
            .issueDate(DateTimeUtils.dateOf(ticketGrantingTicket.getCreationTime()))
            .jwtId(ticketGrantingTicket.getId())
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(validUntilDate)
            .attributes(attributes)
            .build();
        return jwtBuilder.build(request);
    }
}
