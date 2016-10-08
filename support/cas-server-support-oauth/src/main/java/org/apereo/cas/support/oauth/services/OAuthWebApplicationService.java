package org.apereo.cas.support.oauth.services;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.services.RegisteredService;

/**
 * OAuth web application service.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class OAuthWebApplicationService extends AbstractWebApplicationService {

    /**
     * Instantiates a new OAuth web application service impl.
     *
     * @param registeredService the registered service
     */
    public OAuthWebApplicationService(final RegisteredService registeredService) {
        super(registeredService != null ? String.valueOf(registeredService.getId()) : null, null, null, null);
    }
}
