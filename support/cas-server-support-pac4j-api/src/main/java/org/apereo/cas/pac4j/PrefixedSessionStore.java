package org.apereo.cas.pac4j;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.session.JEESessionStore;

import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * A SessionStore using a prefix for all keys.
 *
 * @author Jerome LELEU
 * @since 6.6.0
 */
@Slf4j
public class PrefixedSessionStore extends JEESessionStore {

    @Getter
    @Setter
    private String prefix = StringUtils.EMPTY;

    public PrefixedSessionStore() {}

    protected PrefixedSessionStore(final HttpSession httpSession) {
        super(httpSession);
    }

    protected String computePrefixedKey(final String key) {
        return prefix + key;
    }

    @Override
    public Optional<Object> get(final WebContext context, final String key) {
        val httpSession = getNativeSession(context, false);
        val prefixedKey = computePrefixedKey(key);
        if (httpSession.isPresent()) {
            val value = httpSession.get().getAttribute(prefixedKey);
            LOGGER.debug("Get value: [{}] for key: [{}]", value, prefixedKey);
            return Optional.ofNullable(value);
        } else {
            LOGGER.debug("Can't get value for key: [{}], no session available", prefixedKey);
            return Optional.empty();
        }
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        val prefixedKey = computePrefixedKey(key);
        if (value == null) {
            val httpSession = getNativeSession(context, false);
            if (httpSession.isPresent()) {
                LOGGER.debug("Remove value for key: [{}]", prefixedKey);
                httpSession.get().removeAttribute(prefixedKey);
            }
        } else {
            val httpSession = getNativeSession(context, true);
            if (value instanceof Exception) {
                LOGGER.debug("Set key: [{}] for value: [{}]", prefixedKey, value.toString());
            } else {
                LOGGER.debug("Set key: [{}] for value: [{}]", prefixedKey, value);
            }
            httpSession.get().setAttribute(prefixedKey, value);
        }
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context, final Object trackableSession) {
        if (trackableSession != null) {
            LOGGER.debug("Rebuild session from trackable session: [{}]", trackableSession);
            val sessionStore = new PrefixedSessionStore((HttpSession) trackableSession);
            sessionStore.setPrefix(this.getPrefix());
            return Optional.of(sessionStore);
        } else {
            LOGGER.debug("Unable to build session from trackable session");
            return Optional.empty();
        }
    }
}
