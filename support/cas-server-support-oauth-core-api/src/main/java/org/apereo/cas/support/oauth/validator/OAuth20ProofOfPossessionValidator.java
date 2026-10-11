package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import com.nimbusds.jose.JWSAlgorithm;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.WebContext;

/**
 * This is {@link OAuth20ProofOfPossessionValidator}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OAuth20ProofOfPossessionValidator {

    /**
     * Validate.
     *
     * @param webContext  the web context
     * @param accessToken the access token
     * @throws Throwable the throwable
     */
    void validate(WebContext webContext, @Nullable OAuth20AccessToken accessToken) throws Throwable;

    /**
     * Validate.
     *
     * @param webContext the web context
     * @throws Throwable the throwable
     */
    default void validate(final WebContext webContext) throws Throwable {
        validate(webContext, null);
    }

    /**
     * Verify the DPoP proof of a token request whose client is not the authenticated profile, such as device code
     * polling or the JWT bearer grant. A verified proof and the SHA-256 JWK thumbprint of its key are put on the request,
     * under {@code DPoP} and {@code DPoPConfirmation}, for the token to be bound to that key. A request without a proof
     * from a client registered with {@code dpop_bound_access_tokens} is refused (RFC 9449, section 5.2).
     *
     * @param webContext        the web context
     * @param registeredService the client that makes the token request
     * @throws Throwable when the proof does not verify, or is missing for a client that must present one
     */
    void validateTokenRequest(WebContext webContext, OAuthRegisteredService registeredService) throws Throwable;

    /**
     * Verify the DPoP proof that accompanies a request to a protected resource.
     * <p>
     * This is a different check from the one above, which belongs to the token endpoint. RFC 9449,
     * section 7.1 binds a proof presented at a protected resource to the access token itself through
     * the {@code ath} claim, and to the confirmation the authorization server recorded when it issued
     * the token. Verifying only {@code htm}, {@code htu} and {@code jti}, as the token endpoint does,
     * would leave a stolen token as useful as a legitimately held one, which is the whole point the
     * sender constraint exists to deny.
     * <p>
     * The access token as the client presented it is required rather than its decoded identifier,
     * because {@code ath} hashes the presented value. A sender-constrained token must also arrive in
     * the {@code Authorization} header under the {@code DPoP} scheme, never as a bearer token
     * (RFC 9449, section 7.2).
     *
     * @param webContext           the web context
     * @param presentedAccessToken the access token exactly as the client presented it
     * @param accessToken          the access token ticket the presented value resolved to
     * @throws Throwable when the request carries no proof for a sender-constrained token, or the
     *                   proof does not verify; {@link DPoPBoundAccessTokenDowngradeException} when
     *                   the token is not presented under the {@code DPoP} scheme
     */
    void validateProtectedResourceRequest(WebContext webContext, String presentedAccessToken,
                                          OAuth20AccessToken accessToken)
            throws Throwable;

    /**
     * Verify the DPoP proof of a request that binds what it creates to the proof's key without issuing a token, such as a
     * pushed authorization request (RFC 9449, section 10.1). The proof is checked as at the token endpoint, nonce included,
     * and the authenticated profile is left untouched.
     *
     * @param webContext the web context
     * @return the SHA-256 JWK thumbprint of the proof's key, or empty when the request carries no DPoP proof
     * @throws Throwable when the proof does not verify
     */
    Optional<String> validateKeyBinding(WebContext webContext) throws Throwable;

    /**
     * The JWS algorithms accepted for DPoP proofs, which a protected resource lists in the {@code algs} parameter of its
     * {@code DPoP} challenges (RFC 9449, section 7.1).
     *
     * @return the accepted signing algorithms
     */
    Set<JWSAlgorithm> getAcceptedSigningAlgorithms();
}
