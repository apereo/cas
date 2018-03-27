package org.apereo.cas.services;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import java.util.Map;

/**
 * Return all attributes for the service, regardless of service settings.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ReturnAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 5519257723778012771L;

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> resolvedAttributes, final RegisteredService service) {
        return resolvedAttributes;
    }

}
