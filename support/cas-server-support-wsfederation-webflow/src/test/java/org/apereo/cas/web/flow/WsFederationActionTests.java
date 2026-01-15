package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseWsFederationWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.wsfed[0].identity-provider-url=https://example.org/adfs/ls/",
        "cas.authn.wsfed[0].identity-provider-identifier=https://example.org/adfs/services/trust",
        "cas.authn.wsfed[0].relying-party-identifier=urn:cas:example",
        "cas.authn.wsfed[0].signing-certificate-resources=classpath:adfs-signing.crt",
        "cas.authn.wsfed[0].identity-attribute=upn"
    })
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
class WsFederationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WS_FEDERATION)
    protected Action wsFederationAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyRequestOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        wsFederationAction.execute(context);
        assertFalse(WebUtils.getWsFederationDelegatedClients(context, WsFedClient.class).isEmpty());
    }
}
