package org.apereo.cas.util.crypto;

import lombok.NoArgsConstructor;

/**
 * This is {@link DecryptionException}.
 *
 * @author Timur Duehr
 * @since 5.3.5
 */
@NoArgsConstructor
public class DecryptionException extends RuntimeException {
    private static final long serialVersionUID = 7000029397148959897L;

    public DecryptionException(final Throwable cause) {
        super(cause);
    }
}
