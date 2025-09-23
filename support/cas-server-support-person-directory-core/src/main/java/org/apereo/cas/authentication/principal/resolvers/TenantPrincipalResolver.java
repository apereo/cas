package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.Credential;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link TenantPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class TenantPrincipalResolver extends PersonDirectoryPrincipalResolver {
    public TenantPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    @Override
    public boolean supports(final Credential credential) {
        return super.supports(credential) && StringUtils.isNotBlank(credential.getTenant());
    }
}
