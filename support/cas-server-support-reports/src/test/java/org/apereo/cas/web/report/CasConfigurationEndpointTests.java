package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.SimplePropertySource;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.JsonUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasConfigurationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("ActuatorEndpoint")
@ImportAutoConfiguration(CasCoreEnvironmentBootstrapAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
    CasConfigurationEndpointTests.CasConfigurationTestConfiguration.class,
    AbstractCasEndpointTests.SharedTestConfiguration.class
},
    properties = {
        "cas.standalone.configuration-security.psw=Q7M9w4NjYnBxb2#mW",
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.casConfig.access=UNRESTRICTED",
        "management.endpoint.refresh.access=UNRESTRICTED"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CasConfigurationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("casServerPrefix")
    private ObjectProvider<CasServerPrefix> casServerPrefix;

    @Autowired
    @Qualifier("simplePropertySource")
    private PropertySource simplePropertySource;

    @Autowired
    private ConfigurableEnvironment environment;

    @Test
    void verifyEncryptionDecryption() throws Throwable {
        val value = UUID.randomUUID().toString();
        val encrypted = mockMvc.perform(post("/actuator/casConfig/encrypt")
                .content(value)
                .contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        val decrypted = mockMvc.perform(post("/actuator/casConfig/decrypt")
                .content(encrypted)
                .contentType(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertEquals(decrypted, value);
    }

    
    @Test
    void verifyPropertyUpdate() throws Throwable {
        assertEquals("https://cas.example.org:8443/cas", casServerPrefix.getObject().prefix());
        var results = JsonUtils.parse(mockMvc.perform(post("/actuator/casConfig/update")
                .content(JsonUtils.render(List.of(
                    Map.of(
                        "name", "cas.server.prefix",
                        "value", "https://sso.apereo.org/cas",
                        "propertySource", simplePropertySource.getName()
                    ),
                    Map.of(
                        "name", "cas.server.tomcat.http[].enabled",
                        "value", "true",
                        "propertySource", simplePropertySource.getName()
                    )
                )))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), List.class);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("https://cas.example.org:8443/cas", casServerPrefix.getObject().prefix());


        results = JsonUtils.parse(mockMvc.perform(post("/actuator/casConfig/retrieve")
                .content(JsonUtils.render(
                    Map.of(
                        "name", "cas.server.prefix",
                        "value", "https://sso.apereo.org/cas",
                        "propertySource", simplePropertySource.getName()
                    )
                ))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), List.class);
        assertNotNull(results);
        assertFalse(results.isEmpty());

        mockMvc.perform(post("/actuator/refresh")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        assertEquals("https://sso.apereo.org/cas", casServerPrefix.getObject().prefix());

        mockMvc.perform(delete("/actuator/casConfig")
                .content(JsonUtils.render(
                    Map.of(
                        "propertySource", simplePropertySource.getName()
                    )
                ))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    void verifyIndexedPropertyGroupUpdate() throws Throwable {
        val source = new SimplePropertySource();
        environment.getPropertySources().addFirst(source);
        try {
            val firstRepository = List.of(
                Map.of(
                    "name", "cas.authn.attribute-repository.ldap[].ldap-url",
                    "value", "ldap://localhost:10389",
                    "propertySource", source.getName()
                ),
                Map.of(
                    "name", "cas.authn.attribute-repository.ldap[].base-dn",
                    "value", "ou=people,dc=example,dc=org",
                    "propertySource", source.getName()
                ),
                Map.of(
                    "name", "cas.authn.attribute-repository.ldap[].attributes.uid",
                    "value", "uid",
                    "propertySource", source.getName()
                )
            );
            mockMvc.perform(post("/actuator/casConfig/update")
                    .content(JsonUtils.render(firstRepository))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

            assertEquals("ldap://localhost:10389",
                source.getProperty("cas.authn.attribute-repository.ldap[0].ldap-url"));
            assertEquals("ou=people,dc=example,dc=org",
                source.getProperty("cas.authn.attribute-repository.ldap[0].base-dn"));
            assertEquals("uid", source.getProperty("cas.authn.attribute-repository.ldap[0].attributes.uid"));
            assertNull(source.getProperty("cas.authn.attribute-repository.ldap[].ldap-url"));

            val secondRepository = List.of(
                Map.of(
                    "name", "cas.authn.attribute-repository.ldap[].ldap-url",
                    "value", "ldap://localhost:20389",
                    "propertySource", source.getName()
                ),
                Map.of(
                    "name", "cas.authn.attribute-repository.ldap[].base-dn",
                    "value", "ou=staff,dc=example,dc=org",
                    "propertySource", source.getName()
                )
            );
            mockMvc.perform(post("/actuator/casConfig/update")
                    .content(JsonUtils.render(secondRepository))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

            assertEquals("ldap://localhost:20389",
                source.getProperty("cas.authn.attribute-repository.ldap[1].ldap-url"));
            assertEquals("ou=staff,dc=example,dc=org",
                source.getProperty("cas.authn.attribute-repository.ldap[1].base-dn"));

            val jdbcRepository = List.of(
                Map.of(
                    "name", "cas.authn.attribute-repository.jdbc[].single-row",
                    "value", "false",
                    "propertySource", source.getName()
                ),
                Map.of(
                    "name", "cas.authn.attribute-repository.jdbc[].url",
                    "value", "jdbc:mysql://localhost:3306/cas",
                    "propertySource", source.getName()
                ),
                Map.of(
                    "name", "cas.authn.attribute-repository.jdbc[].attributes.firstname",
                    "value", "first-name",
                    "propertySource", source.getName()
                ),
                Map.of(
                    "name", "cas.authn.attribute-repository.jdbc[].column-mappings.attrname",
                    "value", "attrvalue",
                    "propertySource", source.getName()
                )
            );
            mockMvc.perform(post("/actuator/casConfig/update")
                    .content(JsonUtils.render(jdbcRepository))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

            assertEquals("false", source.getProperty("cas.authn.attribute-repository.jdbc[0].single-row"));
            assertEquals("jdbc:mysql://localhost:3306/cas",
                source.getProperty("cas.authn.attribute-repository.jdbc[0].url"));
            assertEquals("first-name",
                source.getProperty("cas.authn.attribute-repository.jdbc[0].attributes.firstname"));
            assertEquals("attrvalue",
                source.getProperty("cas.authn.attribute-repository.jdbc[0].column-mappings.attrname"));
            assertNull(source.getProperty("cas.authn.attribute-repository.jdbc[].single-row"));
        } finally {
            environment.getPropertySources().remove(source.getName());
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class CasConfigurationTestConfiguration {

        @Bean
        @Qualifier("simplePropertySource")
        public PropertySource simplePropertySource() {
            return new SimplePropertySource();
        }

        @Bean
        public InitializingBean simplePropertySourceLocator(
            @Qualifier("simplePropertySource") final PropertySource simplePropertySource,
            final ConfigurableEnvironment environment) {
            return () -> environment.getPropertySources().addFirst(simplePropertySource);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasServerPrefix casServerPrefix(final CasConfigurationProperties casProperties) {
            return new CasServerPrefix(casProperties.getServer().getPrefix());
        }
    }

    public record CasServerPrefix(String prefix) {
    }
}
