package org.apereo.cas.azure.ad.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.azure.ad.config.AzureActiveDirectoryAuthenticationConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.adal4j.AuthenticationResult;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AzureActiveDirectoryAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AzureActiveDirectoryAuthenticationConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class
},
    properties = {
        "cas.authn.azure-active-directory.client-id=12345678-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.azure-active-directory.login-url=https://login.microsoftonline.com/common/",

        "cas.authn.attribute-repository.azure-active-directory[0].client-id=12345678-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.attribute-repository.azure-active-directory[0].client-secret=msdbdsf84d"
    })
@Tag("AuthenticationHandler")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AzureActiveDirectoryAuthenticationHandlerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("microsoftAzureActiveDirectoryAttributeRepositories")
    private List<IPersonAttributeDao> microsoftAzureActiveDirectoryAttributeRepositories;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
    private AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler;

    @Test
    public void verifyOperationFails() {
        assertFalse(microsoftAzureActiveDirectoryAttributeRepositories.isEmpty());
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
            "castest@hotmail.onmicrosoft.com", "1234567890");
        assertThrows(FailedLoginException.class, () -> microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(c));
    }

    @Test
    public void verifySuccess() throws Exception {
        val handler = getMockAzureActiveDirectoryAuthenticationHandler(8890);
        val entity = MAPPER.writeValueAsString(RegisteredServiceTestUtils.getTestAttributes());
        try (val webServer = new MockWebServer(8890,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();

            val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@hotmail.onmicrosoft.com", "1234567890");
            assertNotNull(handler.authenticate(c));
        }
    }

    @Test
    public void verifyOperationFailsLogin() throws Exception {
        val handler = getMockAzureActiveDirectoryAuthenticationHandler(7787);
        val entity = MAPPER.writeValueAsString(RegisteredServiceTestUtils.getTestAttributes());
        try (val webServer = new MockWebServer(7787,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.UNAUTHORIZED)) {
            webServer.start();

            val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@hotmail.onmicrosoft.com", "1234567890");
            assertThrows(FailedLoginException.class, () -> handler.authenticate(c));
        }
    }

    private AzureActiveDirectoryAuthenticationHandler getMockAzureActiveDirectoryAuthenticationHandler(final int port) {
        return new AzureActiveDirectoryAuthenticationHandler(getClass().getName(), servicesManager,
            PrincipalFactoryUtils.newPrincipalFactory(), 0, casProperties.getAuthn().getAzureActiveDirectory().getClientId(),
            casProperties.getAuthn().getAzureActiveDirectory().getLoginUrl(), "http://localhost:" + port) {
            @Override
            protected AuthenticationResult getAccessTokenFromUserCredentials(final String username, final String password) throws Exception {
                return new AuthenticationResult("accessType", UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(), 3600, UUID.randomUUID().toString(), null, true);
            }
        };
    }


}
