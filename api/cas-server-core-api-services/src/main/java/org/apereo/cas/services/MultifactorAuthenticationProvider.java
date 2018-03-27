package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.Event;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProvider}
 * that describes an external authentication entity/provider
 * matched against a registered service. Providers may be given
 * the ability to check authentication provider for availability
 * before actually producing a relevant identifier.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationProvider extends Serializable, Ordered {

    /**
     * Ensure the provider is available.
     *
     * @param service the service
     * @return true /false flag once verification is successful.
     * @throws AuthenticationException the authentication exception
     */
    boolean isAvailable(RegisteredService service) throws AuthenticationException;

    /**
     * Gets id for this provider.
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the friendly-name for this provider.
     *
     * @return the name
     */
    String getFriendlyName();

    /**
     * Does provider match/support this identifier?
     * The identifier passed may be formed as a regular expression.
     *
     * @param identifier the identifier
     * @return the boolean
     */
    boolean matches(String identifier);

    /**
     * Indicates whether the current active event is supported by
     * this mfa provider based on the given authentication and service definition.
     * This allows each mfa provider to design bypass rules based on traits
     * of the service or authentication, or both.
     *
     * @param event             the event
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param request           the request
     * @return true if supported
     */
    boolean supports(Event event, Authentication authentication,
                     RegisteredService registeredService,
                     HttpServletRequest request);

}
