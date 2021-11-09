package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;

import lombok.ToString;

import java.util.Optional;

/**
 * Implementation of PrincipalResolver that converts the OpenId
 * user name to a Principal.
 *
 * @author Scott Battaglia
 * @since 3.1
 * @deprecated 6.2
 */
@ToString(callSuper = true)
@Deprecated(since = "6.2.0")
public class OpenIdPrincipalResolver extends PersonDirectoryPrincipalResolver {

    public OpenIdPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenIdCredential;
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        return ((OpenIdCredential) credential).getUsername();
    }
}
