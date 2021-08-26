package org.apereo.cas;

import org.apereo.cas.token.JwtBuilderTests;
import org.apereo.cas.token.JwtTokenCipherSigningPublicKeyEndpointTests;
import org.apereo.cas.token.JwtTokenTicketBuilderTests;
import org.apereo.cas.token.JwtTokenTicketBuilderWithoutCryptoTests;
import org.apereo.cas.token.JwtTokenTicketBuilderWithoutEncryptionTests;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutorTests;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTokenTicketTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    JwtTicketCipherExecutorTests.class,
    JwtTokenCipherSigningPublicKeyEndpointTests.class,
    JwtTokenTicketBuilderTests.class,
    JwtBuilderTests.class,
    RegisteredServiceJwtTicketCipherExecutorTests.class,
    JwtTokenTicketBuilderWithoutCryptoTests.class,
    JwtTokenTicketBuilderWithoutEncryptionTests.class
})
@Suite
public class AllTokenTicketTestsSuite {
}
