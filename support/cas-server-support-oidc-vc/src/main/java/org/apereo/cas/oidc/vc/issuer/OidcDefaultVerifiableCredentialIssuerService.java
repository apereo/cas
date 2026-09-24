package org.apereo.cas.oidc.vc.issuer;

import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialEncoderFactory;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public List<OidcVerifiableCredentialIssuerResponse> issue(final OidcVerifiableCredentialValidationContext context,
                                                              final Set<String> consumedNonces) throws Throwable {
        val configuration = context.resolveConfigurationId();
        val encoder = credentialEncoderFactory.findByConfiguration(configuration);
        val responses = new ArrayList<OidcVerifiableCredentialIssuerResponse>();
        for (val proofJwt : context.resolveProofs()) {
            val proof = credentialProofValidator.validate(proofJwt, consumedNonces);
            responses.add(new OidcVerifiableCredentialIssuerResponse(encoder.encode(context, proof)));
        }
        return responses;
    }
}
