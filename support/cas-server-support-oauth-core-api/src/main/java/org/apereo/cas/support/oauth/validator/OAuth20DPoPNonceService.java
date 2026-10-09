package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.Nonce;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.WebContext;
import org.springframework.http.HttpHeaders;

/**
 * Hands out and checks server-provided nonces for DPoP proofs (RFC 9449, sections 8 and 9). Once turned on, every DPoP proof
 * must carry one in its {@code nonce} claim.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OAuth20DPoPNonceService {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oauthDPoPNonceService";

    /**
     * The claim of a DPoP proof that carries the nonce.
     */
    String NONCE_CLAIM = "nonce";

    /**
     * Whether nonces are handed out, and therefore required in DPoP proofs.
     *
     * @return true when enabled
     */
    boolean isEnabled();

    /**
     * Create a nonce.
     *
     * @return the nonce
     * @throws Throwable the throwable
     */
    String create() throws Throwable;

    /**
     * Whether a nonce was handed out by CAS and has not expired. A nonce may be used more than once while valid.
     *
     * @param nonce the nonce
     * @return true when valid
     */
    boolean isValid(@Nullable String nonce);

    /**
     * Whether a verified DPoP proof satisfies the nonce requirement: nonces are not turned on, or its {@code nonce} claim
     * carries a valid one.
     *
     * @param proof the proof
     * @return true when accepted
     * @throws Exception the exception
     */
    default boolean isAccepted(final SignedJWT proof) throws Exception {
        return !isEnabled() || isValid(proof.getJWTClaimsSet().getStringClaim(NONCE_CLAIM));
    }

    /**
     * The nonces a DPoP proof verifier is to accept, so it lets the proof's own {@code nonce} claim through for this service to
     * check; with nonces turned off, none, and the verifier refuses proofs that carry a {@code nonce} claim.
     *
     * @param proof the proof
     * @return the nonces
     * @throws Exception the exception
     */
    default Set<Nonce> getAcceptedNonces(final SignedJWT proof) throws Exception {
        val nonce = proof.getJWTClaimsSet().getStringClaim(NONCE_CLAIM);
        return isEnabled() && StringUtils.isNotBlank(nonce) ? Set.of(new Nonce(nonce)) : Set.of();
    }

    /**
     * Hand out a fresh nonce in the {@code DPoP-Nonce} header of the response, which is then not to be cached.
     *
     * @param webContext the web context
     * @return the nonce
     * @throws Throwable the throwable
     */
    default String provide(final WebContext webContext) throws Throwable {
        val nonce = create();
        webContext.setResponseHeader(OAuth20Constants.DPOP_NONCE, nonce);
        webContext.setResponseHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return nonce;
    }
}
