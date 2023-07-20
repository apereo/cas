package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConsentAccountProfilePrepareActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAccountActions")
@Import(CasWebflowAccountProfileConfiguration.class)
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
class ConsentAccountProfilePrepareActionTests extends BaseConsentActionTests {
    @Autowired
    @Qualifier(ConsentDecisionBuilder.BEAN_NAME)
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_CONSENT_ACCOUNT_PROFILE_PREPARE)
    private Action consentAccountProfilePrepareAction;

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    private ConsentRepository consentRepository;
    
    @Test
    void verifyOperation() throws Exception {
        val uid = UUID.randomUUID().toString();
        val desc = consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService(), uid,
            CoreAuthenticationTestUtils.getAttributes());
        consentRepository.storeConsentDecision(desc);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val authn = RegisteredServiceTestUtils.getAuthentication(uid);
        WebUtils.putAuthentication(authn, context);

        val result = consentAccountProfilePrepareAction.execute(context);
        assertNull(result);
        assertTrue(context.getFlowScope().contains("consentDecisions"));
    }
}
