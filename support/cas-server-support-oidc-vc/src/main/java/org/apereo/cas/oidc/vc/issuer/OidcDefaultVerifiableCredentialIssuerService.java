package org.apereo.cas.oidc.vc.issuer;

import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialEncoderFactory;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult;
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
    public List<VerifiableCredentialProofResult> validateProofs(final OidcVerifiableCredentialValidationContext context,
                                                                final Set<String> consumedNonces)
            throws Throwable {
        val configuration = context.resolveConfigurationId();
        val attestationProof = context.resolveAttestationProof();
        if (attestationProof != null) {
            return credentialProofValidator.validateAttestation(attestationProof, configuration, consumedNonces);
        }
        val proofs = new ArrayList<VerifiableCredentialProofResult>();
        for (val proofJwt : context.resolveProofs()) {
            proofs.add(credentialProofValidator.validate(proofJwt, configuration, consumedNonces));
        }
        return proofs;
    }

    @Override
    public List<OidcVerifiableCredentialIssuerResponse> encode(final OidcVerifiableCredentialValidationContext context,
                                                               final List<VerifiableCredentialProofResult> proofs)
            throws Throwable {
        val encoder = credentialEncoderFactory.findByConfiguration(context.resolveConfigurationId());
        val responses = new ArrayList<OidcVerifiableCredentialIssuerResponse>();
        for (val proof : proofs) {
            responses.add(new OidcVerifiableCredentialIssuerResponse(encoder.encode(context, proof)));
        }
        return responses;
    }
}
