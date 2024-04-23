package org.apereo.cas.util;

import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.apereo.cas.util.gen.DefaultLongNumericGenerator;
import org.apereo.cas.util.gen.NumericGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;

import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Default implementation of {@link UniqueTicketIdGenerator}. Implementation
 * utilizes a {@link DefaultLongNumericGenerator} and a {@link org.apereo.cas.util.gen.DefaultRandomStringGenerator} to
 * construct the ticket id.
 * <p>
 * Tickets are of the form [PREFIX]-[SEQUENCE NUMBER]-[RANDOM STRING]-[SUFFIX]
 * </p>
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Setter
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

    public DefaultUniqueTicketIdGenerator() {
        this(TICKET_SIZE);
    }

    public DefaultUniqueTicketIdGenerator(final long maxLength) {
        this(maxLength, null);
    }

    public DefaultUniqueTicketIdGenerator(final long maxLength, final String suffix) {
        setMaxLength(maxLength);
        setSuffix(suffix);
    }

    public DefaultUniqueTicketIdGenerator(final NumericGenerator numericGenerator,
                                          final RandomStringGenerator randomStringGenerator,
                                          final String suffix) {
        this.randomStringGenerator = randomStringGenerator;
        this.numericGenerator = numericGenerator;
        setSuffix(suffix);
    }

    /**
     * Due to a bug in mod-auth-cas and possibly other clients in the way tickets are parsed,
     * the ticket id body is sanitized to remove the character "_", replacing it with "-" instead.
     * This might be revisited in the future and removed, once at least mod-auth-cas fixes
     * the issue.
     *
     * @param prefix The prefix we want attached to the ticket.
     * @return the ticket id
     */
    @Override
    public String getNewTicketId(final String prefix) {
        val number = this.numericGenerator.getNextNumberAsString();
        val ticketBody = this.randomStringGenerator.getNewString().replace('_', SEPARATOR);
        val origSuffix = StringUtils.defaultString(this.suffix);
        val finalizedSuffix = StringUtils.isEmpty(origSuffix) ? origSuffix : SEPARATOR + origSuffix;
        return prefix + SEPARATOR + number + SEPARATOR + ticketBody + finalizedSuffix;
    }

    /**
     * Sets max length of id generation.
     *
     * @param maxLength the max length
     */
    public void setMaxLength(final long maxLength) {
        this.randomStringGenerator = new Base64RandomStringGenerator(maxLength);
        this.numericGenerator = new DefaultLongNumericGenerator(1);
    }
}
