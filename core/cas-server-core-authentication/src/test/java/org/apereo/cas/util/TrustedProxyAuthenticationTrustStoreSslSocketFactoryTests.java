package org.apereo.cas.util;

import org.apereo.cas.authentication.DefaultCasSslContext;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class TrustedProxyAuthenticationTrustStoreSslSocketFactoryTests {
    private static final ClassPathResource TRUST_STORE = new ClassPathResource("truststore.jks");
    private static final String TRUST_STORE_PSW = "changeit";

    private HttpClient client;

    @BeforeEach
    @SneakyThrows
    public void prepareHttpClient() {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(new SSLConnectionSocketFactory(new DefaultCasSslContext(TRUST_STORE, TRUST_STORE_PSW, KeyStore.getDefaultType()).getSslContext()));
        this.client = clientFactory.getObject();
    }

    @Test
    public void verifySuccessfulConnection() {
        val valid = client.isValidEndPoint("https://www.github.com");
        assertTrue(valid);
    }

    @Test
    public void verifySuccessfulConnectionWithCustomSSLCert() {
        val valid = client.isValidEndPoint("https://self-signed.badssl.com");
        assertTrue(valid);
    }

}
