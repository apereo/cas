package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.Credential;

/**
 * Implementation of PrincipalResolver that converts the OpenId
 * user name to a Principal.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdPrincipalResolver extends PersonDirectoryPrincipalResolver {

    @Override
    protected String extractPrincipalId(final Credential credential) {
        return ((OpenIdCredential) credential).getUsername();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenIdCredential;
    }

}
