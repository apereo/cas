package org.apereo.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link UniqueTicketIdGenerator} that is able auto-configure
 * the suffix based on the underlying host name.
 * <p>In order to assist with multi-node deployments, in scenarios where CAS configuration
 * and specially {@code application.properties} file is externalized, it would be ideal to simply just have one set
 * of configuration files for all nodes, such that there would for instance be one {@code application.properties} file
 * for all nodes. This would remove the need to copy/sync config files over across nodes, again in a
 * situation where they are externalized.
 * <p>The drawback is that in keeping only one {@code application.properties} file, we'd lose the ability
 * to define unique {@code host.name} property values for each node as the suffix, which would assist with troubleshooting
 * and diagnostics. To provide a remedy, this ticket generator is able to retrieve the {@code host.name} value directly from
 * the actual node name, rather than relying on the configuration, only if one isn't specified in
 * the {@code application.properties} file. </p>
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class HostNameBasedUniqueTicketIdGenerator extends DefaultUniqueTicketIdGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostNameBasedUniqueTicketIdGenerator.class);

    /**
     * Instantiates a new Host name based unique ticket id generator.
     *
     * @param maxLength the max length
     * @param suffix    the suffix
     */
    public HostNameBasedUniqueTicketIdGenerator(final int maxLength, final String suffix) {
        super(maxLength, determineTicketSuffixByHostName(suffix));
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
     *
     * @param suffix the suffix
     * @return the shortened ticket suffix based on the hostname
     * @since 4.1.0
     */
    private static String determineTicketSuffixByHostName(final String suffix) {
        if (StringUtils.isNotBlank(suffix)) {
            return suffix;
        }
        return InetAddressUtils.getCasServerHostName();
    }

    /**
     * The type Ticket granting ticket id generator.
     */
    public static class TicketGrantingTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {

        /**
         * Instantiates a new Ticket granting ticket id generator.
         *
         * @param maxLength the max length
         * @param suffix    the suffix
         */
        public TicketGrantingTicketIdGenerator(final int maxLength,
                                               final String suffix) {
            super(maxLength, suffix);
        }
    }

    /**
     * The type Service ticket id generator.
     */
    public static class ServiceTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {

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

    /**
     * The type Proxy ticket id generator.
     */
    public static class ProxyTicketIdGenerator extends HostNameBasedUniqueTicketIdGenerator {
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

}
