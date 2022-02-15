package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jooq.lambda.Unchecked;

import java.net.InetAddress;
import java.net.URL;

/**
 * This is {@link InetAddressUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
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
            val url = new URL(urlAddr);
            return InetAddress.getByName(url.getHost());
        } catch (final Exception e) {
            System.out.println("Host name could not be determined automatically! - in InetAddressUtils.getByName cause:" + e);
        }
        return null;
    }


    /**
     * Gets cas server host name.
     *
     * @return the cas server host name
     */
    public static String getCasServerHostName() {
        return Unchecked.supplier(() -> {
            try {
                val hostName = InetAddress.getLocalHost().getHostName();
                val index = hostName.indexOf('.');
                if (index > 0) {
                    return hostName.substring(0, index);
                }
                return hostName;
            } catch(java.net.UnknownHostException ex) {
                System.out.println("InetAddressUtils.getCasServerHostName failed!cause:" + ex);
                return "unknown";
            }
        }).get();
    }

    /**
     * Gets cas server host address.
     *
     * @param name the name
     * @return the cas server host address
     */
    public static String getCasServerHostAddress(final String name) {
        return Unchecked.supplier(() -> {
            val host = getByName(name);
            
            if (host != null) {
                return host.getHostAddress();
            }
            return null;
        }).get();
    }
}
