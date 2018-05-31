package org.apereo.cas.services.web.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.security.ResponseHeadersEnforcementFilter;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.ArgumentExtractor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link RegisteredServiceResponseHeadersEnforcementFilter}. A filter extension that looks at the properties of a
 * registered service to determine if headers should be injected into the response.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class RegisteredServiceResponseHeadersEnforcementFilter extends ResponseHeadersEnforcementFilter {
    private final ServicesManager servicesManager;
    private final ArgumentExtractor argumentExtractor;

    @Override
    protected void decideInsertContentSecurityPolicyHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        if (shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY)) {
            super.insertContentSecurityPolicyHeader(httpServletResponse, httpServletRequest);
        } else {
            super.decideInsertContentSecurityPolicyHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertXSSProtectionHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        if (shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION)) {
            super.insertXSSProtectionHeader(httpServletResponse, httpServletRequest);
        } else {
            super.decideInsertXSSProtectionHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertXFrameOptionsHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        if (shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS)) {
            super.insertXFrameOptionsHeader(httpServletResponse, httpServletRequest);
        } else {
            super.decideInsertXFrameOptionsHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertXContentTypeOptionsHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        if (shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS)) {
            super.insertXContentTypeOptionsHeader(httpServletResponse, httpServletRequest);
        } else {
            super.decideInsertXContentTypeOptionsHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertCacheControlHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        if (shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL)) {
            super.insertCacheControlHeader(httpServletResponse, httpServletRequest);
        } else {
            super.decideInsertCacheControlHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertStrictTransportSecurityHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        if (shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest,
            RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY)) {
            super.insertStrictTransportSecurityHeader(httpServletResponse, httpServletRequest);
        } else {
            super.decideInsertStrictTransportSecurityHeader(httpServletResponse, httpServletRequest);
        }
    }

    private boolean shouldHttpHeaderBeInjectedIntoResponse(final HttpServletRequest request,
                                                           final RegisteredServiceProperties property) {
        final var result = getRegisteredServiceFromRequest(request);
        if (result.isPresent()) {
            final var properties = result.get().getProperties();
            if (properties.containsKey(property.getPropertyName())) {
                final var prop = properties.get(property.getPropertyName());
                return BooleanUtils.toBoolean(prop.getValue());
            }
        }
        return false;
    }

    /**
     * Gets registered service from request.
     * Reading the request body by the argument extractor here may cause the underlying request stream
     * to close. If there are any underlying controllers or components that expect to read
     * or parse the request body, like those that handle ticket validation, they would fail given the
     * {@link HttpServletRequest#getReader()} is consumed by the argument extractor here and not available anymore.
     * Therefor, any of the inner components of the extractor might have to cache the request body
     * as an attribute, etc so they can re-process and re-extract as needed.
     *
     * @param request the request
     * @return the registered service from request
     */
    private Optional<RegisteredService> getRegisteredServiceFromRequest(final HttpServletRequest request) {
        final var service = this.argumentExtractor.extractService(request);
        if (service != null) {
            return Optional.ofNullable(this.servicesManager.findServiceBy(service));
        }
        return Optional.empty();
    }
}
