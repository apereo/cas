package org.apereo.cas.ticket.registry.compact;

import module java.base;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Encodes the fields of a compact ticket as a versioned sequence of length-prefixed values,
 * i.e. {@code 1;10:1727000000...}, so field values never need escaping.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class CompactTicketCodec {
    /**
     * Current version of the compact ticket format.
     */
    public static final int VERSION = 1;

    private static final char VERSION_SEPARATOR = ';';

    private static final char LENGTH_SEPARATOR = ':';

    private static final int MAX_LENGTH_DIGITS = 9;

    /**
     * Encode fields, prefixed with the format version.
     *
     * @param fields the fields
     * @return the encoded value
     */
    public static String encode(final List<String> fields) {
        val builder = new StringBuilder().append(VERSION).append(VERSION_SEPARATOR);
        fields.forEach(field -> append(builder, field));
        return builder.toString();
    }

    /**
     * Decode fields encoded by {@link #encode(List)}.
     *
     * @param value the value
     * @return the fields
     */
    public static List<String> decode(final String value) {
        val header = String.valueOf(VERSION) + VERSION_SEPARATOR;
        if (!value.startsWith(header)) {
            throw new IllegalArgumentException("Unsupported compact ticket format");
        }
        return read(value, header.length());
    }

    /**
     * Encode values into a single field.
     *
     * @param values the values
     * @return the encoded field
     */
    public static String encodeValues(final Collection<?> values) {
        val builder = new StringBuilder();
        values.forEach(value -> append(builder, Objects.toString(value, StringUtils.EMPTY)));
        return builder.toString();
    }

    /**
     * Decode a field encoded by {@link #encodeValues(Collection)}.
     *
     * @param value the field
     * @return the values
     */
    public static List<String> decodeValues(final String value) {
        return read(value, 0);
    }

    private static void append(final StringBuilder builder, final @Nullable String value) {
        val field = Objects.toString(value, StringUtils.EMPTY);
        builder.append(field.length()).append(LENGTH_SEPARATOR).append(field);
    }

    private static List<String> read(final String value, final int start) {
        val fields = new ArrayList<String>();
        var position = start;
        while (position < value.length()) {
            val separator = value.indexOf(LENGTH_SEPARATOR, position);
            if (separator <= position || separator - position > MAX_LENGTH_DIGITS) {
                throw new IllegalArgumentException("Invalid compact ticket field length");
            }
            var length = 0;
            for (var index = position; index < separator; index++) {
                val digit = value.charAt(index);
                if (digit < '0' || digit > '9') {
                    throw new IllegalArgumentException("Invalid compact ticket field length");
                }
                length = length * 10 + (digit - '0');
            }
            val end = separator + 1 + length;
            if (end > value.length()) {
                throw new IllegalArgumentException("Truncated compact ticket field");
            }
            fields.add(value.substring(separator + 1, end));
            position = end;
        }
        return fields;
    }
}
