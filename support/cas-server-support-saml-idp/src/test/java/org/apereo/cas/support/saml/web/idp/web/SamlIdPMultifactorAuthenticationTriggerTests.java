package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.web.flow.BaseSamlIdPWebflowTests;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFATrigger")
@Import(SamlIdPMultifactorAuthenticationTriggerTests.MultifactorTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml4944",
    "cas.authn.saml-idp.core.context.authentication-context-class-mappings=context1->mfa-dummy"
})
class SamlIdPMultifactorAuthenticationTriggerTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier("samlIdPMultifactorAuthenticationTrigger")
    private MultifactorAuthenticationTrigger samlIdPMultifactorAuthenticationTrigger;

    @Test
    void verifyContextMapping() throws Throwable {
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
        request.addParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID());
        val response = new MockHttpServletResponse();

        val messageContext = new MessageContext();
        signAuthnRequest(request, response, authnRequest, registeredService, messageContext);
        messageContext.setMessage(authnRequest);
        val context = Pair.of(authnRequest, messageContext);

        SamlIdPSessionManager.of(openSamlConfigBean, samlIdPDistributedSessionStore)
            .store(new JEEContext(request, response), context);
        assertTrue(samlIdPMultifactorAuthenticationTrigger.supports(request, registeredService,
            RegisteredServiceTestUtils.getAuthentication(), service));
        val result = samlIdPMultifactorAuthenticationTrigger.isActivated(RegisteredServiceTestUtils.getAuthentication(),
            registeredService, request, response, service);
        assertTrue(result.isPresent());
    }

    @TestConfiguration(value = "MultifactorTestConfiguration", proxyBeanMethods = false)
    static class MultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider("mfa-dummy");
        }

        @Bean
        public SamlObjectSignatureValidator samlObjectSignatureValidator() throws Throwable {
            val mockValidator = mock(SamlObjectSignatureValidator.class);
            when(mockValidator.verifySamlProfileRequest(any(), any(MetadataResolver.class), any(), any())).thenReturn(Boolean.TRUE);
            when(mockValidator.verifySamlProfileRequest(any(), any(SamlRegisteredServiceMetadataAdaptor.class),
                any(), any(MessageContext.class))).thenReturn(Boolean.TRUE);
            return mockValidator;
        }

    }
}
