package com.yubico.core;

import jakarta.servlet.http.HttpServletRequest;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.data.ByteArray;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * WebAuthn cache using the web session.
 *
 * @author Jerome LELEU
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class WebSessionWebAuthnCache<R> implements WebAuthnCache<R> {

    private static final ObjectMapper MAPPER = JacksonCodecs.json().findAndRegisterModules();

    private final String mapName;

    private final Class<R> clazz;

    private static HttpSession retrieveSession(final HttpServletRequest request) {
        HttpSession session = null;
        if (request != null) {
            session = request.getSession(true);
            LOGGER.trace("Session from web request: [{}]", session);
        } else {
            LOGGER.error("Cannot get session");
        }
        return session;
    }

    private Map<String,String> retrieveMap(final HttpServletRequest request) {
        val session = retrieveSession(request);
        if (session != null) {
            var map = (Map<String, String>) session.getAttribute(mapName);
            if (map == null) {
                map = new HashMap<>();
                session.setAttribute(mapName, map);
            }
            return map;
        }
        return new HashMap<>();
    }

    @Override
    public void put(final HttpServletRequest request, final ByteArray key, final R obj) {
        val key64 = key.getBase64();
        FunctionUtils.doUnchecked(__ -> {
            val value = MAPPER.writeValueAsString(obj);
            LOGGER.trace("Put value([{}]): [{}] for key: [{}]", clazz, value, key64);
            retrieveMap(request).put(key64, value);
        });
    }

    @Override
    public R getIfPresent(final HttpServletRequest request, final ByteArray key) {
        val key64 = key.getBase64();
        val value = retrieveMap(request).get(key64);
        if (value == null) {
            return null;
        }
        LOGGER.trace("GetIfPresent value([{}]): [{}] for key: [{}]", clazz, value, key64);
        return FunctionUtils.doUnchecked(() -> MAPPER.readValue(value, clazz));
    }

    @Override
    public R get(final HttpServletRequest request, final ByteArray key, final Function<ByteArray, ? extends R> mappingFunction) {
        val key64 = key.getBase64();
        val map = retrieveMap(request);
        return FunctionUtils.doUnchecked(() -> {
            var value = retrieveMap(request).get(key64);
            if (value == null) {
                val newObj = mappingFunction.apply(key);
                if (newObj != null) {
                    value = MAPPER.writeValueAsString(newObj);
                    LOGGER.trace("Save value([{}]): [{}] for key: [{}]", clazz, value, key64);
                    map.put(key64, value);
                }
                return newObj;
            }
            LOGGER.trace("Get value([{}]): [{}] for key: [{}]", clazz, value, key64);
            return MAPPER.readValue(value, clazz);
        });
    }

    @Override
    public void invalidate(final HttpServletRequest request, final ByteArray key) {
        val key64 = key.getBase64();
        LOGGER.trace("Invalidate value for key: [{}]", key64);
        retrieveMap(request).remove(key64);
    }
}
