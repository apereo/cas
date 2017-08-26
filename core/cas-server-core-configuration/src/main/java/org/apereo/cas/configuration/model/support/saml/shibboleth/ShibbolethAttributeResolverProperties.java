package org.apereo.cas.configuration.model.support.saml.shibboleth;

import org.apereo.cas.util.CollectionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link ShibbolethAttributeResolverProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ShibbolethAttributeResolverProperties implements Serializable {

    private static final long serialVersionUID = 6315982182145371022L;
    /**
     * List of Shibboleth's attribute resolver XMLM resources to parse and load.
     * Each resource can either be found on the file system, as a classpath entry
     * or via a URL if needed.
     */
    private List<Resource> resources = CollectionUtils.wrap(new ClassPathResource("attribute-resolver.xml"));

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(final List<Resource> resources) {
        this.resources = resources;
    }
}
