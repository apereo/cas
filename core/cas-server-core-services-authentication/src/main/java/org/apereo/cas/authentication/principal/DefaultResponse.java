package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.EncodingUtils;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Encapsulates a Response to send back for a particular service.
 *
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DefaultResponse implements Response {

    /**
     * Pattern to detect unprintable ASCII characters.
     */
    private static final Pattern NON_PRINTABLE = Pattern.compile("[\\x00-\\x1F\\x7F]+");

    private static final int RESPONSE_INITIAL_CAPACITY = 200;

    private static final long serialVersionUID = -8251042088720603062L;

    private final ResponseType responseType;

    private final String url;

    private final Map<String, String> attributes;

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
        LOGGER.debug("Sanitized URL for redirect response is [{}]", sanitizedUrl);
        val fragmentSplit = Splitter.on("#").splitToList(sanitizedUrl);
        builder.append(fragmentSplit.get(0));
        val params = parameters.entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null)
            .map(entry -> {
                try {
                    return String.join("=", entry.getKey(), EncodingUtils.urlEncode(entry.getValue()));
                } catch (final Exception e) {
                    return String.join("=", entry.getKey(), entry.getValue());
                }
            })
            .collect(Collectors.joining("&"));
        if (!(params == null || params.isEmpty())) {
            builder.append(fragmentSplit.get(0).contains("?") ? "&" : "?");
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
        val sb = new StringBuffer(url.length());
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
