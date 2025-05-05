package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMonitorAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    AuthenticationAttributeReleasePolicyTests.CasCoreAuthenticationSupportTestConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreMonitorAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class
},
    properties = {
        "cas.authn.core.groovy-authentication-resolution.location=classpath:GroovyAuthenticationHandlerResolver.groovy",
        "cas.authn.core.engine.groovy-pre-processor.location=classpath:GroovyPreProcessor.groovy",
        "cas.authn.core.engine.groovy-post-processor.location=classpath:GroovyPostProcessor.groovy",
        "cas.authn.authentication-attribute-release.enabled=false",
        "cas.authn.attribute-repository.core.expiration-time=0",
        "cas.authn.policy.source-selection-enabled=true"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
@AutoConfigureObservability
class AuthenticationAttributeReleasePolicyTests {
    @Autowired
    @Qualifier(AuthenticationManager.BEAN_NAME)
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_GLOBAL_PRINCIPAL_ATTRIBUTE_REPOSITORY)
    private RegisteredServicePrincipalAttributesRepository globalPrincipalAttributeRepository;

    @Autowired
    @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
    private AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    @Autowired
    @Qualifier("groovyAuthenticationHandlerResolver")
    private AuthenticationHandlerResolver groovyAuthenticationHandlerResolver;

    @Autowired
    @Qualifier("groovyAuthenticationProcessorExecutionPlanConfigurer")
    private AuthenticationEventExecutionPlanConfigurer groovyAuthenticationProcessorExecutionPlanConfigurer;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() throws Throwable{
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("185.86.151.11").setLocalAddr("185.86.151.11").setClientInfo();
        
        assertNotNull(groovyAuthenticationHandlerResolver);
        assertNotNull(globalPrincipalAttributeRepository);
        assertNotNull(authenticationManager);
        assertNotNull(groovyAuthenticationProcessorExecutionPlanConfigurer);
        
        val attributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(
            CoreAuthenticationTestUtils.getAuthentication(), mock(Assertion.class),
            Map.of(), CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(attributes.isEmpty());

        assertThrows(AuthenticationException.class, () -> authenticationManager.authenticate(CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())));
    }

    @TestConfiguration(value = "CasCoreAuthenticationAutoConfigurationTestConfiguration", proxyBeanMethods = false)
    static class CasCoreAuthenticationSupportTestConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = AuthenticationServiceSelectionPlan.BEAN_NAME)
        public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan() {
            return mock(AuthenticationServiceSelectionPlan.class);
        }
    }
}
