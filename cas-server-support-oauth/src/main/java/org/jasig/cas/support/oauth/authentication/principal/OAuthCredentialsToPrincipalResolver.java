package org.jasig.cas.support.oauth.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class resolves the principal id regarding the OAuth credentials : principal id is the type of the provider # the user identifier.
 * 
 * @author Jerome Leleu
 */
public class OAuthCredentialsToPrincipalResolver extends AbstractPersonDirectoryCredentialsToPrincipalResolver
    implements CredentialsToPrincipalResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthCredentialsToPrincipalResolver.class);
    
    @Override
    protected String extractPrincipalId(final Credentials credentials) {
        
        OAuthCredentials oauthCredentials = (OAuthCredentials) credentials;
        
        String uid = oauthCredentials.getProviderType() + "#" + oauthCredentials.getUserId();
        
        logger.debug("uid : {}", uid);
        
        return uid;
    }
    
    /**
     * Return true if Credentials are OAuthCredentials, false otherwise.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null && (OAuthCredentials.class.isAssignableFrom(credentials.getClass()));
    }
}
