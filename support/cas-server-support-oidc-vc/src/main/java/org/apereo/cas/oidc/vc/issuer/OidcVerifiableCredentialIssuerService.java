package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult;

/**
 * This is {@link OidcVerifiableCredentialIssuerService}. Issuance happens in two steps: the proofs of possession are
 * validated, which yields the holder keys, and a credential is then encoded for each key. Deferred issuance keeps the
 * validated holder keys and encodes the credentials later.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface OidcVerifiableCredentialIssuerService {

    /**
     * Validate the proofs of possession of the credential request.
     *
     * @param context        the context
     * @param consumedNonces nonces already consumed while handling the current credential request
     * @return the proof results, one per holder key a credential is issued for
     * @throws Throwable the throwable
     */
    List<VerifiableCredentialProofResult> validateProofs(OidcVerifiableCredentialValidationContext context,
                                                         Set<String> consumedNonces)
            throws Throwable;

    /**
     * Encode one credential per validated holder key.
     *
     * @param context the context
     * @param proofs  the validated proofs
     * @return the issued credentials
     * @throws Throwable the throwable
     */
    List<OidcVerifiableCredentialIssuerResponse> encode(OidcVerifiableCredentialValidationContext context,
                                                        List<VerifiableCredentialProofResult> proofs)
            throws Throwable;

    /**
     * Issue verifiable credential response.
     *
     * @param context        the context
     * @param consumedNonces nonces already consumed while handling the current credential request
     * @return the verifiable credential response
     * @throws Throwable the throwable
     */
    default List<OidcVerifiableCredentialIssuerResponse> issue(final OidcVerifiableCredentialValidationContext context,
                                                               final Set<String> consumedNonces)
            throws Throwable {
        return encode(context, validateProofs(context, consumedNonces));
    }

    /**
     * Issue verifiable credential response.
     *
     * @param context the context
     * @return the verifiable credential response
     * @throws Throwable the throwable
     */
    default List<OidcVerifiableCredentialIssuerResponse> issue(
        final OidcVerifiableCredentialValidationContext context)
            throws Throwable {
        return issue(context, new HashSet<>());
    }
}
