package org.apereo.cas.authentication.principal;

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
@Tag("Simple")
public class PrincipalNameTransformerUtilsTests {
    @Test
    public void verifyAction() {
        val properties = new PrincipalTransformationProperties();
        properties.setPrefix("prefix-");
        properties.setSuffix("-suffix");
        properties.setCaseConversion(PrincipalTransformationProperties.CaseConversion.UPPERCASE);
        val t = PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties);
        val result = t.transform("userid");
        assertEquals("PREFIX-USERID-SUFFIX", result);
    }

    @Test
    public void verifyGroovyAction() {
        val properties = new PrincipalTransformationProperties();
        properties.getGroovy().setLocation(new ClassPathResource("SomeGroovyScript.groovy"));
        properties.setCaseConversion(PrincipalTransformationProperties.CaseConversion.LOWERCASE);
        val t = PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties);
        assertNotNull(t);
    }

    @Test
    public void verifyRegexAction() {
        val properties = new PrincipalTransformationProperties();
        properties.setPattern("test.+");
        val t = PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties);
        assertNotNull(t);
    }
}
