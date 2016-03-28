package org.jasig.cas.authentication.support;

import org.jasig.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link NoOpCasAttributeEncoder} that does no encoding of attributes received.
 * It will simply return the same exact collection of attributes received back to the caller.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("noOpCasAttributeEncoder")
public class NoOpCasAttributeEncoder implements CasAttributeEncoder {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final Service service) {
        logger.warn("Attributes are not encoded via {}. Total of {} attributes will be returned for service {}", 
                this.getClass().getSimpleName(), attributes.size(), service);
        return new HashMap<>(attributes);
    }
}
