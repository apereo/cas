package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepositoryTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is {@link OneTimeTokenAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowEvents")
@SpringBootTest(classes = BaseOneTimeTokenRepositoryTests.SharedTestConfiguration.class)
public class OneTimeTokenAuthenticationWebflowEventResolverTests {
    @Autowired
    @Qualifier("oneTimeTokenAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver resolver;

    @Test
    public void verifyResolver() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val authn = RegisteredServiceTestUtils.getAuthentication("casuser");
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authn));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authn, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolveSingle(context).getId());
    }
}
