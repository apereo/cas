package org.jasig.cas.support.oauth.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a common implementation of provider for OAuth protocol v2.0.
 * 
 * @author Jerome Leleu
 */
public abstract class BaseOAuth20Provider extends BaseOAuthProvider {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseOAuth20Provider.class);
    
    @Override
    public String getAuthorizationUrl(HttpSession session) {
        // no requestToken for OAuth 2.0 -> no need to save it in session
        String authorizationUrl = service.getAuthorizationUrl(null);
        logger.debug("authorizationUrl : {}", authorizationUrl);
        return authorizationUrl;
    }
    
    @Override
    public Token getAccessToken(HttpSession session, String token, String verifier) {
        logger.debug("verifier : {}", verifier);
        Verifier providerVerifier = new Verifier(verifier);
        // no request token saved in session (OAuth v2.0)
        Token accessToken = service.getAccessToken(null, providerVerifier);
        logger.debug("accessToken : {}", accessToken);
        return accessToken;
    }
    
    @Override
    public String extractTokenFromRequest(HttpServletRequest request) {
        return null;
    }
    
    @Override
    public String extractVerifierFromRequest(HttpServletRequest request) {
        return request.getParameter("code");
    }
}
