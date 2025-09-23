package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.Client;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedClientAuthenticationWebflowStateContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
class DefaultDelegatedClientAuthenticationWebflowStateContributorTests {
    @Autowired
    @Qualifier("defaultDelegatedClientAuthenticationWebflowStateContributor")
    private DelegatedClientAuthenticationWebflowStateContributor contributor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
    private DelegatedIdentityProviders identityProviders;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyForceAuthnWhenSsoDisabled() throws Throwable {
        val requestContext = MockRequestContext.create(applicationContext);
        val httpServletResponse = requestContext.getHttpServletResponse();
        val httpServletRequest = requestContext.getHttpServletRequest();
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        requestContext.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setSsoEnabled(false));
        servicesManager.save(registeredService);

        val context = new JEEContext(httpServletRequest, httpServletResponse);

        val client = identityProviders.findClient("CasClient", context).orElseThrow();
        val details = contributor.store(requestContext, context, client);
        assertTrue(details.containsKey(CasProtocolConstants.PARAMETER_SERVICE));
        assertTrue(details.containsKey(CasProtocolConstants.PARAMETER_TARGET_SERVICE));
        assertTrue(details.containsKey(Client.class.getName()));
        assertTrue(details.containsKey(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN));
    }

}
