package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties class for http.client.truststore.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class HttpClientProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -7494946569869245770L;

    /**
     * Connection timeout for all operations that reach out to URL endpoints.
     */
    @DurationCapable
    private String connectionTimeout = "PT5S";

    /**
     * Determines the default socket timeout value for I/O operations.
     */
    @DurationCapable
    private String socketTimeout = "PT5S";

    /**
     * Determines the timeout until arrival of a response from the opposite endpoint.
     * A timeout value of zero is interpreted as an infinite timeout.
     * Please note that response timeout may be unsupported by HTTP transports with message multiplexing.
     */
    @DurationCapable
    private String responseTimeout = "PT5S";

    /**
     * Indicates timeout for async operations.
     */
    @DurationCapable
    private String asyncTimeout = "PT5S";

    /**
     * Enable hostname verification when attempting to contact URL endpoints.
     * May also be set to {@code none} to disable verification.
     */
    private String hostNameVerifier = "default";

    /**
     * Configuration properties namespace for embedded Java SSL trust store.
     */
    @NestedConfigurationProperty
    private HttpClientTrustStoreProperties truststore = new HttpClientTrustStoreProperties();

    /**
     * Whether CAS should accept local URLs.
     * For example {@code http(s)://localhost/logout}.
     */
    private boolean allowLocalUrls;

    /**
     * If specified the regular expression will be used to validate the url's authority.
     */
    @RegularExpressionCapable
    private String authorityValidationRegex;

    /**
     * Send requests via a proxy; define the hostname.
     */
    private String proxyHost;

    /**
     * Send requests via a proxy; define the proxy port.
     * Negative/zero values should deactivate the proxy configuration
     * for the http client.
     */
    private int proxyPort;

    /**
     * Whether the regular expression specified with {@link #authorityValidationRegex}
     * should be handled as case-sensitive ({@code true}) or case-insensitive ({@code false}). If
     * no {@link #authorityValidationRegex} is set, this value does not have any effect.
     */
    private boolean authorityValidationRegExCaseSensitive = true;

    /**
     * The default headers to use for any HTTP connection.
     * This is defined as map, where the key is the header name
     * and the value is the header value that should be sent
     * along with request.
     */
    private Map<String, String> defaultHeaders = new HashMap<>();
}
