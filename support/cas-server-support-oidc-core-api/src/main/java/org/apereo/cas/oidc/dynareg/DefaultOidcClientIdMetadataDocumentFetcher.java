package org.apereo.cas.oidc.dynareg;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import com.google.common.net.InetAddresses;
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
import java.net.Proxy;

/**
 * This is {@link DefaultOidcClientIdMetadataDocumentFetcher}. It fetches client ID metadata documents with the
 * protections the specification asks for (draft-ietf-oauth-client-id-metadata-document, section 6): the host is resolved
 * once and every address it resolves to must be public, the connection is made to those addresses only, so a host whose
 * name later resolves elsewhere cannot redirect the request, redirects are not followed, only {@code 200} responses are
 * accepted, the body must be served as JSON and be at most {@value #MAXIMUM_DOCUMENT_SIZE} bytes, unless another limit is
 * given, and the request has a timeout. The same protections apply to other documents a client points CAS to, such as
 * its sector identifier document, which need not be served as JSON, since such files are often hosted as plain text.
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

    private static final int IPV4_EMBEDDING_PREFIX_LENGTH = 96;

    private static final byte[] IPV4_MAPPED_PREFIX = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, 0, 0, 0, 0};

    private static final List<AddressBlock> IPV4_EMBEDDING_IPV6 = Stream.concat(AddressBlock.of("::/96", "64:ff9b::/96").stream(),
        Stream.of(new AddressBlock(IPV4_MAPPED_PREFIX, IPV4_EMBEDDING_PREFIX_LENGTH))).toList();

    private static final Pattern MAX_AGE = Pattern.compile("(?:^|,)\\s*max-age\\s*=\\s*\"?(\\d+)\"?", Pattern.CASE_INSENSITIVE);

    private final HttpClient httpClient;

    private final Duration timeout;

    private final Predicate<InetAddress> addressPolicy;

    private final Function<String, InetAddress[]> hostResolver;

    private final int maximumDocumentSize;

    private final boolean jsonContentTypeRequired;

    public DefaultOidcClientIdMetadataDocumentFetcher(final HttpClient httpClient, final Duration timeout) {
        this(httpClient, timeout, MAXIMUM_DOCUMENT_SIZE, true);
    }

    public DefaultOidcClientIdMetadataDocumentFetcher(final HttpClient httpClient, final Duration timeout,
                                                      final int maximumDocumentSize, final boolean jsonContentTypeRequired) {
        this(httpClient, timeout, DefaultOidcClientIdMetadataDocumentFetcher::isPublicAddress,
            host -> {
                try {
                    return InetAddress.getAllByName(host);
                } catch (final UnknownHostException e) {
                    return new InetAddress[0];
                }
            }, maximumDocumentSize, jsonContentTypeRequired);
    }

    public DefaultOidcClientIdMetadataDocumentFetcher(final HttpClient httpClient, final Duration timeout,
                                                      final Predicate<InetAddress> addressPolicy,
                                                      final Function<String, InetAddress[]> hostResolver) {
        this(httpClient, timeout, addressPolicy, hostResolver, MAXIMUM_DOCUMENT_SIZE, true);
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
            .maximumResponseSize(maximumDocumentSize)
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
            if (jsonContentTypeRequired && (contentType == null || !isJson(contentType.getValue()))) {
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
     * including IPv4 addresses embedded in IPv4-mapped, IPv4-compatible and NAT64 IPv6 addresses.
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

    /**
     * Whether an address may be contacted where addresses of private and loopback networks are allowed: link-local,
     * unspecified and multicast addresses are refused, such as the metadata service of a cloud provider, including IPv4
     * addresses embedded in IPv4-mapped, IPv4-compatible and NAT64 IPv6 addresses.
     *
     * @param address the address
     * @return true or false
     */
    public static boolean isAllowedWithPrivateNetworks(final InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        if (address instanceof Inet4Address) {
            return true;
        }
        return IPV4_EMBEDDING_IPV6.stream()
            .filter(block -> block.contains(address))
            .findFirst()
            .map(_ -> FunctionUtils.doUnchecked(() -> InetAddress.getByAddress(
                Arrays.copyOfRange(address.getAddress(), IPV6_LENGTH - IPV4_LENGTH, IPV6_LENGTH))))
            .map(DefaultOidcClientIdMetadataDocumentFetcher::isAllowedWithPrivateNetworks)
            .orElse(Boolean.TRUE);
    }

    /**
     * Whether a request may be sent to the host of the URL where addresses of private and loopback networks are allowed:
     * none of the addresses the host resolves to may be refused by {@link #isAllowedWithPrivateNetworks(InetAddress)}. A
     * host written as an address is always checked. A host name sent through an HTTP proxy of the JVM, which the proxy
     * selector names with an address, is resolved by the proxy, which then decides where it may go, so it is not resolved
     * here; one sent through a SOCKS proxy is resolved by the JVM before it connects, so it is checked as for a direct
     * connection, unless the JVM cannot resolve it and leaves it to the proxy.
     *
     * @param url the URL
     * @return true or false
     * @throws IOException when the host cannot be resolved
     */
    public static boolean isAllowedWithPrivateNetworks(final URL url) throws IOException {
        return isAllowedWithPrivateNetworks(url, ProxySelector.getDefault());
    }

    static boolean isAllowedWithPrivateNetworks(final URL url, @Nullable final ProxySelector proxySelector) throws IOException {
        if (InetAddresses.isUriInetAddress(url.getHost())) {
            return isAllowedWithPrivateNetworks(InetAddresses.forUriString(url.getHost()));
        }
        val proxies = selectProxies(url, proxySelector);
        if (proxies.stream().anyMatch(proxy -> proxy.type() == Proxy.Type.HTTP && proxy.address() != null)) {
            return true;
        }
        try {
            return Arrays.stream(InetAddress.getAllByName(url.getHost())).allMatch(DefaultOidcClientIdMetadataDocumentFetcher::isAllowedWithPrivateNetworks);
        } catch (final UnknownHostException e) {
            if (proxies.stream().anyMatch(proxy -> proxy.type() == Proxy.Type.SOCKS && proxy.address() != null)) {
                return true;
            }
            throw e;
        }
    }

    private static List<Proxy> selectProxies(final URL url, @Nullable final ProxySelector proxySelector) {
        return FunctionUtils.doAndHandle(() -> {
            val uri = url.toURI();
            return Optional.ofNullable(proxySelector)
                .map(selector -> selector.select(uri))
                .orElseGet(List::of);
        }, _ -> List.<Proxy>of()).get();
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
