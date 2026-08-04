package org.apereo.cas.oidc.vc.issuer;

import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialEncoderFactory;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.util.List;

/**
 * This is {@link OidcDefaultVerifiableCredentialIssuerService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcDefaultVerifiableCredentialIssuerService implements OidcVerifiableCredentialIssuerService {
    protected final OidcVerifiableCredentialProofValidator credentialProofValidator;
    protected final OidcVerifiableCredentialEncoderFactory credentialEncoderFactory;

    @Override
    public List<OidcVerifiableCredentialIssuerResponse> issue(final OidcVerifiableCredentialValidationContext context) throws Throwable {
        val proof = credentialProofValidator.validate(context.credentialRequest());
        val configuration = context.resolveCredentialId();
        val encoder = credentialEncoderFactory.findByConfiguration(configuration);
        val signedCredential = encoder.encode(context, proof);
        return List.of(new OidcVerifiableCredentialIssuerResponse(
            encoder.getFormat(),
            signedCredential,
            proof.nonce()
        ));
    }
}
