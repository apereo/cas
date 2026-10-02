package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
@Import({
    BaseWebflowConfigurerTests.SharedTestConfiguration.class,
    AcceptPasswordlessAuthenticationActionTests.AcceptPasswordlessAuthenticationTestConfiguration.class
})
@Tag("WebflowAuthenticationActions")
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org",
    "cas.authn.passwordless.accounts.simple.casuser2=casuser2@example.org",
    "cas.authn.passwordless.accounts.simple.casuser3=casuser3@example.org"
})
class AcceptPasswordlessAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    private static final Map<String, CyclicBarrier> REDEMPTION_BARRIERS = new ConcurrentHashMap<>();

    private static final Queue<AuditActionContext> AUDIT_RECORDS = new ConcurrentLinkedQueue<>();

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
    void verifyWrongTokenIsAudited() throws Throwable {
        val username = "casuser-" + UUID.randomUUID();
        createToken(username);
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);
        putAccountInto(context, username);
        context.setParameter("token", UUID.randomUUID().toString());
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, acceptPasswordlessAuthenticationAction.execute(context).getId());
        assertTrue(passwordlessTokenRepository.findToken(username).isPresent());
        assertTrue(AUDIT_RECORDS.stream().anyMatch(record -> username.equals(record.getPrincipal())
            && "AUTHENTICATION_FAILED".equals(record.getActionPerformed())));
    }

    @Test
    void verifyMissingTokenAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

        putAccountInto(context, "casuser3");
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, acceptPasswordlessAuthenticationAction.execute(context).getId());
    }

    @Test
    void verifyTokenRedeemedOnceUnderConcurrency() throws Throwable {
        val username = "casuser-" + UUID.randomUUID();
        val token = createToken(username);
        REDEMPTION_BARRIERS.put(username, new CyclicBarrier(2));
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val results = new ArrayList<Future<String>>();
            for (var i = 0; i < 2; i++) {
                results.add(executor.submit(() -> {
                    val context = MockRequestContext.create(applicationContext);
                    context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);
                    putAccountInto(context, username);
                    context.setParameter("token", token.getToken());
                    return acceptPasswordlessAuthenticationAction.execute(context).getId();
                }));
            }
            var successes = 0;
            for (val result : results) {
                if (CasWebflowConstants.TRANSITION_ID_SUCCESS.equals(result.get())) {
                    successes++;
                }
            }
            assertEquals(1, successes);
        } finally {
            REDEMPTION_BARRIERS.remove(username);
        }
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

    @TestConfiguration(value = "AcceptPasswordlessAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AcceptPasswordlessAuthenticationTestConfiguration {
        @Bean
        public AuditTrailExecutionPlanConfigurer passwordlessAuditTrailExecutionPlanConfigurer() {
            return plan -> plan.registerAuditTrailManager(AUDIT_RECORDS::add);
        }

        @Bean
        public PasswordlessAuthenticationPreProcessor concurrentRedemptionPreProcessor() {
            return (builder, account, service, credential, token) -> {
                val barrier = REDEMPTION_BARRIERS.get(account.getUsername());
                if (barrier != null) {
                    FunctionUtils.doAndHandle(() -> barrier.await(5, TimeUnit.SECONDS));
                }
                return builder;
            };
        }
    }
}
