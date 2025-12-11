package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * This is {@link BaseMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
}, properties = "cas.authn.mfa.groovy-script.location=classpath:/GroovyMfaTrigger.groovy")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseMultifactorAuthenticationTriggerTests {

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationAttributeMultifactorAuthenticationTrigger")
    protected MultifactorAuthenticationTrigger authenticationAttributeMultifactorAuthenticationTrigger;
    
    @Autowired
    @Qualifier("scriptedRegisteredServiceMultifactorAuthenticationTrigger")
    protected MultifactorAuthenticationTrigger scriptedRegisteredServiceMultifactorAuthenticationTrigger;

    @Autowired
    @Qualifier("registeredServicePrincipalAttributeMultifactorAuthenticationTrigger")
    protected MultifactorAuthenticationTrigger registeredServicePrincipalAttributeMultifactorAuthenticationTrigger;

    @Autowired
    @Qualifier("registeredServiceMultifactorAuthenticationTrigger")
    protected MultifactorAuthenticationTrigger registeredServiceMultifactorAuthenticationTrigger;

    @Autowired
    @Qualifier("principalAttributeMultifactorAuthenticationTrigger")
    protected MultifactorAuthenticationTrigger principalAttributeMultifactorAuthenticationTrigger;

    @Autowired
    @Qualifier("globalMultifactorAuthenticationTrigger")
    protected MultifactorAuthenticationTrigger globalMultifactorAuthenticationTrigger;

    @Autowired
    @Qualifier("groovyScriptMultifactorAuthenticationTrigger")
    protected MultifactorAuthenticationTrigger groovyScriptMultifactorAuthenticationTrigger;

    
    @BeforeEach
    void setup() {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("185.88.151.12");
        httpRequest.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        var clientInfo = ClientInfo.from(httpRequest);
        ClientInfoHolder.setClientInfo(clientInfo);
    }

    @TestConfiguration(value = "TestMultifactorTestConfiguration", proxyBeanMethods = false)
    static class TestMultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}
