package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLdapRegisteredServiceMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Ldap")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultLdapRegisteredServiceMapperTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyIntegerIdOperation() {
        val mapper = new DefaultLdapRegisteredServiceMapper(casProperties.getServiceRegistry().getLdap(),
            new RegisteredServiceJsonSerializer(applicationContext));
        val id = String.format("^http://www.serviceid%s.org", RandomUtils.nextInt());
        val rs = RegisteredServiceTestUtils.getRegisteredService(id, CasRegisteredService.class);
        assertNotNull(mapper.mapFromRegisteredService(String.format("uid=%s,dc=example,dc=org", rs.getId()), rs));
    }
}
