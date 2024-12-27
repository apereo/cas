package org.apereo.cas.web.saml2;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationSaml2ConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("CasConfiguration")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    DelegatedAuthenticationSaml2ConfigurationTests.SAMLTestConfiguration.class,
    BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class
})
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedAuthenticationSaml2ConfigurationTests {
    @Autowired
    @Qualifier(DelegatedIdentityProviderFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
    private SAMLMessageStoreFactory hazelcastSAMLMessageStoreFactory;

    @Test
    void verifyOperation() {
        assertNotNull(hazelcastSAMLMessageStoreFactory);
    }

    @TestConfiguration(value = "SAMLTestConfiguration", proxyBeanMethods = false)
    static class SAMLTestConfiguration {
        @Bean(destroyMethod = "shutdown")
        public HazelcastInstance casTicketRegistryHazelcastInstance() {
            val hz = new BaseHazelcastProperties();
            hz.getCluster().getCore().setInstanceName(UUID.randomUUID().toString());
            return HazelcastInstanceFactory.getOrCreateHazelcastInstance(HazelcastConfigurationFactory.build(hz));
        }
    }

}
