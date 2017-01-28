package org.apereo.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.gen.DefaultLongNumericGenerator;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.gen.NumericGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;

/**
 * Default implementation of {@link UniqueTicketIdGenerator}. Implementation
 * utilizes a DefaultLongNumericGeneraor and a DefaultRandomStringGenerator to
 * construct the ticket id.
 * <p>
 * Tickets are of the form [PREFIX]-[SEQUENCE NUMBER]-[RANDOM STRING]-[SUFFIX]
 * </p>
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class DefaultUniqueTicketIdGenerator implements UniqueTicketIdGenerator {
    
    /**
     * The numeric generator to generate the static part of the id.
     */
    private NumericGenerator numericGenerator;

    /**
     * The RandomStringGenerator to generate the secure random part of the id.
     */
    private RandomStringGenerator randomStringGenerator;

    /**
     * Optional suffix to ensure uniqueness across JVMs by specifying unique
     * values.
     */
    private String suffix;
    private int initialCapacity;

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with default values
     * including a {@link DefaultLongNumericGenerator} with a starting value of
     * 1.
     */
    public DefaultUniqueTicketIdGenerator() {
        this(DefaultRandomStringGenerator.DEFAULT_MAX_RANDOM_LENGTH);
    }

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with a specified
     * maximum length for the random portion.
     *
     * @param maxLength the maximum length of the random string used to generate
     *                  the id.
     */
    public DefaultUniqueTicketIdGenerator(final int maxLength) {
        this(maxLength, null);
    }

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with a specified
     * maximum length for the random portion.
     *
     * @param maxLength the maximum length of the random string used to generate
     *                  the id.
     * @param suffix    the value to append at the end of the unique id to ensure
     *                  uniqueness across JVMs.
     */
    public DefaultUniqueTicketIdGenerator(final int maxLength, final String suffix) {
        setMaxLength(maxLength);
        setSuffix(suffix);
    }

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with a specified
     * maximum length for the random portion.
     *
     * @param numericGenerator      the numeric generator
     * @param randomStringGenerator the random string generator
     * @param suffix                the value to append at the end of the unique id to ensure
     *                              uniqueness across JVMs.
     * @since 4.1.0
     */
    public DefaultUniqueTicketIdGenerator(final NumericGenerator numericGenerator,
                                          final RandomStringGenerator randomStringGenerator,
                                          final String suffix) {
        this.randomStringGenerator = randomStringGenerator;
        this.numericGenerator = numericGenerator;
        setSuffix(suffix);
    }

    @Override
    public String getNewTicketId(final String prefix) {
        final String number = this.numericGenerator.getNextNumberAsString();
        final int capacity = prefix.length() + initialCapacity + number.length();
        return new StringBuilder(capacity)
                .append(prefix)
                .append('-')
                .append(number)
                .append('-')
                .append(this.randomStringGenerator.getNewString())
                .append(this.suffix)
                .toString();
    }

    /**
     * Sets suffix.
     *
     * @param suffix the suffix
     */
    public void setSuffix(final String suffix) {
        this.suffix = StringUtils.isNoneBlank(suffix) ? '-' + suffix : StringUtils.EMPTY;
        initialCapacity = 2 + this.suffix.length() + this.randomStringGenerator.getMaxLength();
    }

    /**
     * Sets max length of id generation.
     *
     * @param maxLength the max length
     */
    public void setMaxLength(final int maxLength) {
        this.randomStringGenerator = new DefaultRandomStringGenerator(maxLength);
        this.numericGenerator = new DefaultLongNumericGenerator(1);
    }
}
