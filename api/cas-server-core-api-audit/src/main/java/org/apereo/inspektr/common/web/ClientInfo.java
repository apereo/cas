package org.apereo.inspektr.common.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.jooq.lambda.Unchecked;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Captures information from the {@link HttpServletRequest} to log later.
 *
 * @author Scott Battaglia
 * @since 1.0
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 7492721606084356617L;

    /**
     * IP Address of the client (Remote).
     */
    @JsonProperty("clientIpAddress")
    private String clientIpAddress;

    /**
     * IP Address of the server (local).
     */
    @JsonProperty("serverIpAddress")
    private String serverIpAddress;

    @JsonProperty("geoLocation")
    private String geoLocation;

    @JsonProperty("userAgent")
    private String userAgent;

    @JsonProperty("headers")
    private Map<String, String> headers = new HashMap<>();

    @JsonProperty("extraInfo")
    private Map<String, Serializable> extraInfo = new HashMap<>();

    @JsonProperty("locale")
    private Locale locale;

    public ClientInfo(final String clientIpAddress, final String serverIpAddress,
                      final String userAgent, final String geoLocation) {
        setClientIpAddress(clientIpAddress);
        setServerIpAddress(serverIpAddress);
        setUserAgent(userAgent);
        setGeoLocation(geoLocation);
    }

    /**
     * Gets server ip address.
     *
     * @return the server ip address
     */
    public String getServerIpAddress() {
        return Objects.requireNonNullElse(this.serverIpAddress, "unknown");
    }

    /**
     * Gets client ip address.
     *
     * @return the client ip address
     */
    public String getClientIpAddress() {
        return Objects.requireNonNullElse(this.clientIpAddress, "unknown");
    }

    /**
     * Gets geo location.
     *
     * @return the geo location
     */
    public String getGeoLocation() {
        return Objects.requireNonNullElse(geoLocation, "unknown");
    }

    /**
     * Gets user agent.
     *
     * @return the user agent
     */
    public String getUserAgent() {
        return Objects.requireNonNullElse(userAgent, "unknown");
    }

    /**
     * Gets headers.
     *
     * @return the headers
     */
    public Map<String, Serializable> getHeaders() {
        return Map.copyOf(this.headers);
    }

    /**
     * Gets locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return Objects.requireNonNullElse(locale, Locale.ENGLISH);
    }

    /**
     * Sets headers.
     *
     * @param headers the headers
     * @return the headers
     */
    @CanIgnoreReturnValue
    public ClientInfo setHeaders(final Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Sets server ip address.
     *
     * @param serverIpAddress the server ip address
     * @return the server ip address
     */
    @CanIgnoreReturnValue
    public ClientInfo setServerIpAddress(final String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
        return this;
    }

    /**
     * Sets client ip address.
     *
     * @param clientIpAddress the client ip address
     * @return the client ip address
     */
    @CanIgnoreReturnValue
    public ClientInfo setClientIpAddress(final String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
        return this;
    }

    /**
     * Sets geo location.
     *
     * @param geoLocation the geo location
     * @return the geo location
     */
    @CanIgnoreReturnValue
    public ClientInfo setGeoLocation(final String geoLocation) {
        this.geoLocation = geoLocation;
        return this;
    }

    /**
     * Sets user agent.
     *
     * @param userAgent the user agent
     * @return the user agent
     */
    @CanIgnoreReturnValue
    public ClientInfo setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Sets locale.
     *
     * @param locale the locale
     * @return the locale
     */
    @CanIgnoreReturnValue
    public ClientInfo setLocale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Gets extra info.
     *
     * @return the extra info
     */
    public Map<String, ? extends Serializable> getExtraInfo() {
        return Map.copyOf(extraInfo);
    }

    /**
     * Sets extra info.
     *
     * @param extraInfo the extra info
     */
    @CanIgnoreReturnValue
    public ClientInfo setExtraInfo(final Map<String, Serializable> extraInfo) {
        this.extraInfo = extraInfo;
        return this;
    }

    /**
     * Empty client info.
     *
     * @return the client info
     */
    public static ClientInfo empty() {
        return new ClientInfo();
    }

    /**
     * Include extra info into the final client-info.
     *
     * @param name  the name
     * @param value the value
     * @return the client info
     */
    @CanIgnoreReturnValue
    public ClientInfo include(final String name, final Serializable value) {
        this.extraInfo.put(name, value);
        return this;
    }

    /**
     * From client info.
     *
     * @param request the request
     * @return the client info
     */
    public static ClientInfo from(final HttpServletRequest request) {
        return ClientInfo.from(request, null, null, false);
    }

    /**
     * Build client info.
     *
     * @param request                       the request
     * @param alternateServerAddrHeaderName the alternate server addr header name
     * @param alternateLocalAddrHeaderName  the alternate local addr header name
     * @param useServerHostAddress          the use server host address
     * @return the client info
     */
    public static ClientInfo from(final HttpServletRequest request,
                                  final String alternateServerAddrHeaderName,
                                  final String alternateLocalAddrHeaderName,
                                  final boolean useServerHostAddress) {
        val locale = request != null ? request.getLocale() : Locale.getDefault();
        val headers = new HashMap<String, String>();

        if (request != null) {
            val headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                var headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
        }

        var serverIpAddress = request != null ? request.getLocalAddr() : null;
        var clientIpAddress = request != null ? request.getRemoteAddr() : null;

        var geoLocation = "unknown";
        var userAgent = "unknown";

        if (request != null) {
            if (useServerHostAddress) {
                serverIpAddress = Unchecked.supplier(() -> InetAddress.getLocalHost().getHostAddress()).get();
            } else if (alternateServerAddrHeaderName != null && !alternateServerAddrHeaderName.isEmpty()) {
                serverIpAddress = request.getHeader(alternateServerAddrHeaderName) != null
                    ? request.getHeader(alternateServerAddrHeaderName) : request.getLocalAddr();
            }

            if (alternateLocalAddrHeaderName != null && !alternateLocalAddrHeaderName.isEmpty()) {
                clientIpAddress = request.getHeader(alternateLocalAddrHeaderName) != null
                    ? request.getHeader(alternateLocalAddrHeaderName)
                    : request.getRemoteAddr();
            }
            val header = request.getHeader("user-agent");
            userAgent = header == null ? "unknown" : header;

            var geo = request.getParameter("geolocation");
            if (geo == null) {
                geo = request.getHeader("geolocation");
            }
            geoLocation = geo == null ? "unknown" : geo;
        }

        val serverIp = serverIpAddress == null ? "unknown" : serverIpAddress;
        val clientIp = clientIpAddress == null ? "unknown" : clientIpAddress;

        return ClientInfo
            .empty()
            .setClientIpAddress(clientIp)
            .setServerIpAddress(serverIp)
            .setLocale(locale)
            .setGeoLocation(StringEscapeUtils.escapeHtml4(geoLocation))
            .setUserAgent(StringEscapeUtils.escapeHtml4(userAgent))
            .setHeaders(headers);
    }
}
