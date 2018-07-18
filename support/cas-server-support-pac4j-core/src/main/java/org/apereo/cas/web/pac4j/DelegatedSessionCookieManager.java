package org.apereo.cas.web.pac4j;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.J2EContext;

import javax.servlet.http.HttpSession;
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
    private final StringSerializer<Map<String, Object>> serializer;

    /**
     * Store.
     *
     * @param webContext the web context
     */
    public void store(final J2EContext webContext) {
        val session = Maps.<String, Object>newLinkedHashMap();
        val webSession = (HttpSession) webContext.getSessionStore().getTrackableSession(webContext);
        val names = webSession.getAttributeNames();
        while (names.hasMoreElements()) {
            val name = names.nextElement();
            val value = webSession.getAttribute(name);
            session.put(name, value);
        }
        val cookieValue = serializeSessionValues(session);
        cookieGenerator.addCookie(webContext.getRequest(), webContext.getResponse(), cookieValue);
    }

    /**
     * Retrieve.
     *
     * @param webContext the web context
     */
    public void restore(final J2EContext webContext) {
        val value = cookieGenerator.retrieveCookieValue(webContext.getRequest());
        if (StringUtils.isNotBlank(value)) {
            val blob = EncodingUtils.hexDecode(value);
            val session = serializer.from(blob);
            session.forEach((k, v) -> webContext.getSessionStore().set(webContext, k, v));
        }
        cookieGenerator.removeCookie(webContext.getResponse());
    }

    private String serializeSessionValues(final Map<String, Object> attributes) {
        val blob = serializer.toString(attributes);
        return EncodingUtils.hexEncode(blob);
    }

}
