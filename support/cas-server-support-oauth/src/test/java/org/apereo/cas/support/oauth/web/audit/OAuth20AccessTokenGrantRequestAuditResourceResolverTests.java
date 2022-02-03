package org.apereo.cas.support.oauth.web.audit;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AccessTokenGrantRequestAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OAuth")
public class OAuth20AccessTokenGrantRequestAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        val r = new OAuth20AccessTokenGrantRequestAuditResourceResolver();
        val token = mock(OAuth20Token.class);
        when(token.getId()).thenReturn("CODE");
        when(token.getService()).thenReturn(RegisteredServiceTestUtils.getService());
        when(token.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());

        val service = new OAuthRegisteredService();
        service.setClientId("CLIENTID");
        service.setName("OAUTH");
        service.setId(123);

        val holder = AccessTokenRequestContext.builder()
            .scopes(CollectionUtils.wrapSet("email"))
            .service(token.getService())
            .authentication(token.getAuthentication())
            .registeredService(service)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .token(token)
            .ticketGrantingTicket(token.getTicketGrantingTicket())
            .redirectUri("https://oauth.example.org")
            .build();
        val result = AuditableExecutionResult.builder()
            .executionResult(holder)
            .build();
        assertTrue(r.resolveFrom(mock(JoinPoint.class), result).length > 0);
    }
}
