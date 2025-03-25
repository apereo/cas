package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.CasSamlIdentityProviderDiscoveryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.saml2.BaseSaml2DelegatedAuthenticationTests;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class,
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
        val httpServletRequest = new MockHttpServletRequest();
        assertFalse(controller.getDiscoveryFeed(StringUtils.EMPTY).isEmpty());
        assertFalse(controller.getDiscoveryFeed("https://cas.example.org/idp").isEmpty());
        assertNotNull(controller.home(httpServletRequest));
        assertNotNull(controller.redirect("https://cas.example.org/idp", httpServletRequest, new MockHttpServletResponse()));

        assertThrows(UnauthorizedServiceException.class, () -> {
            httpServletRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "https://service.example");

            val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
            val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
            policy.setAllowedProviders(List.of("OtherClient"));
            policy.setPermitUndefined(false);
            accessStrategy.setDelegatedAuthenticationPolicy(policy);
            val service = RegisteredServiceTestUtils.getRegisteredService("https://service.example");
            service.setAccessStrategy(accessStrategy);
            servicesManager.save(service);
            controller.redirect("https://cas.example.org/idp", httpServletRequest, new MockHttpServletResponse());
        });
    }
}
