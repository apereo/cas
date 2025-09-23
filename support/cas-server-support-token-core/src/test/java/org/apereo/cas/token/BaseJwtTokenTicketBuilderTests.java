package org.apereo.cas.token;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasTokenCoreAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.validation.TicketValidationResult;
import org.apereo.cas.validation.TicketValidator;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.List;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseJwtTokenTicketBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    BaseJwtTokenTicketBuilderTests.TokenTicketBuilderTestConfiguration.class,
    CasTokenCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class
})
@ExtendWith(CasTestExtension.class)
public abstract class BaseJwtTokenTicketBuilderTests {
    @Autowired
    @Qualifier(TokenTicketBuilder.BEAN_NAME)
    protected TokenTicketBuilder tokenTicketBuilder;

    @Autowired
    @Qualifier("tokenCipherExecutor")
    protected CipherExecutor tokenCipherExecutor;

    @Autowired
    @Qualifier(JwtBuilder.TICKET_JWT_BUILDER_BEAN_NAME)
    protected JwtBuilder tokenTicketJwtBuilder;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @TestConfiguration(value = "TokenTicketBuilderTestConfiguration", proxyBeanMethods = false)
    static class TokenTicketBuilderTestConfiguration implements InitializingBean {
        @Autowired
        @Qualifier("servicesManager")
        private ServicesManager servicesManager;

        @Override
        public void afterPropertiesSet() {
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("https://cas.example.org.+"));
            servicesManager.save(createRegisteredService("https://jwt.example.org/cas.*", true, true));
            servicesManager.save(createRegisteredService("https://jwt.no-encryption-key.example.org/cas.*", true, false));
        }

        @Bean
        public TicketValidator tokenTicketValidator() throws Throwable {
            val validator = mock(TicketValidator.class);
            val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser",
                CollectionUtils.wrap("name", List.of("value"),
                    ProtocolAttributeEncoder.encodeAttribute("custom:name"), CollectionUtils.wrapList("custom:value")));
            when(validator.validate(anyString(), anyString()))
                .thenReturn(TicketValidationResult.builder().principal(principal).build());
            return validator;

        }

        private BaseRegisteredService createRegisteredService(final String id, final boolean hasSigningKey, final boolean hasEncryptionKey) {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);

            if (hasSigningKey) {
                registeredService.getProperties().put(
                    RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY.getPropertyName(),
                    new DefaultRegisteredServiceProperty().addValue("pR3Vizkn5FSY5xCg84cIS4m-b6jomamZD68C8ash-TlNmgGPcoLgbgquxHPoi24tRmGpqHgM4mEykctcQzZ-Xg"));
            }
            if (hasEncryptionKey) {
                registeredService.getProperties().put(
                    RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY.getPropertyName(),
                    new DefaultRegisteredServiceProperty().addValue("0KVXaN-nlXafRUwgsr3H_l6hkufY7lzoTy7OVI5pN0E"));
                registeredService.getProperties().put(
                    RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_ALG.getPropertyName(),
                    new DefaultRegisteredServiceProperty().addValue(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256));
                servicesManager.save(registeredService);
            }
            return registeredService;
        }
    }
}
