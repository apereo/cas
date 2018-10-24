package org.apereo.cas.tokens;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
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
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasRestConfiguration;
import org.apereo.cas.config.CasRestTokensConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.net.URL;
import java.util.List;

/**
 * This is {@link BaseTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    BaseTicketResourceEntityResponseFactoryTests.TicketResourceTestConfiguration.class,
    CasRestTokensConfiguration.class,
    CasRestConfiguration.class,
    TokenCoreConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasRestTokensConfiguration.class,
    CasRestConfiguration.class,
    CasCoreTicketsConfiguration.class
})
public abstract class BaseTicketResourceEntityResponseFactoryTests {
    @Autowired
    @Qualifier("ticketGrantingTicketResourceEntityResponseFactory")
    protected TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    protected AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("tokenCipherExecutor")
    protected CipherExecutor tokenCipherExecutor;

    @Autowired
    @Qualifier("centralAuthenticationService")
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("serviceTicketResourceEntityResponseFactory")
    protected ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory;

    @TestConfiguration
    public static class TicketResourceTestConfiguration implements InitializingBean {

        @Autowired
        @Qualifier("inMemoryRegisteredServices")
        private List inMemoryRegisteredServices;

        public void init() {
            inMemoryRegisteredServices.add(RegisteredServiceTestUtils.getRegisteredService("https://cas.example.org.+"));
        }

        @Override
        public void afterPropertiesSet() {
            init();
        }

        @Bean
        public AbstractUrlBasedTicketValidator casClientTicketValidator() {
            return new AbstractUrlBasedTicketValidator("https://cas.example.org") {
                @Override
                protected String getUrlSuffix() {
                    return "/cas";
                }

                @Override
                protected Assertion parseResponseFromServer(final String s) {
                    return new AssertionImpl(new AttributePrincipalImpl("casuser", CollectionUtils.wrap("name", "value")));
                }

                @Override
                protected String retrieveResponseFromServer(final URL url, final String s) {
                    return "theresponse";
                }
            };
        }
    }
}
