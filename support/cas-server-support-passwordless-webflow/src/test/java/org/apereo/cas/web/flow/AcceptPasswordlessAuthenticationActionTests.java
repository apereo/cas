package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptPasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org",
    "cas.authn.passwordless.accounts.simple.casuser2=casuser2@example.org",
    "cas.authn.passwordless.accounts.simple.casuser3=casuser3@example.org"
})
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

        putAccountInto(context, "casuser");
        val token = createToken("casuser");

        context.setParameter("token", token.getToken());

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, acceptPasswordlessAuthenticationAction.execute(context).getId());
        assertTrue(passwordlessTokenRepository.findToken("casuser").isEmpty());
    }

    @Test
    void verifyUnknownToken() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

        putAccountInto(context, "casuser2");
        createToken("casuser2");

        context.setParameter("token", UUID.randomUUID().toString());

        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, acceptPasswordlessAuthenticationAction.execute(context).getId());
    }

    @Test
    void verifyMissingTokenAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

        putAccountInto(context, "casuser3");
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, acceptPasswordlessAuthenticationAction.execute(context).getId());
    }

    /**
     * The token repository is keyed by principal and shared by this class, so each test works on an
     * account of its own rather than competing for the tokens issued to a single one.
     *
     * @param context  the request context
     * @param username the account to place into the flow
     * @return the account
     */
    private static PasswordlessUserAccount putAccountInto(final MockRequestContext context, final String username) {
        val account = PasswordlessUserAccount.builder()
            .email("email")
            .phone("phone")
            .username(username)
            .name(username)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        return account;
    }

    private PasswordlessAuthenticationToken createToken(final String username) {
        val passwordlessUserAccount = PasswordlessUserAccount.builder().username(username).build();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(username).build();
        val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);
        passwordlessTokenRepository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        return token;
    }
}
