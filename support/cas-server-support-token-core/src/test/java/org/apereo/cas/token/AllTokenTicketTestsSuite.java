package org.apereo.cas.token;

import org.apereo.cas.token.cipher.TokenTicketCipherExecutorTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTokenTicketTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    TokenTicketCipherExecutorTests.class,
    JWTTokenTicketBuilderTests.class,
    JWTTokenTicketBuilderWithoutCryptoTests.class,
    JWTTokenTicketBuilderWithoutEncryptionTests.class
})
public class AllTokenTicketTestsSuite {
}
