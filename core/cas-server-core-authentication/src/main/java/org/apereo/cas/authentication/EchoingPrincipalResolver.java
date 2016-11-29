package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;

/**
 * This is {@link EchoingPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class EchoingPrincipalResolver implements PrincipalResolver {
    @Override
    public Principal resolve(final Credential credential, final Principal principal) {
        return principal;
    }

    @Override
    public boolean supports(final Credential credential) {
        return StringUtils.isNotBlank(credential.getId());
    }
}
