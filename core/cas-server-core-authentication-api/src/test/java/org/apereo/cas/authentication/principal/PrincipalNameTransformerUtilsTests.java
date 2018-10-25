package org.apereo.cas.authentication.principal;

import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalNameTransformerUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
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
}
