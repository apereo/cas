package org.apereo.cas.gauth;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.val;
import java.util.Locale;

/**
 * This is {@link RedisCompositeKey}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@AllArgsConstructor
@With
public class RedisCompositeKey {
    private static final String CAS_PREFIX_TOKEN_ACCOUNT = "CAS_TOKEN_ACCOUNT";

    private static final String CAS_PREFIX_TOKEN_PRINCIPAL = "CAS_TOKEN_PRINCIPAL";

    @Builder.Default
    private final String query = "*";

    private final String prefix;

    /**
     * To key pattern string.
     *
     * @return the string
     */
    public String toKeyPattern() {
        return String.format("%s:%s", prefix, query);
    }

    /**
     * For credentials redis composite key.
     *
     * @return the redis composite key
     */
    public static RedisCompositeKey forAccounts() {
        return RedisCompositeKey.builder().prefix(CAS_PREFIX_TOKEN_ACCOUNT).build();
    }

    /**
     * For principals redis composite key.
     *
     * @return the redis composite key
     */
    public static RedisCompositeKey forPrincipals() {
        return RedisCompositeKey.builder().prefix(CAS_PREFIX_TOKEN_PRINCIPAL).build();
    }

    /**
     * With principal redis composite key.
     *
     * @param account the account
     * @return the redis composite key
     */
    public RedisCompositeKey withPrincipal(final OneTimeTokenAccount account) {
        return withPrincipal(account.getUsername());
    }

    /**
     * With principal redis composite key.
     *
     * @param account the account
     * @return the redis composite key
     */
    public RedisCompositeKey withPrincipal(final String account) {
        val username = account.trim().toLowerCase(Locale.ENGLISH);
        return RedisCompositeKey.forPrincipals().withQuery(username);
    }

    /**
     * With account redis composite key.
     *
     * @param account the account
     * @return the redis composite key
     */
    public RedisCompositeKey withAccount(final OneTimeTokenAccount account) {
        return withAccount(account.getId());
    }

    /**
     * With account redis composite key.
     *
     * @param account the account
     * @return the redis composite key
     */
    public RedisCompositeKey withAccount(final long account) {
        return RedisCompositeKey.forAccounts().withQuery(String.valueOf(account));
    }


}
