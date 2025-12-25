package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
     * @param value the value
     * @return the list
     */
    @PostMapping(value = "/update",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update configuration value",
        parameters = {
            @Parameter(name = "value", required = true, description = "The value to set"),
            @Parameter(name = "name", required = true, description = "The name of the property to update")
        })
    public List<String> updateProperty(
        @RequestBody final ConfigurationPropertyUpdateRequest value) {
        val activeSources = applicationContext.getEnvironment().getPropertySources();
        return activeSources
            .stream()
            .map(source -> {
                if (source instanceof final MutablePropertySource mutable) {
                    return mutable;
                }
                if (source instanceof final BootstrapPropertySource bootstrap
                    && bootstrap.getDelegate() instanceof final MutablePropertySource mutable) {
                    return mutable;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .map(source -> source.setProperty(value.name(), value.value()).getName())
            .toList();
    }

    public record ConfigurationPropertyUpdateRequest(
        @NonNull String name,
        @NonNull String value) {
    }
}
