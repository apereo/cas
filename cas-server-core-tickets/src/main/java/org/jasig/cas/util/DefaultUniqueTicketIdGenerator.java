package org.jasig.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    /** The logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The numeric generator to generate the static part of the id. */
    private NumericGenerator numericGenerator;

    /** The RandomStringGenerator to generate the secure random part of the id. */
    private RandomStringGenerator randomStringGenerator;

    /**
     * Optional suffix to ensure uniqueness across JVMs by specifying unique
     * values.
     */
    private String suffix;

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
     * the id.
     */
    public DefaultUniqueTicketIdGenerator(final int maxLength) {
        this(maxLength, null);
    }


    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with a specified
     * maximum length for the random portion.
     *
     * @param maxLength the maximum length of the random string used to generate
     * the id.
     * @param suffix the value to append at the end of the unique id to ensure
     * uniqueness across JVMs.
     */
    public DefaultUniqueTicketIdGenerator(final int maxLength, final String suffix) {
        setMaxLength(maxLength);
        setSuffix(suffix);
    }

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with a specified
     * maximum length for the random portion.
     *
     * @param numericGenerator the numeric generator
     * @param randomStringGenerator the random string generator
     * @param suffix the value to append at the end of the unique id to ensure
     * uniqueness across JVMs.
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
    public final String getNewTicketId(final String prefix) {
        final String number = this.numericGenerator.getNextNumberAsString();
        final StringBuilder buffer = new StringBuilder(prefix.length() + 2
            + (StringUtils.isNotBlank(this.suffix) ? this.suffix.length() : 0) + this.randomStringGenerator.getMaxLength()
            + number.length());

        buffer.append(prefix);
        buffer.append('-');
        buffer.append(number);
        buffer.append('-');
        buffer.append(this.randomStringGenerator.getNewString());

        if (this.suffix != null) {
            buffer.append(this.suffix);
        }

        return buffer.toString();
    }

    public void setSuffix(final String suffix) {
        this.suffix = StringUtils.isNoneBlank(suffix) ? '-' + suffix : null;
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

    /**
     * The type Ticket granting ticket id generator.
     */
    @Component("ticketGrantingTicketUniqueIdGenerator")
    public static class TicketGrantingTicketIdGenerator extends DefaultUniqueTicketIdGenerator {
        @Autowired
        @Override
        public void setSuffix(@Value("${host.name:cas01.example.org}") final String suffix) {
            super.setSuffix(suffix);
        }

        @Autowired
        @Override
        public void setMaxLength(@Value("${tgt.ticket.maxlength:50}") final int maxLength) {
            super.setMaxLength(maxLength);
        }
    }

    /**
     * The type Service ticket id generator.
     */
    @Component("serviceTicketUniqueIdGenerator")
    public static class ServiceTicketIdGenerator extends DefaultUniqueTicketIdGenerator {
        @Autowired
        @Override
        public void setSuffix(@Value("${host.name:cas01.example.org}") final String suffix) {
            super.setSuffix(suffix);
        }

        @Autowired
        @Override
        public void setMaxLength(@Value("${st.ticket.maxlength:20}") final int maxLength) {
            super.setMaxLength(maxLength);
        }
    }

    /**
     * The type Login ticket id generator.
     */
    @Component("loginTicketUniqueIdGenerator")
    public static class LoginTicketIdGenerator extends DefaultUniqueTicketIdGenerator {
        @Autowired
        @Override
        public void setSuffix(@Value("${host.name:cas01.example.org}") final String suffix) {
            super.setSuffix(suffix);
        }

        @Autowired
        @Override
        public void setMaxLength(@Value("${lt.ticket.maxlength:20}") final int maxLength) {
            super.setMaxLength(maxLength);
        }
    }

    /**
     * The type Proxy ticket id generator.
     */
    @Component("proxy20TicketUniqueIdGenerator")
    public static class ProxyTicketIdGenerator extends DefaultUniqueTicketIdGenerator {
        @Autowired
        @Override
        public void setSuffix(@Value("${host.name:cas01.example.org}") final String suffix) {
            super.setSuffix(suffix);
        }

        @Autowired
        @Override
        public void setMaxLength(@Value("${pgt.ticket.maxlength:50}") final int maxLength) {
            super.setMaxLength(maxLength);
        }
    }


}
