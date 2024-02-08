package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccessTokenTokenExchangeGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuth")
public class AccessTokenTokenExchangeGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("accessTokenTokenExchangeGrantRequestExtractor")
    private AccessTokenGrantRequestExtractor extractor;

    @Test
    void verifyExtraction() throws Throwable {
        val service = addRegisteredService(Set.of(OAuth20GrantTypes.TOKEN_EXCHANGE));
        val request = new MockHttpServletRequest();

        val subjectToken = getAccessToken(service.getServiceId(), service.getClientId());
        ticketRegistry.addTicket(subjectToken);
        
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN, subjectToken.getId());
        request.addParameter(OAuth20Constants.SUBJECT_TOKEN_TYPE, OAuth20TokenExchangeTypes.ACCESS_TOKEN.getType());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.TOKEN_EXCHANGE.getType());
        request.addParameter(OAuth20Constants.AUDIENCE, service.getClientId());
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        assertEquals(OAuth20ResponseTypes.NONE, extractor.getResponseType());
        assertTrue(extractor.supports(context));
        val tokenContext = extractor.extract(context);
        assertNotNull(tokenContext);
        assertNotNull(tokenContext.getSubjectToken());
        assertFalse(tokenContext.getTokenExchangeAudience().isEmpty());
        assertNull(tokenContext.getTokenExchangeResource());
        assertEquals(OAuth20TokenExchangeTypes.ACCESS_TOKEN, tokenContext.getSubjectTokenType());
        assertEquals(OAuth20TokenExchangeTypes.ACCESS_TOKEN, tokenContext.getRequestedTokenType());
    }
}
