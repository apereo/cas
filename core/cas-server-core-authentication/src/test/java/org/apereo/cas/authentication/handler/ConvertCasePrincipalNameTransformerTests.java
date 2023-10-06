package org.apereo.cas.authentication.handler;

import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the switch-case transformer.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("Authentication")
class ConvertCasePrincipalNameTransformerTests {

    @Test
    void verifyUpperCaseTransformerWithTrimAndDelegate() throws Throwable {
        val suffixTrans = new PrefixSuffixPrincipalNameTransformer();
        suffixTrans.setPrefix("a");
        suffixTrans.setSuffix("z");
        val transformer = new ConvertCasePrincipalNameTransformer();
        transformer.setToUpperCase(true);
        val result = transformer.transform(suffixTrans.transform("   uid  "));
        assertEquals("A   UID  Z", result);
    }

    @Test
    void verifyUpperCaseTransformerWithTrim() throws Throwable {
        val transformer = new ConvertCasePrincipalNameTransformer();
        transformer.setToUpperCase(true);
        val result = transformer.transform("   uid  ");
        assertEquals("UID", result);
    }

    @Test
    void verifyLowerCaseTransformerWithTrim() throws Throwable {
        val transformer = new ConvertCasePrincipalNameTransformer();
        val result = transformer.transform("   UID  ");
        assertEquals("uid", result);
    }
}
