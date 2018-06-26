package org.apereo.cas.support.saml.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
public class SamlAuthenticationMetaDataPopulatorTests {

    private SamlAuthenticationMetaDataPopulator populator;

    @Before
    public void initialize() {
        this.populator = new SamlAuthenticationMetaDataPopulator();
    }

    @Test
    public void verifyAuthenticationTypeFound() {
        final var credentials = new UsernamePasswordCredential();
        final var builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        final var auth = builder.build();

        assertEquals(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_PASSWORD,
            auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
    }

    @Test
    public void verifyAuthenticationTypeFoundByDefault() {
        final var credentials = new CustomCredential();
        final var builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        final var auth = builder.build();
        assertNotNull(auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
    }

    @Test
    public void verifyAuthenticationTypeFoundCustom() {
        final var credentials = new CustomCredential();

        final Map<String, String> added = new HashMap<>();
        added.put(CustomCredential.class.getName(), "FF");

        this.populator.setUserDefinedMappings(added);

        final var builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        final var auth = builder.build();

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

}
