package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RegexUtils;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Encapsulates a Response to send back for a particular service.
 *
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Slf4j
public record DefaultResponse(ResponseType responseType, String url, Map<String, String> attributes) implements Response {

    /**
     * Pattern to detect unprintable ASCII characters.
     */
    private static final Pattern NON_PRINTABLE = RegexUtils.createPattern("[\\x00-\\x1F\\x7F]+");

    private static final int RESPONSE_INITIAL_CAPACITY = 200;

    @Serial
    private static final long serialVersionUID = -8251042088720603062L;

    /**
     * Gets the post response.
     *
     * @param url        the url
     * @param attributes the attributes
     * @return the post response
     */
    public static Response getPostResponse(final String url, final Map<String, String> attributes) {
        return new DefaultResponse(ResponseType.POST, url, attributes);
    }

    /**
     * Gets header response.
     *
     * @param url        the url
     * @param attributes the attributes
     * @return the header response
     */
    public static Response getHeaderResponse(final String url, final Map<String, String> attributes) {
        return new DefaultResponse(ResponseType.HEADER, url, attributes);
    }

    /**
     * Gets the redirect response.
     *
     * @param url        the url
     * @param parameters the parameters
     * @return the redirect response
     */
    public static Response getRedirectResponse(final String url, final Map<String, String> parameters) {
        val builder = new StringBuilder(parameters.size() * RESPONSE_INITIAL_CAPACITY);
        val sanitizedUrl = sanitizeUrl(url);
        LOGGER.trace("Sanitized URL for redirect response is [{}]", sanitizedUrl);
        val fragmentSplit = Splitter.on("#").splitToList(sanitizedUrl);
        builder.append(fragmentSplit.getFirst());
        val params = parameters.entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null)
            .map(entry -> String.join("=", entry.getKey(), EncodingUtils.urlEncode(entry.getValue())))
            .collect(Collectors.joining("&"));

        if (!params.isEmpty()) {
            builder.append(fragmentSplit.getFirst().contains("?") ? "&" : "?");
            builder.append(params);
        }
        if (fragmentSplit.size() > 1) {
            builder.append('#');
            builder.append(fragmentSplit.get(1));
        }
        val urlRedirect = builder.toString();
        LOGGER.debug("Final redirect response is [{}]", urlRedirect);
        return new DefaultResponse(ResponseType.REDIRECT, urlRedirect, parameters);
    }

    /**
     * Sanitize a URL provided by a relying party by normalizing non-printable
     * ASCII character sequences into spaces.  This functionality protects
     * against CRLF attacks and other similar attacks using invisible characters
     * that could be abused to trick user agents.
     *
     * @param url URL to sanitize.
     * @return Sanitized URL string.
     */
    private static String sanitizeUrl(final String url) {
        val m = NON_PRINTABLE.matcher(url);
        val sb = new StringBuilder(url.length());
        var hasNonPrintable = false;
        while (m.find()) {
            m.appendReplacement(sb, " ");
            hasNonPrintable = true;
        }
        m.appendTail(sb);
        if (hasNonPrintable) {
            LOGGER.warn("The following redirect URL has been sanitized and may be sign of attack:\n[{}]", url);
        }
        return sb.toString();
    }
}
