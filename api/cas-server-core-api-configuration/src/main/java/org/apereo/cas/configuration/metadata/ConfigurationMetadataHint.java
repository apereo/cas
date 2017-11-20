package org.apereo.cas.configuration.metadata;

import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.boot.configurationmetadata.ValueProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link ConfigurationMetadataHint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConfigurationMetadataHint {
    private String name;
    private final List<ValueHint> values = new ArrayList();
    private final List<ValueProvider> providers = new ArrayList();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<ValueHint> getValues() {
        return values;
    }

    public List<ValueProvider> getProviders() {
        return providers;
    }
}
