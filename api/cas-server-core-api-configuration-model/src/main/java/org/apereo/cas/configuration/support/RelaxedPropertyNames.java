package org.apereo.cas.configuration.support;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This is {@link RelaxedPropertyNames}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RelaxedPropertyNames implements Iterable<String> {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([^A-Z-])([A-Z])");

    private static final Pattern SEPARATED_TO_CAMEL_CASE_PATTERN = Pattern.compile("[_\\-.]");

    private final String name;

    private final Set<String> values = new LinkedHashSet<>(0);

    public RelaxedPropertyNames(final String name) {
        this.name = StringUtils.defaultString(name);
        initialize(RelaxedPropertyNames.this.name, this.values);
    }

    /**
     * Return a relaxed name for the given source camelCase source name.
     *
     * @param name the source name in camelCase
     * @return the relaxed names
     */
    public static RelaxedPropertyNames forCamelCase(final String name) {
        final var result = new StringBuilder();
        for (final var c : name.toCharArray()) {
            result.append(Character.isUpperCase(c) && result.length() > 0
                && result.charAt(result.length() - 1) != '-'
                ? "-" + Character.toLowerCase(c) : c);
        }
        return new RelaxedPropertyNames(result.toString());
    }

    public Set<String> getValues() {
        return values;
    }

    @Override
    public Iterator<String> iterator() {
        return this.values.iterator();
    }

    private static void initialize(final String name, final Set<String> values) {
        if (values.contains(name)) {
            return;
        }
        for (final var variation : Variation.values()) {
            for (final var manipulation : Manipulation.values()) {
                var result = name;
                result = manipulation.apply(result);
                result = variation.apply(result);
                values.add(result);
                initialize(result, values);
            }
        }
    }

    /**
     * Name variations.
     */
    enum Variation {

        NONE {
            @Override
            public String apply(final String value) {
                return value;
            }

        },

        LOWERCASE {
            @Override
            public String apply(final String value) {
                return value.isEmpty() ? value : value.toLowerCase();
            }

        },

        UPPERCASE {
            @Override
            public String apply(final String value) {
                return value.isEmpty() ? value : value.toUpperCase();
            }

        };

        public abstract String apply(String value);

    }

    /**
     * Name manipulations.
     */
    enum Manipulation {

        NONE {
            @Override
            public String apply(final String value) {
                return value;
            }

        },

        HYPHEN_TO_UNDERSCORE {
            @Override
            public String apply(final String value) {
                return value.indexOf('-') != -1 ? value.replace('-', '_') : value;
            }

        },

        UNDERSCORE_TO_PERIOD {
            @Override
            public String apply(final String value) {
                return value.indexOf('_') != -1 ? value.replace('_', '.') : value;
            }

        },

        PERIOD_TO_UNDERSCORE {
            @Override
            public String apply(final String value) {
                return value.indexOf('.') != -1 ? value.replace('.', '_') : value;
            }

        },

        CAMELCASE_TO_UNDERSCORE {
            @Override
            public String apply(final String value) {
                if (value.isEmpty()) {
                    return value;
                }
                var matcher = CAMEL_CASE_PATTERN.matcher(value);
                if (!matcher.find()) {
                    return value;
                }
                matcher = matcher.reset();
                final var result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, matcher.group(1) + '_'
                        + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }

        },

        CAMELCASE_TO_HYPHEN {
            @Override
            public String apply(final String value) {
                if (value.isEmpty()) {
                    return value;
                }
                var matcher = CAMEL_CASE_PATTERN.matcher(value);
                if (!matcher.find()) {
                    return value;
                }
                matcher = matcher.reset();
                final var result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, matcher.group(1) + '-'
                        + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }

        },

        SEPARATED_TO_CAMELCASE {
            @Override
            public String apply(final String value) {
                return separatedToCamelCase(value, false);
            }

        },

        CASE_INSENSITIVE_SEPARATED_TO_CAMELCASE {
            @Override
            public String apply(final String value) {
                return separatedToCamelCase(value, true);
            }

        };

        private static final char[] SUFFIXES = new char[]{'_', '-', '.'};

        private static String separatedToCamelCase(final String value, final boolean caseInsensitive) {
            if (value.isEmpty()) {
                return value;
            }
            final var builder = new StringBuilder();
            for (final var field : SEPARATED_TO_CAMEL_CASE_PATTERN.split(value)) {
                final var fieldCased = caseInsensitive ? field.toLowerCase() : field;
                builder.append(builder.length() == 0 ? field : StringUtils.capitalize(fieldCased));
            }
            final var lastChar = value.charAt(value.length() - 1);
            for (final var suffix : SUFFIXES) {
                if (lastChar == suffix) {
                    builder.append(suffix);
                    break;
                }
            }
            return builder.toString();
        }

        public abstract String apply(String value);

    }

}
