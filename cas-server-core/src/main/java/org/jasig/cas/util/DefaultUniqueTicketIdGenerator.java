/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link UniqueTicketIdGenerator}. Implementation
 * utilizes a DefaultLongNumericGeneraor and a DefaultRandomStringGenerator to
 * construct the ticket id.
 * <p>
 * Tickets are of the form [PREFIX]-[SEQUENCE NUMBER]-[RANDOM STRING]-[SUFFIX]
 * </p>
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class DefaultUniqueTicketIdGenerator implements UniqueTicketIdGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUniqueTicketIdGenerator.class);

    /** The numeric generator to generate the static part of the id. */
    private final NumericGenerator numericGenerator;

    /** The RandomStringGenerator to generate the secure random part of the id. */
    private final RandomStringGenerator randomStringGenerator;

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
        this(null);
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
     * Creates an instance of DefaultUniqueTicketIdGenerator with default values
     * including a {@link DefaultLongNumericGenerator} with a starting value of
     * 1.
     *
     * @param suffix the value to append at the end of the unique id to ensure
     * uniqueness across JVMs.
     */
    public DefaultUniqueTicketIdGenerator(final String suffix) {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new DefaultRandomStringGenerator();
        prepareTicketSuffix(suffix);
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
    public DefaultUniqueTicketIdGenerator(final int maxLength,
        final String suffix) {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new DefaultRandomStringGenerator(maxLength);
        prepareTicketSuffix(suffix);
    }

    public String getNewTicketId(final String prefix) {
        final String number = this.numericGenerator.getNextNumberAsString();
        final StringBuilder buffer = new StringBuilder(prefix.length() + 2
            + (this.suffix != null ? this.suffix.length() : 0) + this.randomStringGenerator.getMaxLength()
            + number.length());

        buffer.append(prefix);
        buffer.append("-");
        buffer.append(number);
        buffer.append("-");
        buffer.append(this.randomStringGenerator.getNewString());

        if (this.suffix != null) {
            buffer.append(this.suffix);
        }

        return buffer.toString();
    }

    private void prepareTicketSuffix(final String suffix) {
        this.suffix = null;

        if (StringUtils.isEmpty(suffix)) {
            try {
                this.suffix = InetAddress.getLocalHost().getCanonicalHostName();
                LOGGER.debug("Automatically determined ticket suffix to be [{}].", this.suffix);
            } catch (final UnknownHostException e) {
                LOGGER.debug("Host name could not be determined automatically for the ticket suffix.", e);
            }
        } else {
            this.suffix = "-" + suffix;
        }
    }
}
