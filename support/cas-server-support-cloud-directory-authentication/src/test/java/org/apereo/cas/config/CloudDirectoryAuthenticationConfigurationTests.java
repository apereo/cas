package org.apereo.cas.config;

import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;

import com.amazonaws.services.clouddirectory.AmazonCloudDirectory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CloudDirectoryAuthenticationConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    CloudDirectoryAuthenticationConfiguration.class
},
    properties = {
        "cas.authn.cloudDirectory.usernameAttributeName=username",
        "cas.authn.cloudDirectory.passwordAttributeName=password",
        "cas.authn.cloudDirectory.endpoint=http://127.0.0.1:1234"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("AmazonWebServices")
public class CloudDirectoryAuthenticationConfigurationTests {
    @Autowired
    @Qualifier("amazonCloudDirectory")
    private AmazonCloudDirectory amazonCloudDirectory;

    @Test
    public void verifyOperation() {
        assertNotNull(amazonCloudDirectory);
    }
}
