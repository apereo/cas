package org.apereo.cas.pac4j;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.DefaultBrowserSessionStorage;

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

    private Map<String, Object> sessionAttributes = new LinkedHashMap<>();

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
        attributes.putAll(sessionAttributes);

        val encoded = SerializationUtils.serializeAndEncodeObject(this.webflowCipherExecutor, attributes);
        val trackableSession = new String(encoded, StandardCharsets.UTF_8);
        return Optional.of(DefaultBrowserSessionStorage.builder().payload(trackableSession).build());
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context,
                                                            final Object trackableSession) {
        val encoded = trackableSession instanceof BrowserSessionStorage storage
            ? storage.getPayload().getBytes(StandardCharsets.UTF_8)
            : trackableSession.toString().getBytes(StandardCharsets.UTF_8);
        val attributes = (Map<String, Object>) SerializationUtils.decodeAndDeserializeObject(encoded, webflowCipherExecutor, LinkedHashMap.class);
        attributes.forEach((key, value) -> set(context, key, value));
        this.sessionAttributes.putAll(attributes);
        return Optional.of(this);
    }
}
