package org.apereo.cas.token;

import org.apereo.cas.token.cipher.JwtTicketCipherExecutorTests;

import org.junit.platform.suite.api.SelectClasses;

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
public class AllTokenTicketTestsSuite {
}
