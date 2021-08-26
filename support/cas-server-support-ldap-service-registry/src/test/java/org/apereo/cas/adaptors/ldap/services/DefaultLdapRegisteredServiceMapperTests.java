package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

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
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DefaultLdapRegisteredServiceMapperTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyIntegerIdOperation() {
        val mapper = new DefaultLdapRegisteredServiceMapper(casProperties.getServiceRegistry().getLdap());
        val id = String.format("^http://www.serviceid%s.org", RandomUtils.nextInt());
        val rs = RegisteredServiceTestUtils.getRegisteredService(id, RegexRegisteredService.class);
        assertNotNull(mapper.mapFromRegisteredService(String.format("uid=%s,dc=example,dc=org", rs.getId()), rs));
    }
}
