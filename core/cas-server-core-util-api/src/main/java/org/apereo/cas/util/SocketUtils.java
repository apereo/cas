package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * This is {@link SocketUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public class SocketUtils {
    /**
     * Is tcp port available.
     *
     * @param port the port
     * @return true/false
     */
    public static boolean isTcpPortAvailable(final int port) {
        try (val serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port), 1);
            return true;
        } catch (final Exception ex) {
            return false;
        }
    }
}
