package org.apereo.cas.token;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.token.cipher.RegisteredServiceTokenTicketCipherExecutor;
import org.apereo.cas.util.DateTimeUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.jasig.cas.client.validation.TicketValidator;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
    private final String casSeverPrefix;
    private final CipherExecutor<String, String> defaultTokenCipherExecutor;
    private final ExpirationPolicy expirationPolicy;
    private final ServicesManager servicesManager;

    @Override
    @SneakyThrows
    public String build(final String serviceTicketId, final Service service) {
        final var assertion = this.ticketValidator.validate(serviceTicketId, service.getId());
        final Map<String, Object> attributes = new LinkedHashMap<>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());

        final Date validUntilDate;
        if (assertion.getValidUntilDate() != null) {
            validUntilDate = assertion.getValidUntilDate();
        } else {
            final var dt = ZonedDateTime.now().plusSeconds(expirationPolicy.getTimeToLive());
            validUntilDate = DateTimeUtils.dateOf(dt);
        }
        return buildJwt(serviceTicketId, service.getId(), assertion.getAuthenticationDate(),
            assertion.getPrincipal().getName(), validUntilDate, attributes);
    }

    @Override
    @SneakyThrows
    public String build(final TicketGrantingTicket ticketGrantingTicket) {
        final var authentication = ticketGrantingTicket.getAuthentication();
        final Map<String, Object> attributes = new LinkedHashMap<>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        final var dt = ZonedDateTime.now().plusSeconds(expirationPolicy.getTimeToLive());
        final var validUntilDate = DateTimeUtils.dateOf(dt);
        return buildJwt(ticketGrantingTicket.getId(),
            casSeverPrefix,
            DateTimeUtils.dateOf(ticketGrantingTicket.getCreationTime()),
            authentication.getPrincipal().getId(),
            validUntilDate,
            attributes);
    }

    private String buildJwt(final String jwtId,
                            final String serviceAudience,
                            final Date issueDate,
                            final String subject,
                            final Date validUntilDate,
                            final Map<String, Object> attributes) {
        final var claims =
            new JWTClaimsSet.Builder()
                .audience(serviceAudience)
                .issuer(casSeverPrefix)
                .jwtID(jwtId)
                .issueTime(issueDate)
                .subject(subject);

        attributes.forEach(claims::claim);
        claims.expirationTime(validUntilDate);

        final var claimsSet = claims.build();
        final var object = claimsSet.toJSONObject();

        final var jwtJson = object.toJSONString();
        LOGGER.debug("Generated JWT [{}]", JsonValue.readJSON(jwtJson).toString(Stringify.FORMATTED));

        LOGGER.debug("Locating service [{}] in service registry", serviceAudience);
        final var registeredService = this.servicesManager.findServiceBy(serviceAudience);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        LOGGER.debug("Locating service specific signing and encryption keys for [{}] in service registry", serviceAudience);
        final RegisteredServiceCipherExecutor serviceCipher = new RegisteredServiceTokenTicketCipherExecutor();
        if (serviceCipher.supports(registeredService)) {
            LOGGER.debug("Encoding JWT based on keys provided by service [{}]", registeredService.getServiceId());
            return serviceCipher.encode(jwtJson, Optional.of(registeredService));
        }

        if (defaultTokenCipherExecutor.isEnabled()) {
            LOGGER.debug("Encoding JWT based on default global keys for [{}]", serviceAudience);
            return defaultTokenCipherExecutor.encode(jwtJson);
        }
        final var header =new PlainHeader.Builder()
            .type(JOSEObjectType.JWT)
            .build();
        final var token = new PlainJWT(header, claimsSet).serialize();
        LOGGER.trace("Generating plain JWT as the ticket: [{}]", token);
        return token;
    }
}
