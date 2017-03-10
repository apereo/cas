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

    /** A pattern match that does not match anything. */
    public static final Pattern MATCH_NOTHING_PATTERN = Pattern.compile("a^");

    private static final Logger LOGGER = LoggerFactory.getLogger(RegexUtils.class);

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
                Pattern.compile(pattern);
                return true;
            }
        } catch (final PatternSyntaxException exception) {
            LOGGER.debug("Pattern [{}] is not a valid regex.", pattern);
        }
        return false;
    }

    /**
     * Creates the pattern. Matching is by default
     * case insensitive.
     *
     * @param pattern the pattern, may not be null.
     * @return the pattern or or {@link RegexUtils#MATCH_NOTHING_PATTERN}
     * if pattern is null or invalid.
     */
    public static Pattern createPattern(final String pattern) {
        return createPattern(pattern, Pattern.CASE_INSENSITIVE);
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
        return createPattern(pattern, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
    }

    /**
     * Creates the pattern with the given flags.
     *
     * @param pattern the pattern, may be null.
     * @return the compiled pattern or {@link RegexUtils#MATCH_NOTHING_PATTERN}
     * if pattern is null or invalid.
     */
    private static Pattern createPattern(final String pattern, final int flags) {
        if (pattern == null) {
            LOGGER.debug("Pattern [{}] can't be null", pattern);
            return MATCH_NOTHING_PATTERN;
        }
        try {
            return Pattern.compile(pattern, flags);
        } catch (final PatternSyntaxException exception) {
            LOGGER.debug("Pattern [{}] is not a valid regex.", pattern);
            return MATCH_NOTHING_PATTERN;
        }
    }
}
