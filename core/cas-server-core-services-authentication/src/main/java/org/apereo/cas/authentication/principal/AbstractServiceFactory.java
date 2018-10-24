package org.apereo.cas.authentication.principal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The {@link AbstractServiceFactory} is the parent class providing
 * convenience methods for creating service objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@SuppressWarnings("TypeParameterShadowing")
@Slf4j
@ToString
@Getter
@Setter
public abstract class AbstractServiceFactory<T extends Service> implements ServiceFactory<T> {

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public <T extends Service> T createService(final String id, final Class<T> clazz) {
        final Service service = createService(id);
        if (!clazz.isAssignableFrom(service.getClass())) {
            throw new ClassCastException("Service [" + service.getId() + " is of type " + service.getClass() + " when we were expecting " + clazz);
        }
        return (T) service;
    }

    @Override
    public <T extends Service> T createService(final HttpServletRequest request, final Class<T> clazz) {
        final Service service = createService(request);
        if (!clazz.isAssignableFrom(service.getClass())) {
            throw new ClassCastException("Service [" + service.getId() + " is of type " + service.getClass() + " when we were expecting " + clazz);
        }
        return (T) service;
    }

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
        final int jsessionPosition = url.indexOf(";jsession");
        if (jsessionPosition == -1) {
            return url;
        }
        final int questionMarkPosition = url.indexOf('?');
        if (questionMarkPosition < jsessionPosition) {
            return url.substring(0, url.indexOf(";jsession"));
        }
        return url.substring(0, jsessionPosition) + url.substring(questionMarkPosition);
    }

    /**
     * Gets source parameter.
     *
     * @param request    the request
     * @param paramNames the param names
     * @return the source parameter
     */
    protected static String getSourceParameter(final HttpServletRequest request, final String... paramNames) {
        if (request != null) {
            final Map<String, String[]> parameterMap = request.getParameterMap();
            final String param = Stream.of(paramNames)
                .filter(p -> parameterMap.containsKey(p) || request.getAttribute(p) != null)
                .findFirst()
                .orElse(null);
            return param;
        }
        return null;
    }
}
