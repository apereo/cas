package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.data.CredentialRegistration;
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
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;
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
        "cas.authn.mfa.web-authn.core.allow-primary-authentication=true",
        "cas.authn.mfa.web-authn.core.allow-untrusted-attestation=true",
        "cas.authn.mfa.web-authn.core.passkey-upgrade-enabled=true"
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

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WEBAUTHN_CHECK_PASSKEY_UPGRADE)
    private Action webAuthnCheckPasskeyUpgradeAction;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

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

    @Test
    void verifyPasskeyUpgrade() throws Throwable {
        val loginFlow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertTrue(loginFlow.containsState(CasWebflowConstants.STATE_ID_WEBAUTHN_VIEW_PASSKEY_UPGRADE));
        val sendTicketGrantingTicket = (TransitionableState) loginFlow.getState(CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET);
        assertEquals(CasWebflowConstants.STATE_ID_WEBAUTHN_CHECK_PASSKEY_UPGRADE,
            sendTicketGrantingTicket.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId());

        val context = MockRequestContext.create(applicationContext);
        val username = UUID.randomUUID().toString();
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(username), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_NO, webAuthnCheckPasskeyUpgradeAction.execute(context).getId());

        val typedUsername = username.toUpperCase(Locale.ROOT);
        WebUtils.putCredential(context, new UsernamePasswordCredential(typedUsername, "Mellon"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_YES, webAuthnCheckPasskeyUpgradeAction.execute(context).getId());
        assertEquals(typedUsername, context.getFlowScope().get(WebAuthnCheckPasskeyUpgradeAction.FLOW_SCOPE_PASSKEY_UPGRADE_USERNAME));
        assertEquals(username, context.getFlowScope().get(WebAuthnCheckPasskeyUpgradeAction.FLOW_SCOPE_PASSKEY_UPGRADE_DISPLAY_NAME));

        webAuthnCredentialRepository.addRegistrationByUsername(username, CredentialRegistration.builder().build());
        assertEquals(CasWebflowConstants.TRANSITION_ID_NO, webAuthnCheckPasskeyUpgradeAction.execute(context).getId());
    }
}
