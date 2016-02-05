package org.jasig.cas.util;

/**
 * String utility class.
 * @author Timur Duehr
 * @since 4.3.0
 */
public final class StringUtils {
    private StringUtils() {
    }

    /**
     * Is null, empty or all whitespace.
     * @param cs string to check for emptiness
     * @return emptiness
     */
    public static boolean isBlank(final CharSequence cs) {
        return cs == null || cs.length() == 0 || cs.chars().allMatch(Character::isWhitespace);
    }

    /**
     * Is not blank.
     * @param cs string to check for emptiness
     * @return non-emptiness
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }
}
