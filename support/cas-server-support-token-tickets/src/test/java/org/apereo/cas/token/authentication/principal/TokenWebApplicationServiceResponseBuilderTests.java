package org.apereo.cas.token.authentication.principal;

import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.token.cipher.TokenTicketCipherExecutor;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link TokenWebApplicationServiceResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    TokenCoreConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuthenticationConfiguration.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public class TokenWebApplicationServiceResponseBuilderTests {

    @Autowired
    @Qualifier("webApplicationServiceResponseBuilder")
    private ResponseBuilder responseBuilder;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> serviceFactory;

    @Test
    public void verifyDecrypt() {
        final String signingSecret = "EihBwA3OuDQMm4gdWzkqRJ87596G7o7a_naJAJipxFoRJbXK7APRcnCA91Y30rJdh4q-C2dmpfV6eNhQT0bR5A";
        final String encryptionSecret = "dJ2YpUd-r_Qd7e3nDm79WiIHkqaLT8yZt6nN5eG0YnE";

        final TokenTicketCipherExecutor cipher = new TokenTicketCipherExecutor(encryptionSecret, signingSecret, true);
        final String result = cipher.decode(cipher.encode("ThisIsValue"));
        assertEquals("ThisIsValue", result);
    }

    @Test
    public void verifyDecryptNoEncryption() {
        final String signingSecret = "EihBwA3OuDQMm4gdWzkqRJ87596G7o7a_naJAJipxFoRJbXK7APRcnCA91Y30rJdh4q-C2dmpfV6eNhQT0bR5A";
        final String encryptionSecret = "dJ2YpUd-r_Qd7e3nDm79WiIHkqaLT8yZt6nN5eG0YnE";
        final TokenTicketCipherExecutor cipher = new TokenTicketCipherExecutor(encryptionSecret, signingSecret, false);
        final String result = cipher.decode(cipher.encode("ThisIsValue"));
        assertEquals("ThisIsValue", result);
    }

    @Test
    public void verifyBuilderSupport() {
        assertTrue(responseBuilder.supports(serviceFactory.createService("test")));
    }
}
