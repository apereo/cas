package org.apereo.cas.syncope.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.syncope.BaseSyncopeTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SyncopeWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 18080)
@Import(BaseSyncopeTests.SharedTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.syncope.provisioning.enabled=true",
    "cas.authn.syncope.provisioning.url=http://localhost:18080/syncope",
    "cas.authn.syncope.provisioning.basic-auth-username=admin",
    "cas.authn.syncope.provisioning.basic-auth-password=password"
})
@Tag("Syncope")
class SyncopeWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SYNCOPE_PRINCIPAL_PROVISIONER_ACTION)
    private Action syncopePrincipalProvisionerAction;

    @Test
    void verifyCreateUpdateOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val username = RandomUtils.randomAlphabetic(8);
        val authentication = RegisteredServiceTestUtils.getAuthentication(username);
        WebUtils.putAuthentication(authentication, context);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, syncopePrincipalProvisionerAction.execute(context).getId());

        val principal = RegisteredServiceTestUtils.getPrincipal(username, Map.of("email", List.of("example@google.com")));
        val newAuthentication = RegisteredServiceTestUtils.getAuthentication(principal);
        WebUtils.putAuthentication(newAuthentication, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, syncopePrincipalProvisionerAction.execute(context).getId());
    }
}
