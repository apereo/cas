package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasOAuth20AuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuth20Configuration;
import org.apereo.cas.config.CasOAuth20EndpointsConfiguration;
import org.apereo.cas.config.CasOAuth20ThrottleConfiguration;
import org.apereo.cas.config.CasOAuth20WebflowConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.config.OidcComponentSerializationConfiguration;
import org.apereo.cas.oidc.config.OidcConfiguration;
import org.apereo.cas.oidc.config.OidcEndpointsConfiguration;
import org.apereo.cas.oidc.config.OidcLogoutConfiguration;
import org.apereo.cas.oidc.config.OidcResponseConfiguration;
import org.apereo.cas.oidc.config.OidcThrottleConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.throttle.ThrottledRequestFilter;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CasThymeleafConfiguration.class,
    CasThemesConfiguration.class,
    OidcConfiguration.class,
    OidcResponseConfiguration.class,
    OidcLogoutConfiguration.class,
    OidcEndpointsConfiguration.class,
    OidcComponentSerializationConfiguration.class,
    OidcThrottleConfiguration.class,
    CasOAuth20Configuration.class,
    CasOAuth20EndpointsConfiguration.class,
    CasOAuth20AuthenticationServiceSelectionStrategyConfiguration.class,
    CasOAuth20ThrottleConfiguration.class,
    CasOAuth20WebflowConfiguration.class,
    CasThrottlingConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = "cas.authn.oidc.jwks.jwks-file=classpath:keystore.jwks")
@Tag("OIDC")
public class OidcWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier("oidcThrottledRequestFilter")
    private ThrottledRequestFilter oidcThrottledRequestFilter;

    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        val request = new MockHttpServletRequest();
        request.setServerPort(8080);
        request.setRequestURI("/cas/oidc/" + OidcConstants.AUTHORIZE_URL);
        val response = new MockHttpServletResponse();
        assertTrue(oidcThrottledRequestFilter.supports(request, response));
    }
}
