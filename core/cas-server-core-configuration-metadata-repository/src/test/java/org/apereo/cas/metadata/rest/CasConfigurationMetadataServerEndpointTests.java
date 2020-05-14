package org.apereo.cas.metadata.rest;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationMetadataServerEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class CasConfigurationMetadataServerEndpointTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() {
        val repository = new CasConfigurationMetadataRepository();
        val endpoint = new CasConfigurationMetadataServerEndpoint(casProperties, repository);
        val result = endpoint.properties();
        assertNotNull(result);
        assertFalse(result.isEmpty());

        val results = endpoint.search("server.port");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
