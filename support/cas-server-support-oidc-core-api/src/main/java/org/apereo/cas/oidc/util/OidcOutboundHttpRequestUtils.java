package org.apereo.cas.oidc.util;

import module java.base;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.HttpMethod;

/**
 * Utilities for safely retrieving attacker-controlled OIDC resources.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class OidcOutboundHttpRequestUtils {
    private static final int MAXIMUM_RESPONSE_SIZE = 64 * 1024;

    /**
     * Determine whether the value is an HTTP URL.
     *
     * @param value the value
     * @return true or false
     */
    public static boolean isHttpUrl(final String value) {
        try {
            val scheme = new URI(value).getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Validate an HTTPS URL and pin its public DNS addresses.
     *
     * @param value the URL
     * @return resolved public addresses keyed by host
     */
    public static Map<String, InetAddress[]> validate(final String value) {
        try {
            val uri = new URI(value);
            var host = uri.getHost();
            if (!uri.isAbsolute() || !"https".equalsIgnoreCase(uri.getScheme()) || StringUtils.isBlank(host)
                || uri.getRawUserInfo() != null || uri.getRawFragment() != null
                || uri.getPort() == 0 || uri.getPort() > 65_535) {
                throw new IllegalArgumentException("OIDC resource URL must be an absolute HTTPS URL");
            }
            if (host.startsWith("[") && host.endsWith("]")) {
                host = host.substring(1, host.length() - 1);
            }
            val addresses = InetAddress.getAllByName(host);
            if (addresses.length == 0 || Arrays.stream(addresses).anyMatch(address -> !isPubliclyRoutable(address))) {
                throw new IllegalArgumentException("OIDC resource URL must resolve to a public address");
            }
            val resolvedAddresses = new LinkedHashMap<String, InetAddress[]>();
            resolvedAddresses.put(host, addresses);
            resolvedAddresses.put(host.toLowerCase(Locale.ENGLISH), addresses);
            val unicodeHost = IDN.toUnicode(host);
            resolvedAddresses.put(unicodeHost, addresses);
            resolvedAddresses.put(unicodeHost.toLowerCase(Locale.ENGLISH), addresses);
            return resolvedAddresses;
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid OIDC resource URL", e);
        }
    }

    /**
     * Retrieve an OIDC resource using a validated, pinned HTTPS connection.
     *
     * @param value the URL
     * @return response body
     * @throws Exception in case retrieval fails
     */
    public static String fetch(final String value) throws Exception {
        HttpResponse response = null;
        try {
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(value)
                .resolvedAddresses(validate(value))
                .redirectsEnabled(false)
                .build()
                .withoutRetry();
            response = HttpUtils.execute(exec);
            if (response == null || response.getCode() != HttpStatus.SC_OK
                || !(response instanceof final HttpEntityContainer container) || container.getEntity() == null) {
                throw new IllegalArgumentException("Unable to retrieve OIDC resource URL");
            }
            try (val content = container.getEntity().getContent()) {
                val resultBytes = content.readNBytes(MAXIMUM_RESPONSE_SIZE + 1);
                if (resultBytes.length > MAXIMUM_RESPONSE_SIZE) {
                    throw new IllegalArgumentException("OIDC resource response is too large");
                }
                return new String(resultBytes, StandardCharsets.UTF_8);
            }
        } finally {
            HttpUtils.close(response);
        }
    }

    private static boolean isPubliclyRoutable(final InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
            || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        val bytes = address.getAddress();
        if (bytes.length == 4) {
            return isPublicIpv4Address(bytes);
        }
        if (bytes.length != 16 || (Byte.toUnsignedInt(bytes[0]) & 0xE0) != 0x20) {
            return false;
        }
        val first = Byte.toUnsignedInt(bytes[0]);
        val second = Byte.toUnsignedInt(bytes[1]);
        val third = Byte.toUnsignedInt(bytes[2]);
        val fourth = Byte.toUnsignedInt(bytes[3]);
        return !(first == 0x20 && second == 0x01 && ((third & 0xFE) == 0 || (third == 0x0D && fourth == 0xB8)))
            && !(first == 0x20 && second == 0x02)
            && !(first == 0x3F && second == 0xFE)
            && !(first == 0x3F && second == 0xFF && (third & 0xF0) == 0);
    }

    private static boolean isPublicIpv4Address(final byte[] bytes) {
        val first = Byte.toUnsignedInt(bytes[0]);
        val second = Byte.toUnsignedInt(bytes[1]);
        val third = Byte.toUnsignedInt(bytes[2]);
        return first != 0 && first != 10 && first != 127
            && !(first == 100 && second >= 64 && second <= 127)
            && !(first == 169 && second == 254)
            && !(first == 172 && second >= 16 && second <= 31)
            && !(first == 192 && second == 0 && third == 0)
            && !(first == 192 && second == 0 && third == 2)
            && !(first == 192 && second == 88 && third == 99)
            && !(first == 192 && second == 168)
            && !(first == 198 && (second == 18 || second == 19))
            && !(first == 198 && second == 51 && third == 100)
            && !(first == 203 && second == 0 && third == 113)
            && first < 224;
    }
}
