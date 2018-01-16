package org.apereo.cas.authentication.principal.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;
import lombok.ToString;

/**
 * This is {@link EchoingPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
public class EchoingPrincipalResolver implements PrincipalResolver {

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
    public IPersonAttributeDao getAttributeRepository() {
        return null;
    }
}
