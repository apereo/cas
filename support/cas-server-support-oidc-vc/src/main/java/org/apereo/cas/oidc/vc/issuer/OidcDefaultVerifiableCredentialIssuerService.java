package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jose4j.jwt.JwtClaims;

/**
 * This is {@link OidcDefaultVerifiableCredentialIssuerService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcDefaultVerifiableCredentialIssuerService implements OidcVerifiableCredentialIssuerService {
    private final OidcConfigurationContext configurationContext;
    private final OidcVerifiableCredentialProofValidator credentialProofValidator;
    
    @Override
    public OidcVerifiableCredentialResponse issue(final CredentialRequestValidationContext context) {
        return FunctionUtils.doUnchecked(() -> {
            val proof = credentialProofValidator.validate(context.credentialRequest());
            
            val authentication = Objects.requireNonNull(context.accessToken().getAuthentication());
            val principal = configurationContext.getPrincipalResolver().resolve(
                new BasicIdentifiableCredential(authentication.getPrincipal().getId()));

            val vcClaims = produceClaims(Objects.requireNonNull(principal), context);

            val configurationId = context.credentialRequest().getCredentialConfigurationId();
            val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
            val configuration = properties.getIssuer().getCredentialConfigurations().get(configurationId);

            val payload = new LinkedHashMap<String, Object>();
            payload.put("sub", principal.getId());
            payload.put("client_id", context.accessToken().getClientId());
            payload.put("credential_configuration_id", configurationId);
            payload.put("claims", vcClaims);
            payload.put("cnf", proof.holderJwk().toJSONObject());

            val signedCredential = signVerifiableCredential(payload, context);
            val response = new OidcVerifiableCredentialResponse();
            response.setFormat(configuration.getFormat());
            response.setCredential(signedCredential);
            return response;
        });
    }

    protected String signVerifiableCredential(final Map<String, Object> payload,
                                              final CredentialRequestValidationContext context) throws Throwable {
        val jwtClaims = new JwtClaims();
        jwtClaims.setSubject(payload.get("sub").toString());
        jwtClaims.setIssuedAtToNow();
        jwtClaims.setExpirationTimeMinutesInTheFuture(5);
        val claims = (Map<String, ?>) payload.get("claims");
        claims.forEach(jwtClaims::setClaim);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(),
            context.accessToken().getClientId());
        return configurationContext.getIdTokenSigningAndEncryptionService()
            .encode(Objects.requireNonNull(registeredService), jwtClaims);
    }

    protected Map<String, Object> produceClaims(final Principal principal, final CredentialRequestValidationContext context) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val configurationId = context.credentialRequest().getCredentialConfigurationId();
        val configuration = properties.getIssuer().getCredentialConfigurations().get(configurationId);
        val claims = new LinkedHashMap<String, Object>();

        configuration.getClaims().forEach((claimName, claimProps) -> {
            val rawValue = principal.getAttributes().get(claimName);

            if (rawValue == null && claimProps.isMandatory()) {
                throw new IllegalArgumentException("Missing required principal attribute for claim %s".formatted(claimName));
            }
            if (rawValue != null) {
                val mappedValue = convertToClaimValue(rawValue, claimProps.getValueType(), claimName);
                if (mappedValue != null) {
                    claims.put(claimName, mappedValue);
                }
            }
        });
        return claims;
    }

    protected Object convertToClaimValue(final Object rawValue, final String valueType, final String claimName) {
        val firstValue = CollectionUtils.firstElement(rawValue).orElseThrow().toString();
        return switch (valueType) {
            case "boolean" -> BooleanUtils.toBoolean(firstValue);
            case "number" -> NumberUtils.createNumber(firstValue).doubleValue();
            case "array" -> CollectionUtils.toCollection(rawValue);
            default -> firstValue;
        };
    }
}
