package org.apereo.cas.services.web.support;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.filters.ResponseHeadersEnforcementFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.http.HttpStatus;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link RegisteredServiceResponseHeadersEnforcementFilter}. A filter extension that looks at the properties of a
 * registered service to determine if headers should be injected into the response.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class RegisteredServiceResponseHeadersEnforcementFilter extends ResponseHeadersEnforcementFilter {
    private final ObjectProvider<ServicesManager> servicesManagerProvider;

    private final ObjectProvider<ArgumentExtractor> argumentExtractor;

    private final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    private final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    private final WebEndpointProperties webEndpointProperties;

    private static String getStringProperty(final Optional<RegisteredService> result,
                                            final RegisteredServiceProperties property) {
        if (result.isPresent()) {
            val registeredService = result.get();
            LOGGER.trace("Resolved registered service [{}] from request to enforce response headers", registeredService);
            val properties = registeredService.getProperties();
            if (!properties.containsKey(property.getPropertyName())) {
                LOGGER.trace("Resolved registered service [{}] from request does not contain a property definition for [{}]",
                    registeredService.getName(), property.getPropertyName());
                return null;
            }
            val prop = properties.get(property.getPropertyName());
            return prop.value();
        }
        LOGGER.trace("Resolved registered service from request can not be located");
        return null;
    }

    private static Optional<Boolean> shouldHttpHeaderBeInjectedIntoResponse(final Optional<RegisteredService> registeredService,
                                                                            final RegisteredServiceProperties property) {
        val propValue = getStringProperty(registeredService, property);
        if (propValue != null) {
            return Optional.of(BooleanUtils.toBoolean(propValue));
        }
        return Optional.empty();
    }

    @Override
    protected Optional<RegisteredService> prepareFilterBeforeExecution(final HttpServletResponse httpServletResponse,
                                                                       final HttpServletRequest httpServletRequest) throws Throwable {
        val basePath = webEndpointProperties.getBasePath();
        if (httpServletRequest.getRequestURI().contains(basePath)) {
            return Optional.empty();
        }
        
        val service = argumentExtractor.getObject().extractService(httpServletRequest);
        if (service != null) {
            LOGGER.trace("Attempting to resolve service for [{}]", service);
            val resolved = authenticationRequestServiceSelectionStrategies.getObject().resolveService(service);
            val servicesManager = servicesManagerProvider.getObject();
            val registeredService = NumberUtils.isCreatable(resolved.getId())
                ? servicesManager.findServiceBy(Long.parseLong(resolved.getId()))
                : servicesManager.findServiceBy(resolved);
            val audit = AuditableContext
                .builder()
                .registeredService(registeredService)
                .service(service)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.getObject().execute(audit);
            if (accessResult.isExecutionFailure()) {
                httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
                httpServletRequest.setAttribute(RequestDispatcher.ERROR_EXCEPTION, accessResult.getException().orElse(null));
                return Optional.empty();
            }
            return Optional.of(registeredService);
        }
        return Optional.empty();
    }
    @Override
    protected void decideInsertContentSecurityPolicyHeader(final HttpServletResponse httpServletResponse,
                                                           final HttpServletRequest httpServletRequest,
                                                           final Optional<RegisteredService> result) {

        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(result,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY);

        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                insertContentSecurityPolicyHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("ContentSecurityPolicy header disabled by service definition");
            }
        } else {
            super.decideInsertContentSecurityPolicyHeader(httpServletResponse, httpServletRequest, result);
        }
    }

    @Override
    protected void decideInsertXSSProtectionHeader(final HttpServletResponse httpServletResponse,
                                                   final HttpServletRequest httpServletRequest,
                                                   final Optional<RegisteredService> result) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(result,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION);
        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                insertXSSProtectionHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("XSSProtection header disabled by service definition");
            }
        } else {
            super.decideInsertXSSProtectionHeader(httpServletResponse, httpServletRequest, result);
        }
    }

    @Override
    protected void decideInsertXFrameOptionsHeader(final HttpServletResponse httpServletResponse,
                                                   final HttpServletRequest httpServletRequest,
                                                   final Optional<RegisteredService> result) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(result,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS);

        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                val xFrameOptions = getStringProperty(result, RegisteredServiceProperties.HTTP_HEADER_XFRAME_OPTIONS);
                insertXFrameOptionsHeader(httpServletResponse, httpServletRequest, xFrameOptions);
            } else {
                LOGGER.trace("XFrameOptions header disabled by service definition");
            }
        } else {
            super.decideInsertXFrameOptionsHeader(httpServletResponse, httpServletRequest, result);
        }
    }

    @Override
    protected void decideInsertXContentTypeOptionsHeader(final HttpServletResponse httpServletResponse,
                                                         final HttpServletRequest httpServletRequest,
                                                         final Optional<RegisteredService> result) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(result,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS);
        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                insertXContentTypeOptionsHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("XContentOptions header disabled by service definition");
            }
        } else {
            super.decideInsertXContentTypeOptionsHeader(httpServletResponse, httpServletRequest, result);
        }
    }

    @Override
    protected void decideInsertCacheControlHeader(final HttpServletResponse httpServletResponse,
                                                  final HttpServletRequest httpServletRequest,
                                                  final Optional<RegisteredService> result) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(result,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                insertCacheControlHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("EnableCacheControl header disabled by service definition");
            }
        } else {
            super.decideInsertCacheControlHeader(httpServletResponse, httpServletRequest, result);
        }
    }

    @Override
    protected void decideInsertStrictTransportSecurityHeader(final HttpServletResponse httpServletResponse,
                                                             final HttpServletRequest httpServletRequest,
                                                             final Optional<RegisteredService> result) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(result,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY);
        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                val headerValue = StringUtils.defaultIfBlank(getStringProperty(result,
                    RegisteredServiceProperties.HTTP_HEADER_STRICT_TRANSPORT_SECURITY), getStrictTransportSecurityHeader());
                insertStrictTransportSecurityHeader(httpServletResponse, httpServletRequest, headerValue);
            } else {
                LOGGER.trace("StrictTransportSecurity header disabled by service definition");
            }
        } else {
            super.decideInsertStrictTransportSecurityHeader(httpServletResponse, httpServletRequest, result);
        }
    }
}
