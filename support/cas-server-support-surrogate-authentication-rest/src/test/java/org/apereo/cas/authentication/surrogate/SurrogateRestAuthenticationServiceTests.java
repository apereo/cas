package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationAuditConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationMetadataConfiguration;
import org.apereo.cas.config.SurrogateRestAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link SurrogateRestAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    SurrogateRestAuthenticationConfiguration.class,
    SurrogateAuthenticationConfiguration.class,
    SurrogateAuthenticationAuditConfiguration.class,
    SurrogateAuthenticationMetadataConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasThemesConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class
})
@TestPropertySource(properties = "cas.authn.surrogate.rest.url=http://localhost:9301")
@Getter
public class SurrogateRestAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService service;

    private MockWebServer webServer;

    @Override
    @Test
    public void verifyList() {
        var data = "";
        try {
            data = MAPPER.writeValueAsString(CollectionUtils.wrapList("casuser", "otheruser"));
        } catch (final JsonProcessingException e) {
            throw new AssertionError(e);
        }
        try (val webServer = new MockWebServer(9301,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            this.webServer = webServer;
            this.webServer.start();
            assertTrue(this.webServer.isRunning());
            super.verifyList();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Override
    @Test
    public void verifyProxying() {
        var data = "";
        try {
            data = MAPPER.writeValueAsString(CollectionUtils.wrapList("casuser", "otheruser"));
        } catch (final JsonProcessingException e) {
            throw new AssertionError(e);
        }
        try (val webServer = new MockWebServer(9310,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getAuthn().getSurrogate().getRest().setUrl("http://localhost:9310");
            val surrogateService = new SurrogateRestAuthenticationService(props.getAuthn().getSurrogate().getRest(), servicesManager);

            val result = surrogateService.canAuthenticateAs("cassurrogate",
                CoreAuthenticationTestUtils.getPrincipal("casuser"),
                CoreAuthenticationTestUtils.getService());
            // Can't use super() until the REST classes are completely refactored and don't need an actual server to connect to.
            assertTrue(result);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
