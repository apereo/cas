package org.apereo.cas.token.authentication;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.TokenAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This is {@link TokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreWebConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        TokenAuthenticationHandlerTests.TestTokenAuthenticationConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class, 
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        TokenAuthenticationConfiguration.class})
public class TokenAuthenticationHandlerTests {

    private static final RandomStringGenerator RANDOM_STRING_GENERATOR = new DefaultRandomStringGenerator();
    private static final String SIGNING_SECRET = RANDOM_STRING_GENERATOR.getNewString(256);
    private static final String ENCRYPTION_SECRET = RANDOM_STRING_GENERATOR.getNewString(48);

    @Autowired
    @Qualifier("tokenAuthenticationHandler")
    private AuthenticationHandler tokenAuthenticationHandler;

    @Test
    public void verifyKeysAreSane() throws Exception {
        final JwtGenerator<CommonProfile> g = new JwtGenerator<>();
        g.setSignatureConfiguration(new SecretSignatureConfiguration(SIGNING_SECRET, JWSAlgorithm.HS256));
        g.setEncryptionConfiguration(new SecretEncryptionConfiguration(ENCRYPTION_SECRET,
                JWEAlgorithm.DIR, EncryptionMethod.A192CBC_HS384));

        final CommonProfile profile = new CommonProfile();
        profile.setId("casuser");
        final String token = g.generate(profile);
        final TokenCredential c = new TokenCredential(token, RegisteredServiceTestUtils.getService());
        final HandlerResult result = this.tokenAuthenticationHandler.authenticate(c);
        assertNotNull(result);
        assertEquals(result.getPrincipal().getId(), profile.getId());
    }

    @Configuration("TokenAuthenticationTests")
    public static class TestTokenAuthenticationConfiguration {
        @Bean
        public List inMemoryRegisteredServices() {
            final AbstractRegisteredService svc = RegisteredServiceTestUtils.getRegisteredService(".*");
            svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

            DefaultRegisteredServiceProperty p = new DefaultRegisteredServiceProperty();
            p.addValue(SIGNING_SECRET);
            svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName(), p);

            p = new DefaultRegisteredServiceProperty();
            p.addValue(ENCRYPTION_SECRET);
            svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION.getPropertyName(), p);

            final List l = new ArrayList();
            l.add(svc);
            return l;
        }
    }

}
