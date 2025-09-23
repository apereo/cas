package org.apereo.cas.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.UrlValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

/**
 * This is {@link LogoutWebApplicationServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class LogoutWebApplicationServiceFactory extends WebApplicationServiceFactory {
    private final LogoutProperties logoutProperties;

    public LogoutWebApplicationServiceFactory(final TenantExtractor tenantExtractor,
                                              final UrlValidator urlValidator,
                                              final LogoutProperties logoutProperties) {
        super(tenantExtractor, urlValidator);
        this.logoutProperties = logoutProperties;
    }

    @Override
    protected String getRequestedService(final HttpServletRequest request) {
        if (request.getRequestURI().endsWith(CasProtocolConstants.ENDPOINT_LOGOUT)) {
            val service = logoutProperties.getRedirectParameter()
                .stream()
                .map(paramName -> {
                    LOGGER.trace("Using request parameter name [{}] to detect destination service, if any", paramName);
                    return request.getParameter(paramName);
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(StringUtils.EMPTY);
            LOGGER.trace("Located target service [{}] for redirection after logout", service);
            return service;
        }
        return null;
    }
}
