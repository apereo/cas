package org.apereo.cas.token;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.TokenCoreComponentSerializationConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
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
import org.springframework.context.annotation.Lazy;

import java.net.URL;
import java.util.List;

/**
 * This is {@link BaseJwtTokenTicketBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    TokenCoreConfiguration.class,
    TokenCoreComponentSerializationConfiguration.class,
    BaseJwtTokenTicketBuilderTests.TokenTicketBuilderTestConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class
})
public abstract class BaseJwtTokenTicketBuilderTests {
    @Autowired
    @Qualifier("tokenTicketBuilder")
    protected TokenTicketBuilder tokenTicketBuilder;

    @Autowired
    @Qualifier("tokenCipherExecutor")
    protected CipherExecutor tokenCipherExecutor;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @TestConfiguration("TokenTicketBuilderTestConfiguration")
    @Lazy(false)
    public static class TokenTicketBuilderTestConfiguration implements InitializingBean {
        @Autowired
        @Qualifier("inMemoryRegisteredServices")
        private List inMemoryRegisteredServices;

        @Override
        public void afterPropertiesSet() {
            inMemoryRegisteredServices.add(RegisteredServiceTestUtils.getRegisteredService("https://cas.example.org.+"));
            inMemoryRegisteredServices.add(createRegisteredService("https://jwt.example.org/cas.*", true, true));
            inMemoryRegisteredServices.add(createRegisteredService("https://jwt.no-encryption-key.example.org/cas.*", true, false));
        }

        private AbstractRegisteredService createRegisteredService(final String id, final boolean hasSigningKey, final boolean hasEncryptionKey) {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);

            if (hasSigningKey) {
                val signingKey = new DefaultRegisteredServiceProperty();
                signingKey.addValue("pR3Vizkn5FSY5xCg84cIS4m-b6jomamZD68C8ash-TlNmgGPcoLgbgquxHPoi24tRmGpqHgM4mEykctcQzZ-Xg");
                registeredService.getProperties().put(
                    RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY.getPropertyName(), signingKey);
            }
            if (hasEncryptionKey) {
                val encKey = new DefaultRegisteredServiceProperty();
                encKey.addValue("0KVXaN-nlXafRUwgsr3H_l6hkufY7lzoTy7OVI5pN0E");
                registeredService.getProperties().put(
                    RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY.getPropertyName(), encKey);

                inMemoryRegisteredServices.add(registeredService);
            }
            return registeredService;
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
