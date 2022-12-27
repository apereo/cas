package org.apereo.cas;

import org.apereo.cas.config.GoogleCloudSecretsManagerCloudConfigBootstrapConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.*;

/**
 * This is {@link GoogleCloudSecretsManagerCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    GoogleCloudSecretsManagerCloudConfigBootstrapConfiguration.class
}, properties = {

})
@EnableConfigurationProperties(CasConfigurationProperties.class)

@Slf4j
public class GoogleCloudSecretsManagerCloudConfigBootstrapConfigurationTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {

    }

    @Test
    public void verifyOperation() {
        assertNotNull(casProperties);
    }
}
