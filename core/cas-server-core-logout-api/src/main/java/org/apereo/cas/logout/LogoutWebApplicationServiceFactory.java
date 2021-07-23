package org.apereo.cas.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link LogoutWebApplicationServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class LogoutWebApplicationServiceFactory extends WebApplicationServiceFactory {
    private final LogoutProperties logoutProperties;

    @Override
    protected String getRequestedService(final HttpServletRequest request) {
        if (request.getRequestURI().endsWith(CasProtocolConstants.ENDPOINT_LOGOUT)) {
            val paramName = logoutProperties.getRedirectParameter();
            LOGGER.trace("Using request parameter name [{}] to detect destination service, if any", paramName);
            val service = request.getParameter(paramName);
            LOGGER.trace("Located target service [{}] for redirection after logout", service);
            return service;
        }
        return null;
    }
}
