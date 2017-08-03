package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link NoOpProtocolAttributeEncoder} that does no encoding of attributes received.
 * It will simply return the same exact collection of attributes received back to the caller.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class NoOpProtocolAttributeEncoder implements ProtocolAttributeEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpProtocolAttributeEncoder.class);
    
    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final RegisteredService service) {
        LOGGER.warn("Attributes are not encoded via [{}]. Total of [{}] attributes will be returned for service [{}]", 
                this.getClass().getSimpleName(), attributes.size(), service);
        return new HashMap<>(attributes);
    }
}
