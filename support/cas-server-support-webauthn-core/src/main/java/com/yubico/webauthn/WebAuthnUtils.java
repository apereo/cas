package com.yubico.webauthn;

import com.yubico.webauthn.data.ByteArray;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apereo.cas.util.RandomUtils;

/**
 * This is {@link WebAuthnUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@UtilityClass
public class WebAuthnUtils {
    private static final int ID_LENGTH = 32;

    /**
     * Generate random id byte array.
     *
     * @return the byte array
     */
    public static ByteArray generateRandomId() {
        val bytes = new byte[ID_LENGTH];
        RandomUtils.getNativeInstance().nextBytes(bytes);
        return new ByteArray(bytes);
    }
}
