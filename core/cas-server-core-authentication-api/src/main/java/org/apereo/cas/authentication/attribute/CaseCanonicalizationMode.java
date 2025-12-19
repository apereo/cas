package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apache.commons.lang3.StringUtils;

/**
 * The enum Case canonicalization mode.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public enum CaseCanonicalizationMode {

    /**
     * Lower case  canonicalization mode.
     */
    LOWER {
        @Override
        public String canonicalize(final String value) {
            return StringUtils.lowerCase(value);
        }

        @Override
        public String canonicalize(final String value, final Locale locale) {
            return StringUtils.lowerCase(value, locale);
        }
    },
    /**
     * uppercase canonicalization.
     */
    UPPER {
        @Override
        public String canonicalize(final String value) {
            return StringUtils.upperCase(value);
        }

        @Override
        public String canonicalize(final String value, final Locale locale) {
            return StringUtils.upperCase(value, locale);
        }
    },
    /**
     * Disabled.
     */
    NONE {
        @Override
        public String canonicalize(final String value) {
            return value;
        }

        @Override
        public String canonicalize(final String value, final Locale locale) {
            return value;
        }
    };

    /**
     * Canonicalize string.
     *
     * @param value the value
     * @return the string
     */
    public abstract String canonicalize(String value);

    /**
     * Canonicalize string.
     *
     * @param value  the value
     * @param locale the locale
     * @return the string
     */
    public abstract String canonicalize(String value, Locale locale);
}
