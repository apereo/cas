package org.apereo.cas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * This is {@link InetAddressUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class InetAddressUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(InetAddressUtils.class);

    private InetAddressUtils() {
    }

    /**
     * Gets cas server host name.
     *
     * @return the cas server host name
     */
    public static String getCasServerHostName() {
        try {
            final String hostName = InetAddress.getLocalHost().getCanonicalHostName();
            final int index = hostName.indexOf('.');
            if (index > 0) {
                return hostName.substring(0, index);
            }
            return hostName;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Host name could not be determined automatically.", e);
        }
    }
}
