package org.apereo.cas.util;

import java.net.InetAddress;

/**
 * This is {@link InetAddressUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class InetAddressUtils {
    private InetAddressUtils() {
    }

    /**
     * Gets cas server host name.
     *
     * @return the cas server host name
     */
    public static String getCasServerHostName() {
        try {
            final String hostName = InetAddress.getLocalHost().getHostName();
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
