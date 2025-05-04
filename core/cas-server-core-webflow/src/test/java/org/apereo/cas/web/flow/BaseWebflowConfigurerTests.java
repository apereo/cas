package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreEventsAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link BaseWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.tgc.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
    "cas.tgc.crypto.encryption.key=u696jJnPvm1DHLR7yVCSKMMzzoPoFxJZW4-MP1CkM5w",
    "cas.tgc.crypto.signing.key=zPdNCd0R1oMR0ClzEqZzapkte8rO0tNvygYjmHoUhitAu6CBscwMC3ZTKy8tleTKiQ6GVcuiQQgxfd1nSKxf7w",

    "cas.webflow.crypto.encryption.key=qLhvLuaobvfzMmbo9U_bYA",
    "cas.webflow.crypto.signing.key=oZeAR5pEXsolruu4OQYsQKxf-FCvFzSsKlsVaKmfIl6pNzoPm6zPW94NRS1af7vT-0bb3DpPBeksvBXjloEsiA"
})
@AutoConfigureObservability
@ExtendWith(CasTestExtension.class)
public abstract class BaseWebflowConfigurerTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
    protected CasWebflowExecutionPlan casWebflowExecutionPlan;

    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
    protected FlowDefinitionRegistry flowDefinitionRegistry;

    @BeforeEach
    void setup() {
        casWebflowExecutionPlan.execute();
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreEventsAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(SharedTestConfiguration.AttributeRepositoryTestConfiguration.class)
    public static class SharedTestConfiguration {

        @TestConfiguration(value = "AttributeRepositoryTestConfiguration", proxyBeanMethods = false)
        public static class AttributeRepositoryTestConfiguration {
            @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            @Bean
            public PersonAttributeDao attributeRepository() {
                val attrs = CollectionUtils.wrap(
                    "uid", CollectionUtils.wrap("uid"),
                    "mail", CollectionUtils.wrap("cas@apereo.org"),
                    "eduPersonAffiliation", CollectionUtils.wrap("developer"),
                    "groupMembership", CollectionUtils.wrap("adopters"));
                return new StubPersonAttributeDao((Map) attrs);
            }

            @Bean
            public AttributeRepositoryResolver attributeRepositoryResolver() {
                return query -> Set.of(PersonAttributeDao.WILDCARD);
            }
        }
    }
}
