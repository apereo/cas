package org.apereo.cas.support.saml.util;

import org.apereo.cas.config.CoreSamlConfigurationTests;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.authentication.principal.SamlService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link Saml10ObjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
@SpringBootTest(classes = CoreSamlConfigurationTests.SharedTestConfiguration.class)
public class Saml10ObjectBuilderTests {
    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private OpenSamlConfigBean openSamlConfigBean;

    @Test
    public void verifyOperation() {
        val saml10ObjectBuilder = new Saml10ObjectBuilder(this.openSamlConfigBean);
        var result = saml10ObjectBuilder.newStatus(StatusCode.DEFAULT_ELEMENT_NAME);
        assertNotNull(result);

        val samlService = new SamlService();
        samlService.setRequestId(UUID.randomUUID().toString());
        val response = saml10ObjectBuilder.newResponse(UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()), UUID.randomUUID().toString(),
            samlService);
        assertNotNull(response);

    }
}
