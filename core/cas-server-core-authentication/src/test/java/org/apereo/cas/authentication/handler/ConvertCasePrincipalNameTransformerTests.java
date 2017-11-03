package org.apereo.cas.authentication.handler;

import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the switch-case transformer.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class ConvertCasePrincipalNameTransformerTests {

    @Test
    public void verifyUpperCaseTransformerWithTrimAndDelegate() {
        final PrefixSuffixPrincipalNameTransformer suffixTrans = new PrefixSuffixPrincipalNameTransformer();
        suffixTrans.setPrefix("a");
        suffixTrans.setSuffix("z");
        final ConvertCasePrincipalNameTransformer transformer = new ConvertCasePrincipalNameTransformer();
        transformer.setToUpperCase(true);
        final String result = transformer.transform(suffixTrans.transform("   uid  "));
        assertEquals(result, "A   UID  Z");
    }

    @Test
    public void verifyUpperCaseTransformerWithTrim() {
        final ConvertCasePrincipalNameTransformer transformer = new ConvertCasePrincipalNameTransformer();
        transformer.setToUpperCase(true);
        final String result = transformer.transform("   uid  ");
        assertEquals(result, "UID");
    }

    @Test
    public void verifyLowerCaseTransformerWithTrim() {
        final ConvertCasePrincipalNameTransformer transformer = new ConvertCasePrincipalNameTransformer();
        final String result = transformer.transform("   UID  ");
        assertEquals(result, "uid");
    }
}
