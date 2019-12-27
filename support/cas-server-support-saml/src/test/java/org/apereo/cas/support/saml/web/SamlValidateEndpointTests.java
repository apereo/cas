package org.apereo.cas.support.saml.web;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.SamlConfiguration;
import org.apereo.cas.config.authentication.support.SamlAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.authentication.support.SamlServiceFactoryConfiguration;
import org.apereo.cas.config.authentication.support.SamlUniqueTicketIdGeneratorConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlValidateEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    AbstractCasEndpointTests.SharedTestConfiguration.class,
    ThymeleafAutoConfiguration.class,
    CoreSamlConfiguration.class,
    CasValidationConfiguration.class,
    CasProtocolViewsConfiguration.class,
    SamlServiceFactoryConfiguration.class,
    SamlUniqueTicketIdGeneratorConfiguration.class,
    SamlAuthenticationEventExecutionPlanConfiguration.class,
    SamlConfiguration.class
},
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.samlValidate.enabled=true"
    })
@Tag("SAML")
public class SamlValidateEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("samlValidateEndpoint")
    private SamlValidateEndpoint samlValidateEndpoint;

    @Test
    public void verifyOperation() {
        val results = samlValidateEndpoint.handle("sample", "sample",
            CoreAuthenticationTestUtils.getService().getId());
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
