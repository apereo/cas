package org.apereo.cas.pac4j.client;

import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.net.URIBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
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
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(DelegatedClientNameExtractor.class);
    
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
    default Optional<String> extract(final WebContext context) {
        return extract(((JEEContext) context).getNativeRequest());
    }

    /**
     * Extract client name from http request parameter.
     *
     * @return the delegated client name extractor
     */
    static DelegatedClientNameExtractor fromHttpRequest() {
        return context -> {
            var clientName = context.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
            var relayState = context.getParameter("RelayState");
            if (StringUtils.isBlank(clientName) && StringUtils.isNotBlank(relayState) && Strings.CI.startsWith(relayState, "http")) {
                clientName = extractClientNameFromRelayStateUrl(relayState);
            }
            return Optional.ofNullable(clientName);
        };
    }

    private static String extractClientNameFromRelayStateUrl(final String relayState) {
        return FunctionUtils.doAndHandle(() -> {
            val uriBuilder = new URIBuilder(relayState);
            val relayStateParam = uriBuilder.getFirstQueryParam(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
            return relayStateParam != null ? relayStateParam.getValue() : null;
        });
    }
}
