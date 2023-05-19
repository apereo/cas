package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.ValidationResponseType;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The {@link WebApplicationServiceFactory} is responsible for
 * creating {@link WebApplicationService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class WebApplicationServiceFactory extends AbstractServiceFactory<WebApplicationService> {
    private static final List<String> IGNORED_ATTRIBUTES_PARAMS = List.of(
        CasProtocolConstants.PARAMETER_PASSWORD,
        CasProtocolConstants.PARAMETER_SERVICE,
        CasProtocolConstants.PARAMETER_TARGET_SERVICE,
        CasProtocolConstants.PARAMETER_TICKET,
        CasProtocolConstants.PARAMETER_FORMAT);

    private static AbstractWebApplicationService determineWebApplicationFormat(
        final HttpServletRequest request,
        final AbstractWebApplicationService webApplicationService) {
        val format = Optional.ofNullable(request)
            .map(httpServletRequest -> httpServletRequest.getParameter(CasProtocolConstants.PARAMETER_FORMAT))
            .orElse(StringUtils.EMPTY);
        try {
            if (StringUtils.isNotBlank(format)) {
                val formatType = ValidationResponseType.valueOf(Objects.requireNonNull(format).toUpperCase(Locale.ENGLISH));
                webApplicationService.setFormat(formatType);
            }
        } catch (final Exception e) {
            LOGGER.error("Format specified in the request [{}] is not recognized", format);
        }
        return webApplicationService;
    }

    @Override
    public WebApplicationService createService(final HttpServletRequest request) {
        val serviceToUse = getRequestedService(request);
        if (StringUtils.isBlank(serviceToUse)) {
            LOGGER.trace("No service is specified in the request. Skipping service creation");
            return null;
        }
        return newWebApplicationService(request, serviceToUse);
    }

    @Override
    public WebApplicationService createService(final String id) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        return newWebApplicationService(request, id);
    }

    protected AbstractWebApplicationService newWebApplicationService(
        final HttpServletRequest request, final String serviceToUse) {
        val artifactId = Optional.ofNullable(request)
            .map(httpServletRequest -> httpServletRequest.getParameter(CasProtocolConstants.PARAMETER_TICKET))
            .orElse(null);
        val id = cleanupUrl(serviceToUse);
        val newService = new SimpleWebApplicationServiceImpl(id, serviceToUse, artifactId);
        determineWebApplicationFormat(request, newService);
        val source = getSourceParameter(request, CasProtocolConstants.PARAMETER_TARGET_SERVICE,
            CasProtocolConstants.PARAMETER_SERVICE);
        newService.setSource(source);
        if (request != null) {
            populateAttributes(newService, request);
            if (StringUtils.isNotBlank(source)) {
                newService.getAttributes().put(source, CollectionUtils.wrap(id));
            }
        }
        return newService;
    }

    protected void populateAttributes(final AbstractWebApplicationService service, final HttpServletRequest request) {
        val attributes = (Map) request.getParameterMap()
            .entrySet()
            .stream()
            .filter(entry -> !IGNORED_ATTRIBUTES_PARAMS.contains(entry.getKey()))
            .map(entry -> Pair.of(entry.getKey(), CollectionUtils.toCollection(entry.getValue(), ArrayList.class)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        attributes.putAll(extractQueryParameters(service));
        LOGGER.trace("Extracted attributes [{}] for service [{}]", attributes, service.getId());
        service.setAttributes(new HashMap(attributes));
    }

    protected Map<String, List> extractQueryParameters(final WebApplicationService service) {
        val attributes = new LinkedHashMap<String, List>();
        val originalUrl = service.getOriginalUrl();
        try {
            if (StringUtils.isNotBlank(originalUrl) && originalUrl.startsWith("http") && originalUrl.contains("?")) {
                val queryParams = FunctionUtils.doUnchecked(() -> new URIBuilder(originalUrl).getQueryParams());
                queryParams.forEach(pair -> attributes.put(pair.getName(), CollectionUtils.wrapArrayList(pair.getValue())));
            }
        } catch (final Exception e) {
            LOGGER.error("Unable to extract query parameters from [{}]: [{}]", originalUrl, e.getMessage());
        }
        return attributes;
    }

    protected String getRequestedService(final HttpServletRequest request) {
        val targetService = request.getParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE);
        val service = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        val serviceAttribute = request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);

        if (StringUtils.isNotBlank(targetService)) {
            return targetService;
        }
        if (StringUtils.isNotBlank(service)) {
            return service;
        }
        if (serviceAttribute != null) {
            if (serviceAttribute instanceof Service) {
                return ((Principal) serviceAttribute).getId();
            }
            return serviceAttribute.toString();
        }
        return null;
    }
}
