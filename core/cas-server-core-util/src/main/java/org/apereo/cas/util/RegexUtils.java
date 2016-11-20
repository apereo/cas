package org.apereo.cas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Utility class to assist with regex operations.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class RegexUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegexUtils.class);
    private static final Pattern MATCH_NOTHING_PATTERN = Pattern.compile("a^");

    private RegexUtils() {}

    /**
     * Check to see if the specified pattern is a valid regular expression.
     *
     * @param pattern the pattern
     * @return whether this is a valid regex or not
     */
    public static boolean isValidRegex(final String pattern) {
        try {
            if (pattern != null) {
                Pattern.compile(pattern).pattern();
                return true;
            }
        } catch (final PatternSyntaxException exception) {
            LOGGER.debug("Pattern {} is not a valid regex.", pattern);
        }
        return false;
    }

    /**
     * Creates the pattern. Matching is by default
     * case insensitive.
     *
     * @param pattern the pattern, may not be null.
     * @return the pattern or empty.
     */
    public static Pattern createPattern(final String pattern) {
        if (RegexUtils.isValidRegex(pattern)) {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        }
        return MATCH_NOTHING_PATTERN;
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
