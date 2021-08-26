package org.apereo.cas.token;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jasig.cas.client.validation.TicketValidator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link JwtTokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class JwtTokenTicketBuilder implements TokenTicketBuilder {
    private final TicketValidator ticketValidator;

    private final ExpirationPolicyBuilder expirationPolicy;

    private final JwtBuilder jwtBuilder;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    @Override
    @SneakyThrows
    public String build(final String serviceTicketId, final WebApplicationService webApplicationService) {
        val assertion = this.ticketValidator.validate(serviceTicketId, webApplicationService.getId());
        val attributes = (Map) CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(assertion.getAttributes());
        attributes.putAll(CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(assertion.getPrincipal().getAttributes()));

        LOGGER.trace("Assertion attributes received are [{}]", attributes);
        val registeredService = servicesManager.findServiceBy(webApplicationService);
        val finalAttributes = ProtocolAttributeEncoder.decodeAttributes(attributes, registeredService, webApplicationService);
        LOGGER.debug("Final attributes decoded are [{}]", finalAttributes);

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
            .registeredService(Optional.ofNullable(registeredService))
            .serviceAudience(webApplicationService.getId())
            .issueDate(assertion.getAuthenticationDate())
            .jwtId(serviceTicketId)
            .subject(assertion.getPrincipal().getName())
            .validUntilDate(validUntilDate)
            .attributes(finalAttributes)
            .issuer(casProperties.getServer().getPrefix())
            .build();
        LOGGER.debug("Building JWT using [{}]", request);
        return jwtBuilder.build(request);
    }

    @Override
    @SneakyThrows
    public String build(final TicketGrantingTicket ticketGrantingTicket, final Map<String, List<Object>> claims) {
        val authentication = ticketGrantingTicket.getAuthentication();

        val attributes = new HashMap<>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());
        attributes.putAll(claims);

        val dt = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTimeToLive());
        val validUntilDate = DateTimeUtils.dateOf(dt);

        val builder = JwtBuilder.JwtRequest.builder();
        val request = builder
            .serviceAudience(casProperties.getServer().getPrefix())
            .registeredService(Optional.empty())
            .issueDate(DateTimeUtils.dateOf(ticketGrantingTicket.getCreationTime()))
            .jwtId(ticketGrantingTicket.getId())
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(validUntilDate)
            .attributes(attributes)
            .issuer(casProperties.getServer().getPrefix())
            .build();
        return jwtBuilder.build(request);
    }

    /**
     * Gets time to live.
     *
     * @return the time to live
     */
    protected Long getTimeToLive() {
        val timeToLive = expirationPolicy.buildTicketExpirationPolicy().getTimeToLive();
        return Long.MAX_VALUE == timeToLive ? Long.valueOf(Integer.MAX_VALUE) : timeToLive;
    }
}
