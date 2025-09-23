package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import java.net.InetAddress;
import java.net.URI;
import java.util.Optional;

/**
 * This is {@link InetAddressUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class InetAddressUtils {
    private static final String CAS_SERVER_HOST_NAME;

    static {
        CAS_SERVER_HOST_NAME = FunctionUtils.doAndHandle(() -> {
            val hostName = InetAddress.getLocalHost().getHostName();
            val index = hostName.indexOf('.');
            return index > 0 ? hostName.substring(0, index) : hostName;
        }, throwable -> "unknown").get();
    }

    /**
     * Gets by name.
     *
     * @param urlAddr the host
     * @return the by name
     */
    public static InetAddress getByName(final String urlAddr) {
        return FunctionUtils.doAndHandle(() -> {
            val url = new URI(urlAddr).toURL();
            return InetAddress.getAllByName(url.getHost())[0];
        }, e -> {
            LOGGER.trace("Host name could not be determined automatically.", e);
            return null;
        }).get();
    }


    /**
     * Gets cas server host name.
     *
     * @return the cas server host name
     */
    public static String getCasServerHostName() {
        return CAS_SERVER_HOST_NAME;
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
            return Optional.ofNullable(host).map(InetAddress::getHostAddress).orElse(null);
        }).get();
    }
}
