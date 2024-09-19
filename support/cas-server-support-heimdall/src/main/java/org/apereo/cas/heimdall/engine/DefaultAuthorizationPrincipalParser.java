package org.apereo.cas.heimdall.engine;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DefaultAuthorizationPrincipalParser}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class DefaultAuthorizationPrincipalParser implements AuthorizationPrincipalParser {
    protected final TicketRegistry ticketRegistry;

    @Override
    public Principal parse(final String authorizationHeader) throws Throwable {
        val claims = parseAuthorizationHeader(authorizationHeader);
        return PrincipalFactoryUtils.newPrincipalFactory()
            .createPrincipal(claims.getSubject(), (Map) claims.getClaims());
    }

    protected JWTClaimsSet parseAuthorizationHeader(final String authorizationHeader) throws Throwable {
        val token = StringUtils.removeStart(authorizationHeader, "Bearer ");
        try {
            return JwtBuilder.parse(token);
        } catch (final Exception e) {
            val ticket = ticketRegistry.getTicket(token, OAuth20AccessToken.class);
            FunctionUtils.throwIf(ticket == null || ticket.isExpired(), AuthenticationException::new);
            val claimsMap = new HashMap<String, Object>(ticket.getClaims());
            val authentication = ticket.getAuthentication();
            claimsMap.putAll(authentication.getAttributes());
            claimsMap.putAll(authentication.getPrincipal().getAttributes());
            claimsMap.put(OAuth20Constants.SCOPE, ticket.getScopes());
            claimsMap.put(OAuth20Constants.TOKEN, token);
            claimsMap.put(OAuth20Constants.CLAIM_SUB, authentication.getPrincipal().getId());
            return JWTClaimsSet.parse(claimsMap);
        }
    }
}
