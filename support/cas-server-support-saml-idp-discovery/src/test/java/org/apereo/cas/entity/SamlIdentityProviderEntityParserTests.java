package org.apereo.cas.entity;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdentityProviderEntityParserTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAML")
public class SamlIdentityProviderEntityParserTests {
    @Test
    public void verifyOperation() throws Exception {
        val parser = new SamlIdentityProviderEntityParser(new ClassPathResource("disco-feed.json"));
        assertFalse(parser.getIdentityProviders().isEmpty());
    }
}
