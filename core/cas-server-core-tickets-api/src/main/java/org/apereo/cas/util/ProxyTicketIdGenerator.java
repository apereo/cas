package org.apereo.cas.util;

/**
 * This is {@link ProxyTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ProxyTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {
    /**
     * Instantiates a new Proxy ticket id generator.
     *
     * @param maxLength the max length
     * @param suffix    the suffix
     */
    public ProxyTicketIdGenerator(final int maxLength,
                                  final String suffix) {
        super(maxLength, suffix);
    }
}
