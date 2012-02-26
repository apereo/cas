/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
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
 * @since 3.5.0
 */
public final class OAuthCredentialsToPrincipalResolver extends AbstractPersonDirectoryCredentialsToPrincipalResolver
    implements CredentialsToPrincipalResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthCredentialsToPrincipalResolver.class);
    
    @Override
    protected String extractPrincipalId(final Credentials credentials) {
        OAuthCredentials oauthCredentials = (OAuthCredentials) credentials;
        String principalId = oauthCredentials.getProviderType() + "#" + oauthCredentials.getUserId();
        logger.debug("principalId : {}", principalId);
        return principalId;
    }
    
    /**
     * Return true if Credentials are OAuthCredentials, false otherwise.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null && (OAuthCredentials.class.isAssignableFrom(credentials.getClass()));
    }
}
