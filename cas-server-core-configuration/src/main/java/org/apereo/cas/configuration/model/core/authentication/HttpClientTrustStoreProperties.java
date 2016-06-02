package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for http.client.truststore.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "http.client.truststore", ignoreUnknownFields = false)
public class HttpClientTrustStoreProperties {

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
