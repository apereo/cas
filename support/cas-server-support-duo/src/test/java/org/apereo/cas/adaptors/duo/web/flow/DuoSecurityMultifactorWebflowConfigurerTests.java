package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link DuoSecurityMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseDuoSecurityTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
        "cas.authn.mfa.duo[0].duo-application-key=abcdefghijklmnop",
        "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com",
        "cas.authn.mfa.duo[0].trusted-device-enabled=true",
        "cas.authn.mfa.trusted.core.device-registration-enabled=true"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowConfig")
public class DuoSecurityMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    protected FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry() {
        return this.applicationContext.getBean(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, FlowDefinitionRegistry.class);
    }

    @Override
    protected String getMultifactorEventId() {
        return DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER;
    }
}

