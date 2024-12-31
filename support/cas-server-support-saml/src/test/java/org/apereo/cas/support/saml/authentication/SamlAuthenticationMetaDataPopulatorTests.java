package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Tag("SAML1")
class SamlAuthenticationMetaDataPopulatorTests {

    private SamlAuthenticationMetaDataPopulator populator;

    @BeforeEach
    void initialize() {
        this.populator = new SamlAuthenticationMetaDataPopulator();
    }

    @Test
    void verifyAuthenticationTypeFound() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();

        assertEquals(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_PASSWORD,
            auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD).getFirst());
    }

    @Test
    void verifyAuthenticationTypeFoundByDefault() {
        val credentials = new CustomCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();
        assertNotNull(auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD).getFirst());
    }

    @Test
    void verifyAuthenticationTypeFoundCustom() {
        val credentials = new CustomCredential();

        val added = new HashMap<String, String>();
        added.put(CustomCredential.class.getName(), "FF");

        this.populator.setUserDefinedMappings(added);

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();

        assertEquals(
            "FF",
            auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD).getFirst());
    }

    private static final class CustomCredential implements Credential {

        @Serial
        private static final long serialVersionUID = 8040541789035593268L;

        @Override
        public String getId() {
            return "nobody";
        }

        @Override
        public CredentialMetadata getCredentialMetadata() {
            return new BasicCredentialMetadata(this);
        }
    }

}
