package org.apereo.cas.util;

import org.apereo.cas.util.crypto.GlibcCryptPasswordEncoderTests;
import org.apereo.cas.util.crypto.PublicKeyFactoryBeanTests;
import org.apereo.cas.util.function.FunctionUtilsTests;
import org.apereo.cas.util.io.FileWatcherServiceTests;
import org.apereo.cas.util.io.PathWatcherServiceTests;
import org.apereo.cas.util.io.TemporaryFileSystemResourceTests;
import org.apereo.cas.util.scripting.GroovyShellScriptTests;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResourceTests;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolverTests;
import org.apereo.cas.util.spring.boot.DefaultCasBannerTests;
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
    SpringExpressionLanguageValueResolverTests.class,
    GlibcCryptPasswordEncoderTests.class,
    DefaultCasBannerTests.class,
    GroovyShellScriptTests.class,
    FunctionUtilsTests.class,
    HttpRequestUtilsTests.class,
    WatchableGroovyScriptResourceTests.class,
    TemporaryFileSystemResourceTests.class,
    PathWatcherServiceTests.class,
    FileWatcherServiceTests.class,
    RegexPrincipalNameTransformerTests.class,
    GroovyPrincipalNameTransformerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllUtilTestsSuite {
}
