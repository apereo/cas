package org.apereo.cas.qr.validation;

import org.apereo.cas.CentralAuthenticationService;
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
        val tgt = centralAuthenticationService.getTicket(claims.getJWTID(), TicketGrantingTicket.class);
        val dt = DateTimeUtils.localDateTimeOf(claims.getExpirationTime());

        if (LocalDateTime.now(Clock.systemUTC()).isAfter(dt)) {
            LOGGER.warn("Token with id [{}] has expired", tgt.getId());
            return QRAuthenticationTokenValidationResult.builder().build();
        }

        if (!tgt.getAuthentication().getPrincipal().getId().equals(claims.getSubject())) {
            LOGGER.warn("Token with id [{}] does not belong to the assigned principal", claims.getSubject());
            return QRAuthenticationTokenValidationResult.builder().build();
        }

        if (!claims.getIssuer().equals(casProperties.getServer().getPrefix())) {
            LOGGER.warn("Token with id [{}] has an invalid issuer [{}] that does not match [{}]", tgt.getId(),
                claims.getIssuer(), casProperties.getServer().getPrefix());
            return QRAuthenticationTokenValidationResult.builder().build();
        }
        return QRAuthenticationTokenValidationResult.builder()
            .authentication(tgt.getAuthentication())
            .build();
    }
}
