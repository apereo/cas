package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.SimpleUrlValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.core.Ordered;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The {@link AbstractServiceFactory} is the parent class providing
 * convenience methods for creating service objects.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SuppressWarnings("TypeParameterShadowing")
@ToString
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractServiceFactory<T extends Service> implements ServiceFactory<T> {
    private static final List<String> IGNORED_ATTRIBUTES_PARAMS = List.of(
        CasProtocolConstants.PARAMETER_PASSWORD,
        CasProtocolConstants.PARAMETER_SERVICE,
        CasProtocolConstants.PARAMETER_TARGET_SERVICE,
        CasProtocolConstants.PARAMETER_TICKET,
        CasProtocolConstants.PARAMETER_FORMAT);

    private final TenantExtractor tenantExtractor;

    private int order = Ordered.LOWEST_PRECEDENCE;


    /**
     * Cleanup the url. Removes jsession ids and query strings.
     *
     * @param url the url
     * @return sanitized url.
     */
    protected static String cleanupUrl(final String url) {
        if (url == null) {
            return null;
        }
        val jsessionPosition = url.indexOf(";jsession");
        if (jsessionPosition == -1) {
            return url;
        }
        val questionMarkPosition = url.indexOf('?');
        if (questionMarkPosition < jsessionPosition) {
            return url.substring(0, url.indexOf(";jsession"));
        }
        return url.substring(0, jsessionPosition) + url.substring(questionMarkPosition);
    }

    protected static String getSourceParameter(final HttpServletRequest request, final String... paramNames) {
        if (request != null) {
            val parameterMap = request.getParameterMap();
            return Stream.of(paramNames)
                .filter(p -> parameterMap.containsKey(p) || request.getAttribute(p) != null)
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    protected Map<String, List> extractQueryParameters(final Service service) {
        val attributes = new LinkedHashMap<String, List>();
        if (service instanceof final WebApplicationService webApplicationService) {
            val originalUrl = webApplicationService.getOriginalUrl();
            try {
                if (StringUtils.isNotBlank(originalUrl) && SimpleUrlValidator.getInstance().isValid(originalUrl)) {
                    val queryParams = FunctionUtils.doUnchecked(() -> new URIBuilder(originalUrl).getQueryParams());
                    queryParams.forEach(pair -> {
                        val values = CollectionUtils.wrapArrayList(StringEscapeUtils.escapeHtml4(pair.getValue()));
                        attributes.put(pair.getName(), values);
                    });
                }
            } catch (final Exception e) {
                LOGGER.error("Unable to extract query parameters from [{}]: [{}]", originalUrl, e.getMessage());
            }
        }
        return attributes;
    }

    protected Service populateAttributes(final Service service, final HttpServletRequest request) {
        val attributes = (Map) request.getParameterMap()
            .entrySet()
            .stream()
            .filter(entry -> !IGNORED_ATTRIBUTES_PARAMS.contains(entry.getKey()))
            .map(entry -> Pair.of(entry.getKey(), CollectionUtils.toCollection(entry.getValue(), ArrayList.class)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        attributes.putAll(extractQueryParameters(service));

        val collectAttributes = Objects.requireNonNullElse((Boolean) request.getAttribute(COLLECT_SERVICE_ATTRIBUTES), Boolean.TRUE);
        if (collectAttributes) {
            val httpRequest = new LinkedHashMap<>();
            FunctionUtils.doIfNotBlank(request.getPathInfo(), value -> collectHttpRequestProperty("pathInfo", value, httpRequest));
            FunctionUtils.doIfNotBlank(request.getMethod(), value -> collectHttpRequestProperty("httpMethod", value, httpRequest));
            FunctionUtils.doIfNotBlank(request.getRequestURL(), value -> collectHttpRequestProperty("requestURL", value.toString(), httpRequest));
            FunctionUtils.doIfNotBlank(request.getRequestURI(), value -> collectHttpRequestProperty("requestURI", value, httpRequest));
            FunctionUtils.doIfNotBlank(request.getRequestId(), value -> collectHttpRequestProperty("requestId", value, httpRequest));
            FunctionUtils.doIfNotBlank(request.getContentType(), value -> collectHttpRequestProperty("contentType", value, httpRequest));
            FunctionUtils.doIfNotBlank(request.getContextPath(), value -> collectHttpRequestProperty("contextPath", value, httpRequest));
            FunctionUtils.doIfNotBlank(request.getLocalName(), value -> collectHttpRequestProperty("localeName", value, httpRequest));
            if (!httpRequest.isEmpty()) {
                attributes.put(Service.SERVICE_ATTRIBUTE_HTTP_REQUEST, httpRequest);
            }

            val cookies = new LinkedHashMap<>();
            FunctionUtils.doIfNotNull(request.getCookies(), __ -> Arrays.stream(request.getCookies())
                .forEach(cookie -> collectHttpRequestProperty("cookie-%s".formatted(cookie.getName()), cookie.getValue(), cookies)));
            if (!cookies.isEmpty()) {
                attributes.put(Service.SERVICE_ATTRIBUTE_COOKIES, cookies);
            }
            
            val headers = new LinkedHashMap<>();
            FunctionUtils.doIfNotNull(request.getHeaderNames(), __ -> StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(request.getHeaderNames().asIterator(), Spliterator.ORDERED), false)
                .forEach(header -> collectHttpRequestProperty("header-%s".formatted(header), request.getHeader(header), headers)));
            if (!headers.isEmpty()) {
                attributes.put(Service.SERVICE_ATTRIBUTE_HEADERS, headers);
            }
        }

        LOGGER.trace("Extracted attributes [{}] for service [{}]", attributes, service.getId());
        service.setAttributes(attributes);
        tenantExtractor.extract(request)
            .map(TenantDefinition::getId)
            .ifPresent(service::setTenant);
        return service;
    }

    protected void collectHttpRequestProperty(final String name, final String value, final Map attributes) {
        if (StringUtils.isNotBlank(value)) {
            val entries = CollectionUtils.wrapArrayList(StringEscapeUtils.escapeHtml4(value));
            attributes.put(HttpServletRequest.class.getName() + '.' + name, entries);
        }
    }

    @Override
    public <T extends Service> T createService(final String id, final Class<T> clazz) {
        var service = createService(id);
        if (!clazz.isAssignableFrom(service.getClass())) {
            throw new ClassCastException("Service [" + service.getId() + " is of type " + service.getClass() + " when we were expecting " + clazz);
        }
        return (T) service;
    }

    @Override
    public <T extends Service> T createService(final HttpServletRequest request, final Class<T> clazz) {
        var service = createService(request);
        if (service != null && !clazz.isAssignableFrom(service.getClass())) {
            throw new ClassCastException("Service [" + service.getId() + " is of type " + service.getClass() + " when we were expecting " + clazz);
        }
        return (T) service;
    }

    @Override
    public T createService(final String id, final HttpServletRequest request) {
        val service = createService(id);
        return (T) populateAttributes(service, request);
    }
}
