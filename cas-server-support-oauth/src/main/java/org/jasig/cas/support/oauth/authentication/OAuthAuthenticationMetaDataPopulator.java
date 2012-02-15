package org.jasig.cas.support.oauth.authentication;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;

/**
 * This class is a meta data populator for OAuth authentication. As attributes are stored in OAuthCredentials, they are added to the
 * returned principal.
 * 
 * @author Jerome Leleu
 */
public class OAuthAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {
    
    public Authentication populateAttributes(Authentication authentication, Credentials credentials) {
        if (credentials instanceof OAuthCredentials) {
            OAuthCredentials oauthCredentials = (OAuthCredentials) credentials;
            authentication.getAttributes().putAll(oauthCredentials.getUserAttributes());
        }
        return authentication;
    }
}
