package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.CredentialCustomFieldsAttributeMetaDataPopulator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CredentialCustomFieldsAttributeMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class CredentialCustomFieldsAttributeMetaDataPopulatorTests {
    private final CredentialCustomFieldsAttributeMetaDataPopulator populator =
        new CredentialCustomFieldsAttributeMetaDataPopulator();

    @Test
    public void verifyPopulator() {
        val credentials = new UsernamePasswordCredential();
        credentials.getCustomFields().put("field1", "value1");
        credentials.getCustomFields().put("field2", List.of("value2"));

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        assertTrue(populator.supports(credentials));
        
        val auth = builder.build();
        assertNotNull(auth.getAttributes().get("field1"));
        assertNotNull(auth.getAttributes().get("field2"));
    }
}
