package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.pac4j.core.context.J2EContext;

/**
 * This is {@link DefaultOAuth20ProfileScopeToAttributesFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultOAuth20ProfileScopeToAttributesFilter implements OAuth20ProfileScopeToAttributesFilter {
    @Override
    public Principal filter(final Service service, final Principal profile,
                            final RegisteredService registeredService,
                            final J2EContext context) {
        return profile;
    }
}
