package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class SamlAuthenticationMetaDataPopulatorTests {

    private SamlAuthenticationMetaDataPopulator populator;

    @BeforeEach
    public void initialize() {
        this.populator = new SamlAuthenticationMetaDataPopulator();
    }

    @Test
    public void verifyAuthenticationTypeFound() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        val auth = builder.build();

        assertEquals(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_PASSWORD,
            auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
    }

    @Test
    public void verifyAuthenticationTypeFoundByDefault() {
        val credentials = new CustomCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        val auth = builder.build();
        assertNotNull(auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
    }

    @Test
    public void verifyAuthenticationTypeFoundCustom() {
        val credentials = new CustomCredential();

        val added = new HashMap<String, String>();
        added.put(CustomCredential.class.getName(), "FF");

        this.populator.setUserDefinedMappings(added);

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        val auth = builder.build();

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
