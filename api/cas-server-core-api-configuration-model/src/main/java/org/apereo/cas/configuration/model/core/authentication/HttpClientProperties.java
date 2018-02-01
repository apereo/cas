package org.apereo.cas.configuration.model.core.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.core.io.Resource;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class for http.client.truststore.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Slf4j
@Getter
@Setter
public class HttpClientProperties implements Serializable {

    private static final long serialVersionUID = -7494946569869245770L;

    /**
     * Connection timeout for all operations that reach out to URL endpoints.
     */
    private String connectionTimeout = "PT5S";

    /**
     * Read timeout for all operations that reach out to URL endpoints.
     */
    private String readTimeout = "PT5S";

    /**
     * Indicates timeout for async operations.
     */
    private String asyncTimeout = "PT5S";

    /**
     * Enable hostname verification when attempting to contact URL endpoints.
     * May also be set to {@code none} to disable verification.
     */
    private String hostNameVerifier = "default";

    /**
     * Configuration properties namespace for embedded Java SSL trust store.
     */
    private Truststore truststore = new Truststore();

    /**
     * Whether CAS should accept local logout URLs.
     * For example http(s)://localhost/logout
     */
    private boolean allowLocalLogoutUrls;

    /**
     * If specified the regular expression will be used to validate the url's authority.
     */
    private String authorityValidationRegEx;

    /**
     * Whether the regular expression specified with {@code authorityValidationRegEx} should be handled as case-sensitive
     * ({@code true}) or case-insensitive ({@code false}). If no {@code authorityValidationRegEx} is set, this value does not have any effect.
     */
    private boolean authorityValidationRegExCaseSensitive = true;

    @Getter
    @Setter
    public static class Truststore implements Serializable {

        private static final long serialVersionUID = -1357168622083627654L;

        /**
         * The CAS local truststore resource to contain certificates to the CAS deployment.
         * In the event that local certificates are to be imported into the CAS running environment,
         * a local truststore is provided by CAS to improve portability of configuration across environments.
         */
        private Resource file;

        /**
         * The truststore password.
         */
        private String psw = "changeit";
    }
}
