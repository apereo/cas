package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This component is used to handle release of authentication attributes in validation responses.
 *
 * @author Daniel Frett
 * @since 5.2.0
 */
public interface AuthenticationAttributeReleasePolicy {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "authenticationAttributeReleasePolicy";

    /**
     * NoOp authentication attribute release policy.
     *
     * @return the authentication attribute release policy
     */
    static AuthenticationAttributeReleasePolicy none() {
        return new AuthenticationAttributeReleasePolicy() {
            @Override
            public Map<String, List<Object>> getAuthenticationAttributesForRelease(final Authentication authentication,
                                                                                   final Assertion assertion, final Map<String, Object> model,
                                                                                   final RegisteredService service) {
                return new HashMap<>();
            }

            @Override
            public Map<String, List<Object>> getAuthenticationAttributesForRelease(final Authentication authentication,
                                                                                   final RegisteredService service) {
                return new HashMap<>();
            }
        };
    }

    /**
     * This method will return the Authentication attributes that should be released.
     *
     * @param authentication The authentication object we are processing.
     * @param assertion      the assertion
     * @param model          the model
     * @param service        the service
     * @return The attributes to be released
     */
    Map<String, List<Object>> getAuthenticationAttributesForRelease(Authentication authentication,
                                                                    Assertion assertion,
                                                                    Map<String, Object> model,
                                                                    RegisteredService service);

    /**
     * Gets authentication attributes for release.
     *
     * @param authentication the authentication
     * @param service        the service
     * @return the authentication attributes for release
     */
    Map<String, List<Object>> getAuthenticationAttributesForRelease(Authentication authentication, RegisteredService service);
}
