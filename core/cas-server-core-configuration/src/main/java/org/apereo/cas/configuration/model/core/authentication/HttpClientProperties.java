package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for http.client.truststore.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class HttpClientProperties {
    private int connectionTimeout = 5000;
    private int readTimeout = 5000;
    private int asyncTimeout = 5000;

    private Truststore truststore = new Truststore();
    
    public int getAsyncTimeout() {
        return asyncTimeout;
    }

    public void setAsyncTimeout(final int asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }
    
    public Truststore getTruststore() {
        return truststore;
    }

    public void setTruststore(final Truststore truststore) {
        this.truststore = truststore;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public static class Truststore {
        private Resource file = new ClassPathResource("truststore.jks");

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
