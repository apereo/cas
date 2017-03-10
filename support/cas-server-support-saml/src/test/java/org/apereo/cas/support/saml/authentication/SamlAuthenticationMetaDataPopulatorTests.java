package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 *
 */
public class SamlAuthenticationMetaDataPopulatorTests {

    private SamlAuthenticationMetaDataPopulator populator;

    @Before
    public void setUp() throws Exception {
        this.populator = new SamlAuthenticationMetaDataPopulator();
    }

    @Test
    public void verifyAuthenticationTypeFound() {
        final UsernamePasswordCredential credentials = new UsernamePasswordCredential();
        final AuthenticationBuilder builder = newAuthenticationBuilder(
                CoreAuthenticationTestUtils.getPrincipal());
        this.populator.populateAttributes(builder, credentials);
        final Authentication auth = builder.build();

        assertEquals(
                auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD),
                SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_PASSWORD);
    }

    @Test
    public void verifyAuthenticationTypeNotFound() {
        final CustomCredential credentials = new CustomCredential();
        final AuthenticationBuilder builder = newAuthenticationBuilder(
                CoreAuthenticationTestUtils.getPrincipal());
        this.populator.populateAttributes(builder, credentials);
        final Authentication auth = builder.build();

        assertNull(auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
    }

    @Test
    public void verifyAuthenticationTypeFoundCustom() {
        final CustomCredential credentials = new CustomCredential();

        final Map<String, String> added = new HashMap<>();
        added.put(CustomCredential.class.getName(), "FF");

        this.populator.setUserDefinedMappings(added);

        final AuthenticationBuilder builder = newAuthenticationBuilder(
                CoreAuthenticationTestUtils.getPrincipal());
        this.populator.populateAttributes(builder, credentials);
        final Authentication auth = builder.build();

        assertEquals(
                "FF",
                auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
    }

    private static class CustomCredential implements Credential {

        private static final long serialVersionUID = 8040541789035593268L;

        @Override
        public String getId() {
            return "nobody";
        }
    }

    private static AuthenticationBuilder newAuthenticationBuilder(final Principal principal) {
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        final AuthenticationHandler handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        return new DefaultAuthenticationBuilder(principal)
                .addCredential(meta)
                .addSuccess("test", new DefaultHandlerResult(handler, meta));
    }
}
