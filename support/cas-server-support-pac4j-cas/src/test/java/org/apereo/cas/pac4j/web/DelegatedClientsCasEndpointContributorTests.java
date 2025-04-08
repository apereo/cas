package org.apereo.cas.pac4j.web;

import org.apereo.cas.config.CasDelegatedAuthenticationCasAutoConfiguration;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.BaseClient;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientsCasEndpointContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTest(
    classes = {
        CasDelegatedAuthenticationCasAutoConfiguration.class,
        BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
    },
    properties = {
        "cas.authn.pac4j.cas[0].login-url=https://login.example.org/login",
        "cas.authn.pac4j.cas[0].protocol=SAML",
        "cas.authn.pac4j.cas[0].principal-id-attribute=uid",
        "cas.authn.pac4j.cas[0].css-class=cssClass",
        "cas.authn.pac4j.cas[0].display-name=My CAS",
        "cas.authn.pac4j.core.lazy-init=true"
    })
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
class DelegatedClientsCasEndpointContributorTests {
    @Autowired
    @Qualifier("delegatedClientsCasEndpointContributor")
    private DelegatedClientsEndpointContributor delegatedClientsOidcEndpointContributor;

    @Autowired
    @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
    private DelegatedIdentityProviders identityProviders;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext).withUserAgent();
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val casClient = identityProviders.findClient("CasClient", webContext).map(BaseClient.class::cast).orElseThrow();
        assertTrue(delegatedClientsOidcEndpointContributor.supports(casClient));
        assertFalse(delegatedClientsOidcEndpointContributor.contribute(casClient).isEmpty());
    }
}
