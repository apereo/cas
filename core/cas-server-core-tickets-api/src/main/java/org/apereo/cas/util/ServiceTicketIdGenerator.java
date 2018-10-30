package org.apereo.cas.util;

/**
 * This is {@link ServiceTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ServiceTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {

    /**
     * Instantiates a new Service ticket id generator.
     *
     * @param maxLength the max length
     * @param suffix    the suffix
     */
    public ServiceTicketIdGenerator(final int maxLength,
                                    final String suffix) {
        super(maxLength, suffix);
    }
}
