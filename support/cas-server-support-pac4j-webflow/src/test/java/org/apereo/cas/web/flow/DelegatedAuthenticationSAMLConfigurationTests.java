package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;
import org.apereo.cas.web.flow.config.DelegatedAuthenticationSAMLConfiguration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationSAMLConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("CasConfiguration")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DelegatedAuthenticationSAMLConfigurationTests.SAMLTestConfiguration.class,
    DelegatedAuthenticationSAMLConfiguration.class,
    CasCoreHttpConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedAuthenticationSAMLConfigurationTests {
    @Autowired
    @Qualifier(DelegatedClientFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
    private SAMLMessageStoreFactory hazelcastSAMLMessageStoreFactory;

    @Test
    public void verifyOperation() {
        assertNotNull(hazelcastSAMLMessageStoreFactory);
    }

    @TestConfiguration
    public static class SAMLTestConfiguration {
        @Bean(destroyMethod = "shutdown")
        public HazelcastInstance casTicketRegistryHazelcastInstance() {
            val hz = new BaseHazelcastProperties();
            hz.getCluster().getCore().setInstanceName(UUID.randomUUID().toString());
            return HazelcastInstanceFactory.getOrCreateHazelcastInstance(HazelcastConfigurationFactory.build(hz));
        }
    }

}
