package org.apereo.cas.configuration.metadata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.boot.configurationmetadata.ValueProvider;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ConfigurationMetadataHint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class ConfigurationMetadataHint {

    private String name;

    private final List<ValueHint> values = new ArrayList();

    private final List<ValueProvider> providers = new ArrayList();
}
