package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.model.core.ticket.RememberMeAuthenticationProperties;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Tag("AuthenticationMetadata")
public class RememberMeAuthenticationMetaDataPopulatorTests {

    @Test
    public void verifyWithTrueRememberMeCredentials() {
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(true);
        val builder = newBuilder(c, new RememberMeAuthenticationProperties());
        val auth = builder.build();

        assertEquals(true, auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME).get(0));
    }

    @Test
    public void verifyRememberMeUserAgentAndIp() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(true);
        val builder = newBuilder(c, new RememberMeAuthenticationProperties()
            .setSupportedUserAgents("Chrome")
            .setSupportedIpAddresses("123.+"));
        val auth = builder.build();

        assertFalse(auth.getAttributes().containsKey(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void verifyRememberMeUserAgent() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(true);
        val builder = newBuilder(c, new RememberMeAuthenticationProperties()
            .setSupportedUserAgents("Chrome"));
        val auth = builder.build();

        assertEquals(true, auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME).get(0));
    }

    @Test
    public void verifyRememberMeIp() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(true);
        val builder = newBuilder(c, new RememberMeAuthenticationProperties()
            .setSupportedIpAddresses("192.+"));
        val auth = builder.build();

        assertFalse(auth.getAttributes().containsKey(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void verifyWithFalseRememberMeCredentials() {
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(false);
        val builder = newBuilder(c, new RememberMeAuthenticationProperties());
        val auth = builder.build();

        assertNull(auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void verifyWithoutRememberMeCredentials() {
        val builder = newBuilder(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new RememberMeAuthenticationProperties());
        val auth = builder.build();

        assertNull(auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    private static AuthenticationBuilder newBuilder(final Credential credential,
                                                    final RememberMeAuthenticationProperties properties) {
        val populator = new RememberMeAuthenticationMetaDataPopulator(properties);
        val meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal())
            .addCredential(meta)
            .addSuccess("test", new DefaultAuthenticationHandlerExecutionResult(handler, meta));

        if (populator.supports(credential)) {
            populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(credential));
        }
        return builder;
    }

}
