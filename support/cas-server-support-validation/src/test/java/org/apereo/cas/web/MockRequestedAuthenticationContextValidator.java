package org.apereo.cas.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationContextValidationResult;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link MockRequestedAuthenticationContextValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class MockRequestedAuthenticationContextValidator implements RequestedAuthenticationContextValidator {
    @Override
    public AuthenticationContextValidationResult validateAuthenticationContext(final Assertion assertion, final HttpServletRequest request) {
        return AuthenticationContextValidationResult.builder().success(true).build();
    }

    @Override
    public AuthenticationContextValidationResult validateAuthenticationContext(final HttpServletRequest request,
                                                                               final RegisteredService registeredService,
                                                                               final Authentication authentication,
                                                                               final Service service) {
        return AuthenticationContextValidationResult.builder().success(true).build();
    }
}
