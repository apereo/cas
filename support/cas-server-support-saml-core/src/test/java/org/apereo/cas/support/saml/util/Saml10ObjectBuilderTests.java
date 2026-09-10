package org.apereo.cas.support.saml.util;

import module java.base;
import org.apereo.cas.config.BaseSamlConfigurationTests;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.saml.saml2.core.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link Saml10ObjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML1")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSamlConfigurationTests.SharedTestConfiguration.class)
class Saml10ObjectBuilderTests {
    private static final String RECIPIENT = "https://app.example.org/cas-callback";

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private OpenSamlConfigBean openSamlConfigBean;

    @Test
    void verifyOperation() {
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

    @Test
    void verifyResponseNamesItsRecipientAndEchoesTheRequestId() {
        val saml10ObjectBuilder = new Saml10ObjectBuilder(this.openSamlConfigBean);
        val requestId = '_' + UUID.randomUUID().toString();
        val samlService = new SamlService();
        samlService.setRequestId(requestId);

        val response = saml10ObjectBuilder.newResponse(UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()), RECIPIENT, samlService);

        assertEquals(RECIPIENT, response.getRecipient());
        assertEquals(requestId, response.getInResponseTo(),
            "InResponseTo must reference the request being answered, never the recipient");
    }

    @Test
    void verifyInResponseToIsOmittedWhenTheRequestCarriedNoIdentifier() {
        val saml10ObjectBuilder = new Saml10ObjectBuilder(this.openSamlConfigBean);
        val response = saml10ObjectBuilder.newResponse(UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()), RECIPIENT, new SamlService());

        assertEquals(RECIPIENT, response.getRecipient());
        assertNull(response.getInResponseTo());
    }

    @Test
    void verifyRecipientIsOmittedWhenUndefined() {
        val saml10ObjectBuilder = new Saml10ObjectBuilder(this.openSamlConfigBean);
        val response = saml10ObjectBuilder.newResponse(UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()), null, new SamlService());

        assertNull(response.getRecipient());
        assertNull(response.getInResponseTo());
    }
}
