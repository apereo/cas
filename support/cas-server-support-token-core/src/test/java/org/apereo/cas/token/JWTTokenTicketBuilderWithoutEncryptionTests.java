package org.apereo.cas.token;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * This is {@link JWTTokenTicketBuilderWithoutEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
        CasCoreTicketsConfiguration.class,
        JWTTokenTicketBuilderWithoutEncryptionTests.TokenTicketBuilderTestConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        TokenCoreConfiguration.class})
@Slf4j
@TestPropertySource(properties = "cas.authn.token.crypto.encryptionEnabled=false")
public class JWTTokenTicketBuilderWithoutEncryptionTests {

    @Autowired
    @Qualifier("tokenTicketBuilder")
    private TokenTicketBuilder tokenTicketBuilder;

    @Autowired
    @Qualifier("tokenCipherExecutor")
    private CipherExecutor tokenCipherExecutor;

    @Test
    public void verifyJwtForServiceTicket() throws Exception {
        final String jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        final Object result = tokenCipherExecutor.decode(jwt);
        final JWTClaimsSet claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @TestConfiguration
    public static class TokenTicketBuilderTestConfiguration {

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
