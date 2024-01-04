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
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.TokenCoreAutoConfiguration;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.validation.TicketValidator;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.List;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseJwtTokenTicketBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    BaseJwtTokenTicketBuilderTests.TokenTicketBuilderTestConfiguration.class,
    TokenCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class
})
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
                .thenReturn(TicketValidator.ValidationResult.builder().principal(principal).build());
            return validator;

        }

        private BaseRegisteredService createRegisteredService(final String id, final boolean hasSigningKey, final boolean hasEncryptionKey) {
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

                servicesManager.save(registeredService);
            }
            return registeredService;
        }
    }
}
