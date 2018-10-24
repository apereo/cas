package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.metadata.RememberMeAuthenticationMetaDataPopulator;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.2.1
 */
public class RememberMeAuthenticationMetaDataPopulatorTests {

    private final RememberMeAuthenticationMetaDataPopulator p = new RememberMeAuthenticationMetaDataPopulator();

    @Test
    public void verifyWithTrueRememberMeCredentials() {
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(true);
        val builder = newBuilder(c);
        val auth = builder.build();

        assertEquals(true, auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void verifyWithFalseRememberMeCredentials() {
        val c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(false);
        val builder = newBuilder(c);
        val auth = builder.build();

        assertNull(auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void verifyWithoutRememberMeCredentials() {
        val builder = newBuilder(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        val auth = builder.build();

        assertNull(auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    private AuthenticationBuilder newBuilder(final Credential credential) {
        val meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val builder = new DefaultAuthenticationBuilder(CoreAuthenticationTestUtils.getPrincipal())
            .addCredential(meta)
            .addSuccess("test", new DefaultAuthenticationHandlerExecutionResult(handler, meta));

        if (this.p.supports(credential)) {
            this.p.populateAttributes(builder, DefaultAuthenticationTransaction.of(credential));
        }
        return builder;
    }

}
