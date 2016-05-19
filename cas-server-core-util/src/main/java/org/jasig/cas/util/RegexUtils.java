package org.jasig.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Utility class to assist with resource operations.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class RegexUtils {
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
            if (pattern == null) {
                throw new IllegalArgumentException("Pattern cannot be null");
            }
            LOGGER.debug("Pattern {} is a valid regex.", Pattern.compile(pattern).pattern());
            return true;
        } catch (final Exception exception) {
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
        if (isValidRegex(pattern)) {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        }
        return null;
    }
    
    /**
     * Concatenate all elements in the given collection to form a regex pattern.
     *
     * @param requiredValues  the required values
     * @param caseInsensitive the case insensitive
     * @return the pattern
     */
    public static Pattern concatenate(final Collection<String> requiredValues, final boolean caseInsensitive) {
        final StringBuilder builder = new StringBuilder(requiredValues.size());
        for (final String requiredValue : requiredValues) {
            builder.append('(').append(requiredValue).append(")|");
        }
        final String pattern = StringUtils.removeEnd(builder.toString(), "|");
        if (isValidRegex(pattern)) {
            return Pattern.compile(pattern, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
        }
        return null;
    }
}
