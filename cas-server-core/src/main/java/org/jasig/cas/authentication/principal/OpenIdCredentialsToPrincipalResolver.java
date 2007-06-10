/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Implementation of CredentialsToPrincipalResolver that converts the OpenId
 * user name to a Principal.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public class OpenIdCredentialsToPrincipalResolver implements
    CredentialsToPrincipalResolver {

    public Principal resolvePrincipal(final Credentials credentials) {
        final OpenIdCredentials c = (OpenIdCredentials) credentials;
        return new SimplePrincipal(c.getUsername());
    }

    public boolean supports(final Credentials credentials) {
        return credentials instanceof OpenIdCredentials;
    }

}
