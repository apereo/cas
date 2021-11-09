package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseCasSimpleMultifactorSendTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
public abstract class BaseCasSimpleMultifactorSendTokenActionTests {
    @Autowired
    @Qualifier("mfaSimpleMultifactorSendTokenAction")
    protected Action mfaSimpleMultifactorSendTokenAction;

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationHandler")
    protected AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationProvider")
    protected MultifactorAuthenticationProvider casSimpleMultifactorAuthenticationProvider;

    protected Pair<String, RequestContext> createToken(final String user) throws Exception {
        val context = buildRequestContextFor(user);
        val event = mfaSimpleMultifactorSendTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        return Pair.of(event.getAttributes().getString("token"), context);
    }

    protected MockRequestContext buildRequestContextFor(final String user) {
        val context = new MockRequestContext();
        val messageContext = (DefaultMessageContext) context.getMessageContext();
        messageContext.setMessageSource(mock(MessageSource.class));

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        WebUtils.putServiceIntoFlashScope(context, RegisteredServiceTestUtils.getService());

        val principal = RegisteredServiceTestUtils.getPrincipal(user,
            CollectionUtils.wrap("phone", List.of("123456789"), "mail", List.of("cas@example.org")));
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, casSimpleMultifactorAuthenticationProvider);
        return context;
    }
}
