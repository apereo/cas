package org.jasig.cas.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An implementation of {@link org.jasig.cas.ticket.UniqueTicketIdGenerator} that is able auto-configure
 * the suffix based on the underlying host name.
 *
 * <p>In order to assist with multi-node deployments, in scenarios where CAS configuration
 * and specially {@code cas.properties} file is externalized, it would be ideal to simply just have one set
 * of configuration files for all nodes, such that there would for instance be one {@code cas.properties} file
 * for all nodes. This would remove the need to copy/sync config files over across nodes, again in a
 * situation where they are externalized.
 * <p>The drawback is that in keeping only one {@code cas.properties} file, we'd lose the ability
 * to define unique {@code host.name} property values for each node as the suffix, which would assist with troubleshooting
 * and diagnostics. To provide a remedy, this ticket generator is able to retrieve the {@code host.name} value directly from
 * the actual node name, rather than relying on the configuration, only if one isn't specified in
 * the {@code cas.properties} file. </p>
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class HostNameBasedUniqueTicketIdGenerator extends DefaultUniqueTicketIdGenerator {
    /**
     * Instantiates a new Host name based unique ticket id generator.
     *
     * @param maxLength the max length
     */
    public HostNameBasedUniqueTicketIdGenerator(final int maxLength) {
        super(maxLength, determineTicketSuffixByHostName());
    }

    /**
     * Appends the first part of the host name to the ticket id,
     * so as to moderately provide a relevant unique value mapped to
     * the host name AND not auto-leak infrastructure data out into the configuration and logs.
     * <ul>
     * <li>If the CAS node name is {@code cas-01.sso.edu} then, the suffix
     * determined would just be {@code cas-01}</li>
     * <li>If the CAS node name is {@code cas-01} then, the suffix
     * determined would just be {@code cas-01}</li>
     * </ul>
     * @return the shortened ticket suffix based on the hostname
     * @since 4.1.0
     */
    private static String determineTicketSuffixByHostName() {
        try {
            final String hostName = InetAddress.getLocalHost().getCanonicalHostName();
            final int index = hostName.indexOf('.');
            if (index > 0) {
                return hostName.substring(0, index);
            }
            return hostName;
        } catch (final UnknownHostException e) {
            throw new RuntimeException("Host name could not be determined automatically for the ticket suffix.", e);
        }
    }
}
