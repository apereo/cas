package org.apereo.cas.configuration.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * A central organizing component for CAS configuration model. It encapsulates nested classes representing various
 * configuration properties for all CAS subsystems. Those type-safe configuration classes are implemented via
 * Spring Boot's <code>@ConfigurationProperties</code> mechanism.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Component("casConfigurationModel")
public class CasConfigurationModel {

    /**
     * Config props for http.client.truststore.
     */
    @ConfigurationProperties(prefix = "http.client.truststore", ignoreUnknownFields = false)
    public static class HttpClientTrustStoreConfigurationProperties {

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
