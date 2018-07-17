package org.apereo.cas.token;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.token.cipher.RegisteredServiceTokenTicketCipherExecutor;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.jasig.cas.client.validation.TicketValidator;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
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
        return buildJwt(serviceTicketId, service.getId(), assertion.getAuthenticationDate(),
            assertion.getPrincipal().getName(), validUntilDate, attributes);
    }

    @Override
    @SneakyThrows
    public String build(final TicketGrantingTicket ticketGrantingTicket) {
        val authentication = ticketGrantingTicket.getAuthentication();
        val attributes = new HashMap<String, Object>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        val dt = ZonedDateTime.now().plusSeconds(expirationPolicy.getTimeToLive());
        val validUntilDate = DateTimeUtils.dateOf(dt);
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
        val claims =
            new JWTClaimsSet.Builder()
                .audience(serviceAudience)
                .issuer(casSeverPrefix)
                .jwtID(jwtId)
                .issueTime(issueDate)
                .subject(subject);

        attributes.forEach(claims::claim);
        claims.expirationTime(validUntilDate);

        val claimsSet = claims.build();
        val object = claimsSet.toJSONObject();

        val jwtJson = object.toJSONString();
        LOGGER.debug("Generated JWT [{}]", JsonValue.readJSON(jwtJson).toString(Stringify.FORMATTED));

        LOGGER.debug("Locating service [{}] in service registry", serviceAudience);
        val registeredService = this.servicesManager.findServiceBy(serviceAudience);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        LOGGER.debug("Locating service specific signing and encryption keys for [{}] in service registry", serviceAudience);
        val serviceCipher = new RegisteredServiceTokenTicketCipherExecutor();
        if (serviceCipher.supports(registeredService)) {
            LOGGER.debug("Encoding JWT based on keys provided by service [{}]", registeredService.getServiceId());
            return serviceCipher.encode(jwtJson, Optional.of(registeredService));
        }

        if (defaultTokenCipherExecutor.isEnabled()) {
            LOGGER.debug("Encoding JWT based on default global keys for [{}]", serviceAudience);
            return defaultTokenCipherExecutor.encode(jwtJson);
        }
        val header = new PlainHeader.Builder()
            .type(JOSEObjectType.JWT)
            .build();
        val token = new PlainJWT(header, claimsSet).serialize();
        LOGGER.trace("Generating plain JWT as the ticket: [{}]", token);
        return token;
    }
}
