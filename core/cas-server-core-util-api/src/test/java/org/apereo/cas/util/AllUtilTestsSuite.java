package org.apereo.cas.util;

import org.apereo.cas.util.crypto.GlibcCryptPasswordEncoderTests;
import org.apereo.cas.util.crypto.PublicKeyFactoryBeanTests;
import org.apereo.cas.util.transforms.GroovyPrincipalNameTransformerTests;
import org.apereo.cas.util.transforms.RegexPrincipalNameTransformerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllUtilTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    PublicKeyFactoryBeanTests.class,
    GlibcCryptPasswordEncoderTests.class,
    RegexPrincipalNameTransformerTests.class,
    GroovyPrincipalNameTransformerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllUtilTestsSuite {
}
