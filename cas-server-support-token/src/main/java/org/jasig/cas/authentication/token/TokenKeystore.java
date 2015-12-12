package org.jasig.cas.authentication.token;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link TokenKeystore}.
 * A key store is a set of {@link TokenKey} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface TokenKeystore {
    /**
     * Retrieve all {@link TokenKey}s in the store.
     *
     * @return An {@link ArrayList} of the {@linkplain TokenKey}s.
     */
    Collection<TokenKey> keys();

    /**
     * Put a new {@link TokenKey} in the store.
     *
     * @param key The {@linkplain TokenKey} to add.
     */
    void add(TokenKey key);

    /**
     * Retrieve a {@link TokenKey} from the store that has a specific name.
     *
     * @param name The name of the {@linkplain TokenKey}.
     * @return The {@linkplain TokenKey} or {@code null} if it doesn't exist.
     */
    TokenKey get(String name);
}
