package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * This is {@link BaseCasRestActuatorEndpoint}.
 * This class serves as the parent component for all actuator
 * endpoints that need to support REST-like operations via Spring MVC.
 * Such endpoints typically need direct access to the {@link jakarta.servlet.http.HttpServletRequest}.
 * While the base class here act as a typical {@link RestController}, it also provides
 * a {@link #ping(String)} operation that allows Spring Boot to detect the controller
 * and mark it as an actuator endpoint.
 * This class exists to account for the removal and deprecation of {@code RestControllerEndpoint} annotation.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RestController
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
     * Ping.
     *
     * @param ping the ping
     * @return the map
     */
    @ReadOperation
    public Map<String, String> ping(@Selector final String ping) {
        return Map.of("response", "pong");
    }

}
