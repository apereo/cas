package org.apereo.cas.oidc.vc.issuer.enc;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;

/**
 * This is {@link BaseOidcVerifiableCredentialEncoder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public abstract class BaseOidcVerifiableCredentialEncoder implements OidcVerifiableCredentialEncoder {
    protected final OidcConfigurationContext configurationContext;

    /**
     * Length of time an issued credential remains valid, per credential configuration.
     *
     * @param configurationId the credential configuration id
     * @return the credential validity
     */
    protected Duration resolveCredentialValidity(final String configurationId) {
        return Beans.newDuration(resolveConfiguration(configurationId).getCredentialValidity());
    }

    protected Map<String, Object> produceClaims(final Principal principal, final OidcVerifiableCredentialValidationContext context) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val configurationId = context.resolveConfigurationId();
        val configuration = properties.getIssuer().getCredentialConfigurations().get(configurationId);
        Objects.requireNonNull(configuration, () -> "Unable to locate credential configuration " + configurationId);
        val claims = new LinkedHashMap<String, Object>();

        configuration.getClaims().forEach((claimName, claimProps) -> {
            val rawValue = principal.getAttributes().get(claimName);

            if (rawValue == null && claimProps.isMandatory()) {
                throw new IllegalArgumentException("Missing required principal attribute for claim %s".formatted(claimName));
            }
            if (rawValue != null) {
                val claimValue = rawValue.size() == 1 ? rawValue.getFirst() : rawValue;
                claims.put(claimName, !(claimValue instanceof Number) && NumberUtils.isParsable(claimValue.toString())
                    ? NumberUtils.createNumber(claimValue.toString())
                    : claimValue);
            }
        });
        return claims;
    }

    protected OidcVerifiableCredentialConfigurationProperties resolveConfiguration(final String configurationId) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val configuration = properties.getIssuer().getCredentialConfigurations().get(configurationId);
        Objects.requireNonNull(configuration, () -> "Unable to locate credential configuration " + configurationId);
        return configuration;
    }

    protected String sign(
        final String sub, final OidcVerifiableCredentialValidationContext context,
        final OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult proof,
        final CheckedConsumer<JwtClaims> claimsConsumer) throws Throwable {
        val oidc = configurationContext.getCasProperties().getAuthn().getOidc();
        val configurationId = context.resolveConfigurationId();

        val issuedAt = NumericDate.now();
        val jwtClaims = new JwtClaims();
        jwtClaims.setSubject(sub);
        jwtClaims.setIssuedAt(issuedAt);
        jwtClaims.setExpirationTime(NumericDate.fromSeconds(
            issuedAt.getValue() + resolveCredentialValidity(configurationId).toSeconds()));

        val nb = Long.valueOf(Beans.newDuration(oidc.getCore().getSkew()).toMinutes()).floatValue();
        jwtClaims.setNotBeforeMinutesInThePast(nb);
        jwtClaims.setStringClaim("typ", getFormat().getValue());
        jwtClaims.setJwtId(UUID.randomUUID().toString());

        val issuer = oidc.getCore().getIssuer();
        jwtClaims.setIssuer(issuer);
        jwtClaims.setClaim("vct", issuer + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL + '/' + configurationId);
        jwtClaims.setClaim("cnf", Map.of("jwk", proof.holderJwk().toJSONObject()));
        
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(),
            context.accessToken().getClientId());
        claimsConsumer.accept(jwtClaims);
        return configurationContext.getIdTokenSigningAndEncryptionService()
            .encode(Objects.requireNonNull(registeredService), jwtClaims);
    }
}
