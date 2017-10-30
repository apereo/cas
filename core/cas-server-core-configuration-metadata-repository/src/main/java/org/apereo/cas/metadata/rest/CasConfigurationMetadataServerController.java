package org.apereo.cas.metadata.rest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedNames;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link CasConfigurationMetadataServerController}.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasConfigurationMetadataServerController extends BaseCasMvcEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationMetadataServerController.class);

    private final CasConfigurationMetadataRepository repository;

    public CasConfigurationMetadataServerController(final CasConfigurationMetadataRepository repository,
                                                    final CasConfigurationProperties casProperties) {
        super("configmetadata", "/configmetadata", casProperties.getMonitor().getEndpoints().getConfigurationMetadata(), casProperties);
        this.repository = repository;
    }

    /**
     * Find cas configuration property by name.
     *
     * @param propertyName the property name
     * @return the response entity
     */
    @GetMapping(path = "/property")
    public ResponseEntity<ConfigurationMetadataProperty> findByPropertyName(@RequestParam("name") final String propertyName) {
        final ConfigurationMetadataProperty configMetadataProp = repository.getRepository().getAllProperties().get(propertyName);
        return ResponseEntity.ok(configMetadataProp);
    }

    /**
     * Find cas configuration group by group name.
     *
     * @param name the property name
     * @return the response entity
     */
    @GetMapping(path = "/group")
    public ResponseEntity<ConfigurationMetadataGroup> findByGroupName(@RequestParam("name") final String name) {
        final ConfigurationMetadataGroup grp = repository.getRepository().getAllGroups().get(name);
        return ResponseEntity.ok(grp);
    }

    /**
     * Find all groups.
     *
     * @return the response entity
     */
    @GetMapping(path = "/groups")
    public ResponseEntity<Map<String, ConfigurationMetadataGroup>> findAllGroups() {
        return ResponseEntity.ok(repository.getRepository().getAllGroups());
    }

    /**
     * Find all properties.
     *
     * @return the response entity
     */
    @GetMapping(path = "/properties")
    public ResponseEntity<Map<String, ConfigurationMetadataProperty>> findAllProperties() {
        final Map<String, ConfigurationMetadataProperty> allProps = repository.getRepository().getAllProperties();
        return ResponseEntity.ok(allProps);
    }

    /**
     * Search for property.
     *
     * @param name the name
     * @return the response entity
     */
    @GetMapping(path = "/search")
    public ResponseEntity<List<ConfigurationMetadataSearchResult>> search(@RequestParam(value = "name", required = false) final String name) {
        List results = new ArrayList<>();
        final Map<String, ConfigurationMetadataProperty> allProps = repository.getRepository().getAllProperties();

        if (StringUtils.isNotBlank(name) && RegexUtils.isValidRegex(name)) {
            final String names = StreamSupport.stream(RelaxedNames.forCamelCase(name).spliterator(), false)
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
        return ResponseEntity.ok(results);
    }

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        return new ModelAndView("monitoring/viewConfigMetadata");
    }

}
