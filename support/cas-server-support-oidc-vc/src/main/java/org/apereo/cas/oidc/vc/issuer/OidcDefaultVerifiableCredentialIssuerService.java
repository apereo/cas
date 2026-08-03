package org.apereo.cas.oidc.vc.issuer;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.issuer.metadata.CredentialConfigurationFormats;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.authlete.sd.SDObjectBuilder;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.jspecify.annotations.NonNull;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * This is {@link OidcDefaultVerifiableCredentialIssuerService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcDefaultVerifiableCredentialIssuerService implements OidcVerifiableCredentialIssuerService {
    private static final int CLAIM_VALIDITY_IN_MINUTES = 5;
    
    private final OidcConfigurationContext configurationContext;
    private final OidcVerifiableCredentialProofValidator credentialProofValidator;

    @Override
    public OidcVerifiableCredentialIssuerResponse issue(final OidcVerifiableCredentialValidationContext context) throws Throwable {
        val proof = credentialProofValidator.validate(context.credentialRequest());

        val authentication = Objects.requireNonNull(context.accessToken().getAuthentication());
        val principal = configurationContext.getPrincipalResolver().resolve(
            new BasicIdentifiableCredential(authentication.getPrincipal().getId()));

        val vcClaims = produceClaims(Objects.requireNonNull(principal), context);
        val issuer = configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer();
        val configurationId = resolveRequestedConfigurationId(context);
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val configuration = properties.getIssuer().getCredentialConfigurations().get(configurationId);
        Objects.requireNonNull(configuration, () -> "Unable to locate credential configuration " + configurationId);

        val chosenFormat = determineCredentialFormat(configuration);

        val payload = new LinkedHashMap<String, Object>();
        return switch (chosenFormat) {
            case DC_SD_JWT -> {
                payload.put("sub", principal.getId());
                payload.put("client_id", context.accessToken().getClientId());
                payload.put("credential_configuration_id", configurationId);
                payload.put("claims", vcClaims);
                payload.put("cnf", proof.holderJwk().toJSONObject());
                payload.put("iss", issuer);
                yield signAndProduceCredentialResponse(context, payload, configuration, proof);
            }
            case JWT_VC_JSON -> {
                val credentialSubject = new LinkedHashMap<String, Object>();
                credentialSubject.put("id", principal.getId());
                credentialSubject.putAll(vcClaims);

                val vc = new LinkedHashMap<String, Object>();
                vc.put("@context", List.of("https://www.w3.org/2018/credentials/v1"));
                vc.put("type", List.of("VerifiableCredential", configuration.getScope()));
                vc.put("credentialSubject", credentialSubject);
                payload.put("vc", vc);

                val now = Instant.now(Clock.systemUTC());
                payload.put("iss", issuer);
                payload.put("sub", principal.getId());
                payload.put("iat", now.getEpochSecond());
                payload.put("nbf", now.getEpochSecond());
                payload.put("jti", UUID.randomUUID().toString());
                payload.put("cnf", proof.holderJwk().toJSONObject());
                yield signAndProduceCredentialResponse(context, payload, configuration, proof);
            }
            case JWT_VC_JSON_LD -> {
                val now = Instant.now(Clock.systemUTC());
                val credentialSubject = new LinkedHashMap<String, Object>();
                credentialSubject.put("id", principal.getId());
                credentialSubject.putAll(vcClaims);

                payload.put("@context", List.of(
                    "https://www.w3.org/ns/credentials/v2",
                    issuer + "/contexts/" + configuration.getScope() + "-v1.jsonld"
                ));

                val id = UUID.randomUUID();
                payload.put("id", "urn:uuid:" + id);
                payload.put("sub", principal.getId());
                payload.put("type", List.of("VerifiableCredential", configuration.getScope()));
                payload.put("credentialSubject", credentialSubject);
                payload.put("issuer", issuer);
                payload.put("validFrom", now.toString());
                payload.put("validUntil", now.plus(CLAIM_VALIDITY_IN_MINUTES, ChronoUnit.MINUTES).toString());
                payload.put("iat", now.getEpochSecond());
                payload.put("nbf", now.getEpochSecond());
                payload.put("jti", id.toString());
                yield signAndProduceCredentialResponse(context, payload, configuration, proof);
            }
        };
    }

    private static @NonNull CredentialConfigurationFormats determineCredentialFormat(
        final OidcVerifiableCredentialConfigurationProperties configuration) {
        return Arrays.stream(CredentialConfigurationFormats.values())
            .filter(format -> format.getFormat().equalsIgnoreCase(configuration.getFormat()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported format %s for credential configuration with scope %s"
                .formatted(configuration.getFormat(), configuration.getScope())));
    }

    protected OidcVerifiableCredentialIssuerResponse signAndProduceCredentialResponse(
        final OidcVerifiableCredentialValidationContext context,
        final Map<String, Object> payload,
        final OidcVerifiableCredentialConfigurationProperties configuration,
        final OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult proof) throws Throwable {

        val signedCredential = signVerifiableCredential(payload, context, configuration);
        return new OidcVerifiableCredentialIssuerResponse(
            configuration.getFormat(),
            signedCredential,
            proof.nonce()
        );
    }

    protected String signVerifiableCredential(final Map<String, Object> payload,
                                              final OidcVerifiableCredentialValidationContext context,
                                              final OidcVerifiableCredentialConfigurationProperties configuration) throws Throwable {
        val jwtClaims = new JwtClaims();
        jwtClaims.setSubject(payload.get("sub").toString());
        jwtClaims.setIssuedAtToNow();
        jwtClaims.setExpirationTimeMinutesInTheFuture(CLAIM_VALIDITY_IN_MINUTES);

        jwtClaims.setStringClaim("typ", "vc+jwt");
        jwtClaims.setStringClaim("cty", "vc");

        val format = determineCredentialFormat(configuration);
        val claims = (Map<String, Object>) switch (format) {
            case DC_SD_JWT -> payload.get("claims");
            case JWT_VC_JSON -> payload.get("vc");
            case JWT_VC_JSON_LD -> payload.get("credentialSubject");
        };

        val disclosures = new ArrayList<Disclosure>();
        if (format == CredentialConfigurationFormats.DC_SD_JWT) {
            val sdBuilder = new SDObjectBuilder();
            claims.forEach((claimName, claimValue) -> {
                val claimDefn = configuration.getClaims().get(claimName);
                if (claimDefn.isDisclosable()) {
                    val disclosure = new Disclosure(claimName, claimValue);
                    disclosures.add(disclosure);
                    sdBuilder.putSDClaim(disclosure);
                } else {
                    sdBuilder.putClaim(claimName, claimValue);
                }
            });
            sdBuilder.build().forEach(jwtClaims::setClaim);
        } else {
            claims.forEach(jwtClaims::setClaim);
        }

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(),
            context.accessToken().getClientId());
        val signedClaims = configurationContext.getIdTokenSigningAndEncryptionService()
            .encode(Objects.requireNonNull(registeredService), jwtClaims);
        return format == CredentialConfigurationFormats.DC_SD_JWT
            ? new SDJWT(signedClaims, disclosures).toString()
            : signedClaims;
    }
    
    protected Map<String, Object> produceClaims(final Principal principal, final OidcVerifiableCredentialValidationContext context) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val configurationId = resolveRequestedConfigurationId(context);
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
                claims.put(claimName, claimValue);
            }
        });
        return claims;
    }

    protected String resolveRequestedConfigurationId(final OidcVerifiableCredentialValidationContext context) {
        val principal = context.accessToken().getAuthentication().getPrincipal();
        return StringUtils.isBlank(context.credentialRequest().getCredentialConfigurationId())
            ? principal.getAttributes().get("credentialConfigurationIds").getFirst().toString()
            : context.credentialRequest().getCredentialConfigurationId();
    }
}
