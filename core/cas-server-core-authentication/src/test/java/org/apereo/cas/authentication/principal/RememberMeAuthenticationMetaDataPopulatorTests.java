package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.model.core.ticket.RememberMeAuthenticationProperties;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Tag("AuthenticationMetadata")
class RememberMeAuthenticationMetaDataPopulatorTests {

    private static AuthenticationBuilder newBuilder(final Credential credential, final RememberMeAuthenticationProperties properties) {
        val meta = new UsernamePasswordCredential();
        val populator = new RememberMeAuthenticationMetaDataPopulator(properties);
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal())
            .addCredential(meta)
            .addSuccess("test", new DefaultAuthenticationHandlerExecutionResult(handler, meta));

        if (populator.supports(credential)) {
            populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credential));
        }
        return builder;
    }

    @Test
    void verifyWithTrueRememberMeCredentials() {
        val credential = new RememberMeUsernamePasswordCredential();
        credential.setRememberMe(true);
        val builder = newBuilder(credential, new RememberMeAuthenticationProperties());
        val auth = builder.build();

        assertEquals(true, auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME).getFirst());
    }

    @Test
    void verifyRememberMeUserAgentAndIp() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpHeaders.USER_AGENT, "Chrome");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        val credential = new RememberMeUsernamePasswordCredential();
        credential.setRememberMe(true);
        val builder = newBuilder(credential, new RememberMeAuthenticationProperties()
            .setSupportedUserAgents("Chrome")
            .setSupportedIpAddresses("123.+"));
        val auth = builder.build();
        assertFalse(auth.containsAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    void verifyRememberMeUserAgent() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpHeaders.USER_AGENT, "Chrome");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        val credential = new RememberMeUsernamePasswordCredential();
        credential.setRememberMe(true);
        val builder = newBuilder(credential, new RememberMeAuthenticationProperties()
            .setSupportedUserAgents("Chrome"));
        val auth = builder.build();

        assertEquals(true, auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME).getFirst());
    }

    @Test
    void verifyRememberMeIp() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(true);
        val builder = newBuilder(c, new RememberMeAuthenticationProperties()
            .setSupportedIpAddresses("192.+"));
        val auth = builder.build();
        assertFalse(auth.containsAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    void verifyWithFalseRememberMeCredentials() {
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(false);
        val builder = newBuilder(c, new RememberMeAuthenticationProperties());
        val auth = builder.build();
        assertNull(auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    void verifyWithoutRememberMeCredentials() {
        val builder = newBuilder(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new RememberMeAuthenticationProperties());
        val auth = builder.build();
        assertNull(auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

}
