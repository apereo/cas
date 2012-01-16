package org.jasig.cas.support.oauth.provider;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.cas.support.oauth.provider.impl.GoogleProvider;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class is a default implementation of an OAuth protocol provider based on the Scribe library. It should work for all OAuth providers.
 * In subclasses, some methods are to be implemented / customized for specific needs depending on the provider.
 * 
 * @author Jerome Leleu
 */
public abstract class BaseOAuthProvider implements OAuthProvider, InitializingBean {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseOAuthProvider.class);
    
    protected OAuthService service;
    
    protected String name;
    
    protected String key;
    
    protected String secret;
    
    protected String callbackUrl;
    
    private String loginUrl;
    
    public void afterPropertiesSet() throws Exception {
        this.callbackUrl = loginUrl + "?oauth_provider=" + URLEncoder.encode(name);
        initService();
    }
    
    protected abstract void initService();
    
    /**
     * Gets the name of the provider.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the authorization url.
     * 
     * @param session
     * @return
     */
    public abstract String getAuthorizationUrl(HttpSession session);
    
    /**
     * Retrieves the access token from the token, the verifier and the session.
     * 
     * @param session
     * @param token
     * @param verifier
     * @return
     */
    public abstract Token getAccessToken(HttpSession session, String token, String verifier);
    
    /**
     * Retrieves the user identifier from the access token.
     * 
     * @param accessToken
     * @return
     */
    public String getUserId(Token accessToken) {
        String body = sendRequestForProfile(accessToken, getProfileUrl());
        if (body == null) {
            return null;
        }
        return extractUserId(body);
    }
    
    /**
     * Retrieves the url of the profile of the authenticated user for this provider.
     * 
     * @return
     */
    protected abstract String getProfileUrl();
    
    /**
     * Makes a request to get the profile of the authenticated user for this provider.
     * 
     * @param accessToken
     * @param profileUrl
     * @return
     */
    protected String sendRequestForProfile(Token accessToken, String profileUrl) {
        logger.debug("accessToken : {} / profileUrl : {}", accessToken, profileUrl);
        OAuthRequest request = new OAuthRequest(Verb.GET, profileUrl);
        service.signRequest(accessToken, request);
        // for Google
        if (this instanceof GoogleProvider) {
            request.addHeader("GData-Version", "3.0");
        }
        Response response = request.send();
        int code = response.getCode();
        String body = response.getBody();
        logger.debug("response code : {} / response body : {}", code, body);
        if (code != 200) {
            logger.error("Get the user profile failed, code : " + code + " / body : " + body);
            return null;
        }
        return body;
    }
    
    /**
     * Extracts the user identifier from the response (JSON, XML...) of the profile url.
     * 
     * @param body
     * @return
     */
    protected abstract String extractUserId(String body);
    
    public String extractTokenFromRequest(HttpServletRequest request) {
        return request.getParameter("oauth_token");
    }
    
    public String extractVerifierFromRequest(HttpServletRequest request) {
        return request.getParameter("oauth_verifier");
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
    
    @Override
    public String toString() {
        return "[" + name + "]";
    }
}
