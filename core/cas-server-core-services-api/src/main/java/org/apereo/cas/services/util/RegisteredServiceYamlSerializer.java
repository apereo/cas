package org.apereo.cas.services.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * This is {@link RegisteredServiceYamlSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServiceYamlSerializer extends DefaultRegisteredServiceJsonSerializer {
    private static final long serialVersionUID = -6026921045861422473L;

    @Override
    protected JsonFactory getJsonFactory() {
        return new YAMLFactory();
    }
}
