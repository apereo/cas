package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
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
     * because {@code ath} hashes the presented value.
     *
     * @param webContext           the web context
     * @param presentedAccessToken the access token exactly as the client presented it
     * @param accessToken          the access token ticket the presented value resolved to
     * @throws Throwable when the request carries no proof for a sender-constrained token, or the
     *                   proof does not verify
     */
    void validateProtectedResourceRequest(WebContext webContext, String presentedAccessToken,
                                          OAuth20AccessToken accessToken) throws Throwable;
}
