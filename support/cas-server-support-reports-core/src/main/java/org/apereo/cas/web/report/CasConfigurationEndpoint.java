package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.configuration.api.MutableConfigurationProperty;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This is {@link CasConfigurationEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Endpoint(id = "casConfig", defaultAccess = Access.NONE)
public class CasConfigurationEndpoint extends BaseCasRestActuatorEndpoint {
    private final CipherExecutor<String, String> casConfigurationCipherExecutor;

    public CasConfigurationEndpoint(final CasConfigurationProperties casProperties,
                                    final ConfigurableApplicationContext applicationContext,
                                    final CipherExecutor<String, String> casConfigurationCipherExecutor) {
        super(casProperties, applicationContext);
        this.casConfigurationCipherExecutor = casConfigurationCipherExecutor;
    }

    /**
     * Encrypt value.
     *
     * @param value the value
     * @return the response entity
     */
    @PostMapping(value = "/encrypt",
        produces = MediaType.TEXT_PLAIN_VALUE,
        consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Encrypt configuration value",
        parameters = @Parameter(name = "value", required = true, description = "The value to encrypt"))
    public ResponseEntity<@NonNull String> encrypt(@RequestBody final String value) {
        return ResponseEntity.ok(casConfigurationCipherExecutor.encode(value));
    }

    /**
     * Decrypt value.
     *
     * @param value the value
     * @return the response entity
     */
    @PostMapping(value = "/decrypt",
        produces = MediaType.TEXT_PLAIN_VALUE,
        consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Decrypt configuration value",
        parameters = @Parameter(name = "value", required = true, description = "The value to decrypt"))
    public ResponseEntity<@NonNull String> decrypt(@RequestBody final String value) {
        return ResponseEntity.ok(casConfigurationCipherExecutor.decode(value));
    }


    /**
     * Retrieve properties matching a name.
     *
     * @param request the request
     * @return the list
     */
    @PostMapping(value = "/retrieve",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE
        })
    @Operation(summary = "Fetch configuration property",
        parameters = {
            @Parameter(name = "value", required = true, description = "The value to match"),
            @Parameter(name = "name", required = true, description = "The name (pattern) of the property to fetch"),
            @Parameter(name = "propertySource", required = false, description = "The name of the property source that should be examined")
        })
    public List<MutableConfigurationProperty> retrieveProperty(
        @RequestBody final ConfigurationPropertyRetrievalRequest request) {
        val activeSources = CasCoreConfigurationUtils.getMutablePropertySources(applicationContext);
        return activeSources
            .stream()
            .filter(source -> StringUtils.isBlank(request.propertySource()) || request.propertySource().equalsIgnoreCase(source.getName()))
            .map(source -> source.getPropertyNames(request.name()))
            .flatMap(List::stream)
            .toList()
            .stream()
            .filter(entry -> ((MutableConfigurationProperty) entry).value() != null)
            .filter(entry -> {
                val property = (MutableConfigurationProperty) entry;
                return StringUtils.isBlank(request.value())
                    || (property.value() != null && property.value().toString().equalsIgnoreCase(request.value()));
            })
            .toList();
    }

    /**
     * Update property and return list of sources that operated.
     *
     * @param updateRequests the update requests
     * @return the list of property sources that operated
     */
    @PostMapping(value = "/update",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update configuration value",
        parameters = {
            @Parameter(name = "value", required = true, description = "The value to set"),
            @Parameter(name = "name", required = true, description = "The name of the property to update"),
            @Parameter(name = "propertySource", required = false, description = "The name of the property source that should be updated")
        })
    public List<String> updateProperty(@RequestBody final List<ConfigurationPropertyUpdateRequest> updateRequests) {
        val activeSources = CasCoreConfigurationUtils.getMutablePropertySources(applicationContext);

        val propertyGroups = new HashMap<String, Integer>();
        for (val request : updateRequests) {
            if (request.name().contains("[]")) {
                val group = request.name().substring(0, request.name().indexOf('['));
                propertyGroups.computeIfAbsent(group, _ -> {
                    val usedIndexes = getUsedIndexesForPropertyGroup(request, activeSources);
                    return usedIndexes.isEmpty() ? 0 : usedIndexes.getLast() + 1;
                });
            }
        }

        return activeSources
            .stream()
            .map(source -> updateRequests
                .stream()
                .filter(request -> StringUtils.isBlank(request.propertySource()) || request.propertySource().equalsIgnoreCase(source.getName()))
                .map(request -> {
                    if (request.name().contains("[]")) {
                        val group = request.name().substring(0, request.name().indexOf('['));
                        val activeIndex = propertyGroups.get(group);
                        val groupedProperty = request.name().replace("[]", "[%s]".formatted(activeIndex));
                        source.setProperty(groupedProperty, request.value());
                    } else {
                        source.setProperty(request.name(), request.value());
                    }
                    return source.getName();
                })
                .toList())
            .flatMap(Collection::stream)
            .toList();
    }

    private static List<Integer> getUsedIndexesForPropertyGroup(
        final ConfigurationPropertyUpdateRequest updateRequest,
        final List<MutablePropertySource> sources) {
        val group = updateRequest.name().substring(0, updateRequest.name().indexOf('['));
        val indexedPattern = RegexUtils.createPattern(group + "\\[(\\d+)]");

        val usedIndexes = sources
            .stream()
            .filter(ps -> ps instanceof EnumerablePropertySource<?>)
            .map(ps -> (EnumerablePropertySource<?>) ps)
            .flatMap(eps -> Arrays.stream(eps.getPropertyNames()))
            .map(indexedPattern::matcher).filter(Matcher::find)
            .map(matcher -> Integer.parseInt(matcher.group(1)))
            .collect(Collectors.toCollection(TreeSet::new));
        return usedIndexes.stream().toList();
    }

    /**
     * Remove property.
     *
     * @param request the request
     * @return the list of property sources that operated
     */
    @DeleteMapping
    @Operation(summary = "Remove configuration property",
        parameters = {
            @Parameter(name = "name", required = false, description = "The name of the property to remove. Leave blank to remove all properties"),
            @Parameter(name = "propertySource", required = false, description = "The name of the property source that should be updated")
        })
    public List<String> removeProperty(@RequestBody final ConfigurationPropertyDeleteRequest request) {
        val activeSources = CasCoreConfigurationUtils.getMutablePropertySources(applicationContext);
        if (StringUtils.isBlank(request.name())) {
            return activeSources
                .stream()
                .map(source -> Stream.of(request)
                    .filter(v -> StringUtils.isBlank(v.propertySource()) || v.propertySource().equalsIgnoreCase(source.getName()))
                    .map(v -> {
                        source.removeAll();
                        return source.getName();
                    })
                    .toList())
                .flatMap(Collection::stream)
                .toList();
        }
        return activeSources
            .stream()
            .map(source -> Stream.of(request)
                .filter(v -> StringUtils.isNotBlank(v.name()))
                .filter(v -> StringUtils.isBlank(v.propertySource()) || v.propertySource().equalsIgnoreCase(source.getName()))
                .map(v -> {
                    source.removeProperty(v.name());
                    return source.getName();
                })
                .toList())
            .flatMap(Collection::stream)
            .toList();
    }

    public record ConfigurationPropertyRetrievalRequest(
        @NonNull String name,
        @NonNull String value,
        @Nullable String propertySource) {
    }

    public record ConfigurationPropertyUpdateRequest(
        @NonNull String name,
        @NonNull String value,
        @Nullable String propertySource) {
    }

    public record ConfigurationPropertyDeleteRequest(
        String name,
        @Nullable String propertySource) {
    }
}
