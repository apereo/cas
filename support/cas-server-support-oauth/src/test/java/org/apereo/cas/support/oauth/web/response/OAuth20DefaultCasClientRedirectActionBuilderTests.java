package org.apereo.cas.support.oauth.web.response;

import org.apereo.cas.AbstractOAuth20Tests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultCasClientRedirectActionBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20DefaultCasClientRedirectActionBuilderTests extends AbstractOAuth20Tests {

    @Test
    public void verifyOperation() {
        val client = new CasClient(new CasConfiguration("https://example.org/cas/login"));
        client.setCallbackUrl("https://example.org/cas/callback");
        client.init();
        val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
        val input = oauthCasClientRedirectActionBuilder.build(client, context);
        assertFalse(input.isEmpty());

    }

}
