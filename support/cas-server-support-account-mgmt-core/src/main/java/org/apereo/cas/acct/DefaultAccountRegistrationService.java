package org.apereo.cas.acct;

import org.apereo.cas.acct.provision.AccountRegistrationProvisioner;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;

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

    private final AccountRegistrationUsernameBuilder accountRegistrationUsernameBuilder;

    private final AccountRegistrationProvisioner accountRegistrationProvisioner;

    @Audit(action = AuditableActions.ACCOUNT_REGISTRATION,
        actionResolverName = AuditActionResolvers.ACCOUNT_REGISTRATION_TOKEN_VALIDATION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.ACCOUNT_REGISTRATION_TOKEN_VALIDATION_RESOURCE_RESOLVER)
    @Override
    public AccountRegistrationRequest validateToken(final String token) throws Exception {
        val claimsJson = this.cipherExecutor.decode(token);
        val claims = JwtClaims.parse(claimsJson);

        val issuer = casProperties.getServer().getPrefix();
        if (!claims.getIssuer().equals(issuer)) {
            LOGGER.error("Token issuer [{}] does not match CAS' [{}]", claims.getIssuer(), issuer);
            return null;
        }
        if (claims.getAudience().isEmpty() || !claims.getAudience().get(0).equals(issuer)) {
            LOGGER.error("Token audience does not match CAS");
            return null;
        }
        if (StringUtils.isBlank(claims.getSubject())) {
            LOGGER.error("Token has no subject identifier");
            return null;
        }
        val props = casProperties.getAccountRegistration().getCore();
        val holder = ClientInfoHolder.getClientInfo();
        if (props.isIncludeServerIpAddress() && !claims.getStringClaimValue("origin").equals(holder.getServerIpAddress())) {
            LOGGER.error("Token origin server IP address does not match CAS");
            return null;
        }
        if (props.isIncludeClientIpAddress() && !claims.getStringClaimValue("client").equals(holder.getClientIpAddress())) {
            LOGGER.error("Token client IP address does not match CAS");
            return null;
        }

        val expirationTime = claims.getExpirationTime();
        if (expirationTime.isBefore(NumericDate.now())) {
            LOGGER.error("Token has expired with expiration time of [{}].", expirationTime);
            return null;
        }
        return new AccountRegistrationRequest(claims.getClaimsMap());
    }

    @Audit(action = AuditableActions.ACCOUNT_REGISTRATION,
        actionResolverName = AuditActionResolvers.ACCOUNT_REGISTRATION_TOKEN_CREATION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.ACCOUNT_REGISTRATION_TOKEN_CREATION_RESOURCE_RESOLVER)
    @Override
    public String createToken(final AccountRegistrationRequest registrationRequest) {
        val token = UUID.randomUUID().toString();
        val claims = new JwtClaims();
        claims.setJwtId(token);
        claims.setIssuer(casProperties.getServer().getPrefix());
        claims.setAudience(casProperties.getServer().getPrefix());
        val props = casProperties.getAccountRegistration().getCore();
        val minutes = Beans.newDuration(props.getExpiration()).toMinutes();
        claims.setExpirationTimeMinutesInTheFuture((float) minutes);
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
        val username = accountRegistrationUsernameBuilder.build(registrationRequest);
        claims.setSubject(username);
        registrationRequest.getProperties().forEach(claims::setClaim);
        LOGGER.debug("Creating account registration token for [{}]", username);
        val json = claims.toJson();

        LOGGER.debug("Encoding the generated JSON token...");
        return this.cipherExecutor.encode(json);
    }
}
