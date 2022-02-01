package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAuthorizationModelAndViewBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.discovery.authorization-response-issuer-parameter-supported=true")
public class OidcAuthorizationModelAndViewBuilderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oauthAuthorizationModelAndViewBuilder")
    private OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder;

    @Test
    public void verifyOperationForOidc() throws Exception {
        val parameters = new HashMap<String, String>();
        val registeredService = getOidcRegisteredService();
        val results = oauthAuthorizationModelAndViewBuilder.build(registeredService,
            OAuth20ResponseModeTypes.FORM_POST, "https://localhost:8443/app/redirect", parameters);
        assertTrue(results.getModel().containsKey("originalUrl"));
        val url = results.getModel().get("originalUrl").toString();
        assertTrue(url.contains(OidcConstants.ISS.concat("=")));
        val params = (Map) results.getModel().get("parameters");
        assertTrue(params.containsKey(OidcConstants.ISS));
    }

    @Test
    public void verifyOperationForNonOidc() throws Exception {
        val parameters = new HashMap<String, String>();
        val registeredService = getOAuthRegisteredService(UUID.randomUUID().toString(), "https://localhost:8443/app/redirect");
        val results = oauthAuthorizationModelAndViewBuilder.build(registeredService,
            OAuth20ResponseModeTypes.NONE, "https://localhost:8443/app/redirect", parameters);
        val view = (RedirectView) results.getView();
        assertEquals("https://localhost:8443/app/redirect", Objects.requireNonNull(view).getUrl());
    }
}
