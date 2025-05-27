package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.core.Ordered;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RegisteredServiceAccessStrategy}
 * that can decide if a service is recognized and authorized to participate
 * in the CAS protocol flow during authentication/validation events.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAccessStrategy extends Serializable, Ordered {

    /**
     * Verify is the service is enabled and recognized by CAS.
     *
     * @param registeredService the registered service
     * @param service           the service
     * @return true /false if service is enabled
     */
    default boolean isServiceAccessAllowed(final RegisteredService registeredService,
                                           final Service service) {
        return true;
    }

    /**
     * Assert that the service can participate in sso.
     *
     * @param registeredService the registered service
     * @return true /false if service can participate in sso
     */
    default boolean isServiceAccessAllowedForSso(final RegisteredService registeredService) {
        return true;
    }

    /**
     * Verify authorization policy by checking the pre-configured rules
     * that may depend on what the principal might be carrying.
     * <p>
     * Verify presence of service required attributes.
     * <ul>
     * <li>If no rejected attributes are specified, authz is granted.</li>
     * <li>If no required attributes are specified, authz is granted.</li>
     * <li>If ALL attributes must be present, and the principal contains all and there is
     * at least one attribute value that matches the rejected, authz is denied.</li>
     * <li>If ALL attributes must be present, and the principal contains all and there is
     * at least one attribute value that matches the required, authz is granted.</li>
     * <li>If ALL attributes don't have to be present, and there is at least
     * one principal attribute present whose value matches the rejected, authz is denied.</li>
     * <li>If ALL attributes don't have to be present, and there is at least
     * one principal attribute present whose value matches the required, authz is granted.</li>
     * <li>Otherwise, access is denied</li>
     * </ul>
     *
     * @param request the request
     * @return true /false if service access can be granted to principal
     * @throws Throwable the throwable
     */
    default boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        return true;
    }

    /**
     * Redirect the request to a separate and possibly external URL
     * in case authorization fails for this service. If no URL is
     * specified, CAS shall redirect the request by default to a generic
     * page that describes the authorization failed attempt.
     *
     * @return the redirect url
     * @since 4.2
     */
    default URI getUnauthorizedRedirectUrl() {
        return null;
    }

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * Return the delegated authentication policy for this service.
     *
     * @return authn policy
     */
    default RegisteredServiceDelegatedAuthenticationPolicy getDelegatedAuthenticationPolicy() {
        return null;
    }

    /**
     * Expose underlying attributes for auditing purposes.
     *
     * @return required attributes
     */
    default Map<String, Set<String>> getRequiredAttributes() {
        return new HashMap<>();
    }

}
