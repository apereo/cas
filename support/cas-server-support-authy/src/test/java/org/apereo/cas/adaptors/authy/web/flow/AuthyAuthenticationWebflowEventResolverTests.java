package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.AuthyTokenCredential;
import org.apereo.cas.adaptors.authy.BaseAuthyAuthenticationTests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
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
 * This is {@link AuthyAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowEvents")
@SpringBootTest(classes = BaseAuthyAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.authy.api-key=example",
        "cas.authn.mfa.authy.api-url=http://localhost:8080/authy"
    })
public class AuthyAuthenticationWebflowEventResolverTests {
    private final RequestContext context = mock(RequestContext.class);

    @Autowired
    @Qualifier("authyAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver resolver;

    @BeforeEach
    public void beforeAll() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putCredential(context, new AuthyTokenCredential("token"));
    }

    @Test
    public void verifyOperation() {
        val authn = RegisteredServiceTestUtils.getAuthentication("casuser");
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authn));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authn, context);

        val event = resolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        val support = new EventFactorySupport();
        assertTrue(event.getAttributes().contains(support.getExceptionAttributeName()));
    }

}
