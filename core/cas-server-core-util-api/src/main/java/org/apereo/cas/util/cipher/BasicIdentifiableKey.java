package org.apereo.cas.util.cipher;

import org.apereo.cas.util.crypto.IdentifiableKey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.io.Serial;
import java.security.Key;

/**
 * This is {@link BasicIdentifiableKey}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
public class BasicIdentifiableKey implements IdentifiableKey {
    @Serial
    private static final long serialVersionUID = 2615165623502691246L;

    private final String id;

    @Delegate(types = Key.class)
    private final Key key;
}
