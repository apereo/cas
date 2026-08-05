package org.apereo.cas.oidc.vc.issuer.enc;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import lombok.Getter;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialJwtVcJsonLdEncoder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OidcVerifiableCredentialJwtVcJsonLdEncoder extends BaseOidcVerifiableCredentialEncoder {
    private final OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats format =
        OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.JWT_VC_JSON_LD;

    public OidcVerifiableCredentialJwtVcJsonLdEncoder(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public String encode(final OidcVerifiableCredentialValidationContext context,
                         final OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult proof) throws Throwable {
        val authentication = Objects.requireNonNull(context.accessToken().getAuthentication());
        val principal = configurationContext.getPrincipalResolver().resolve(
            new BasicIdentifiableCredential(authentication.getPrincipal().getId()));
        val verifiableClaims = produceClaims(Objects.requireNonNull(principal), context);
        val configurationId = context.resolveConfigurationId();
        val configuration = resolveConfiguration(configurationId);
        val issuer = configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer();

        return sign(principal.getId(), context, proof, jwtClaims -> {
            jwtClaims.setStringClaim("sub", principal.getId());
            jwtClaims.setStringClaim("client_id", context.accessToken().getClientId());
            jwtClaims.setStringClaim("credential_configuration_id", configurationId);
            
            val now = Instant.now(Clock.systemUTC());
            val credentialSubject = new LinkedHashMap<String, Object>();
            credentialSubject.put("id", principal.getId());
            credentialSubject.putAll(verifiableClaims);

            jwtClaims.setStringListClaim("@context", List.of(
                "https://www.w3.org/ns/credentials/v2",
                issuer + "/contexts/" + configuration.getScope() + "-v1.jsonld"
            ));

            jwtClaims.setStringClaim("id", "urn:uuid:" + jwtClaims.getJwtId());
            jwtClaims.setStringListClaim("type", List.of("VerifiableCredential", configuration.getScope()));
            jwtClaims.setClaim("credentialSubject", credentialSubject);
            jwtClaims.setStringClaim("issuer", issuer);
            jwtClaims.setStringClaim("validFrom", now.toString());
            jwtClaims.setStringClaim("validUntil", now.plus(CLAIM_VALIDITY_IN_MINUTES, ChronoUnit.MINUTES).toString());
            jwtClaims.setClaim("iat", now.getEpochSecond());
            jwtClaims.setClaim("nbf", now.getEpochSecond());

            verifiableClaims.forEach(jwtClaims::setClaim);
        });
    }
}
