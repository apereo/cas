package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
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
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockParameterMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusAuthenticationWebflowEventResolverFailureTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseRadiusMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.radius.client.shared-secret=NoSecret",
        "cas.authn.radius.client.inet-address=localhost,localguest",
        "cas.authn.mfa.radius.allowed-authentication-attempts=2",
        "cas.authn.mfa.radius.client.shared-secret=NoSecret",
        "cas.authn.mfa.radius.client.inet-address=localhost,localguest"
    })
@Tag("Radius")
public class RadiusAuthenticationWebflowEventResolverFailureTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("radiusAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver radiusAuthenticationWebflowEventResolver;

    private RequestContext context;

    @BeforeEach
    public void initialize() {
        this.context = mock(RequestContext.class);
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        val request = new MockHttpServletRequest();
        when(context.getExternalContext())
            .thenReturn(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        when(context.getFlowExecutionContext()).thenReturn(
            new MockFlowExecutionContext(new MockFlowSession(new Flow("mockFlow"))));

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authentication));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        WebUtils.putAuthenticationResultBuilder(builder, context);
    }

    @Test
    public void verifyFailsOperation() {
        WebUtils.putCredential(context, new RadiusTokenCredential("token"));

        var event = radiusAuthenticationWebflowEventResolver.resolveSingle(this.context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertTrue(context.getFlowScope().contains(RadiusAuthenticationWebflowEventResolver.FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS));

        event = radiusAuthenticationWebflowEventResolver.resolveSingle(this.context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_CANCEL, event.getId());
        assertFalse(context.getFlowScope().contains(RadiusAuthenticationWebflowEventResolver.FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS));
    }

}
