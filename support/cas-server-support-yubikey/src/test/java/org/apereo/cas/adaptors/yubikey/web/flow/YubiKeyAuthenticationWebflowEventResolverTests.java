package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockParameterMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link YubiKeyAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As="
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowEvents")
public class YubiKeyAuthenticationWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("yubikeyAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver;

    @Test
    public void verifyOperationFails() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = mock(RequestContext.class);
        when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        WebUtils.putCredential(context,
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "123456"));
        val event = yubikeyAuthenticationWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        val support = new EventFactorySupport();
        assertTrue(event.getAttributes().contains(support.getExceptionAttributeName()));
    }

}
