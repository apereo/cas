package com.yubico.core;

import com.yubico.webauthn.data.ByteArray;
import jakarta.servlet.http.HttpServletRequest;

import java.util.function.Function;

/**
 * WebAuthn cache.
 *
 * @author Jerome LELEU
 * @since 7.3.0
 */
public interface WebAuthnCache<R> {

    void put(HttpServletRequest request, ByteArray key, R obj);

    R getIfPresent(HttpServletRequest request, ByteArray key);

    R get(HttpServletRequest request, ByteArray key, Function<ByteArray, ? extends R> mappingFunction);

    void invalidate(HttpServletRequest request, ByteArray key);
}