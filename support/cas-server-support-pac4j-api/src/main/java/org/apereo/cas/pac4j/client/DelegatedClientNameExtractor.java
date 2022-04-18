package org.apereo.cas.pac4j.client;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link DelegatedClientNameExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface DelegatedClientNameExtractor {

    /**
     * Extract client name.
     *
     * @param context the context
     * @return the string
     */
    Optional<String> extract(HttpServletRequest context);

    /**
     * Extract from context.
     *
     * @param context the context
     * @return the optional
     */
    default Optional<String> extract(WebContext context) {
        return extract(JEEContext.class.cast(context).getNativeRequest());
    }

    /**
     * Extract client name from http request parameter.
     *
     * @return the delegated client name extractor
     */
    static DelegatedClientNameExtractor fromHttpRequest() {
        return context -> Optional.ofNullable(context.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER));
    }
}
