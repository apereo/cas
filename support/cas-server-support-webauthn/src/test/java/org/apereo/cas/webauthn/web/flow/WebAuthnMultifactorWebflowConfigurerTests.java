package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.webauthn.WebAuthnMultifactorAttestationTrustSourceFidoProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.web-authn.core.trust-source.fido.legal-header=" + WebAuthnMultifactorAttestationTrustSourceFidoProperties.DEFAULT_LEGAL_HEADER,
        "cas.authn.mfa.web-authn.core.allowed-origins=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.application-id=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.relying-party-name=CAS WebAuthn Demo",
        "cas.authn.mfa.web-authn.core.relying-party-id=example.org",
        "cas.authn.mfa.web-authn.core.allow-primary-authentication=true"
    })
@Tag("WebflowMfaConfig")
@ExtendWith(CasTestExtension.class)
@Getter
class WebAuthnMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
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
    void verifyCsrfOperation() throws Throwable {
        val webAuthnFlow = (Flow) flowDefinitionRegistry.getFlowDefinition(WebAuthnMultifactorWebflowConfigurer.FLOW_ID_MFA_WEBAUTHN);
        webAuthnFlow.setApplicationContext(applicationContext);
        val context = MockRequestContext.create(applicationContext);
        context.setActiveFlow(webAuthnFlow);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);

        val registration = (ViewState) webAuthnFlow.getState(CasWebflowConstants.STATE_ID_WEBAUTHN_VIEW_REGISTRATION);
        registration.enter(context);
        assertNotNull(context.getFlowScope().get("_csrf"));
    }
}
