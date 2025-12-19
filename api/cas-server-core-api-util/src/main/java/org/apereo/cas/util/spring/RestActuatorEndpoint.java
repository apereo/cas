package org.apereo.cas.util.spring;

import module java.base;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.FilteredEndpoint;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This is {@link RestActuatorEndpoint}.
 * <p>
 * This annotation serves as to mark a spring {@link org.springframework.web.servlet.mvc.Controller} as an actuator endpoint.
 * Such endpoints are then able to use Spring MVC operations such as {@link org.springframework.web.bind.annotation.GetMapping},
 * {@link org.springframework.web.bind.annotation.PostMapping}, {@link org.springframework.web.bind.annotation.DeleteMapping}, etc.
 * Also, these endpoints typically need direct access to the {@link jakarta.servlet.http.HttpServletRequest}.
 * This class exists to account for the ultimate removal and deprecation of {@code RestControllerEndpoint} annotation.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Endpoint
@FilteredEndpoint(RestActuatorEndpointFilter.class)
@ResponseBody
public @interface RestActuatorEndpoint {
}
