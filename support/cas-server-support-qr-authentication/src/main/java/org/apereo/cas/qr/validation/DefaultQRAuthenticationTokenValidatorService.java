package org.apereo.cas.qr.validation;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.qr.QRAuthenticationConstants;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import java.time.Clock;
import java.time.LocalDateTime;

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

    private final TicketRegistry ticketRegistry;

    private final CasConfigurationProperties casProperties;

    private final QRAuthenticationDeviceRepository deviceRepository;

    @Override
    public QRAuthenticationTokenValidationResult validate(final QRAuthenticationTokenValidationRequest request) {
        val claims = jwtBuilder.unpack(request.getRegisteredService(), request.getToken());
        LOGGER.trace("Unpacked QR token as [{}]", claims);

        val tgt = ticketRegistry.getTicket(claims.getJWTID(), TicketGrantingTicket.class);
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

        val tokenDeviceId = FunctionUtils.doUnchecked(() -> claims.getStringClaim(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID));
        if (!Strings.CI.equals(tokenDeviceId, request.getDeviceId())) {
            LOGGER.warn("Request device identifier [{}] does not match the token's identifier: [{}]", request.getDeviceId(), tokenDeviceId);
            throw new AuthenticationException("Request is assigned an invalid device identifier");
        }

        if (!deviceRepository.isAuthorizedDeviceFor(request.getDeviceId(), claims.getSubject())) {
            val message = String.format("Token is not authorized for device identifier [%s]", request.getDeviceId());
            throw new AuthenticationException(message);
        }

        return QRAuthenticationTokenValidationResult.builder()
            .authentication(authentication)
            .build();
    }
}
