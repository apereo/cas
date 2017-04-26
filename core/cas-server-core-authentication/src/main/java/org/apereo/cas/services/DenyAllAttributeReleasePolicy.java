package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A deny rule to refuse all service from receiving attributes, whether default or not.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DenyAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6215588543966639050L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DenyAllAttributeReleasePolicy.class);

    public DenyAllAttributeReleasePolicy() {
        setExcludeDefaultAttributes(true);
        setPrincipalIdAttribute(null);
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attributes,
                                                        final RegisteredService service) {
        LOGGER.debug("Ignoring all attributes given the service is designed to never receive any.");
        return Collections.emptyMap();
    }

    @Override
    public boolean isExcludeDefaultAttributes() {
        return true;
    }

    @Override
    public String getPrincipalIdAttribute() {
        return null;
    }

    @Override
    protected Map<String, Object> returnFinalAttributesCollection(final Map<String, Object> attributesToRelease,
                                                                  final RegisteredService service) {
        LOGGER.info("CAS will not authorize anything for release, given the service is denied access to all attributes. "
                + "If there are any default attributes set to be released to all services, "
                + "those are also skipped for [{}]", service);
        return new HashMap<>();
    }
}
