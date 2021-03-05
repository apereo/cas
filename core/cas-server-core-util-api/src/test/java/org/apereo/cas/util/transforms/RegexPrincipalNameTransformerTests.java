package org.apereo.cas.util.transforms;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegexPrincipalNameTransformerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Authentication")
public class RegexPrincipalNameTransformerTests {
    @Test
    public void verifyOperation() {
        val chain = new ChainingPrincipalNameTransformer();
        chain.addTransformer(new RegexPrincipalNameTransformer("(\\w+)@\\w+.org"));
        chain.addTransformer(new ConvertCasePrincipalNameTransformer(true));
        val result = chain.transform("cas@example.org");
        assertEquals("CAS", result);
    }

    @Test
    public void verifyNoOperation() {
        val chain = new ChainingPrincipalNameTransformer();
        chain.addTransformer(new RegexPrincipalNameTransformer("(\\w+)@\\w+.org"));
        val result = chain.transform(" cas  ");
        assertEquals("cas", result);
    }
}
