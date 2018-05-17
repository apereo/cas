package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link PrincipalNameTransformerUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class PrincipalNameTransformerUtilsTests {
    @Test
    public void verifyAction() {
        final PrincipalTransformationProperties properties = new PrincipalTransformationProperties();
        properties.setPrefix("prefix-");
        properties.setSuffix("-suffix");
        properties.setCaseConversion(PrincipalTransformationProperties.CaseConversion.UPPERCASE);
        final PrincipalNameTransformer t = PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties);
        final String result = t.transform("userid");
        assertEquals("PREFIX-USERID-SUFFIX", result);
    }
}
