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
 * This is {@link OAuth20AccessTokenAtHashGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
public class OAuth20AccessTokenAtHashGeneratorTests extends AbstractOAuth20Tests {
    @Test
    public void verifyNoneAlgorithm() {
        val hash = generateHashWithAlgorithm("none");
        assertEquals("QUJDRA", hash);
    }

    private String generateHashWithAlgorithm(final String alg) {
        val accessToken = getAccessToken();
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());

        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(registeredService)
            .service(accessToken.getService())
            .casProperties(casProperties)
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .build()
            .encode();

        return OAuth20AccessTokenAtHashGenerator.builder()
            .algorithm(alg)
            .registeredService(registeredService)
            .encodedAccessToken(encodedAccessToken)
            .build()
            .generate();
    }

    @Test
    public void verifySha512Algorithm() {
        val hash = generateHashWithAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA512);
        assertEquals("SexVvYP81ng449OFzoMWaeP4Faf0S3ql-NUrXUI1TEY", hash);
    }

    @Test
    public void verifySha256Algorithm() {
        val hash = generateHashWithAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA256);
        assertEquals("4S4RWs9FUrJWi1XpPL05OQ", hash);
    }

    @Test
    public void verifySha384Algorithm() {
        val hash = generateHashWithAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA384);
        assertEquals("bxfiOJnSNFoVa69p58Arvdo74Fc2eEnA", hash);
    }

    @Test
    public void verifyUnknownAlgorithm() {
        assertThrows(IllegalArgumentException.class, () -> generateHashWithAlgorithm("xyz"));
    }
}
