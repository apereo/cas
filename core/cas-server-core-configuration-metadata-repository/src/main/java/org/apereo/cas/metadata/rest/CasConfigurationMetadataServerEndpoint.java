package org.apereo.cas.metadata.rest;

import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.RegexUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link CasConfigurationMetadataServerEndpoint}.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Endpoint(id = "configuration-metadata", enableByDefault = false)
@RequiredArgsConstructor
public class CasConfigurationMetadataServerEndpoint {
    private final CasConfigurationMetadataRepository repository;

    /**
     * Find all properties.
     *
     * @return the response entity
     */
    @ReadOperation
    public Map<String, ConfigurationMetadataProperty> properties() {
        return repository.getRepository().getAllProperties();
    }

    /**
     * Search for property.
     *
     * @param name the name
     * @return the response entity
     */
    @ReadOperation
    public List<ConfigurationMetadataSearchResult> search(@Selector final String name) {
        val allProps = repository.getRepository().getAllProperties();

        if (StringUtils.isNotBlank(name) && RegexUtils.isValidRegex(name)) {
            val names = StreamSupport.stream(RelaxedPropertyNames.forCamelCase(name).spliterator(), false)
                .map(Object::toString)
                .collect(Collectors.joining("|"));
            val pattern = RegexUtils.createPattern(names);
            return allProps.entrySet()
                .stream()
                .filter(propEntry -> RegexUtils.find(pattern, propEntry.getKey()))
                .map(propEntry -> new ConfigurationMetadataSearchResult(propEntry.getValue(), repository))
                .sorted()
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
