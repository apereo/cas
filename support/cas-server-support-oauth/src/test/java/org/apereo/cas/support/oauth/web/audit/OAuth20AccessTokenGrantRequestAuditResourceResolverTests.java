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
class OAuth20AccessTokenGrantRequestAuditResourceResolverTests {
    @Test
    void verifyAction() {
        val resolver = new OAuth20AccessTokenGrantRequestAuditResourceResolver();
        val token = mock(OAuth20Token.class);
        when(token.getId()).thenReturn("CODE");
        val tokenService = RegisteredServiceTestUtils.getService();
        val authentication = RegisteredServiceTestUtils.getAuthentication();
        when(token.getAuthentication()).thenReturn(authentication);

        val service = new OAuthRegisteredService();
        service.setClientId("CLIENTID");
        service.setName("OAUTH");
        service.setId(123);

        val holder = AccessTokenRequestContext.builder()
            .scopes(CollectionUtils.wrapSet("email"))
            .service(tokenService)
            .authentication(authentication)
            .registeredService(service)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .token(token)
            .redirectUri("https://oauth.example.org")
            .build();
        val result = AuditableExecutionResult.builder()
            .executionResult(holder)
            .build();
        assertTrue(resolver.resolveFrom(mock(JoinPoint.class), result).length > 0);
    }
}
