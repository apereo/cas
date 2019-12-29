package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
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
        "cas.authn.mfa.duo[0].duoSecretKey=1234567890",
        "cas.authn.mfa.duo[0].duoApplicationKey=abcdefghijklmnop",
        "cas.authn.mfa.duo[0].duoIntegrationKey=QRSTUVWXYZ",
        "cas.authn.mfa.duo[0].duoApiHost=theapi.duosecurity.com",
        "cas.authn.mfa.duo[0].trustedDeviceEnabled=true",
        "cas.authn.mfa.trusted.deviceRegistrationEnabled=true"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Webflow")
public class DuoSecurityMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    protected FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry() {
        return this.applicationContext.getBean(DuoSecurityMultifactorProperties.DEFAULT_IDENTIFIER, FlowDefinitionRegistry.class);
    }

    @Override
    protected String getMultifactorEventId() {
        return DuoSecurityMultifactorProperties.DEFAULT_IDENTIFIER;
    }
}

