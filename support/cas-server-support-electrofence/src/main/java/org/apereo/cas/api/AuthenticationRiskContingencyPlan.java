package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link AuthenticationRiskContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface AuthenticationRiskContingencyPlan {

    /**
     * Execute authentication risk contingency plan and produce a response.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param score          the score
     * @param request        the request
     * @return the authentication risk contingency response
     * @throws Throwable the throwable
     */
    AuthenticationRiskContingencyResponse execute(Authentication authentication,
                                                  RegisteredService service,
                                                  AuthenticationRiskScore score,
                                                  HttpServletRequest request) throws Throwable;
}
