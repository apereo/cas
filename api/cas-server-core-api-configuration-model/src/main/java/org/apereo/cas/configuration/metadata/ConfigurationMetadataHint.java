package org.apereo.cas.configuration.metadata;

import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class ConfigurationMetadataHint {
    private final List<ValueHint> values = new ArrayList<>(0);
    private final List<ValueProvider> providers = new ArrayList<>(0);
    private String name;
}
