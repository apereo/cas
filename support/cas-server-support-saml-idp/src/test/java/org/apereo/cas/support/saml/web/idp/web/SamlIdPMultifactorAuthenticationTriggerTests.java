package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.web.flow.BaseSamlIdPWebflowTests;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFATrigger")
@Import(SamlIdPMultifactorAuthenticationTriggerTests.MultifactorTestConfiguration.class)
@TestPropertySource(properties = "cas.authn.saml-idp.core.authentication-context-class-mappings=context1->mfa-dummy")
public class SamlIdPMultifactorAuthenticationTriggerTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier("samlIdPMultifactorAuthenticationTrigger")
    private MultifactorAuthenticationTrigger samlIdPMultifactorAuthenticationTrigger;

    @Test
    public void verifyContextMapping() throws Exception {
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        
        val authnRequest = SamlIdPTestUtils.getAuthnRequest(openSamlConfigBean, registeredService);
        var builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        val classRef = (AuthnContextClassRef) builder.buildObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        classRef.setURI("context1");
        builder = (SAMLObjectBuilder) openSamlConfigBean.getBuilderFactory()
            .getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        val reqCtx = (RequestedAuthnContext) builder.buildObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        reqCtx.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        reqCtx.getAuthnContextClassRefs().add(classRef);
        authnRequest.setRequestedAuthnContext(reqCtx);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val messageContext = new MessageContext();
        messageContext.setMessage(authnRequest);
        val context = Pair.of(authnRequest, messageContext);
        SamlIdPUtils.storeSamlRequest(new JEEContext(request, response), openSamlConfigBean,
            samlIdPDistributedSessionStore, context);
        
        assertTrue(samlIdPMultifactorAuthenticationTrigger.supports(request, registeredService,
            RegisteredServiceTestUtils.getAuthentication(), service));
        val result = samlIdPMultifactorAuthenticationTrigger.isActivated(RegisteredServiceTestUtils.getAuthentication(),
            registeredService, request, service);
        assertTrue(result.isPresent());
    }

    @TestConfiguration("MultifactorTestConfiguration")
    public static class MultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider("mfa-dummy");
        }
    }
}
