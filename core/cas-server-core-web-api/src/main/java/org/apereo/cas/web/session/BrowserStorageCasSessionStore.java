package org.apereo.cas.web.session;

import module java.base;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.DefaultBrowserStorage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link BrowserStorageCasSessionStore}. Values are kept in the http session, under a prefix of their own,
 * while a request is processed; the trackable session is a {@link BrowserStorage} payload, signed and encrypted, that
 * carries a small record of those values only, the storage context they belong to and when they expire, so the browser
 * can hand them back to whichever node receives the next request. Other http session attributes never leave the server.
 * A payload issued for another context, one that has expired, or one that cannot be read restores nothing.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@Getter
public class BrowserStorageCasSessionStore extends DefaultCasSessionStore {
    private static final String PAYLOAD_CONTEXT_KEY = "context";

    private static final String REQUEST_ATTRIBUTE_SESSION_ATTRIBUTES = "sessionStorageAttributes";

    private final CipherExecutor webflowCipherExecutor;

    private final String browserStorageContextKey;

    private final Duration timeToLive;

    public BrowserStorageCasSessionStore(final CipherExecutor webflowCipherExecutor,
                                         final String browserStorageContextKey,
                                         final Duration timeToLive) {
        super(browserStorageContextKey + '.');
        this.webflowCipherExecutor = webflowCipherExecutor;
        this.browserStorageContextKey = browserStorageContextKey;
        this.timeToLive = timeToLive;
    }

    @Override
    public Optional<Object> getTrackableSession(final HttpServletRequest request) {
        val attributes = new LinkedHashMap<String, Serializable>();
        getHttpSession(request, false).ifPresent(session -> Collections.list(session.getAttributeNames())
            .stream()
            .filter(name -> name.startsWith(getPrefix()))
            .forEach(name -> {
                val value = session.getAttribute(name);
                if (value != null) {
                    val key = name.substring(getPrefix().length());
                    if (!(value instanceof final Serializable serializable)) {
                        throw new IllegalArgumentException("Session value " + key + " cannot be kept in browser storage since it is not serializable");
                    }
                    attributes.put(key, serializable);
                }
            }));
        val state = new BrowserStorageSessionState(browserStorageContextKey, Instant.now(Clock.systemUTC()).plus(timeToLive), attributes);
        val encoded = SerializationUtils.serializeAndEncodeObject(webflowCipherExecutor, state);
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
        val restored = readSessionState(trackableSession)
            .filter(state -> {
                if (!browserStorageContextKey.equals(state.context())) {
                    LOGGER.warn("Browser storage payload belongs to [{}] and not to [{}]", state.context(), browserStorageContextKey);
                    return false;
                }
                if (Instant.now(Clock.systemUTC()).isAfter(state.expiresAt())) {
                    LOGGER.warn("Browser storage payload for [{}] expired at [{}]", browserStorageContextKey, state.expiresAt());
                    return false;
                }
                return true;
            });
        restored.ifPresent(state -> {
            val attributes = new LinkedHashMap<String, Object>(state.attributes());
            attributes.forEach((key, value) -> setSessionAttribute(request, key, value));
            request.setAttribute(REQUEST_ATTRIBUTE_SESSION_ATTRIBUTES, attributes);
        });
        return restored.map(_ -> this);
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
     * Store every given attribute in the session, under the prefix of this store, so it travels with the trackable session.
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

    private Optional<BrowserStorageSessionState> readSessionState(final Object trackableSession) {
        try {
            val payload = DefaultBrowserStorage
                .builder()
                .storageType(BrowserStorage.BrowserStorageTypes.LOCAL)
                .context(browserStorageContextKey)
                .payload(trackableSession instanceof final BrowserStorage storage ? storage.getPayload() : trackableSession.toString())
                .build()
                .getPayloadJson()
                .get(PAYLOAD_CONTEXT_KEY);
            return Optional.ofNullable(payload)
                .map(value -> value.toString().getBytes(StandardCharsets.UTF_8))
                .map(encoded -> SerializationUtils.decodeAndDeserializeObject(encoded, webflowCipherExecutor, BrowserStorageSessionState.class));
        } catch (final Exception e) {
            LOGGER.warn("Unable to read browser storage payload for [{}]: [{}]", browserStorageContextKey, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * What travels through browser storage: the values stored through this store, the storage context they belong to,
     * and when they expire.
     *
     * @param context    the browser storage context
     * @param expiresAt  the instant after which the values are refused
     * @param attributes the values, by key without the prefix
     */
    private record BrowserStorageSessionState(String context, Instant expiresAt, Map<String, Serializable> attributes) implements Serializable {
        @Serial
        private static final long serialVersionUID = 4626351813458745410L;

        BrowserStorageSessionState {
            Objects.requireNonNull(context, "context");
            Objects.requireNonNull(expiresAt, "expiresAt");
            Objects.requireNonNull(attributes, "attributes");
        }
    }
}
