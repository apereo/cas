package org.apereo.cas.util;

import module java.base;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * This is {@link SocketUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public class SocketUtils {
    private static final int CONNECT_TIMEOUT_MILLIS = 2_000;

    /**
     * Is tcp port available.
     *
     * @param port the port
     * @return true/false
     */
    public static boolean isTcpPortAvailable(final int port) {
        try (val socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByName("localhost"), port), CONNECT_TIMEOUT_MILLIS);
            return false;
        } catch (final Exception ex) {
            return true;
        }
    }
}
