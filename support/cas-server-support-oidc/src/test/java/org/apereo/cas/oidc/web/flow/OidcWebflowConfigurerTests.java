package org.apereo.cas.oidc.web.flow;

import module java.base;
import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.config.CasOAuth20WebflowAutoConfiguration;
import org.apereo.cas.config.CasOidcAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThrottlingAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.throttle.ThrottledRequestFilter;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
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
@ImportAutoConfiguration({
    CasThymeleafAutoConfiguration.class,
    CasThrottlingAutoConfiguration.class,
    CasThemesAutoConfiguration.class,
    CasOidcAutoConfiguration.class,
    CasOAuth20AutoConfiguration.class,
    CasOAuth20WebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasAccountManagementWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
})
@TestPropertySource(properties = {
    "CasFeatureModule.AccountManagement.enabled=true",
    "cas.authn.oidc.jwks.file-system.jwks-file=classpath:keystore.jwks"
})
@Tag("OIDCWeb")
@ExtendWith(CasTestExtension.class)
class OidcWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier("oidcThrottledRequestFilter")
    private ThrottledRequestFilter oidcThrottledRequestFilter;

    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        val request = new MockHttpServletRequest();
        request.setServerPort(8080);
        request.setRequestURI("/cas/oidc/" + OidcConstants.AUTHORIZE_URL);
        val response = new MockHttpServletResponse();
        assertTrue(oidcThrottledRequestFilter.supports(request, response));
    }

    @ParameterizedTest
    @MethodSource("getThrottledEndpoints")
    void verifyEncodedEndpointPathsAreThrottled(final String endpoint) {
        val encodedEndpoint = "%%%02x%s".formatted((int) endpoint.charAt(0), endpoint.substring(1));
        val request = new MockHttpServletRequest();
        request.setServerPort(8080);
        request.setRequestURI("/cas/oidc/" + encodedEndpoint);
        val response = new MockHttpServletResponse();
        assertTrue(oidcThrottledRequestFilter.supports(request, response));
    }

    static Stream<String> getThrottledEndpoints() {
        return Stream.of(
            OidcConstants.AUTHORIZE_URL,
            OidcConstants.ACCESS_TOKEN_URL,
            OidcConstants.TOKEN_URL,
            OidcConstants.PROFILE_URL,
            OidcConstants.JWKS_URL,
            OidcConstants.CLIENT_CONFIGURATION_URL,
            OidcConstants.REVOCATION_URL,
            OidcConstants.INTROSPECTION_URL);
    }
}
