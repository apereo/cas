package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationAccountService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseCasSimpleMultifactorSendTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseCasSimpleMultifactorSendTokenActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_SEND_TOKEN)
    protected Action mfaSimpleMultifactorSendTokenAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_VERIFY_EMAIL)
    protected Action mfaSimpleMultifactorVerifyEmailAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_UPDATE_EMAIL)
    protected Action mfaSimpleMultifactorUpdateEmailAction;

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationHandler")
    protected AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    protected TicketFactory defaultTicketFactory;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasSimpleMultifactorAuthenticationAccountService.BEAN_NAME)
    protected ObjectProvider<CasSimpleMultifactorAuthenticationAccountService> accountService;

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationProvider")
    protected MultifactorAuthenticationProvider casSimpleMultifactorAuthenticationProvider;

    protected Pair<String, RequestContext> createToken(final String user) throws Exception {
        val context = buildRequestContextFor(user);
        return executeTokenRequest(context);
    }

    protected Pair<String, RequestContext> executeTokenRequest(final RequestContext context) throws Exception {
        val event = mfaSimpleMultifactorSendTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        return Pair.of(event.getAttributes().getString("token"), context);
    }

    protected MockRequestContext buildRequestContextFor(final Principal principal) throws Exception {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlashScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, casSimpleMultifactorAuthenticationProvider);
        return context;
    }

    protected MockRequestContext buildRequestContextFor(final String user) throws Exception {
        return buildRequestContextFor(user, "cas@example.org", "123456789");
    }

    protected MockRequestContext buildRequestContextFor(final String user, final String email) throws Exception {
        return buildRequestContextFor(user, email, null);
    }

    protected MockRequestContext buildRequestContextFor(final String user, final String email,
                                                        final String phone) throws Exception {
        val attributes = new HashMap<String, List<Object>>();
        if (StringUtils.isNotBlank(email)) {
            attributes.put("mail", CollectionUtils.wrap(email));
        }
        if (StringUtils.isNotBlank(phone)) {
            attributes.put("phone", CollectionUtils.wrap(phone));
        }
        val principal = RegisteredServiceTestUtils.getPrincipal(user, attributes);
        return buildRequestContextFor(principal);
    }
}
