package org.apereo.cas.oidc.dynareg;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import com.google.common.primitives.Longs;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * This is {@link DefaultOidcClientIdMetadataDocumentFetcher}. It fetches client ID metadata documents with the
 * protections the specification asks for (draft-ietf-oauth-client-id-metadata-document, section 6): the host is resolved
 * once and every address it resolves to must be public, the connection is made to those addresses only, so a host whose
 * name later resolves elsewhere cannot redirect the request, redirects are not followed, only {@code 200} responses are
 * accepted, the body must be JSON and at most {@value #MAXIMUM_DOCUMENT_SIZE} bytes, and the request has a timeout.
 * TLS follows the CAS HTTP client, so its trust store settings apply. An outbound HTTP proxy configured through system
 * properties resolves the host itself, which bypasses the address check; do not route these requests through one.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class DefaultOidcClientIdMetadataDocumentFetcher implements OidcClientIdMetadataDocumentFetcher {
    /**
     * The largest document read, the limit the specification recommends.
     */
    public static final int MAXIMUM_DOCUMENT_SIZE = 5 * 1024;

    private static final int IPV4_LENGTH = 4;

    private static final int IPV6_LENGTH = 16;

    private static final List<AddressBlock> NON_PUBLIC_IPV4 = AddressBlock.of("0.0.0.0/8", "10.0.0.0/8", "100.64.0.0/10",
        "127.0.0.0/8", "169.254.0.0/16", "172.16.0.0/12", "192.0.0.0/24", "192.0.2.0/24", "192.168.0.0/16", "198.18.0.0/15",
        "198.51.100.0/24", "203.0.113.0/24", "224.0.0.0/4", "240.0.0.0/4");

    private static final List<AddressBlock> NON_PUBLIC_IPV6 = AddressBlock.of("fc00::/7", "fe80::/10", "ff00::/8", "2001:db8::/32");

    private static final List<AddressBlock> IPV4_EMBEDDING_IPV6 = AddressBlock.of("::/96", "64:ff9b::/96");

    private static final Pattern MAX_AGE = Pattern.compile("(?:^|,)\\s*max-age\\s*=\\s*\"?(\\d+)\"?", Pattern.CASE_INSENSITIVE);

    private final HttpClient httpClient;

    private final Duration timeout;

    private final Predicate<InetAddress> addressPolicy;

    private final Function<String, InetAddress[]> hostResolver;

    public DefaultOidcClientIdMetadataDocumentFetcher(final HttpClient httpClient, final Duration timeout) {
        this(httpClient, timeout, DefaultOidcClientIdMetadataDocumentFetcher::isPublicAddress,
            host -> {
                try {
                    return InetAddress.getAllByName(host);
                } catch (final UnknownHostException e) {
                    return new InetAddress[0];
                }
            });
    }

    @Override
    public OidcClientIdMetadataDocument fetch(final URI clientId) throws Exception {
        val host = Objects.requireNonNull(clientId.getHost(), "The client id has no host");
        val addresses = hostResolver.apply(host);
        if (addresses.length == 0) {
            throw new IllegalArgumentException("Host %s cannot be resolved".formatted(host));
        }
        if (!Arrays.stream(addresses).allMatch(addressPolicy)) {
            throw new IllegalArgumentException("Host %s resolves to an address that is not public".formatted(host));
        }
        val execution = HttpExecutionRequest.builder()
            .httpClient(httpClient)
            .method(HttpMethod.GET)
            .url(clientId.toString())
            .headers(Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
            .redirectsEnabled(false)
            .maximumRetryAttempts(1)
            .maximumResponseSize(MAXIMUM_DOCUMENT_SIZE)
            .responseTimeout(timeout)
            .resolvedAddresses(Map.of(host, addresses))
            .build();
        HttpResponse response = null;
        try {
            response = HttpUtils.execute(execution);
            if (response == null || response.getCode() != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("Document request answered with status %s"
                    .formatted(response == null ? "none" : response.getCode()));
            }
            val contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
            if (contentType == null || !isJson(contentType.getValue())) {
                throw new IllegalArgumentException("Document is not served as JSON");
            }
            if (!(response instanceof final HttpEntityContainer container) || container.getEntity() == null) {
                throw new IllegalArgumentException("Document is empty");
            }
            try (val content = container.getEntity().getContent()) {
                val json = IOUtils.toString(content, StandardCharsets.UTF_8);
                val cacheControl = response.getFirstHeader(HttpHeaders.CACHE_CONTROL);
                return new OidcClientIdMetadataDocument(clientId.toString(), json,
                    cacheLifetime(cacheControl == null ? null : cacheControl.getValue()));
            }
        } finally {
            HttpUtils.close(response);
        }
    }

    /**
     * Whether an address may be contacted to fetch a document. Unspecified, loopback, link-local, private (site-local,
     * carrier-grade NAT and unique local), multicast, documentation, benchmarking and reserved addresses are refused,
     * including IPv4 addresses embedded in IPv4-compatible and NAT64 IPv6 addresses.
     *
     * @param address the address
     * @return true if the address is public
     */
    public static boolean isPublicAddress(final InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
            || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        if (address instanceof Inet4Address) {
            return NON_PUBLIC_IPV4.stream().noneMatch(block -> block.contains(address));
        }
        if (NON_PUBLIC_IPV6.stream().anyMatch(block -> block.contains(address))) {
            return false;
        }
        return IPV4_EMBEDDING_IPV6.stream()
            .filter(block -> block.contains(address))
            .findFirst()
            .map(_ -> FunctionUtils.doUnchecked(() -> InetAddress.getByAddress(
                Arrays.copyOfRange(address.getAddress(), IPV6_LENGTH - IPV4_LENGTH, IPV6_LENGTH))))
            .map(DefaultOidcClientIdMetadataDocumentFetcher::isPublicAddress)
            .orElse(Boolean.TRUE);
    }

    private static boolean isJson(final String contentType) {
        val mediaType = StringUtils.substringBefore(contentType, ";").trim();
        return Strings.CI.equals(mediaType, MediaType.APPLICATION_JSON_VALUE) || Strings.CI.endsWith(mediaType, "+json");
    }

    private static Duration cacheLifetime(@Nullable final String cacheControl) {
        if (StringUtils.isBlank(cacheControl) || Strings.CI.containsAny(cacheControl, "no-store", "no-cache")) {
            return Duration.ZERO;
        }
        val matcher = MAX_AGE.matcher(cacheControl);
        if (!matcher.find()) {
            return Duration.ZERO;
        }
        val seconds = Longs.tryParse(matcher.group(1));
        return seconds == null ? Duration.ZERO : Duration.ofSeconds(seconds);
    }

    private static final class AddressBlock {
        private final byte[] network;

        private final int prefixLength;

        private AddressBlock(final byte[] network, final int prefixLength) {
            this.network = network.clone();
            this.prefixLength = prefixLength;
        }

        static List<AddressBlock> of(final String... blocks) {
            return Arrays.stream(blocks)
                .map(block -> new AddressBlock(FunctionUtils.doUnchecked(
                    () -> InetAddress.getByName(StringUtils.substringBefore(block, "/")).getAddress()),
                    Integer.parseInt(StringUtils.substringAfter(block, "/"))))
                .toList();
        }

        boolean contains(final InetAddress address) {
            val bytes = address.getAddress();
            if (bytes.length != network.length) {
                return false;
            }
            val fullBytes = prefixLength / Byte.SIZE;
            for (var index = 0; index < fullBytes; index++) {
                if (bytes[index] != network[index]) {
                    return false;
                }
            }
            val remainingBits = prefixLength % Byte.SIZE;
            if (remainingBits == 0) {
                return true;
            }
            val mask = (byte) (-1 << (Byte.SIZE - remainingBits));
            return (bytes[fullBytes] & mask) == (network[fullBytes] & mask);
        }
    }
}
