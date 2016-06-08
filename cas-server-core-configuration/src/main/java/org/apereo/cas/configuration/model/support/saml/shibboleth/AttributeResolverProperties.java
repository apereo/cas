package org.apereo.cas.configuration.model.support.saml.shibboleth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * This is {@link AttributeResolverProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "shibboleth.attributeResolver", ignoreUnknownFields = false)
public class AttributeResolverProperties {
    
    private List<Resource> resources;

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(final List<Resource> resources) {
        this.resources = resources;
    }
}
