package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
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
     * Update property and return list of sources that operated.
     *
     * @param values the values
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
    public List<String> updateProperty(
        @RequestBody final List<ConfigurationPropertyUpdateRequest> values) {
        val activeSources = CasCoreConfigurationUtils.getMutablePropertySources(applicationContext);
        return activeSources
            .stream()
            .map(source -> values
                .stream()
                .filter(v -> StringUtils.isBlank(v.propertySource()) || v.propertySource().equalsIgnoreCase(source.getName()))
                .map(v -> source.setProperty(v.name(), v.value()))
                .map(_ -> source.getName())
                .toList())
            .flatMap(Collection::stream)
            .toList();
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
            @Parameter(name = "name", required = true, description = "The name of the property to remove"),
            @Parameter(name = "propertySource", required = false, description = "The name of the property source that should be updated")
        })
    public List<String> removeProperty(@RequestBody final ConfigurationPropertyDeleteRequest request) {
        val activeSources = CasCoreConfigurationUtils.getMutablePropertySources(applicationContext);
        return activeSources
            .stream()
            .map(source -> Stream.of(request)
                .filter(v -> StringUtils.isBlank(v.propertySource()) || v.propertySource().equalsIgnoreCase(source.getName()))
                .map(v -> {
                    source.removeProperty(v.name());
                    return source.getName();
                })
                .toList())
            .flatMap(Collection::stream)
            .toList();
    }

    public record ConfigurationPropertyUpdateRequest(
        @NonNull String name,
        @NonNull String value,
        @Nullable String propertySource) {
    }

    public record ConfigurationPropertyDeleteRequest(
        @NonNull String name,
        @Nullable String propertySource) {
    }
}
