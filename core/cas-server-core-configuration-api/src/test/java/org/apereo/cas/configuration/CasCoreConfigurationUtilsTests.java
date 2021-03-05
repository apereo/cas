package org.apereo.cas.configuration;

import org.apereo.cas.configuration.model.core.authentication.AuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
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
    "cas.authn.attribute-repository.attribute-definition-store.json.location=file:/defn-test.json",
    "cas.authn.attribute-repository.ldap[0].ldap-url=ldap://localhost:1389"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
public class CasCoreConfigurationUtilsTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() {
        val map = CasCoreConfigurationUtils.asMap(casProperties.withHolder());

        assertEquals("https://sso.example.org", map.get("cas.server.name"));
        assertEquals("https://sso.example.org/cas", map.get("cas.server.prefix"));
        assertEquals("file:/defn-test.json", map.get("cas.authn.attribute-repository.attribute-definition-store.json.location"));
        assertEquals("ldap://localhost:1389", map.get("cas.authn.attribute-repository.ldap[0].ldap-url"));
    }

    @Test
    public void verifyMapping() {
        val props = new CasConfigurationProperties();
        props.getAuthn().getSyncope().setName("SyncopeAuth");
        props.getAuthn().getSyncope().setUrl("https://github.com/apereo/cas");
        props.getAuthn().getSyncope().setDomain("Master");

        val filters = new SimpleFilterProvider()
            .setFailOnUnknownId(false)
            .addFilter(CasConfigurationProperties.class.getSimpleName(), SimpleBeanPropertyFilter.filterOutAllExcept(
                CasCoreConfigurationUtils.getPropertyName(CasConfigurationProperties.class, CasConfigurationProperties::getAuthn)))
            .addFilter(AuthenticationProperties.class.getSimpleName(), SimpleBeanPropertyFilter.filterOutAllExcept(
                CasCoreConfigurationUtils.getPropertyName(AuthenticationProperties.class, AuthenticationProperties::getSyncope)));
        val map = CasCoreConfigurationUtils.asMap(props.withHolder(), filters);
        assertTrue(map.keySet().stream().allMatch(key -> key.startsWith("cas.authn.syncope")));
    }

    @Test
    public void verifyMappingCollections() {
        val props = new CasConfigurationProperties();
        val ldap = new LdapAuthenticationProperties();
        ldap.setLdapUrl("http://localhost:1234");
        ldap.setBaseDn("ou=example");
        ldap.setBindDn("admin-user");
        ldap.setBindCredential("admin-psw");
        props.getAuthn().getLdap().add(ldap);

        val filters = new SimpleFilterProvider()
            .setFailOnUnknownId(false)
            .addFilter(CasConfigurationProperties.class.getSimpleName(), SimpleBeanPropertyFilter.filterOutAllExcept(
                CasCoreConfigurationUtils.getPropertyName(CasConfigurationProperties.class, CasConfigurationProperties::getAuthn)))
            .addFilter(AuthenticationProperties.class.getSimpleName(), SimpleBeanPropertyFilter.filterOutAllExcept(
                CasCoreConfigurationUtils.getPropertyName(AuthenticationProperties.class, AuthenticationProperties::getLdap)));
        val map = CasCoreConfigurationUtils.asMap(props.withHolder(), filters);
        assertTrue(map.keySet().stream().allMatch(key -> key.startsWith("cas.authn.ldap")));
    }
}
