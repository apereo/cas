package org.jasig.cas.support.oauth.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.scribe.model.Token;

/**
 * This interface represents an identity provider using OAuth protocol.
 * 
 * @author Jerome Leleu
 */
public interface OAuthProvider {
    
    /**
     * Gets the name of the provider.
     */
    public String getName();
    
    /**
     * Gets the authorization url.
     * 
     * @param session
     * @return
     */
    public String getAuthorizationUrl(HttpSession session);
    
    /**
     * Retrieves the access token from the token, the verifier and the session.
     * 
     * @param session
     * @param token
     * @param verifier
     * @return
     */
    public Token getAccessToken(HttpSession session, String token, String verifier);
    
    /**
     * Retrieves the user identifier from the access token.
     * 
     * @param accessToken
     * @return
     */
    public String getUserId(Token accessToken);
    
    /**
     * Extracts the token from the request.
     * 
     * @param request
     * @return
     */
    public String extractTokenFromRequest(HttpServletRequest request);
    
    /**
     * Extracts the verifier from the request.
     * 
     * @param request
     * @return
     */
    public String extractVerifierFromRequest(HttpServletRequest request);
}
