package org.jasig.cas.authentication.principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link AbstractServiceFactory} is the parent class providing
 * convenience methods for creating service objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractServiceFactory<T extends Service> implements ServiceFactory<T> {

    /** Logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public <T1 extends Service> T1 createService(final String id, final Class<? extends Service> clazz) {
        final Service service = createService(id);

        if (!clazz.isAssignableFrom(service.getClass())) {
            throw new ClassCastException("Service [" + service.getId()
                    + " is of type " + service.getClass()
                    + " when we were expecting " + clazz);
        }
        return (T1) service;
    }

    @Override
    public <T1 extends Service> T1 createService(final HttpServletRequest request, final Class<? extends Service> clazz) {
        final Service service = createService(request);

        if (!clazz.isAssignableFrom(service.getClass())) {
            throw new ClassCastException("Service [" + service.getId()
                    + " is of type " + service.getClass()
                    + " when we were expecting " + clazz);
        }
        return (T1) service;
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

        return url.substring(0, jsessionPosition)
                + url.substring(questionMarkPosition);
    }

}

