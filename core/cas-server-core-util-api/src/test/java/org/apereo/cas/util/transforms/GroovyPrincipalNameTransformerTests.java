package org.apereo.cas.util.transforms;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPrincipalNameTransformerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
public class GroovyPrincipalNameTransformerTests {
    @Test
    public void verifyOperation() throws Exception {
        val chain = new ChainingPrincipalNameTransformer();
        chain.addTransformer(new GroovyPrincipalNameTransformer(new ClassPathResource("GroovyTransformer.groovy")));
        chain.addTransformer(new ConvertCasePrincipalNameTransformer(true));
        val result = chain.transform("cas@example.org");
        assertEquals("CAS-PERSON", result);
    }
}
