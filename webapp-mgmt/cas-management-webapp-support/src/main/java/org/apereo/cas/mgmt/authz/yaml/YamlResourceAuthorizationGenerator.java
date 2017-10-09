package org.apereo.cas.mgmt.authz.yaml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apereo.cas.mgmt.authz.json.JsonResourceAuthorizationGenerator;
import org.springframework.core.io.Resource;

/**
 * This is {@link YamlResourceAuthorizationGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class YamlResourceAuthorizationGenerator extends JsonResourceAuthorizationGenerator {
    public YamlResourceAuthorizationGenerator(final Resource resource) {
        super(resource);
    }

    @Override
    protected JsonFactory getJsonFactory() {
        return new YAMLFactory();
    }
}
