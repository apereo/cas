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
    private String connectionTimeout = "PT5S";
    private String readTimeout = "PT5S";
    private String asyncTimeout = "PT5S";
    private String hostNameVerifier = "default";

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
        private Resource file;

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
