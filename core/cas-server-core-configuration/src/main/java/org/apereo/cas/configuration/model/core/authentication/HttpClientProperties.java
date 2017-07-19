package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.Beans;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for http.client.truststore.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class HttpClientProperties {
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
     * May also be set to <code>none</code> to disable verification.
     */
    private String hostNameVerifier = "default";

    /**
     * Configuration properties namespace for embedded Java SSL trust store.
     */
    private Truststore truststore = new Truststore();

    public String getHostNameVerifier() {
        return hostNameVerifier;
    }

    public void setHostNameVerifier(final String hostNameVerifier) {
        this.hostNameVerifier = hostNameVerifier;
    }

    public long getAsyncTimeout() {
        return Beans.newDuration(this.asyncTimeout).toMillis();
    }

    public void setAsyncTimeout(final String asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }
    
    public Truststore getTruststore() {
        return truststore;
    }

    public void setTruststore(final Truststore truststore) {
        this.truststore = truststore;
    }

    public long getConnectionTimeout() {
        return Beans.newDuration(this.connectionTimeout).toMillis();
    }

    public void setConnectionTimeout(final String connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getReadTimeout() {
        return Beans.newDuration(this.readTimeout).toMillis();
    }

    public void setReadTimeout(final String readTimeout) {
        this.readTimeout = readTimeout;
    }

    public static class Truststore {

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

        public Resource getFile() {
            return file;
        }

        public void setFile(final Resource file) {
            this.file = file;
        }

        public String getPsw() {
            return psw;
        }

        public void setPsw(final String psw) {
            this.psw = psw;
        }
    }
}
