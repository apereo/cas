package org.apereo.cas.configuration.metadata;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.boot.configurationmetadata.ValueProvider;

/**
 * This is {@link ConfigurationMetadataHint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConfigurationMetadataHint {
    private final List<ValueHint> values = new ArrayList<>();

    private final List<ValueProvider> providers = new ArrayList<>();

    private String name;
}
