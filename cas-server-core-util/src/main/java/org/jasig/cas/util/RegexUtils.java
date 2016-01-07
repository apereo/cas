package org.jasig.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
            LOGGER.debug("Pattern {} is a valid regex.", Pattern.compile(pattern).pattern());
            return true;
        } catch (final PatternSyntaxException exception) {
            return false;
        }
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
