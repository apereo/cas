package org.apereo.cas.audit.spi;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.ImmutableAssertion;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;

import java.util.Arrays;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.st;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AssertionAsReturnValuePrincipalResolver}
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.9
 */
public class AssertionAsReturnValuePrincipalResolverTests extends AbstractCentralAuthenticationServiceTests {

    @Test
    public void verifyResolverAssertionReturnValue() throws Exception {
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationResult authnResult = TestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), c);
        final Authentication authn = authnResult.getAuthentication();

        final TicketOrCredentialPrincipalResolver delegate = new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        final AssertionAsReturnValuePrincipalResolver res = new AssertionAsReturnValuePrincipalResolver(delegate);
        final JoinPoint jp = mock(JoinPoint.class);
        Assertion returnedAssertion =
                new ImmutableAssertion(authnResult.getAuthentication(), Arrays.asList(authn), authnResult.getService(), true);

        final String result = res.resolveFrom(jp, returnedAssertion);
        assertNotNull(result);
        assertEquals(result, c.getId());
    }
}
