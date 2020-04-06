package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;

import lombok.ToString;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.Optional;
import java.util.Set;

/**
 * Implementation of PrincipalResolver that converts the OpenId
 * user name to a Principal.
 *
 * @author Scott Battaglia
 * @deprecated 6.2
 * @since 3.1
 */
@ToString(callSuper = true)
@Deprecated(since = "6.2.0")
public class OpenIdPrincipalResolver extends PersonDirectoryPrincipalResolver {

    public OpenIdPrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                   final boolean returnNullIfNoAttributes, final String principalAttributeName,
                                   final boolean useCurrentPrincipalId, final boolean resolveAttributes,
                                   final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, useCurrentPrincipalId, resolveAttributes,
            activeAttributeRepositoryIdentifiers);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        return ((OpenIdCredential) credential).getUsername();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenIdCredential;
    }
}
