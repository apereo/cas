package org.apereo.cas.token;

import org.apereo.cas.token.cipher.JwtTicketCipherExecutorTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTokenTicketTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    JwtTicketCipherExecutorTests.class,
    JwtTokenTicketBuilderTests.class,
    JwtTokenTicketBuilderWithoutCryptoTests.class,
    JwtTokenTicketBuilderWithoutEncryptionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTokenTicketTestsSuite {
}
