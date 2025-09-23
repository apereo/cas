package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.cas.ws.idp.WSFederationConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CustomNamespaceWSFederationClaimsReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WSFederation")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class CustomNamespaceWSFederationClaimsReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "verifyWsFedCustomSerializePolicyToJson.json");
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAttributeRelease() throws Throwable {

        val service = RegisteredServiceTestUtils.getRegisteredService("verifyAttributeRelease");
        val policy = new CustomNamespaceWSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.COMMON_NAME.getClaim(), "cn",
                WSFederationClaims.EMAIL_ADDRESS.getClaim(), "email"));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", "casuser", "email", "cas@example.org"));
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        var results = policy.getAttributes(releasePolicyContext);
        assertSame(2, results.size());
        assertTrue(results.containsKey(WSFederationConstants.HTTP_SCHEMAS_APEREO_CAS.concat(WSFederationClaims.COMMON_NAME.getClaim())));
        assertTrue(results.containsKey(WSFederationConstants.HTTP_SCHEMAS_APEREO_CAS.concat(WSFederationClaims.EMAIL_ADDRESS.getClaim())));
    }


    @Test
    void verifySerializePolicyToJson() throws IOException {
        val policyWritten = new CustomNamespaceWSFederationClaimsReleasePolicy(
            CollectionUtils.wrap(WSFederationClaims.COMMON_NAME.getClaim(), "cn",
                WSFederationClaims.EMAIL_ADDRESS.getClaim(), "email"));
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, CustomNamespaceWSFederationClaimsReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
