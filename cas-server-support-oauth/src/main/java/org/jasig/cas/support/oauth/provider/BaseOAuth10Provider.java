package org.jasig.cas.support.oauth.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a common implementation of provider for OAuth protocol v1.0.
 * 
 * @author Jerome Leleu
 */
public abstract class BaseOAuth10Provider extends BaseOAuthProvider {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseOAuth10Provider.class);
    
    @Override
    public String getAuthorizationUrl(HttpSession session) {
        Token requestToken = service.getRequestToken();
        logger.debug("requestToken : {}", requestToken);
        // save requestToken in session
        session.setAttribute(name + "#tokenRequest", requestToken);
        String authorizationUrl = service.getAuthorizationUrl(requestToken);
        logger.debug("authorizationUrl : {}", authorizationUrl);
        return authorizationUrl;
    }
    
    @Override
    public Token getAccessToken(HttpSession session, String token, String verifier) {
        logger.debug("verifier : {}", verifier);
        logger.debug("token : {}", token);
        // get tokenRequest from session
        Token tokenRequest = (Token) session.getAttribute(name + "#tokenRequest");
        logger.debug("tokenRequest : {}", tokenRequest);
        if (tokenRequest == null) {
            throw new OAuthException("Token request expired");
        }
        String savedToken = tokenRequest.getToken();
        logger.debug("savedToken : {}", savedToken);
        if (!StringUtils.equals(token, savedToken)) {
            throw new OAuthException("Token received : " + token + " is different from saved token : " + savedToken);
        }
        Verifier providerVerifier = new Verifier(verifier);
        Token accessToken = service.getAccessToken(tokenRequest, providerVerifier);
        logger.debug("accessToken : {}", accessToken);
        return accessToken;
    }
    
    @Override
    public String extractTokenFromRequest(HttpServletRequest request) {
        return request.getParameter("oauth_token");
    }
    
    @Override
    public String extractVerifierFromRequest(HttpServletRequest request) {
        return request.getParameter("oauth_verifier");
    }
}
