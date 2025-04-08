package org.apereo.cas.web.saml2;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DelegatedSaml2ClientMetadataJdbcTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class, properties = {
    "CasFeatureModule.DelegatedAuthentication.saml-jdbc.enabled=true",

    "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.auto-commit=true",
    "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.table-name=sp_metadata",
    "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.url=jdbc:postgresql://localhost:5432/saml",
    "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.user=postgres",
    "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.password=password",
    "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.driver-class=org.postgresql.Driver",
    "cas.authn.pac4j.saml[0].metadata.service-provider.jdbc.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatedSaml2ClientMetadataJdbcTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @BeforeAll
    public static void setUp() {
        var dataSource = JpaBeans.newDataSource("org.postgresql.Driver",
            "postgres", "password", "jdbc:postgresql://localhost:5432/saml");
        var template = new JdbcTemplate(dataSource);
        template.execute("DROP TABLE IF EXISTS sp_metadata");
        template.execute("CREATE TABLE sp_metadata (entityId VARCHAR(255), metadata TEXT)");
    }

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/metadata"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());

        assertNotNull(mockMvc.perform(get(DelegatedSaml2ClientMetadataController.BASE_ENDPOINT_SERVICE_PROVIDER + "/SAML2Client/metadata"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
    }
}
