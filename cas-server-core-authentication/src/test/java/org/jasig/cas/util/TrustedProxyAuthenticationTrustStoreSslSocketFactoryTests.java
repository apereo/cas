package org.jasig.cas.util;

import org.jasig.cas.authentication.FileTrustStoreSslSocketFactory;
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.SimpleHttpClientFactoryBean;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class TrustedProxyAuthenticationTrustStoreSslSocketFactoryTests {
    private static final ClassPathResource TRUST_STORE = new ClassPathResource("truststore.jks");
    private static final String TRUST_STORE_PSW = "changeit";

    private HttpClient client;

    @Before
    public void prepareHttpClient() throws Exception {
        final FileTrustStoreSslSocketFactory sslFactory = new FileTrustStoreSslSocketFactory(
                TRUST_STORE.getFile(), TRUST_STORE_PSW);

        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory);
        this.client = clientFactory.getObject();
    }

    @Ignore
    public void verifySuccessfulConnection() {
        final boolean valid = client.isValidEndPoint("https://www.github.com");
        assertTrue(valid);
    }

    @Test
    public void verifySuccessfulConnectionWithCustomSSLCert() {
        final boolean valid = client.isValidEndPoint("https://www.cacert.org");
        assertTrue(valid);
    }

}
