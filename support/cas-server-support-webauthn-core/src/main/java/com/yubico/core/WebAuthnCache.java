package com.yubico.core;

import com.yubico.webauthn.data.ByteArray;

import java.util.function.Function;

/**
 * WebAuthn cache.
 *
 * @author Jerome LELEU
 * @since 7.3.0
 */
public interface WebAuthnCache<R> {

    void put(ByteArray key, R request);

    R getIfPresent(ByteArray key);

    R get(ByteArray key, Function<ByteArray, ? extends R> mappingFunction);

    void invalidate(ByteArray key);
}
