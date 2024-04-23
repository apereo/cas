package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.CasSamlIdentityProviderDiscoveryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdentityProviderDiscoveryFeedControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
@SpringBootTest(classes = {
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    CasSamlIdentityProviderDiscoveryAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class SamlIdentityProviderDiscoveryFeedControllerTests {
    @Autowired
    @Qualifier("identityProviderDiscoveryFeedController")
    private SamlIdentityProviderDiscoveryFeedController controller;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyFeed() throws Throwable {
        assertFalse(controller.getDiscoveryFeed().isEmpty());
        assertNotNull(controller.home());
        assertNotNull(controller.redirect("https://cas.example.org/idp",
            new MockHttpServletRequest(), new MockHttpServletResponse()));

        assertThrows(UnauthorizedServiceException.class, () -> {
            val request = new MockHttpServletRequest();
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "https://service.example");

            val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
            val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
            policy.setAllowedProviders(List.of("OtherClient"));
            policy.setPermitUndefined(false);
            accessStrategy.setDelegatedAuthenticationPolicy(policy);
            val service = RegisteredServiceTestUtils.getRegisteredService("https://service.example");
            service.setAccessStrategy(accessStrategy);
            servicesManager.save(service);
            controller.redirect("https://cas.example.org/idp", request, new MockHttpServletResponse());
        });
    }
}
