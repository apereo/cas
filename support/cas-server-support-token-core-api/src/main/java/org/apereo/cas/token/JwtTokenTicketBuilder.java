package org.apereo.cas.token;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jasig.cas.client.validation.TicketValidator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link JwtTokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@RequiredArgsConstructor
public class JwtTokenTicketBuilder implements TokenTicketBuilder {
    private final TicketValidator ticketValidator;

    private final ExpirationPolicyBuilder expirationPolicy;

    private final JwtBuilder jwtBuilder;

    private final ServicesManager servicesManager;

    @Override
    @SneakyThrows
    public String build(final String serviceTicketId, final Service service) {
        val assertion = this.ticketValidator.validate(serviceTicketId, service.getId());
        val attributes = CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(assertion.getAttributes());
        attributes.putAll(CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(assertion.getPrincipal().getAttributes()));

        val validUntilDate = FunctionUtils.doIf(
            assertion.getValidUntilDate() != null,
            assertion::getValidUntilDate,
            () -> {
                val dt = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTimeToLive());
                return DateTimeUtils.dateOf(dt);
            })
            .get();

        val builder = JwtBuilder.JwtRequest.builder();
        val request = builder
            .registeredService(Optional.ofNullable(servicesManager.findServiceBy(service)))
            .serviceAudience(service.getId())
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
        val attributes = new HashMap<String, List<Object>>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        val dt = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTimeToLive());
        val validUntilDate = DateTimeUtils.dateOf(dt);

        val builder = JwtBuilder.JwtRequest.builder();
        val request = builder.serviceAudience(jwtBuilder.getCasSeverPrefix())
            .registeredService(Optional.empty())
            .issueDate(DateTimeUtils.dateOf(ticketGrantingTicket.getCreationTime()))
            .jwtId(ticketGrantingTicket.getId())
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(validUntilDate)
            .attributes(attributes)
            .build();
        return jwtBuilder.build(request);
    }

    protected Long getTimeToLive() {
        val timeToLive = expirationPolicy.buildTicketExpirationPolicy().getTimeToLive();
        return Long.MAX_VALUE == timeToLive ? Long.valueOf(Integer.MAX_VALUE) : timeToLive;
    }
}
