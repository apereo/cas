package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.support.spnego.MockJcifsAuthentication;
import org.apereo.cas.support.spnego.util.SpnegoConstants;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import jcifs.spnego.Authentication;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.webflow.action.EventFactorySupport;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SpnegoCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Import(SpnegoCredentialsActionTests.SpnegoAuthenticationTestConfiguration.class)
@Tag("Spnego")
class SpnegoCredentialsActionTests extends AbstractSpnegoTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader(HttpHeaders.AUTHORIZATION,
            SpnegoConstants.NEGOTIATE + ' ' + EncodingUtils.encodeBase64("credential"));
        spnegoAction.execute(context);
        assertNotNull(context.getHttpServletResponse().getHeader(SpnegoConstants.HEADER_AUTHENTICATE));
    }

    @Test
    void verifyNoAuthzHeader() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, spnegoAction.execute(context).getId());
    }

    @Test
    void verifyErrorWithBadCredential() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader(HttpHeaders.AUTHORIZATION,
            SpnegoConstants.NEGOTIATE + ' ' + EncodingUtils.encodeBase64("credential"));
        val stResolver = mock(CasWebflowEventResolver.class);
        val err = new EventFactorySupport().error(this);
        when(stResolver.resolveSingle(any())).thenReturn(err);
        val adaptive = mock(AdaptiveAuthenticationPolicy.class);
        when(adaptive.isAuthenticationRequestAllowed(any(), anyString(), any())).thenReturn(false);
        val action = new SpnegoCredentialsAction(mock(CasDelegatingWebflowEventResolver.class),
            stResolver, adaptive, false);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, action.execute(context).getId());
    }

    @Test
    void verifyBadAuthorizationHeader() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader(HttpHeaders.AUTHORIZATION, "XYZ");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, spnegoAction.execute(context).getId());
    }

    @TestConfiguration(value = "SpnegoAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class SpnegoAuthenticationTestConfiguration {
        @Bean
        public BlockingQueue<List<Authentication>> spnegoAuthenticationsPool() {
            val queue = new ArrayBlockingQueue<List<Authentication>>(1);
            queue.add(CollectionUtils.wrapList(new MockJcifsAuthentication()));
            return queue;
        }
    }
}
