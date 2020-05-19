package org.apereo.cas.web;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.pac4j.config.Pac4jDelegatedAuthenticationConfiguration;
import org.apereo.cas.support.pac4j.config.support.authentication.Pac4jAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.support.pac4j.config.support.authentication.Pac4jDelegatedAuthenticationSerializationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.flow.config.DelegatedAuthenticationWebflowConfiguration;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link BaseDelegatedAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseDelegatedAuthenticationTests {

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        ThymeleafAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @EnableWebMvc
    @Import({
        WebMvcAutoConfiguration.class,
        MockMvcAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreHttpConfiguration.class,
        CoreSamlConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        DelegatedAuthenticationWebflowTestConfiguration.class,
        Pac4jDelegatedAuthenticationConfiguration.class,
        Pac4jAuthenticationEventExecutionPlanConfiguration.class,
        Pac4jDelegatedAuthenticationSerializationConfiguration.class,
        DelegatedAuthenticationWebflowConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration("Saml2ClientMetadataControllerTestConfiguration")
    @Lazy(false)
    public static class DelegatedAuthenticationWebflowTestConfiguration {
        @Bean
        public Clients builtClients() throws Exception {
            val idpMetadata = new File("src/test/resources/idp-metadata.xml").getCanonicalPath();
            val keystorePath = new File(FileUtils.getTempDirectory(), "keystore").getCanonicalPath();
            val spMetadataPath = new File(FileUtils.getTempDirectory(), "sp-metadata.xml").getCanonicalPath();

            val saml2Config = new SAML2Configuration(keystorePath, "changeit", "changeit", idpMetadata);
            saml2Config.setServiceProviderEntityId("cas:example:sp");
            saml2Config.setServiceProviderMetadataPath(spMetadataPath);
            saml2Config.setAuthnRequestBindingType("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
            saml2Config.init();

            val saml2Client = new SAML2Client(saml2Config);
            saml2Client.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT, Boolean.TRUE);
            saml2Client.setCallbackUrl("http://callback.example.org");
            saml2Client.init();

            val casClient = new CasClient(new CasConfiguration("https://sso.example.org/cas/login"));
            casClient.setCallbackUrl("http://callback.example.org");
            casClient.init();

            val facebookClient = new FacebookClient() {
                @Override
                protected Optional<OAuth20Credentials> retrieveCredentials(final WebContext context) {
                    return Optional.of(new OAuth20Credentials("fakeVerifier"));
                }
            };
            facebookClient.setProfileCreator((credentials, context) -> {
                val profile = new CommonProfile();
                profile.setClientName(facebookClient.getName());
                profile.setId("casuser");
                profile.addAttribute("uid", "casuser");
                profile.addAttribute("givenName", "ApereoCAS");
                profile.addAttribute("memberOf", "admin");
                return Optional.of(profile);
            });
            facebookClient.setName(FacebookClient.class.getSimpleName());

            return new Clients("https://cas.login.com", List.of(saml2Client, casClient, facebookClient));
        }
    }
}
