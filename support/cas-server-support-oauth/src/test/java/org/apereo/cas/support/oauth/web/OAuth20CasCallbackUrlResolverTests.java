package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;

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
        val output = casCallbackUrlResolver.compute(casProperties.getServer().getPrefix(),
            new JEEContext(request, response));
        assertNotNull(output);
    }

}
