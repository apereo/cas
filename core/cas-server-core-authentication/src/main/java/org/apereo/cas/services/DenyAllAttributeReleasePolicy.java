package org.apereo.cas.services;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
* A deny rule to refuse all service from receiving attributes, whether default or not.
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DenyAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6215588543966639050L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DenyAllAttributeReleasePolicy.class);
    
    public DenyAllAttributeReleasePolicy() {
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Map<String, Object> attributes) {
        LOGGER.debug("Ignoring all attributes given the service is designed to never receive any.");
        return Maps.newHashMap();
    }

    @Override
    protected Map<String, Object> returnFinalAttributesCollection(final Map<String, Object> attributesToRelease) {
        LOGGER.info("CAS will not authorize anything for release, given the service is denied access to all attributes. " 
                  + "If there are any default attributes set to be released to all services, those are also skipped for this service");
        return Maps.newHashMap();
    }
}
