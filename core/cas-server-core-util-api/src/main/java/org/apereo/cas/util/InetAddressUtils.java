package org.apereo.cas.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.URL;

/**
 * This is {@link InetAddressUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class InetAddressUtils {

    /**
     * Gets by name.
     *
     * @param urlAddr the host
     * @return the by name
     */
    public static InetAddress getByName(final String urlAddr) {
        try {
            final URL url = new URL(urlAddr);
            return InetAddress.getByName(url.getHost());
        } catch (final Exception e) {
            LOGGER.debug("Host name could not be determined automatically.", e);
        }
        return null;
    }


    /**
     * Gets cas server host name.
     *
     * @return the cas server host name
     */
    @SneakyThrows
    public static String getCasServerHostName() {
        final String hostName = InetAddress.getLocalHost().getHostName();
        final int index = hostName.indexOf('.');
        if (index > 0) {
            return hostName.substring(0, index);
        }
        return hostName;
    }
}
