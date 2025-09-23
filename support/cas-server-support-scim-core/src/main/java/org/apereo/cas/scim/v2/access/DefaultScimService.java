package org.apereo.cas.scim.v2.access;

import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.scim.v2.BaseScimService;

/**
 * This is {@link DefaultScimService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class DefaultScimService extends BaseScimService<ScimProperties> {
    public DefaultScimService(final ScimProperties scimProperties) {
        super(scimProperties);
    }
}
