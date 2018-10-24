package org.apereo.cas.authentication;

import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.KeyStore;

import static org.junit.Assert.*;

/**
 * Tests for the {@code FileTrustStoreSslSocketFactory} class, checking for self-signed
 * and missing certificates via a local truststore.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class FileTrustStoreSslSocketFactoryTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("truststore.jks");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @SneakyThrows
    private static SSLConnectionSocketFactory sslFactory(final Resource resource, final String password) {
        return new SSLConnectionSocketFactory(new DefaultCasSslContext(resource,
            password,
            KeyStore.getDefaultType()).getSslContext());
    }

    private static SSLConnectionSocketFactory sslFactory() {
        return sslFactory(RESOURCE, "changeit");
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyWithCertAvailable() {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        val client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://self-signed.badssl.com"));
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyWithCertAvailable2() {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        val client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://untrusted-root.badssl.com"));
    }

    @Test
    public void verifyTrustStoreNotFound() {
        this.thrown.expect(IOException.class);
        sslFactory(new FileSystemResource("test.jks"), "changeit");
    }

    @Test
    public void verifyTrustStoreBadPassword() {
        this.thrown.expect(IOException.class);
        sslFactory(RESOURCE, "invalid");
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyForValidEndpointWithNoCert() {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        val client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://www.google.com"));
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyWihInsecureEndpoint() {
        val clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        val client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("http://wikipedia.org"));
    }
}
