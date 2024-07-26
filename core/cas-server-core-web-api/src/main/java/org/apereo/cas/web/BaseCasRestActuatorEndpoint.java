package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.RestActuatorEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This is {@link BaseCasRestActuatorEndpoint}.
 * This class serves as the parent component for all actuator
 * endpoints that need to support REST-like operations via Spring MVC.
 * Such endpoints typically need direct access to the {@link jakarta.servlet.http.HttpServletRequest}.
 * This class exists to account for the removal and deprecation of {@code RestControllerEndpoint} annotation.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RestActuatorEndpoint
public abstract class BaseCasRestActuatorEndpoint extends BaseCasActuatorEndpoint {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    protected final ConfigurableApplicationContext applicationContext;

    protected BaseCasRestActuatorEndpoint(final CasConfigurationProperties casProperties,
                                          final ConfigurableApplicationContext applicationContext) {
        super(casProperties);
        this.applicationContext = applicationContext;
    }

    /**
     * Head response entity.
     *
     * @return the response entity
     */
    @Operation(summary = "Reports back to the presence of the endpoint without any content")
    @RequestMapping(method = RequestMethod.HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity head() {
        return ResponseEntity.ok().build();
    }
}
