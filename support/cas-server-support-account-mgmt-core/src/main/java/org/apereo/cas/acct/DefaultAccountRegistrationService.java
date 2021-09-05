package org.apereo.cas.acct;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;

import java.io.Serializable;
import java.util.UUID;

/**
 * This is {@link DefaultAccountRegistrationService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DefaultAccountRegistrationService implements AccountRegistrationService {
    private final AccountRegistrationPropertyLoader accountRegistrationPropertyLoader;

    private final CasConfigurationProperties casProperties;

    private final CipherExecutor<Serializable, String> cipherExecutor;

    @Override
    public String createToken(final AccountRegistrationRequest registrationRequest) {
        val token = UUID.randomUUID().toString();
        val claims = new JwtClaims();
        claims.setJwtId(token);
        claims.setIssuer(casProperties.getServer().getPrefix());
        claims.setAudience(casProperties.getServer().getPrefix());
        val props = casProperties.getAccountRegistration().getCore();
        claims.setExpirationTimeMinutesInTheFuture((float) props.getExpirationMinutes());
        claims.setIssuedAtToNow();

        val holder = ClientInfoHolder.getClientInfo();
        if (holder != null) {
            if (props.isIncludeServerIpAddress()) {
                claims.setStringClaim("origin", holder.getServerIpAddress());
            }
            if (props.isIncludeClientIpAddress()) {
                claims.setStringClaim("client", holder.getClientIpAddress());
            }
        }
        claims.setSubject(registrationRequest.getUsername());
        LOGGER.debug("Creating account registration token for [{}]", registrationRequest.getUsername());
        val json = claims.toJson();

        LOGGER.debug("Encoding the generated JSON token...");
        return this.cipherExecutor.encode(json);
    }
}
