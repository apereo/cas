package org.apereo.cas.token;

import org.apereo.cas.token.cipher.JwtTicketCipherExecutorTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTokenTicketTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    JwtTicketCipherExecutorTests.class,
    JWTTokenTicketBuilderTests.class,
    JWTTokenTicketBuilderWithoutCryptoTests.class,
    JWTTokenTicketBuilderWithoutEncryptionTests.class
})
public class AllTokenTicketTestsSuite {
}
