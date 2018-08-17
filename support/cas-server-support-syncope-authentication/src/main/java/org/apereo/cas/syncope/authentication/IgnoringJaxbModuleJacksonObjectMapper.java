package org.apereo.cas.syncope.authentication;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/**
 * This is {@link IgnoringJaxbModuleJacksonObjectMapper}. This is a special implementation
 * of jackson's {@link ObjectMapper} that ignores the jaxrs-json module
 * and prevents it from registration. This is because some of the syncope
 * objects with jaxrs annotations have duplicate property field names and tags
 * and allowing the jaxrs provider to take part in the serialization of those objects
 * causes issues.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
class IgnoringJaxbModuleJacksonObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = 5560775756926741124L;

    @Override
    public ObjectMapper registerModule(final Module module) {
        if (module instanceof JaxbAnnotationModule) {
            return this;
        }
        return super.registerModule(module);
    }
}
