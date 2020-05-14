package org.apereo.cas.configuration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreConfigurationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "cas.server.name=https://sso.example.org",
    "cas.server.prefix=https://sso.example.org/cas",
    "cas.person-directory.attribute-definition-store.json.location=file:/defn-test.json",
    "cas.authn.attribute-repository.ldap[0].ldap-url=ldap://localhost:1389"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class CasCoreConfigurationUtilsTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() {
        val map = CasCoreConfigurationUtils.asMap(casProperties.withHolder());
        assertEquals("https://sso.example.org", map.get("cas.server.name"));
        assertEquals("https://sso.example.org/cas", map.get("cas.server.prefix"));
        assertEquals("file:/defn-test.json", map.get("cas.personDirectory.attributeDefinitionStore.json.location"));
        assertEquals("ldap://localhost:1389", map.get("cas.authn.attributeRepository.ldap[0].ldapUrl"));
    }
}
