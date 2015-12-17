package org.jasig.cas.authentication.handler;

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
    public void verifyUpperCaseTranformerWithTrimAndDelegate() {
        final PrefixSuffixPrincipalNameTransformer suffixTrans = new PrefixSuffixPrincipalNameTransformer();
        suffixTrans.setPrefix("a");
        suffixTrans.setSuffix("z");
        final ConvertCasePrincipalNameTransformer transformer = new ConvertCasePrincipalNameTransformer(suffixTrans);
        transformer.setToUpperCase(true);
        final String result = transformer.transform("   uid  ");
        assertEquals(result, "AUIDZ");
    }
    
    @Test
    public void verifyUpperCaseTranformerWithTrim() {
        final ConvertCasePrincipalNameTransformer transformer = new ConvertCasePrincipalNameTransformer();
        transformer.init();
        transformer.setToUpperCase(true);
        final String result = transformer.transform("   uid  ");
        assertEquals(result, "UID");
    }
    
    @Test
    public void verifyLowerCaseTranformerWithTrim() {
        final ConvertCasePrincipalNameTransformer transformer = new ConvertCasePrincipalNameTransformer();
        transformer.init();
        final String result = transformer.transform("   UID  ");

        assertEquals(result, "uid");
    }
}
