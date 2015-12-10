package org.jasig.cas.authentication;


import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultAuthenticationContextBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class DefaultAuthenticationContextBuilderTests {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AuthenticationManager manager;
    private DefaultAuthenticationContextBuilder builder;

    @Before
    public void setup() {
        this.builder = new DefaultAuthenticationContextBuilder();
        final DefaultPrincipalElectionStrategy principalStrategy = new DefaultPrincipalElectionStrategy();
        principalStrategy.setPrincipalFactory(new DefaultPrincipalFactory());
        builder.setPrincipalElectionStrategy(principalStrategy);

        final AcceptUsersAuthenticationHandler handler = new AcceptUsersAuthenticationHandler();
        handler.setUsers(Collections.singletonMap("casuser", "Mellon"));
        this.manager = new PolicyBasedAuthenticationManager(handler, new SimpleTestUsernamePasswordAuthenticationHandler());
    }

    @Test
    public void checkBuilderWithSingleAuthN() throws Exception {
        final Authentication authN = manager.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword());
        builder.collect(authN);
        final AuthenticationContext ctx = builder.build();
        assertNotNull(ctx);
        assertNotNull(ctx.getAuthentication());
        assertEquals(ctx.getAuthentication().getPrincipal(), authN.getPrincipal());
    }

    @Test
    public void checkBuilderWithManyAuthNAsDuplicate() throws Exception {
        final Authentication authN1 = manager.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword());
        builder.collect(authN1);

        final Authentication authN2 = manager.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword());
        builder.collect(authN2);

        assertEquals(builder.size(), 2);

        final AuthenticationContext ctx = builder.build();
        assertNotNull(ctx);
        assertNotNull(ctx.getAuthentication());
        assertEquals(ctx.getAuthentication().getPrincipal(), authN1.getPrincipal());
    }

    @Test
    public void checkBuilderWithDuplicateAuthN() throws Exception {
        final Authentication authN1 = manager.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword());
        builder.collect(authN1);
        builder.collect(authN1);
        assertEquals(builder.size(), 1);
    }

    @Test
    public void verifyBuilderAttributesWithManyAuthN() throws Exception {
        final Authentication authN1 = manager.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword());
        builder.collect(authN1);

        final Authentication authN2 = manager.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));
        builder.collect(authN2);

        assertEquals(builder.size(), 2);

        final AuthenticationContext ctx = builder.build();
        assertNotNull(ctx);
        assertNotNull(ctx.getAuthentication());
        assertEquals(ctx.getAuthentication().getPrincipal(), authN1.getPrincipal());
        assertEquals(ctx.getAuthentication().getAttributes().size(), 1);
        assertTrue(ctx.getAuthentication().getAttributes().containsKey(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE));
    }

    @Test
    public void checkBuilderWithManyThreads() throws Exception {
        this.manager = new PolicyBasedAuthenticationManager(new SimpleTestUsernamePasswordAuthenticationHandler());

        final int concurrentSize = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(concurrentSize);
        try {
            final List<AuthenticationContextGenerator> generators = new ArrayList<>(concurrentSize);
            for (int i = 0; i < concurrentSize; i++) {
                generators.add(new AuthenticationContextGenerator(this.manager, this.builder,
                        TestUtils.getCredentialsWithSameUsernameAndPassword("user" + i)));
            }
            final List<Future<AuthenticationContextBuilder>> results = executor.invokeAll(generators);
            for (final Future<AuthenticationContextBuilder> result : results) {
                final AuthenticationContextBuilder resultBuilder = result.get();
                assertEquals(resultBuilder.size(), concurrentSize);
                final AuthenticationContext ctx = resultBuilder.build();
                assertNotNull(ctx.getAuthentication());
                assertEquals(ctx.getAuthentication().getSuccesses().size(), 1);
                assertEquals(ctx.getAuthentication().getCredentials().size(), concurrentSize);
                assertEquals(ctx.getAuthentication().getFailures().size(), 0);
            }
        } catch (final Exception e) {
            logger.error("checkBuilderWithManyThreads produced an error", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private static class AuthenticationContextGenerator implements Callable<AuthenticationContextBuilder> {
        private final AuthenticationManager manager;
        private final DefaultAuthenticationContextBuilder builder;
        private final Credential credential;

        AuthenticationContextGenerator(final AuthenticationManager manager,
                                       final DefaultAuthenticationContextBuilder builder,
                                       final Credential credential) {
            this.manager = manager;
            this.builder = builder;
            this.credential = credential;
        }

        @Override
        public AuthenticationContextBuilder call() throws Exception {
            return this.builder.collect(this.manager.authenticate(credential));
        }
    }

}
