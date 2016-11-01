package org.apereo.cas.authentication;

import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.junit.Ignore;
import org.junit.Test;
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

    @Test
    public void verifyTrustStoreLoadingSuccessfullyWithCertAvailable() throws Exception {
        final ClassPathResource resource = new ClassPathResource("truststore.jks");
        final FileTrustStoreSslSocketFactory factory = new FileTrustStoreSslSocketFactory(resource, "changeit");
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(factory);
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://self-signed.badssl.com"));
    }
    
    @Ignore
    @Test
    public void verifyTrustStoreLoadingSuccessfullyWithCertAvailable2() throws Exception {
        final ClassPathResource resource = new ClassPathResource("truststore.jks");
        final FileTrustStoreSslSocketFactory factory = new FileTrustStoreSslSocketFactory(resource, "changeit");
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(factory);
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://test.scaldingspoon.org/idp/shibboleth"));
    }


    @Test(expected = RuntimeException.class)
     public void verifyTrustStoreNotFound() throws Exception {
        new FileTrustStoreSslSocketFactory(new FileSystemResource("test.jks"), "changeit");
    }

    @Test(expected = RuntimeException.class)
    public void verifyTrustStoreBadPassword() throws Exception {
        final ClassPathResource resource = new ClassPathResource("truststore.jks");
        new FileTrustStoreSslSocketFactory(resource, "invalid");
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyForValidEndpointWithNoCert() throws Exception {
        final ClassPathResource resource = new ClassPathResource("truststore.jks");
        final FileTrustStoreSslSocketFactory factory = new FileTrustStoreSslSocketFactory(resource, "changeit");
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(factory);
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://www.google.com"));
    }
    @Test
    public void verifyTrustStoreLoadingSuccessfullyWihInsecureEndpoint() throws Exception {
        final ClassPathResource resource = new ClassPathResource("truststore.jks");
        final FileTrustStoreSslSocketFactory factory = new FileTrustStoreSslSocketFactory(resource, "changeit");
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(factory);
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("http://wikipedia.org"));
    }
}
