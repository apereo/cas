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
 * This is {@link OidcVerifiableCredentialJwtVcJsonEncoder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OidcVerifiableCredentialJwtVcJsonEncoder extends BaseOidcVerifiableCredentialEncoder {
    private final OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats format =
        OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.JWT_VC_JSON;

    public OidcVerifiableCredentialJwtVcJsonEncoder(final OidcConfigurationContext configurationContext) {
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

        return sign(principal.getId(), context, proof, jwtClaims -> {
            jwtClaims.setStringClaim("sub", principal.getId());
            jwtClaims.setStringClaim("client_id", context.accessToken().getClientId());
            jwtClaims.setStringClaim("credential_configuration_id", configurationId);

            val credentialSubject = new LinkedHashMap<String, Object>();
            credentialSubject.put("id", principal.getId());
            credentialSubject.putAll(verifiableClaims);
            val vc = new LinkedHashMap<String, Object>();
            vc.put("@context", List.of("https://www.w3.org/2018/credentials/v1"));
            vc.put("type", List.of("VerifiableCredential", configuration.getScope()));
            vc.put("credentialSubject", credentialSubject);
            jwtClaims.setClaim("vc", vc);

            verifiableClaims.forEach(jwtClaims::setClaim);
        });
    }
}
