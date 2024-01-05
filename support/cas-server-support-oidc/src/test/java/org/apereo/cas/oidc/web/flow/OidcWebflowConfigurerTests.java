package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.config.CasOAuth20WebflowAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThrottlingAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.OidcAutoConfiguration;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.throttle.ThrottledRequestFilter;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
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
    CasThymeleafAutoConfiguration.class,
    CasThrottlingAutoConfiguration.class,
    CasThemesAutoConfiguration.class,
    OidcAutoConfiguration.class,
    CasOAuth20AutoConfiguration.class,
    CasOAuth20WebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
})
@TestPropertySource(properties = {
    "spring.mvc.pathmatch.matching-strategy=ant-path-matcher",
    "cas.authn.oidc.jwks.file-system.jwks-file=classpath:keystore.jwks"
})
@Tag("OIDC")
class OidcWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier("oidcThrottledRequestFilter")
    private ThrottledRequestFilter oidcThrottledRequestFilter;

    @Test
    void verifyOperation() throws Throwable {
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
