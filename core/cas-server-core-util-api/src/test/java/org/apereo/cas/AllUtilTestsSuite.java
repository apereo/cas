package org.apereo.cas;

import org.apereo.cas.util.DigestUtilsTests;
import org.apereo.cas.util.HttpRequestUtilsTests;
import org.apereo.cas.util.HttpUtilsTests;
import org.apereo.cas.util.InetAddressUtilsTests;
import org.apereo.cas.util.LoggingUtilsTests;
import org.apereo.cas.util.SocketUtilsTests;
import org.apereo.cas.util.cache.DistributedCacheManagerTests;
import org.apereo.cas.util.cache.DistributedCacheObjectTests;
import org.apereo.cas.util.crypto.CertUtilsTests;
import org.apereo.cas.util.crypto.GlibcCryptPasswordEncoderTests;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBeanTests;
import org.apereo.cas.util.crypto.PublicKeyFactoryBeanTests;
import org.apereo.cas.util.function.FunctionUtilsTests;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBeanTests;
import org.apereo.cas.util.io.FileWatcherServiceTests;
import org.apereo.cas.util.io.PathWatcherServiceTests;
import org.apereo.cas.util.io.TemporaryFileSystemResourceTests;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManagerTests;
import org.apereo.cas.util.scripting.GroovyShellScriptTests;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResourceTests;
import org.apereo.cas.util.serialization.MessageSanitizationUtilsTests;
import org.apereo.cas.util.serialization.SerializationUtilsTests;
import org.apereo.cas.util.spring.BeanContainerTests;
import org.apereo.cas.util.spring.ConvertersTests;
import org.apereo.cas.util.spring.SpringAwareMessageMessageInterpolatorTests;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolverTests;
import org.apereo.cas.util.spring.boot.BeanDefinitionStoreFailureAnalyzerTests;
import org.apereo.cas.util.spring.boot.ConditionalOnMatchingHostnameTests;
import org.apereo.cas.util.spring.boot.DefaultCasBannerTests;
import org.apereo.cas.util.ssl.CompositeX509KeyManagerTests;
import org.apereo.cas.util.ssl.CompositeX509TrustManagerTests;
import org.apereo.cas.util.transforms.GroovyPrincipalNameTransformerTests;
import org.apereo.cas.util.transforms.RegexPrincipalNameTransformerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    CertUtilsTests.class,
    PrivateKeyFactoryBeanTests.class,
    DistributedCacheManagerTests.class,
    DigestUtilsTests.class,
    DistributedCacheObjectTests.class,
    SerializationUtilsTests.class,
    SpringAwareMessageMessageInterpolatorTests.class,
    HttpUtilsTests.class,
    ConvertersTests.class,
    BeanDefinitionStoreFailureAnalyzerTests.class,
    ConditionalOnMatchingHostnameTests.class,
    SimpleHttpClientFactoryBeanTests.class,
    GroovyScriptResourceCacheManagerTests.class,
    LoggingUtilsTests.class,
    SocketUtilsTests.class,
    BeanContainerTests.class,
    MessageSanitizationUtilsTests.class,
    InetAddressUtilsTests.class,
    CompositeX509TrustManagerTests.class,
    CompositeX509KeyManagerTests.class,
    HttpRequestUtilsTests.class,
    WatchableGroovyScriptResourceTests.class,
    TemporaryFileSystemResourceTests.class,
    PathWatcherServiceTests.class,
    FileWatcherServiceTests.class,
    RegexPrincipalNameTransformerTests.class,
    GroovyPrincipalNameTransformerTests.class
})
@Suite
public class AllUtilTestsSuite {
}
