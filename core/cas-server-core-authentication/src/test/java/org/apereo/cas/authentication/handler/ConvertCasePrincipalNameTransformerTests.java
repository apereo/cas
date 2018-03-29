package org.apereo.cas.authentication.handler;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ConvertCasePrincipalNameTransformerTests {

    @Test
    public void verifyUpperCaseTransformerWithTrimAndDelegate() {
        final var suffixTrans = new PrefixSuffixPrincipalNameTransformer();
        suffixTrans.setPrefix("a");
        suffixTrans.setSuffix("z");
        final var transformer = new ConvertCasePrincipalNameTransformer();
        transformer.setToUpperCase(true);
        final var result = transformer.transform(suffixTrans.transform("   uid  "));
        assertEquals("A   UID  Z", result);
    }

    @Test
    public void verifyUpperCaseTransformerWithTrim() {
        final var transformer = new ConvertCasePrincipalNameTransformer();
        transformer.setToUpperCase(true);
        final var result = transformer.transform("   uid  ");
        assertEquals("UID", result);
    }

    @Test
    public void verifyLowerCaseTransformerWithTrim() {
        final var transformer = new ConvertCasePrincipalNameTransformer();
        final var result = transformer.transform("   UID  ");
        assertEquals("uid", result);
    }
}
