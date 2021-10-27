/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
 * @since 4.1.4
 */
public final class RegexUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegexUtils.class);

    /**
     * Instantiates a new Regex utils.
     */
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
