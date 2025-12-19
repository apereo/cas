package org.apereo.cas.metadata.rest;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationMetadataServerEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
class CasConfigurationMetadataServerEndpointTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() {
        val repository = new CasConfigurationMetadataRepository();
        val endpoint = new CasConfigurationMetadataServerEndpoint(casProperties, applicationContext, repository);
        val result = endpoint.properties();
        assertNotNull(result);
        assertFalse(result.isEmpty());

        val results = endpoint.search("server.port");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
