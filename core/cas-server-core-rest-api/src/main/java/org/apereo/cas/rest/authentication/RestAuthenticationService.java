package org.apereo.cas.rest.authentication;

import org.apereo.cas.authentication.AuthenticationResult;

import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link RestAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface RestAuthenticationService {

    /**
     * Default bean name.
     */
    String DEFAULT_BEAN_NAME = "restAuthenticationService";

    /**
     * Authenticate.
     *
     * @param requestBody the request body
     * @param request     the request
     * @return the optional
     */
    Optional<AuthenticationResult> authenticate(MultiValueMap<String, String> requestBody,
                                                HttpServletRequest request);
}
