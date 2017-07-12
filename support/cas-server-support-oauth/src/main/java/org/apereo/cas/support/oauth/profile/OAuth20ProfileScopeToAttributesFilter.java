package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.pac4j.core.context.J2EContext;

/**
 * This is {@link OAuth20ProfileScopeToAttributesFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface OAuth20ProfileScopeToAttributesFilter {

    /**
     * Filter attributes.
     *
     * @param service           the service
     * @param profile           the profile
     * @param registeredService the registered service
     * @param context           the context
     * @return the map
     */
    Principal filter(Service service, Principal profile,
                     RegisteredService registeredService,
                     J2EContext context);

    /**
     * Reconcile the service definition.
     * Usual operations involve translating scopes
     * to attribute release policies if needed.
     * The operation is expected to persist service changes.
     *
     * @param service the service
     */
    default void reconcile(RegisteredService service) {
    }
}
