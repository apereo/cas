package org.apereo.cas.pac4j;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.DefaultBrowserStorage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.session.JEESessionStore;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BrowserWebStorageSessionStore}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Accessors(chain = true)
@Getter
@Setter
public class BrowserWebStorageSessionStore extends JEESessionStore {
    private final CipherExecutor webflowCipherExecutor;
    private final String browserStorageContextKey;

    @Override
    public Optional<Object> getTrackableSession(final WebContext context) {
        val currentSession = super.getTrackableSession(context);
        val attributes = new LinkedHashMap<String, Object>();
        currentSession
            .map(HttpSession.class::cast)
            .ifPresent(session -> {
                val names = session.getAttributeNames();
                while (names.hasMoreElements()) {
                    val name = names.nextElement();
                    val value = session.getAttribute(name);
                    if (value != null) {
                        attributes.put(name, value);
                    }
                }
            });
        val encoded = SerializationUtils.serializeAndEncodeObject(this.webflowCipherExecutor, attributes);
        val trackableSession = new String(encoded, StandardCharsets.UTF_8);
        return Optional.of(DefaultBrowserStorage
            .builder()
            .context(browserStorageContextKey)
            .storageType(BrowserStorage.BrowserStorageTypes.LOCAL)
            .build()
            .setPayloadJson(Map.of("context", trackableSession)));
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context,
                                                            final Object trackableSession) {
        val encoded = DefaultBrowserStorage
            .builder()
            .storageType(BrowserStorage.BrowserStorageTypes.LOCAL)
            .context(browserStorageContextKey)
            .payload(trackableSession instanceof final BrowserStorage bs ? bs.getPayload() : trackableSession.toString())
            .build()
            .getPayloadJson()
            .get("context")
            .toString()
            .getBytes(StandardCharsets.UTF_8);
        val attributes = (Map<String, Object>) SerializationUtils.decodeAndDeserializeObject(encoded, webflowCipherExecutor, LinkedHashMap.class);
        attributes.forEach((key, value) -> set(context, key, value));
        context.setRequestAttribute("sessionStorageAttributes", attributes);
        return Optional.of(this);
    }

    /**
     * Gets session attributes.
     *
     * @param context the context
     * @return the session attributes
     */
    public Map<String, Object> getSessionAttributes(final WebContext context) {
        return context.getRequestAttribute("sessionStorageAttributes", Map.class).orElseGet(LinkedHashMap::new);
    }

    /**
     * Set session attributes for session store.
     *
     * @param context    the context
     * @param properties the properties
     * @return the session store
     */
    @CanIgnoreReturnValue
    public SessionStore withSessionAttributes(final WebContext context, final Map<String, Object> properties) {
        properties.forEach((key, value) -> set(context, key, value));
        return this;
    }
}
