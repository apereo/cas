package org.apereo.cas.util;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class to assist with regex operations.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class RegexUtils {

    private RegexUtils() {}

    /**
     * Check to see if the specified pattern is a valid regular expression.
     *
     * @param pattern the pattern
     * @return whether this is a valid regex or not
     */
    public static boolean isValidRegex(final String pattern) {
        return pattern != null;
    }

    /**
     * Creates the pattern. Matching is by default
     * case insensitive.
     *
     * @param pattern the pattern, may not be null.
     * @return the pattern or empty. 
     */
    public static Optional<Pattern> createPattern(final String pattern) {
        if (RegexUtils.isValidRegex(pattern)) {
            return Optional.of(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
        }
        return Optional.empty();
    }
    
    /**
     * Concatenate all elements in the given collection to form a regex pattern.
     *
     * @param requiredValues  the required values
     * @param caseInsensitive the case insensitive
     * @return the pattern
     */
    public static Pattern concatenate(final Collection<String> requiredValues, final boolean caseInsensitive) {
        final String pattern = requiredValues.stream().collect(Collectors.joining("|", "(", ")"));
        if (isValidRegex(pattern)) {
            return Pattern.compile(pattern, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
        }
        return null;
    }
}
