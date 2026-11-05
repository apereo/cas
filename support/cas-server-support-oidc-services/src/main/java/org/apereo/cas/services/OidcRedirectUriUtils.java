package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Translates between the redirect URIs of a client registered by CAS, dynamically or through the federation, and the
 * service id of its registered service.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class OidcRedirectUriUtils {
    private static final String CASE_SENSITIVE = "(?-i)";

    private static final String QUOTE_START = "\\Q";

    private static final String QUOTE_END = "\\E";

    private static final String ESCAPED_QUOTE_END = "\\\\E";

    private static final String SERVICE_ID_PREFIX = "^(";

    private static final String SERVICE_ID_SUFFIX = ")$";

    private static final Pattern AUTHORITY = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://");

    /**
     * The service id of a client registered with the given redirect URIs: a regular expression that matches each of them
     * exactly, case included, and nothing else. Joining the URIs as they are would let {@code .} match any character, so
     * that a look-alike host matches, and {@code ?} make the preceding character optional, so that a URI with a query
     * never matches itself. Service ids are matched ignoring case unless they say otherwise, which this one does.
     *
     * @param redirectUris the redirect URIs
     * @return the service id
     */
    public static String toServiceId(final Collection<String> redirectUris) {
        return redirectUris
            .stream()
            .map(Pattern::quote)
            .collect(Collectors.joining("|", CASE_SENSITIVE + SERVICE_ID_PREFIX, SERVICE_ID_SUFFIX));
    }

    /**
     * The redirect URIs a service id built by {@link #toServiceId(Collection)} matches, including one built before it
     * matched case. Any other service id, such as one defined by hand, is returned as the only redirect URI, as it is.
     *
     * @param serviceId the service id
     * @return the redirect URIs
     */
    public static List<String> toRedirectUris(final String serviceId) {
        val prefix = Strings.CS.startsWith(serviceId, CASE_SENSITIVE) ? CASE_SENSITIVE + SERVICE_ID_PREFIX : SERVICE_ID_PREFIX;
        if (!Strings.CS.startsWith(serviceId, prefix + QUOTE_START) || !Strings.CS.endsWith(serviceId, QUOTE_END + SERVICE_ID_SUFFIX)) {
            return CollectionUtils.wrapList(serviceId);
        }
        val body = serviceId.substring(prefix.length(), serviceId.length() - SERVICE_ID_SUFFIX.length());
        val redirectUris = new ArrayList<String>();
        var current = new StringBuilder();
        var position = 0;
        while (position < body.length()) {
            if (body.charAt(position) == '|') {
                redirectUris.add(current.toString());
                current = new StringBuilder();
                position++;
                continue;
            }
            if (body.startsWith(ESCAPED_QUOTE_END, position)) {
                current.append(QUOTE_END);
                position += ESCAPED_QUOTE_END.length();
                continue;
            }
            val end = body.startsWith(QUOTE_START, position) ? body.indexOf(QUOTE_END, position + QUOTE_START.length()) : -1;
            if (end < 0) {
                return CollectionUtils.wrapList(serviceId);
            }
            current.append(body, position + QUOTE_START.length(), end);
            position = end + QUOTE_END.length();
        }
        redirectUris.add(current.toString());
        return redirectUris;
    }

    /**
     * The host of a redirect URI, as the sector identifier of pairwise subject identifiers is read from it when the client
     * has no sector identifier URI (OpenID Connect Core 1.0, section 8.1). Every check of the sector of a client reads
     * hosts this way, so that the host a check sees is the host identifiers are calculated for.
     *
     * @param redirectUri the redirect URI
     * @return the host, or empty when the redirect URI names none or cannot be read
     */
    public static Optional<String> toHost(final String redirectUri) {
        try {
            return Optional.ofNullable(UriComponentsBuilder.fromUriString(redirectUri).build().getHost()).filter(StringUtils::isNotBlank);
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * The distinct hosts of the redirect URIs of a client, read as {@link #toHost(String)} does. A redirect URI without an
     * authority, such as one with the private-use scheme of a native application, names no host; one with an authority
     * whose host cannot be read is refused, since a check that set it aside would not see the host it may stand for.
     *
     * @param redirectUris the redirect URIs
     * @return the hosts
     * @throws IllegalArgumentException when a redirect URI has an authority without a host that can be read
     */
    public static List<String> toHosts(final Collection<String> redirectUris) {
        return redirectUris
            .stream()
            .map(redirectUri -> toHost(redirectUri).or(() -> {
                if (AUTHORITY.matcher(redirectUri).find()) {
                    throw new IllegalArgumentException("Redirect URI %s has no host that can be read".formatted(redirectUri));
                }
                return Optional.empty();
            }))
            .flatMap(Optional::stream)
            .distinct()
            .toList();
    }
}
