package org.apereo.cas.gauth.web.flow;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Time-bound verification marks on a {@link OneTimeTokenAccount}.
 * Removing or confirming a device takes two requests: the first validates a code and marks the account as verified,
 * the second acts on that mark. The login webflow keeps its state client-side, so the mark is stored on the account.
 * It records when it was set and is honored only for {@link #VERIFICATION_LIFETIME}, so a verification that was
 * started and abandoned cannot be used later on. Marks without a time, as written by earlier versions, are ignored.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
class GoogleAuthenticatorAccountVerificationUtils {
    /**
     * How long a verification mark is honored, in either direction, to allow for clock skew between nodes.
     */
    static final Duration VERIFICATION_LIFETIME = Duration.ofMinutes(5);

    private static final char SEPARATOR = ':';

    /**
     * Mark the account as verified for the given purpose, replacing any earlier mark for it.
     *
     * @param account  the account
     * @param property the verification property
     */
    static void markVerified(final OneTimeTokenAccount account, final String property) {
        val properties = withoutVerification(account, property);
        properties.add(property + SEPARATOR + Instant.now(Clock.systemUTC()).toEpochMilli());
        account.setProperties(properties);
    }

    /**
     * Whether the account carries a verification mark for the given purpose that is still within its lifetime.
     *
     * @param account  the account
     * @param property the verification property
     * @return true if verified recently enough
     */
    static boolean isVerified(final OneTimeTokenAccount account, final String property) {
        val prefix = property + SEPARATOR;
        val now = Instant.now(Clock.systemUTC());
        return account.getProperties()
            .stream()
            .filter(value -> value.startsWith(prefix))
            .map(value -> value.substring(prefix.length()))
            .filter(NumberUtils::isDigits)
            .map(value -> Instant.ofEpochMilli(Long.parseLong(value)))
            .anyMatch(verifiedAt -> Duration.between(verifiedAt, now).abs().compareTo(VERIFICATION_LIFETIME) < 0);
    }

    /**
     * Remove every verification mark for the given purpose, with or without a time. The account gets a new list,
     * since the one a repository hands back is not always modifiable.
     *
     * @param account  the account
     * @param property the verification property
     */
    static void clearVerification(final OneTimeTokenAccount account, final String property) {
        account.setProperties(withoutVerification(account, property));
    }

    private static List<String> withoutVerification(final OneTimeTokenAccount account, final String property) {
        return account.getProperties()
            .stream()
            .filter(value -> !value.equals(property) && !value.startsWith(property + SEPARATOR))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
