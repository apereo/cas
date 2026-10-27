package org.apereo.cas.web.session;

import module java.base;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.DefaultBrowserStorage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link BrowserStorageCasSessionStore}. Values are kept in the http session while a request is processed;
 * the trackable session is a {@link BrowserStorage} payload that carries every session attribute, signed and encrypted,
 * so the browser can hand the session back to whichever node receives the next request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
@RequiredArgsConstructor
public class BrowserStorageCasSessionStore extends DefaultCasSessionStore {
    private static final String PAYLOAD_CONTEXT_KEY = "context";

    private static final String REQUEST_ATTRIBUTE_SESSION_ATTRIBUTES = "sessionStorageAttributes";

    private final CipherExecutor webflowCipherExecutor;

    private final String browserStorageContextKey;

    @Override
    public Optional<Object> getTrackableSession(final HttpServletRequest request) {
        val attributes = new LinkedHashMap<String, Object>();
        val session = request.getSession(false);
        if (session != null) {
            val names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                val name = names.nextElement();
                val value = session.getAttribute(name);
                if (value != null) {
                    attributes.put(name, value);
                }
            }
        }
        val encoded = SerializationUtils.serializeAndEncodeObject(webflowCipherExecutor, attributes);
        val trackableSession = new String(encoded, StandardCharsets.UTF_8);
        return Optional.of(DefaultBrowserStorage
            .builder()
            .context(browserStorageContextKey)
            .storageType(BrowserStorage.BrowserStorageTypes.LOCAL)
            .build()
            .setPayloadJson(Map.of(PAYLOAD_CONTEXT_KEY, trackableSession)));
    }

    @Override
    public Optional<CasSessionStore> buildFromTrackableSession(final HttpServletRequest request, final Object trackableSession) {
        val encoded = DefaultBrowserStorage
            .builder()
            .storageType(BrowserStorage.BrowserStorageTypes.LOCAL)
            .context(browserStorageContextKey)
            .payload(trackableSession instanceof final BrowserStorage storage ? storage.getPayload() : trackableSession.toString())
            .build()
            .getPayloadJson()
            .get(PAYLOAD_CONTEXT_KEY)
            .toString()
            .getBytes(StandardCharsets.UTF_8);
        val attributes = (Map<String, Object>) SerializationUtils.decodeAndDeserializeObject(encoded, webflowCipherExecutor, LinkedHashMap.class);
        attributes.forEach((key, value) -> setSessionAttribute(request, key, value));
        request.setAttribute(REQUEST_ATTRIBUTE_SESSION_ATTRIBUTES, attributes);
        return Optional.of(this);
    }

    /**
     * Get the session attributes restored by the last {@link #buildFromTrackableSession(HttpServletRequest, Object)}
     * call made for this request.
     *
     * @param request the request
     * @return the restored attributes, or an empty map
     */
    public Map<String, Object> getSessionAttributes(final HttpServletRequest request) {
        return request.getAttribute(REQUEST_ATTRIBUTE_SESSION_ATTRIBUTES) instanceof final Map<?, ?> attributes
            ? (Map<String, Object>) attributes
            : new LinkedHashMap<>();
    }

    /**
     * Store every given attribute in the session.
     *
     * @param request    the request
     * @param attributes the attributes
     * @return this store
     */
    @CanIgnoreReturnValue
    public BrowserStorageCasSessionStore withSessionAttributes(final HttpServletRequest request, final Map<String, ?> attributes) {
        attributes.forEach((key, value) -> setSessionAttribute(request, key, value));
        return this;
    }
}
