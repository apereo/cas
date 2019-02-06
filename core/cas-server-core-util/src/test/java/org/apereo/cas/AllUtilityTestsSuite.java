package org.apereo.cas;

import org.apereo.cas.util.CollectionUtilsTests;
import org.apereo.cas.util.CompressionUtilsTests;
import org.apereo.cas.util.DateTimeUtilsTests;
import org.apereo.cas.util.EncodingUtilsTests;
import org.apereo.cas.util.RandomUtilsTests;
import org.apereo.cas.util.RegexUtilsTests;
import org.apereo.cas.util.ResourceUtilsTests;
import org.apereo.cas.util.ScriptingUtilsTests;
import org.apereo.cas.util.cipher.BinaryCipherExecutorTests;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutorTests;
import org.apereo.cas.util.cipher.JsonWebKeySetStringCipherExecutorTests;
import org.apereo.cas.util.cipher.ProtocolTicketCipherExecutorTests;
import org.apereo.cas.util.cipher.RsaKeyPairCipherExecutorTests;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutorTests;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutorTests;
import org.apereo.cas.util.gen.Base64RandomStringGeneratorTests;
import org.apereo.cas.util.gen.ChainingPrincipalNameTransformerTests;
import org.apereo.cas.util.gen.DefaultLongNumericGeneratorTests;
import org.apereo.cas.util.gen.DefaultRandomStringGeneratorTests;
import org.apereo.cas.util.gen.HexRandomStringGeneratorTests;
import org.apereo.cas.util.http.HttpMessageTests;
import org.apereo.cas.util.http.SimpleHttpClientTests;
import org.apereo.cas.util.io.CommunicationsManagerTests;
import org.apereo.cas.util.io.GroovySmsSenderTests;
import org.apereo.cas.util.io.RestfulSmsSenderTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllUtilityTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    JsonWebKeySetStringCipherExecutorTests.class,
    ProtocolTicketCipherExecutorTests.class,
    TicketGrantingCookieCipherExecutorTests.class,
    WebflowConversationStateCipherExecutorTests.class,
    RsaKeyPairCipherExecutorTests.class,
    HttpMessageTests.class,
    SimpleHttpClientTests.class,
    CommunicationsManagerTests.class,
    Base64RandomStringGeneratorTests.class,
    ChainingPrincipalNameTransformerTests.class,
    CollectionUtilsTests.class,
    CompressionUtilsTests.class,
    DateTimeUtilsTests.class,
    DefaultLongNumericGeneratorTests.class,
    DefaultRandomStringGeneratorTests.class,
    DefaultTicketCipherExecutorTests.class,
    EncodingUtilsTests.class,
    HexRandomStringGeneratorTests.class,
    RegexUtilsTests.class,
    BinaryCipherExecutorTests.class,
    ResourceUtilsTests.class,
    ScriptingUtilsTests.class,
    GroovySmsSenderTests.class,
    RestfulSmsSenderTests.class,
    RandomUtilsTests.class
})
public class AllUtilityTestsSuite {
}
