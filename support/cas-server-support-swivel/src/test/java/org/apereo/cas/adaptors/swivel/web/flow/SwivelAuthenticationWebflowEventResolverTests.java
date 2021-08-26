package org.apereo.cas.adaptors.swivel.web.flow;

import org.apereo.cas.adaptors.swivel.BaseSwivelAuthenticationTests;
import org.apereo.cas.adaptors.swivel.SwivelTokenCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
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
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SwivelAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowEvents")
@SpringBootTest(classes = BaseSwivelAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.swivel.swivel-url=http://localhost:9191",
        "cas.authn.mfa.swivel.shared-secret=$ecret",
        "cas.authn.mfa.swivel.ignore-ssl-errors=true",
        "cas.authn.mfa.swivel.trusted-device-enabled=true",
        "cas.authn.mfa.trusted.core.device-registration-enabled=true"
    })
public class SwivelAuthenticationWebflowEventResolverTests {
    @Autowired
    @Qualifier("swivelAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver resolver;

    @Test
    public void verifyResolver() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val authn = RegisteredServiceTestUtils.getAuthentication("casuser");
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authn));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authn, context);
        WebUtils.putCredential(context, new SwivelTokenCredential("token"));
        val event = resolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        val support = new EventFactorySupport();
        assertTrue(event.getAttributes().contains(support.getExceptionAttributeName()));
    }
}
