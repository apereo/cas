package org.jasig.cas.support.oauth.authentication.handler.support;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.jasig.cas.support.oauth.provider.OAuthProvider;
import org.scribe.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.context.ExternalContextHolder;

/**
 * This handler authenticates OAuth credentials : it uses the token and verifier to get an access token to get the user identifier returned
 * by the provider for an authenticated user.
 * 
 * @author Jerome Leleu
 */
public class OAuthAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    
    protected static final Logger logger = LoggerFactory.getLogger(OAuthAuthenticationHandler.class);
    
    @NotNull
    private List<OAuthProvider> providers;
    
    public boolean supports(Credentials credentials) {
        return credentials != null && (OAuthCredentials.class.isAssignableFrom(credentials.getClass()));
    }
    
    @Override
    protected boolean doAuthentication(Credentials credentials) throws AuthenticationException {
        OAuthCredentials oauthCredentials = (OAuthCredentials) credentials;
        
        String token = oauthCredentials.getToken();
        logger.debug("token : {}", token);
        String verifier = oauthCredentials.getVerifier();
        logger.debug("verifier : {}", verifier);
        
        String providerName = oauthCredentials.getProviderName();
        logger.debug("providerName : {}", providerName);
        // get provider
        OAuthProvider provider = null;
        for (OAuthProvider aProvider : providers) {
            if (StringUtils.equals(providerName, aProvider.getName())) {
                provider = aProvider;
                break;
            }
        }
        
        HttpServletRequest request = (HttpServletRequest) ExternalContextHolder.getExternalContext().getNativeRequest();
        Token accessToken = provider.getAccessToken(request.getSession(), token, verifier);
        String userId = provider.getUserId(accessToken);
        logger.debug("userId : {}", userId);
        
        if (StringUtils.isNotBlank(userId)) {
            oauthCredentials.setUserId(userId);
            return true;
        } else {
            oauthCredentials.setUserId(null);
            return false;
        }
    }
    
    public void setProviders(List<OAuthProvider> providers) {
        this.providers = providers;
    }
}
