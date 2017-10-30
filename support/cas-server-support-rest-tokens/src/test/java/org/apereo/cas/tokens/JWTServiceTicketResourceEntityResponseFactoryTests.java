package org.apereo.cas.tokens;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
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
import org.apereo.cas.config.CasRestTokensConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * This is {@link JWTServiceTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreTicketsConfiguration.class,
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
        TokenCoreConfiguration.class,
        JWTServiceTicketResourceEntityResponseFactoryTests.TicketResourceTestConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasRestTokensConfiguration.class})
public class JWTServiceTicketResourceEntityResponseFactoryTests {

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("tokenCipherExecutor")
    private CipherExecutor tokenCipherExecutor;
    
    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("serviceTicketResourceEntityResponseFactory")
    private ServiceTicketResourceEntityResponseFactory serviceTicketResourceEntityResponseFactory;

    @Test
    public void verifyServiceTicketAsDefault() {
        final AuthenticationResult result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport);
        final TicketGrantingTicket tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        final Service service = RegisteredServiceTestUtils.getService("test");
        final ResponseEntity<String> response = serviceTicketResourceEntityResponseFactory.build(tgt.getId(), service, result);
        assertNotNull(response);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void verifyServiceTicketAsJwt() throws Exception {
        final AuthenticationResult result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        final TicketGrantingTicket tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        final Service service = RegisteredServiceTestUtils.getService("jwtservice");
        final ResponseEntity<String> response = serviceTicketResourceEntityResponseFactory.build(tgt.getId(), service, result);
        assertNotNull(response);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertFalse(response.getBody().startsWith(ServiceTicket.PREFIX));
        
        final Object jwt = this.tokenCipherExecutor.decode(response.getBody());
        final JWTClaimsSet claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }

    @TestConfiguration
    public static class TicketResourceTestConfiguration {

        @Bean
        public AbstractUrlBasedTicketValidator casClientTicketValidator() {
            final AbstractUrlBasedTicketValidator validator = new AbstractUrlBasedTicketValidator("https://cas.example.org") {
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
            return validator;
        }
    }
}
