package org.apereo.cas.util;

/**
 * This is {@link ProxyGrantingTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ProxyGrantingTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {

    public ProxyGrantingTicketIdGenerator(final int maxLength, final String suffix) {
        super(maxLength, suffix);
    }
}
