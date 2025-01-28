package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;

import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20TokenHashGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
class OAuth20TokenHashGeneratorTests extends AbstractOAuth20Tests {
    @Test
    void verifyNoneAlgorithm() {
        val hash = generateHashWithAlgorithm("none");
        assertEquals("QVQtMTIzNDU2", hash);
    }

    private String generateHashWithAlgorithm(final String alg) {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        val accessToken = getAccessToken("123456", registeredService.getServiceId(), registeredService.getClientId());

        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, registeredService,
            accessToken, accessToken.getService(), false).encode(accessToken.getId());
        return OAuth20TokenHashGenerator
            .builder()
            .algorithm(alg)
            .registeredService(registeredService)
            .token(encodedAccessToken)
            .build()
            .generate();
    }

    @Test
    void verifySha512Algorithm() {
        val hash = generateHashWithAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA512);
        assertEquals("EZsVCVYBY_zw4RAkJ3s759RxOZ8UingrP-52KQX4G6E", hash);
    }

    @Test
    void verifySha256Algorithm() {
        val hash = generateHashWithAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA256);
        assertEquals("IzG3xSUlcgF_gHDCvKd3fQ", hash);
    }

    @Test
    void verifySha384Algorithm() {
        val hash = generateHashWithAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA384);
        assertEquals("9Kb1tRRQ1YATHOB95-YAH3LHmnF2Lu7l", hash);
    }

    @Test
    void verifyUnknownAlgorithm() {
        assertThrows(IllegalArgumentException.class, () -> generateHashWithAlgorithm("xyz"));
    }
}
