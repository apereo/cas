package org.apereo.cas.support.spnego.authentication.principal;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;
import lombok.NoArgsConstructor;

/**
 * Implementation of a CredentialToPrincipalResolver that takes a
 * SpnegoCredential and returns a SimplePrincipal.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @since 3.1
 */
@Slf4j
@ToString(callSuper = true)
@NoArgsConstructor
public class SpnegoPrincipalResolver extends PersonDirectoryPrincipalResolver {

    public SpnegoPrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                   final boolean returnNullIfNoAttributes, final PrincipalNameTransformer principalNameTransformer, final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalNameTransformer, principalAttributeName);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Principal currentPrincipal) {
        final SpnegoCredential c = (SpnegoCredential) credential;
        final String id = c.getPrincipal().getId();
        return id;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && SpnegoCredential.class.equals(credential.getClass());
    }
}
