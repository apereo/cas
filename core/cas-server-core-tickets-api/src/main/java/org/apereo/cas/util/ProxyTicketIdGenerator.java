package org.apereo.cas.util;
import module java.base;

/**
 * This is {@link ProxyTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ProxyTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {
    public ProxyTicketIdGenerator(final long maxLength,
                                  final String suffix) {
        super(maxLength, suffix);
    }
}
