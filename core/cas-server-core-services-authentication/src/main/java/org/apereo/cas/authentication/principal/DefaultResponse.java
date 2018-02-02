package org.apereo.cas.authentication.principal;

import com.google.common.base.Splitter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.EncodingUtils;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Encapsulates a Response to send back for a particular service.
 *
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Slf4j
@Getter
@AllArgsConstructor
public class DefaultResponse implements Response {

    /**
     * Pattern to detect unprintable ASCII characters.
     */
    private static final Pattern NON_PRINTABLE = Pattern.compile("[\\x00-\\x1F\\x7F]+");

    private static final int CONST_REDIRECT_RESPONSE_MULTIPLIER = 40;

    private static final int CONST_REDIRECT_RESPONSE_BUFFER = 100;

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
        final StringBuilder builder = new StringBuilder(parameters.size() * CONST_REDIRECT_RESPONSE_MULTIPLIER + CONST_REDIRECT_RESPONSE_BUFFER);
        final String sanitizedUrl = sanitizeUrl(url);
        LOGGER.debug("Sanitized URL for redirect response is [{}]", sanitizedUrl);
        final List<String> fragmentSplit = Splitter.on("#").splitToList(sanitizedUrl);
        builder.append(fragmentSplit.get(0));
        final String params = parameters.entrySet().stream().filter(entry -> entry.getValue() != null).map(entry -> {
            String param;
            try {
                param = String.join("=", entry.getKey(), EncodingUtils.urlEncode(entry.getValue()));
            } catch (final Exception e) {
                param = String.join("=", entry.getKey(), entry.getValue());
            }
            return param;
        }).collect(Collectors.joining("&"));
        if (!(params == null || params.isEmpty())) {
            builder.append(url.contains("?") ? "&" : "?");
            builder.append(params);
        }
        if (fragmentSplit.size() > 1) {
            builder.append('#');
            builder.append(fragmentSplit.get(1));
        }
        final String urlRedirect = builder.toString();
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
        final Matcher m = NON_PRINTABLE.matcher(url);
        final StringBuffer sb = new StringBuffer(url.length());
        boolean hasNonPrintable = false;
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
