package org.apereo.cas.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Set;

import javax.security.auth.login.AccountNotFoundException;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import lombok.val;

public class SurrogateAuthenticationExceptionHandlerActionTests {

    @Test
    public void handleAuthenticationNonSurrogateCredential() {
        val ctx = mock(RequestContext.class);
        when(ctx.getMessageContext()).thenReturn(mock(MessageContext.class));
        RememberMeUsernamePasswordCredential rememberMeUsernamePasswordCredential = new RememberMeUsernamePasswordCredential();
        rememberMeUsernamePasswordCredential.setUsername("casuser");
        rememberMeUsernamePasswordCredential.setPassword("mellon");

        val requestScope = new LocalAttributeMap<Object>();
        requestScope.put("credential", rememberMeUsernamePasswordCredential);
        when(ctx.getRequestScope()).thenReturn(requestScope);
        when(ctx.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(ctx.getConversationScope()).thenReturn(new LocalAttributeMap<>());

        val map = new HashMap<String, Throwable>();
        map.put("notFound", new AccountNotFoundException());
        val handler = new SurrogateAuthenticationExceptionHandlerAction(Set.of(AccountNotFoundException.class),
            MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);

        val id = handler.handleAuthenticationException(new AuthenticationException(map), ctx);
        assertEquals(AccountNotFoundException.class.getSimpleName(), id);
        assertTrue(WebUtils.getCredential(ctx) instanceof RememberMeUsernamePasswordCredential);
    }

    @Test
    public void handleAuthenticationSurrogateCredential() {
        val ctx = mock(RequestContext.class);
        when(ctx.getMessageContext()).thenReturn(mock(MessageContext.class));
        SurrogateUsernamePasswordCredential surrogateCredential = new SurrogateUsernamePasswordCredential();
        surrogateCredential.setUsername("casuser");
        surrogateCredential.setPassword("mellon");
        surrogateCredential.setSurrogateUsername("surrogate");

        val requestScope = new LocalAttributeMap<Object>();
        requestScope.put("credential", surrogateCredential);
        when(ctx.getRequestScope()).thenReturn(requestScope);
        when(ctx.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(ctx.getConversationScope()).thenReturn(new LocalAttributeMap<>());

        val map = new HashMap<String, Throwable>();
        map.put("notFound", new AccountNotFoundException());
        val handler = new SurrogateAuthenticationExceptionHandlerAction(Set.of(AccountNotFoundException.class),
            MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);

        val id = handler.handleAuthenticationException(new AuthenticationException(map), ctx);
        assertEquals(AccountNotFoundException.class.getSimpleName(), id);
        assertTrue(WebUtils.getCredential(ctx) instanceof RememberMeUsernamePasswordCredential);
    }

}
