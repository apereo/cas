package org.apereo.cas.web.flow.delegation;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BasePasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordlessDelegatedClientAuthenticationRequestCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
class PasswordlessDelegatedClientAuthenticationRequestCustomizerTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier("passwordlessDelegatedClientAuthenticationRequestCustomizer")
    private DelegatedClientAuthenticationRequestCustomizer passwordlessDelegatedClientAuthenticationRequestCustomizer;

    @Test
    void verifySupports() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val oidcClient = new OidcClient(new OidcConfiguration());
        assertFalse(passwordlessDelegatedClientAuthenticationRequestCustomizer.supports(oidcClient, webContext, context));

        val account = PasswordlessUserAccount.builder().username("casuser").build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertFalse(passwordlessDelegatedClientAuthenticationRequestCustomizer.supports(new FormClient(), webContext, context));
        assertTrue(passwordlessDelegatedClientAuthenticationRequestCustomizer.supports(oidcClient, webContext, context));
    }

    @Test
    void verifyCustomizeCreatesCustomParameters() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder().username("casuser").build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());

        passwordlessDelegatedClientAuthenticationRequestCustomizer.customize(
            new OidcClient(new OidcConfiguration()), webContext, context);

        assertEquals(Map.of(OidcConfiguration.LOGIN_HINT, account.getUsername()),
            webContext.getRequestAttribute(OidcConfiguration.CUSTOM_PARAMS).orElseThrow());
    }

    @Test
    void verifyCustomizePreservesExistingCustomParameters() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder().username("casuser").build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val customParams = new HashMap<String, String>();
        customParams.put("prompt", "login");
        customParams.put(OidcConfiguration.LOGIN_HINT, "otheruser");
        webContext.setRequestAttribute(OidcConfiguration.CUSTOM_PARAMS, customParams);

        passwordlessDelegatedClientAuthenticationRequestCustomizer.customize(
            new OidcClient(new OidcConfiguration()), webContext, context);

        assertEquals("login", customParams.get("prompt"));
        assertEquals(account.getUsername(), customParams.get(OidcConfiguration.LOGIN_HINT));
        assertSame(customParams, webContext.getRequestAttribute(OidcConfiguration.CUSTOM_PARAMS).orElseThrow());
    }
}
