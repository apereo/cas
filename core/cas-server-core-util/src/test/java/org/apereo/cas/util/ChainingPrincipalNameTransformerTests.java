package org.apereo.cas.util;

import org.apereo.cas.util.transforms.ChainingPrincipalNameTransformer;
import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
import org.apereo.cas.util.transforms.RegexPrincipalNameTransformer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link ChainingPrincipalNameTransformerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ChainingPrincipalNameTransformerTests {
    @Test
    public void verifyChain() {
        final ChainingPrincipalNameTransformer t = new ChainingPrincipalNameTransformer();
        t.addTransformer(new RegexPrincipalNameTransformer("(.+)@example.org"));
        t.addTransformer(new PrefixSuffixPrincipalNameTransformer("prefix-", "-suffix"));
        t.addTransformer(new ConvertCasePrincipalNameTransformer(true));
        final String uid = t.transform("casuser@example.org");
        assertTrue("PREFIX-CASUSER-SUFFIX".equals(uid));
    }

}
