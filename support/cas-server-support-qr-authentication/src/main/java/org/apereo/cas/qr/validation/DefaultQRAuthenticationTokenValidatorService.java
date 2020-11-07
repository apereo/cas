package org.apereo.cas.qr.validation;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * This is {@link QRAuthenticationTokenValidatorService}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultQRAuthenticationTokenValidatorService implements QRAuthenticationTokenValidatorService {
    private final JwtBuilder jwtBuilder;

    private final CentralAuthenticationService centralAuthenticationService;

    private final CasConfigurationProperties casProperties;

    @Override
    public QRAuthenticationTokenValidationResult validate(final Optional<RegisteredService> service, final String token) {
        val claims = jwtBuilder.unpack(service, token);
        LOGGER.trace("Unpacked QR token as [{}]", claims);

        val tgt = centralAuthenticationService.getTicket(claims.getJWTID(), TicketGrantingTicket.class);
        val dt = DateTimeUtils.localDateTimeOf(claims.getExpirationTime());

        val now = LocalDateTime.now(Clock.systemUTC());
        if (now.isAfter(dt)) {
            LOGGER.trace("Comparing now at [{}] with token's expiration time [{}]", now, dt);
            throw new AuthenticationException(String.format("Token %s has expired", tgt.getId()));
        }

        val authentication = tgt.getAuthentication();
        LOGGER.trace("Authentication attempt linked to [{}] is [{}]", tgt.getId(), authentication);

        if (!authentication.getPrincipal().getId().equals(claims.getSubject())) {
            val message = String.format("Token %s does not belong to the assigned principal", claims.getSubject());
            throw new AuthenticationException(message);
        }

        if (!claims.getIssuer().equals(casProperties.getServer().getPrefix())) {
            val message = String.format("Token %s has an invalid issuer %s that does not match %s", tgt.getId(),
                claims.getIssuer(), casProperties.getServer().getPrefix());
            throw new AuthenticationException(message);
        }
        return QRAuthenticationTokenValidationResult.builder()
            .authentication(authentication)
            .build();
    }
}
