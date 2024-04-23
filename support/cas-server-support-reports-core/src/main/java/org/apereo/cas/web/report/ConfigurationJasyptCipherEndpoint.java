package org.apereo.cas.web.report;

import org.apereo.cas.util.crypto.CipherExecutor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This is {@link ConfigurationJasyptCipherEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@RestControllerEndpoint(id = "casConfig", enableByDefault = false)
public class ConfigurationJasyptCipherEndpoint {
    private final CipherExecutor<String, String> casConfigurationCipherExecutor;

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
               parameters = @Parameter(name = "value", required = true))
    public ResponseEntity<String> encrypt(@RequestBody final String value) {
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
               parameters = @Parameter(name = "value", required = true))
    public ResponseEntity<String> decrypt(@RequestBody final String value) {
        return ResponseEntity.ok(casConfigurationCipherExecutor.decode(value));
    }
}
