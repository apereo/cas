package org.apereo.cas.metadata.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.RegexUtils;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link CasConfigurationMetadataServerEndpoint}.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Endpoint(id = "configuration-metadata", enableByDefault = false)
public class CasConfigurationMetadataServerEndpoint {
    private final CasConfigurationMetadataRepository repository;

    public CasConfigurationMetadataServerEndpoint(final CasConfigurationMetadataRepository repository) {
        this.repository = repository;
    }

    /**
     * Find cas configuration property by name.
     *
     * @param name the property name
     * @return the response entity
     */
    @ReadOperation
    public ConfigurationMetadataProperty property(@Selector final String name) {
        return repository.getRepository().getAllProperties().get(name);
    }

    /**
     * Find cas configuration group by group name.
     *
     * @param name the property name
     * @return the response entity
     */
    @ReadOperation
    public ConfigurationMetadataGroup group(@Selector final String name) {
        return repository.getRepository().getAllGroups().get(name);
    }

    /**
     * Find all groups.
     *
     * @return the response entity
     */
    @ReadOperation
    public Map<String, ConfigurationMetadataGroup> groups() {
        return repository.getRepository().getAllGroups();
    }

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
        List results = new ArrayList<>();
        final Map<String, ConfigurationMetadataProperty> allProps = repository.getRepository().getAllProperties();

        if (StringUtils.isNotBlank(name) && RegexUtils.isValidRegex(name)) {
            final String names = StreamSupport.stream(RelaxedPropertyNames.forCamelCase(name).spliterator(), false)
                .map(Object::toString)
                .collect(Collectors.joining("|"));
            final Pattern pattern = RegexUtils.createPattern(names);
            results = allProps.entrySet()
                .stream()
                .filter(propEntry -> RegexUtils.find(pattern, propEntry.getKey()))
                .map(propEntry -> new ConfigurationMetadataSearchResult(propEntry.getValue(), repository))
                .collect(Collectors.toList());
            Collections.sort(results);
        }
        return results;
    }
}
