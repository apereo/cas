package org.apereo.cas.metadata.rest;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@Endpoint(id = "configurationMetadata", defaultAccess = Access.NONE)
public class CasConfigurationMetadataServerEndpoint extends BaseCasRestActuatorEndpoint {
    private final CasConfigurationMetadataRepository repository;

    public CasConfigurationMetadataServerEndpoint(final CasConfigurationProperties casProperties,
                                                  final ConfigurableApplicationContext applicationContext,
                                                  final CasConfigurationMetadataRepository repository) {
        super(casProperties, applicationContext);
        this.repository = repository;
    }

    /**
     * Find all properties.
     *
     * @return the response entity
     */
    @GetMapping
    @Operation(summary = "Get all properties from the repository")
    public Map<String, ConfigurationMetadataProperty> properties() {
        return repository.getRepository().getAllProperties();
    }

    /**
     * Search for property.
     *
     * @param term the term
     * @return the response entity
     */
    @GetMapping("/{term}")
    @Operation(summary = "Get all properties from the repository that match the name or description", parameters =
        @Parameter(name = "term", required = true, description = "The search term to search for"))
    public List<ConfigurationMetadataSearchResult> search(
        @PathVariable
        final String term) {
        val allProps = repository.getRepository().getAllProperties();
        val names = StreamSupport.stream(RelaxedPropertyNames.forCamelCase(term).spliterator(), false)
            .map(Object::toString)
            .collect(Collectors.joining("|"));
        val pattern = RegexUtils.createPattern(names);
        return allProps.entrySet()
            .parallelStream()
            .filter(entry ->
                RegexUtils.find(pattern, entry.getKey()) || RegexUtils.find(pattern, StringUtils.defaultString(entry.getValue().getDescription())))
            .map(entry ->
                new ConfigurationMetadataSearchResult(entry.getValue()))
            .sorted()
            .toList();
    }
}
