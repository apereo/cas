package org.apereo.cas.authentication;

import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import static org.junit.Assert.*;

/**
 * Tests for the {@code FileTrustStoreSslSocketFactory} class, checking for self-signed
 * and missing certificates via a local truststore.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class FileTrustStoreSslSocketFactoryTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("truststore.jks");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyTrustStoreLoadingSuccessfullyWithCertAvailable() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://self-signed.badssl.com"));
    }
    
    @Test
    public void verifyTrustStoreLoadingSuccessfullyWithCertAvailable2() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://untrusted-root.badssl.com"));
    }

    @Test
     public void verifyTrustStoreNotFound() throws Exception {
        this.thrown.expect(RuntimeException.class);
        new FileTrustStoreSslSocketFactory(new FileSystemResource("test.jks"), "changeit");
    }

    @Test
    public void verifyTrustStoreBadPassword() throws Exception {
        this.thrown.expect(RuntimeException.class);
        new FileTrustStoreSslSocketFactory(RESOURCE, "invalid");
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyForValidEndpointWithNoCert() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://www.google.com"));
    }
    @Test
    public void verifyTrustStoreLoadingSuccessfullyWihInsecureEndpoint() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("http://wikipedia.org"));
    }

    private static FileTrustStoreSslSocketFactory sslFactory() {
        return new FileTrustStoreSslSocketFactory(RESOURCE, "changeit");
    }
}
