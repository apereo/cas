package org.apereo.cas.util;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @since 5.1.0
 */
public class OidcAuthorizationRequestSupportTests {

    @Test
    public void verify() throws Exception {
        final String url = "https://tralala.whapi.com/something?prompt=value1";
        final Set<String> authorizationRequest = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);

        assertEquals("value1", authorizationRequest.toArray()[0]);
    }
}
