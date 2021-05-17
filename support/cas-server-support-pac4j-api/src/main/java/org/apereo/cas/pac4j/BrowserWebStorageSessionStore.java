package org.apereo.cas.pac4j;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.apereo.cas.web.DefaultBrowserSessionStorage;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;

import javax.servlet.http.HttpSession;
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
public class BrowserWebStorageSessionStore extends JEESessionStore {
    private final CipherExecutor webflowCipherExecutor;

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
        return Optional.of(DefaultBrowserSessionStorage.builder().payload(trackableSession).build());
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context,
                                                            final Object trackableSession) {
        val encoded = trackableSession.toString().getBytes(StandardCharsets.UTF_8);
        val attributes = (Map<String, Object>) SerializationUtils.decodeAndDeserializeObject(encoded, webflowCipherExecutor, LinkedHashMap.class);
        attributes.forEach((key, value) -> set(context, key, value));
        return Optional.of(this);
    }
}
