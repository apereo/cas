package org.apereo.cas.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apereo.cas.util.services.RegisteredServiceJsonSerializer;

/**
 * This is {@link RegisteredServiceYamlSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServiceYamlSerializer extends RegisteredServiceJsonSerializer {
    private static final long serialVersionUID = -6026921045861422473L;

    @Override
    protected JsonFactory getJsonFactory() {
        return new YAMLFactory();
    }
}
