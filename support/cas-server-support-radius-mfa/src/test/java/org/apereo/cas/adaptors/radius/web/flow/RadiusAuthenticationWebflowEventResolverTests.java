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
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseRadiusMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.radius.client.shared-secret=NoSecret",
        "cas.authn.radius.client.inet-address=localhost,localguest",
        "cas.authn.mfa.radius.client.shared-secret=NoSecret",
        "cas.authn.mfa.radius.client.inet-address=localhost,localguest"
    })
@Tag("Radius")
public class RadiusAuthenticationWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("radiusAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver radiusAuthenticationWebflowEventResolver;

    private MockRequestContext context;

    @BeforeEach
    public void initialize() {
        context = new MockRequestContext();
        val messageContext = (DefaultMessageContext) context.getMessageContext();
        messageContext.setMessageSource(mock(MessageSource.class));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

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
    public void verifyOperation() {
        val event = radiusAuthenticationWebflowEventResolver.resolveSingle(this.context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    public void verifyFailsOperation() {
        WebUtils.putCredential(context, new RadiusTokenCredential("token"));
        val event = radiusAuthenticationWebflowEventResolver.resolveSingle(this.context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
    }

}
