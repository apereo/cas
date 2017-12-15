package org.apereo.cas.authentication.principal.resolvers;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link EchoingPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class EchoingPrincipalResolver implements PrincipalResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(EchoingPrincipalResolver.class);

    @Override
    public Principal resolve(final Credential credential, final Principal principal, final AuthenticationHandler handler) {
        LOGGER.debug("Echoing back the authenticated principal [{}]", principal);
        return principal;
    }

    @Override
    public boolean supports(final Credential credential) {
        return StringUtils.isNotBlank(credential.getId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        return null;
    }
}
