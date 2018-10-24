package org.apereo.cas.impl.token;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.category.RestfulApiCategory;
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
import org.apereo.cas.config.PasswordlessAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestfulPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    PasswordlessAuthenticationConfiguration.class,
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
@TestPropertySource(properties = "cas.authn.passwordless.tokens.rest.url=http://localhost:9293")
@Category(RestfulApiCategory.class)
public class RestfulPasswordlessTokenRepositoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("passwordlessTokenRepository")
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Autowired
    @Qualifier("passwordlessCipherExecutor")
    private CipherExecutor passwordlessCipherExecutor;

    @Test
    public void verifyFindToken() {
        val token = passwordlessTokenRepository.createToken("casuser");
        val data = passwordlessCipherExecutor.encode(token).toString();
        try (val webServer = new MockWebServer(9306,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val tokens = new CasConfigurationProperties().getAuthn().getPasswordless().getTokens();
            tokens.getRest().setUrl("http://localhost:9306");
            val passwordless = new RestfulPasswordlessTokenRepository(tokens.getExpireInSeconds(), tokens.getRest(), passwordlessCipherExecutor);

            val foundToken = passwordless.findToken("casuser");
            assertNotNull(foundToken);
            assertTrue(foundToken.isPresent());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifySaveToken() {
        val data = "THE_TOKEN";
        try (val webServer = new MockWebServer(9307,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val tokens = new CasConfigurationProperties().getAuthn().getPasswordless().getTokens();
            tokens.getRest().setUrl("http://localhost:9307");
            val passwordless = new RestfulPasswordlessTokenRepository(tokens.getExpireInSeconds(), tokens.getRest(), passwordlessCipherExecutor);

            passwordless.saveToken("casuser", data);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyDeleteToken() {
        try (val webServer = new MockWebServer(9293,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            passwordlessTokenRepository.deleteToken("casuser", "123456");
            passwordlessTokenRepository.deleteTokens("casuser");
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
