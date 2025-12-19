package org.apereo.cas.token.authentication;

import module java.base;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TokenAuthenticationSecurityTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Authentication")
class TokenAuthenticationSecurityTests {

    @Test
    void verifyRsaOperation() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL);
        registeredService.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());

        val signing = new DefaultRegisteredServiceProperty();
        signing.addValue("classpath:/RSA4096Private.key");
        registeredService.getProperties().put(RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName(), signing);

        val signAlg = new DefaultRegisteredServiceProperty();
        signAlg.addValue(JWSAlgorithm.RS512.getName());
        registeredService.getProperties().put(RegisteredServiceProperties.TOKEN_SECRET_SIGNING_ALG.getPropertyName(), signAlg);

        val encryption = new DefaultRegisteredServiceProperty();
        encryption.addValue("classpath:/RSA4096Public.key");
        registeredService.getProperties().put(RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION.getPropertyName(), encryption);

        val encAlg = new DefaultRegisteredServiceProperty();
        encAlg.addValue(JWEAlgorithm.RSA_OAEP_256.getName());
        registeredService.getProperties().put(RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_ALG.getPropertyName(), encAlg);

        val encMethod = new DefaultRegisteredServiceProperty();
        encMethod.addValue(EncryptionMethod.A256CBC_HS512.getName());
        registeredService.getProperties().put(RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_METHOD.getPropertyName(), encMethod);

        val security = TokenAuthenticationSecurity.forRegisteredService(registeredService);
        val token = security.toGenerator().generate(Map.of("sub", "casuser", "cn", "CAS"));
        val profile = security.toAuthenticator().validateToken(token);
        assertTrue(profile.containsAttribute("cn"));
    }
}
