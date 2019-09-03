package org.apereo.cas.ticket.accesstoken;

/**
 * This is {@link OAuth20AccessTokenIdExtractor}.
 *
 * @author charlibot
 * @since 6.1.0
 */
@FunctionalInterface
public interface OAuth20AccessTokenIdExtractor {

    /**
     * Gets the access token id from a request.
     *
     * @param accessTokenFromRequest String either AT-... or the jwt access token
     * @return Access token id
     */
    String extractId(String accessTokenFromRequest);

}
