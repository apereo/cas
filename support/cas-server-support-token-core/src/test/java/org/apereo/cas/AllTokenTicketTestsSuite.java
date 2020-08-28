package org.apereo.cas;

import org.apereo.cas.token.JwtTokenCipherSigningPublicKeyEndpointTests;
import org.apereo.cas.token.JwtTokenTicketBuilderTests;
import org.apereo.cas.token.JwtTokenTicketBuilderWithoutCryptoTests;
import org.apereo.cas.token.JwtTokenTicketBuilderWithoutEncryptionTests;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutorTests;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutorTests;

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
    JwtTokenCipherSigningPublicKeyEndpointTests.class,
    JwtTokenTicketBuilderTests.class,
    RegisteredServiceJwtTicketCipherExecutorTests.class,
    JwtTokenTicketBuilderWithoutCryptoTests.class,
    JwtTokenTicketBuilderWithoutEncryptionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTokenTicketTestsSuite {
}
