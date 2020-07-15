package org.apereo.cas.web.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.MockServletContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Utility")
public class WebUtilsTests {

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        assertNull(WebUtils.getHttpServletRequestUserAgentFromRequestContext(context));
        assertNull(WebUtils.getHttpServletRequestUserAgentFromRequestContext(request));
        assertNull(WebUtils.getAuthenticationResult(context));
        assertNull(WebUtils.getHttpServletRequestGeoLocationFromRequestContext());

        assertNotNull(WebUtils.produceUnauthorizedErrorView());
        assertNotNull(WebUtils.produceErrorView(new IllegalArgumentException()));
        assertNotNull(WebUtils.produceErrorView("error-view", new IllegalArgumentException()));
        assertNotNull(WebUtils.getHttpRequestFullUrl(context));
        assertFalse(WebUtils.isGraphicalUserAuthenticationEnabled(context));
        assertTrue(WebUtils.getDelegatedAuthenticationProviderConfigurations(context).isEmpty());

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                WebUtils.putInitialHttpRequestPostParameters(context);
                WebUtils.putExistingSingleSignOnSessionAvailable(context, true);
                WebUtils.putExistingSingleSignOnSessionPrincipal(context, CoreAuthenticationTestUtils.getPrincipal());
                WebUtils.putAvailableAuthenticationHandleNames(context, List.of());
                WebUtils.putPasswordManagementEnabled(context, true);
                WebUtils.putRecaptchaPropertiesFlowScope(context, new GoogleRecaptchaProperties());
                WebUtils.putLogoutUrls(context, Map.of());
                val ac = OneTimeTokenAccount.builder()
                    .validationCode(123456)
                    .username("casuser")
                    .name("Example")
                    .build();
                WebUtils.putOneTimeTokenAccount(context, ac);
                assertNotNull(WebUtils.getOneTimeTokenAccount(context, OneTimeTokenAccount.class));
                WebUtils.putOneTimeTokenAccounts(context, List.of(ac));

                WebUtils.putWarnCookieIfRequestParameterPresent(null, context);
            }
        });
        WebUtils.putCredential(context, new UsernamePasswordCredential("casuser", "password"));
        assertThrows(ClassCastException.class, () -> WebUtils.getCredential(context, OneTimeTokenCredential.class));
        assertThrows(IllegalArgumentException.class, () -> WebUtils.getPrincipalFromRequestContext(context, null));

        request.addParameter(WebUtils.PUBLIC_WORKSTATION_ATTRIBUTE, "true");
        WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
        assertTrue(WebUtils.isAuthenticatingAtPublicWorkstation(context));
    }
}
