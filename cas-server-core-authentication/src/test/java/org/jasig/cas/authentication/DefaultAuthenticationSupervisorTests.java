package org.jasig.cas.authentication;

import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuthenticationSupervisorTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class DefaultAuthenticationSupervisorTests {

    private AuthenticationManager manager;
    private DefaultAuthenticationContextBuilder builder;
    private DefaultAuthenticationTransactionManager authenticationSupervisor;

    @Before
    public void setup() {
        this.builder = new DefaultAuthenticationContextBuilder();
        final DefaultPrincipalElectionStrategy principalStrategy = new DefaultPrincipalElectionStrategy();
        principalStrategy.setPrincipalFactory(new DefaultPrincipalFactory());
        builder.setPrincipalElectionStrategy(principalStrategy);

        final AcceptUsersAuthenticationHandler handler = new AcceptUsersAuthenticationHandler();
        handler.setUsers(Collections.singletonMap("casuser", "Mellon"));
        this.manager = new PolicyBasedAuthenticationManager(handler, new SimpleTestUsernamePasswordAuthenticationHandler());

        this.authenticationSupervisor = new DefaultAuthenticationTransactionManager();
        this.authenticationSupervisor.setAuthenticationContextBuilder(this.builder);
        this.authenticationSupervisor.setAuthenticationManager(this.manager);
    }

    @Test
    public void verifySupervisor() throws Exception {
        authenticationSupervisor.processAuthenticationAttempt(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final AuthenticationContext ctx = authenticationSupervisor.build();
        assertNotNull(ctx);
        assertNotNull(ctx.getAuthentication());
        assertNotNull(ctx.getAuthentication().getPrincipal());
    }

    @Test
    public void verifyElectionStrategy() throws Exception {
        this.builder.setPrincipalElectionStrategy(new PrincipalElectionStrategy() {
            @Override
            public Principal nominate(final Collection<Authentication> authentications, final Map<String, Object> principalAttributes) {
                final Principal principal = mock(Principal.class);
                when(principal.getId()).thenReturn(TestUtils.CONST_USERNAME);
                return principal;
            }
        });

        authenticationSupervisor.processAuthenticationAttempt(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final AuthenticationContext ctx = authenticationSupervisor.build();
        assertNotNull(ctx);
        assertNotNull(ctx.getAuthentication());
        assertNotNull(ctx.getAuthentication().getPrincipal());
        assertEquals(ctx.getAuthentication().getPrincipal().getId(), TestUtils.CONST_USERNAME);
    }
}
