package org.apereo.cas.oidc.vc.issuer.enc;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import com.authlete.sd.Disclosure;
import com.authlete.sd.SDJWT;
import com.authlete.sd.SDObjectBuilder;
import lombok.Getter;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialDcSdJwtEncoder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OidcVerifiableCredentialDcSdJwtEncoder extends OidcVerifiableCredentialBaseEncoder {
    private final OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats format =
        OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.DC_SD_JWT;

    public OidcVerifiableCredentialDcSdJwtEncoder(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public String encode(final OidcVerifiableCredentialValidationContext context,
                         final OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult proof) throws Throwable {
        val authentication = Objects.requireNonNull(context.accessToken().getAuthentication());
        val principal = configurationContext.getPrincipalResolver().resolve(
            new BasicIdentifiableCredential(authentication.getPrincipal().getId()));

        val verifiableClaims = produceClaims(Objects.requireNonNull(principal), context);
        val configurationId = context.resolveCredentialId();
        val configuration = resolveConfiguration(configurationId);

        val disclosures = new ArrayList<Disclosure>();
        val signedClaims = sign(principal.getId(), context, jwtClaims -> {
            val sdBuilder = new SDObjectBuilder();
            verifiableClaims.forEach((claimName, claimValue) -> {
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
            jwtClaims.setStringClaim("sub", principal.getId());
            jwtClaims.setStringClaim("client_id", context.accessToken().getClientId());
            jwtClaims.setStringClaim("credential_configuration_id", configurationId);
            jwtClaims.setClaim("cnf", proof.holderJwk().toJSONObject());
        });
        return new SDJWT(signedClaims, disclosures).toString();
    }
}
