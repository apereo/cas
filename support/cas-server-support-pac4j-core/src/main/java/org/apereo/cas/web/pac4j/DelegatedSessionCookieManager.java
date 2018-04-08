package org.apereo.cas.web.pac4j;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.core.context.J2EContext;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Map;

/**
 * This is {@link DelegatedSessionCookieManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class DelegatedSessionCookieManager {

    private final CookieRetrievingCookieGenerator cookieGenerator;
    private final SessionStoreCookieSerializer serializer = new SessionStoreCookieSerializer();

    /**
     * Store.
     *
     * @param webContext the web context
     */
    public void store(final J2EContext webContext) {
        final Map<String, Object> session = Maps.newLinkedHashMap();
        final HttpSession webSession = (HttpSession) webContext.getSessionStore().getTrackableSession(webContext);
        final Enumeration<String> names = webSession.getAttributeNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Object value = webSession.getAttribute(name);
            session.put(name, value);
        }
        final String cookieValue = serializeSessionValues(session);
        cookieGenerator.addCookie(webContext.getRequest(), webContext.getResponse(), cookieValue);
    }

    /**
     * Retrieve.
     *
     * @param webContext the web context
     */
    public void restore(final J2EContext webContext) {
        final String value = cookieGenerator.retrieveCookieValue(webContext.getRequest());
        if (StringUtils.isNotBlank(value)) {
            final String blob = EncodingUtils.hexDecode(value);
            final Map<String, Object> session = serializer.from(blob);
            session.forEach((k, v) -> webContext.getSessionStore().set(webContext, k, v));
        }
        cookieGenerator.removeCookie(webContext.getResponse());
    }

    private String serializeSessionValues(final Map<String, Object> attributes) {
        final String blob = serializer.toString(attributes);
        return EncodingUtils.hexEncode(blob);
    }

}
