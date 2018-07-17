package org.apereo.cas.token;

import org.apereo.cas.token.cipher.TokenTicketCipherExecutorTests;

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
    TokenTicketCipherExecutorTests.class,
    JWTTokenTicketBuilderTests.class,
    JWTTokenTicketBuilderWithoutCryptoTests.class,
    JWTTokenTicketBuilderWithoutEncryptionTests.class
})
public class AllTokenTicketTestsSuite {
}
