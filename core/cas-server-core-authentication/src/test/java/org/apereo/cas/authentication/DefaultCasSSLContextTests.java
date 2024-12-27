package org.apereo.cas.authentication;

import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasSSLContextTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Authentication")
@EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
class DefaultCasSSLContextTests {

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration(CasCoreWebAutoConfiguration.class)
    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
    public static class SharedTestConfiguration {
        static String contactUrl(final String addr, final CasSSLContext context) throws Exception {
            val url = new URI(addr).toURL();
            val connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            connection.setSSLSocketFactory(context.getSslContext().getSocketFactory());
            try (val is = connection.getInputStream()) {
                return IOUtils.toString(is, StandardCharsets.UTF_8);
            }
        }
    }

    @Nested
    @SpringBootTest(classes = SharedTestConfiguration.class)
    @EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
    @ExtendWith(CasTestExtension.class)
    public class SystemSslContext {
        @Autowired
        @Qualifier(CasSSLContext.BEAN_NAME)
        private CasSSLContext casSslContext;

        @Test
        void verifyOperation() {
            assertNotNull(casSslContext.getTrustManagerFactory());
            assertThrows(Exception.class,
                () -> SharedTestConfiguration.contactUrl("https://self-signed.badssl.com", casSslContext));
        }
    }

    @Nested
    @SpringBootTest(classes = SharedTestConfiguration.class,
        properties = "cas.http-client.host-name-verifier=none")
    @ExtendWith(CasTestExtension.class)
    public class DisabledSslContext {
        @Autowired
        @Qualifier(CasSSLContext.BEAN_NAME)
        private CasSSLContext casSslContext;

        @Test
        void verifyOperation() throws Throwable {
            assertNotNull(SharedTestConfiguration.contactUrl("https://untrusted-root.badssl.com/", casSslContext));
        }
    }
}
