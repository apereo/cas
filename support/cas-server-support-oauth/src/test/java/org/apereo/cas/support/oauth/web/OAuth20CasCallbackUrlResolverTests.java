package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.http.url.UrlResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CasCallbackUrlResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20CasCallbackUrlResolverTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("casCallbackUrlResolver")
    private UrlResolver casCallbackUrlResolver;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        request.addParameter(OAuth20Constants.RESPONSE_MODE, OAuth20ResponseModeTypes.FORM_POST.getType());
        val output = casCallbackUrlResolver.compute(
            OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()),
            new JEEContext(request, response));
        assertNotNull(output);
    }

}
