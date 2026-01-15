package org.apereo.cas.util;
import module java.base;

/**
 * This is {@link ServiceTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ServiceTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {

    public ServiceTicketIdGenerator(final int maxLength,
                                    final String suffix) {
        super(maxLength, suffix);
    }
}
