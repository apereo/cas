package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalNameTransformerUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Utility")
class PrincipalNameTransformerUtilsTests {
    @Test
    void verifyBlockingAction() {
        val properties = new PrincipalTransformationProperties();
        properties.setBlockingPattern(".+@.+\\.com");
        val transformer = PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties);
        assertThrows(PreventedException.class, () -> transformer.transform("hello@cas.com"));
    }

    @Test
    void verifyAction() throws Throwable {
        val properties = new PrincipalTransformationProperties();
        properties.setPrefix("prefix-");
        properties.setSuffix("-suffix");
        properties.setCaseConversion(PrincipalTransformationProperties.CaseConversion.UPPERCASE);
        val transformer = PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties);
        val result = transformer.transform("userid");
        assertEquals("PREFIX-USERID-SUFFIX", result);
    }

    @Test
    void verifyGroovyAction() {
        val properties = new PrincipalTransformationProperties();
        properties.getGroovy().setLocation(new ClassPathResource("SomeGroovyScript.groovy"));
        properties.setCaseConversion(PrincipalTransformationProperties.CaseConversion.LOWERCASE);
        val transformer = PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties);
        assertNotNull(transformer);
    }

    @Test
    void verifyRegexAction() {
        val properties = new PrincipalTransformationProperties();
        properties.setPattern("test.+");
        val t = PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties);
        assertNotNull(t);
    }
}
