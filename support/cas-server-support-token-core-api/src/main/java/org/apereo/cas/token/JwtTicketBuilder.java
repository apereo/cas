package org.apereo.cas.token;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.TicketValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link JwtTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTicketBuilder implements TokenTicketBuilder {
    private final TicketValidator ticketValidator;

    private final TicketFactory ticketFactory;

    private final JwtBuilder jwtBuilder;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    @Override
    public String build(final String serviceTicketId, final WebApplicationService webApplicationService) throws Throwable {
        val assertion = FunctionUtils.doUnchecked(() -> ticketValidator.validate(serviceTicketId, webApplicationService.getId()));
        val attributes = new LinkedHashMap(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());

        LOGGER.trace("Assertion attributes received are [{}]", attributes);
        val registeredService = servicesManager.findServiceBy(webApplicationService);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        val finalAttributes = ProtocolAttributeEncoder.decodeAttributes(attributes, registeredService, webApplicationService);
        LOGGER.debug("Final attributes decoded are [{}]", finalAttributes);

        val expirationPolicy = ticketFactory.get(ServiceTicket.class).getExpirationPolicyBuilder();
        val dateTime = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTimeToLive(expirationPolicy, registeredService));
        val validUntilDate = DateTimeUtils.dateOf(dateTime);
        val builder = JwtBuilder.JwtRequest.builder();
        val request = builder
            .registeredService(Optional.of(registeredService))
            .serviceAudience(Set.of(webApplicationService.getId()))
            .issueDate(new Date())
            .jwtId(serviceTicketId)
            .subject(assertion.getPrincipal().getId())
            .validUntilDate(validUntilDate)
            .attributes(finalAttributes)
            .issuer(casProperties.getServer().getPrefix())
            .build();
        LOGGER.debug("Building JWT using [{}]", request);
        return jwtBuilder.build(request);
    }

    @Override
    public String build(final Authentication authentication,
                        final RegisteredService registeredService,
                        final String jwtIdentifier,
                        final Map<String, List<Object>> claims) throws Throwable {
        val attributes = new HashMap<>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());
        attributes.putAll(claims);

        val expirationPolicy = ticketFactory.get(TicketGrantingTicket.class).getExpirationPolicyBuilder();
        val dt = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTimeToLive(expirationPolicy, registeredService));
        val validUntilDate = DateTimeUtils.dateOf(dt);

        val builder = JwtBuilder.JwtRequest.builder();
        val request = builder
            .serviceAudience(Set.of(casProperties.getServer().getPrefix()))
            .registeredService(Optional.ofNullable(registeredService))
            .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
            .jwtId(jwtIdentifier)
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(validUntilDate)
            .attributes(attributes)
            .issuer(casProperties.getServer().getPrefix())
            .build();
        return jwtBuilder.build(request);
    }

    protected Long getTimeToLive(final ExpirationPolicyBuilder expirationPolicy,
                                 final RegisteredService registeredService) {
        val timeToLive = expirationPolicy.buildTicketExpirationPolicyFor(registeredService).getTimeToLive();
        return Long.MAX_VALUE == timeToLive ? Long.valueOf(Integer.MAX_VALUE) : timeToLive;
    }

}
