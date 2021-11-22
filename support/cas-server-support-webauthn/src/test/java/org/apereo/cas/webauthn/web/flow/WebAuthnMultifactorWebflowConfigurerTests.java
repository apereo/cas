package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestControlContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.web-authn.core.allowed-origins=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.application-id=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.relying-party-name=CAS WebAuthn Demo",
        "cas.authn.mfa.web-authn.core.relying-party-id=example.org",
        "cas.authn.mfa.web-authn.core.allow-primary-authentication=true"
    })
@Tag("WebflowMfaConfig")
@Getter
public class WebAuthnMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("webAuthnFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;

    @Override
    protected String getMultifactorEventId() {
        return WebAuthnMultifactorWebflowConfigurer.FLOW_ID_MFA_WEBAUTHN;
    }

    @Test
    public void verifyCsrfOperation() {
        val webAuthnFlow = (Flow) loginFlowDefinitionRegistry.getFlowDefinition(WebAuthnMultifactorWebflowConfigurer.FLOW_ID_MFA_WEBAUTHN);
        val context = new MockRequestControlContext(webAuthnFlow);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, webAuthnMultifactorAuthenticationProvider);
        
        val registration = (ViewState) webAuthnFlow.getState(CasWebflowConstants.STATE_ID_WEBAUTHN_VIEW_REGISTRATION);
        registration.enter(context);
        assertNotNull(context.getFlowScope().get("_csrf"));
    }
}
