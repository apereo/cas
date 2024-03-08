package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptPasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
@TestPropertySource(properties = "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org")
@Execution(ExecutionMode.SAME_THREAD)
class AcceptPasswordlessAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN)
    private Action acceptPasswordlessAuthenticationAction;

    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

        putAccountInto(context);
        val token = createToken();

        context.setParameter("token", token.getToken());

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, acceptPasswordlessAuthenticationAction.execute(context).getId());
        assertTrue(passwordlessTokenRepository.findToken("casuser").isEmpty());
    }

    @Test
    void verifyUnknownToken() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

        putAccountInto(context);
        createToken();

        context.setParameter("token", UUID.randomUUID().toString());

        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, acceptPasswordlessAuthenticationAction.execute(context).getId());
    }

    @Test
    void verifyMissingTokenAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

        putAccountInto(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, acceptPasswordlessAuthenticationAction.execute(context).getId());
    }

    private static PasswordlessUserAccount putAccountInto(final MockRequestContext context) {
        val account = PasswordlessUserAccount.builder()
            .email("email")
            .phone("phone")
            .username("casuser")
            .name("casuser")
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        return account;
    }

    private PasswordlessAuthenticationToken createToken() {
        val passwordlessUserAccount = PasswordlessUserAccount.builder().username("casuser").build();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username("casuser").build();
        val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);
        passwordlessTokenRepository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        return token;
    }
}
